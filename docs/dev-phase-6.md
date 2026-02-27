好，下面给你补齐 **第6阶段文档（MySQL版）**。
这一阶段是你整套 MVP 的“运营与风控收口”，目标是把前 5 个阶段的业务（领养、喂养、救助）放进一个 **可运营、可治理、可排查** 的后台体系里。

---

# 第6阶段技术任务单（给 Codex）

## 模块：后台增强与风控（投诉工单 + 黑名单 + 城市开关 + Dashboard + 日志检索）

---

## 0）阶段目标与边界

### 本阶段目标

为前 5 个阶段补齐 **运营后台能力** 和 **基础风控能力**，实现：

1. **投诉工单系统（MVP）**
   用户可提交投诉，后台可受理、跟进、结案
2. **黑名单与行为拦截（MVP）**
   后台可按用户/手机号等维度封禁特定行为
3. **城市功能开关**
   允许按城市开关模块（领养/喂养/救助）
4. **运营 Dashboard**
   看核心待办和业务概览
5. **后台审计日志检索**
   可按模块/操作人/时间检索后台行为

---

### 本阶段边界（严格控制）

### ✅ 本期做

* 投诉工单（提交、查看、后台处理）
* 黑名单（新增、启用、停用、命中校验）
* 城市开关（按 feature_key + city_code）
* Dashboard（概览 + 待办 + 趋势基础版）
* 审计日志检索（基于 `admin_audit_logs`）
* 在关键业务动作上接入风控拦截（发布、下单、线索提交等）

### ❌ 本期不做

* 自动化风控策略引擎（规则 DSL）
* 机器学习风控评分
* 工单 SLA 自动催办
* 工单多级流转/协同审批
* 操作日志导出 CSV / BI 报表中心
* 多维告警中心（短信/邮件/飞书）

---

## 1）目录结构增量（在现有项目基础上新增）

---

### 后端（Spring Boot）

```text
services/api-server/src/main/java/com/petplatform/modules/
├─ ops/
│  ├─ controller/
│  │  ├─ ComplaintTicketController.java           // 用户端：提交投诉/我的投诉/详情/补充说明
│  │  ├─ AdminComplaintController.java            // 后台：投诉工单列表/详情/处理
│  │  ├─ AdminRiskController.java                 // 后台：黑名单管理
│  │  ├─ AdminCityFeatureController.java          // 后台：城市功能开关
│  │  ├─ AdminDashboardController.java            // 后台：Dashboard概览/待办/趋势
│  │  └─ AdminAuditLogController.java             // 后台：审计日志检索
│  ├─ dto/
│  │  ├─ user/
│  │  │  ├─ SubmitComplaintTicketRequest.java
│  │  │  ├─ MyComplaintTicketQuery.java
│  │  │  ├─ ComplaintTicketListItemDTO.java
│  │  │  ├─ ComplaintTicketDetailDTO.java
│  │  │  └─ ReplyComplaintTicketRequest.java
│  │  └─ admin/
│  │     ├─ AdminComplaintTicketQuery.java
│  │     ├─ AdminComplaintTicketListItemDTO.java
│  │     ├─ AdminComplaintTicketDetailDTO.java
│  │     ├─ UpdateComplaintTicketStatusRequest.java
│  │     ├─ AdminReplyComplaintTicketRequest.java
│  │     ├─ AdminBlacklistQuery.java
│  │     ├─ AdminBlacklistListItemDTO.java
│  │     ├─ UpsertBlacklistEntryRequest.java
│  │     ├─ UpdateBlacklistEntryStatusRequest.java
│  │     ├─ CityFeatureSwitchQuery.java
│  │     ├─ CityFeatureSwitchDTO.java
│  │     ├─ UpsertCityFeatureSwitchRequest.java
│  │     ├─ DashboardSummaryDTO.java
│  │     ├─ DashboardTodoItemDTO.java
│  │     ├─ DashboardTrendQuery.java
│  │     ├─ DashboardTrendPointDTO.java
│  │     ├─ AdminAuditLogQuery.java
│  │     ├─ AdminAuditLogListItemDTO.java
│  │     └─ AdminAuditLogDetailDTO.java
│  ├─ service/
│  │  ├─ ComplaintTicketService.java
│  │  ├─ AdminComplaintService.java
│  │  ├─ RiskBlacklistService.java
│  │  ├─ CityFeatureSwitchService.java
│  │  ├─ DashboardService.java
│  │  ├─ AuditLogQueryService.java
│  │  └─ impl/
│  ├─ entity/
│  │  ├─ ComplaintTicketEntity.java
│  │  ├─ ComplaintTicketReplyEntity.java
│  │  ├─ RiskBlacklistEntryEntity.java
│  │  └─ CityFeatureSwitchEntity.java
│  ├─ repository/
│  │  ├─ ComplaintTicketRepository.java
│  │  ├─ ComplaintTicketReplyRepository.java
│  │  ├─ RiskBlacklistEntryRepository.java
│  │  └─ CityFeatureSwitchRepository.java
│  ├─ enums/
│  │  ├─ ComplaintTicketStatus.java
│  │  ├─ ComplaintTargetType.java
│  │  ├─ ComplaintPriority.java
│  │  ├─ ComplaintReplyAuthorType.java
│  │  ├─ BlacklistSubjectType.java
│  │  ├─ BlacklistScopeType.java
│  │  ├─ BlacklistActionMode.java
│  │  ├─ BlacklistStatus.java
│  │  └─ CityFeatureKey.java
│  └─ risk/
│     ├─ RiskGuard.java                           // 统一风控拦截入口
│     ├─ RiskCheckContext.java
│     └─ RiskCheckResult.java
```

> `ops/risk` 这层是关键：后面所有模块（领养/喂养/救助）在关键写操作前调用它做黑名单和城市开关校验。

---

### 前端（用户端 Web）

```text
apps/web-portal/src/pages/
├─ Support/
│  ├─ ComplaintSubmitPage.tsx
│  ├─ MyComplaintTicketsPage.tsx
│  ├─ MyComplaintTicketDetailPage.tsx
│  └─ components/
│     ├─ ComplaintTargetSelector.tsx
│     ├─ ComplaintStatusTag.tsx
│     └─ ComplaintReplyComposer.tsx
```

---

### 前端（Admin）

```text
apps/admin-console/src/pages/
├─ Ops/
│  ├─ DashboardPage.tsx
│  ├─ ComplaintTicketListPage.tsx
│  ├─ ComplaintTicketDetailPage.tsx
│  ├─ BlacklistListPage.tsx
│  ├─ BlacklistEditPage.tsx
│  ├─ CityFeatureSwitchPage.tsx
│  ├─ AuditLogSearchPage.tsx
│  └─ components/
│     ├─ ComplaintStatusTag.tsx
│     ├─ PriorityTag.tsx
│     ├─ BlacklistStatusTag.tsx
│     ├─ FeatureSwitchToggle.tsx
│     ├─ DashboardKpiCard.tsx
│     └─ AuditLogDetailDrawer.tsx
```

---

## 2）数据库设计（MySQL 版本）

> 使用 MySQL 8.x，`utf8mb4`，时间字段 `datetime(3)`，JSON 字段用 `json`。
> 本阶段建议新增 4 张表，并给 `admin_audit_logs` 增加索引（如果前面没建够）。

---

## 2.1 新增表：`complaint_tickets`

用途：投诉工单主表（用户提交，后台处理）

### 字段

* `id` bigint auto_increment pk

* `ticket_no` varchar(32) not null unique

* `reporter_user_id` bigint not null

* `target_type` varchar(32) not null

    * `ADOPTION_POST`
    * `ADOPTION_APPLICATION`
    * `FEEDING_ORDER`
    * `FEEDING_PROVIDER`
    * `RESCUE_RESOURCE`
    * `USER`
    * `OTHER`

* `target_id` bigint null

* `title` varchar(200) not null

* `content` text not null

* `evidence_file_ids` json null                     // 仅存 file_object_id 数组（MVP）

* `contact_mobile` varchar(32) null

* `contact_mobile_masked` varchar(32) null

* `priority` varchar(16) not null

    * `LOW`
    * `MEDIUM`
    * `HIGH`
    * `URGENT`

* `status` varchar(32) not null

    * `SUBMITTED`
    * `IN_REVIEW`
    * `WAITING_USER`
    * `RESOLVED`
    * `REJECTED`
    * `CLOSED`
    * `CANCELLED_BY_USER`

* `assigned_admin_id` bigint null

* `triage_note` varchar(255) null

* `resolution_note` varchar(255) null

* `last_reply_at` datetime(3) null

* `handled_at` datetime(3) null

* `created_at` datetime(3) not null

* `updated_at` datetime(3) not null

### 索引

* `idx_complaint_tickets_reporter_status (reporter_user_id, status)`
* `idx_complaint_tickets_target (target_type, target_id)`
* `idx_complaint_tickets_status_priority (status, priority)`
* `idx_complaint_tickets_last_reply_at (last_reply_at)`
* `idx_complaint_tickets_created_at (created_at)`

---

## 2.2 新增表：`complaint_ticket_replies`

用途：工单回复记录（用户补充 / 后台回复）

### 字段

* `id` bigint auto_increment pk
* `ticket_id` bigint not null
* `author_type` varchar(16) not null

    * `USER`
    * `ADMIN`
    * `SYSTEM`
* `author_user_id` bigint null                     // USER 时有值
* `author_admin_id` bigint null                    // ADMIN 时有值
* `content` text not null
* `is_internal_note` tinyint(1) not null default 0   // 后台内部备注（用户不可见）
* `created_at` datetime(3) not null

### 索引

* `idx_complaint_ticket_replies_ticket_id (ticket_id, created_at)`

---

## 2.3 新增表：`risk_blacklist_entries`

用途：黑名单与限制规则（MVP 版）

### 字段

* `id` bigint auto_increment pk

* `subject_type` varchar(32) not null

    * `USER_ID`
    * `MOBILE`
    * `IP`
    * `DEVICE`（预留）

* `subject_value` varchar(128) not null

* `scope_type` varchar(32) not null

    * `GLOBAL`
    * `MODULE`
    * `ACTION`

* `scope_value` varchar(64) not null

    * 示例：

        * `GLOBAL`（全局）
        * `ADOPTION`
        * `FEEDING`
        * `RESCUE`
        * `ADOPTION_POST_CREATE`
        * `FEEDING_ORDER_CREATE`
        * `RESCUE_CLUE_SUBMIT`

* `action_mode` varchar(16) not null

    * `BLOCK`         // 直接拦截
    * `REVIEW_ONLY`   // 强制进审核（本期可先记录，后续扩展）
    * `LIMIT`         // 限流（本期可先不实现频控，仅保留字段）

* `reason_code` varchar(64) not null

* `reason_note` varchar(255) null

* `start_at` datetime(3) not null

* `end_at` datetime(3) null                        // null = 永久

* `status` varchar(16) not null

    * `ACTIVE`
    * `DISABLED`
    * `EXPIRED`

* `created_by_admin_id` bigint null

* `updated_by_admin_id` bigint null

* `created_at` datetime(3) not null

* `updated_at` datetime(3) not null

### 索引

* `idx_risk_blacklist_subject (subject_type, subject_value)`
* `idx_risk_blacklist_scope_status (scope_type, scope_value, status)`
* `idx_risk_blacklist_active_time (status, start_at, end_at)`

### 唯一建议（可选）

* 不强制唯一（允许同主体多条不同 scope 规则）

---

## 2.4 新增表：`city_feature_switches`

用途：按城市管理功能开关（读/写开关 + 说明）

### 字段

* `id` bigint auto_increment pk

* `city_code` varchar(32) not null

* `city_name` varchar(64) not null

* `feature_key` varchar(64) not null

    * `ADOPTION`
    * `FEEDING`
    * `RESCUE_GUIDE`
    * `RESCUE_RESOURCE`
    * `RESCUE_CLUE_SUBMIT`

* `is_enabled` tinyint(1) not null default 1         // 总开关

* `allow_read` tinyint(1) not null default 1         // 是否允许浏览（有些场景只关写）

* `allow_write` tinyint(1) not null default 1        // 是否允许发布/提交/下单

* `notice_text` varchar(255) null                    // 前台提示文案

* `effective_from` datetime(3) null

* `effective_to` datetime(3) null

* `updated_by_admin_id` bigint null

* `created_at` datetime(3) not null

* `updated_at` datetime(3) not null

### 索引 / 唯一约束

* 唯一约束：`uk_city_feature_switch (city_code, feature_key)`
* 索引：`idx_city_feature_switch_feature (feature_key, is_enabled)`

---

## 2.5 对现有表的索引增强（建议）

### `admin_audit_logs`

如果还没有，补以下索引（按你前面的表结构适配字段名）：

* `idx_admin_audit_logs_operator_time (operator_admin_id, created_at)`
* `idx_admin_audit_logs_module_action_time (module_name, action_name, created_at)`
* `idx_admin_audit_logs_target (target_type, target_id)`
* `idx_admin_audit_logs_created_at (created_at)`

> 这样第6阶段的“日志检索”会快很多。

---

## 2.6 Migration 文件建议（MySQL）

```text
V22__create_complaint_tickets.sql
V23__create_complaint_ticket_replies.sql
V24__create_risk_blacklist_entries.sql
V25__create_city_feature_switches.sql
V26__add_indexes_for_admin_audit_logs.sql
V27__seed_default_city_feature_switches.sql        -- 可选
```

---

## 3）接口清单（Phase 6）

---

## 3.1 用户端：投诉工单（`/api/v1/support/complaints`）

### 1）提交投诉工单

**POST** `/api/v1/support/complaints`

#### 前置规则

* 必须登录
* `evidenceFileIds`（若有）需校验文件归属 + `READY`

#### DTO：`SubmitComplaintTicketRequest`

* `targetType` (string, required)
* `targetId` (long, optional)
* `title` (string, required, max 200)
* `content` (string, required, max 5000)
* `priority` (string, optional, default `MEDIUM`)
* `contactMobile` (string, optional, max 32)
* `evidenceFileIds` (long[], optional, max 9)

#### 返回：`ComplaintTicketDetailDTO`

* 状态应为 `SUBMITTED`

---

### 2）获取我的投诉工单列表

**GET** `/api/v1/support/complaints/my`

#### Query：`MyComplaintTicketQuery`

* `page`
* `pageSize`
* `status` (optional)

#### 返回：`PageResponse<ComplaintTicketListItemDTO>`

* `ticketId`
* `ticketNo`
* `targetType`
* `title`
* `priority`
* `status`
* `lastReplyAt`
* `createdAt`
* `updatedAt`

---

### 3）获取投诉工单详情（我的）

**GET** `/api/v1/support/complaints/{ticketId}`

#### 规则

* 仅提交者本人可查看

#### 返回：`ComplaintTicketDetailDTO`

* `ticketId`
* `ticketNo`
* `targetType`
* `targetId`
* `title`
* `content`
* `priority`
* `status`
* `triageNote`
* `resolutionNote`
* `contactMobileMasked`
* `evidencePhotos`（解析 file urls）
* `replies`（仅公开回复，不含 internal note）
* `createdAt`
* `updatedAt`

---

### 4）补充说明/回复工单（用户）

**POST** `/api/v1/support/complaints/{ticketId}/reply`

#### DTO：`ReplyComplaintTicketRequest`

* `content` (string, required, max 2000)

#### 规则

* 仅提交者本人
* 允许状态：

    * `SUBMITTED`
    * `IN_REVIEW`
    * `WAITING_USER`
* 若当前状态是 `WAITING_USER`，用户回复后自动回到 `IN_REVIEW`

---

### 5）取消工单（用户，可选但建议做）

**POST** `/api/v1/support/complaints/{ticketId}/cancel`

#### 规则

* 仅提交者本人
* 仅 `SUBMITTED` / `WAITING_USER` 可取消
* 状态 -> `CANCELLED_BY_USER`

---

## 3.2 后台：投诉工单处理（`/api/admin/v1/ops/complaints`）

### 1）工单列表

**GET** `/api/admin/v1/ops/complaints`

#### Query：`AdminComplaintTicketQuery`

* `page`
* `pageSize`
* `status` (optional)
* `priority` (optional)
* `targetType` (optional)
* `assignedAdminId` (optional)
* `keyword` (optional: ticketNo/title/reporterUserId)
* `dateFrom` / `dateTo` (optional)

#### 返回：`PageResponse<AdminComplaintTicketListItemDTO>`

* `ticketId`
* `ticketNo`
* `reporterUserId`
* `targetType`
* `title`
* `priority`
* `status`
* `assignedAdminName`
* `lastReplyAt`
* `createdAt`

---

### 2）工单详情

**GET** `/api/admin/v1/ops/complaints/{ticketId}`

#### 返回：`AdminComplaintTicketDetailDTO`

包含：

* 主表全部字段（联系方式明文）
* 证据图片
* 完整回复列表（含 internal note）
* 处理信息（分配、备注、时间）

---

### 3）后台回复工单

**POST** `/api/admin/v1/ops/complaints/{ticketId}/reply`

#### DTO：`AdminReplyComplaintTicketRequest`

* `content` (string, required, max 2000)
* `isInternalNote` (boolean, optional, default false)
* `moveToStatus` (string, optional)

    * `IN_REVIEW`
    * `WAITING_USER`

#### 规则

* 仅后台角色
* 写 `complaint_ticket_replies`
* 可选同步更新工单状态
* 写 `admin_audit_logs`

---

### 4）更新工单状态（受理/结案/驳回等）

**POST** `/api/admin/v1/ops/complaints/{ticketId}/status`

#### DTO：`UpdateComplaintTicketStatusRequest`

* `status` (required)

    * `IN_REVIEW`
    * `WAITING_USER`
    * `RESOLVED`
    * `REJECTED`
    * `CLOSED`
* `assignedAdminId` (optional)
* `triageNote` (optional, max 255)
* `resolutionNote` (optional, max 255)

#### 规则

* 状态流转严格校验（见状态机）
* `RESOLVED/REJECTED/CLOSED` 时填 `handled_at`
* 写 `admin_audit_logs`

---

## 3.3 后台：黑名单管理（`/api/admin/v1/ops/risk/blacklists`）

### 1）黑名单列表

**GET** `/api/admin/v1/ops/risk/blacklists`

#### Query：`AdminBlacklistQuery`

* `page`
* `pageSize`
* `subjectType` (optional)
* `subjectValue` (optional)
* `scopeType` (optional)
* `scopeValue` (optional)
* `status` (optional)
* `keyword` (optional)

#### 返回：`PageResponse<AdminBlacklistListItemDTO>`

* `blacklistId`
* `subjectType`
* `subjectValueMasked`（敏感值脱敏展示）
* `scopeType`
* `scopeValue`
* `actionMode`
* `reasonCode`
* `status`
* `startAt`
* `endAt`
* `updatedAt`

---

### 2）新增/编辑黑名单规则

**PUT** `/api/admin/v1/ops/risk/blacklists/{blacklistId}`（或 `POST/PUT` 分开）

#### DTO：`UpsertBlacklistEntryRequest`

* `subjectType` (required)
* `subjectValue` (required)
* `scopeType` (required)
* `scopeValue` (required)
* `actionMode` (required)
* `reasonCode` (required)
* `reasonNote` (optional, max 255)
* `startAt` (required)
* `endAt` (optional)
* `status` (optional: `ACTIVE` / `DISABLED`)

#### 规则

* `endAt` 若存在必须 > `startAt`
* 写 `admin_audit_logs`

---

### 3）启用/停用黑名单规则

**POST** `/api/admin/v1/ops/risk/blacklists/{blacklistId}/status`

#### DTO：`UpdateBlacklistEntryStatusRequest`

* `status` (required: `ACTIVE` / `DISABLED`)
* `reasonNote` (optional)

---

## 3.4 后台：城市功能开关（`/api/admin/v1/ops/city-features`）

### 1）查询城市功能开关

**GET** `/api/admin/v1/ops/city-features`

#### Query：`CityFeatureSwitchQuery`

* `cityCode` (optional)
* `featureKey` (optional)
* `page` / `pageSize`（可选，数据量小也可不分页）

#### 返回：`PageResponse<CityFeatureSwitchDTO>`

* `switchId`
* `cityCode`
* `cityName`
* `featureKey`
* `isEnabled`
* `allowRead`
* `allowWrite`
* `noticeText`
* `effectiveFrom`
* `effectiveTo`
* `updatedAt`

---

### 2）新增/更新城市功能开关

**PUT** `/api/admin/v1/ops/city-features/{switchId}`（或 `POST/PUT` 分开）

#### DTO：`UpsertCityFeatureSwitchRequest`

* `cityCode` (required)
* `cityName` (required)
* `featureKey` (required)
* `isEnabled` (required)
* `allowRead` (required)
* `allowWrite` (required)
* `noticeText` (optional, max 255)
* `effectiveFrom` (optional)
* `effectiveTo` (optional)

#### 规则

* 同 `(cityCode, featureKey)` upsert
* 写 `admin_audit_logs`

---

## 3.5 后台：Dashboard（`/api/admin/v1/ops/dashboard`）

### 1）概览 KPI

**GET** `/api/admin/v1/ops/dashboard/summary`

#### 返回：`DashboardSummaryDTO`

建议字段：

* `todayNewUsers`

* `todayNewAdoptionPosts`

* `todayNewFeedingOrders`

* `todayNewRescueClues`

* `todayNewComplaints`

* `pendingVerificationCount`

* `pendingAdoptionReviewCount`

* `pendingComplaintCount`

* `pendingRescueClueCount`

* `activeFeedingProviders`

* `publishedAdoptionPosts`

* `activeRescueResources`

* `generatedAt`

---

### 2）待办列表

**GET** `/api/admin/v1/ops/dashboard/todos`

#### 返回：`List<DashboardTodoItemDTO>`

每条可包含：

* `todoType`（如 `VERIFICATION_REVIEW`, `ADOPTION_REVIEW`, `COMPLAINT_PENDING`, `RESCUE_TRIAGE`）
* `count`
* `title`
* `targetRoute`（前端跳转路径）
* `priority`

---

### 3）趋势数据（基础版）

**GET** `/api/admin/v1/ops/dashboard/trends`

#### Query：`DashboardTrendQuery`

* `metric` (required)

    * `NEW_USERS`
    * `ADOPTION_POSTS`
    * `FEEDING_ORDERS`
    * `RESCUE_CLUES`
    * `COMPLAINTS`
* `days` (optional, default 7, max 30)

#### 返回：`List<DashboardTrendPointDTO>`

* `date`
* `value`

---

## 3.6 后台：审计日志检索（`/api/admin/v1/ops/audit-logs`）

### 1）日志列表查询

**GET** `/api/admin/v1/ops/audit-logs`

#### Query：`AdminAuditLogQuery`

* `page`
* `pageSize`
* `operatorAdminId` (optional)
* `moduleName` (optional)
* `actionName` (optional)
* `targetType` (optional)
* `targetId` (optional)
* `keyword` (optional)  // 可搜 remark/摘要
* `dateFrom` / `dateTo` (optional)

#### 返回：`PageResponse<AdminAuditLogListItemDTO>`

* `auditLogId`
* `operatorAdminId`
* `operatorAdminName`
* `moduleName`
* `actionName`
* `targetType`
* `targetId`
* `summary`
* `createdAt`

---

### 2）日志详情

**GET** `/api/admin/v1/ops/audit-logs/{auditLogId}`

#### 返回：`AdminAuditLogDetailDTO`

* `auditLogId`
* `operatorAdminId`
* `operatorAdminName`
* `moduleName`
* `actionName`
* `targetType`
* `targetId`
* `beforeSnapshot`
* `afterSnapshot`
* `extraData`
* `createdAt`

---

## 4）DTO 字段清单（给 Codex 建类）

---

## 4.1 用户端投诉 DTO

### `SubmitComplaintTicketRequest`

* `targetType: String`
* `targetId: Long?`
* `title: String`
* `content: String`
* `priority: String?`
* `contactMobile: String?`
* `evidenceFileIds: List<Long>?`

### `MyComplaintTicketQuery`

* `page: Integer`
* `pageSize: Integer`
* `status: String?`

### `ComplaintTicketListItemDTO`

* `ticketId: Long`
* `ticketNo: String`
* `targetType: String`
* `title: String`
* `priority: String`
* `status: String`
* `lastReplyAt: String?`
* `createdAt: String`
* `updatedAt: String`

### `ComplaintTicketDetailDTO`

* `ticketId: Long`
* `ticketNo: String`
* `targetType: String`
* `targetId: Long?`
* `title: String`
* `content: String`
* `priority: String`
* `status: String`
* `triageNote: String?`
* `resolutionNote: String?`
* `contactMobileMasked: String?`
* `evidencePhotos: List<ComplaintEvidencePhotoDTO>`
* `replies: List<ComplaintReplyDTO>`
* `createdAt: String`
* `updatedAt: String`

### `ComplaintEvidencePhotoDTO`

* `fileId: Long`
* `url: String`

### `ComplaintReplyDTO`

* `replyId: Long`
* `authorType: String`
* `authorName: String?`
* `content: String`
* `createdAt: String`

### `ReplyComplaintTicketRequest`

* `content: String`

---

## 4.2 后台投诉 DTO

### `AdminComplaintTicketQuery`

* `page: Integer`
* `pageSize: Integer`
* `status: String?`
* `priority: String?`
* `targetType: String?`
* `assignedAdminId: Long?`
* `keyword: String?`
* `dateFrom: String?`
* `dateTo: String?`

### `AdminComplaintTicketListItemDTO`

* `ticketId: Long`
* `ticketNo: String`
* `reporterUserId: Long`
* `targetType: String`
* `title: String`
* `priority: String`
* `status: String`
* `assignedAdminName: String?`
* `lastReplyAt: String?`
* `createdAt: String`

### `AdminComplaintTicketDetailDTO`

* `ticketId: Long`
* `ticketNo: String`
* `reporterUserId: Long`
* `reporterNickname: String?`
* `targetType: String`
* `targetId: Long?`
* `title: String`
* `content: String`
* `priority: String`
* `status: String`
* `assignedAdminId: Long?`
* `assignedAdminName: String?`
* `triageNote: String?`
* `resolutionNote: String?`
* `contactMobile: String?`
* `evidencePhotos: List<ComplaintEvidencePhotoDTO>`
* `replies: List<AdminComplaintReplyDTO>`
* `lastReplyAt: String?`
* `handledAt: String?`
* `createdAt: String`
* `updatedAt: String`

### `AdminComplaintReplyDTO`

* `replyId: Long`
* `authorType: String`
* `authorName: String?`
* `content: String`
* `isInternalNote: Boolean`
* `createdAt: String`

### `AdminReplyComplaintTicketRequest`

* `content: String`
* `isInternalNote: Boolean?`
* `moveToStatus: String?`

### `UpdateComplaintTicketStatusRequest`

* `status: String`
* `assignedAdminId: Long?`
* `triageNote: String?`
* `resolutionNote: String?`

---

## 4.3 黑名单 DTO

### `AdminBlacklistQuery`

* `page: Integer`
* `pageSize: Integer`
* `subjectType: String?`
* `subjectValue: String?`
* `scopeType: String?`
* `scopeValue: String?`
* `status: String?`
* `keyword: String?`

### `AdminBlacklistListItemDTO`

* `blacklistId: Long`
* `subjectType: String`
* `subjectValueMasked: String`
* `scopeType: String`
* `scopeValue: String`
* `actionMode: String`
* `reasonCode: String`
* `status: String`
* `startAt: String`
* `endAt: String?`
* `updatedAt: String`

### `UpsertBlacklistEntryRequest`

* `subjectType: String`
* `subjectValue: String`
* `scopeType: String`
* `scopeValue: String`
* `actionMode: String`
* `reasonCode: String`
* `reasonNote: String?`
* `startAt: String`
* `endAt: String?`
* `status: String?`

### `UpdateBlacklistEntryStatusRequest`

* `status: String`
* `reasonNote: String?`

---

## 4.4 城市开关 DTO

### `CityFeatureSwitchQuery`

* `page: Integer?`
* `pageSize: Integer?`
* `cityCode: String?`
* `featureKey: String?`

### `CityFeatureSwitchDTO`

* `switchId: Long`
* `cityCode: String`
* `cityName: String`
* `featureKey: String`
* `isEnabled: Boolean`
* `allowRead: Boolean`
* `allowWrite: Boolean`
* `noticeText: String?`
* `effectiveFrom: String?`
* `effectiveTo: String?`
* `updatedAt: String`

### `UpsertCityFeatureSwitchRequest`

* `cityCode: String`
* `cityName: String`
* `featureKey: String`
* `isEnabled: Boolean`
* `allowRead: Boolean`
* `allowWrite: Boolean`
* `noticeText: String?`
* `effectiveFrom: String?`
* `effectiveTo: String?`

---

## 4.5 Dashboard DTO

### `DashboardSummaryDTO`

* `todayNewUsers: Long`
* `todayNewAdoptionPosts: Long`
* `todayNewFeedingOrders: Long`
* `todayNewRescueClues: Long`
* `todayNewComplaints: Long`
* `pendingVerificationCount: Long`
* `pendingAdoptionReviewCount: Long`
* `pendingComplaintCount: Long`
* `pendingRescueClueCount: Long`
* `activeFeedingProviders: Long`
* `publishedAdoptionPosts: Long`
* `activeRescueResources: Long`
* `generatedAt: String`

### `DashboardTodoItemDTO`

* `todoType: String`
* `title: String`
* `count: Long`
* `priority: String`
* `targetRoute: String`

### `DashboardTrendQuery`

* `metric: String`
* `days: Integer?`

### `DashboardTrendPointDTO`

* `date: String`
* `value: Long`

---

## 4.6 审计日志 DTO

### `AdminAuditLogQuery`

* `page: Integer`
* `pageSize: Integer`
* `operatorAdminId: Long?`
* `moduleName: String?`
* `actionName: String?`
* `targetType: String?`
* `targetId: Long?`
* `keyword: String?`
* `dateFrom: String?`
* `dateTo: String?`

### `AdminAuditLogListItemDTO`

* `auditLogId: Long`
* `operatorAdminId: Long`
* `operatorAdminName: String?`
* `moduleName: String`
* `actionName: String`
* `targetType: String?`
* `targetId: Long?`
* `summary: String?`
* `createdAt: String`

### `AdminAuditLogDetailDTO`

* `auditLogId: Long`
* `operatorAdminId: Long`
* `operatorAdminName: String?`
* `moduleName: String`
* `actionName: String`
* `targetType: String?`
* `targetId: Long?`
* `beforeSnapshot: Object?`
* `afterSnapshot: Object?`
* `extraData: Object?`
* `createdAt: String`

---

## 5）状态机规则（必须后端校验）

---

## 5.1 投诉工单状态机（`complaint_tickets.status`）

### 状态

* `SUBMITTED`
* `IN_REVIEW`
* `WAITING_USER`
* `RESOLVED`
* `REJECTED`
* `CLOSED`
* `CANCELLED_BY_USER`

### 合法流转

* 用户提交：`null -> SUBMITTED`
* 后台受理：`SUBMITTED -> IN_REVIEW`
* 后台要求补充：`IN_REVIEW -> WAITING_USER`
* 用户补充后：`WAITING_USER -> IN_REVIEW`
* 后台结案：`IN_REVIEW -> RESOLVED`
* 后台驳回：`IN_REVIEW -> REJECTED`
* 后台关闭：`RESOLVED -> CLOSED`
* 后台关闭：`REJECTED -> CLOSED`
* 用户取消：`SUBMITTED -> CANCELLED_BY_USER`
* 用户取消：`WAITING_USER -> CANCELLED_BY_USER`

### 非法流转（拦截）

* `RESOLVED -> IN_REVIEW`
* `CLOSED -> *`
* `CANCELLED_BY_USER -> *`

---

## 5.2 黑名单规则状态机（`risk_blacklist_entries.status`）

### 状态

* `ACTIVE`
* `DISABLED`
* `EXPIRED`

### 合法流转

* 新建：`null -> ACTIVE`（或 `DISABLED`）
* 启用：`DISABLED -> ACTIVE`
* 停用：`ACTIVE -> DISABLED`
* 自动过期：`ACTIVE -> EXPIRED`（由读取时判定或定时任务更新）
* 过期后手动启用（可选）：`EXPIRED -> ACTIVE`（若修改时间）

### 规则

* 命中逻辑仅对 `ACTIVE` 且当前时间在区间内生效
* `EXPIRED` 可由查询时动态判定，不一定要真实写回 DB（MVP 可动态判断）

---

## 5.3 城市开关生效规则（`city_feature_switches`）

不是严格状态机，但有生效逻辑：

### 生效条件

一条开关规则生效，当：

* `is_enabled = 1`
* 且当前时间满足 `effective_from/effective_to`（若配置了）

### 拦截逻辑建议

* `allow_read = 0`：列表/详情直接返回“该城市暂未开放”
* `allow_write = 0`：发帖/申请/下单/线索提交等写操作拦截
* 前台返回 `noticeText` 作为提示

---

## 6）风控接入点（必须落地）

这是第6阶段最关键的“系统性改造”，不是只做后台页面。

---

## 6.1 统一风控检查入口：`RiskGuard`

在关键写操作前调用：

### 统一方法建议

* `checkUserAction(userId, mobile, cityCode, actionKey)`
* 返回 `RiskCheckResult`

    * `allowed: boolean`
    * `errorCode: String?`
    * `noticeText: String?`

---

## 6.2 必须接入的业务动作（前几阶段模块）

至少接这些：

### Phase 3（领养/送养）

* `ADOPTION_POST_CREATE`（发布送养帖）
* `ADOPTION_APPLICATION_SUBMIT`（提交领养申请）

### Phase 4（上门喂养）

* `FEEDING_PROVIDER_PROFILE_ACTIVATE`（服务者上架）
* `FEEDING_ORDER_CREATE`（主人下单）

### Phase 5（救助）

* `RESCUE_CLUE_SUBMIT`（提交救助线索）

---

## 6.3 城市开关接入点

### 读接口（按模块）

* `/adoptions/posts`、`/adoptions/posts/{id}` -> `ADOPTION`
* `/feeding/providers`、`/feeding/providers/{id}` -> `FEEDING`
* `/rescue/guides` -> `RESCUE_GUIDE`
* `/rescue/resources` -> `RESCUE_RESOURCE`

### 写接口（按模块）

* 发帖/申请/下单/线索提交分别检查对应 `allow_write`

---

## 7）错误码增量（Phase 6）

新增错误码建议：

### 投诉工单

* `COMPLAINT_TICKET_NOT_FOUND`
* `COMPLAINT_TICKET_NOT_OWNER`
* `COMPLAINT_TICKET_STATUS_INVALID`
* `COMPLAINT_TICKET_FILE_INVALID`
* `COMPLAINT_TICKET_FILE_NOT_OWNED`

### 黑名单/风控

* `RISK_BLACKLIST_BLOCKED`
* `RISK_ACTION_NOT_ALLOWED`
* `RISK_SCOPE_NOT_ALLOWED`
* `RISK_CONFIG_INVALID`

### 城市功能开关

* `CITY_FEATURE_NOT_OPEN`
* `CITY_FEATURE_READ_DISABLED`
* `CITY_FEATURE_WRITE_DISABLED`

### 审计日志

* `AUDIT_LOG_NOT_FOUND`

---

## 8）前端页面任务（用户端 + Admin）

---

## 8.1 用户端（Web Portal）

### 页面 1：提交投诉 `/support/complaints/new`

内容：

* 投诉对象（targetType + targetId，可选）
* 标题
* 描述
* 优先级
* 联系方式
* 证据图片上传（1~9）
* 提交按钮

交互：

* 从业务详情页（订单/帖子）可带入 target 信息
* 提交成功跳转“我的工单详情”

---

### 页面 2：我的投诉 `/me/support/complaints`

内容：

* 工单列表（编号、标题、优先级、状态、最后回复时间）
* 状态筛选

---

### 页面 3：工单详情 `/me/support/complaints/:ticketId`

内容：

* 工单信息
* 对话时间线（用户/平台回复）
* 状态标签
* 补充说明输入框（当状态允许）
* 取消工单按钮（状态允许）

---

## 8.2 Admin（Admin Console）

### 页面 1：运营 Dashboard `/ops/dashboard`

内容：

* KPI 卡片（今日新增、待办数）
* 待办列表（可跳转）
* 趋势图（最近7天）

---

### 页面 2：投诉工单列表 `/ops/complaints`

内容：

* 筛选（状态、优先级、目标类型、时间）
* 列表（编号、标题、用户、优先级、状态、最后回复）
* 点击进入详情

---

### 页面 3：投诉工单详情 `/ops/complaints/:ticketId`

内容：

* 工单主信息
* 证据图
* 回复时间线（支持 internal note）
* 操作区：

    * 回复
    * 改状态
    * 分配处理人
    * 填分流/结案备注

---

### 页面 4：黑名单管理 `/ops/risk/blacklists`

内容：

* 列表 + 筛选
* 新增/编辑弹窗（或独立页）
* 启用/停用操作

---

### 页面 5：城市开关 `/ops/city-features`

内容：

* 按城市+功能查看
* 批量编辑（可后做）
* 单条开关切换（读/写）
* 提示文案设置

---

### 页面 6：审计日志检索 `/ops/audit-logs`

内容：

* 检索条件（模块、动作、操作人、时间）
* 日志列表
* 详情抽屉（before/after）

---

## 9）Codex 分任务投喂顺序（第6阶段）

---

## Task 6.1：数据库迁移 + 实体 + 枚举（MySQL）

**目标**

* 新增 4 张表：

    * `complaint_tickets`
    * `complaint_ticket_replies`
    * `risk_blacklist_entries`
    * `city_feature_switches`
* 给 `admin_audit_logs` 增加检索索引
* 实体、枚举、Repository

**验收**

* Flyway 成功
* 表结构/索引正确
* 项目可启动

---

## Task 6.2：用户端投诉工单接口

**目标**

* `POST /api/v1/support/complaints`
* `GET /api/v1/support/complaints/my`
* `GET /api/v1/support/complaints/{id}`
* `POST /api/v1/support/complaints/{id}/reply`
* `POST /api/v1/support/complaints/{id}/cancel`（建议做）

**要求**

* 文件归属校验（evidenceFileIds）
* 仅本人可看/回复/取消
* 状态机严格校验

**验收**

* 用户可提交工单并查看状态
* 可在等待补充时回复

---

## Task 6.3：后台投诉工单处理接口

**目标**

* 工单列表/详情
* 后台回复
* 更新状态（受理/待用户/结案/驳回/关闭）

**要求**

* 回复支持 internal note
* 所有后台动作写 `admin_audit_logs`
* 状态流转严格校验

**验收**

* 后台可完整处理投诉工单
* 用户端能看到公开回复和状态变化

---

## Task 6.4：黑名单管理接口 + `RiskGuard`

**目标**

* 黑名单列表/新增编辑/启停
* 实现统一 `RiskGuard`
* 在关键写接口接入黑名单拦截

**要求**

* 支持 `USER_ID` 和 `MOBILE` 至少两种 subjectType（MVP）
* 支持 `GLOBAL`/`ACTION` scope
* 命中时返回统一错误码与提示
* 写 `admin_audit_logs`

**验收**

* 黑名单规则可管理
* 被拉黑用户在指定动作上被拦截

---

## Task 6.5：城市功能开关接口 + 业务接入

**目标**

* 城市开关列表/新增更新
* 在读写接口接入 city feature 校验

**要求**

* 读/写开关分离（`allowRead` / `allowWrite`）
* 命中后返回 `noticeText`
* 写 `admin_audit_logs`

**验收**

* 关闭某城市某功能后，前台读写行为符合预期

---

## Task 6.6：Dashboard 接口

**目标**

* `/summary`
* `/todos`
* `/trends`

**要求**

* 直接聚合现有业务表（MVP）
* 时间按 UTC 计算（与项目统一）
* DTO 结构稳定，便于前端渲染

**验收**

* 后台能看到核心 KPI 和待办

---

## Task 6.7：审计日志检索接口

**目标**

* 审计日志列表查询
* 审计日志详情

**要求**

* 基于现有 `admin_audit_logs`
* 支持多条件检索 + 分页
* 注意 `before/after` JSON 返回

**验收**

* 后台可检索审核/风控/开关等操作日志

---

## Task 6.8：前端页面（用户投诉 + Admin 运营风控）

**目标**

* 用户端：投诉提交、我的投诉、工单详情
* Admin：Dashboard、工单、黑名单、城市开关、审计日志

**要求**

* 风格延续“温暖治愈 + 专业可信”（用户端）
* Admin 风格偏专业后台（信息密度更高）
* 先保证流程可用，再优化交互

**验收**

* 用户投诉闭环跑通
* 后台运营风控页面可用
* 黑名单/城市开关能真实影响业务行为

---

## Task 6.9：联调与回归测试（全链路）

**重点回归项**

* 黑名单命中后相关动作被拦截
* 城市功能关闭后读/写行为正确
* 投诉工单状态机正确
* 后台回复 internal note 用户不可见
* Dashboard 数据口径与实际表数据基本一致
* 审计日志检索能查到本阶段后台操作

---

## 10）总结

```text
实现第6阶段（后台增强与风控模块），基于已完成的 Phase 0~5 继续开发，并使用 MySQL。

【本阶段范围】
1) 投诉工单（用户提交、查看；后台处理）
2) 黑名单管理（后台可配置；关键动作拦截）
3) 城市功能开关（按城市 + featureKey 控制读写）
4) Dashboard（概览/待办/趋势）
5) 审计日志检索（基于 admin_audit_logs）

【严格边界】
- 不做自动化策略引擎
- 不做复杂工单SLA
- 不做BI报表导出
- 不做告警中心

【数据库要求（MySQL）】
新增表：
- complaint_tickets
- complaint_ticket_replies
- risk_blacklist_entries
- city_feature_switches

并给 admin_audit_logs 增加常用检索索引。

要求：
- JSON 字段使用 MySQL json
- 时间字段使用 datetime(3)
- 所有表 utf8mb4
- Flyway 迁移脚本使用 MySQL 语法

【关键业务规则】
1) 投诉工单状态机严格实现：
   SUBMITTED -> IN_REVIEW -> WAITING_USER -> IN_REVIEW -> RESOLVED/REJECTED -> CLOSED
   支持用户在 SUBMITTED/WAITING_USER 取消工单
2) 黑名单命中逻辑：
   - 支持 subjectType: USER_ID / MOBILE（至少）
   - 支持 scopeType: GLOBAL / ACTION（至少）
   - 命中 ACTIVE 且生效时间内的规则时，BLOCK 模式直接拦截
3) 城市功能开关：
   - featureKey 至少支持 ADOPTION / FEEDING / RESCUE_GUIDE / RESCUE_RESOURCE / RESCUE_CLUE_SUBMIT
   - 区分 allowRead / allowWrite
   - 命中时返回 noticeText
4) 后台所有操作（工单处理/黑名单/城市开关）写 admin_audit_logs

【必须接入风控的业务动作】
- ADOPTION_POST_CREATE
- ADOPTION_APPLICATION_SUBMIT
- FEEDING_PROVIDER_PROFILE_ACTIVATE
- FEEDING_ORDER_CREATE
- RESCUE_CLUE_SUBMIT

【接口范围】
用户端：
- 提交投诉
- 我的投诉列表
- 我的投诉详情
- 工单补充回复
- 取消工单（建议）

后台：
- 投诉工单列表/详情/回复/状态更新
- 黑名单列表/新增编辑/启停
- 城市功能开关列表/新增更新
- Dashboard summary/todos/trends
- 审计日志列表/详情

【前端范围】
用户端：
- 提交投诉页
- 我的投诉列表页
- 工单详情页

Admin：
- Dashboard
- 投诉工单列表/详情
- 黑名单管理
- 城市功能开关
- 审计日志检索

先保证功能闭环、权限和状态机正确，再优化样式和交互。

【验收标准】
- 用户投诉闭环可用
- 后台可处理工单并记录审计日志
- 黑名单配置后能拦截指定动作
- 城市开关能控制指定城市功能读/写
- Dashboard 能展示核心概览和待办
- 审计日志可检索本阶段所有后台操作
```

---

## 11）第6阶段验收清单（你自己验）

* [ ] Flyway 新增表迁移成功（MySQL）
* [ ] `admin_audit_logs` 索引补齐且查询正常
* [ ] 用户可提交投诉工单（含证据图）
* [ ] 我的投诉列表/详情可查看
* [ ] 用户可在允许状态补充回复/取消工单
* [ ] 后台可查看并处理工单（回复、改状态、分配）
* [ ] internal note 用户端不可见
* [ ] 黑名单规则可创建/启停
* [ ] 黑名单命中后指定动作被拦截
* [ ] 城市功能开关可配置
* [ ] 城市功能开关能影响前台读/写行为
* [ ] Dashboard 概览/待办/趋势可展示
* [ ] 审计日志检索与详情可用
* [ ] 本阶段后台动作写入审计日志

---

## 12）到这里这套 MVP 的阶段图（收尾说明）

现在这套路线已经是一个完整的 MVP 了（并且比很多“想法型副业”成熟很多）：

* **Phase 0**：基础设施（登录、文件、后台框架）
* **Phase 2**：认证体系（实名/服务者认证）
* **Phase 3**：领养送养闭环
* **Phase 4**：上门喂养闭环
* **Phase 5**：救助信息中枢（指引+资源+线索）
* **Phase 6**：运营与风控收口（投诉、黑名单、开关、Dashboard、日志）
