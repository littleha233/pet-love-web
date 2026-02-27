```text
请把当前项目的数据库从 PostgreSQL 全量迁移为 MySQL（目标版本：MySQL 8.4，兼容 8.0），并确保 Phase 0 + 第二阶段（认证模块）都能正常运行。

【目标】
1) 后端 Spring Boot 数据源改为 MySQL
2) Docker Compose 本地环境改为 MySQL
3) Flyway SQL 迁移脚本从 PostgreSQL 语法改为 MySQL 语法
4) 实体/Repository/原生 SQL 中所有 PostgreSQL 特有写法改为 MySQL
5) 不改 API 合约（接口路径、DTO、响应结构保持不变）
6) 完成后本地可启动、可迁移、可登录、可跑认证审核流程

========================
一、基础改造（必须）
========================

1. 修改后端依赖（services/api-server/pom.xml）
- 移除 PostgreSQL 驱动依赖
- 增加 MySQL 驱动（mysql-connector-j）
- 保留 Flyway（继续使用）

2. 修改 application 配置（application.yml / application-dev.yml / application-test.yml）
- spring.datasource.url 改为 MySQL JDBC URL
  示例：
  jdbc:mysql://localhost:3306/pet_platform?useUnicode=true&characterEncoding=utf8&serverTimezone=UTC&allowPublicKeyRetrieval=true&useSSL=false
- spring.datasource.driver-class-name = com.mysql.cj.jdbc.Driver
- spring.datasource.username / password 改为 MySQL
- 如果使用 JPA/Hibernate：
  - spring.jpa.database-platform = org.hibernate.dialect.MySQLDialect
  - spring.jpa.hibernate.ddl-auto = none（继续以 Flyway 为准）
- 明确时区使用 UTC（应用层和 JDBC 都统一 UTC）

3. 修改 docker-compose.yml
- 删除 postgres 服务
- 新增 mysql 服务（mysql:8.4）
- 配置：
  - MYSQL_DATABASE=pet_platform
  - MYSQL_USER=pet
  - MYSQL_PASSWORD=pet_dev_password
  - MYSQL_ROOT_PASSWORD=root_dev_password
- 增加 healthcheck（mysqladmin ping）
- 保留 MinIO / Redis（如已有）
- 更新 api-server 对 DB 的依赖与连接参数

========================
二、Flyway SQL 迁移脚本改造（重点）
========================

请逐个检查并改造 src/main/resources/db/migration/*.sql，确保 MySQL 语法可执行。

【统一改造规则】
1. PostgreSQL 主键自增
- bigserial / serial -> bigint auto_increment / int auto_increment

2. JSON 类型
- jsonb -> json
- 与 jsonb 相关的 PostgreSQL cast（如 ::jsonb）全部移除或改为 MySQL 合法写法

3. 布尔类型
- boolean -> tinyint(1)
- 默认值 true/false -> 1/0

4. 时间字段
- timestamp -> datetime(3)（建议统一）
- now() 可保留（MySQL 支持）
- 不要使用 PostgreSQL 特有时区函数

5. 索引与约束
- 检查所有 index/unique 语法是否兼容 MySQL
- PostgreSQL 特有的部分索引、表达式索引如有，先改成普通索引（保证可用优先）
- 保持唯一约束语义不变（例如 unique(user_id, verification_type)）

6. 字符串与文本
- varchar / text 基本可保留
- 所有表默认字符集设为 utf8mb4，排序规则 utf8mb4_0900_ai_ci（若 compose/init 未设置，则在建表语句中指定）

7. 外键
- 检查 FK 语法和命名是否兼容
- 引擎使用 InnoDB（确保外键可用）

8. SQL 兼容性检查
- 移除 PostgreSQL 专有语法：
  - RETURNING
  - ILIKE
  - ON CONFLICT ... DO UPDATE
  - ::type cast
  - true/false（用于 tinyint 时改 1/0）
- 替换建议：
  - ILIKE -> LIKE（必要时对比前先 LOWER()）
  - ON CONFLICT -> ON DUPLICATE KEY UPDATE（如确实有）
  - RETURNING -> 改为普通 INSERT/UPDATE，然后由应用层查询

【特别检查表（已存在于项目）】
- users
- user_profiles
- auth_otp_codes
- auth_refresh_tokens
- file_objects
- admin_users
- admin_audit_logs
- cities
- system_configs
- user_verifications（第二阶段新增）

========================
三、代码层改造（Repository / SQL / 实体）
========================

1. 检查所有 Repository / Mapper / Native SQL
- 搜索项目中是否有 PostgreSQL 专有语法：
  - ILIKE
  - ::jsonb / ::text
  - RETURNING
  - ON CONFLICT
  - jsonb_* 函数
- 全部替换为 MySQL 等价实现

2. 实体字段类型映射
- 之前映射到 jsonb 的字段（如 json/jsonb）保持为 JSON 字符串或对象映射（取决于现有实现）
- 确保 Hibernate/Jackson 对 MySQL JSON 字段读写正常
- boolean 字段在数据库层变成 tinyint(1) 后，Java 层仍保持 Boolean/boolean

3. 审计日志与配置表 JSON 字段
- admin_audit_logs.before_snapshot / after_snapshot
- system_configs.config_value
- user_profiles.role_flags（如果有）
- user_verifications.provider_service_pet_types / provider_capability_tags
上述字段保持 JSON 语义不变，只改数据库类型与 SQL 写法

========================
四、本地环境与脚本改造
========================

1. 更新 .env.example
- 增加 MySQL 连接信息（host/port/db/user/password）
- 删除 PostgreSQL 相关变量

2. 更新 scripts/dev-start.sh / seed-local.sh（如存在）
- 启动检查从 Postgres 改为 MySQL
- 等待 DB ready 的逻辑适配 mysqladmin ping

3. 更新 README / docs/phase0/local-setup.md
- 文档中的 PostgreSQL 安装/启动说明改为 MySQL
- 保留 MinIO 启动说明

========================
五、回归验证（必须执行）
========================

请完成以下验证，并在输出中给出结果（成功/失败 + 修复点）：

1. `docker compose up` 成功（MySQL + MinIO + API）
2. Flyway 自动迁移成功（所有 V1...Vn 执行完成）
3. 用户端 OTP 登录成功（Phase 0）
4. 文件上传成功（Phase 0）
5. Admin 登录成功（Phase 0）
6. 第二阶段认证流程可用：
   - 提交实名认证
   - Admin 审核通过/驳回
   - 前台状态回显正确
7. admin_audit_logs 正常写入
8. `/health` 和 `/ready` 正常

========================
六、输出要求（给我看的结果）
========================

完成后请输出：
1. 修改文件清单（按路径列出）
2. 关键改动摘要（依赖、配置、Flyway、SQL 兼容、Docker）
3. MySQL 版本与连接信息（dev）
4. 迁移脚本兼容性说明（列出做了哪些 PostgreSQL -> MySQL 替换）
5. 回归测试结果（逐项）
6. 若有未完成项，明确写出原因和建议修复方式

【注意】
- 不要改动 API 路径和 DTO 字段
- 不要引入新业务功能
- 优先保证现有功能可跑通，再做风格/重构
```

---

* **这次迁库只做“兼容迁移”，不要顺手改业务逻辑。**
* Codex 先完成“能跑通”，你再做一次人工验收（尤其是 Flyway 脚本和认证流程）。

备注：本机MySQL的账号是root，密码是Littleha233!