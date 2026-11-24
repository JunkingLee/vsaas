# 生产部署要点

1. 复制 env.production.example 为 .env，修改所有密钥
2. 首次执行 sql/init.sql
3. 开启 Hook 鉴权、修改 JWT/Play 密钥
4. 健康检查 /actuator/health
