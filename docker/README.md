# Docker 部署说明

## 方式 A（推荐）：本机编译打包 + Compose 只跑运行镜像

```bash
bash scripts/package-release.sh
docker compose -f docker/docker-compose.release.yml up -d --build
```

产物：`release/jars/*.jar`、`release/web/`

## 方式 B：仅中间件 + 本机 java -jar

```bash
cd docker && docker compose up -d
bash scripts/package-release.sh
java -jar release/jars/vsaas-auth.jar
```

## 方式 C：源码在容器内构建

```bash
docker compose -f docker/docker-compose.full.yml up -d --build
```

## 访问

| 入口 | 地址 |
|------|------|
| 前端 | http://localhost:3000 |
| API | http://localhost:8001～8008 |
| SRS | http://localhost:8080 |

演示账号：`admin` / `admin123`
