# PetLove Web - Phase 0 Backend

运行与联调步骤请看：[README-RUNBOOK.md](README-RUNBOOK.md)

## 1. 当前实现范围

本次已实现 `dev-phase-1.md` 的 Phase 0 后端基础能力（不含领养/喂养/救助业务）：

- 统一接口响应体与错误码
- requestId 日志链路
- JWT + Refresh Token Rotation（用户端 + Admin 端）
- OTP 登录（开发环境 mock OTP）
- 用户资料查询/更新
- 文件上传（本地存储）
- Admin 登录、菜单、用户管理、审计日志
- Flyway 数据库迁移（Phase 0 核心表）
- OpenAPI 文档（Swagger UI）

## 2. 模块结构（后端）

后端代码入口：`src/main/java/com/petlove/weblove`

- `common`：统一响应、异常、日志过滤器、工具
- `config`：配置属性、安全配置、OpenAPI
- `security`：JWT、鉴权过滤器、权限上下文
- `modules/auth`：OTP/登录/refresh/logout/me
- `modules/user`：profile + Admin 用户管理
- `modules/file`：上传、文件元信息、文件内容读取
- `modules/admin`：Admin 鉴权、菜单、审计日志
- `modules/system`：health/ready/cities、启动初始化

## 3. 数据库与迁移

Flyway 迁移脚本：`src/main/resources/db/migration`

- `V1__init_core_tables.sql`
- `V2__seed_base_data.sql`
- `V3__add_indexes.sql`

Phase 0 已落表：

- `users`
- `user_profiles`
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

默认启动：

- PostgreSQL: `localhost:5432`
- MinIO: `localhost:9000`（console `localhost:9001`）

### 4.2 启动后端

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

备注：

- 默认配置文件为 `application.yml`（H2 内存库，便于快速测试）
- `local` profile 使用 PostgreSQL（建议联调时使用）

## 5. 测试

```bash
./mvnw test
```

## 6. 默认账号

应用启动时会自动初始化一个超级管理员（若不存在）：

- username: `admin`
- password: `Admin@123456`

可在 `application.yml` 的 `app.bootstrap.*` 修改。

## 7. API 文档

启动后访问：

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`

## 8. 已实现接口（Phase 0）

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

### Admin

- `POST /api/admin/v1/auth/login`
- `POST /api/admin/v1/auth/refresh`
- `POST /api/admin/v1/auth/logout`
- `GET /api/admin/v1/auth/me`
- `GET /api/admin/v1/meta/menus`
- `GET /api/admin/v1/users`
- `PATCH /api/admin/v1/users/{userId}/status`
- `GET /api/admin/v1/audit-logs`

## 9. 安全与状态机说明

- 用户状态：`ACTIVE / DISABLED / BANNED`
- OTP 状态：`ISSUED / VERIFIED / USED / EXPIRED`
- Refresh Token 状态：`ACTIVE / ROTATED / REVOKED / EXPIRED`
- 文件状态：`UPLOADING / READY / FAILED / DELETED`

关键规则：

- refresh token 使用后立即 `ROTATED`
- 被禁用/封禁用户无法继续访问受保护接口
- 用户状态变更必须写入 `admin_audit_logs`

## 10. 已知说明

- 文件存储当前为本地磁盘（`app.storage.local-root`），MinIO 容器已预置，后续可无缝切换到对象存储实现。
- Phase 0 仅实现基础设施，不包含领养/喂养/救助业务流程。
