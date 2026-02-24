# PetLove Runbook

## 当前项目进展

- 后端：已完成 `dev-phase-1.md` 的 Phase 0 基础设施。
- 前端：已完成 `web-user` 静态页面（首页、领养列表、服务页），目前使用 mock 数据。
- 业务状态：领养/喂养/救助业务接口尚未开始开发，当前重点是底座能力可运行。

后端已可用能力：

- 用户 OTP 登录、Token 刷新、登出、当前用户信息
- 用户资料查询与更新
- 文件上传与文件访问
- 城市元数据、健康检查
- Admin 登录、用户状态管理、审计日志

## 启动顺序（推荐）

推荐顺序：

1. 启动基础依赖（PostgreSQL、MinIO）
2. 启动后端（Spring Boot）
3. 启动前端（web-user）

如果只是快速体验接口，也可以跳过第 1 步，后端默认会用 H2 内存库启动。

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

- PostgreSQL: `localhost:5432`
- MinIO API: `localhost:9000`
- MinIO Console: `http://localhost:9001`

## 启动后端

### 方式 A：完整本地联调（推荐）

使用 PostgreSQL：

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

### 方式 B：快速启动（不依赖 Docker）

使用 H2 内存数据库：

```bash
./mvnw spring-boot:run
```

启动后访问：

- Swagger: `http://localhost:8080/swagger-ui/index.html`
- 健康检查: `http://localhost:8080/api/v1/system/health`

## 启动前端

新开一个终端窗口：

```bash
cd web-user
npm install
npm run dev
```

前端默认地址：

- `http://localhost:5173`

说明：当前前端页面是静态 mock，不依赖后端接口即可浏览。

## 当前可用账号

后端启动时会自动初始化默认管理员账号：

- username: `admin`
- password: `Admin@123456`

## 快速验证后端接口

在项目根目录或任意终端执行：

```bash
curl -s http://localhost:8080/api/v1/system/health
```

```bash
curl -s http://localhost:8080/api/v1/meta/cities
```

发送 OTP（dev 环境会返回 `mockCode`）：

```bash
curl -s -X POST http://localhost:8080/api/v1/auth/otp/send \
  -H 'Content-Type: application/json' \
  -d '{"channel":"MOBILE","target":"13800138000","purpose":"LOGIN"}'
```

## 停止服务

停止前端：

- 在前端终端按 `Ctrl+C`

停止后端：

- 在后端终端按 `Ctrl+C`

停止 Docker 依赖：

```bash
docker compose down
```

## 常见问题

1. `Port 8080 was already in use`

- 说明端口被占用。
- 处理：停掉占用进程，或改 `server.port`。

2. 前端 `npm install` 卡住

- 重试：`npm install --no-audit --no-fund`

3. Swagger 能打开但接口 401

- 受保护接口需要 Bearer Token。
- 先调用 OTP 登录拿 `accessToken`，再带 `Authorization: Bearer <token>`。

4. 数据库连接失败（local profile）

- 确认 `docker compose ps` 中 postgres 是 healthy。
- 检查 `.env` 中 `DB_*` 是否与 `application-local.yml` 一致。
