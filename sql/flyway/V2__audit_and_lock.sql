CREATE TABLE IF NOT EXISTS sys_audit_log (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT,
    user_id BIGINT,
    action VARCHAR(128),
    resource VARCHAR(128),
    detail TEXT,
    ip VARCHAR(64),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_audit_tenant_time ON sys_audit_log(tenant_id, created_at);
CREATE TABLE IF NOT EXISTS shedlock (
    name VARCHAR(64) NOT NULL PRIMARY KEY,
    lock_until TIMESTAMP NOT NULL,
    locked_at TIMESTAMP NOT NULL,
    locked_by VARCHAR(255) NOT NULL
);
