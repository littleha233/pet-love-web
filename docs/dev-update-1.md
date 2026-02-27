1. **短信登录模块 PRD（MVP）**
2. **MySQL 表结构 SQL（Flyway）**
3. **错误码清单**

这版默认你主要面向中国用户，方案采用：**短信验证码登录 + 自动注册**。

---

# 一、短信登录模块 PRD（给 Codex）

## 1）模块目标

为当前项目新增 **手机号短信验证码登录** 能力（MVP）：

* 用户输入手机号 + 验证码即可登录
* 若手机号不存在，则 **自动注册并登录**
* 支持短信发送限流、防刷、验证码状态管理
* 接入你第 6 阶段的风控（黑名单 / 风险拦截）

---

## 2）范围与边界

### ✅ 本期做

* 短信验证码发送（`LOGIN` 场景）
* 手机号验证码登录（自动注册）
* 验证码记录与发送日志落库
* 发送频控（手机号/IP）
* 验证码错误次数限制、过期、一次性使用
* 接入 `RiskGuard`（至少 `AUTH_SMS_SEND`，可选 `AUTH_LOGIN_MOBILE`）

### ❌ 本期不做

* 手机号+密码登录
* 图形验证码（但预留 `captchaToken` 字段）
* 第三方登录（微信/Apple）
* 多通道短信故障切换（先接一个 provider）
* 找回密码（后续复用验证码体系）

---

## 3）用户流程（Web 端）

### 流程 A：发送验证码

1. 用户输入手机号
2. 点击“发送验证码”
3. 前端调用 `POST /api/v1/auth/sms/send`
4. 后端校验手机号格式、频控、风控
5. 后端生成 6 位验证码（仅存 hash）
6. 调用短信供应商发送
7. 返回成功 + 倒计时秒数（如 60）

### 流程 B：验证码登录（自动注册）

1. 用户输入手机号 + 验证码
2. 前端调用 `POST /api/v1/auth/login/mobile`
3. 后端校验验证码（存在、未过期、未使用、未超尝试次数）
4. 校验通过后将验证码标记为 `VERIFIED`
5. 查用户：

    * 存在 → 登录
    * 不存在 → 自动创建账号并登录
6. 返回 `accessToken / refreshToken / userInfo / isNewUser`

---

## 4）接口设计（后端 API）

---

### 4.1 发送验证码

**POST** `/api/v1/auth/sms/send`

#### Request DTO：`SendSmsCodeRequest`

* `mobile` (string, required)
* `bizType` (string, required, 固定 `LOGIN`)
* `captchaToken` (string, optional, 预留)
* `deviceId` (string, optional)

#### Response DTO：`SendSmsCodeResponse`

* `success` (boolean)
* `cooldownSeconds` (int)  // 建议返回 60
* `traceId` (string, optional)

#### 业务规则

* 手机号格式校验（中国大陆手机号）
* 频控：

    * 同手机号 **60 秒**内仅允许发送 1 次
    * 同手机号 **24 小时内最多 10 次**
    * 同 IP **24 小时内最多 30 次**
* 风控：

    * 调用 `RiskGuard.checkUserAction(...)`
    * 建议 actionKey：`AUTH_SMS_SEND`
    * 命中黑名单则拦截
* 验证码规则：

    * 6 位数字
    * 有效期 5 分钟
    * 仅保存 `code_hash`，不保存明文验证码
* 短信发送成功后写：

    * `sms_code_records`
    * `sms_send_logs`
* 短信发送失败也要写 `sms_send_logs`

---

### 4.2 手机号验证码登录（自动注册）

**POST** `/api/v1/auth/login/mobile`

#### Request DTO：`MobileCodeLoginRequest`

* `mobile` (string, required)
* `code` (string, required)
* `deviceId` (string, optional)

#### Response DTO：`MobileCodeLoginResponse`

* `accessToken` (string)
* `refreshToken` (string)
* `expiresIn` (long)
* `isNewUser` (boolean)
* `userInfo`

    * `userId` (long)
    * `nickname` (string)
    * `avatarUrl` (string, nullable)
    * `mobileMasked` (string)
    * `realNameVerified` (boolean, 如果你已有实名状态可返回)

#### 业务规则

* 校验验证码记录：

    * `mobile + bizType=LOGIN`
    * 取最近一条 `status=SENT`
    * 未过期、未超过最大尝试次数
* 验证码错误时：

    * `attempt_count + 1`
    * 达到 `max_attempts` 则 `status -> FAILED`
* 验证成功时：

    * `status -> VERIFIED`
    * `verified_at = now`
    * 一次性使用
* 用户登录：

    * 若用户存在（按 `mobile` 查询）→ 登录
    * 若不存在 → 自动创建用户：

        * `mobile`
        * `mobile_verified_at = now`
        * `register_channel = MOBILE_SMS`
        * `last_login_at = now`
        * 默认昵称（如 `用户xxxx`）
* Token 签发复用你现有 auth/token 体系
* 可选风控 actionKey：`AUTH_LOGIN_MOBILE`

---

## 5）验证码状态机（必须后端严格控制）

`sms_code_records.status`：

* `SENT`
* `VERIFIED`
* `EXPIRED`
* `FAILED`

### 合法流转

* `null -> SENT`
* `SENT -> VERIFIED`（验证码正确）
* `SENT -> EXPIRED`（过期）
* `SENT -> FAILED`（尝试次数超限）
* （可选）短信发送失败直接记录为 `FAILED`

### 规则

* 有效期：`5 分钟`
* 最大尝试次数：`5 次`
* 验证成功后立即失效（一次性）
* 后端负责过期判断（不依赖定时任务）

---

## 6）安全与风控要求（MVP 必做）

### 6.1 验证码存储

* 不保存明文验证码
* 使用 `SHA-256` / `HMAC-SHA256` 存 `code_hash`
* 建议哈希输入：`pepper + mobile + bizType + code`

### 6.2 频控（必须）

* 同手机号 60 秒内 1 次
* 同手机号 24h 最多 10 次
* 同 IP 24h 最多 30 次

### 6.3 风控接入（复用 Phase 6）

* `AUTH_SMS_SEND`
* `AUTH_LOGIN_MOBILE`（可选）
* 黑名单命中时返回统一错误码（见错误码清单）

### 6.4 日志

* 短信发送请求/响应要落库（`sms_send_logs`）
* 不要在日志里打印明文验证码

---

## 7）后端模块建议目录（Codex 实现参考）

```text
services/api-server/src/main/java/com/petplatform/modules/auth/
├─ controller/
│  ├─ AuthSmsController.java
│  └─ AuthMobileLoginController.java
├─ dto/
│  ├─ SendSmsCodeRequest.java
│  ├─ SendSmsCodeResponse.java
│  ├─ MobileCodeLoginRequest.java
│  └─ MobileCodeLoginResponse.java
├─ service/
│  ├─ SmsCodeService.java
│  ├─ MobileAuthService.java
│  ├─ SmsProviderClient.java                // 抽象接口
│  ├─ impl/
│  │  ├─ SmsCodeServiceImpl.java
│  │  ├─ MobileAuthServiceImpl.java
│  │  └─ AliyunSmsProviderClient.java       // 中国用户优先，先做一个实现
├─ entity/
│  ├─ SmsCodeRecordEntity.java
│  └─ SmsSendLogEntity.java
├─ repository/
│  ├─ SmsCodeRecordRepository.java
│  └─ SmsSendLogRepository.java
└─ enums/
   ├─ SmsBizType.java
   ├─ SmsCodeStatus.java
   └─ SmsSendStatus.java
```

> `AliyunSmsProviderClient` 只是示例命名，你也可以让 Codex 做成 `ChinaSmsProviderClient` 再接具体厂商 SDK。

---

## 8）配置项（application-prod.yml / env）

建议让 Codex 增加配置：

* `auth.sms.enabled` = true
* `auth.sms.provider` = `aliyun`（或你选的厂商 key）
* `auth.sms.sign-name`
* `auth.sms.template.login-code`
* `auth.sms.code.ttl-seconds` = 300
* `auth.sms.code.length` = 6
* `auth.sms.cooldown-seconds` = 60
* `auth.sms.daily-limit.per-mobile` = 10
* `auth.sms.daily-limit.per-ip` = 30
* `auth.sms.max-attempts` = 5
* `auth.sms.hash-pepper` = （生产环境从 env 读取）

---

## 9）验收标准（给 Codex）

* [ ] 能发送短信验证码（登录场景）
* [ ] 发送频率限制生效（手机号/IP）
* [ ] 验证码仅保存 hash，不存明文
* [ ] 验证码过期、错误次数限制生效
* [ ] 手机号登录成功（已有账号）
* [ ] 手机号自动注册并登录成功（新账号）
* [ ] `sms_code_records` 和 `sms_send_logs` 落库正常
* [ ] 命中黑名单时能被拦截（至少发码场景）
* [ ] 错误码与前端提示可用

---

# 二、MySQL 表结构 SQL（Flyway）

> 下面给你按 Flyway 脚本拆成 3 个文件。
> **注意**：默认你当前用户表叫 `users`，主键为 `id`。如果你项目实际表名/字段名不同，让 Codex按实际表结构改。

---

## `V28__alter_users_add_mobile_fields.sql`

```sql
-- 为 users 表增加手机号登录相关字段（如已有同名字段，请按实际情况调整脚本）
ALTER TABLE `users`
    ADD COLUMN `mobile` VARCHAR(32) NULL COMMENT '手机号（中国大陆）' AFTER `id`,
    ADD COLUMN `mobile_verified_at` DATETIME(3) NULL COMMENT '手机号验证通过时间' AFTER `mobile`,
    ADD COLUMN `register_channel` VARCHAR(32) NULL COMMENT '注册渠道：MOBILE_SMS/EMAIL/WECHAT...' AFTER `mobile_verified_at`,
    ADD COLUMN `last_login_at` DATETIME(3) NULL COMMENT '最后登录时间' AFTER `register_channel`;

-- 手机号唯一索引（允许多个NULL）
CREATE UNIQUE INDEX `uk_users_mobile` ON `users` (`mobile`);

-- 常用查询索引（可选）
CREATE INDEX `idx_users_last_login_at` ON `users` (`last_login_at`);
```

---

## `V29__create_sms_code_records.sql`

```sql
CREATE TABLE `sms_code_records` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `mobile` VARCHAR(32) NOT NULL COMMENT '手机号',
    `biz_type` VARCHAR(32) NOT NULL COMMENT '业务类型：LOGIN/BIND_MOBILE/RESET_PASSWORD',
    `code_hash` VARCHAR(128) NOT NULL COMMENT '验证码哈希（不存明文）',
    `status` VARCHAR(16) NOT NULL COMMENT '状态：SENT/VERIFIED/EXPIRED/FAILED',

    `provider` VARCHAR(32) NULL COMMENT '短信供应商标识',
    `provider_template_code` VARCHAR(64) NULL COMMENT '短信模板编码',
    `provider_message_id` VARCHAR(128) NULL COMMENT '供应商消息ID',

    `expire_at` DATETIME(3) NOT NULL COMMENT '验证码过期时间',
    `verified_at` DATETIME(3) NULL COMMENT '验证码验证成功时间',

    `attempt_count` INT NOT NULL DEFAULT 0 COMMENT '验证码校验失败次数',
    `max_attempts` INT NOT NULL DEFAULT 5 COMMENT '最大尝试次数',

    `client_ip` VARCHAR(64) NULL COMMENT '客户端IP',
    `device_id` VARCHAR(128) NULL COMMENT '设备ID（前端可传）',

    `created_at` DATETIME(3) NOT NULL COMMENT '创建时间',
    `updated_at` DATETIME(3) NOT NULL COMMENT '更新时间',

    PRIMARY KEY (`id`),

    KEY `idx_sms_code_mobile_biz_status_created` (`mobile`, `biz_type`, `status`, `created_at`),
    KEY `idx_sms_code_mobile_created` (`mobile`, `created_at`),
    KEY `idx_sms_code_ip_created` (`client_ip`, `created_at`),
    KEY `idx_sms_code_expire_at` (`expire_at`),
    KEY `idx_sms_code_status_created` (`status`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='短信验证码记录表';
```

---

## `V30__create_sms_send_logs.sql`

```sql
CREATE TABLE `sms_send_logs` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `mobile` VARCHAR(32) NOT NULL COMMENT '手机号',
    `biz_type` VARCHAR(32) NOT NULL COMMENT '业务类型：LOGIN/BIND_MOBILE/RESET_PASSWORD',

    `provider` VARCHAR(32) NOT NULL COMMENT '短信供应商标识',
    `template_code` VARCHAR(64) NULL COMMENT '短信模板编码',
    `sign_name` VARCHAR(64) NULL COMMENT '短信签名',

    `request_payload` JSON NULL COMMENT '请求参数（脱敏后）',
    `response_payload` JSON NULL COMMENT '供应商响应',
    `send_status` VARCHAR(16) NOT NULL COMMENT '发送状态：SUCCESS/FAILED',
    `error_code` VARCHAR(64) NULL COMMENT '供应商错误码',
    `error_message` VARCHAR(255) NULL COMMENT '错误信息',

    `provider_message_id` VARCHAR(128) NULL COMMENT '供应商消息ID',
    `client_ip` VARCHAR(64) NULL COMMENT '客户端IP',
    `device_id` VARCHAR(128) NULL COMMENT '设备ID（前端可传）',

    `created_at` DATETIME(3) NOT NULL COMMENT '创建时间',

    PRIMARY KEY (`id`),

    KEY `idx_sms_send_logs_mobile_created` (`mobile`, `created_at`),
    KEY `idx_sms_send_logs_biz_created` (`biz_type`, `created_at`),
    KEY `idx_sms_send_logs_status_created` (`send_status`, `created_at`),
    KEY `idx_sms_send_logs_provider_created` (`provider`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='短信发送日志表';
```

---

## （可选）`V31__seed_sms_auth_system_configs.sql`

如果你有 `system_configs` 表并且想把默认配置种进去，可以再让 Codex补这个脚本。
没有的话，先走 `application.yml + env` 即可。

---

# 三、错误码清单（短信登录模块）

下面是建议直接加入你项目统一错误码枚举的清单（Codex 可直接落地）。

---

## 1）发送验证码相关

### `AUTH_MOBILE_INVALID`

* 含义：手机号格式不合法
* 场景：发送验证码 / 登录时手机号校验失败

### `AUTH_SMS_BIZ_TYPE_INVALID`

* 含义：短信业务类型非法
* 场景：`bizType` 非系统支持值（本期只支持 `LOGIN`）

### `AUTH_SMS_SEND_TOO_FREQUENT`

* 含义：发送太频繁（冷却时间未到）
* 场景：同手机号 60 秒内重复发送

### `AUTH_SMS_MOBILE_DAILY_LIMIT_EXCEEDED`

* 含义：手机号当日发送次数超限
* 场景：同手机号超过 10 次/日

### `AUTH_SMS_IP_DAILY_LIMIT_EXCEEDED`

* 含义：IP 当日发送次数超限
* 场景：同 IP 超过 30 次/日

### `AUTH_SMS_PROVIDER_SEND_FAILED`

* 含义：短信网关发送失败
* 场景：第三方短信供应商返回错误

### `AUTH_SMS_CAPTCHA_REQUIRED`

* 含义：需要图形验证码/行为验证（预留）
* 场景：后续风控升级时使用

### `AUTH_SMS_CAPTCHA_INVALID`

* 含义：图形验证码/行为验证失败（预留）

---

## 2）验证码校验相关

### `AUTH_SMS_CODE_NOT_FOUND`

* 含义：未找到可用验证码记录
* 场景：手机号从未发送过，或没有 `SENT` 状态的有效记录

### `AUTH_SMS_CODE_EXPIRED`

* 含义：验证码已过期
* 场景：超过 5 分钟有效期

### `AUTH_SMS_CODE_INCORRECT`

* 含义：验证码错误
* 场景：输入验证码与 `code_hash` 不匹配

### `AUTH_SMS_CODE_ATTEMPTS_EXCEEDED`

* 含义：验证码错误次数已超限
* 场景：达到 `max_attempts`（如 5 次）

### `AUTH_SMS_CODE_ALREADY_USED`

* 含义：验证码已被使用
* 场景：状态已是 `VERIFIED`

### `AUTH_SMS_CODE_STATUS_INVALID`

* 含义：验证码状态非法
* 场景：状态不是可校验状态（如 `FAILED/EXPIRED`）

---

## 3）登录与用户相关

### `AUTH_LOGIN_MOBILE_BLOCKED`

* 含义：手机号登录被风控拦截
* 场景：命中 `RiskGuard`，action=`AUTH_LOGIN_MOBILE`

### `AUTH_SMS_SEND_BLOCKED`

* 含义：发短信被风控拦截
* 场景：命中 `RiskGuard`，action=`AUTH_SMS_SEND`

### `AUTH_TOKEN_ISSUE_FAILED`

* 含义：Token 签发失败
* 场景：账号登录成功后签发 token 异常（兜底）

### `AUTH_USER_CREATE_FAILED`

* 含义：自动注册用户失败
* 场景：验证码通过后创建用户异常（DB/并发）

---

## 4）文件/日志（可选兜底）

### `AUTH_SMS_LOG_WRITE_FAILED`（可选，不建议对前端暴露）

* 含义：短信日志写入失败
* 场景：内部日志异常（通常记录日志并不中断主流程，视你实现而定）

---

# 四、给 Codex 的执行提示词（可直接复制）

```text
实现“手机号短信验证码登录（自动注册）”模块，数据库使用 MySQL，主要面向中国用户。

【目标】
- 用户输入手机号 + 验证码即可登录
- 若手机号不存在，则自动创建账号并登录
- 支持发送验证码、验证码校验、频控、防刷、日志记录
- 接入现有 Phase6 风控（RiskGuard）

【接口】
1) POST /api/v1/auth/sms/send
   入参:
   - mobile
   - bizType=LOGIN
   - captchaToken（预留）
   - deviceId（可选）

   规则:
   - 校验中国大陆手机号格式
   - 频控：
     - 同手机号60秒1次
     - 同手机号24小时最多10次
     - 同IP24小时最多30次
   - 调用 RiskGuard（actionKey=AUTH_SMS_SEND），命中则拦截
   - 生成6位验证码（仅保存hash，不保存明文）
   - 有效期5分钟
   - 调用短信Provider发送
   - 写入 sms_code_records 和 sms_send_logs
   - 返回 cooldownSeconds

2) POST /api/v1/auth/login/mobile
   入参:
   - mobile
   - code
   - deviceId（可选）

   规则:
   - 校验验证码记录存在、未过期、状态可用、未超最大尝试次数
   - 验证码错误则 attempt_count +1，达到上限后 status=FAILED
   - 验证成功后 status=VERIFIED（一次性）
   - 可选调用 RiskGuard（actionKey=AUTH_LOGIN_MOBILE）
   - 查询 users.mobile：
     - 存在 -> 登录
     - 不存在 -> 自动注册（register_channel=MOBILE_SMS）
   - 更新 users.mobile_verified_at / users.last_login_at
   - 返回 accessToken / refreshToken / userInfo / isNewUser

【数据库（MySQL + Flyway）】
请按以下迁移实现（如 users 表字段已存在，按实际表结构调整）：
- V28__alter_users_add_mobile_fields.sql
- V29__create_sms_code_records.sql
- V30__create_sms_send_logs.sql

字段要求：
- users 新增 mobile（唯一）、mobile_verified_at、register_channel、last_login_at
- sms_code_records:
  mobile, biz_type, code_hash, status, expire_at, verified_at,
  attempt_count, max_attempts, provider, provider_message_id, client_ip, device_id, created_at, updated_at
- sms_send_logs:
  provider请求/响应日志（JSON），send_status，error_code，error_message

【状态机】
sms_code_records.status:
- SENT
- VERIFIED
- EXPIRED
- FAILED

规则:
- 有效期5分钟
- 最大尝试5次
- 验证成功即失效（一次性）

【短信Provider实现】
- 抽象接口 SmsProviderClient
- 先实现一个中国短信服务商客户端（例如阿里云短信）
- 配置从 application yml + env 读取：
  - auth.sms.provider
  - auth.sms.sign-name
  - auth.sms.template.login-code
  - auth.sms.hash-pepper
  - auth.sms.cooldown-seconds
  - auth.sms.daily-limit.per-mobile
  - auth.sms.daily-limit.per-ip
  - auth.sms.max-attempts
  - auth.sms.code.ttl-seconds

【安全要求】
- 严禁存储明文验证码
- 严禁日志打印明文验证码
- 错误码清晰可前端展示
- 所有短信发送失败原因必须落库（sms_send_logs）

【前端（Web）】
- 新增手机号验证码登录页面/弹窗
- 包含手机号输入、验证码输入、发送验证码按钮（倒计时）
- 按钮文案：“验证码登录 / 自动注册”
- 对错误码做友好提示（发送频繁/验证码错误/验证码过期等）

【验收】
- 发送验证码可用，频控生效
- 验证码登录可用（已有账号）
- 自动注册登录可用（新账号）
- 风控拦截可用（AUTH_SMS_SEND 至少）
- 表数据落库正确（sms_code_records / sms_send_logs）
```

---

# 五、下一步最省事的落地顺序（建议）

1. **先让 Codex 落地后端 + Flyway（不接真实短信）**
   用“控制台打印验证码”的 Mock Provider 跑通链路（仅本地开发）

2. **再接真实短信 Provider（中国通道）**
   把 `SmsProviderClient` 的实现替换成真实网关调用

3. **最后上前端登录页**
   先跑通“验证码登录/自动注册”，样式后面再优化

---