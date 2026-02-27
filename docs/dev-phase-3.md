---

# 第3阶段技术任务单（给 Codex）

## 模块：领养/送养（MVP 以“送养帖 + 领养申请”为主）

---

## 0）阶段目标与边界

### 本阶段目标

实现 **领养/送养核心闭环（MVP）**：

1. 用户发布 **送养帖（待领养宠物）**
2. 后台审核帖子（通过/驳回/下架）
3. 用户浏览列表和详情
4. 用户提交领养申请
5. 发布者处理申请（接受/拒绝）
6. 状态流转完整、可追踪

---

### 本阶段边界（严格控制）

### ✅ 本期做

* 送养帖发布（含宠物信息 + 宠物图片）
* 帖子审核（后台）
* 列表/详情浏览
* 领养申请提交
* 发布者处理申请（接受/拒绝）
* 我的发布 / 我的申请
* 审计日志（后台审核动作）

### ❌ 本期不做

* “求领帖”（用户发帖求领养）——**先不做**
* 站内即时聊天（用“申请留言”代替）
* 线上支付 / 保证金
* 回访系统（D7/D30）——后续阶段再加
* 举报投诉（放到第6阶段风控）
* 推荐算法/匹配算法

> 说明：虽然模块叫“领养/送养”，但 MVP 先做 **送养帖 + 领养申请**，这样闭环更清晰，也更贴近你最初的需求（“不知道去哪领养/送养”）。

---

## 1）目录结构增量（在现有项目基础上新增）

> 只列新增模块和页面，Codex 按这个放文件。

### 后端（Spring Boot）

```text id="6u0v2d"
services/api-server/src/main/java/com/petplatform/modules/
├─ adoption/
│  ├─ controller/
│  │  ├─ AdoptionPostController.java          // 用户端：列表、详情、发布、我的发布、关闭
│  │  ├─ AdoptionApplicationController.java   // 用户端：申请、我的申请、处理申请
│  │  └─ AdminAdoptionController.java         // 后台：审核、下架、列表详情
│  ├─ dto/
│  │  ├─ user/
│  │  │  ├─ CreateRehomePostRequest.java
│  │  │  ├─ UpdateRehomePostRequest.java              // 可选（若本期支持编辑）
│  │  │  ├─ RehomePostListItemDTO.java
│  │  │  ├─ RehomePostDetailDTO.java
│  │  │  ├─ MyRehomePostListItemDTO.java
│  │  │  ├─ SubmitAdoptionApplicationRequest.java
│  │  │  ├─ AdoptionApplicationDTO.java
│  │  │  ├─ HandleAdoptionApplicationRequest.java
│  │  │  └─ MyAdoptionApplicationListItemDTO.java
│  │  └─ admin/
│  │     ├─ AdminAdoptionPostQuery.java
│  │     ├─ AdminAdoptionPostListItemDTO.java
│  │     ├─ AdminAdoptionPostDetailDTO.java
│  │     ├─ ApproveAdoptionPostRequest.java
│  │     └─ RejectAdoptionPostRequest.java
│  ├─ service/
│  │  ├─ AdoptionPostService.java
│  │  ├─ AdoptionApplicationService.java
│  │  ├─ AdminAdoptionService.java
│  │  └─ impl/
│  ├─ entity/
│  │  ├─ PetEntity.java
│  │  ├─ PetMediaEntity.java
│  │  ├─ AdoptionPostEntity.java
│  │  └─ AdoptionApplicationEntity.java
│  ├─ repository/
│  │  ├─ PetRepository.java
│  │  ├─ PetMediaRepository.java
│  │  ├─ AdoptionPostRepository.java
│  │  └─ AdoptionApplicationRepository.java
│  └─ enums/
│     ├─ PetType.java
│     ├─ PetGender.java
│     ├─ NeuteredStatus.java
│     ├─ VaccinatedStatus.java
│     ├─ AdoptionPostStatus.java
│     └─ AdoptionApplicationStatus.java
```

---

### 前端（用户端 Web）

```text id="6uowju"
apps/web-portal/src/pages/
├─ Adoption/
│  ├─ AdoptionListPage.tsx                  // 领养列表页
│  ├─ AdoptionDetailPage.tsx                // 详情页
│  ├─ RehomePostCreatePage.tsx              // 发布送养帖
│  ├─ MyRehomePostsPage.tsx                 // 我的发布
│  ├─ MyRehomePostApplicationsPage.tsx      // 我发布的帖子收到的申请
│  ├─ MyAdoptionApplicationsPage.tsx        // 我的领养申请
│  └─ components/
│     ├─ PetCard.tsx
│     ├─ PostStatusTag.tsx
│     ├─ ApplicationStatusTag.tsx
│     └─ PetInfoForm.tsx
```

---

### 前端（Admin）

```text id="jqm8u2"
apps/admin-console/src/pages/
├─ Adoptions/
│  ├─ AdoptionPostReviewListPage.tsx        // 审核列表
│  ├─ AdoptionPostReviewDetailPage.tsx      // 审核详情
│  └─ components/
│     ├─ ReviewActionModal.tsx
│     └─ AdoptionPostStatusTag.tsx
```

---

## 2）数据库设计（MySQL 版本）

> 本阶段新增 4 张核心表（MySQL 8.x）
> 使用 `utf8mb4`，时间字段建议 `datetime(3)`，JSON 用 `json`。

---

## 2.1 新增表：`pets`

用途：宠物实体（送养帖绑定的宠物信息）

### 字段（MySQL）

* `id` bigint auto_increment pk
* `owner_user_id` bigint not null  （发布者）
* `pet_type` varchar(16) not null  (`CAT` / `DOG`)
* `name` varchar(64) null
* `gender` varchar(16) null (`MALE` / `FEMALE` / `UNKNOWN`)
* `age_months` int null
* `breed` varchar(128) null
* `weight_kg` decimal(5,2) null
* `neutered_status` varchar(16) null (`YES` / `NO` / `UNKNOWN`)
* `vaccinated_status` varchar(16) null (`YES` / `NO` / `PARTIAL` / `UNKNOWN`)
* `health_note` text null
* `temperament_tags` json null          // 例如 ["亲人","怕生","活泼"]
* `special_care_note` text null
* `created_at` datetime(3) not null
* `updated_at` datetime(3) not null

### 索引

* `idx_pets_owner_user_id (owner_user_id)`
* `idx_pets_pet_type (pet_type)`

---

## 2.2 新增表：`pet_media`

用途：宠物图片/视频（MVP 先支持图片）

### 字段

* `id` bigint auto_increment pk
* `pet_id` bigint not null
* `file_object_id` bigint not null            // 关联 Phase0 的 file_objects
* `media_type` varchar(16) not null           // `IMAGE`（先只支持 IMAGE）
* `sort_order` int not null default 0
* `created_at` datetime(3) not null

### 索引/约束

* `idx_pet_media_pet_id_sort (pet_id, sort_order)`
* FK:

    * `pet_id -> pets.id`
    * `file_object_id -> file_objects.id`

---

## 2.3 新增表：`adoption_posts`

用途：送养帖主表（待审核/已发布等）

### 字段

* `id` bigint auto_increment pk
* `publisher_user_id` bigint not null
* `pet_id` bigint not null
* `title` varchar(200) not null
* `content` text not null
* `city_code` varchar(32) not null
* `city_name` varchar(64) not null
* `district_name` varchar(64) null
* `status` varchar(32) not null

    * `PENDING_REVIEW`
    * `PUBLISHED`
    * `REJECTED`
    * `CLOSED`
    * `OFFLINE`
* `submit_version` int not null default 1
* `reject_reason_code` varchar(64) null
* `reject_reason_text` varchar(255) null
* `reviewed_by_admin_id` bigint null
* `reviewed_at` datetime(3) null
* `published_at` datetime(3) null
* `closed_at` datetime(3) null
* `view_count` int not null default 0
* `created_at` datetime(3) not null
* `updated_at` datetime(3) not null

### 索引

* `idx_adoption_posts_city_status (city_code, status)`
* `idx_adoption_posts_publisher_status (publisher_user_id, status)`
* `idx_adoption_posts_pet_id (pet_id)`
* `idx_adoption_posts_updated_at (updated_at)`

---

## 2.4 新增表：`adoption_applications`

用途：领养申请

### 字段

* `id` bigint auto_increment pk
* `post_id` bigint not null
* `applicant_user_id` bigint not null
* `message` text not null
* `living_env_note` text null          // 可选：居住环境简述
* `pet_experience_note` text null      // 可选：养宠经验
* `status` varchar(32) not null

    * `SUBMITTED`
    * `ACCEPTED`
    * `REJECTED`
    * `WITHDRAWN`
* `handled_by_user_id` bigint null      // 通常是帖子发布者
* `handled_at` datetime(3) null
* `decision_note` varchar(255) null
* `created_at` datetime(3) not null
* `updated_at` datetime(3) not null

### 约束/索引

* 唯一约束：`uk_post_applicant (post_id, applicant_user_id)`
* 索引：

    * `idx_adoption_applications_post_status (post_id, status)`
    * `idx_adoption_applications_applicant_status (applicant_user_id, status)`

---

## 2.5 Migration 文件建议（MySQL）

```text id="xvjlwm"
V7__create_pets_and_pet_media.sql
V8__create_adoption_posts.sql
V9__create_adoption_applications.sql
V10__add_adoption_indexes_and_seed_configs.sql   // 可选
```

> 注意：SQL 语法按 MySQL 写（`json`、`bigint auto_increment`、`datetime(3)`、`tinyint(1)` 等）。

---

## 3）接口清单（Phase 3）

---

## 3.1 用户端接口（`/api/v1/adoptions/**`）

---

### A. 领养列表页（公开可访问）

### 1）获取送养帖列表

**GET** `/api/v1/adoptions/posts`

#### Query 参数

* `page` (required)
* `pageSize` (required)
* `cityCode` (optional)
* `petType` (optional: `CAT` / `DOG`)
* `keyword` (optional)
* `status` 不对用户开放（固定查 `PUBLISHED`）

#### 返回 DTO：`PageResponse<RehomePostListItemDTO>`

字段建议：

* `postId`
* `title`
* `cityCode`
* `cityName`
* `districtName`
* `petType`
* `petName`
* `petGender`
* `ageMonths`
* `breed`
* `coverImageUrl`
* `temperamentTags` (string[])
* `publishedAt`
* `viewCount`

---

### 2）获取送养帖详情

**GET** `/api/v1/adoptions/posts/{postId}`

#### 返回 DTO：`RehomePostDetailDTO`

* `postId`
* `title`
* `content`
* `status`（用户端仅对发布者本人展示真实状态；普通人仅能访问 PUBLISHED）
* `cityCode`
* `cityName`
* `districtName`
* `publisher`

    * `userId`
    * `nickname`
    * `avatarUrl`
    * `isRealNameVerified`（可选，增强信任）
* `pet`

    * `petId`
    * `petType`
    * `name`
    * `gender`
    * `ageMonths`
    * `breed`
    * `weightKg`
    * `neuteredStatus`
    * `vaccinatedStatus`
    * `healthNote`
    * `temperamentTags`
    * `specialCareNote`
    * `media` (数组：`fileId/url/sortOrder`)
* `publishedAt`
* `viewCount`
* `applicationStats`

    * `total`
    * `acceptedCount`
* `viewerContext`（登录时）

    * `hasApplied` (bool)
    * `canApply` (bool)
    * `cannotApplyReason` (nullable)

---

### B. 发布与我的帖子（需登录）

### 3）发布送养帖（创建并提交审核）

**POST** `/api/v1/adoptions/posts`

#### 前置规则

* 必须登录
* 建议强制 **实名认证已通过**（推荐，提升可信度）

    * 若你想放宽，也可以只要求登录；但我建议这里强制实名

#### DTO：`CreateRehomePostRequest`

* `title` (string, required, max 200)

* `content` (string, required, max 5000)

* `cityCode` (string, required)

* `cityName` (string, required)

* `districtName` (string, optional)

* `petType` (string, required: `CAT` / `DOG`)

* `petName` (string, optional, max 64)

* `petGender` (string, optional)

* `ageMonths` (int, optional, 0~360)

* `breed` (string, optional, max 128)

* `weightKg` (decimal/string, optional)

* `neuteredStatus` (string, optional)

* `vaccinatedStatus` (string, optional)

* `healthNote` (string, optional, max 1000)

* `temperamentTags` (string[], optional, max 10)

* `specialCareNote` (string, optional, max 1000)

* `petImageFileIds` (long[], required, min 1, max 9)

#### 返回 DTO：`RehomePostDetailDTO`

* 返回状态应为 `PENDING_REVIEW`

---

### 4）获取我的送养帖列表

**GET** `/api/v1/adoptions/my/posts`

#### Query

* `page`
* `pageSize`
* `status` (optional)

#### 返回 DTO：`PageResponse<MyRehomePostListItemDTO>`

* `postId`
* `title`
* `status`
* `coverImageUrl`
* `cityName`
* `applicationCount`
* `rejectReasonText` (若被驳回)
* `updatedAt`
* `publishedAt`

---

### 5）重新提交审核（驳回后）

**POST** `/api/v1/adoptions/posts/{postId}/resubmit`

#### 规则

* 仅帖子发布者可操作
* 仅 `REJECTED` 状态可操作
* `submit_version + 1`
* 状态改为 `PENDING_REVIEW`
* 清空驳回信息（或保留历史仅当前字段清空）

#### 返回

* `Verification/Status` 类似风格即可（或直接返回更新后的帖子 DTO）

---

### 6）关闭帖子（已送养完成 / 不再开放）

**POST** `/api/v1/adoptions/posts/{postId}/close`

#### 规则

* 仅帖子发布者可操作
* 仅 `PUBLISHED` 状态可操作
* 改为 `CLOSED`

---

### C. 领养申请（需登录）

### 7）提交领养申请

**POST** `/api/v1/adoptions/posts/{postId}/applications`

#### 规则

* 必须登录
* 帖子必须 `PUBLISHED`
* 不能申请自己的帖子
* 同一用户对同一帖子只能提交一次（依赖唯一约束）
* 若帖子已 `CLOSED/OFFLINE` 不允许申请

#### DTO：`SubmitAdoptionApplicationRequest`

* `message` (string, required, max 2000)
* `livingEnvNote` (string, optional, max 1000)
* `petExperienceNote` (string, optional, max 1000)

#### 返回 DTO：`AdoptionApplicationDTO`

* `applicationId`
* `postId`
* `status`
* `createdAt`

---

### 8）获取我的领养申请列表（我是申请人）

**GET** `/api/v1/adoptions/my/applications`

#### Query

* `page`
* `pageSize`
* `status` (optional)

#### 返回 DTO：`PageResponse<MyAdoptionApplicationListItemDTO>`

* `applicationId`
* `postId`
* `postTitle`
* `postCoverImageUrl`
* `status`
* `cityName`
* `createdAt`
* `handledAt`

---

### 9）获取某个帖子的申请列表（我是发布者）

**GET** `/api/v1/adoptions/posts/{postId}/applications`

#### 规则

* 仅帖子发布者可查看

#### 返回 DTO：`PageResponse<AdoptionApplicationDTO>`

字段：

* `applicationId`
* `postId`
* `applicant`

    * `userId`
    * `nickname`
    * `avatarUrl`
    * `isRealNameVerified`（可选）
* `message`
* `livingEnvNote`
* `petExperienceNote`
* `status`
* `createdAt`
* `handledAt`
* `decisionNote`

---

### 10）处理申请（接受/拒绝）

**POST** `/api/v1/adoptions/applications/{applicationId}/handle`

#### 规则

* 仅该帖子发布者可操作
* 仅 `SUBMITTED` 可处理
* `action = ACCEPT` 时：

    * 当前申请 -> `ACCEPTED`
    * 帖子 -> `CLOSED`（自动关闭）
    * 其他同帖 `SUBMITTED` 申请可自动置 `REJECTED`（推荐做）
* `action = REJECT` 时：

    * 当前申请 -> `REJECTED`

#### DTO：`HandleAdoptionApplicationRequest`

* `action` (string, required: `ACCEPT` / `REJECT`)
* `decisionNote` (string, optional, max 255)

---

### 11）撤回申请（我是申请人）

**POST** `/api/v1/adoptions/applications/{applicationId}/withdraw`

#### 规则

* 仅申请人可操作
* 仅 `SUBMITTED` 可撤回
* 状态 -> `WITHDRAWN`

---

## 3.2 Admin 接口（`/api/admin/v1/adoptions/**`）

---

### 1）审核列表（帖子）

**GET** `/api/admin/v1/adoptions/posts`

#### Query：`AdminAdoptionPostQuery`

* `page`
* `pageSize`
* `status` (optional: `PENDING_REVIEW/PUBLISHED/REJECTED/CLOSED/OFFLINE`)
* `cityCode` (optional)
* `petType` (optional)
* `keyword` (optional: 标题/用户ID/昵称)
* `dateFrom` / `dateTo` (optional)

#### 返回：`PageResponse<AdminAdoptionPostListItemDTO>`

* `postId`
* `title`
* `status`
* `publisherUserId`
* `publisherNickname`
* `cityName`
* `petType`
* `submitVersion`
* `reviewedByAdminName`
* `reviewedAt`
* `updatedAt`

---

### 2）审核详情（帖子）

**GET** `/api/admin/v1/adoptions/posts/{postId}`

#### 返回：`AdminAdoptionPostDetailDTO`

包含：

* 帖子基础信息
* 发布者信息（脱敏）
* 宠物详细信息
* 宠物图片 URLs
* 审核信息（状态、版本、驳回原因、审核人）
* 申请统计（数量）
* 时间字段

---

### 3）审核通过

**POST** `/api/admin/v1/adoptions/posts/{postId}/approve`

#### DTO：`ApproveAdoptionPostRequest`

* `remark` (string, optional, max 200)

#### 规则

* 仅 `PENDING_REVIEW` 可通过
* 状态 -> `PUBLISHED`
* 写 `reviewed_by_admin_id/reviewed_at/published_at`
* 写 `admin_audit_logs`

---

### 4）审核驳回

**POST** `/api/admin/v1/adoptions/posts/{postId}/reject`

#### DTO：`RejectAdoptionPostRequest`

* `rejectReasonCode` (string, required)

    * 建议枚举：

        * `TITLE_OR_CONTENT_INVALID`
        * `PET_INFO_INCOMPLETE`
        * `PET_IMAGES_INVALID`
        * `CONTACT_OR_SCAM_RISK`
        * `DUPLICATE_POST`
        * `OTHER`
* `rejectReasonText` (string, required, max 255)
* `remark` (string, optional)

#### 规则

* 仅 `PENDING_REVIEW` 可驳回
* 状态 -> `REJECTED`
* 写审计日志

---

### 5）后台下架（强制）

**POST** `/api/admin/v1/adoptions/posts/{postId}/offline`

#### DTO

* `reason` (string, required, max 255)

#### 规则

* `PUBLISHED` / `CLOSED` / `REJECTED` 均可下架（按你需求）
* 状态 -> `OFFLINE`
* 写审计日志

---

## 4）DTO 字段清单（给 Codex 建类）

---

## 4.1 用户端 DTO

### `CreateRehomePostRequest`

* `title: String`

* `content: String`

* `cityCode: String`

* `cityName: String`

* `districtName: String?`

* `petType: String`

* `petName: String?`

* `petGender: String?`

* `ageMonths: Integer?`

* `breed: String?`

* `weightKg: BigDecimal?`

* `neuteredStatus: String?`

* `vaccinatedStatus: String?`

* `healthNote: String?`

* `temperamentTags: List<String>?`

* `specialCareNote: String?`

* `petImageFileIds: List<Long>`

---

### `RehomePostListItemDTO`

* `postId: Long`
* `title: String`
* `cityCode: String`
* `cityName: String`
* `districtName: String?`
* `petType: String`
* `petName: String?`
* `petGender: String?`
* `ageMonths: Integer?`
* `breed: String?`
* `coverImageUrl: String?`
* `temperamentTags: List<String>?`
* `publishedAt: String?`
* `viewCount: Integer`

---

### `RehomePostDetailDTO`

* `postId: Long`
* `title: String`
* `content: String`
* `status: String`
* `cityCode: String`
* `cityName: String`
* `districtName: String?`
* `publisher: RehomePostPublisherDTO`
* `pet: RehomePostPetDTO`
* `publishedAt: String?`
* `viewCount: Integer`
* `applicationStats: ApplicationStatsDTO`
* `viewerContext: ViewerContextDTO?`

#### `RehomePostPublisherDTO`

* `userId: Long`
* `nickname: String`
* `avatarUrl: String?`
* `isRealNameVerified: Boolean?`

#### `RehomePostPetDTO`

* `petId: Long`
* `petType: String`
* `name: String?`
* `gender: String?`
* `ageMonths: Integer?`
* `breed: String?`
* `weightKg: BigDecimal?`
* `neuteredStatus: String?`
* `vaccinatedStatus: String?`
* `healthNote: String?`
* `temperamentTags: List<String>?`
* `specialCareNote: String?`
* `media: List<PetMediaDTO>`

#### `PetMediaDTO`

* `fileId: Long`
* `url: String`
* `sortOrder: Integer`

#### `ApplicationStatsDTO`

* `total: Integer`
* `acceptedCount: Integer`

#### `ViewerContextDTO`

* `hasApplied: Boolean`
* `canApply: Boolean`
* `cannotApplyReason: String?`

---

### `SubmitAdoptionApplicationRequest`

* `message: String`
* `livingEnvNote: String?`
* `petExperienceNote: String?`

---

### `AdoptionApplicationDTO`

* `applicationId: Long`
* `postId: Long`
* `applicant: ApplicantSummaryDTO?`   // 对“发布者查看申请”场景返回
* `message: String`
* `livingEnvNote: String?`
* `petExperienceNote: String?`
* `status: String`
* `createdAt: String`
* `handledAt: String?`
* `decisionNote: String?`

#### `ApplicantSummaryDTO`

* `userId: Long`
* `nickname: String`
* `avatarUrl: String?`
* `isRealNameVerified: Boolean?`

---

### `HandleAdoptionApplicationRequest`

* `action: String` (`ACCEPT` / `REJECT`)
* `decisionNote: String?`

---

### `MyRehomePostListItemDTO`

* `postId: Long`
* `title: String`
* `status: String`
* `coverImageUrl: String?`
* `cityName: String`
* `applicationCount: Integer`
* `rejectReasonText: String?`
* `updatedAt: String`
* `publishedAt: String?`

---

### `MyAdoptionApplicationListItemDTO`

* `applicationId: Long`
* `postId: Long`
* `postTitle: String`
* `postCoverImageUrl: String?`
* `status: String`
* `cityName: String`
* `createdAt: String`
* `handledAt: String?`

---

## 4.2 Admin DTO

### `AdminAdoptionPostQuery`

* `page: Integer`
* `pageSize: Integer`
* `status: String?`
* `cityCode: String?`
* `petType: String?`
* `keyword: String?`
* `dateFrom: String?`
* `dateTo: String?`

---

### `AdminAdoptionPostListItemDTO`

* `postId: Long`
* `title: String`
* `status: String`
* `publisherUserId: Long`
* `publisherNickname: String`
* `cityName: String`
* `petType: String`
* `submitVersion: Integer`
* `reviewedByAdminName: String?`
* `reviewedAt: String?`
* `updatedAt: String`

---

### `AdminAdoptionPostDetailDTO`

* `postId: Long`

* `title: String`

* `content: String`

* `status: String`

* `submitVersion: Integer`

* `publisherUserId: Long`

* `publisherNickname: String`

* `publisherMobileMasked: String?`

* `cityCode: String`

* `cityName: String`

* `districtName: String?`

* `petType: String`

* `petName: String?`

* `petGender: String?`

* `ageMonths: Integer?`

* `breed: String?`

* `weightKg: BigDecimal?`

* `neuteredStatus: String?`

* `vaccinatedStatus: String?`

* `healthNote: String?`

* `temperamentTags: List<String>?`

* `specialCareNote: String?`

* `petImages: List<String>`

* `applicationCount: Integer`

* `rejectReasonCode: String?`

* `rejectReasonText: String?`

* `reviewedByAdminName: String?`

* `reviewedAt: String?`

* `publishedAt: String?`

* `closedAt: String?`

* `createdAt: String`

* `updatedAt: String`

---

### `ApproveAdoptionPostRequest`

* `remark: String?`

### `RejectAdoptionPostRequest`

* `rejectReasonCode: String`
* `rejectReasonText: String`
* `remark: String?`

---

## 5）状态机规则（必须后端校验）

---

## 5.1 帖子状态机（`adoption_posts.status`）

### 状态

* `PENDING_REVIEW`
* `PUBLISHED`
* `REJECTED`
* `CLOSED`
* `OFFLINE`

### 合法流转

* 新建发布：`null -> PENDING_REVIEW`
* 审核通过：`PENDING_REVIEW -> PUBLISHED`
* 审核驳回：`PENDING_REVIEW -> REJECTED`
* 驳回重提：`REJECTED -> PENDING_REVIEW`（`submit_version + 1`）
* 用户关闭：`PUBLISHED -> CLOSED`
* 后台下架：`PUBLISHED -> OFFLINE`
* 后台下架：`CLOSED -> OFFLINE`
* （可选）后台下架：`REJECTED -> OFFLINE`

### 非法流转（拦截）

* `PUBLISHED -> PENDING_REVIEW`（本期不支持编辑重审）
* `CLOSED -> PUBLISHED`
* `OFFLINE -> *`（本期视为终态）

---

## 5.2 申请状态机（`adoption_applications.status`）

### 状态

* `SUBMITTED`
* `ACCEPTED`
* `REJECTED`
* `WITHDRAWN`

### 合法流转

* 提交申请：`null -> SUBMITTED`
* 发布者接受：`SUBMITTED -> ACCEPTED`
* 发布者拒绝：`SUBMITTED -> REJECTED`
* 申请人撤回：`SUBMITTED -> WITHDRAWN`

### 关联规则（很关键）

* 当某申请 `ACCEPTED` 时：

    * 所属帖子自动 `PUBLISHED -> CLOSED`
    * 同帖其他 `SUBMITTED` 申请建议自动改为 `REJECTED`（推荐实现，避免悬挂状态）

---

## 5.3 依赖与权限规则

### 发布送养帖前置

* 推荐强制：用户 `实名认证 = APPROVED`
* 否则返回错误码：`ADOPTION_REAL_NAME_REQUIRED`

### 文件校验

`petImageFileIds` 必须满足：

* 文件存在
* `file_objects.status = READY`
* 文件归当前用户所有
* `bizType` 合法（建议 `PET_MEDIA` 或 `OTHER` 先兼容）

### 帖子详情访问

* 普通用户只能查看 `PUBLISHED` 帖子
* 帖子发布者本人可查看自己的所有状态帖子
* Admin 走后台接口查看全部

### 申请权限

* 不能申请自己的帖子
* 不能重复申请同一帖子（唯一约束）
* 帖子非 `PUBLISHED` 不能申请

---

## 6）错误码增量（Phase 3）

在现有错误码基础上新增：

* `ADOPTION_REAL_NAME_REQUIRED`
* `ADOPTION_POST_NOT_FOUND`
* `ADOPTION_POST_STATUS_INVALID`
* `ADOPTION_POST_NOT_OWNER`
* `ADOPTION_POST_REVIEW_NOT_ALLOWED`
* `ADOPTION_POST_FILE_INVALID`
* `ADOPTION_POST_FILE_NOT_OWNED`
* `ADOPTION_APPLICATION_NOT_FOUND`
* `ADOPTION_APPLICATION_DUPLICATE`
* `ADOPTION_APPLICATION_NOT_ALLOWED`
* `ADOPTION_APPLICATION_NOT_OWNER`
* `ADOPTION_APPLICATION_HANDLE_NOT_ALLOWED`
* `ADOPTION_CANNOT_APPLY_OWN_POST`

---

## 7）前端页面任务（用户端 + Admin）

---

## 7.1 用户端（Web Portal）

### 页面 1：领养列表页 `/adoption`

内容：

* 顶部筛选（城市、猫/狗、关键词）
* 卡片列表（宠物图、标题、基本信息、标签）
* 分页

交互：

* 点卡片进入详情
* 未登录也可浏览

---

### 页面 2：领养详情页 `/adoption/:postId`

内容：

* 图片轮播（宠物图）
* 宠物信息卡（年龄、品种、绝育、疫苗、性格）
* 帖子正文
* 发布者信息（昵称、可选实名标记）
* 申请按钮 / 已申请状态

交互：

* 登录后可提交申请（弹窗或独立表单）
* 已申请则显示状态，不重复申请

---

### 页面 3：发布送养帖 `/adoption/rehome/new`

内容：

* 帖子信息表单
* 宠物信息表单
* 图片上传（1~9）
* 提交按钮

交互：

* 未实名认证用户拦截并提示去认证
* 提交成功后跳转“我的发布”，状态显示“待审核”

---

### 页面 4：我的发布 `/me/adoption/posts`

内容：

* 我的帖子列表
* 状态标签（待审/已发布/驳回/已关闭/已下架）
* 驳回原因展示
* 操作按钮：

    * 查看详情
    * 重新提交（驳回）
    * 关闭帖子（已发布）

---

### 页面 5：帖子收到的申请 `/me/adoption/posts/:postId/applications`

内容：

* 申请列表（头像、昵称、留言、养宠经验）
* 申请状态
* 操作按钮（接受/拒绝）

交互：

* 处理后状态即时更新
* 接受后帖子关闭（前端提示）

---

### 页面 6：我的领养申请 `/me/adoption/applications`

内容：

* 我提交过的申请列表
* 帖子标题、封面、城市
* 状态标签
* 可撤回（仅 SUBMITTED）

---

## 7.2 Admin（Admin Console）

### 页面 1：送养帖审核列表 `/adoptions/posts`

筛选：

* 状态、城市、宠物类型、关键词、日期范围

列表：

* 帖子ID / 标题
* 发布者
* 城市
* 宠物类型
* 状态
* 版本号
* 更新时间
* 操作（查看）

---

### 页面 2：送养帖审核详情 `/adoptions/posts/:postId`

展示：

* 帖子内容
* 宠物信息
* 宠物图片预览
* 发布者脱敏信息
* 审核历史（当前版本信息即可）
* 驳回原因（若有）

操作：

* 通过（仅 `PENDING_REVIEW`）
* 驳回（弹窗选择原因 + 填说明）
* 下架（已发布/已关闭）

---

## 8）Codex 分任务投喂顺序（第3阶段）

---

## Task 3.1：数据库迁移 + 实体 + 枚举

**目标**

* 新增 4 张表（`pets` / `pet_media` / `adoption_posts` / `adoption_applications`）
* MySQL Flyway 脚本（utf8mb4 + json + datetime(3)）
* 新增实体、枚举、Repository

**验收**

* Flyway 执行成功
* 表结构与索引正确
* 项目可启动

---

## Task 3.2：送养帖用户端接口（发布/列表/详情/我的发布）

**目标**

* `GET /api/v1/adoptions/posts`
* `GET /api/v1/adoptions/posts/{id}`
* `POST /api/v1/adoptions/posts`
* `GET /api/v1/adoptions/my/posts`
* `POST /api/v1/adoptions/posts/{id}/resubmit`
* `POST /api/v1/adoptions/posts/{id}/close`

**要求**

* 发布接口事务处理：创建 `pets` + `pet_media` + `adoption_posts`
* 文件归属校验
* 实名前置校验（推荐开启）
* 详情访问权限正确
* `view_count` 可先简单 `+1`

**验收**

* 可发布帖子并进入待审核
* 列表和详情可正常返回
* 我的发布可查看状态
* 驳回后可重提

---

## Task 3.3：领养申请接口（提交/我的申请/处理申请）

**目标**

* `POST /api/v1/adoptions/posts/{id}/applications`
* `GET /api/v1/adoptions/my/applications`
* `GET /api/v1/adoptions/posts/{id}/applications`
* `POST /api/v1/adoptions/applications/{id}/handle`
* `POST /api/v1/adoptions/applications/{id}/withdraw`

**要求**

* 唯一约束防重复申请
* 不能申请自己的帖子
* 处理申请时校验帖子归属
* 接受申请后自动关闭帖子
* 建议自动拒绝同帖其他待处理申请

**验收**

* 完整跑通申请流程
* 状态流转正确
* 权限校验正确

---

## Task 3.4：Admin 审核接口（列表/详情/通过/驳回/下架）

**目标**

* `GET /api/admin/v1/adoptions/posts`
* `GET /api/admin/v1/adoptions/posts/{id}`
* `POST /approve`
* `POST /reject`
* `POST /offline`

**要求**

* 审核动作写 `admin_audit_logs`
* 仅 `AUDITOR` / `SUPER_ADMIN` 可审核
* 状态流转严格校验

**验收**

* 后台可审核帖子
* 前台状态联动正确
* 审计日志有记录

---

## Task 3.5：用户端前端页面（领养列表/详情/发布/我的）

**目标**

* 先做 4 页：

    * 列表页
    * 详情页
    * 发布送养帖页
    * 我的发布页
* 接入真实 API

**要求**

* 风格延续你当前“温暖治愈 + 专业可信”
* 宠物卡片组件化
* 上传与表单校验完整

**验收**

* 用户能从页面发帖并看到状态
* 列表/详情体验完整

---

## Task 3.6：用户端前端页面（申请相关）

**目标**

* 详情页申请表单
* 我的申请页
* 我收到的申请页（发布者视角）

**验收**

* 前端完整跑通申请提交与处理流程

---

## Task 3.7：Admin 前端页面（审核列表/详情）

**目标**

* 审核列表页
* 审核详情页
* 驳回弹窗 / 下架弹窗

**验收**

* 后台审核流程完整可用

---

## Task 3.8：联调回归与测试

**重点回归项**

* 未实名不能发送养帖（若启用该规则）
* 帖子审核前不可被普通用户访问
* 驳回后重提版本号 +1
* 同一用户不能重复申请同一帖子
* 接受申请后帖子自动关闭
* 审计日志完整记录审核动作

---

## 9）给 Codex 的执行提示词（可直接复制）

```text id="wxwqmy"
实现第3阶段（领养/送养模块），基于已完成的 Phase 0 + 第二阶段认证模块继续开发，并使用 MySQL（不是 PostgreSQL）。

【本阶段范围】
1) 送养帖发布（宠物信息 + 图片）
2) 帖子审核（后台）
3) 列表/详情浏览
4) 领养申请提交
5) 发布者处理申请（接受/拒绝）
6) 我的发布 / 我的申请

【严格边界】
- 本期不做“求领帖”
- 不做即时聊天（用申请留言替代）
- 不做支付
- 不做举报投诉
- 不做回访功能

【数据库要求（MySQL）】
- 新增表：
  - pets
  - pet_media
  - adoption_posts
  - adoption_applications
- JSON 字段使用 MySQL json 类型
- 时间字段使用 datetime(3)
- 所有表 utf8mb4
- 使用 Flyway 迁移

【业务规则（必须）】
- 送养帖发布后状态 = PENDING_REVIEW
- 后台审核通过 -> PUBLISHED
- 后台驳回 -> REJECTED
- 驳回后可重提，submit_version + 1
- 只有 PUBLISHED 帖子允许提交领养申请
- 申请人不能申请自己的帖子
- 同一用户对同一帖子只能申请一次
- 发布者可接受/拒绝申请
- 接受某申请后，帖子自动变 CLOSED（并建议自动拒绝其他待处理申请）

【权限与校验】
- 发布送养帖建议强制实名认证通过（调用已有认证模块校验）
- 宠物图片 fileId 必须校验：
  - 文件存在
  - status=READY
  - 归当前用户所有
- 后台审核动作必须写 admin_audit_logs
- 所有 DTO 做参数校验
- 所有接口保持统一 ApiResponse 格式

【前端要求】
- 用户端页面：
  - 领养列表页
  - 领养详情页
  - 发布送养帖页
  - 我的发布页
  - 我的申请页
  - 我收到的申请页
- Admin 页面：
  - 送养帖审核列表
  - 送养帖审核详情
- 先保证流程可用，再优化样式

【验收标准】
- 用户可发布送养帖并进入待审核
- Admin 可审核通过/驳回/下架
- 普通用户可浏览已发布帖子
- 用户可提交领养申请
- 发布者可处理申请
- 接受申请后帖子自动关闭
- 审计日志可查询
```

---

## 10）第3阶段验收清单（你自己验）

* [ ] Flyway 新增表迁移成功（MySQL）
* [ ] 可发布送养帖（带宠物图片）
* [ ] 发布后状态为待审核
* [ ] 后台可审核通过/驳回/下架
* [ ] 普通用户只能看到已发布帖子
* [ ] 领养列表/详情可正常展示
* [ ] 可提交领养申请
* [ ] 同一用户不可重复申请同一帖子
* [ ] 发布者可查看并处理申请
* [ ] 接受申请后帖子自动关闭
* [ ] 后台审核动作写入审计日志

---
