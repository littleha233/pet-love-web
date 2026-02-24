# PetLove Web - Phase 0 + Phase 2 Backend

运行与联调步骤请看：[README-RUNBOOK.md](README-RUNBOOK.md)

## 1. 当前实现范围

当前后端已完成：

- `dev-phase-1.md` 的 Phase 0 基础设施
- `dev-phase-2.md` 的 Phase 2 用户认证模块（后端）

已实现能力：

- 统一接口响应体与错误码
- requestId 日志链路
- JWT + Refresh Token Rotation（用户端 + Admin 端）
- OTP 登录（开发环境 mock OTP）
- 用户资料查询/更新
- 文件上传（本地存储）
- 用户实名认证提交/查询
- 用户服务者认证提交/查询
- Admin 认证审核（通过/驳回）+ 审计日志
- 审核通过后角色生效（`PROVIDER`）
- Flyway 数据库迁移（V1~V5）
- OpenAPI 文档（Swagger UI）

## 2. 模块结构（后端）

后端代码入口：`src/main/java/com/petlove/weblove`

- `common`：统一响应、异常、日志过滤器、工具
- `config`：配置属性、安全配置、OpenAPI
- `security`：JWT、鉴权过滤器、权限上下文
- `modules/auth`：OTP/登录/refresh/logout/me
- `modules/user`：profile + Admin 用户管理
- `modules/file`：上传、文件元信息、文件内容读取
- `modules/verification`：实名认证/服务者认证 + Admin 审核
- `modules/admin`：Admin 鉴权、菜单、审计日志
- `modules/system`：health/ready/cities、启动初始化

## 3. 数据库与迁移

Flyway 迁移脚本：`src/main/resources/db/migration`

- `V1__init_core_tables.sql`
- `V2__seed_base_data.sql`
- `V3__add_indexes.sql`
- `V4__create_user_verifications.sql`
- `V5__alter_user_profiles_add_verification_flags.sql`

当前核心表：

- `users`
- `user_profiles`
- `user_verifications`
- `auth_otp_codes`
- `auth_refresh_tokens`
- `file_objects`
- `admin_users`
- `admin_audit_logs`
- `cities`
- `system_configs`

## 4. 本地启动

### 4.1 启动基础设施

```bash
cp .env.example .env
docker compose up -d
```

默认端口：

- MySQL: `localhost:3307`（避免与本机已有 MySQL 冲突）
- MinIO: `localhost:9000`（console `localhost:9001`）

### 4.2 启动后端

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

备注：

- 默认配置文件为 `application.yml`（H2 内存库，MySQL 兼容模式）
- `local` profile 使用 MySQL（建议联调时使用）

## 5. 测试

```bash
./mvnw test
```

## 6. 默认账号

应用启动时会自动初始化一个超级管理员（若不存在）：

- username: `admin`
- password: `Admin@123456`

## 7. API 文档

启动后访问：

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`

## 8. 已实现接口

### 用户端

- `POST /api/v1/auth/otp/send`
- `POST /api/v1/auth/login/otp`
- `POST /api/v1/auth/refresh`
- `POST /api/v1/auth/logout`
- `GET /api/v1/auth/me`
- `GET /api/v1/users/me/profile`
- `PATCH /api/v1/users/me/profile`
- `POST /api/v1/files/upload`
- `GET /api/v1/files/{fileId}`
- `GET /api/v1/files/content/{fileId}`
- `GET /api/v1/system/health`
- `GET /api/v1/system/ready`
- `GET /api/v1/meta/cities`
- `GET /api/v1/verifications/me`
- `GET /api/v1/verifications/{type}` (`REAL_NAME` / `PROVIDER`)
- `POST /api/v1/verifications/real-name/submit`
- `POST /api/v1/verifications/provider/submit`

### Admin

- `POST /api/admin/v1/auth/login`
- `POST /api/admin/v1/auth/refresh`
- `POST /api/admin/v1/auth/logout`
- `GET /api/admin/v1/auth/me`
- `GET /api/admin/v1/meta/menus`
- `GET /api/admin/v1/users`
- `PATCH /api/admin/v1/users/{userId}/status`
- `GET /api/admin/v1/audit-logs`
- `GET /api/admin/v1/verifications`
- `GET /api/admin/v1/verifications/{verificationId}`
- `POST /api/admin/v1/verifications/{verificationId}/approve`
- `POST /api/admin/v1/verifications/{verificationId}/reject`

## 9. 状态机与关键规则

- 用户状态：`ACTIVE / DISABLED / BANNED`
- OTP 状态：`ISSUED / VERIFIED / USED / EXPIRED`
- Refresh Token 状态：`ACTIVE / ROTATED / REVOKED / EXPIRED`
- 文件状态：`UPLOADING / READY / FAILED / DELETED`
- 认证状态：`PENDING / APPROVED / REJECTED`

关键规则：

- refresh token 使用后立即 `ROTATED`
- 被禁用/封禁用户无法继续访问受保护接口
- 服务者认证前必须实名认证通过
- 驳回后可重提，`submit_version + 1`
- 身份证号仅存 `hash + masked`，不落明文
- Admin 审核动作必须写入 `admin_audit_logs`

## 10. 已知说明

- 文件存储当前为本地磁盘（`app.storage.local-root`），MinIO 容器已预置。
- 当前仓库前端仍以 `web-user` 静态页面为主；Phase 2 前端页面与独立 Admin Console 尚未在本仓库落地。
- 领养/喂养/救助业务流程仍未开始开发。
