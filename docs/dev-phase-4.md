---

# 第4阶段技术任务单（给 Codex）

## 模块：上门喂养（核心闭环 MVP）

---

## 0）阶段目标与边界

### 本阶段目标

实现 **上门喂养服务的核心闭环（MVP）**：

1. 服务者（已通过认证）完善服务资料并上架
2. 宠物主人浏览服务者并发起上门喂养订单（预约请求）
3. 服务者接单/拒单
4. 服务过程中按次留痕（照片 + 文本记录）
5. 服务结束后主人确认完成并评价

---

### 本阶段边界（严格控制）

### ✅ 本期做

* 服务者资料页（公开展示）
* 服务者列表/详情（前台）
* 主人发起喂养订单（预约请求）
* 服务者接单/拒单
* 服务分次（visit）执行与留痕
* 主人查看服务留痕
* 主人确认完成 + 评价
* 我的订单（主人视角）
* 我的接单（服务者视角）

### ❌ 本期不做

* 在线支付（先做“价格展示/报价”，不收款）
* 即时聊天（先用订单留言/备注）
* 实时定位轨迹
* 抢单大厅（先做“指定服务者下单”）
* 优惠券/营销
* 投诉仲裁（第6阶段风控再做）

---

## 1）目录结构增量（在现有项目基础上新增）

---

### 后端（Spring Boot）

```text
services/api-server/src/main/java/com/petplatform/modules/
├─ feeding/
│  ├─ controller/
│  │  ├─ FeedingProviderController.java          // 前台：服务者列表/详情、服务者资料维护
│  │  ├─ FeedingOrderController.java             // 前台：下单、我的订单、订单详情、确认完成、评价
│  │  └─ FeedingProviderOrderController.java     // 服务者端：接单、我的接单、visit 留痕
│  ├─ dto/
│  │  ├─ user/
│  │  │  ├─ FeedingProviderListQuery.java
│  │  │  ├─ FeedingProviderListItemDTO.java
│  │  │  ├─ FeedingProviderDetailDTO.java
│  │  │  ├─ CreateFeedingOrderRequest.java
│  │  │  ├─ FeedingOrderDetailDTO.java
│  │  │  ├─ MyFeedingOrderListItemDTO.java
│  │  │  ├─ ConfirmFeedingOrderCompleteRequest.java
│  │  │  ├─ SubmitFeedingReviewRequest.java
│  │  │  └─ FeedingReviewDTO.java
│  │  └─ provider/
│  │     ├─ UpsertFeedingProviderProfileRequest.java
│  │     ├─ ProviderOrderListQuery.java
│  │     ├─ ProviderOrderListItemDTO.java
│  │     ├─ ProviderRespondOrderRequest.java
│  │     ├─ StartVisitRequest.java                // 可选（可不带字段）
│  │     ├─ SubmitVisitLogRequest.java
│  │     └─ FeedingVisitLogDTO.java
│  ├─ service/
│  │  ├─ FeedingProviderService.java
│  │  ├─ FeedingOrderService.java
│  │  ├─ FeedingProviderOrderService.java
│  │  └─ impl/
│  ├─ entity/
│  │  ├─ FeedingProviderProfileEntity.java
│  │  ├─ FeedingOrderEntity.java
│  │  ├─ FeedingOrderPetEntity.java
│  │  ├─ FeedingOrderVisitEntity.java
│  │  ├─ FeedingVisitMediaEntity.java
│  │  └─ FeedingOrderReviewEntity.java
│  ├─ repository/
│  │  ├─ FeedingProviderProfileRepository.java
│  │  ├─ FeedingOrderRepository.java
│  │  ├─ FeedingOrderPetRepository.java
│  │  ├─ FeedingOrderVisitRepository.java
│  │  ├─ FeedingVisitMediaRepository.java
│  │  └─ FeedingOrderReviewRepository.java
│  └─ enums/
│     ├─ FeedingProviderProfileStatus.java
│     ├─ FeedingOrderStatus.java
│     ├─ FeedingVisitStatus.java
│     ├─ FeedingServiceItemTag.java
│     └─ FeedingPetType.java                      // 可复用 Phase3 的 PetType，也可不单独建
```

---

### 前端（用户端 Web）

```text
apps/web-portal/src/pages/
├─ Feeding/
│  ├─ FeedingProviderListPage.tsx                // 服务者列表
│  ├─ FeedingProviderDetailPage.tsx              // 服务者详情
│  ├─ FeedingOrderCreatePage.tsx                 // 发起预约请求（下单）
│  ├─ MyFeedingOrdersPage.tsx                    // 我的订单（主人）
│  ├─ MyFeedingOrderDetailPage.tsx               // 订单详情（主人）
│  ├─ FeedingProviderCenterPage.tsx              // 服务者中心（资料维护）
│  ├─ FeedingProviderOrdersPage.tsx              // 我的接单（服务者）
│  ├─ FeedingProviderOrderDetailPage.tsx         // 订单详情（服务者 + visit 留痕）
│  └─ components/
│     ├─ FeedingProviderCard.tsx
│     ├─ FeedingOrderStatusTag.tsx
│     ├─ FeedingVisitStatusTag.tsx
│     ├─ FeedingVisitEditor.tsx
│     └─ FeedingReviewModal.tsx
```

> 第4阶段先不做 Admin 页面（运营后台增强和风控在第6阶段统一补）。

---

## 2）数据库设计（MySQL 版本）

> 使用 MySQL 8.x，`utf8mb4`，时间字段 `datetime(3)`，JSON 字段用 `json`。

---

## 2.1 新增表：`feeding_provider_profiles`

用途：服务者上门喂养资料（公开展示）

### 字段

* `id` bigint auto_increment pk
* `provider_user_id` bigint not null unique
* `status` varchar(32) not null

    * `DRAFT`
    * `ACTIVE`
    * `PAUSED`
* `display_name` varchar(64) null
* `headline` varchar(128) null              // 例如：春节上门喂养/猫咪友好
* `intro` text null
* `service_city_code` varchar(32) not null
* `service_city_name` varchar(64) not null
* `service_districts` json null             // 可服务区域列表
* `service_pet_types` json not null         // ["CAT","DOG"]
* `service_item_tags` json not null         // ["FEED","WATER","LITTER","PHOTO_REPORT"]
* `base_price_per_visit` decimal(10,2) null // 展示用参考价
* `experience_years` int null
* `max_orders_per_day` int null
* `accept_notes` varchar(255) null          // 接单说明
* `rating_avg` decimal(3,2) not null default 0.00
* `rating_count` int not null default 0
* `completed_order_count` int not null default 0
* `created_at` datetime(3) not null
* `updated_at` datetime(3) not null

### 索引

* `idx_feeding_provider_profiles_city_status (service_city_code, status)`
* `idx_feeding_provider_profiles_updated_at (updated_at)`

### 约束规则

* 只有 **已通过服务者认证（Phase2 PROVIDER）** 的用户，才能创建/更新并设为 `ACTIVE`

---

## 2.2 新增表：`feeding_orders`

用途：上门喂养订单主表（主人发起的预约请求）

### 字段

* `id` bigint auto_increment pk

* `order_no` varchar(32) not null unique        // 业务单号（建议生成）

* `owner_user_id` bigint not null               // 宠物主人

* `provider_user_id` bigint not null            // 指定服务者

* `provider_profile_id` bigint not null

* `status` varchar(32) not null

    * `PENDING_PROVIDER_ACCEPT`
    * `REJECTED_BY_PROVIDER`
    * `CONFIRMED`
    * `IN_SERVICE`
    * `WAITING_OWNER_CONFIRM`
    * `COMPLETED`
    * `CANCELLED_BY_OWNER`
    * `CANCELLED_BY_PROVIDER`

* `service_city_code` varchar(32) not null

* `service_city_name` varchar(64) not null

* `service_district_name` varchar(64) null

* `service_address_detail` varchar(255) not null      // 敏感信息，仅订单相关方可见

* `service_address_note` varchar(255) null            // 门禁/楼栋/注意事项

* `contact_name` varchar(64) not null

* `contact_mobile` varchar(32) not null               // 后续可加密；MVP 先明文存储

* `contact_mobile_masked` varchar(32) not null

* `service_item_tags` json not null                   // 本单服务项（如 FEED/WATER/LITTER）

* `visit_count` int not null

* `owner_note` text null

* `requested_total_amount` decimal(10,2) null         // 主人预期预算（可空）

* `quoted_total_amount` decimal(10,2) null            // 服务者确认报价（可空）

* `currency` varchar(8) not null default 'CNY'

* `provider_response_note` varchar(255) null

* `cancel_reason` varchar(255) null

* `owner_confirmed_at` datetime(3) null

* `created_at` datetime(3) not null

* `updated_at` datetime(3) not null

### 索引

* `idx_feeding_orders_owner_status (owner_user_id, status)`
* `idx_feeding_orders_provider_status (provider_user_id, status)`
* `idx_feeding_orders_status_created_at (status, created_at)`
* `idx_feeding_orders_city_status (service_city_code, status)`

---

## 2.3 新增表：`feeding_order_pets`

用途：订单关联宠物（支持一单多宠），并保存下单时快照

### 字段

* `id` bigint auto_increment pk
* `order_id` bigint not null
* `pet_id` bigint not null                       // 关联 Phase3 的 pets 表（复用）
* `pet_name_snapshot` varchar(64) null
* `pet_type_snapshot` varchar(16) not null
* `pet_gender_snapshot` varchar(16) null
* `age_months_snapshot` int null
* `breed_snapshot` varchar(128) null
* `special_care_note_snapshot` text null
* `created_at` datetime(3) not null

### 索引/约束

* `idx_feeding_order_pets_order_id (order_id)`
* `idx_feeding_order_pets_pet_id (pet_id)`
* 唯一约束：`uk_feeding_order_pets_order_pet (order_id, pet_id)`

---

## 2.4 新增表：`feeding_order_visits`

用途：每次上门服务的子任务（留痕单位）

### 字段

* `id` bigint auto_increment pk

* `order_id` bigint not null

* `visit_index` int not null                     // 第几次上门（1..N）

* `planned_start_at` datetime(3) not null

* `planned_end_at` datetime(3) not null

* `status` varchar(32) not null

    * `PENDING`
    * `STARTED`
    * `DONE`
    * `CANCELLED`

* `actual_start_at` datetime(3) null

* `actual_end_at` datetime(3) null

* `food_done` tinyint(1) not null default 0

* `water_done` tinyint(1) not null default 0

* `litter_done` tinyint(1) not null default 0

* `play_done` tinyint(1) not null default 0

* `health_observation` varchar(255) null

* `visit_note` text null

* `created_at` datetime(3) not null

* `updated_at` datetime(3) not null

### 索引/约束

* `idx_feeding_order_visits_order_status (order_id, status)`
* `idx_feeding_order_visits_planned_start (planned_start_at)`
* 唯一约束：`uk_feeding_order_visits_order_index (order_id, visit_index)`

---

## 2.5 新增表：`feeding_visit_media`

用途：每次服务留痕图片

### 字段

* `id` bigint auto_increment pk
* `visit_id` bigint not null
* `file_object_id` bigint not null
* `sort_order` int not null default 0
* `created_at` datetime(3) not null

### 索引

* `idx_feeding_visit_media_visit_sort (visit_id, sort_order)`

---

## 2.6 新增表：`feeding_order_reviews`

用途：主人完成服务后的评价（每单一评）

### 字段

* `id` bigint auto_increment pk
* `order_id` bigint not null unique
* `owner_user_id` bigint not null
* `provider_user_id` bigint not null
* `rating_overall` int not null                  // 1~5
* `rating_timeliness` int null                   // 可选
* `rating_cleanliness` int null                  // 可选
* `rating_attitude` int null                     // 可选
* `content` varchar(1000) null
* `created_at` datetime(3) not null
* `updated_at` datetime(3) not null

### 索引

* `idx_feeding_order_reviews_provider (provider_user_id, created_at)`

---

## 2.7 Migration 文件建议（MySQL）

```text
V11__create_feeding_provider_profiles.sql
V12__create_feeding_orders.sql
V13__create_feeding_order_pets.sql
V14__create_feeding_order_visits.sql
V15__create_feeding_visit_media.sql
V16__create_feeding_order_reviews.sql
```

---

## 3）接口清单（Phase 4）

---

## 3.1 前台：服务者展示与资料维护（`/api/v1/feeding/providers`）

---

### 1）获取服务者列表（公开）

**GET** `/api/v1/feeding/providers`

#### Query：`FeedingProviderListQuery`

* `page` (required)
* `pageSize` (required)
* `cityCode` (optional)
* `petType` (optional: `CAT` / `DOG`)
* `keyword` (optional)
* `sortBy` (optional: `DEFAULT` / `RATING` / `LATEST`)

#### 返回：`PageResponse<FeedingProviderListItemDTO>`

* `providerUserId`
* `providerProfileId`
* `displayName`
* `headline`
* `avatarUrl`
* `serviceCityCode`
* `serviceCityName`
* `servicePetTypes`
* `serviceItemTags`
* `basePricePerVisit`
* `ratingAvg`
* `ratingCount`
* `completedOrderCount`

> 仅返回 `status=ACTIVE` 的服务者资料。

---

### 2）获取服务者详情（公开）

**GET** `/api/v1/feeding/providers/{providerUserId}`

#### 返回：`FeedingProviderDetailDTO`

* `providerUserId`
* `providerProfileId`
* `displayName`
* `headline`
* `intro`
* `avatarUrl`
* `serviceCityCode`
* `serviceCityName`
* `serviceDistricts`
* `servicePetTypes`
* `serviceItemTags`
* `basePricePerVisit`
* `experienceYears`
* `acceptNotes`
* `ratingAvg`
* `ratingCount`
* `completedOrderCount`
* `viewerContext`

    * `canCreateOrder` (bool)
    * `cannotCreateOrderReason` (nullable)

---

### 3）获取我的服务者资料（服务者端）

**GET** `/api/v1/feeding/providers/me/profile`

#### 规则

* 必须是 `PROVIDER`（Phase2 认证通过）

---

### 4）创建/更新我的服务者资料（服务者端）

**PUT** `/api/v1/feeding/providers/me/profile`

#### DTO：`UpsertFeedingProviderProfileRequest`

* `status` (string, optional: `DRAFT` / `ACTIVE` / `PAUSED`)
* `displayName` (string, optional, max 64)
* `headline` (string, optional, max 128)
* `intro` (string, optional, max 2000)
* `serviceCityCode` (string, required)
* `serviceCityName` (string, required)
* `serviceDistricts` (string[], optional)
* `servicePetTypes` (string[], required, min 1)
* `serviceItemTags` (string[], required, min 1)
* `basePricePerVisit` (decimal, optional)
* `experienceYears` (int, optional, 0~50)
* `maxOrdersPerDay` (int, optional, 1~50)
* `acceptNotes` (string, optional, max 255)

#### 规则

* 仅已通过服务者认证的用户可操作
* 若设置为 `ACTIVE`，需做最小字段完整性校验（城市、服务宠物类型、服务项、简介）

---

## 3.2 前台：主人下单与我的订单（`/api/v1/feeding/orders`）

---

### 1）发起喂养订单（预约请求）

**POST** `/api/v1/feeding/orders`

#### 前置规则

* 必须登录
* 推荐强制：`实名认证 = APPROVED`
* 只能对 `ACTIVE` 服务者下单
* `petIds` 必须归当前用户所有（复用 Phase3 `pets`）

#### DTO：`CreateFeedingOrderRequest`

* `providerUserId` (long, required)

* `serviceCityCode` (string, required)

* `serviceCityName` (string, required)

* `serviceDistrictName` (string, optional)

* `serviceAddressDetail` (string, required, max 255)

* `serviceAddressNote` (string, optional, max 255)

* `contactName` (string, required, max 64)

* `contactMobile` (string, required, max 32)

* `petIds` (long[], required, min 1, max 5)

* `serviceItemTags` (string[], required, min 1)

* `ownerNote` (string, optional, max 2000)

* `requestedTotalAmount` (decimal, optional)

* `visits` (array, required, min 1, max 20)

    * `plannedStartAt` (datetime string, required)
    * `plannedEndAt` (datetime string, required)

#### 返回：`FeedingOrderDetailDTO`

* 状态应为 `PENDING_PROVIDER_ACCEPT`

---

### 2）我的订单列表（主人视角）

**GET** `/api/v1/feeding/orders/my`

#### Query

* `page`
* `pageSize`
* `status` (optional)

#### 返回：`PageResponse<MyFeedingOrderListItemDTO>`

* `orderId`
* `orderNo`
* `status`
* `providerUserId`
* `providerDisplayName`
* `providerAvatarUrl`
* `serviceCityName`
* `visitCount`
* `quotedTotalAmount`
* `nextVisitPlannedAt` (nullable)
* `createdAt`
* `updatedAt`

---

### 3）订单详情（主人视角）

**GET** `/api/v1/feeding/orders/{orderId}`

#### 规则

* 仅订单主人或服务者本人可查看（同一接口返回 viewerContext）

#### 返回：`FeedingOrderDetailDTO`

包含：

* 订单基础信息（状态、价格、地址、联系人）
* 服务者信息（昵称、头像、评分）
* 宠物快照列表
* Visit 列表（计划时间、实际时间、状态、留痕内容、图片）
* 评价信息（若已评价）
* `viewerContext`

    * `isOwner`
    * `isProvider`
    * `canCancel`
    * `canConfirmComplete`
    * `canReview`

---

### 4）取消订单（主人）

**POST** `/api/v1/feeding/orders/{orderId}/cancel`

#### 规则

* 仅主人可操作
* 允许状态：

    * `PENDING_PROVIDER_ACCEPT`
    * `CONFIRMED`（且尚未开始任何 visit）
* 状态 -> `CANCELLED_BY_OWNER`

---

### 5）确认订单完成（主人）

**POST** `/api/v1/feeding/orders/{orderId}/confirm-complete`

#### DTO：`ConfirmFeedingOrderCompleteRequest`

* `remark` (string, optional, max 255)

#### 规则

* 仅主人可操作
* 仅 `WAITING_OWNER_CONFIRM` 可操作
* 状态 -> `COMPLETED`
* 同时更新服务者统计（`completed_order_count`）

---

### 6）提交评价（主人）

**POST** `/api/v1/feeding/orders/{orderId}/review`

#### DTO：`SubmitFeedingReviewRequest`

* `ratingOverall` (int, required, 1~5)
* `ratingTimeliness` (int, optional, 1~5)
* `ratingCleanliness` (int, optional, 1~5)
* `ratingAttitude` (int, optional, 1~5)
* `content` (string, optional, max 1000)

#### 规则

* 仅主人可操作
* 仅 `COMPLETED` 可评价
* 每单仅一评
* 写入后更新服务者评分汇总（`rating_avg`, `rating_count`）

---

## 3.3 服务者端：接单与服务留痕（`/api/v1/feeding/provider-orders`）

---

### 1）我的接单列表（服务者）

**GET** `/api/v1/feeding/provider-orders/my`

#### Query：`ProviderOrderListQuery`

* `page`
* `pageSize`
* `status` (optional)

#### 返回：`PageResponse<ProviderOrderListItemDTO>`

* `orderId`
* `orderNo`
* `status`
* `ownerUserId`
* `ownerNickname`
* `serviceCityName`
* `visitCount`
* `quotedTotalAmount`
* `nextVisitPlannedAt`
* `createdAt`
* `updatedAt`

---

### 2）服务者响应订单（接单/拒单）

**POST** `/api/v1/feeding/provider-orders/{orderId}/respond`

#### DTO：`ProviderRespondOrderRequest`

* `action` (string, required: `ACCEPT` / `REJECT`)
* `quotedTotalAmount` (decimal, optional; `ACCEPT` 时建议填写)
* `providerResponseNote` (string, optional, max 255)

#### 规则

* 仅订单服务者本人可操作
* 仅 `PENDING_PROVIDER_ACCEPT` 可操作
* `ACCEPT` -> `CONFIRMED`
* `REJECT` -> `REJECTED_BY_PROVIDER`

---

### 3）开始某次服务（visit）

**POST** `/api/v1/feeding/provider-orders/visits/{visitId}/start`

#### 规则

* 仅订单服务者本人可操作
* `visit.status = PENDING` 才可开始
* `visit.status -> STARTED`
* 填 `actual_start_at`
* 若订单状态为 `CONFIRMED`，订单状态 -> `IN_SERVICE`

---

### 4）提交某次服务留痕（visit log）

**POST** `/api/v1/feeding/provider-orders/visits/{visitId}/submit-log`

#### DTO：`SubmitVisitLogRequest`

* `foodDone` (boolean, optional)
* `waterDone` (boolean, optional)
* `litterDone` (boolean, optional)
* `playDone` (boolean, optional)
* `healthObservation` (string, optional, max 255)
* `visitNote` (string, optional, max 2000)
* `photoFileIds` (long[], optional, max 9)

#### 规则

* 仅订单服务者本人可操作
* `visit.status` 必须是 `STARTED`（也可兼容 `PENDING` 自动开始，建议别放太宽）
* 校验 `photoFileIds` 归属与文件状态（`READY`）
* 提交后：

    * `visit.status -> DONE`
    * 填 `actual_end_at`
    * 写 `feeding_visit_media`
* 若订单所有 visit 都 `DONE`：

    * 订单状态 -> `WAITING_OWNER_CONFIRM`

---

### 5）服务者取消订单（可选，建议做）

**POST** `/api/v1/feeding/provider-orders/{orderId}/cancel`

#### DTO

* `reason` (string, required, max 255)

#### 规则

* 仅服务者本人可操作
* 允许状态：

    * `PENDING_PROVIDER_ACCEPT`
    * `CONFIRMED`（且尚未开始任何 visit）
* 状态 -> `CANCELLED_BY_PROVIDER`

---

## 4）DTO 字段清单（给 Codex 建类）

---

## 4.1 服务者展示/资料 DTO

### `FeedingProviderListQuery`

* `page: Integer`
* `pageSize: Integer`
* `cityCode: String?`
* `petType: String?`
* `keyword: String?`
* `sortBy: String?`

### `FeedingProviderListItemDTO`

* `providerUserId: Long`
* `providerProfileId: Long`
* `displayName: String`
* `headline: String?`
* `avatarUrl: String?`
* `serviceCityCode: String`
* `serviceCityName: String`
* `servicePetTypes: List<String>`
* `serviceItemTags: List<String>`
* `basePricePerVisit: BigDecimal?`
* `ratingAvg: BigDecimal`
* `ratingCount: Integer`
* `completedOrderCount: Integer`

### `FeedingProviderDetailDTO`

* `providerUserId: Long`
* `providerProfileId: Long`
* `displayName: String`
* `headline: String?`
* `intro: String?`
* `avatarUrl: String?`
* `serviceCityCode: String`
* `serviceCityName: String`
* `serviceDistricts: List<String>?`
* `servicePetTypes: List<String>`
* `serviceItemTags: List<String>`
* `basePricePerVisit: BigDecimal?`
* `experienceYears: Integer?`
* `acceptNotes: String?`
* `ratingAvg: BigDecimal`
* `ratingCount: Integer`
* `completedOrderCount: Integer`
* `viewerContext: FeedingProviderViewerContextDTO?`

### `FeedingProviderViewerContextDTO`

* `canCreateOrder: Boolean`
* `cannotCreateOrderReason: String?`

### `UpsertFeedingProviderProfileRequest`

* `status: String?`
* `displayName: String?`
* `headline: String?`
* `intro: String?`
* `serviceCityCode: String`
* `serviceCityName: String`
* `serviceDistricts: List<String>?`
* `servicePetTypes: List<String>`
* `serviceItemTags: List<String>`
* `basePricePerVisit: BigDecimal?`
* `experienceYears: Integer?`
* `maxOrdersPerDay: Integer?`
* `acceptNotes: String?`

---

## 4.2 订单/visit DTO

### `CreateFeedingOrderRequest`

* `providerUserId: Long`
* `serviceCityCode: String`
* `serviceCityName: String`
* `serviceDistrictName: String?`
* `serviceAddressDetail: String`
* `serviceAddressNote: String?`
* `contactName: String`
* `contactMobile: String`
* `petIds: List<Long>`
* `serviceItemTags: List<String>`
* `ownerNote: String?`
* `requestedTotalAmount: BigDecimal?`
* `visits: List<CreateFeedingOrderVisitDTO>`

### `CreateFeedingOrderVisitDTO`

* `plannedStartAt: String`
* `plannedEndAt: String`

### `MyFeedingOrderListItemDTO`

* `orderId: Long`
* `orderNo: String`
* `status: String`
* `providerUserId: Long`
* `providerDisplayName: String`
* `providerAvatarUrl: String?`
* `serviceCityName: String`
* `visitCount: Integer`
* `quotedTotalAmount: BigDecimal?`
* `nextVisitPlannedAt: String?`
* `createdAt: String`
* `updatedAt: String`

### `FeedingOrderDetailDTO`

* `orderId: Long`

* `orderNo: String`

* `status: String`

* `ownerUserId: Long`

* `ownerNickname: String`

* `providerUserId: Long`

* `providerDisplayName: String`

* `providerAvatarUrl: String?`

* `providerRatingAvg: BigDecimal?`

* `serviceCityCode: String`

* `serviceCityName: String`

* `serviceDistrictName: String?`

* `serviceAddressDetail: String`        // 仅相关方可见

* `serviceAddressNote: String?`

* `contactName: String`

* `contactMobileMasked: String`

* `serviceItemTags: List<String>`

* `ownerNote: String?`

* `requestedTotalAmount: BigDecimal?`

* `quotedTotalAmount: BigDecimal?`

* `currency: String`

* `pets: List<FeedingOrderPetSnapshotDTO>`

* `visits: List<FeedingOrderVisitDTO>`

* `review: FeedingReviewDTO?`

* `createdAt: String`

* `updatedAt: String`

* `viewerContext: FeedingOrderViewerContextDTO`

### `FeedingOrderPetSnapshotDTO`

* `petId: Long`
* `petName: String?`
* `petType: String`
* `petGender: String?`
* `ageMonths: Integer?`
* `breed: String?`
* `specialCareNote: String?`

### `FeedingOrderVisitDTO`

* `visitId: Long`
* `visitIndex: Integer`
* `plannedStartAt: String`
* `plannedEndAt: String`
* `status: String`
* `actualStartAt: String?`
* `actualEndAt: String?`
* `foodDone: Boolean`
* `waterDone: Boolean`
* `litterDone: Boolean`
* `playDone: Boolean`
* `healthObservation: String?`
* `visitNote: String?`
* `photos: List<VisitPhotoDTO>`

### `VisitPhotoDTO`

* `fileId: Long`
* `url: String`
* `sortOrder: Integer`

### `FeedingOrderViewerContextDTO`

* `isOwner: Boolean`
* `isProvider: Boolean`
* `canCancel: Boolean`
* `canConfirmComplete: Boolean`
* `canReview: Boolean`

### `ConfirmFeedingOrderCompleteRequest`

* `remark: String?`

### `SubmitFeedingReviewRequest`

* `ratingOverall: Integer`
* `ratingTimeliness: Integer?`
* `ratingCleanliness: Integer?`
* `ratingAttitude: Integer?`
* `content: String?`

### `FeedingReviewDTO`

* `reviewId: Long`
* `orderId: Long`
* `ratingOverall: Integer`
* `ratingTimeliness: Integer?`
* `ratingCleanliness: Integer?`
* `ratingAttitude: Integer?`
* `content: String?`
* `createdAt: String`

---

## 4.3 服务者接单/留痕 DTO

### `ProviderOrderListQuery`

* `page: Integer`
* `pageSize: Integer`
* `status: String?`

### `ProviderOrderListItemDTO`

* `orderId: Long`
* `orderNo: String`
* `status: String`
* `ownerUserId: Long`
* `ownerNickname: String`
* `serviceCityName: String`
* `visitCount: Integer`
* `quotedTotalAmount: BigDecimal?`
* `nextVisitPlannedAt: String?`
* `createdAt: String`
* `updatedAt: String`

### `ProviderRespondOrderRequest`

* `action: String` (`ACCEPT` / `REJECT`)
* `quotedTotalAmount: BigDecimal?`
* `providerResponseNote: String?`

### `SubmitVisitLogRequest`

* `foodDone: Boolean?`
* `waterDone: Boolean?`
* `litterDone: Boolean?`
* `playDone: Boolean?`
* `healthObservation: String?`
* `visitNote: String?`
* `photoFileIds: List<Long>?`

---

## 5）状态机规则（必须后端校验）

---

## 5.1 服务者资料状态机（`feeding_provider_profiles.status`）

### 状态

* `DRAFT`
* `ACTIVE`
* `PAUSED`

### 合法流转

* `null -> DRAFT`
* `DRAFT -> ACTIVE`
* `ACTIVE -> PAUSED`
* `PAUSED -> ACTIVE`
* `ACTIVE -> DRAFT`（可不开放）
* `PAUSED -> DRAFT`（可不开放）

### 规则

* `ACTIVE` 才会出现在公开列表
* 设为 `ACTIVE` 时必须校验服务者认证已通过、必要字段完整

---

## 5.2 订单状态机（`feeding_orders.status`）

### 状态

* `PENDING_PROVIDER_ACCEPT`
* `REJECTED_BY_PROVIDER`
* `CONFIRMED`
* `IN_SERVICE`
* `WAITING_OWNER_CONFIRM`
* `COMPLETED`
* `CANCELLED_BY_OWNER`
* `CANCELLED_BY_PROVIDER`

### 合法流转

* 下单：`null -> PENDING_PROVIDER_ACCEPT`
* 服务者拒单：`PENDING_PROVIDER_ACCEPT -> REJECTED_BY_PROVIDER`
* 服务者接单：`PENDING_PROVIDER_ACCEPT -> CONFIRMED`
* 首次开始 visit：`CONFIRMED -> IN_SERVICE`
* 所有 visit 完成：`IN_SERVICE -> WAITING_OWNER_CONFIRM`
* 所有 visit 完成（未显式进入 IN_SERVICE 也可兜底）：`CONFIRMED -> WAITING_OWNER_CONFIRM`
* 主人确认完成：`WAITING_OWNER_CONFIRM -> COMPLETED`
* 主人取消：`PENDING_PROVIDER_ACCEPT/CONFIRMED -> CANCELLED_BY_OWNER`（且未开始任何 visit）
* 服务者取消：`PENDING_PROVIDER_ACCEPT/CONFIRMED -> CANCELLED_BY_PROVIDER`（且未开始任何 visit）

### 非法流转（拦截）

* 已完成/已取消/已拒单状态再次变更
* `IN_SERVICE` 后再取消（本期不支持）

---

## 5.3 Visit 状态机（`feeding_order_visits.status`）

### 状态

* `PENDING`
* `STARTED`
* `DONE`
* `CANCELLED`

### 合法流转

* 创建订单时：`null -> PENDING`
* 服务者开始：`PENDING -> STARTED`
* 提交留痕：`STARTED -> DONE`
* 订单被取消前：`PENDING -> CANCELLED`

### 规则

* 只有服务者本人可变更
* 提交留痕时必须校验文件归属与 `READY`
* 一个 visit 完成后不能重复提交（如需修改，后续再做“编辑留痕”）

---

## 5.4 评价规则（`feeding_order_reviews`）

* 仅订单主人可评价
* 仅 `COMPLETED` 状态可评价
* 每单只能评价一次（`order_id` 唯一）
* 评价成功后更新服务者聚合评分（`rating_avg`, `rating_count`）

---

## 6）依赖与权限规则（跨模块）

---

## 6.1 与第2阶段认证模块的依赖

### 主人下单前置（建议）

* `实名认证 = APPROVED`
* 否则返回：`FEEDING_REAL_NAME_REQUIRED`

### 服务者资料维护/接单前置

* `服务者认证 = APPROVED`
* 否则返回：`FEEDING_PROVIDER_VERIFICATION_REQUIRED`

---

## 6.2 与第3阶段宠物模块的依赖（复用 `pets`）

### 下单时 `petIds` 校验

* 宠物存在
* `pets.owner_user_id = 当前用户`
* 不要求宠物必须有图片（但前端建议有）

> 这里直接复用第3阶段 `pets` 表，避免重复建“我的宠物”表。

---

## 6.3 文件归属校验（复用 Phase0 `file_objects`）

### Visit 留痕图片 `photoFileIds`

必须满足：

* 文件存在
* `status = READY`
* 文件归当前服务者所有（推荐）
* `bizType` 合法（建议 `FEEDING_LOG` 或 `OTHER` 兼容）

---

## 7）错误码增量（Phase 4）

在现有错误码基础上新增：

* `FEEDING_REAL_NAME_REQUIRED`
* `FEEDING_PROVIDER_VERIFICATION_REQUIRED`
* `FEEDING_PROVIDER_PROFILE_NOT_FOUND`
* `FEEDING_PROVIDER_PROFILE_STATUS_INVALID`
* `FEEDING_ORDER_NOT_FOUND`
* `FEEDING_ORDER_STATUS_INVALID`
* `FEEDING_ORDER_NOT_OWNER`
* `FEEDING_ORDER_NOT_PROVIDER`
* `FEEDING_ORDER_CANNOT_CANCEL`
* `FEEDING_ORDER_CANNOT_CONFIRM`
* `FEEDING_ORDER_PET_INVALID`
* `FEEDING_ORDER_PET_NOT_OWNED`
* `FEEDING_VISIT_NOT_FOUND`
* `FEEDING_VISIT_STATUS_INVALID`
* `FEEDING_VISIT_NOT_PROVIDER`
* `FEEDING_VISIT_FILE_INVALID`
* `FEEDING_VISIT_FILE_NOT_OWNED`
* `FEEDING_REVIEW_ALREADY_EXISTS`
* `FEEDING_REVIEW_NOT_ALLOWED`

---

## 8）前端页面任务（用户端 Web）

---

## 8.1 主人侧页面

### 页面 1：服务者列表 `/feeding/providers`

内容：

* 筛选（城市、猫/狗、关键词）
* 服务者卡片（头像、简介、服务项、价格、评分）
* 分页

交互：

* 点击进入服务者详情
* 未登录也可浏览

---

### 页面 2：服务者详情 `/feeding/providers/:providerUserId`

内容：

* 服务者资料（简介、评分、服务区域、服务内容）
* 下单按钮（预约请求）

交互：

* 未登录点击下单 -> 跳登录
* 未实名提示去认证（如果启用实名下单规则）

---

### 页面 3：发起喂养订单 `/feeding/orders/new`

内容：

* 选择服务者（从详情页跳转可带默认）
* 联系信息
* 服务地址
* 选择宠物（复用第3阶段已有宠物；如果你还没做“我的宠物管理”，这里可临时用选择已有宠物）
* 服务项选择
* 上门时间（多次 visit）
* 留言/预算

交互：

* 提交成功后跳“我的订单详情”，状态为待接单

---

### 页面 4：我的订单 `/me/feeding/orders`

内容：

* 列表展示订单状态、服务者、价格、下次上门时间
* 筛选状态

交互：

* 点进订单详情
* 待接单/已确认（未开始）可取消

---

### 页面 5：订单详情（主人）`/me/feeding/orders/:orderId`

内容：

* 订单基础信息
* 服务者信息
* 宠物信息
* Visit 时间轴（每次服务状态）
* 服务留痕（图片 + 备注）
* 操作区：

    * 取消订单（符合条件）
    * 确认完成（所有 visit 完成后）
    * 提交评价（完成后）

---

## 8.2 服务者侧页面

### 页面 6：服务者中心（资料维护）`/provider/feeding/profile`

内容：

* 编辑服务者资料
* 上架/暂停状态切换
* 城市、服务项、价格、简介等字段

交互：

* 非服务者认证用户显示引导（去完成认证）

---

### 页面 7：我的接单 `/provider/feeding/orders`

内容：

* 接单列表（待接单、已确认、服务中、待确认完成、已完成）
* 状态筛选

交互：

* 点进订单详情
* 待接单可接单/拒单

---

### 页面 8：订单详情（服务者）`/provider/feeding/orders/:orderId`

内容：

* 订单信息（地址、联系人、宠物）
* Visit 列表
* 每个 visit 的操作：

    * 开始服务
    * 提交留痕（勾选项 + 文字 + 上传图片）

交互：

* 提交某次留痕后该 visit 状态变 DONE
* 全部 DONE 后订单进入“待主人确认”

---

## 9）Codex 分任务投喂顺序（第4阶段）

---

## Task 4.1：数据库迁移 + 实体 + 枚举（MySQL）

**目标**

* 新增 6 张表：

    * `feeding_provider_profiles`
    * `feeding_orders`
    * `feeding_order_pets`
    * `feeding_order_visits`
    * `feeding_visit_media`
    * `feeding_order_reviews`
* MySQL Flyway 脚本
* 实体、枚举、Repository

**验收**

* Flyway 成功
* 表结构/索引正确
* 项目启动正常

---

## Task 4.2：服务者资料接口（列表/详情/我的资料）

**目标**

* `GET /api/v1/feeding/providers`
* `GET /api/v1/feeding/providers/{providerUserId}`
* `GET /api/v1/feeding/providers/me/profile`
* `PUT /api/v1/feeding/providers/me/profile`

**要求**

* `ACTIVE` 才公开
* 校验服务者认证前置条件
* DTO 参数校验

**验收**

* 服务者可完善资料并上架
* 普通用户可浏览服务者列表与详情

---

## Task 4.3：主人下单接口（订单创建/我的订单/详情/取消）

**目标**

* `POST /api/v1/feeding/orders`
* `GET /api/v1/feeding/orders/my`
* `GET /api/v1/feeding/orders/{orderId}`
* `POST /api/v1/feeding/orders/{orderId}/cancel`

**要求**

* 事务创建：

    * `feeding_orders`
    * `feeding_order_pets`
    * `feeding_order_visits`
* 校验：

    * 主人实名（若启用）
    * 服务者资料 ACTIVE
    * `petIds` 归属
* 地址/电话脱敏返回处理
* 权限控制（仅相关方可看详情）

**验收**

* 能创建订单并进入待接单
* 我的订单和详情可看
* 可取消未开始订单

---

## Task 4.4：服务者接单与 visit 留痕接口

**目标**

* `GET /api/v1/feeding/provider-orders/my`
* `POST /api/v1/feeding/provider-orders/{orderId}/respond`
* `POST /api/v1/feeding/provider-orders/visits/{visitId}/start`
* `POST /api/v1/feeding/provider-orders/visits/{visitId}/submit-log`
* （可选）`POST /api/v1/feeding/provider-orders/{orderId}/cancel`

**要求**

* 严格校验订单/visit 归属
* 留痕图片文件归属校验
* 状态流转严格
* 全部 visit 完成后订单自动转 `WAITING_OWNER_CONFIRM`

**验收**

* 服务者可接单/拒单
* 可逐次提交留痕
* 订单状态自动推进

---

## Task 4.5：主人确认完成 + 评价接口

**目标**

* `POST /api/v1/feeding/orders/{orderId}/confirm-complete`
* `POST /api/v1/feeding/orders/{orderId}/review`

**要求**

* 只有 `WAITING_OWNER_CONFIRM` 才能确认完成
* 只有 `COMPLETED` 才能评价
* 每单一评
* 更新服务者评分统计

**验收**

* 主人能确认完成
* 能评价且服务者评分更新

---

## Task 4.6：前端页面（主人侧）

**目标**

* 服务者列表页
* 服务者详情页
* 发起订单页
* 我的订单页
* 订单详情页（主人）

**要求**

* 延续“温暖治愈 + 专业可信”风格
* 先保证流程可用
* 时间轴展示 visit 留痕

**验收**

* 主人端完整下单与查看留痕流程可跑通

---

## Task 4.7：前端页面（服务者侧）

**目标**

* 服务者资料页
* 我的接单页
* 订单详情页（服务者）
* Visit 留痕编辑组件

**验收**

* 服务者端接单与留痕流程完整可用

---

## Task 4.8：联调与回归测试

**重点回归项**

* 非服务者认证用户不能上架服务者资料
* `ACTIVE` 服务者才能被下单
* 订单仅相关方可查看详情
* visit 状态机正确（PENDING -> STARTED -> DONE）
* 全部 visit 完成后订单变 `WAITING_OWNER_CONFIRM`
* 主人确认后订单变 `COMPLETED`
* 每单仅一评，评分聚合正确

---

## 10）给 Codex 的执行提示词（可直接复制）

```text
实现第4阶段（上门喂养核心模块），基于已完成的 Phase 0 + 第2阶段认证模块 + 第3阶段宠物/领养模块继续开发，并使用 MySQL。

【本阶段范围】
1) 服务者资料（上架/暂停/公开展示）
2) 服务者列表与详情（前台）
3) 主人发起上门喂养订单（预约请求）
4) 服务者接单/拒单
5) 按次服务（visit）留痕：图片+文字
6) 主人确认完成与评价
7) 我的订单（主人）与我的接单（服务者）

【严格边界】
- 不做在线支付
- 不做即时聊天
- 不做抢单大厅（本期是“指定服务者下单”）
- 不做投诉仲裁（后续风控阶段再做）

【数据库要求（MySQL）】
新增表：
- feeding_provider_profiles
- feeding_orders
- feeding_order_pets
- feeding_order_visits
- feeding_visit_media
- feeding_order_reviews

要求：
- JSON 字段使用 MySQL json
- 时间字段使用 datetime(3)
- 所有表 utf8mb4
- Flyway 迁移脚本使用 MySQL 语法

【跨模块依赖】
- 服务者资料上架、接单：必须已通过服务者认证（Phase2 PROVIDER）
- 主人下单：建议强制实名认证通过（Phase2）
- 下单时 petIds 复用第3阶段 pets 表，必须校验 owner_user_id
- 留痕图片复用 file_objects，必须校验 status=READY 且文件归当前服务者所有

【状态机（必须严格实现）】
订单状态：
- PENDING_PROVIDER_ACCEPT
- REJECTED_BY_PROVIDER
- CONFIRMED
- IN_SERVICE
- WAITING_OWNER_CONFIRM
- COMPLETED
- CANCELLED_BY_OWNER
- CANCELLED_BY_PROVIDER

visit 状态：
- PENDING -> STARTED -> DONE
- 订单取消前可把未开始 visit 标为 CANCELLED

规则：
- 服务者接单：PENDING_PROVIDER_ACCEPT -> CONFIRMED
- 服务者拒单：PENDING_PROVIDER_ACCEPT -> REJECTED_BY_PROVIDER
- 第一次开始 visit：CONFIRMED -> IN_SERVICE
- 所有 visit 完成：订单 -> WAITING_OWNER_CONFIRM
- 主人确认完成：WAITING_OWNER_CONFIRM -> COMPLETED
- 主人评价：仅 COMPLETED 且每单一评

【前端要求】
用户端页面：
- 服务者列表页
- 服务者详情页
- 发起订单页
- 我的订单页
- 订单详情页（主人）
- 服务者资料页
- 我的接单页
- 订单详情页（服务者+visit留痕）

先保证功能闭环可用，再优化样式。

【验收标准】
- 服务者可完善资料并上架
- 普通用户可浏览服务者并下单
- 服务者可接单/拒单
- 服务者可按次提交留痕
- 主人可查看留痕、确认完成并评价
- 评分统计可更新
```

---

## 11）第4阶段验收清单（你自己验）

* [ ] Flyway 新增喂养模块表迁移成功（MySQL）
* [ ] 服务者资料可创建/更新/上架（仅认证通过用户）
* [ ] 服务者列表和详情页可展示
* [ ] 主人可创建喂养订单（含多次 visit）
* [ ] 服务者可接单/拒单
* [ ] 服务者可开始 visit 并提交留痕（图片+备注）
* [ ] 全部 visit 完成后订单进入待主人确认
* [ ] 主人可确认完成
* [ ] 主人可评价且每单只能评价一次
* [ ] 服务者评分统计更新正确

---
