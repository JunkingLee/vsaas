CREATE TABLE IF NOT EXISTS billing_usage (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       BIGINT NOT NULL,
    usage_type      VARCHAR(32) NOT NULL,
    usage_value     DECIMAL(16,4) NOT NULL,
    usage_time      TIMESTAMP NOT NULL,
    biz_id          VARCHAR(64),
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_usage_tenant_time ON billing_usage(tenant_id, usage_time);

CREATE TABLE IF NOT EXISTS billing_bill (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       BIGINT NOT NULL,
    bill_no         VARCHAR(64) NOT NULL UNIQUE,
    bill_month      VARCHAR(7) NOT NULL,
    total_amount    DECIMAL(12,2) NOT NULL,
    paid_amount     DECIMAL(12,2) DEFAULT 0,
    status          SMALLINT DEFAULT 0,
    start_time      TIMESTAMP,
    end_time        TIMESTAMP,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS billing_bill_item (
    id              BIGSERIAL PRIMARY KEY,
    bill_id         BIGINT NOT NULL,
    item_type       VARCHAR(32) NOT NULL,
    item_name       VARCHAR(128),
    quantity        DECIMAL(12,4),
    unit_price      DECIMAL(12,4),
    amount          DECIMAL(12,2) NOT NULL
);

INSERT INTO sys_tenant (id, tenant_code, tenant_name, status) VALUES (1, 'demo', '演示租户', 1)
ON CONFLICT DO NOTHING;

INSERT INTO sys_user (tenant_id, username, password, real_name, status)
VALUES (1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '超级管理员', 1)
ON CONFLICT DO NOTHING;

CREATE TABLE IF NOT EXISTS sys_role (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       BIGINT NOT NULL,
    role_code       VARCHAR(64) NOT NULL,
    role_name       VARCHAR(128) NOT NULL,
    remark          VARCHAR(256),
    status          SMALLINT DEFAULT 1,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(tenant_id, role_code)
);

CREATE TABLE IF NOT EXISTS sys_menu (
    id              BIGSERIAL PRIMARY KEY,
    parent_id       BIGINT DEFAULT 0,
    menu_name       VARCHAR(64) NOT NULL,
    path            VARCHAR(128),
    component       VARCHAR(128),
    perms           VARCHAR(128),
    menu_type       SMALLINT DEFAULT 2,
    sort            INT DEFAULT 0,
    status          SMALLINT DEFAULT 1,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS sys_user_role (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL,
    role_id         BIGINT NOT NULL
);

CREATE TABLE IF NOT EXISTS sys_role_menu (
    id              BIGSERIAL PRIMARY KEY,
    role_id         BIGINT NOT NULL,
    menu_id         BIGINT NOT NULL
);

INSERT INTO sys_menu (id, parent_id, menu_name, path, perms, menu_type, sort) VALUES
(1, 0, '系统管理', '/system', NULL, 1, 1),
(2, 1, '用户管理', '/system/user', 'user:list', 2, 1),
(3, 1, '觙色管理', '/system/role', 'role:list', 2, 2),
(4, 0, '设备管理', '/device', 'device:list', 1, 2),
(5, 0, '录像管理', '/record', 'record:list', 1, 3),
(6, 0, '告警中心', '/alarm', 'alarm:list', 1, 4),
(7, 0, '计费中心', '/billing', 'billing:list', 1, 5)
ON CONFLICT DO NOTHING;

INSERT INTO billing_package (package_code, package_name, price, billing_type, max_channels, max_storage_gb, retain_days, status)
VALUES 
('basic', '基础版', 99.00, 1, 10, 100, 7, 1),
('pro', '专业版', 299.00, 1, 50, 500, 30, 1),
('enterprise', '企业版', 999.00, 1, 200, 2000, 90, 1)
ON CONFLICT DO NOTHING;

CREATE TABLE IF NOT EXISTS sys_org (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       BIGINT NOT NULL,
    parent_id       BIGINT DEFAULT 0,
    org_name        VARCHAR(128) NOT NULL,
    org_code        VARCHAR(64),
    sort            INT DEFAULT 0,
    status          SMALLINT DEFAULT 1,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS sys_user_org (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL,
    org_id          BIGINT NOT NULL
);

CREATE TABLE IF NOT EXISTS device_org (
    id              BIGSERIAL PRIMARY KEY,
    device_id       BIGINT NOT NULL,
    org_id          BIGINT NOT NULL
);
