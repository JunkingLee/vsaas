# VSaaS 视频监控云平台

Java 17 + Spring Boot 3.3 + SRS + APISIX + PostgreSQL + MinIO + Vue3

## 模块

| 模块 | 端口 | 能力 |
|------|------|------|
| auth | 8001 | 登录 JWT / refresh |
| device | 8002 | 设备/通道/心跳/GB28181/WVP |
| media | 8003 | FLV/HLS/WebRTC |
| tenant | 8004 | 租户 |
| record | 8005 | 录像 + MinIO + SRS Hook |
| alarm | 8006 | 告警 + WebSocket |
| billing | 8007 | 计费/欠费/支付 |
| user | 8008 | RBAC + 组织 |
| frontend | 3000 | Vue3 管理后台 |

## 启动

```bash
cd docker && docker compose up -d
mvn clean install -DskipTests
cd frontend && npm install && npm run dev
```

详见 CLOSED_LOOP.md、docker/prod/

仓库：https://github.com/JunkingLee/vsaas.git
