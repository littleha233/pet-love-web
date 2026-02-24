# PetLove Runbook

## 当前项目进展

- 后端：已完成 `dev-phase-1.md`（Phase 0）+ `dev-phase-2.md`（Phase 2 后端）+ `dev-phase-3.md`（Phase 3 后端）
- 前端：`web-user` 仍为静态页面（首页、领养列表、服务页，mock 数据）
- 业务状态：Phase 3 领养/送养后端闭环已完成（送养帖 + 领养申请 + Admin 审核）

后端新增可用能力（Phase 2 + Phase 3）：

- 用户实名认证提交/查询
- 用户服务者认证提交/查询
- Admin 认证审核（通过/驳回）
- 审核动作审计日志
- 审核通过后 `/api/v1/auth/me` 返回 `PROVIDER` 角色
- 送养帖发布/列表/详情/我的发布/重提/关闭
- 领养申请提交/我的申请/帖子申请列表/处理/撤回
- Admin 送养帖审核（通过/驳回/下架）

## 启动顺序（推荐）

1. 启动基础依赖（MySQL、MinIO）
2. 启动后端（Spring Boot）
3. 启动前端（web-user）

## 一键准备（首次）

在项目根目录执行：

```bash
cp .env.example .env
docker compose up -d
```

检查容器：

```bash
docker compose ps
```

默认端口：

- MySQL: `localhost:3307`（避免与本机已有 MySQL 冲突）
- MinIO API: `localhost:9000`
- MinIO Console: `http://localhost:9001`

## 启动后端

```bash
./mvnw spring-boot:run
```

启动后访问：

- Swagger: `http://localhost:8080/swagger-ui/index.html`
- 健康检查: `http://localhost:8080/api/v1/system/health`
- 就绪检查: `http://localhost:8080/api/v1/system/ready`

## 启动前端

新开一个终端窗口：

```bash
cd web-user
npm install
npm run dev
```

前端默认地址：

- `http://localhost:5173`

## 当前可用账号

后端启动时会自动初始化默认管理员账号：

- username: `admin`
- password: `Admin@123456`

## 快速验证（Phase 3 最小链路）

### 1) 用户登录（拿 Token）

```bash
curl -s -X POST http://localhost:8080/api/v1/auth/otp/send \
  -H 'Content-Type: application/json' \
  -d '{"channel":"MOBILE","target":"13900139000","purpose":"LOGIN"}'
```

返回里会有 `mockCode`（dev 环境），再登录：

```bash
curl -s -X POST http://localhost:8080/api/v1/auth/login/otp \
  -H 'Content-Type: application/json' \
  -d '{"channel":"MOBILE","target":"13900139000","otpCode":"<mockCode>","clientType":"WEB"}'
```

### 2) 提交实名认证

先上传证件文件（`bizType=ID_CARD`），再调用：

```bash
curl -s -X POST http://localhost:8080/api/v1/verifications/real-name/submit \
  -H 'Authorization: Bearer <userAccessToken>' \
  -H 'Content-Type: application/json' \
  -d '{"realName":"张三","idNo":"310101199901011234","idFrontFileId":1,"idBackFileId":2,"agreeDeclaration":true}'
```

### 3) Admin 审核

```bash
curl -s -X POST http://localhost:8080/api/admin/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"Admin@123456"}'
```

拿到 `adminAccessToken` 后：

```bash
curl -s "http://localhost:8080/api/admin/v1/verifications?verificationType=REAL_NAME&status=PENDING" \
  -H 'Authorization: Bearer <adminAccessToken>'
```

```bash
curl -s -X POST http://localhost:8080/api/admin/v1/verifications/<verificationId>/approve \
  -H 'Authorization: Bearer <adminAccessToken>' \
  -H 'Content-Type: application/json' \
  -d '{"remark":"ok"}'
```

### 4) 角色回显

```bash
curl -s http://localhost:8080/api/v1/auth/me \
  -H 'Authorization: Bearer <userAccessToken>'
```

服务者认证审核通过后，`roles` 应包含 `PROVIDER`。

### 5) 发布送养帖（需实名认证通过）

先上传 1 张宠物图片（`bizType=PET_MEDIA`），拿到 `fileId`，再调用：

```bash
curl -s -X POST http://localhost:8080/api/v1/adoptions/posts \
  -H 'Authorization: Bearer <userAccessToken>' \
  -H 'Content-Type: application/json' \
  -d '{
    "title":"给小橘找新家",
    "content":"因搬家无法继续照顾，希望找到稳定家庭。",
    "cityCode":"310100",
    "cityName":"上海",
    "petType":"CAT",
    "petName":"小橘",
    "petGender":"MALE",
    "ageMonths":18,
    "temperamentTags":["亲人","活泼"],
    "petImageFileIds":[1]
  }'
```

### 6) Admin 审核送养帖

```bash
curl -s "http://localhost:8080/api/admin/v1/adoptions/posts?status=PENDING_REVIEW" \
  -H 'Authorization: Bearer <adminAccessToken>'
```

```bash
curl -s -X POST http://localhost:8080/api/admin/v1/adoptions/posts/<postId>/approve \
  -H 'Authorization: Bearer <adminAccessToken>' \
  -H 'Content-Type: application/json' \
  -d '{"remark":"信息完整，审核通过"}'
```

### 7) 提交领养申请并处理

```bash
curl -s -X POST http://localhost:8080/api/v1/adoptions/posts/<postId>/applications \
  -H 'Authorization: Bearer <anotherUserAccessToken>' \
  -H 'Content-Type: application/json' \
  -d '{"message":"有稳定居住环境，愿意长期负责。"}'
```

发布者处理申请：

```bash
curl -s -X POST http://localhost:8080/api/v1/adoptions/applications/<applicationId>/handle \
  -H 'Authorization: Bearer <publisherUserAccessToken>' \
  -H 'Content-Type: application/json' \
  -d '{"action":"ACCEPT","decisionNote":"沟通顺畅，同意领养"}'
```

处理成功后，帖子状态应自动变为 `CLOSED`。

## 停止服务

停止前端：前端终端按 `Ctrl+C`

停止后端：后端终端按 `Ctrl+C`

停止 Docker 依赖：

```bash
docker compose down
```

## 常见问题

1. `Port 8080 was already in use`

- 说明端口被占用。
- 处理：停掉占用进程，或改 `server.port`。

2. Swagger 能打开但接口 401

- 受保护接口需要 Bearer Token。
- 先调用 OTP 登录拿 `accessToken`，再带 `Authorization: Bearer <token>`。

3. 数据库连接失败

- 确认 `docker compose ps` 中 mysql 是 healthy。
- 检查 `.env` 中 `DB_*` 与 `application.yml` 中占位符是否一致。
