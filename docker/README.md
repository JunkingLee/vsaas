# Docker 部署说明

## 1. 仅中间件（本机跑 Java）

```bash
cd docker
docker compose up -d
```

## 2. 全栈一键（演示 / 一体机）

```bash
# 仓库根目录
docker compose -f docker/docker-compose.full.yml up -d --build
```

| 入口 | 地址 |
|------|------|
| 管理前端 | http://localhost:3000 |
| auth | http://localhost:8001 |
| SRS | http://localhost:8080 |
| MinIO | http://localhost:9001 |
| RabbitMQ | http://localhost:15672 |

演示账号：`admin` / `admin123`

内存建议 ≥ 8GB；首次 `--build` 较慢。

## 3. 单服务镜像

```bash
docker build -f docker/Dockerfile.app --build-arg MODULE=vsaas-auth -t vsaas-auth:1.0 .
```
