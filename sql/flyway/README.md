# Flyway

1. First deploy: run sql/init.sql
2. Incremental: V{n}__xxx.sql with FLYWAY_ENABLED=true on auth
3. V2: audit + shedlock tables
