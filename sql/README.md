# VSaaS 数据库初始化

PostgreSQL 执行顺序：

```bash
psql -U vsaas -d vsaas -f sql/01-schema-core.sql
psql -U vsaas -d vsaas -f sql/02-schema-billing-rbac.sql
psql -U vsaas -d vsaas -f sql/seed-demo.sql
```

演示账号：`admin` / `admin123`（tenant_id=1）
