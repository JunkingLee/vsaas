# Flyway 使用说明

1. **首次环境**：执行根目录 `sql/init.sql` 全量建库（含演示数据）。
2. **增量**：将变更放在 `V{n}__xxx.sql`，auth 服务设置 `FLYWAY_ENABLED=true` 自动迁移。
3. 已有库启用 Flyway 时请 `baseline-on-migrate=true`（已默认）。
4. `V2__audit_and_lock.sql`：审计表 + shedlock。
5. 后续版本示例：`V3__xxx.sql` 只写增量 DDL/DML，勿重复建表。
