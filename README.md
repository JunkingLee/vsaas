# VSaaS 视频监控云平台

Java 21 + Spring Boot 3.3 + SRS + APISIX + PostgreSQL + MinIO + Vue3

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

## 环境要求

- **JDK 21**
- Maven 3.9+
- Docker（中间件）
- Node 18+（前端）

## 快速启动

```bash
bash scripts/start-dev.sh
mvn -pl vsaas-auth -am spring-boot:run
# ... 其他模块
cd frontend && npm i && npm run dev
```

演示账号：`admin` / `admin123`  
联调：[CLOSED_LOOP.md](CLOSED_LOOP.md)
