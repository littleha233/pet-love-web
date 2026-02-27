# 0. 项目定位与边界（先定住，不然后面会越做越散）

## 产品定位（一句话）

**本地宠物服务与公益领养平台（Web 版 MVP）**
先解决三个高频问题：

1. **领养/送养信息不透明**
2. **上门喂养找人不专业、不放心**
3. **普通人不知道宠物救助正确路径**

---

## MVP 功能边界（必须严格卡住）

### ✅ 本期做（Web MVP）

1. **领养/送养信息发布与浏览**
2. **上门喂养预约/接单/服务留痕**
3. **救助指引助手（流程化，不做救助执行）**
4. **基础用户体系（登录、实名认证、角色）**
5. **后台审核与风控（内容审核、投诉处理、订单介入）**

### ❌ 本期不做（明确排除）

1. **宠物商城 / 商家入驻交易**
2. **宠物社区内容流（发帖、评论、关注）**
3. **在线问诊 / 医疗诊断**
4. **复杂即时聊天系统（先用站内留言/订单留言）**
5. **在线支付（先预留接口，首版可线下支付或平台人工确认）**
6. **移动端 App / 小程序（后续复用 API）**

---

## 合规边界（产品文案和功能都要体现）

### 1) 医疗边界

* 平台仅提供 **“救助指引 / 就医建议提示 / 机构信息”**
* 不提供在线诊断、开药建议、医疗结论

### 2) 救助边界

* 平台提供 **“发现流浪动物后的标准流程 + 本地救助资源”**
* 平台不承诺实时救援、不承诺救助成功

### 3) 上门服务边界

* 平台提供撮合、留痕、评价、投诉机制
* 首版不承诺保险赔付（后续可加）
* 必须有服务协议、免责边界、纠纷处理流程

---

# 1. MVP PRD（功能清单 + 页面结构 + 用户流程）

---

## 1.1 用户角色定义（Web MVP）

### C 端用户（普通用户）

* 浏览领养信息
* 发布送养信息（或发布领养需求）
* 发布上门喂养需求
* 使用救助指引
* 提交投诉/举报

### 服务者（上门喂养服务者）

* 申请成为服务者
* 设置服务范围/价格/可服务时间
* 接单、执行、上传服务留痕
* 查看评价

### 平台运营（后台）

* 审核内容（领养/送养）
* 审核服务者认证
* 处理订单异常与投诉
* 管理救助资源目录
* 风控封禁 / 拉黑

---

## 1.2 MVP 模块清单（按优先级）

## P0（必须）

### 模块 A：用户与认证

* 登录（手机号验证码 / 邮箱验证码，二选一）
* 用户资料（昵称、头像、城市）
* 实名认证（至少服务者必须实名）
* 角色切换（普通用户 / 服务者）

**边界：**

* 首版不做第三方社交登录
* 首版不做复杂 KYC，只做基础实名信息 + 证件照片（后台审核）

---

### 模块 B：领养/送养信息

#### 用户侧功能

* 浏览列表（按城市、宠物类型、状态筛选）
* 查看详情（宠物信息、健康情况、是否绝育、联系方式方式）
* 发布信息（领养/送养）
* 提交领养申请
* 查看我的发布 / 我的申请

#### 平台侧能力（后台）

* 审核帖子（通过/驳回）
* 审核照片
* 下架帖子
* 查看举报

**边界：**

* 首版不做担保交易
* 首版不做复杂匹配算法
* 首版不做聊天，只做“申请 + 留言”

---

### 模块 C：上门喂养（核心交易模块）

#### 用户侧功能（宠物主人）

* 发布喂养需求（时间、地址、宠物习性、服务内容）
* 查看服务者候选（同城）
* 发起预约（生成订单）
* 查看订单状态
* 服务后评价

#### 服务者侧功能

* 服务者资料页（简介、经验、价格）
* 可服务区域设置
* 接单 / 拒单
* 到达打卡、离开打卡
* 上传服务留痕（图片/视频/文字）
* 异常上报（宠物不进食、门锁问题等）

#### 平台侧功能

* 订单查看与人工介入
* 异常单处理
* 投诉仲裁（记录结果）

**边界：**

* 首版不做线上支付（先支持“线下支付 + 平台记录”）
* 首版不做路线轨迹采集（只做到达/离开打卡）
* 首版不做保险系统（但预留字段）

---

### 模块 D：救助指引助手（流程型功能）

#### 用户侧功能

* 表单式输入：

    * 城市/地点
    * 宠物类型
    * 是否受伤
    * 是否可接近
    * 是否能临时安置
* 输出结果：

    * 建议操作步骤（SOP）
    * 注意事项（安全、应激）
    * 本地救助资源（机构/志愿者/电话）
    * 可提交“救助线索”

#### 平台侧功能

* 管理城市救助资源目录
* 管理指引文案（模板）

**边界：**

* 不做实时派单救助
* 不做诊疗建议
* 不做 24 小时热线承诺

---

### 模块 E：举报/投诉/风控（基础版）

* 举报领养帖子
* 投诉喂养订单
* 后台处理记录
* 黑名单（用户/服务者）
* 审核日志

---

## P1（可延后，但建议预留字段）

* 系统通知（站内通知）
* 订单留言（非即时聊天）
* 服务者评分标签（准时、细心、照片清晰）
* 城市开通开关（某城市暂未开通服务）

---

## 1.3 Web 页面结构（信息架构）

> 建议 Web 首版做 **响应式页面**，后续移动端复用 API。

## 前台（用户端）

### 公共页面

* `/` 首页（4个入口）
* `/adoption` 领养/送养列表
* `/adoption/:id` 详情页
* `/feeding` 上门喂养首页（服务说明 + 服务者列表）
* `/rescue-guide` 救助指引助手
* `/service-agreement` 服务协议
* `/privacy` 隐私政策

### 登录后页面（用户中心）

* `/me` 我的主页
* `/me/profile` 个人资料
* `/me/verification` 实名认证
* `/me/adoption/posts` 我的发布
* `/me/adoption/applications` 我的申请
* `/me/feeding/orders` 我的喂养订单
* `/me/complaints` 我的投诉/举报
* `/me/provider` 服务者中心（仅服务者可见）

    * 服务者资料
    * 服务区域
    * 接单列表
    * 服务中订单
    * 历史订单 & 评价

### 发布/动作页面

* `/adoption/new` 发布领养/送养
* `/feeding/request/new` 发布喂养需求
* `/feeding/order/:id` 订单详情（用户/服务者不同视角）
* `/rescue/report/new` 提交救助线索

---

## 后台（Admin）

* `/admin/login`
* `/admin/dashboard`
* `/admin/users`
* `/admin/verifications`
* `/admin/adoption/posts`
* `/admin/adoption/applications`
* `/admin/feeding/orders`
* `/admin/feeding/providers`
* `/admin/complaints`
* `/admin/reports`
* `/admin/rescue/resources`
* `/admin/config/cities`
* `/admin/audit-logs`

---

## 1.4 用户流程（核心流程）

---

### 流程 A：送养发布 → 审核 → 领养申请 → 回访

1. 用户注册登录
2. 发布送养信息（宠物基础信息 + 照片 + 健康情况）
3. 平台审核（通过/驳回）
4. 帖子上线
5. 其他用户浏览并提交领养申请
6. 发布者查看申请并选择沟通对象（首版先用站内留言）
7. 发布者标记“已送养”
8. 平台可发起 7 天回访问卷（可人工）

**MVP 验收点**

* 帖子必须先审核再上线
* 申请必须记录时间和留言
* 帖子状态流转完整（待审/已发布/已完成/已下架）

---

### 流程 B：上门喂养下单 → 接单 → 服务留痕 → 评价

1. 用户发布喂养需求（时间、频次、地址、宠物习性）
2. 用户选择服务者发起预约（生成订单）
3. 服务者接单/拒单
4. 到达打卡（记录时间 + 图片）
5. 服务中上传留痕（喂食、换水、猫砂、视频）
6. 离开打卡（记录时间）
7. 用户确认完成（或平台超时自动完成）
8. 用户评价
9. 如有争议，提交投诉 → 后台介入

**MVP 验收点**

* 订单状态机完整（待接单/已接单/服务中/待确认/已完成/已投诉/已取消）
* 服务留痕至少支持图片 + 文本
* 打卡需记录时间（位置可选）

---

### 流程 C：救助指引助手 → 生成 SOP → 提交线索

1. 用户进入救助指引页
2. 填表（是否受伤、是否攻击性、是否可临时安置）
3. 系统生成建议步骤（规则模板）
4. 展示本地救助资源目录
5. 用户可提交救助线索（可选）
6. 后台查看线索（可转发给合作机构，首版手工）

**MVP 验收点**

* 指引结果是规则化生成，不是自由问答
* 资源目录按城市筛选
* 线索可追踪状态（已收到/处理中/已关闭）

---

## 1.5 功能边界文案（给 Codex 和前端用）

下面这段建议直接写进产品文案和页面提示里（避免后续争议）：

```text
平台为宠物领养、上门喂养及救助信息服务平台。
1) 领养/送养信息由用户发布，平台进行基础审核，但不对宠物健康、性格或后续饲养结果作保证；
2) 上门喂养服务由服务者提供，平台提供预约、留痕、评价与投诉处理机制；
3) 救助指引仅提供通用建议与本地资源信息，不构成医疗建议或实时救援承诺；
4) 如遇宠物严重受伤、攻击行为或紧急情况，请及时联系当地专业机构。
```

---

# 2. 数据库核心表设计（Web MVP 版本，Codex 可直接落表）

下面按 **“能直接让 Codex 建模”** 的方式给你。
建议首版用 **PostgreSQL**（结构化强、状态查询方便），文件走 OSS/S3/MinIO。

---

## 2.1 设计原则（先统一）

* 所有表包含：

    * `id`（bigint 或 uuid）
    * `created_at`
    * `updated_at`
* 软删除字段（建议）：

    * `is_deleted`
* 审核类字段统一：

    * `review_status`（PENDING/APPROVED/REJECTED）
    * `reviewed_by`
    * `reviewed_at`
    * `review_remark`
* 状态字段一律用枚举（代码层）+ 字符串存储（数据库）

---

## 2.2 核心枚举（建议先在后端定义）

```text
UserRole:
- USER
- PROVIDER
- ADMIN

VerificationStatus:
- NOT_SUBMITTED
- PENDING
- APPROVED
- REJECTED

AdoptionPostType:
- ADOPT   (领养求领)
- REHOME  (送养)

AdoptionPostStatus:
- DRAFT
- PENDING_REVIEW
- PUBLISHED
- CLOSED
- REJECTED
- OFFLINE

AdoptionApplicationStatus:
- SUBMITTED
- ACCEPTED
- REJECTED
- WITHDRAWN

FeedingOrderStatus:
- PENDING_PROVIDER_CONFIRM
- PROVIDER_REJECTED
- CONFIRMED
- IN_SERVICE
- WAITING_USER_CONFIRM
- COMPLETED
- CANCELED
- DISPUTED

FeedingVisitEventType:
- ARRIVAL_CHECKIN
- SERVICE_LOG
- LEAVE_CHECKOUT
- INCIDENT_REPORT

ComplaintType:
- ADOPTION_REPORT
- FEEDING_COMPLAINT
- USER_REPORT

ComplaintStatus:
- OPEN
- PROCESSING
- RESOLVED
- REJECTED

RescueClueStatus:
- RECEIVED
- PROCESSING
- CLOSED
```

---

## 2.3 用户与认证相关表

## 1) `users`

```text
用途：账号主表（登录身份）
字段：
- id (PK)
- mobile (varchar, unique, nullable)        // 若用手机号登录
- email (varchar, unique, nullable)         // 若用邮箱登录
- password_hash (varchar, nullable)         // 若验证码登录可为空
- login_type (varchar)                      // MOBILE_OTP / EMAIL_OTP / PASSWORD
- status (varchar)                          // ACTIVE / DISABLED / BANNED
- last_login_at (timestamp)
- created_at
- updated_at
索引：
- unique(mobile)
- unique(email)
- index(status)
```

## 2) `user_profiles`

```text
用途：用户资料
字段：
- id (PK)
- user_id (FK users.id, unique)
- nickname (varchar)
- avatar_url (varchar)
- city_code (varchar)
- city_name (varchar)
- district_name (varchar, nullable)
- bio (varchar, nullable)
- role_flags (jsonb)                        // {"is_provider":true}
- created_at
- updated_at
索引：
- index(city_code)
```

## 3) `user_verifications`

```text
用途：实名认证/服务者审核资料
字段：
- id (PK)
- user_id (FK users.id)
- verification_type (varchar)               // REAL_NAME / PROVIDER
- real_name (varchar)
- id_no_masked (varchar)                    // 只存掩码展示
- id_no_hash (varchar)                      // 哈希值用于去重，不明文存储
- id_front_url (varchar)
- id_back_url (varchar)
- holding_id_url (varchar, nullable)
- provider_experience_years (int, nullable)
- provider_desc (text, nullable)
- status (varchar)                          // PENDING / APPROVED / REJECTED
- reviewed_by (FK admin_users.id, nullable)
- reviewed_at (timestamp, nullable)
- review_remark (varchar, nullable)
- created_at
- updated_at
索引：
- index(user_id, verification_type)
- index(status)
```

> 注：身份证明文不要存。敏感字段（地址、电话）建议加密或至少应用层脱敏。

---

## 2.4 城市与资源目录表

## 4) `cities`

```text
用途：城市开通配置
字段：
- id (PK)
- city_code (varchar, unique)
- city_name (varchar)
- is_adoption_enabled (bool)
- is_feeding_enabled (bool)
- is_rescue_enabled (bool)
- created_at
- updated_at
索引：
- unique(city_code)
```

## 5) `rescue_resources`

```text
用途：本地救助资源目录（机构/志愿者/电话）
字段：
- id (PK)
- city_code (varchar)
- resource_type (varchar)                   // ORG / VOLUNTEER / HOSPITAL / GOV
- name (varchar)
- contact_name (varchar, nullable)
- contact_phone (varchar, nullable)
- contact_wechat (varchar, nullable)
- address (varchar, nullable)
- service_hours (varchar, nullable)
- tags (jsonb)                              // ["猫","狗","可接伤病"]
- note (text, nullable)
- status (varchar)                          // ACTIVE / INACTIVE
- created_at
- updated_at
索引：
- index(city_code, status)
- index(resource_type)
```

---

## 2.5 宠物与领养/送养模块表

## 6) `pets`

```text
用途：宠物实体（供领养帖/喂养订单复用）
字段：
- id (PK)
- owner_user_id (FK users.id, nullable)     // 送养/喂养场景可绑定主人
- pet_type (varchar)                        // CAT / DOG / OTHER
- name (varchar, nullable)
- gender (varchar, nullable)                // MALE / FEMALE / UNKNOWN
- age_months (int, nullable)
- breed (varchar, nullable)
- weight_kg (numeric(5,2), nullable)
- neutered_status (varchar, nullable)       // YES / NO / UNKNOWN
- vaccinated_status (varchar, nullable)     // YES / NO / PARTIAL / UNKNOWN
- health_note (text, nullable)
- temperament_tags (jsonb)                  // ["怕生","亲人","会哈人"]
- special_care_note (text, nullable)
- created_at
- updated_at
索引：
- index(owner_user_id)
- index(pet_type)
```

## 7) `pet_media`

```text
用途：宠物图片/视频
字段：
- id (PK)
- pet_id (FK pets.id)
- media_type (varchar)                      // IMAGE / VIDEO
- media_url (varchar)
- sort_order (int)
- created_at
索引：
- index(pet_id, sort_order)
```

## 8) `adoption_posts`

```text
用途：领养/送养发布主表
字段：
- id (PK)
- post_type (varchar)                       // ADOPT / REHOME
- publisher_user_id (FK users.id)
- pet_id (FK pets.id, nullable)             // 求领养时可为空；送养一般有
- title (varchar)
- content (text)
- city_code (varchar)
- district_name (varchar, nullable)
- contact_mode (varchar)                    // IN_APP / PHONE / WECHAT (首版建议 IN_APP)
- status (varchar)                          // DRAFT / PENDING_REVIEW / PUBLISHED / CLOSED / REJECTED / OFFLINE
- review_status (varchar)                   // PENDING / APPROVED / REJECTED
- reviewed_by (FK admin_users.id, nullable)
- reviewed_at (timestamp, nullable)
- review_remark (varchar, nullable)
- published_at (timestamp, nullable)
- closed_at (timestamp, nullable)
- view_count (int, default 0)
- created_at
- updated_at
索引：
- index(city_code, status)
- index(publisher_user_id, status)
- index(review_status)
- index(created_at desc)
```

## 9) `adoption_applications`

```text
用途：领养申请
字段：
- id (PK)
- post_id (FK adoption_posts.id)
- applicant_user_id (FK users.id)
- message (text)
- living_env_note (text, nullable)          // 可选：居住环境
- pet_experience_note (text, nullable)      // 可选：养宠经验
- status (varchar)                          // SUBMITTED / ACCEPTED / REJECTED / WITHDRAWN
- handled_by_user_id (FK users.id, nullable)// 发布者处理
- handled_at (timestamp, nullable)
- created_at
- updated_at
约束：
- unique(post_id, applicant_user_id)        // 一个用户对同一帖只申请一次（可撤回后再申请也可放开）
索引：
- index(post_id, status)
- index(applicant_user_id, status)
```

## 10) `adoption_followups`

```text
用途：领养后回访记录（平台或发布者）
字段：
- id (PK)
- post_id (FK adoption_posts.id)
- application_id (FK adoption_applications.id, nullable)
- followup_type (varchar)                   // D7 / D30 / CUSTOM
- content (text)
- followup_status (varchar)                 // PENDING / DONE
- created_by (FK users.id or admin_users.id)
- created_at
- updated_at
索引：
- index(post_id)
- index(followup_status)
```

---

## 2.6 上门喂养模块表（核心）

## 11) `feeding_provider_profiles`

```text
用途：服务者资料（接单侧）
字段：
- id (PK)
- user_id (FK users.id, unique)
- display_name (varchar)
- intro (text)
- experience_years (int, nullable)
- service_pet_types (jsonb)                 // ["CAT","DOG"]
- base_price_per_visit (numeric(10,2))
- holiday_price_per_visit (numeric(10,2), nullable)
- service_time_slots (jsonb)                // ["09:00-12:00","18:00-21:00"]
- status (varchar)                          // PENDING / ACTIVE / SUSPENDED
- rating_avg (numeric(3,2), default 0)
- rating_count (int, default 0)
- completed_order_count (int, default 0)
- created_at
- updated_at
索引：
- index(status)
- index(rating_avg desc)
```

## 12) `feeding_provider_service_areas`

```text
用途：服务者服务区域
字段：
- id (PK)
- provider_user_id (FK users.id)
- city_code (varchar)
- district_name (varchar)
- radius_km (int, nullable)                 // 可选
- created_at
索引：
- index(provider_user_id)
- index(city_code, district_name)
```

## 13) `feeding_requests`

```text
用途：宠物主人发布的喂养需求（订单前）
字段：
- id (PK)
- requester_user_id (FK users.id)
- city_code (varchar)
- district_name (varchar)
- address_encrypted (text)                  // 敏感信息加密存储
- contact_phone_encrypted (text)
- start_date (date)
- end_date (date)
- visit_frequency_per_day (int)
- preferred_time_slots (jsonb)
- pet_count (int)
- pet_summary (text)                        // 简述，可关联 pets 详细信息
- service_items (jsonb)                     // ["FEED","WATER","LITTER","PLAY"]
- note (text, nullable)
- status (varchar)                          // OPEN / BOOKED / CLOSED / CANCELED
- created_at
- updated_at
索引：
- index(requester_user_id, status)
- index(city_code, status)
- index(start_date, end_date)
```

## 14) `feeding_request_pets`

```text
用途：喂养需求与宠物关联（多宠物）
字段：
- id (PK)
- request_id (FK feeding_requests.id)
- pet_id (FK pets.id)
索引：
- index(request_id)
- index(pet_id)
```

## 15) `feeding_orders`

```text
用途：喂养订单主表（预约成功后）
字段：
- id (PK)
- order_no (varchar, unique)
- request_id (FK feeding_requests.id)
- requester_user_id (FK users.id)
- provider_user_id (FK users.id)
- city_code (varchar)
- service_start_date (date)
- service_end_date (date)
- total_visits (int)
- pricing_snapshot (jsonb)                  // 价格快照，避免后续改价影响历史
- total_amount (numeric(10,2), nullable)
- payment_status (varchar)                  // UNPAID / OFFLINE_PAID / ONLINE_PAID / REFUNDED (预留)
- status (varchar)                          // 订单状态机
- cancel_reason (varchar, nullable)
- dispute_flag (bool, default false)
- user_confirmed_at (timestamp, nullable)
- provider_confirmed_at (timestamp, nullable)
- completed_at (timestamp, nullable)
- created_at
- updated_at
索引：
- unique(order_no)
- index(requester_user_id, status)
- index(provider_user_id, status)
- index(status, created_at desc)
```

## 16) `feeding_order_visits`

```text
用途：按次服务记录（一个订单可能有多次上门）
字段：
- id (PK)
- order_id (FK feeding_orders.id)
- visit_seq (int)                           // 第几次上门
- scheduled_date (date)
- scheduled_time_slot (varchar)
- status (varchar)                          // PENDING / IN_PROGRESS / DONE / FAILED
- arrival_at (timestamp, nullable)
- leave_at (timestamp, nullable)
- created_at
- updated_at
约束：
- unique(order_id, visit_seq)
索引：
- index(order_id, status)
- index(scheduled_date)
```

## 17) `feeding_visit_events`

```text
用途：服务留痕事件（打卡/图片/文字/异常）
字段：
- id (PK)
- order_id (FK feeding_orders.id)
- visit_id (FK feeding_order_visits.id, nullable)
- event_type (varchar)                      // ARRIVAL_CHECKIN / SERVICE_LOG / LEAVE_CHECKOUT / INCIDENT_REPORT
- operator_user_id (FK users.id)            // 一般是服务者
- text_content (text, nullable)
- media_urls (jsonb, nullable)              // ["url1","url2"]
- geo_lite (varchar, nullable)              // 首版可选：粗略位置/街道，不存精确坐标也行
- event_time (timestamp)
- created_at
索引：
- index(order_id, event_time)
- index(visit_id, event_type)
```

## 18) `feeding_order_reviews`

```text
用途：用户评价服务者
字段：
- id (PK)
- order_id (FK feeding_orders.id, unique)
- reviewer_user_id (FK users.id)
- provider_user_id (FK users.id)
- rating (int)                              // 1-5
- tags (jsonb)                              // ["准时","细心","照片清晰"]
- content (text, nullable)
- created_at
索引：
- index(provider_user_id, rating)
```

---

## 2.7 救助指引与线索表

## 19) `rescue_guide_templates`

```text
用途：规则化指引模板（按条件组合）
字段：
- id (PK)
- city_code (varchar, nullable)             // null 表示通用模板
- pet_type (varchar)                        // CAT / DOG / ANY
- injury_level (varchar)                    // NONE / POSSIBLE / SEVERE
- aggression_level (varchar)                // LOW / MEDIUM / HIGH / UNKNOWN
- can_temporarily_host (bool, nullable)
- step_text (text)                          // 渲染给用户的步骤
- caution_text (text, nullable)
- priority (int, default 100)
- status (varchar)                          // ACTIVE / INACTIVE
- created_at
- updated_at
索引：
- index(city_code, status)
- index(priority)
```

## 20) `rescue_clues`

```text
用途：用户提交的救助线索
字段：
- id (PK)
- reporter_user_id (FK users.id, nullable)
- city_code (varchar)
- district_name (varchar, nullable)
- location_text (varchar)
- pet_type (varchar)
- injury_level (varchar)
- aggression_level (varchar)
- can_temporarily_host (bool)
- description (text, nullable)
- media_urls (jsonb, nullable)
- status (varchar)                          // RECEIVED / PROCESSING / CLOSED
- handled_by (FK admin_users.id, nullable)
- handle_remark (text, nullable)
- created_at
- updated_at
索引：
- index(city_code, status)
- index(created_at desc)
```

---

## 2.8 举报/投诉/风控/后台相关表

## 21) `complaints`

```text
用途：统一投诉/举报表
字段：
- id (PK)
- complaint_type (varchar)                  // ADOPTION_REPORT / FEEDING_COMPLAINT / USER_REPORT
- target_type (varchar)                     // ADOPTION_POST / FEEDING_ORDER / USER
- target_id (bigint)
- reporter_user_id (FK users.id)
- reason_code (varchar)
- content (text, nullable)
- evidence_urls (jsonb, nullable)
- status (varchar)                          // OPEN / PROCESSING / RESOLVED / REJECTED
- assigned_admin_id (FK admin_users.id, nullable)
- resolution (text, nullable)
- resolved_at (timestamp, nullable)
- created_at
- updated_at
索引：
- index(target_type, target_id)
- index(status, created_at desc)
- index(reporter_user_id)
```

## 22) `blacklist_records`

```text
用途：黑名单/限制记录
字段：
- id (PK)
- user_id (FK users.id)
- scope (varchar)                           // ALL / ADOPTION / FEEDING
- reason (varchar)
- start_at (timestamp)
- end_at (timestamp, nullable)
- created_by (FK admin_users.id)
- created_at
索引：
- index(user_id, scope)
- index(end_at)
```

## 23) `admin_users`

```text
用途：后台账号
字段：
- id (PK)
- username (varchar, unique)
- password_hash (varchar)
- role (varchar)                            // SUPER_ADMIN / AUDITOR / OPS / CS
- status (varchar)                          // ACTIVE / DISABLED
- created_at
- updated_at
索引：
- unique(username)
- index(role, status)
```

## 24) `admin_audit_logs`

```text
用途：后台操作日志（非常关键）
字段：
- id (PK)
- admin_user_id (FK admin_users.id)
- action (varchar)                          // APPROVE_ADOPTION_POST / REJECT_PROVIDER ...
- target_type (varchar)
- target_id (bigint)
- before_snapshot (jsonb, nullable)
- after_snapshot (jsonb, nullable)
- remark (varchar, nullable)
- created_at
索引：
- index(admin_user_id, created_at desc)
- index(target_type, target_id)
```

---

## 2.9 首版可选表（建议预留）

* `system_notifications`（站内通知）
* `file_uploads`（统一文件元数据）
* `idempotency_keys`（接口幂等）
* `operation_logs`（用户侧操作日志，可后补）

---

# 3. 第一版后台管理设计（Admin v1）

后台不是“锦上添花”，是这类产品的核心。
你这项目如果没有后台审核和风控，前台很快会变成垃圾信息和纠纷池。

---

## 3.1 后台角色权限（建议最小集）

### 1) SUPER_ADMIN

* 全部权限
* 管理后台账号
* 查看所有日志

### 2) AUDITOR（审核）

* 审核实名认证
* 审核服务者申请
* 审核领养/送养帖子
* 下架内容

### 3) OPS（运营）

* 管理救助资源目录
* 管理城市开通状态
* 查看数据看板
* 配置文案模板

### 4) CS（客服/仲裁）

* 处理投诉/举报
* 处理喂养订单异常
* 拉黑/解封（有限权限）

---

## 3.2 后台模块设计（v1）

---

### 模块 1：Dashboard（数据看板）

**目的：** 每天知道平台是不是在健康运行

显示指标（按日/周）：

* 新增注册数
* 实名提交数 / 通过率
* 新增领养/送养帖数 / 审核通过率
* 喂养订单数 / 完成率 / 投诉率
* 救助线索数
* 待处理任务数（审核/投诉）

---

### 模块 2：用户与认证审核

#### 列表页字段

* 用户ID
* 昵称
* 城市
* 认证类型（实名/服务者）
* 提交时间
* 状态
* 审核人

#### 详情页动作

* 查看证件图片
* 查看服务者介绍/经验
* 审核通过
* 驳回（必须填写理由）
* 备注（内部可见）

**关键要求**

* 所有审核动作写入 `admin_audit_logs`
* 驳回理由需标准化（前端可下拉 + 自定义）

---

### 模块 3：领养/送养内容审核

#### 列表筛选

* 城市
* 帖子类型（领养/送养）
* 状态
* 发布时间
* 举报标记

#### 审核重点

* 是否明显广告
* 是否虚假/重复信息
* 是否不当内容（虐待、倒卖倾向）
* 联系方式是否违规（首版可限制站内申请）

#### 动作

* 通过
* 驳回（理由）
* 下架
* 封禁发布者（跳转风控）

---

### 模块 4：上门喂养订单管理（客服核心）

#### 列表筛选

* 订单状态
* 城市
* 服务者
* 用户
* 是否争议单
* 日期范围

#### 详情页信息

* 订单基本信息
* 宠物信息
* 服务留痕时间线（打卡、图片、文字）
* 评价
* 投诉记录（如有）

#### 动作

* 人工标记异常
* 协助取消
* 进入投诉单处理
* 内部备注

---

### 模块 5：投诉/举报中心

建议做统一工单流：

#### 工单字段

* 工单编号
* 类型（领养举报 / 喂养投诉 / 用户举报）
* 目标对象
* 证据
* 状态
* 指派人
* SLA 超时提醒（v1 可先无提醒，只显示）

#### 动作

* 受理
* 驳回
* 处理完成（填写结论）
* 关联处罚（下架 / 警告 / 拉黑）

---

### 模块 6：救助资源目录管理

#### 维护能力

* 按城市维护机构/志愿者
* 标签管理（猫/狗/伤病/夜间）
* 状态启停
* 备注（如“仅白天联系”）

#### 质量要求

* 电话/微信信息要核验
* 资源失效要有下线机制

---

### 模块 7：城市与配置管理

首版最实用的配置项：

* 城市是否开通领养
* 城市是否开通喂养
* 城市是否开通救助指引
* 平台提示文案（公告）
* 举报原因枚举（可配置）

---

### 模块 8：操作日志（审计）

必须可按以下维度查询：

* 后台账号
* 动作类型
* 时间范围
* 目标对象

这是后续处理误操作和纠纷的关键。

---

# 4. 冷启动运营清单（你这个项目成败关键在这里）

你是技术强，这很好；但这个方向的核心瓶颈不是代码，而是**本地供给与信任**。
所以冷启动要先做“密度”，不是先做“流量”。

---

## 4.1 先选一个试点城市（不要全国铺）

选城市标准（满足 3 条即可）：

* 你有本地资源（朋友/宠物店/救助组织）
* 宠物密度高（年轻人多、租房群体多）
* 你能线下跑几次（验证服务流程）

> 目标：先把一个城市做出可用性，再复制。

---

## 4.2 冷启动供给侧（先有供给，再拉需求）

## A. 上门喂养服务者（第一优先）

首批目标：**10~20 人**

来源：

1. 宠物店员工（兼职意愿高）
2. 宠物美容师/寄养店员工
3. 养宠 KOC（有经验、愿意接单）
4. 朋友转介绍

你要准备的招募物料：

* 服务者权益说明（接单、收入、评价）
* 服务规范 SOP（喂食、拍照、异常上报）
* 入驻流程（实名 + 审核）
* 样例订单截图（提高信任感）

---

## B. 救助资源合作方（第二优先）

首批目标：**2~5 个本地机构/志愿者**

* 不需要深度合作，先做资源录入和信息核验
* 重点是“可联系、有人响应、信息准确”

你要做的：

* 建一份本地救助资源表（Excel 也行）
* 电话核验
* 标注服务范围（猫/狗/伤病）

---

## C. 领养/送养信息来源（第三优先）

首批目标：**30~50 条有效信息**
来源：

* 本地救助组织
* 宠物医院公告板
* 宠物店合作
* 朋友圈 / 社群征集

关键不是量，是：

* 信息真实
* 图片清楚
* 资料完整
* 可回访

---

## 4.3 冷启动需求侧（拉第一批用户）

### 渠道建议（先低成本）

1. **小红书**（重点）

    * 内容方向：春节上门喂养避坑、领养流程指南、救助正确姿势
    * 目标：导流到 Web
2. **同城微信群 / 业主群**

    * 重点推广“春节/出差上门喂养”
3. **宠物店线下二维码**

    * 帮店里减轻寄养咨询压力（你给店导流也行）
4. **朋友圈 / 熟人裂变**

    * 先做 10 单，积累评价截图

---

## 4.4 冷启动运营动作清单（按周）

## 第 1 周：准备期

* [ ] 选定试点城市
* [ ] 敲定功能边界（就用上面的）
* [ ] 整理 1 份服务者 SOP
* [ ] 整理 1 份救助指引模板
* [ ] 建立冷启动数据表（供给、需求、订单）

## 第 2~3 周：供给侧搭建

* [ ] 招募首批服务者 10 人
* [ ] 完成 5 人实名认证和审核
* [ ] 建立本地救助资源 20 条
* [ ] 上线首批领养/送养信息 20 条

## 第 4~6 周：小范围试运营

* [ ] 跑出首批 10 单喂养订单
* [ ] 收集服务留痕样本（作为宣传素材）
* [ ] 收集用户评价 10 条
* [ ] 复盘投诉/异常流程

## 第 7~8 周：迭代与判断

* [ ] 看复购率和投诉率
* [ ] 优化服务者审核标准
* [ ] 优化订单流程和留痕模板
* [ ] 决定是否扩大到第二个城区/城市

---

## 4.5 冷启动关键指标（不要只看注册）

首版建议盯这 8 个：

1. 服务者审核通过率
2. 领养/送养帖审核通过率
3. 喂养订单下单转化率
4. 订单完成率
5. 服务留痕完整率（有到达/服务/离开记录）
6. 投诉率
7. 7 日复购率（喂养）
8. 领养申请转化率（帖子→申请）

> 只要“喂养复购率”起来，你这个副业就有长期价值。

---

# 5. 给 Codex 的分阶段开发计划（Web 端，分模块交付）

你说得很对：**最好分阶段、分模块交给 Codex**。
下面这个切法是为了降低返工率。

---

## 5.1 推荐开发顺序（非常关键）

### Phase 0：工程骨架与基础设施（先打地基）

**目标**：先把“可持续开发”的骨架搭好

包含：

* Monorepo（前端 + 后端）
* 环境配置（dev/test/prod）
* 数据库连接与迁移
* 用户鉴权（JWT / Session）
* 文件上传（图片）
* 基础错误码与日志
* RBAC（前后台权限）

**交付标准**

* 能登录
* 能上传图片
* 能跑数据库迁移
* 有基础后台页面框架

---

### Phase 1：用户/认证模块

**目标**：先打通用户体系（后续所有模块都依赖）

包含：

* 注册/登录
* 用户资料
* 实名认证提交
* 服务者认证提交
* 后台审核认证

**交付标准**

* 用户能提交实名
* 后台能审核并写日志
* 用户角色切换生效

---

### Phase 2：领养/送养模块（内容审核模型）

**目标**：先做低风险模块，验证“发布-审核-申请”闭环

包含：

* 宠物信息管理（pets）
* 领养/送养帖子发布
* 列表/详情
* 领养申请
* 后台帖子审核/下架
* 举报入口（简版）

**交付标准**

* 完整状态流转
* 后台审核可用
* 用户可申请并查看状态

---

### Phase 3：上门喂养模块（核心）

**目标**：做你产品最关键的交易闭环

包含：

* 服务者资料 & 服务区域
* 喂养需求发布
* 订单创建与状态机
* 服务者接单/拒单
* 到达/离开打卡
* 服务留痕上传
* 用户确认完成 & 评价
* 投诉入口（接到统一工单）

**交付标准**

* 订单状态机跑通
* 至少一单完整流程可演示
* 留痕时间线可查看

---

### Phase 4：救助指引模块

**目标**：做“规则化指引 + 本地资源目录”

包含：

* 救助指引表单
* 模板匹配规则
* 输出指引结果
* 资源目录展示（按城市）
* 救助线索提交
* 后台资源管理 / 线索查看

**交付标准**

* 输入条件能返回稳定结果
* 后台能维护资源目录

---

### Phase 5：后台增强与风控

**目标**：把平台运营能力补齐

包含：

* 投诉工单中心
* 黑名单
* 城市配置开关
* Dashboard
* 操作日志检索

**交付标准**

* 后台可完成基础运营闭环
* 关键动作全有审计日志

---

## 5.2 给 Codex 的模块任务模板（建议你每次都这样发）

你后续可以按下面模板给 Codex 分单，效果会比“帮我写这个模块”好很多。

```text
【任务名称】
实现 Web MVP 的 <模块名>（后端 + 前端基础页面 + 数据库迁移）

【业务背景】
这是一个宠物平台 Web MVP，当前只做：
1) 领养/送养
2) 上门喂养
3) 救助指引
请严格遵守边界：不做在线支付、不做商城、不做即时聊天、不做医疗诊断。

【本次目标】
实现 <本模块的目标>，跑通最小闭环。

【涉及表】
- 表1
- 表2
- 表3

【需要实现的后端接口】
1. xxx
2. xxx
3. xxx

【需要实现的前端页面】
1. xxx 页面（字段、交互）
2. xxx 页面（字段、交互）

【状态机/枚举】
- 状态A -> 状态B -> 状态C（允许/不允许的流转写清楚）

【验收标准】
- [ ] 能创建数据
- [ ] 能列表查询
- [ ] 能状态流转
- [ ] 后台能审核/处理
- [ ] 异常路径有校验和报错

【代码要求】
- 接口有 DTO 校验
- 关键动作写审计日志
- 敏感信息脱敏返回
- 单元测试覆盖核心状态流转
```

---

## 5.3 建议你给 Codex 的第一批任务（按顺序）

你可以直接复制这几个任务拆开跑：

### Task 1：项目骨架 + 鉴权 + 文件上传

* 后端：用户表、登录、JWT、中间件、上传接口
* 前端：登录页、基础布局、路由守卫
* 后台：Admin 登录 + 空壳菜单

### Task 2：实名认证（用户与服务者）

* `users / user_profiles / user_verifications / admin_audit_logs`
* 用户提交认证页
* 后台审核页

### Task 3：领养/送养模块

* `pets / pet_media / adoption_posts / adoption_applications`
* 发布页、列表页、详情页、申请页
* 后台审核页

### Task 4：上门喂养（核心 v1）

* `feeding_provider_profiles / feeding_provider_service_areas / feeding_requests / feeding_orders / feeding_order_visits / feeding_visit_events / feeding_order_reviews`
* 用户下单页
* 服务者接单页
* 服务留痕页
* 订单详情页

### Task 5：救助指引

* `rescue_guide_templates / rescue_resources / rescue_clues`
* 指引页面 + 后台资源管理

### Task 6：投诉/举报与风控

* `complaints / blacklist_records`
* 用户投诉入口
* 后台工单处理页

---

# 6. 额外建议（为了后面移动端不返工）

你现在先做 Web 是对的。为了后面做移动端更顺，建议一开始就这样设计：

## 6.1 API First（后端优先）

* 前后端严格分离
* API 文档（OpenAPI/Swagger）自动生成
* 前端只调 API，不写“后端模板渲染”

这样后面做移动端（App/小程序）直接复用 API。

---

## 6.2 敏感信息处理（一开始就做）

尤其是上门喂养的地址、电话：

* 数据库存加密（或至少应用层加密）
* 接口返回脱敏
* 只有订单相关角色可见完整信息

---

## 6.3 状态机不要写死在前端

像订单状态、审核状态、投诉状态：

* 后端做状态流转校验
* 前端只负责展示和触发动作

否则后面你会很难改。

---

## 6.4 文件上传统一抽象

宠物图、证件图、服务留痕图本质都是文件：

* 统一 `upload` 接口
* 统一文件命名规则
* 统一图片压缩/大小限制
* 统一鉴权

---

