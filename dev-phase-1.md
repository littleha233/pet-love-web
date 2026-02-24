很好，下面这版我按 **“Codex 可直接执行”** 的格式给你，聚焦 **Phase 0（工程骨架与基础设施）**，不碰业务模块（领养/喂养/救助）实现。

---

# Phase 0 技术任务单（给 Codex）

## 0. 目标与边界（Phase 0）

### 本期目标

搭建一个可持续迭代的 Web MVP 工程底座，包含：

* Monorepo 项目结构（前端 + 后端）
* 后端基础框架（鉴权、日志、错误码、上传、OpenAPI）
* 前端基础框架（用户端 Web + Admin 控制台壳）
* 数据库迁移体系（Flyway）
* 基础 RBAC（先做枚举/路由级）
* 文件上传能力（图片为主）
* 审计日志能力（Admin 操作留痕）
* 可运行的本地开发环境（Docker Compose）

### 本期不做

* 领养/送养业务
* 上门喂养业务
* 救助指引业务
* 在线支付
* 即时聊天
* 商城
* 复杂权限矩阵（数据库化权限可后续做）

---

# 1) 技术栈建议（默认方案）

> 这套方案对你（Java 背景）和 Codex 都比较友好。

## 后端

* **Java 21**
* **Spring Boot 3.x**
* **Maven**
* **Spring Security**（JWT）
* **Spring Validation**（DTO 校验）
* **Springdoc OpenAPI**（Swagger）
* **PostgreSQL 16**
* **Flyway**（数据库迁移）
* **Redis（可选）**（OTP、限流；Phase 0 可先内存实现）
* **MinIO**（本地文件存储，后续可切 S3/OSS）
* **Logback + MDC**（requestId）

## 前端（两个 Web）

* **React + TypeScript + Vite**
* **React Router**
* **TanStack Query**（请求缓存）
* **Axios**
* **Ant Design**（Admin 快速搭建）
* 用户端和 Admin 分成两个前端应用（便于边界清晰）

## 工程管理

* **Monorepo**
* **Docker Compose**（Postgres + MinIO + 可选 Redis）
* **.env** 配置分层（dev/test/prod）

---

# 2) 建议目录结构（Monorepo）

> 这是给 Codex 的标准目录约束，避免它乱放文件。

```text
pet-platform/
├─ README.md
├─ .gitignore
├─ .editorconfig
├─ docker-compose.yml
├─ .env.example
├─ docs/
│  ├─ phase0/
│  │  ├─ api-contract.md
│  │  ├─ error-codes.md
│  │  ├─ state-machines.md
│  │  └─ local-setup.md
│  └─ architecture/
│     ├─ system-overview.md
│     └─ security-baseline.md
├─ apps/
│  ├─ web-portal/                 # 用户端 Web（前台）
│  │  ├─ package.json
│  │  ├─ vite.config.ts
│  │  └─ src/
│  │     ├─ main.tsx
│  │     ├─ App.tsx
│  │     ├─ router/
│  │     ├─ pages/
│  │     │  ├─ Home/
│  │     │  ├─ Login/
│  │     │  └─ Profile/
│  │     ├─ components/
│  │     ├─ services/             # API client
│  │     ├─ store/
│  │     ├─ hooks/
│  │     ├─ types/
│  │     └─ utils/
│  └─ admin-console/              # 后台 Admin
│     ├─ package.json
│     ├─ vite.config.ts
│     └─ src/
│        ├─ main.tsx
│        ├─ App.tsx
│        ├─ router/
│        ├─ pages/
│        │  ├─ Login/
│        │  ├─ Dashboard/
│        │  ├─ Users/
│        │  └─ AuditLogs/
│        ├─ components/
│        ├─ services/
│        ├─ store/
│        ├─ hooks/
│        ├─ types/
│        └─ utils/
├─ services/
│  └─ api-server/                 # Spring Boot 后端
│     ├─ pom.xml
│     ├─ src/main/java/com/petplatform/
│     │  ├─ PetPlatformApplication.java
│     │  ├─ common/
│     │  │  ├─ api/               # ApiResponse / PageResponse
│     │  │  ├─ error/             # ErrorCode / BizException / handler
│     │  │  ├─ log/               # MDC / requestId filter
│     │  │  ├─ security/          # JWT / SecurityConfig / auth principal
│     │  │  ├─ validation/
│     │  │  └─ util/
│     │  ├─ config/               # OpenAPI, CORS, Jackson, MinIO
│     │  ├─ modules/
│     │  │  ├─ auth/
│     │  │  │  ├─ controller/
│     │  │  │  ├─ dto/
│     │  │  │  ├─ service/
│     │  │  │  ├─ entity/
│     │  │  │  ├─ repository/
│     │  │  │  └─ enums/
│     │  │  ├─ user/
│     │  │  │  ├─ controller/
│     │  │  │  ├─ dto/
│     │  │  │  ├─ service/
│     │  │  │  ├─ entity/
│     │  │  │  ├─ repository/
│     │  │  │  └─ enums/
│     │  │  ├─ file/
│     │  │  │  ├─ controller/
│     │  │  │  ├─ dto/
│     │  │  │  ├─ service/
│     │  │  │  ├─ entity/
│     │  │  │  ├─ repository/
│     │  │  │  └─ enums/
│     │  │  ├─ admin/
│     │  │  │  ├─ controller/
│     │  │  │  ├─ dto/
│     │  │  │  ├─ service/
│     │  │  │  ├─ entity/
│     │  │  │  ├─ repository/
│     │  │  │  └─ enums/
│     │  │  └─ system/
│     │  │     ├─ controller/     # health/meta/config
│     │  │     ├─ service/
│     │  │     └─ dto/
│     │  └─ infra/
│     │     ├─ persistence/       # base entity / mybatis or jpa config
│     │     ├─ storage/           # MinIO client wrapper
│     │     └─ cache/             # optional redis
│     ├─ src/main/resources/
│     │  ├─ application.yml
│     │  ├─ application-dev.yml
│     │  ├─ application-test.yml
│     │  └─ db/migration/
│     │     ├─ V1__init_core_tables.sql
│     │     ├─ V2__seed_admin_and_cities.sql
│     │     └─ V3__add_indexes.sql
│     └─ src/test/java/com/petplatform/
├─ packages/
│  ├─ shared-types/               # 可选：前后端共享枚举/类型（TS）
│  └─ eslint-config/              # 可选
└─ scripts/
   ├─ dev-start.sh
   ├─ dev-stop.sh
   └─ seed-local.sh
```

---

# 3) Phase 0 数据库最小表设计（先打底）

> 这里只建基础设施表，不建业务表（领养/喂养/救助放到后续 Phase）

## 3.1 核心表（Phase 0 必须）

1. `users`
2. `user_profiles`
3. `auth_otp_codes`
4. `auth_refresh_tokens`
5. `file_objects`
6. `admin_users`
7. `admin_audit_logs`
8. `cities`
9. `system_configs`（可选，但建议有）

---

## 3.2 建议字段（给 Codex 落表）

### `users`

* `id` (bigserial, pk)
* `mobile` (varchar, unique, nullable)
* `email` (varchar, unique, nullable)
* `password_hash` (varchar, nullable)
* `login_type` (varchar) // MOBILE_OTP / EMAIL_OTP / PASSWORD
* `status` (varchar) // ACTIVE / DISABLED / BANNED
* `last_login_at` (timestamp, nullable)
* `created_at`
* `updated_at`

### `user_profiles`

* `id`
* `user_id` (fk users.id, unique)
* `nickname` (varchar)
* `avatar_file_id` (fk file_objects.id, nullable)
* `avatar_url` (varchar, nullable) // 冗余便于展示
* `city_code` (varchar, nullable)
* `city_name` (varchar, nullable)
* `bio` (varchar, nullable)
* `created_at`
* `updated_at`

### `auth_otp_codes`

* `id`
* `channel` (varchar) // MOBILE / EMAIL
* `target` (varchar) // 手机号或邮箱（建议脱敏+哈希保存）
* `target_hash` (varchar)
* `purpose` (varchar) // LOGIN / BIND / RESET
* `otp_code_hash` (varchar) // 不存明文
* `status` (varchar) // ISSUED / VERIFIED / USED / EXPIRED
* `expires_at` (timestamp)
* `verified_at` (timestamp, nullable)
* `used_at` (timestamp, nullable)
* `request_ip` (varchar, nullable)
* `created_at`

### `auth_refresh_tokens`

* `id`
* `user_id` (fk users.id)
* `token_hash` (varchar, unique)
* `device_id` (varchar, nullable)
* `client_type` (varchar) // WEB / ADMIN_WEB
* `status` (varchar) // ACTIVE / REVOKED / EXPIRED / ROTATED
* `expires_at` (timestamp)
* `revoked_at` (timestamp, nullable)
* `created_at`
* `updated_at`

### `file_objects`

* `id`
* `owner_user_id` (fk users.id, nullable)
* `bucket` (varchar)
* `object_key` (varchar, unique)
* `biz_type` (varchar) // AVATAR / ID_CARD / PET_MEDIA / FEEDING_LOG / OTHER
* `file_name` (varchar)
* `mime_type` (varchar)
* `file_size` (bigint)
* `sha256` (varchar, nullable)
* `status` (varchar) // UPLOADING / READY / FAILED / DELETED
* `public_url` (varchar, nullable)
* `created_at`
* `updated_at`

### `admin_users`

* `id`
* `username` (varchar, unique)
* `password_hash` (varchar)
* `display_name` (varchar)
* `role` (varchar) // SUPER_ADMIN / AUDITOR / OPS / CS
* `status` (varchar) // ACTIVE / DISABLED
* `last_login_at` (timestamp, nullable)
* `created_at`
* `updated_at`

### `admin_audit_logs`

* `id`
* `admin_user_id` (fk admin_users.id)
* `action` (varchar)
* `target_type` (varchar, nullable)
* `target_id` (varchar, nullable)
* `before_snapshot` (jsonb, nullable)
* `after_snapshot` (jsonb, nullable)
* `remark` (varchar, nullable)
* `request_id` (varchar, nullable)
* `created_at`

### `cities`

* `id`
* `city_code` (varchar, unique)
* `city_name` (varchar)
* `is_enabled` (bool)
* `created_at`
* `updated_at`

### `system_configs`（建议）

* `id`
* `config_key` (varchar, unique)
* `config_value` (jsonb)
* `description` (varchar, nullable)
* `created_at`
* `updated_at`

---

# 4) Phase 0 接口清单（API Contract）

> 只做基础能力接口。业务接口（领养/喂养/救助）后续 Phase 再加。

---

## 4.1 公共约定

### Base URL

* 用户端 API：`/api/v1`
* Admin API：`/api/admin/v1`

### 统一响应格式

```json
{
  "code": "OK",
  "message": "success",
  "requestId": "req_xxx",
  "data": {}
}
```

### 统一错误格式

```json
{
  "code": "AUTH_TOKEN_INVALID",
  "message": "Token is invalid or expired",
  "requestId": "req_xxx",
  "details": null
}
```

---

## 4.2 系统与元数据接口（Phase 0）

### 1) 健康检查

* `GET /api/v1/system/health`
* `GET /api/v1/system/ready`

返回：

* app version
* db ok
* storage ok（ready 可以检查）

### 2) 城市列表（基础配置）

* `GET /api/v1/meta/cities`

用途：

* 登录后资料页选城市
* 后续业务模块复用

---

## 4.3 用户鉴权接口（Phase 0）

### 1) 发送 OTP（登录用）

* `POST /api/v1/auth/otp/send`

#### DTO：`SendOtpRequest`

* `channel` (string, required) // `MOBILE` | `EMAIL`
* `target` (string, required) // 手机号或邮箱
* `purpose` (string, required) // `LOGIN`
* `captchaToken` (string, optional) // 预留

#### DTO：`SendOtpResponse`

* `ttlSeconds` (number)
* `mockCode` (string, optional, 仅dev环境返回)

> 说明：Phase 0 可做“开发模式 mock OTP”（控制台日志+响应返回 mockCode），生产再接短信/邮件服务。

---

### 2) OTP 登录

* `POST /api/v1/auth/login/otp`

#### DTO：`OtpLoginRequest`

* `channel` (string, required)
* `target` (string, required)
* `otpCode` (string, required)
* `deviceId` (string, optional)
* `clientType` (string, required) // `WEB`

#### DTO：`AuthTokenResponse`

* `accessToken` (string)
* `accessTokenExpiresIn` (number)
* `refreshToken` (string)
* `refreshTokenExpiresIn` (number)
* `user` (`CurrentUserDTO`)

---

### 3) 刷新 Token

* `POST /api/v1/auth/refresh`

#### DTO：`RefreshTokenRequest`

* `refreshToken` (string, required)
* `deviceId` (string, optional)

#### DTO：`AuthTokenResponse`

同上（返回新 access + refresh，建议 refresh token rotation）

---

### 4) 登出

* `POST /api/v1/auth/logout`

#### DTO：`LogoutRequest`

* `refreshToken` (string, required)

返回：空

---

### 5) 获取当前用户信息

* `GET /api/v1/auth/me`

#### DTO：`CurrentUserDTO`

* `id` (number)
* `mobileMasked` (string, nullable)
* `emailMasked` (string, nullable)
* `status` (string)
* `roles` (string[]) // Phase 0 用户端先 `["USER"]`
* `profile` (`UserProfileDTO`)

---

## 4.4 用户资料接口（Phase 0）

### 1) 获取我的资料

* `GET /api/v1/users/me/profile`

#### DTO：`UserProfileDTO`

* `userId` (number)
* `nickname` (string)
* `avatarUrl` (string, nullable)
* `cityCode` (string, nullable)
* `cityName` (string, nullable)
* `bio` (string, nullable)

---

### 2) 更新我的资料

* `PATCH /api/v1/users/me/profile`

#### DTO：`UpdateUserProfileRequest`

* `nickname` (string, optional, max 32)
* `avatarFileId` (number, optional) // 由上传接口返回
* `cityCode` (string, optional)
* `cityName` (string, optional)
* `bio` (string, optional, max 200)

返回：`UserProfileDTO`

---

## 4.5 文件上传接口（Phase 0）

### 方案（首版）

先做 **单步 multipart 上传**（简单、快）
后续再升级为预签名直传

### 1) 上传文件

* `POST /api/v1/files/upload` (multipart/form-data)

form fields:

* `file` (binary, required)
* `bizType` (string, required) // `AVATAR` / `OTHER`

#### DTO：`FileUploadResponse`

* `fileId` (number)
* `url` (string)
* `fileName` (string)
* `mimeType` (string)
* `fileSize` (number)
* `status` (string) // READY

校验规则：

* 最大 10MB（可配）
* 仅允许图片（jpg/png/webp）用于 `AVATAR`
* 服务端生成 objectKey，禁止信任客户端文件名

---

### 2) 获取文件元信息（可选）

* `GET /api/v1/files/{fileId}`

返回：

* `fileId`
* `url`
* `bizType`
* `mimeType`
* `fileSize`
* `status`

---

## 4.6 Admin 鉴权与基础管理接口（Phase 0）

### 1) Admin 登录

* `POST /api/admin/v1/auth/login`

#### DTO：`AdminLoginRequest`

* `username` (string, required)
* `password` (string, required)

#### DTO：`AdminAuthResponse`

* `accessToken`
* `accessTokenExpiresIn`
* `refreshToken`
* `refreshTokenExpiresIn`
* `admin` (`AdminUserDTO`)

#### DTO：`AdminUserDTO`

* `id`
* `username`
* `displayName`
* `role` // `SUPER_ADMIN` / `AUDITOR` / `OPS` / `CS`
* `status`

---

### 2) Admin 刷新 Token

* `POST /api/admin/v1/auth/refresh`

### 3) Admin 登出

* `POST /api/admin/v1/auth/logout`

### 4) 获取当前 Admin 信息

* `GET /api/admin/v1/auth/me`

---

### 5) Admin 菜单（前端动态菜单）

* `GET /api/admin/v1/meta/menus`

返回（Phase 0 先硬编码按角色返回）：

* Dashboard
* Users（预留）
* Audit Logs

---

### 6) Admin 用户列表（基础版，便于调试）

* `GET /api/admin/v1/users`

query:

* `page`
* `pageSize`
* `status` (optional)
* `keyword` (optional)

返回：

* 分页列表（用户主信息 + profile）

---

### 7) Admin 修改用户状态（基础风控能力）

* `PATCH /api/admin/v1/users/{userId}/status`

#### DTO：`UpdateUserStatusRequest`

* `status` (string, required) // `ACTIVE` / `DISABLED` / `BANNED`
* `reason` (string, required)

要求：

* 写入 `admin_audit_logs`

---

### 8) Admin 审计日志列表

* `GET /api/admin/v1/audit-logs`

query:

* `adminUserId` (optional)
* `action` (optional)
* `targetType` (optional)
* `page`
* `pageSize`

---

# 5) DTO 字段清单（可直接让 Codex 建类）

> 这里按后端 Java DTO 命名给出，Codex 可以直接生成。

---

## 5.1 公共 DTO

### `ApiResponse<T>`

* `code: String`
* `message: String`
* `requestId: String`
* `data: T`

### `PageRequest`

* `page: Integer` (>=1)
* `pageSize: Integer` (1~100)

### `PageResponse<T>`

* `items: List<T>`
* `page: Integer`
* `pageSize: Integer`
* `total: Long`

---

## 5.2 Auth DTO（用户端）

### `SendOtpRequest`

* `channel: String` (`MOBILE`/`EMAIL`)
* `target: String`
* `purpose: String` (`LOGIN`)
* `captchaToken: String?`

### `SendOtpResponse`

* `ttlSeconds: Integer`
* `mockCode: String?`（dev only）

### `OtpLoginRequest`

* `channel: String`
* `target: String`
* `otpCode: String`
* `deviceId: String?`
* `clientType: String` (`WEB`)

### `RefreshTokenRequest`

* `refreshToken: String`
* `deviceId: String?`

### `LogoutRequest`

* `refreshToken: String`

### `CurrentUserDTO`

* `id: Long`
* `mobileMasked: String?`
* `emailMasked: String?`
* `status: String`
* `roles: List<String>`
* `profile: UserProfileDTO`

### `AuthTokenResponse`

* `accessToken: String`
* `accessTokenExpiresIn: Long`
* `refreshToken: String`
* `refreshTokenExpiresIn: Long`
* `user: CurrentUserDTO`

---

## 5.3 User Profile DTO

### `UserProfileDTO`

* `userId: Long`
* `nickname: String`
* `avatarUrl: String?`
* `cityCode: String?`
* `cityName: String?`
* `bio: String?`

### `UpdateUserProfileRequest`

* `nickname: String?` (max 32)
* `avatarFileId: Long?`
* `cityCode: String?`
* `cityName: String?`
* `bio: String?` (max 200)

---

## 5.4 File DTO

### `FileUploadResponse`

* `fileId: Long`
* `url: String`
* `fileName: String`
* `mimeType: String`
* `fileSize: Long`
* `status: String`

### `FileObjectDTO`（可选）

* `fileId: Long`
* `ownerUserId: Long?`
* `bizType: String`
* `url: String`
* `mimeType: String`
* `fileSize: Long`
* `status: String`

---

## 5.5 Admin DTO

### `AdminLoginRequest`

* `username: String`
* `password: String`

### `AdminUserDTO`

* `id: Long`
* `username: String`
* `displayName: String`
* `role: String`
* `status: String`

### `AdminAuthResponse`

* `accessToken: String`
* `accessTokenExpiresIn: Long`
* `refreshToken: String`
* `refreshTokenExpiresIn: Long`
* `admin: AdminUserDTO`

### `UpdateUserStatusRequest`

* `status: String`
* `reason: String`

### `AdminAuditLogDTO`

* `id: Long`
* `adminUserId: Long`
* `adminDisplayName: String`
* `action: String`
* `targetType: String?`
* `targetId: String?`
* `remark: String?`
* `requestId: String?`
* `createdAt: String`

---

# 6) Phase 0 状态机规则（必须后端校验）

> 这部分很关键，Codex 容易把状态控制写散。你要明确要求它把状态流转写在 service 层统一校验。

---

## 6.1 用户状态机（`users.status`）

### 状态

* `ACTIVE`
* `DISABLED`
* `BANNED`

### 合法流转

* `ACTIVE -> DISABLED`
* `ACTIVE -> BANNED`
* `DISABLED -> ACTIVE`
* `DISABLED -> BANNED`
* `BANNED -> ACTIVE`（仅 SUPER_ADMIN）
* `BANNED -> DISABLED`（可选，不建议首版开放）

### 规则

* `DISABLED` 和 `BANNED` 用户都不能登录
* 已登录用户被改为 `DISABLED/BANNED` 后，旧 access token 在下次请求时应被拦截（至少通过查库校验用户状态）

---

## 6.2 OTP 状态机（`auth_otp_codes.status`）

### 状态

* `ISSUED`
* `VERIFIED`
* `USED`
* `EXPIRED`

### 合法流转

* `ISSUED -> VERIFIED`（校验通过）
* `VERIFIED -> USED`（完成登录）
* `ISSUED -> EXPIRED`（超时）
* `VERIFIED -> EXPIRED`（超时）

### 规则

* OTP 只能使用一次
* 同一 target + purpose 在 TTL 内可限频（例如 60 秒内不可重复发送）
* OTP 失败次数限制（可先做简单 5 次）

> 简化做法：也可以不拆 `VERIFIED`，直接 `ISSUED -> USED`。但如果你想支持“先验证后提交”，保留 `VERIFIED` 更稳。

---

## 6.3 Refresh Token 状态机（`auth_refresh_tokens.status`）

### 状态

* `ACTIVE`
* `ROTATED`
* `REVOKED`
* `EXPIRED`

### 合法流转

* `ACTIVE -> ROTATED`（refresh token rotation）
* `ACTIVE -> REVOKED`（主动登出）
* `ACTIVE -> EXPIRED`（超时）
* `ROTATED -> EXPIRED`
* `REVOKED ->`（终态）
* `EXPIRED ->`（终态）

### 规则

* 刷新 token 后，旧 token 必须立即置为 `ROTATED`
* 登出时将指定 refresh token 置为 `REVOKED`
* 使用非 `ACTIVE` refresh token 一律报错

---

## 6.4 文件状态机（`file_objects.status`）

### 状态

* `UPLOADING`
* `READY`
* `FAILED`
* `DELETED`

### 合法流转

* `UPLOADING -> READY`
* `UPLOADING -> FAILED`
* `READY -> DELETED`

### 规则

* 只有 `READY` 的文件可绑定为头像等业务字段
* 删除文件建议先逻辑删（标记 `DELETED`），不立刻物理删除（后续可做异步清理）

---

## 6.5 Admin 状态机（`admin_users.status`）

### 状态

* `ACTIVE`
* `DISABLED`

### 合法流转

* `ACTIVE -> DISABLED`
* `DISABLED -> ACTIVE`

### 规则

* `DISABLED` 后台账号不可登录
* 禁止删除最后一个 `SUPER_ADMIN`（首版即便没做管理接口，也建议保留这个规则）

---

# 7) 后端模块分工（给 Codex 的实现边界）

---

## 7.1 `common` 模块（必须先做）

包含：

* `ApiResponse`
* `ErrorCode`（枚举）
* `BizException`
* 全局异常处理器（`@RestControllerAdvice`）
* `RequestIdFilter`（每个请求注入 `requestId` 到 MDC）
* 时间/JSON 配置
* 分页工具类

### 错误码建议（Phase 0 最小集）

* `OK`
* `INVALID_PARAM`
* `UNAUTHORIZED`
* `FORBIDDEN`
* `NOT_FOUND`
* `INTERNAL_ERROR`
* `AUTH_OTP_INVALID`
* `AUTH_OTP_EXPIRED`
* `AUTH_TOKEN_INVALID`
* `AUTH_REFRESH_TOKEN_INVALID`
* `USER_DISABLED`
* `USER_BANNED`
* `FILE_TYPE_NOT_ALLOWED`
* `FILE_TOO_LARGE`

---

## 7.2 `security` 模块（必须）

包含：

* JWT 生成/解析
* 用户端鉴权过滤器
* Admin 鉴权过滤器（可与用户端分开 token 前缀或 claim）
* `@CurrentUser` / `@CurrentAdmin` 参数注解（可选）
* 路由权限控制（Spring Security 配置）

### 权限策略（Phase 0）

* 用户端：

    * `/api/v1/auth/**`、`/api/v1/system/**`、`/api/v1/meta/**` 公开
    * `/api/v1/users/**`、`/api/v1/files/**` 需登录
* Admin 端：

    * `/api/admin/v1/auth/**` 公开
    * `/api/admin/v1/**` 需 admin token
    * 部分接口限制角色（如改用户状态仅 `SUPER_ADMIN/CS`）

---

## 7.3 `auth` 模块（必须）

包含：

* OTP 发送（Phase 0 可 mock）
* OTP 校验登录
* refresh token rotation
* logout
* me

### 注意点

* OTP 只存 hash，不存明文
* refresh token 只存 hash，不存明文
* 登录成功自动创建 `users` + `user_profiles`（如果不存在）

---

## 7.4 `user` 模块（必须）

包含：

* 获取/更新 profile
* 用户状态查询（给 security 用）
* Admin 用户列表（基础）
* Admin 修改用户状态（带审计日志）

---

## 7.5 `file` 模块（必须）

包含：

* Multipart 上传
* MinIO 存储封装
* 文件元数据落库
* 文件类型/大小校验
* 文件 URL 生成

---

## 7.6 `admin` 模块（必须）

包含：

* Admin 登录/refresh/logout/me
* Admin 菜单接口（按角色返回静态菜单）
* 审计日志查询

---

## 7.7 `system` 模块（必须）

包含：

* health / ready
* cities 列表
* 可选：读取 `system_configs`

---

# 8) 前端页面（Phase 0 最小壳）

---

## 8.1 用户端 `web-portal`（Phase 0）

页面：

1. `/` 首页（占位 + 登录入口）
2. `/login` OTP 登录页
3. `/me/profile` 个人资料页（昵称、头像、城市）
4. 401/403 页面（可选）

需要能力：

* Token 存储（access + refresh）
* 自动 refresh
* 请求拦截器（附带 token / requestId）
* 登录态路由守卫

---

## 8.2 Admin `admin-console`（Phase 0）

页面：

1. `/login` 登录页
2. `/dashboard` 占位看板（可展示健康状态）
3. `/users` 用户列表页（分页 + 状态筛选）
4. `/audit-logs` 审计日志页
5. Layout（侧边菜单 + 顶栏当前管理员）

需要能力：

* Admin token 存储（与用户端分开）
* 动态菜单（调用 `/meta/menus`）
* 基础角色按钮控制（前端可隐藏，后端必须再校验）

---

# 9) Codex 分模块开发任务（建议按顺序投喂）

> 每个任务尽量单独提交，避免 Codex 一次改太多。

---

## Task 0.1：工程骨架 + 本地环境

**目标**

* 创建 Monorepo 目录
* 初始化 `web-portal`、`admin-console`、`api-server`
* Docker Compose 启动 Postgres + MinIO（可选 Redis）
* 后端 Spring Boot 能启动
* 前端两个 Vite 项目能启动

**验收**

* `docker compose up` 成功
* `api-server` `/health` 返回 OK
* 两个前端页面可访问

---

## Task 0.2：后端公共层（common + error + logging）

**目标**

* 统一响应体、错误码、异常处理
* requestId 过滤器 + 日志 MDC
* OpenAPI 配置
* 基础安全配置占位

**验收**

* 非法参数返回统一格式
* 每个响应有 `requestId`
* Swagger 可打开

---

## Task 0.3：数据库迁移 + 核心表

**目标**

* 引入 Flyway
* 建立 Phase 0 核心表（上面那 8~9 张）
* 初始化默认 admin 账号（seed）
* 初始化城市数据（少量 seed）

**验收**

* 本地启动自动迁移成功
* DB 可看到表和索引
* 默认 admin 可登录（下一任务完成后验证）

---

## Task 0.4：用户端鉴权（OTP + JWT + Refresh）

**目标**

* `auth` 模块全部用户端接口
* mock OTP（dev）
* 登录后返回 token
* 刷新 token rotation
* 登出撤销 refresh token

**验收**

* 能发送 OTP（dev 返回 mockCode）
* 能登录获取 token
* 能刷新 token
* 登出后 refresh token 不可用
* 被禁用用户无法登录

---

## Task 0.5：用户资料接口

**目标**

* `GET/PATCH /users/me/profile`
* 自动创建默认 profile
* 头像 fileId 绑定（先预留，不强依赖上传）

**验收**

* 登录后可查看资料
* 可更新昵称/城市/bio
* 参数校验生效

---

## Task 0.6：文件上传（MinIO）

**目标**

* Multipart 上传接口
* MinIO 存储封装
* 文件元数据落库
* 图片类型/大小校验

**验收**

* 上传头像图片成功
* DB 有 `file_objects` 记录
* 返回可访问 URL
* 非法文件类型报错

---

## Task 0.7：Admin 鉴权 + 基础页面接口

**目标**

* Admin 登录/refresh/logout/me
* Admin 菜单接口
* Admin 用户列表
* 修改用户状态（写审计日志）
* 审计日志查询

**验收**

* 默认 admin 能登录
* 能查用户列表
* 能修改用户状态
* `admin_audit_logs` 有记录

---

## Task 0.8：前端用户端（Phase 0 壳）

**目标**

* 登录页（OTP）
* 个人资料页
* 上传头像（调用文件接口）
* API client + token 刷新

**验收**

* 浏览器完成登录
* 修改资料成功
* 上传头像成功

---

## Task 0.9：前端 Admin（Phase 0 壳）

**目标**

* Admin 登录页
* 基础 Layout + 动态菜单
* 用户列表页
* 改状态弹窗
* 审计日志页

**验收**

* Admin 完整可用
* 改用户状态后列表刷新
* 审计日志能看到记录

---

# 10) 给 Codex 的“执行约束”（很重要）

你可以把这段原样贴给 Codex，减少它自由发挥：

```text
请严格按照以下约束实现 Phase 0：

1. 这是基础设施阶段，不实现领养、上门喂养、救助业务模块；
2. 后端使用 Java 21 + Spring Boot + Maven + PostgreSQL + Flyway；
3. 前端使用 React + TypeScript + Vite，用户端和 Admin 分两个项目；
4. 所有接口必须使用统一响应体 ApiResponse；
5. 所有 DTO 必须做参数校验（javax/spring validation）；
6. 所有错误必须返回统一错误码；
7. 所有后台敏感操作（如修改用户状态）必须写 admin_audit_logs；
8. Token 使用 JWT + Refresh Token Rotation，refresh token 只存 hash；
9. OTP 在 dev 环境允许 mock（响应返回 mockCode），但数据库仍需记录 otp 状态；
10. 文件上传必须校验 MIME 和大小，服务端生成 object key；
11. 所有状态流转必须在 service 层校验，不能只靠前端限制；
12. 代码结构必须按模块分层：controller / dto / service / entity / repository；
13. 提供 OpenAPI 文档；
14. 提供本地 docker-compose 启动方式（Postgres + MinIO）。
```

---

# 11) Phase 0 验收清单（你自己验）

你可以按这个清单验收 Codex 输出：

* [ ] 本地一键启动（DB/MinIO/API/前端）
* [ ] Swagger 可访问
* [ ] 用户端 OTP 登录成功
* [ ] Token 过期后能自动 refresh
* [ ] 用户资料可更新
* [ ] 头像上传成功（并能展示）
* [ ] Admin 登录成功
* [ ] Admin 能查用户列表
* [ ] Admin 能禁用用户
* [ ] 禁用用户无法继续登录
* [ ] Admin 操作有审计日志
* [ ] 错误响应格式统一
* [ ] 日志有 requestId

---

# 12) 下一步建议（你接下来怎么喂 Codex）

最稳的方式是：**先让它做 Task 0.1 ~ 0.3**（工程骨架 + DB），你自己跑起来确认后，再做鉴权和上传。不要一口气让它做完整 Phase 0。

如果你愿意，我下一条可以继续给你一版 **Task 0.1 的“超具体 Codex 提示词”**（包括：

* `docker-compose.yml` 要包含哪些服务，
* Spring Boot `pom.xml` 依赖清单，
* 前端初始化命令，
* 第一批文件内容清单），你可以直接复制给 Codex。
