# VSaaS 视频监控云平台

Java 17 + Spring Boot 3.x + SRS + APISIX + PostgreSQL + MinIO + Vue3

## 设计文档

| 文档 | 路径 |
|------|------|
| 系统架构 | [docs/01-系统架构设计.md](docs/01-系统架构设计.md) |
| 方案设计 | [docs/02-方案设计.md](docs/02-方案设计.md) |
| 数据库 | [docs/03-数据库设计.md](docs/03-数据库设计.md) |
| 接口 | [docs/04-接口设计.md](docs/04-接口设计.md) |
| 部署运维 | [docs/05-部署运维手册.md](docs/05-部署运维手册.md) |
| 安全 | [docs/06-安全设计.md](docs/06-安全设计.md) |

## 模块端口

| 模块 | 端口 | 能力 |
|------|------|------|
| auth | 8001 | 登录 JWT |
| device | 8002 | 设备/通道/心跳/GB28181 |
| media | 8003 | FLV/HLS/预览 |
| tenant | 8004 | 租户 |
| record | 8005 | 录像 + MinIO |
| alarm | 8006 | 告警 + WebSocket |
| billing | 8007 | 计费引擎 |
| user | 8008 | RBAC |
| frontend | 3000 | 管理后台 |

## 快速启动

```bash
# 基础设施
cd docker && docker compose up -d

# 库表
psql -U vsaas -d vsaas -f sql/01-schema-core.sql
psql -U vsaas -d vsaas -f sql/02-schema-billing-rbac.sql
psql -U vsaas -d vsaas -f sql/seed-demo.sql

# 后端（各模块）
mvn -pl vsaas-auth -am spring-boot:run
# ... device media record alarm billing user tenant

# 前端
cd frontend && npm i && npm run dev
```

演示账号：`admin` / `admin123`
