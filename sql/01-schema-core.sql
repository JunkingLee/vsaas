-- VSaaS 初始化脚本 (PostgreSQL)
-- 创建数据库后执行

CREATE TABLE IF NOT EXISTS sys_tenant (
    id              BIGSERIAL PRIMARY KEY,
    tenant_code     VARCHAR(64) NOT NULL UNIQUE,
    tenant_name     VARCHAR(128) NOT NULL,
    status          SMALLINT DEFAULT 1,
    package_id      BIGINT,
    expire_time     TIMESTAMP,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS sys_user (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       BIGINT NOT NULL,
    username        VARCHAR(64) NOT NULL,
    password        VARCHAR(128) NOT NULL,
    real_name       VARCHAR(64),
    phone           VARCHAR(20),
    email           VARCHAR(128),
    status          SMALLINT DEFAULT 1,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(tenant_id, username)
);
CREATE INDEX IF NOT EXISTS idx_user_tenant ON sys_user(tenant_id);

CREATE TABLE IF NOT EXISTS device_info (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       BIGINT NOT NULL,
    device_code     VARCHAR(64) NOT NULL,
    device_name     VARCHAR(128),
    protocol        VARCHAR(32) DEFAULT 'RTSP',
    ip              VARCHAR(64),
    port            INT,
    username        VARCHAR(64),
    password        VARCHAR(128),
    status          SMALLINT DEFAULT 0,
    last_online_at  TIMESTAMP,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(tenant_id, device_code)
);
CREATE INDEX IF NOT EXISTS idx_device_tenant ON device_info(tenant_id);

CREATE TABLE IF NOT EXISTS device_channel (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       BIGINT NOT NULL,
    device_id       BIGINT NOT NULL,
    channel_code    VARCHAR(64) NOT NULL,
    channel_name    VARCHAR(128),
    stream_url      VARCHAR(512),
    status          SMALLINT DEFAULT 0,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_channel_device ON device_channel(device_id);

CREATE TABLE IF NOT EXISTS record_plan (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       BIGINT NOT NULL,
    channel_id      BIGINT NOT NULL,
    plan_name       VARCHAR(128),
    record_type     SMALLINT DEFAULT 1,
    retain_days     INT DEFAULT 7,
    status          SMALLINT DEFAULT 1,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS record_file (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       BIGINT NOT NULL,
    channel_id      BIGINT NOT NULL,
    start_time      TIMESTAMP NOT NULL,
    end_time        TIMESTAMP NOT NULL,
    file_path       VARCHAR(512) NOT NULL,
    file_size       BIGINT DEFAULT 0,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_record_channel_time ON record_file(channel_id, start_time);

CREATE TABLE IF NOT EXISTS alarm_record (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       BIGINT NOT NULL,
    channel_id      BIGINT,
    alarm_type      VARCHAR(64) NOT NULL,
    alarm_content   VARCHAR(512),
    alarm_time      TIMESTAMP NOT NULL,
    status          SMALLINT DEFAULT 0,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS billing_package (
    id              BIGSERIAL PRIMARY KEY,
    package_code    VARCHAR(64) NOT NULL UNIQUE,
    package_name    VARCHAR(128) NOT NULL,
    price           DECIMAL(12,2) NOT NULL,
    billing_type    SMALLINT NOT NULL,
    max_channels    INT,
    max_storage_gb  INT,
    retain_days     INT,
    status          SMALLINT DEFAULT 1,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS billing_tenant_package (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       BIGINT NOT NULL,
    package_id      BIGINT NOT NULL,
    start_time      TIMESTAMP NOT NULL,
    end_time        TIMESTAMP,
    status          SMALLINT DEFAULT 1,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
