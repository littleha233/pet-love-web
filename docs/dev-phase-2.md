这里按 **“已完成 Phase 0（工程骨架/鉴权/上传/后台基础）”** 来理解。
那接下来输出的就是你要的 **第二阶段工作（对应我之前路线里的 Phase 1：用户与认证模块）**。

---

# 第二阶段技术任务单（给 Codex）

## 阶段目标

在已完成的 Phase 0 基础上，实现 **用户认证闭环**：

1. **实名认证（普通用户）**
2. **服务者认证（上门喂养服务者申请）**
3. **后台审核（通过/驳回）**
4. **角色生效（`USER` / `PROVIDER`）**
5. **审计日志（后台动作留痕）**

---

## 本阶段边界（严格控制）

### ✅ 做

* 用户提交实名认证材料
* 用户提交服务者认证材料
* 用户查看认证状态
* 后台审核实名认证/服务者认证
* 审核通过后角色生效（`PROVIDER`）
* 后台审计日志记录

### ❌ 不做

* 领养/送养业务
* 上门喂养订单
* 救助指引
* 在线支付
* 即时聊天
* 复杂风控规则（黑名单等放后续阶段）

---

# 1) 目录结构增量（在 Phase 0 基础上新增）

> 只列新增/改动目录，Codex 按这个放文件。

```text
services/api-server/src/main/java/com/petplatform/modules/
├─ verification/
│  ├─ controller/
│  │  ├─ UserVerificationController.java
│  │  └─ AdminVerificationController.java
│  ├─ dto/
│  │  ├─ user/
│  │  │  ├─ SubmitRealNameVerificationRequest.java
│  │  │  ├─ SubmitProviderVerificationRequest.java
│  │  │  ├─ VerificationStatusDTO.java
│  │  │  └─ VerificationDetailDTO.java
│  │  └─ admin/
│  │     ├─ AdminVerificationListItemDTO.java
│  │     ├─ AdminVerificationDetailDTO.java
│  │     ├─ ReviewVerificationRequest.java
│  │     └─ AdminVerificationQuery.java
│  ├─ service/
│  │  ├─ UserVerificationService.java
│  │  ├─ AdminVerificationService.java
│  │  └─ impl/
│  ├─ entity/
│  │  └─ UserVerificationEntity.java
│  ├─ repository/
│  │  └─ UserVerificationRepository.java
│  └─ enums/
│     ├─ VerificationType.java
│     ├─ VerificationStatus.java
│     └─ ProviderCapabilityTag.java   // 可选
```

前端新增页面（用户端 + Admin）：

```text
apps/web-portal/src/pages/
├─ Verification/
│  ├─ VerificationCenterPage.tsx
│  ├─ RealNameSubmitPage.tsx
│  ├─ ProviderSubmitPage.tsx
│  └─ VerificationResultPage.tsx

apps/admin-console/src/pages/
├─ Verifications/
│  ├─ VerificationListPage.tsx
│  ├─ VerificationDetailPage.tsx
│  └─ components/
│     ├─ ReviewModal.tsx
│     └─ VerificationStatusTag.tsx
```

---

# 2) 数据库设计增量（Phase 1 Migration）

## 2.1 新增表：`user_verifications`

> 一个用户每种认证类型一条记录（`REAL_NAME` / `PROVIDER`），驳回后允许复提（更新同一条）。

### 建议字段

* `id` (bigserial, pk)
* `user_id` (fk -> users.id, not null)
* `verification_type` (varchar, not null)

    * `REAL_NAME`
    * `PROVIDER`
* `status` (varchar, not null)

    * `PENDING`
    * `APPROVED`
    * `REJECTED`
* `submit_version` (int, not null default 1)

  > 每次重新提交 +1，便于追踪版本
* `real_name` (varchar, nullable)

  > 仅 `REAL_NAME` 用
* `id_no_masked` (varchar, nullable)
* `id_no_hash` (varchar, nullable)
* `id_front_file_id` (bigint, nullable, fk -> file_objects.id)
* `id_back_file_id` (bigint, nullable, fk -> file_objects.id)
* `holding_id_file_id` (bigint, nullable, fk -> file_objects.id)
* `provider_experience_years` (int, nullable)

  > 仅 `PROVIDER` 用
* `provider_intro` (text, nullable)
* `provider_service_pet_types` (jsonb, nullable)

  > `["CAT","DOG"]`
* `provider_service_city_code` (varchar, nullable)
* `provider_capability_tags` (jsonb, nullable)

  > 可选，如 `["FEED","CLEAN","PHOTO_REPORT"]`
* `reject_reason_code` (varchar, nullable)
* `reject_reason_text` (varchar, nullable)
* `reviewed_by_admin_id` (bigint, nullable, fk -> admin_users.id)
* `reviewed_at` (timestamp, nullable)
* `created_at` (timestamp)
* `updated_at` (timestamp)

### 约束与索引

* 唯一约束：`unique(user_id, verification_type)`
* 索引：

    * `idx_user_verifications_status_type (status, verification_type)`
    * `idx_user_verifications_user_id (user_id)`
    * `idx_user_verifications_updated_at (updated_at desc)`

---

## 2.2 建议新增字段（可选但推荐）

### `user_profiles` 增量字段（便于前端显示）

* `is_real_name_verified` (bool, default false)
* `is_provider_verified` (bool, default false)

> 也可以不加，运行时通过 `user_verifications` 推导；但加了前端列表和 `/auth/me` 更快。

---

## 2.3 Phase 1 Migration 文件建议

新增 SQL 文件：

```text
V4__create_user_verifications.sql
V5__alter_user_profiles_add_verification_flags.sql   // 如果你采用冗余字段
V6__seed_system_configs_verification_rules.sql        // 可选：认证文案/规则
```

---

# 3) 接口清单（Phase 1）

---

## 3.1 用户端接口（`/api/v1/verifications/**`）

### 1) 获取我的认证概览

**GET** `/api/v1/verifications/me`

#### 返回 DTO：`MyVerificationOverviewDTO`

* `realName`

    * `status` (`NOT_SUBMITTED|PENDING|APPROVED|REJECTED`)
    * `submitVersion`
    * `rejectReasonText` (nullable)
    * `updatedAt`
* `provider`

    * `status`
    * `submitVersion`
    * `rejectReasonText` (nullable)
    * `updatedAt`
* `canSubmitProvider` (boolean)

  > 仅实名认证通过才为 true

---

### 2) 获取某类认证详情（用于回显）

**GET** `/api/v1/verifications/{type}`

路径参数：

* `type`: `REAL_NAME` | `PROVIDER`

#### 返回 DTO：`VerificationDetailDTO`

* `verificationType`
* `status`
* `submitVersion`
* `realName` (REAL_NAME 时返回)
* `idNoMasked` (REAL_NAME 时返回)
* `idFrontFileId` / `idFrontUrl`
* `idBackFileId` / `idBackUrl`
* `holdingIdFileId` / `holdingIdUrl`
* `providerExperienceYears` (PROVIDER 时返回)
* `providerIntro`
* `providerServicePetTypes`
* `providerServiceCityCode`
* `providerCapabilityTags`
* `rejectReasonCode`
* `rejectReasonText`
* `reviewedAt`
* `updatedAt`

---

### 3) 提交实名认证

**POST** `/api/v1/verifications/real-name/submit`

#### DTO：`SubmitRealNameVerificationRequest`

* `realName` (string, required, max 32)
* `idNo` (string, required)

  > 服务端只做 hash + masked 存储，不落明文
* `idFrontFileId` (long, required)
* `idBackFileId` (long, required)
* `holdingIdFileId` (long, optional)
* `agreeDeclaration` (boolean, required, must be true)

#### 返回 DTO：`VerificationStatusDTO`

* `verificationType = REAL_NAME`
* `status = PENDING`
* `submitVersion`
* `updatedAt`

---

### 4) 提交服务者认证

**POST** `/api/v1/verifications/provider/submit`

#### 前置规则

* 用户必须 `REAL_NAME = APPROVED`

#### DTO：`SubmitProviderVerificationRequest`

* `providerExperienceYears` (int, required, 0~50)
* `providerIntro` (string, required, max 1000)
* `providerServicePetTypes` (string[], required)

  > `CAT`, `DOG`
* `providerServiceCityCode` (string, required)
* `providerCapabilityTags` (string[], optional)

  > `FEED`, `WATER`, `LITTER`, `PHOTO_REPORT`, `PLAY`
* `supportingFileIds` (long[], optional, max 6)

  > 从业证明、培训证明等（首版可选）
* `agreeServiceCode` (boolean, required, must be true)

#### 返回 DTO：`VerificationStatusDTO`

* `verificationType = PROVIDER`
* `status = PENDING`
* `submitVersion`
* `updatedAt`

---

## 3.2 Admin 端接口（`/api/admin/v1/verifications/**`）

### 1) 认证列表（审核工作台）

**GET** `/api/admin/v1/verifications`

#### Query DTO：`AdminVerificationQuery`

* `page` (int, required)
* `pageSize` (int, required)
* `verificationType` (optional) // `REAL_NAME|PROVIDER`
* `status` (optional) // `PENDING|APPROVED|REJECTED`
* `keyword` (optional)

  > 用户ID / 昵称 / 手机后四位（脱敏查询）
* `cityCode` (optional)

  > PROVIDER 用
* `dateFrom` (optional)
* `dateTo` (optional)

#### 返回 DTO：`PageResponse<AdminVerificationListItemDTO>`

* `id`
* `userId`
* `nickname`
* `mobileMasked`
* `verificationType`
* `status`
* `submitVersion`
* `providerServiceCityCode` (nullable)
* `reviewedByAdminName` (nullable)
* `reviewedAt` (nullable)
* `updatedAt`

---

### 2) 认证详情（审核页）

**GET** `/api/admin/v1/verifications/{verificationId}`

#### 返回 DTO：`AdminVerificationDetailDTO`

包含用户信息 + 提交材料 + 文件链接 + 审核信息：

* `id`
* `userId`
* `nickname`
* `mobileMasked`
* `emailMasked`
* `verificationType`
* `status`
* `submitVersion`
* `realName` / `idNoMasked`（REAL_NAME）
* `idFrontUrl` / `idBackUrl` / `holdingIdUrl`
* `providerExperienceYears` / `providerIntro` / `providerServicePetTypes` / `providerServiceCityCode`
* `providerCapabilityTags`
* `supportingFiles` (可选)
* `rejectReasonCode`
* `rejectReasonText`
* `reviewedByAdminName`
* `reviewedAt`
* `createdAt`
* `updatedAt`

---

### 3) 审核通过

**POST** `/api/admin/v1/verifications/{verificationId}/approve`

#### DTO：`ReviewVerificationRequest`

* `remark` (string, optional, max 200)

#### 规则

* 只能审核 `PENDING`
* 审核通过后：

    * 更新 `status=APPROVED`
    * 填 `reviewed_by_admin_id`, `reviewed_at`
    * 若 `verification_type=REAL_NAME`，更新 `user_profiles.is_real_name_verified=true`
    * 若 `verification_type=PROVIDER`，更新 `user_profiles.is_provider_verified=true`
* 写 `admin_audit_logs`

---

### 4) 审核驳回

**POST** `/api/admin/v1/verifications/{verificationId}/reject`

#### DTO：`ReviewVerificationRequest`（驳回版）

* `rejectReasonCode` (string, required)

    * 建议枚举：

        * `ID_PHOTO_BLURRY`
        * `ID_INFO_MISMATCH`
        * `MISSING_REQUIRED_FILES`
        * `INTRO_TOO_SIMPLE`
        * `INVALID_SUPPORTING_DOC`
        * `OTHER`
* `rejectReasonText` (string, required, max 200)
* `remark` (string, optional, max 200)

#### 规则

* 只能审核 `PENDING`
* 驳回后状态 `REJECTED`
* 写 `admin_audit_logs`

---

# 4) DTO 字段清单（给 Codex 建类）

---

## 4.1 用户端 DTO

### `MyVerificationOverviewDTO`

* `realName: VerificationStatusSummaryDTO`
* `provider: VerificationStatusSummaryDTO`
* `canSubmitProvider: Boolean`

### `VerificationStatusSummaryDTO`

* `status: String` // NOT_SUBMITTED/PENDING/APPROVED/REJECTED
* `submitVersion: Integer?`
* `rejectReasonText: String?`
* `updatedAt: String?`

### `VerificationStatusDTO`

* `verificationType: String`
* `status: String`
* `submitVersion: Integer`
* `updatedAt: String`

### `SubmitRealNameVerificationRequest`

* `realName: String` (`@NotBlank`, `@Size(max=32)`)
* `idNo: String` (`@NotBlank`, `@Size(max=64)`)
* `idFrontFileId: Long` (`@NotNull`)
* `idBackFileId: Long` (`@NotNull`)
* `holdingIdFileId: Long?`
* `agreeDeclaration: Boolean` (`must=true`)

### `SubmitProviderVerificationRequest`

* `providerExperienceYears: Integer` (`@NotNull`, `0~50`)
* `providerIntro: String` (`@NotBlank`, `@Size(max=1000)`)
* `providerServicePetTypes: List<String>` (`@NotEmpty`)
* `providerServiceCityCode: String` (`@NotBlank`)
* `providerCapabilityTags: List<String>?`
* `supportingFileIds: List<Long>?`
* `agreeServiceCode: Boolean` (`must=true`)

### `VerificationDetailDTO`

* `verificationType: String`
* `status: String`
* `submitVersion: Integer`
* `realName: String?`
* `idNoMasked: String?`
* `idFrontFileId: Long?`
* `idFrontUrl: String?`
* `idBackFileId: Long?`
* `idBackUrl: String?`
* `holdingIdFileId: Long?`
* `holdingIdUrl: String?`
* `providerExperienceYears: Integer?`
* `providerIntro: String?`
* `providerServicePetTypes: List<String>?`
* `providerServiceCityCode: String?`
* `providerCapabilityTags: List<String>?`
* `rejectReasonCode: String?`
* `rejectReasonText: String?`
* `reviewedAt: String?`
* `updatedAt: String`

---

## 4.2 Admin DTO

### `AdminVerificationQuery`

* `page: Integer`
* `pageSize: Integer`
* `verificationType: String?`
* `status: String?`
* `keyword: String?`
* `cityCode: String?`
* `dateFrom: String?`
* `dateTo: String?`

### `AdminVerificationListItemDTO`

* `id: Long`
* `userId: Long`
* `nickname: String`
* `mobileMasked: String?`
* `verificationType: String`
* `status: String`
* `submitVersion: Integer`
* `providerServiceCityCode: String?`
* `reviewedByAdminName: String?`
* `reviewedAt: String?`
* `updatedAt: String`

### `AdminVerificationDetailDTO`

* `id: Long`
* `userId: Long`
* `nickname: String`
* `mobileMasked: String?`
* `emailMasked: String?`
* `verificationType: String`
* `status: String`
* `submitVersion: Integer`
* `realName: String?`
* `idNoMasked: String?`
* `idFrontUrl: String?`
* `idBackUrl: String?`
* `holdingIdUrl: String?`
* `providerExperienceYears: Integer?`
* `providerIntro: String?`
* `providerServicePetTypes: List<String>?`
* `providerServiceCityCode: String?`
* `providerCapabilityTags: List<String>?`
* `rejectReasonCode: String?`
* `rejectReasonText: String?`
* `reviewedByAdminName: String?`
* `reviewedAt: String?`
* `createdAt: String`
* `updatedAt: String`

### `ReviewVerificationRequest`

* `rejectReasonCode: String?`
* `rejectReasonText: String?`
* `remark: String?`

> 实现时建议拆成两个 DTO：`ApproveVerificationRequest` / `RejectVerificationRequest`，校验更清晰。

---

# 5) 状态机规则（必须后端校验）

---

## 5.1 认证状态机（`user_verifications.status`）

### 状态

* `PENDING`
* `APPROVED`
* `REJECTED`

### 提交流程规则

* 首次提交：创建记录，状态 `PENDING`
* 驳回后重提：同一条记录更新为 `PENDING`，`submit_version + 1`
* 已通过后：

    * 默认不允许重新提交（首版）
    * 如后续需要“变更认证信息”，再加“重新认证”流程

### 合法流转

* `null -> PENDING`（首次提交）
* `REJECTED -> PENDING`（重提）
* `PENDING -> APPROVED`
* `PENDING -> REJECTED`

### 非法流转（拦截）

* `APPROVED -> PENDING`
* `APPROVED -> REJECTED`
* `REJECTED -> APPROVED`（必须先重提变 `PENDING`）

---

## 5.2 角色生效规则（`PROVIDER`）

* 用户默认角色：`USER`
* 只有当 `verification_type=PROVIDER` 且 `status=APPROVED` 时：

    * `/api/v1/auth/me` 返回角色包含 `PROVIDER`
* `PROVIDER` 审核通过前，前端“服务者中心”入口可隐藏或显示“未认证”

---

## 5.3 依赖规则（非常关键）

### 服务者认证前置条件

* 必须 `REAL_NAME = APPROVED`
* 否则提交服务者认证返回错误：

    * `VERIFICATION_REAL_NAME_REQUIRED`

### 文件归属校验

提交认证时使用的 `fileId` 必须满足：

* 文件存在
* `status = READY`
* 文件归当前用户所有（或系统允许范围）
* `bizType` 合法（例如 `ID_CARD`, `CERTIFICATE`, `OTHER`）

---

# 6) 接口错误码增量（Phase 1）

在 Phase 0 错误码基础上新增：

* `VERIFICATION_REAL_NAME_REQUIRED`
* `VERIFICATION_ALREADY_APPROVED`
* `VERIFICATION_NOT_FOUND`
* `VERIFICATION_STATUS_INVALID`
* `VERIFICATION_FILE_INVALID`
* `VERIFICATION_FILE_NOT_OWNED`
* `VERIFICATION_SUBMIT_NOT_ALLOWED`
* `VERIFICATION_REVIEW_NOT_ALLOWED`

---

# 7) 前端页面任务（用户端 + Admin）

---

## 7.1 用户端页面（Web Portal）

### 页面 1：认证中心 `/me/verification`

显示两个卡片：

1. 实名认证（状态标签 + 按钮）
2. 服务者认证（状态标签 + 按钮）

状态展示：

* 未提交
* 审核中
* 已通过
* 已驳回（显示原因）

按钮逻辑：

* 实名未提交/驳回 -> 去提交页
* 实名审核中 -> 查看详情（只读）
* 实名已通过 -> 查看详情（只读）
* 服务者按钮仅当实名已通过可点击

---

### 页面 2：实名认证提交页 `/me/verification/real-name`

表单字段：

* 姓名
* 身份证号
* 身份证正面上传
* 身份证反面上传
* 手持证件（可选）
* 勾选声明

交互要求：

* 上传调用 `/files/upload`
* 提交前校验
* 提交后跳转认证中心并显示“审核中”

---

### 页面 3：服务者认证提交页 `/me/verification/provider`

表单字段：

* 从业年限
* 个人介绍
* 服务宠物类型（猫/狗）
* 服务城市
* 能力标签（多选）
* 证明材料上传（可选，多张）
* 勾选服务规范声明

交互要求：

* 未实名通过时直接拦截并提示
* 提交成功显示审核中

---

## 7.2 Admin 页面（Admin Console）

### 页面 1：认证审核列表 `/verifications`

筛选项：

* 认证类型
* 状态
* 城市
* 关键词
* 日期范围

列表字段：

* 认证ID
* 用户ID/昵称
* 类型
* 状态
* 版本号
* 城市（服务者）
* 更新时间
* 操作（查看）

---

### 页面 2：认证审核详情 `/verifications/:id`

展示：

* 用户基础信息（脱敏）
* 认证内容
* 图片预览（身份证、证明材料）
* 历史审核结果（当前版本即可）
* 驳回原因（如有）

操作：

* 通过
* 驳回（弹窗选择原因 + 填写说明）

要求：

* 审核操作成功后返回列表并刷新
* 审核按钮仅在 `PENDING` 状态显示

---

# 8) Codex 分任务投喂顺序（第二阶段）

> 建议你按下面顺序一条条喂，成功率高。

---

## Task 1：数据库迁移 + 枚举 + 实体

**目标**

* 新增 `user_verifications` 表
* （可选）`user_profiles` 增加认证标记字段
* 后端枚举：`VerificationType`, `VerificationStatus`
* `UserVerificationEntity` + Repository

**验收**

* Flyway 迁移成功
* 表结构/索引正确
* 本地启动无报错

---

## Task 2：用户端认证接口（提交 + 查询）

**目标**

* `GET /api/v1/verifications/me`
* `GET /api/v1/verifications/{type}`
* `POST /api/v1/verifications/real-name/submit`
* `POST /api/v1/verifications/provider/submit`

**要求**

* DTO 校验
* 文件归属校验
* 服务者认证前置实名校验
* `idNo` 只存 hash + masked，不存明文
* 驳回后重提 `submit_version + 1`

**验收**

* 用户可提交实名
* 用户可提交服务者（实名通过后）
* 查询状态正确
* 非法状态能正确报错

---

## Task 3：Admin 审核接口 + 审计日志

**目标**

* `GET /api/admin/v1/verifications`
* `GET /api/admin/v1/verifications/{id}`
* `POST /approve`
* `POST /reject`

**要求**

* 仅 `AUDITOR` / `SUPER_ADMIN` 可审核（你也可以允许 `CS`）
* 审核动作写 `admin_audit_logs`
* 审核通过后更新用户认证标记/角色推导数据

**验收**

* 后台能查待审列表
* 能查看详情图片
* 能通过/驳回
* 审计日志有记录

---

## Task 4：用户端前端页面（认证中心 + 两个提交页）

**目标**

* 实现 3 个页面：

    * 认证中心
    * 实名提交
    * 服务者提交
* 接入文件上传与认证接口

**要求**

* 状态标签清晰
* 驳回原因显示
* 表单校验完善
* 上传进度与错误提示

**验收**

* 浏览器端可完整提交流程
* 提交后状态变为审核中
* 认证中心显示正确状态

---

## Task 5：Admin 前端页面（审核列表 + 详情）

**目标**

* 审核列表页
* 审核详情页
* 驳回弹窗

**要求**

* 支持筛选与分页
* 状态标签颜色区分
* 图片可预览
* 操作后自动刷新列表

**验收**

* 后台审核全流程可跑通
* 审核操作结果与前台状态一致

---

## Task 6：联调与回归测试（Phase 1 收尾）

**目标**

* 联调用户端 + Admin + API
* 补充单测/集成测试（状态流转重点）
* 修复边界 bug

**重点回归点**

* 驳回后重新提交版本号递增
* 实名未通过不能提服务者
* 已通过认证不能重复提交
* 禁用用户无法调用认证接口（如果你 Phase 0 已做状态拦截）
* Admin 审核日志完整

---

# 9) 给 Codex 的执行提示词（第二阶段）

你可以直接复制这段给 Codex：

```text
实现第二阶段（用户认证模块），基于已完成的 Phase 0 工程骨架继续开发。

【范围】
1) 用户实名认证提交/查询
2) 用户服务者认证提交/查询
3) 后台认证审核（通过/驳回）
4) 审计日志记录
5) 前端用户认证页面 + Admin 审核页面

【严格边界】
- 不实现领养/送养业务
- 不实现上门喂养订单
- 不实现救助指引
- 不实现支付和聊天

【后端要求】
- 新增 user_verifications 表（一个用户每种认证一条记录）
- 驳回后允许重提，submit_version + 1
- idNo 明文不得入库，仅存 hash + masked
- 文件必须校验 owner、status=READY、bizType 合法
- 服务者认证必须要求实名认证已通过
- Admin 审核动作必须写 admin_audit_logs
- 所有接口统一 ApiResponse
- 所有 DTO 做参数校验

【前端要求】
- 用户端：认证中心、实名认证提交页、服务者认证提交页
- Admin：认证审核列表页、认证详情页、驳回弹窗
- 先做功能可用和状态清晰，样式沿用现有风格即可

【验收标准】
- 用户可提交实名认证并查看审核状态
- 实名通过后可提交服务者认证
- Admin 可审核通过/驳回
- 审核结果正确回显到用户端
- 审计日志可查询
```

---

# 10) 第二阶段验收清单（你自己验）

* [ ] 实名认证可提交（含证件图上传）
* [ ] `idNo` 数据库中无明文
* [ ] 实名认证状态可查询
* [ ] 实名未通过时，服务者认证被拦截
* [ ] 服务者认证可提交
* [ ] Admin 能看到待审列表
* [ ] Admin 能查看认证详情和图片
* [ ] Admin 能通过/驳回
* [ ] 驳回原因前台可见
* [ ] 驳回后可重新提交且版本号 +1
* [ ] 审计日志记录审核动作
* [ ] `/auth/me`（或 profile）能体现认证状态/角色变化

---

如果你愿意，我下一条可以继续给你一版 **“第三阶段（领养/送养模块）的 Codex 技术任务单”**，我会沿着你现在这套数据库和前端风格继续往下接，避免返工。
