这一阶段的核心目标是：把“普通人不知道怎么救助宠物”的问题，做成一个 **可用的救助信息入口**（不是复杂调度系统）。

---

# 第5阶段技术任务单（给 Codex）

## 模块：宠物救助指引（指南 + 本地资源目录 + 线索提交）

---

## 0）阶段目标与边界

### 本阶段目标

实现一个 **宠物救助信息中枢（MVP）**，包含三部分：

1. **救助指引**（按场景给普通人清晰步骤）
2. **本地救助资源目录**（医院/救助站/志愿者/官方渠道）
3. **救助线索提交**（用户上报流浪/受伤宠物线索，便于后续跟进）

---

### 本阶段边界（严格控制）

### ✅ 本期做

* 救助指引分类、列表、详情（后台可维护）
* 本地救助资源目录（按城市/类型检索，后台可维护）
* 用户提交救助线索（含图片）
* 用户查看“我的线索”
* 后台线索分流与状态更新（基础版）
* 后台审计日志（复用 `admin_audit_logs`）

### ❌ 本期不做

* 实时派单 / 自动派发给志愿者
* 地图轨迹 / GIS 调度
* 线上支付/捐款
* 即时聊天群组
* 举报/投诉仲裁（第6阶段统一做）
* 复杂 SLA、自动催办、工单系统

> 这一阶段先做“信息正确、入口清晰、流程可追踪”，不做重运营调度。

---

## 1）目录结构增量（在现有项目基础上新增）

---

### 后端（Spring Boot）

```text
services/api-server/src/main/java/com/petplatform/modules/
├─ rescue/
│  ├─ controller/
│  │  ├─ RescueGuideController.java             // 前台：指南列表/详情
│  │  ├─ RescueResourceController.java          // 前台：资源目录列表/详情
│  │  ├─ RescueClueController.java              // 前台：提交线索/我的线索/详情
│  │  └─ AdminRescueController.java             // 后台：指南/资源/线索管理
│  ├─ dto/
│  │  ├─ user/
│  │  │  ├─ RescueGuideListQuery.java
│  │  │  ├─ RescueGuideListItemDTO.java
│  │  │  ├─ RescueGuideDetailDTO.java
│  │  │  ├─ RescueResourceListQuery.java
│  │  │  ├─ RescueResourceListItemDTO.java
│  │  │  ├─ RescueResourceDetailDTO.java
│  │  │  ├─ SubmitRescueClueRequest.java
│  │  │  ├─ RescueClueListItemDTO.java
│  │  │  ├─ RescueClueDetailDTO.java
│  │  │  └─ MyRescueClueQuery.java
│  │  └─ admin/
│  │     ├─ AdminRescueGuideQuery.java
│  │     ├─ AdminRescueGuideListItemDTO.java
│  │     ├─ UpsertRescueGuideRequest.java
│  │     ├─ AdminRescueResourceQuery.java
│  │     ├─ AdminRescueResourceListItemDTO.java
│  │     ├─ UpsertRescueResourceRequest.java
│  │     ├─ AdminRescueClueQuery.java
│  │     ├─ AdminRescueClueListItemDTO.java
│  │     ├─ AdminRescueClueDetailDTO.java
│  │     └─ UpdateRescueClueStatusRequest.java
│  ├─ service/
│  │  ├─ RescueGuideService.java
│  │  ├─ RescueResourceService.java
│  │  ├─ RescueClueService.java
│  │  ├─ AdminRescueService.java
│  │  └─ impl/
│  ├─ entity/
│  │  ├─ RescueGuideEntity.java
│  │  ├─ RescueResourceEntity.java
│  │  ├─ RescueClueEntity.java
│  │  └─ RescueClueMediaEntity.java
│  ├─ repository/
│  │  ├─ RescueGuideRepository.java
│  │  ├─ RescueResourceRepository.java
│  │  ├─ RescueClueRepository.java
│  │  └─ RescueClueMediaRepository.java
│  └─ enums/
│     ├─ RescueGuideStatus.java
│     ├─ RescueGuideScenarioCode.java
│     ├─ RescueResourceType.java
│     ├─ RescueResourceStatus.java
│     ├─ RescueClueStatus.java
│     ├─ RescueUrgencyLevel.java
│     └─ RescuePetConditionTag.java
```

---

### 前端（用户端 Web）

```text
apps/web-portal/src/pages/
├─ Rescue/
│  ├─ RescueHomePage.tsx                      // 救助首页（指引入口 + 资源入口 + 线索提交入口）
│  ├─ RescueGuideListPage.tsx
│  ├─ RescueGuideDetailPage.tsx
│  ├─ RescueResourceListPage.tsx
│  ├─ RescueResourceDetailPage.tsx
│  ├─ RescueClueSubmitPage.tsx
│  ├─ MyRescueCluesPage.tsx
│  ├─ MyRescueClueDetailPage.tsx
│  └─ components/
│     ├─ RescueScenarioCard.tsx
│     ├─ RescueResourceCard.tsx
│     ├─ RescueClueStatusTag.tsx
│     └─ RescueCluePhotoUploader.tsx
```

---

### 前端（Admin）

```text
apps/admin-console/src/pages/
├─ Rescue/
│  ├─ RescueGuideListPage.tsx
│  ├─ RescueGuideEditPage.tsx
│  ├─ RescueResourceListPage.tsx
│  ├─ RescueResourceEditPage.tsx
│  ├─ RescueClueListPage.tsx
│  ├─ RescueClueDetailPage.tsx
│  └─ components/
│     ├─ RescueGuideStatusTag.tsx
│     ├─ RescueResourceStatusTag.tsx
│     ├─ RescueClueStatusTag.tsx
│     └─ RescueClueStatusUpdateModal.tsx
```

---

## 2）数据库设计（MySQL 版本）

> 使用 MySQL 8.x，`utf8mb4`，时间字段 `datetime(3)`，JSON 字段用 `json`。
> 本阶段建议新增 4 张表。

---

## 2.1 新增表：`rescue_guides`

用途：救助指引内容（后台维护，前台展示）

### 字段

* `id` bigint auto_increment pk
* `scenario_code` varchar(64) not null
  例如：

    * `FOUND_STRAY_CAT`
    * `FOUND_STRAY_DOG`
    * `INJURED_CAT`
    * `INJURED_DOG`
    * `ABANDONED_KITTENS`
    * `ABANDONED_PUPPIES`
    * `EMERGENCY_TRANSPORT`
* `title` varchar(200) not null
* `summary` varchar(500) null
* `content_md` mediumtext not null              // Markdown 内容（MVP 足够）
* `city_code` varchar(32) null                  // null = 全国通用；有值 = 城市定制版
* `tags` json null                              // 例如 ["新手","紧急情况","幼崽"]
* `sort_order` int not null default 0
* `status` varchar(32) not null

    * `DRAFT`
    * `PUBLISHED`
    * `OFFLINE`
* `version` int not null default 1
* `published_at` datetime(3) null
* `created_by_admin_id` bigint null
* `updated_by_admin_id` bigint null
* `created_at` datetime(3) not null
* `updated_at` datetime(3) not null

### 索引

* `idx_rescue_guides_status_city_sort (status, city_code, sort_order)`
* `idx_rescue_guides_scenario_status (scenario_code, status)`

> MVP 不做内容版本表，先用 `version` 字段记录次数。

---

## 2.2 新增表：`rescue_resources`

用途：本地救助资源目录（医院/救助站/志愿者/官方）

### 字段

* `id` bigint auto_increment pk
* `resource_type` varchar(32) not null

    * `ANIMAL_HOSPITAL`
    * `SHELTER`
    * `VOLUNTEER_GROUP`
    * `OFFICIAL_CHANNEL`
    * `NGO`
* `name` varchar(200) not null
* `city_code` varchar(32) not null
* `city_name` varchar(64) not null
* `district_name` varchar(64) null
* `address` varchar(255) null
* `contact_phone` varchar(64) null
* `contact_wechat` varchar(64) null
* `contact_other` varchar(255) null
* `service_hours` varchar(128) null
* `service_scope` varchar(255) null             // 服务范围描述
* `accept_pet_types` json null                  // ["CAT","DOG"]
* `capability_tags` json null                   // ["急救","收容","绝育","转运"]
* `description` text null
* `source_url` varchar(500) null                // 数据来源（可选）
* `verified_at` datetime(3) null                // 人工核验时间
* `status` varchar(32) not null

    * `DRAFT`
    * `ACTIVE`
    * `PAUSED`
    * `OFFLINE`
* `sort_order` int not null default 0
* `created_by_admin_id` bigint null
* `updated_by_admin_id` bigint null
* `created_at` datetime(3) not null
* `updated_at` datetime(3) not null

### 索引

* `idx_rescue_resources_city_type_status (city_code, resource_type, status)`
* `idx_rescue_resources_status_sort (status, sort_order)`
* `idx_rescue_resources_verified_at (verified_at)`

---

## 2.3 新增表：`rescue_clues`

用途：用户提交的救助线索（受伤/流浪/疑似遗弃）

### 字段

* `id` bigint auto_increment pk

* `clue_no` varchar(32) not null unique          // 业务编号，便于沟通

* `reporter_user_id` bigint not null             // MVP 先要求登录提交

* `city_code` varchar(32) not null

* `city_name` varchar(64) not null

* `district_name` varchar(64) null

* `location_text` varchar(255) not null          // 位置描述（小区/路口）

* `geo_lat` decimal(10,7) null                   // 可选

* `geo_lng` decimal(10,7) null                   // 可选

* `pet_type` varchar(16) null                    // CAT/DOG/UNKNOWN

* `estimated_count` int null                     // 数量（如一窝幼崽）

* `urgency_level` varchar(16) not null

    * `LOW`
    * `MEDIUM`
    * `HIGH`
    * `EMERGENCY`

* `condition_tags` json null                     // ["受伤","无法站立","幼崽","疑似遗弃"]

* `description` text not null

* `contact_name` varchar(64) not null

* `contact_mobile` varchar(32) not null

* `contact_mobile_masked` varchar(32) not null

* `status` varchar(32) not null

    * `SUBMITTED`
    * `TRIAGED`
    * `IN_PROGRESS`
    * `RESOLVED`
    * `CLOSED`
    * `INVALID`

* `triage_note` varchar(255) null                // 初步分流备注

* `resolution_note` varchar(255) null            // 结案备注

* `handled_by_admin_id` bigint null

* `handled_at` datetime(3) null

* `suggested_resource_ids` json null             // 建议联系的资源目录ID列表 [1,3,5]

* `created_at` datetime(3) not null

* `updated_at` datetime(3) not null

### 索引

* `idx_rescue_clues_reporter_status (reporter_user_id, status)`
* `idx_rescue_clues_city_status (city_code, status)`
* `idx_rescue_clues_urgency_status (urgency_level, status)`
* `idx_rescue_clues_created_at (created_at)`

---

## 2.4 新增表：`rescue_clue_media`

用途：线索图片（MVP 先支持图片）

### 字段

* `id` bigint auto_increment pk
* `clue_id` bigint not null
* `file_object_id` bigint not null
* `sort_order` int not null default 0
* `created_at` datetime(3) not null

### 索引/约束

* `idx_rescue_clue_media_clue_sort (clue_id, sort_order)`

---

## 2.5 Migration 文件建议（MySQL）

```text
V17__create_rescue_guides.sql
V18__create_rescue_resources.sql
V19__create_rescue_clues.sql
V20__create_rescue_clue_media.sql
V21__seed_rescue_guide_templates_and_resource_types.sql   -- 可选
```

---

## 3）接口清单（Phase 5）

---

## 3.1 前台：救助指引（`/api/v1/rescue/guides`）

### 1）获取救助指引列表

**GET** `/api/v1/rescue/guides`

#### Query：`RescueGuideListQuery`

* `page` (required)
* `pageSize` (required)
* `scenarioCode` (optional)
* `cityCode` (optional)
* `keyword` (optional)

#### 返回：`PageResponse<RescueGuideListItemDTO>`

* `guideId`
* `scenarioCode`
* `title`
* `summary`
* `cityCode` (nullable)
* `tags`
* `publishedAt`
* `sortOrder`

> 前台仅查 `status=PUBLISHED`。
> 查询策略：优先城市定制版，其次全国通用版（可先简单实现为都返回，后续优化优先级）。

---

### 2）获取救助指引详情

**GET** `/api/v1/rescue/guides/{guideId}`

#### 返回：`RescueGuideDetailDTO`

* `guideId`
* `scenarioCode`
* `title`
* `summary`
* `contentMd`
* `cityCode`
* `tags`
* `publishedAt`
* `updatedAt`

---

## 3.2 前台：救助资源目录（`/api/v1/rescue/resources`）

### 1）获取救助资源列表

**GET** `/api/v1/rescue/resources`

#### Query：`RescueResourceListQuery`

* `page` (required)
* `pageSize` (required)
* `cityCode` (optional)
* `resourceType` (optional)
* `petType` (optional: `CAT` / `DOG`)
* `keyword` (optional)

#### 返回：`PageResponse<RescueResourceListItemDTO>`

* `resourceId`
* `resourceType`
* `name`
* `cityCode`
* `cityName`
* `districtName`
* `serviceScope`
* `acceptPetTypes`
* `capabilityTags`
* `contactPhoneMasked` (可脱敏)
* `verifiedAt`
* `sortOrder`

> 前台仅查 `status=ACTIVE`。

---

### 2）获取救助资源详情

**GET** `/api/v1/rescue/resources/{resourceId}`

#### 返回：`RescueResourceDetailDTO`

* `resourceId`
* `resourceType`
* `name`
* `cityCode`
* `cityName`
* `districtName`
* `address`
* `contactPhone`
* `contactWechat`
* `contactOther`
* `serviceHours`
* `serviceScope`
* `acceptPetTypes`
* `capabilityTags`
* `description`
* `sourceUrl`
* `verifiedAt`

---

## 3.3 前台：救助线索（`/api/v1/rescue/clues`）

### 1）提交救助线索

**POST** `/api/v1/rescue/clues`

#### 前置规则

* 必须登录（MVP）
* 建议不强制实名（降低救助上报门槛）
* 线索图片可选，但建议至少 1 张（前端提示）

#### DTO：`SubmitRescueClueRequest`

* `cityCode` (string, required)

* `cityName` (string, required)

* `districtName` (string, optional)

* `locationText` (string, required, max 255)

* `geoLat` (decimal, optional)

* `geoLng` (decimal, optional)

* `petType` (string, optional: `CAT` / `DOG` / `UNKNOWN`)

* `estimatedCount` (int, optional, 1~50)

* `urgencyLevel` (string, required)

* `conditionTags` (string[], optional, max 10)

* `description` (string, required, max 5000)

* `contactName` (string, required, max 64)

* `contactMobile` (string, required, max 32)

* `photoFileIds` (long[], optional, max 9)

#### 返回：`RescueClueDetailDTO`

* 状态应为 `SUBMITTED`

---

### 2）获取我的线索列表

**GET** `/api/v1/rescue/clues/my`

#### Query：`MyRescueClueQuery`

* `page`
* `pageSize`
* `status` (optional)

#### 返回：`PageResponse<RescueClueListItemDTO>`

* `clueId`
* `clueNo`
* `cityName`
* `districtName`
* `petType`
* `urgencyLevel`
* `status`
* `createdAt`
* `updatedAt`

---

### 3）获取线索详情（我的）

**GET** `/api/v1/rescue/clues/{clueId}`

#### 规则

* 仅线索提交者本人可查看（Admin 用后台接口）

#### 返回：`RescueClueDetailDTO`

* `clueId`
* `clueNo`
* `cityCode`
* `cityName`
* `districtName`
* `locationText`
* `petType`
* `estimatedCount`
* `urgencyLevel`
* `conditionTags`
* `description`
* `contactName`
* `contactMobileMasked`
* `status`
* `triageNote`
* `resolutionNote`
* `suggestedResources` (数组，可选)

    * `resourceId`
    * `name`
    * `resourceType`
    * `contactPhone`
* `photos` (数组)
* `createdAt`
* `updatedAt`

---

## 3.4 后台接口（`/api/admin/v1/rescue/**`）

---

### A. 指引管理（Admin）

### 1）指引列表

**GET** `/api/admin/v1/rescue/guides`

#### Query：`AdminRescueGuideQuery`

* `page`
* `pageSize`
* `status` (optional)
* `scenarioCode` (optional)
* `cityCode` (optional)
* `keyword` (optional)

#### 返回：`PageResponse<AdminRescueGuideListItemDTO>`

* `guideId`
* `scenarioCode`
* `title`
* `cityCode`
* `status`
* `version`
* `publishedAt`
* `updatedAt`

---

### 2）指引详情

**GET** `/api/admin/v1/rescue/guides/{guideId}`

---

### 3）创建/更新指引

**PUT** `/api/admin/v1/rescue/guides/{guideId}`（或 `POST/PUT` 分开也行）

#### DTO：`UpsertRescueGuideRequest`

* `scenarioCode` (required)
* `title` (required, max 200)
* `summary` (optional, max 500)
* `contentMd` (required)
* `cityCode` (optional)
* `tags` (optional)
* `sortOrder` (optional)
* `status` (optional: `DRAFT` / `PUBLISHED` / `OFFLINE`)

#### 规则

* `PUBLISHED` 时需校验 `title/content/scenarioCode`
* 更新时 `version + 1`
* 写 `admin_audit_logs`

---

### 4）发布指引

**POST** `/api/admin/v1/rescue/guides/{guideId}/publish`

### 5）下线指引

**POST** `/api/admin/v1/rescue/guides/{guideId}/offline`

> 也可以只用 `upsert` 改状态，但拆动作更清楚，便于审计。

---

### B. 资源目录管理（Admin）

### 1）资源列表

**GET** `/api/admin/v1/rescue/resources`

#### Query：`AdminRescueResourceQuery`

* `page`
* `pageSize`
* `status` (optional)
* `cityCode` (optional)
* `resourceType` (optional)
* `keyword` (optional)

---

### 2）资源详情

**GET** `/api/admin/v1/rescue/resources/{resourceId}`

---

### 3）创建/更新资源

**PUT** `/api/admin/v1/rescue/resources/{resourceId}`（或 `POST/PUT` 分开）

#### DTO：`UpsertRescueResourceRequest`

* `resourceType` (required)
* `name` (required, max 200)
* `cityCode` (required)
* `cityName` (required)
* `districtName` (optional)
* `address` (optional)
* `contactPhone` (optional)
* `contactWechat` (optional)
* `contactOther` (optional)
* `serviceHours` (optional)
* `serviceScope` (optional)
* `acceptPetTypes` (optional)
* `capabilityTags` (optional)
* `description` (optional)
* `sourceUrl` (optional)
* `verifiedAt` (optional)
* `sortOrder` (optional)
* `status` (optional: `DRAFT` / `ACTIVE` / `PAUSED` / `OFFLINE`)

#### 规则

* `ACTIVE` 时需校验最小信息完整（名称、城市、至少一种联系方式或地址）
* 写 `admin_audit_logs`

---

### C. 救助线索分流（Admin）

### 1）线索列表

**GET** `/api/admin/v1/rescue/clues`

#### Query：`AdminRescueClueQuery`

* `page`
* `pageSize`
* `status` (optional)
* `urgencyLevel` (optional)
* `cityCode` (optional)
* `petType` (optional)
* `keyword` (optional: clueNo / 联系人 / 位置)
* `dateFrom` / `dateTo` (optional)

#### 返回：`PageResponse<AdminRescueClueListItemDTO>`

* `clueId`
* `clueNo`
* `cityName`
* `districtName`
* `petType`
* `urgencyLevel`
* `status`
* `reporterUserId`
* `handledByAdminName`
* `handledAt`
* `createdAt`

---

### 2）线索详情

**GET** `/api/admin/v1/rescue/clues/{clueId}`

#### 返回：`AdminRescueClueDetailDTO`

包含：

* 线索全部字段（含联系方式明文）
* 图片列表
* 建议资源目录列表
* 处理信息（当前状态、处理人、备注）

---

### 3）更新线索状态（分流/跟进/结案）

**POST** `/api/admin/v1/rescue/clues/{clueId}/status`

#### DTO：`UpdateRescueClueStatusRequest`

* `status` (required)

    * `TRIAGED`
    * `IN_PROGRESS`
    * `RESOLVED`
    * `CLOSED`
    * `INVALID`
* `triageNote` (optional, max 255)
* `resolutionNote` (optional, max 255)
* `suggestedResourceIds` (long[], optional, max 10)

#### 规则

* 仅后台可操作
* 状态流转需校验（见下文）
* 写 `admin_audit_logs`

---

## 4）DTO 字段清单（给 Codex 建类）

---

## 4.1 前台 DTO（用户端）

### `RescueGuideListQuery`

* `page: Integer`
* `pageSize: Integer`
* `scenarioCode: String?`
* `cityCode: String?`
* `keyword: String?`

### `RescueGuideListItemDTO`

* `guideId: Long`
* `scenarioCode: String`
* `title: String`
* `summary: String?`
* `cityCode: String?`
* `tags: List<String>?`
* `publishedAt: String?`
* `sortOrder: Integer`

### `RescueGuideDetailDTO`

* `guideId: Long`
* `scenarioCode: String`
* `title: String`
* `summary: String?`
* `contentMd: String`
* `cityCode: String?`
* `tags: List<String>?`
* `publishedAt: String?`
* `updatedAt: String`

---

### `RescueResourceListQuery`

* `page: Integer`
* `pageSize: Integer`
* `cityCode: String?`
* `resourceType: String?`
* `petType: String?`
* `keyword: String?`

### `RescueResourceListItemDTO`

* `resourceId: Long`
* `resourceType: String`
* `name: String`
* `cityCode: String`
* `cityName: String`
* `districtName: String?`
* `serviceScope: String?`
* `acceptPetTypes: List<String>?`
* `capabilityTags: List<String>?`
* `contactPhoneMasked: String?`
* `verifiedAt: String?`
* `sortOrder: Integer`

### `RescueResourceDetailDTO`

* `resourceId: Long`
* `resourceType: String`
* `name: String`
* `cityCode: String`
* `cityName: String`
* `districtName: String?`
* `address: String?`
* `contactPhone: String?`
* `contactWechat: String?`
* `contactOther: String?`
* `serviceHours: String?`
* `serviceScope: String?`
* `acceptPetTypes: List<String>?`
* `capabilityTags: List<String>?`
* `description: String?`
* `sourceUrl: String?`
* `verifiedAt: String?`

---

### `SubmitRescueClueRequest`

* `cityCode: String`
* `cityName: String`
* `districtName: String?`
* `locationText: String`
* `geoLat: BigDecimal?`
* `geoLng: BigDecimal?`
* `petType: String?`
* `estimatedCount: Integer?`
* `urgencyLevel: String`
* `conditionTags: List<String>?`
* `description: String`
* `contactName: String`
* `contactMobile: String`
* `photoFileIds: List<Long>?`

### `MyRescueClueQuery`

* `page: Integer`
* `pageSize: Integer`
* `status: String?`

### `RescueClueListItemDTO`

* `clueId: Long`
* `clueNo: String`
* `cityName: String`
* `districtName: String?`
* `petType: String?`
* `urgencyLevel: String`
* `status: String`
* `createdAt: String`
* `updatedAt: String`

### `RescueClueDetailDTO`

* `clueId: Long`
* `clueNo: String`
* `cityCode: String`
* `cityName: String`
* `districtName: String?`
* `locationText: String`
* `petType: String?`
* `estimatedCount: Integer?`
* `urgencyLevel: String`
* `conditionTags: List<String>?`
* `description: String`
* `contactName: String`
* `contactMobileMasked: String`
* `status: String`
* `triageNote: String?`
* `resolutionNote: String?`
* `suggestedResources: List<RescueResourceSuggestionDTO>?`
* `photos: List<RescueCluePhotoDTO>`
* `createdAt: String`
* `updatedAt: String`

### `RescueResourceSuggestionDTO`

* `resourceId: Long`
* `name: String`
* `resourceType: String`
* `contactPhone: String?`

### `RescueCluePhotoDTO`

* `fileId: Long`
* `url: String`
* `sortOrder: Integer`

---

## 4.2 Admin DTO

### `AdminRescueGuideQuery`

* `page: Integer`
* `pageSize: Integer`
* `status: String?`
* `scenarioCode: String?`
* `cityCode: String?`
* `keyword: String?`

### `AdminRescueGuideListItemDTO`

* `guideId: Long`
* `scenarioCode: String`
* `title: String`
* `cityCode: String?`
* `status: String`
* `version: Integer`
* `publishedAt: String?`
* `updatedAt: String`

### `UpsertRescueGuideRequest`

* `scenarioCode: String`
* `title: String`
* `summary: String?`
* `contentMd: String`
* `cityCode: String?`
* `tags: List<String>?`
* `sortOrder: Integer?`
* `status: String?`

---

### `AdminRescueResourceQuery`

* `page: Integer`
* `pageSize: Integer`
* `status: String?`
* `cityCode: String?`
* `resourceType: String?`
* `keyword: String?`

### `AdminRescueResourceListItemDTO`

* `resourceId: Long`
* `resourceType: String`
* `name: String`
* `cityName: String`
* `status: String`
* `verifiedAt: String?`
* `updatedAt: String`

### `UpsertRescueResourceRequest`

* `resourceType: String`
* `name: String`
* `cityCode: String`
* `cityName: String`
* `districtName: String?`
* `address: String?`
* `contactPhone: String?`
* `contactWechat: String?`
* `contactOther: String?`
* `serviceHours: String?`
* `serviceScope: String?`
* `acceptPetTypes: List<String>?`
* `capabilityTags: List<String>?`
* `description: String?`
* `sourceUrl: String?`
* `verifiedAt: String?`
* `sortOrder: Integer?`
* `status: String?`

---

### `AdminRescueClueQuery`

* `page: Integer`
* `pageSize: Integer`
* `status: String?`
* `urgencyLevel: String?`
* `cityCode: String?`
* `petType: String?`
* `keyword: String?`
* `dateFrom: String?`
* `dateTo: String?`

### `AdminRescueClueListItemDTO`

* `clueId: Long`
* `clueNo: String`
* `cityName: String`
* `districtName: String?`
* `petType: String?`
* `urgencyLevel: String`
* `status: String`
* `reporterUserId: Long`
* `handledByAdminName: String?`
* `handledAt: String?`
* `createdAt: String`

### `AdminRescueClueDetailDTO`

* `clueId: Long`
* `clueNo: String`
* `reporterUserId: Long`
* `reporterNickname: String?`
* `cityCode: String`
* `cityName: String`
* `districtName: String?`
* `locationText: String`
* `geoLat: BigDecimal?`
* `geoLng: BigDecimal?`
* `petType: String?`
* `estimatedCount: Integer?`
* `urgencyLevel: String`
* `conditionTags: List<String>?`
* `description: String`
* `contactName: String`
* `contactMobile: String`
* `status: String`
* `triageNote: String?`
* `resolutionNote: String?`
* `suggestedResourceIds: List<Long>?`
* `photos: List<RescueCluePhotoDTO>`
* `handledByAdminName: String?`
* `handledAt: String?`
* `createdAt: String`
* `updatedAt: String`

### `UpdateRescueClueStatusRequest`

* `status: String`
* `triageNote: String?`
* `resolutionNote: String?`
* `suggestedResourceIds: List<Long>?`

---

## 5）状态机规则（必须后端校验）

---

## 5.1 指引状态机（`rescue_guides.status`）

### 状态

* `DRAFT`
* `PUBLISHED`
* `OFFLINE`

### 合法流转

* `null -> DRAFT`
* `DRAFT -> PUBLISHED`
* `PUBLISHED -> OFFLINE`
* `OFFLINE -> PUBLISHED`（允许重新上架）
* `PUBLISHED -> DRAFT`（可不开放）

### 规则

* 前台只展示 `PUBLISHED`
* 每次更新内容 `version + 1`
* 发布动作记录 `published_at`

---

## 5.2 资源目录状态机（`rescue_resources.status`）

### 状态

* `DRAFT`
* `ACTIVE`
* `PAUSED`
* `OFFLINE`

### 合法流转

* `null -> DRAFT`
* `DRAFT -> ACTIVE`
* `ACTIVE -> PAUSED`
* `PAUSED -> ACTIVE`
* `ACTIVE -> OFFLINE`
* `PAUSED -> OFFLINE`

### 规则

* 前台只展示 `ACTIVE`
* `ACTIVE` 时必须校验基础联系信息完整（至少一种联系方式或地址）

---

## 5.3 救助线索状态机（`rescue_clues.status`）

### 状态

* `SUBMITTED`      // 用户刚提交
* `TRIAGED`        // 后台已查看并初步分流（给建议资源/备注）
* `IN_PROGRESS`    // 正在跟进（人工）
* `RESOLVED`       // 已解决
* `CLOSED`         // 已关闭（未解决但结束）
* `INVALID`        // 无效线索/重复/恶意

### 合法流转

* `null -> SUBMITTED`
* `SUBMITTED -> TRIAGED`
* `TRIAGED -> IN_PROGRESS`
* `TRIAGED -> RESOLVED`
* `TRIAGED -> CLOSED`
* `TRIAGED -> INVALID`
* `IN_PROGRESS -> RESOLVED`
* `IN_PROGRESS -> CLOSED`
* `IN_PROGRESS -> INVALID`

### 非法流转（拦截）

* `RESOLVED -> *`
* `CLOSED -> *`
* `INVALID -> *`
* `SUBMITTED -> RESOLVED`（建议不允许，要求先分流）

---

## 6）依赖与权限规则（跨模块）

---

## 6.1 与 Phase0 文件上传模块的依赖

### 提交线索图片 `photoFileIds`

必须满足：

* 文件存在
* `file_objects.status = READY`
* 文件归当前登录用户所有
* `bizType` 合法（建议 `RESCUE_CLUE` 或 `OTHER` 兼容）

---

## 6.2 与城市表/系统配置依赖

* `cityCode/cityName` 优先复用已有 `cities` 表（如果 Phase0/前几阶段已建）
* 指引场景枚举和资源类型枚举可同步写入 `system_configs`（可选）

---

## 6.3 权限规则

### 前台

* 指引/资源：公开可访问
* 线索提交与“我的线索”：必须登录
* 线索详情：仅本人可看（Admin 走后台）

### 后台

* 指引/资源维护：`OPERATOR` / `AUDITOR` / `SUPER_ADMIN`（按你现有角色模型）
* 线索状态更新：建议 `CS` / `AUDITOR` / `SUPER_ADMIN`

---

## 7）错误码增量（Phase 5）

新增错误码建议：

* `RESCUE_GUIDE_NOT_FOUND`
* `RESCUE_GUIDE_STATUS_INVALID`
* `RESCUE_RESOURCE_NOT_FOUND`
* `RESCUE_RESOURCE_STATUS_INVALID`
* `RESCUE_CLUE_NOT_FOUND`
* `RESCUE_CLUE_NOT_OWNER`
* `RESCUE_CLUE_STATUS_INVALID`
* `RESCUE_CLUE_FILE_INVALID`
* `RESCUE_CLUE_FILE_NOT_OWNED`
* `RESCUE_CLUE_SUGGESTED_RESOURCE_INVALID`
* `RESCUE_PERMISSION_DENIED`

---

## 8）前端页面任务（用户端 + Admin）

---

## 8.1 用户端（Web Portal）

### 页面 1：救助首页 `/rescue`

内容：

* 场景入口卡片（“发现流浪猫”“发现受伤狗”“幼崽救助”等）
* 本地资源入口（按城市筛选）
* 线索提交入口（醒目）

定位：

* 这个页面是“普通人第一眼就知道该做什么”的入口页

---

### 页面 2：救助指引列表 `/rescue/guides`

内容：

* 场景筛选、城市筛选、关键词
* 指引卡片列表（标题、摘要、标签）

---

### 页面 3：救助指引详情 `/rescue/guides/:guideId`

内容：

* Markdown 渲染（步骤清晰）
* 注意事项（高亮）
* 相关资源推荐入口（同城资源）

---

### 页面 4：救助资源目录 `/rescue/resources`

内容：

* 城市/类型/宠物类型筛选
* 资源卡片（名称、类型、服务范围、联系方式、核验时间）

---

### 页面 5：救助资源详情 `/rescue/resources/:resourceId`

内容：

* 完整联系信息
* 地址、服务时间、能力标签
* 说明文本

---

### 页面 6：提交救助线索 `/rescue/clues/new`

内容：

* 地点（城市/区域/位置描述）
* 宠物情况（类型、数量、紧急程度、情况标签）
* 文字描述
* 联系方式
* 图片上传（1~9）
* 提交按钮

交互：

* 登录校验
* 提交成功后跳“我的线索详情”

---

### 页面 7：我的线索 `/me/rescue/clues`

内容：

* 列表（编号、城市、紧急程度、状态、时间）
* 状态标签清晰（已分流/跟进中/已解决）

---

### 页面 8：线索详情 `/me/rescue/clues/:clueId`

内容：

* 我提交的信息
* 图片
* 当前状态
* 后台给的分流备注（triageNote）
* 推荐资源（如果有）

---

## 8.2 Admin（Admin Console）

### 页面 1：救助指引列表 `/rescue/guides`

* 列表 + 筛选 + 状态标签
* 进入编辑页

### 页面 2：救助指引编辑 `/rescue/guides/:id`

* 表单编辑（标题、摘要、Markdown 内容、标签、状态）
* 支持发布/下线按钮

### 页面 3：救助资源列表 `/rescue/resources`

* 列表 + 城市/类型/状态筛选
* 进入编辑页

### 页面 4：救助资源编辑 `/rescue/resources/:id`

* 完整表单编辑（联系方式/地址/能力标签）
* 支持激活/暂停/下线

### 页面 5：救助线索列表 `/rescue/clues`

* 筛选（状态、紧急度、城市、时间）
* 列表字段突出紧急程度和状态

### 页面 6：救助线索详情 `/rescue/clues/:id`

* 展示线索内容和图片
* 状态更新弹窗（分流/跟进/结案）
* 可选择推荐资源目录（多选）

---

## 9）Codex 分任务投喂顺序（第5阶段）

---

## Task 5.1：数据库迁移 + 实体 + 枚举（MySQL）

**目标**

* 新增 4 张表：

    * `rescue_guides`
    * `rescue_resources`
    * `rescue_clues`
    * `rescue_clue_media`
* MySQL Flyway 脚本
* 实体、枚举、Repository

**验收**

* Flyway 成功
* 表结构/索引正确
* 项目可启动

---

## Task 5.2：前台指引与资源接口（公开）

**目标**

* `GET /api/v1/rescue/guides`
* `GET /api/v1/rescue/guides/{id}`
* `GET /api/v1/rescue/resources`
* `GET /api/v1/rescue/resources/{id}`

**要求**

* 仅返回 `PUBLISHED`/`ACTIVE`
* 支持基础筛选与分页
* DTO 参数校验和统一响应格式

**验收**

* 未登录可正常浏览指南和资源目录

---

## Task 5.3：线索提交与我的线索接口（前台）

**目标**

* `POST /api/v1/rescue/clues`
* `GET /api/v1/rescue/clues/my`
* `GET /api/v1/rescue/clues/{id}`

**要求**

* 校验图片文件归属与 `READY`
* 生成 `clue_no`
* 联系电话保存明文 + 脱敏字段
* 仅本人可看详情

**验收**

* 用户可提线索并看到状态 `SUBMITTED`
* 我的线索列表/详情可查看

---

## Task 5.4：后台管理接口（指引/资源/线索）

**目标**

* 指引：列表/详情/创建更新/发布/下线
* 资源：列表/详情/创建更新/激活暂停下线
* 线索：列表/详情/更新状态

**要求**

* 所有后台动作写 `admin_audit_logs`
* 线索状态流转严格校验
* `suggestedResourceIds` 需校验资源存在且同城（建议）

**验收**

* 后台可维护指引和资源
* 后台可分流线索并更新状态
* 前台线索详情能看到分流备注和推荐资源

---

## Task 5.5：用户端页面（救助首页 + 指引/资源 + 线索）

**目标**

* 救助首页
* 指引列表/详情
* 资源目录列表/详情
* 线索提交页
* 我的线索列表/详情

**要求**

* 风格延续“温暖治愈 + 专业可信”
* 指引详情支持 Markdown 渲染
* 线索状态标签清晰

**验收**

* 用户从“看指引 -> 看资源 -> 提线索”闭环跑通

---

## Task 5.6：Admin 页面（指引/资源/线索）

**目标**

* 指引列表 + 编辑
* 资源列表 + 编辑
* 线索列表 + 详情 + 状态更新弹窗

**验收**

* 管理后台可完整维护救助模块数据

---

## Task 5.7：联调与回归测试

**重点回归项**

* 前台只能看到已发布指引和激活资源
* 线索图片归属校验正确
* 线索详情权限正确（仅本人）
* 后台状态流转符合状态机
* 推荐资源能正确回显到用户端
* 审计日志完整记录后台动作

---

## 10）给 Codex 的执行提示词（可直接复制）

```text
实现第5阶段（宠物救助指引模块），基于已完成的 Phase 0 + Phase 2 认证 + Phase 3 领养/送养 + Phase 4 上门喂养继续开发，并使用 MySQL。

【本阶段范围】
1) 救助指引（列表/详情，后台可维护）
2) 本地救助资源目录（列表/详情，后台可维护）
3) 用户提交救助线索（含图片）
4) 我的线索列表/详情
5) 后台线索分流与状态更新（基础版）

【严格边界】
- 不做实时派单
- 不做聊天
- 不做捐款/支付
- 不做投诉仲裁
- 不做复杂工单和自动催办

【数据库要求（MySQL）】
新增表：
- rescue_guides
- rescue_resources
- rescue_clues
- rescue_clue_media

要求：
- JSON 字段使用 MySQL json
- 时间字段使用 datetime(3)
- 所有表 utf8mb4
- Flyway 迁移脚本使用 MySQL 语法

【前台接口】
- 指引列表/详情（公开）
- 资源目录列表/详情（公开）
- 提交线索（登录）
- 我的线索列表/详情（仅本人）

【后台接口】
- 指引管理（列表/详情/创建更新/发布/下线）
- 资源管理（列表/详情/创建更新/激活暂停下线）
- 线索管理（列表/详情/状态更新）

【关键业务规则】
- 前台只展示 PUBLISHED 指引 和 ACTIVE 资源
- 提交线索时校验 photoFileIds：
  - 文件存在
  - status=READY
  - 归当前用户所有
- 线索状态机严格实现：
  SUBMITTED -> TRIAGED -> IN_PROGRESS -> RESOLVED/CLOSED/INVALID
- 后台状态更新动作写 admin_audit_logs
- suggestedResourceIds 必须校验资源存在（建议同城校验）

【前端要求】
用户端页面：
- 救助首页
- 指引列表/详情
- 资源目录列表/详情
- 提交线索页
- 我的线索列表/详情

Admin 页面：
- 指引列表/编辑
- 资源列表/编辑
- 线索列表/详情（状态更新）

先保证功能闭环和信息结构清晰，再优化样式。

【验收标准】
- 用户可浏览救助指引和本地资源目录
- 用户可提交救助线索并查看状态
- 后台可维护指引和资源
- 后台可分流/跟进/结案线索
- 用户端可看到后台分流备注与推荐资源
```

---

## 11）第5阶段验收清单（你自己验）

* [ ] Flyway 新增救助模块表迁移成功（MySQL）
* [ ] 前台可浏览救助指引列表/详情（仅已发布）
* [ ] 前台可浏览救助资源目录（仅激活资源）
* [ ] 用户可提交救助线索（含图片）
* [ ] 我的线索列表/详情可查看
* [ ] 线索详情权限正确（仅本人）
* [ ] 后台可维护指引（发布/下线）
* [ ] 后台可维护资源（激活/暂停/下线）
* [ ] 后台可更新线索状态（分流/跟进/结案）
* [ ] 用户端可看到后台的分流备注与推荐资源
* [ ] 后台动作写入审计日志

---
