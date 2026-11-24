-- ========== 演示闭环数据 ==========
INSERT INTO device_info (id, tenant_id, device_code, device_name, protocol, ip, port, status, created_at, updated_at)
VALUES (100, 1, 'demo-cam-001', '演示摄像头-大厅', 'RTSP', '192.168.1.100', 554, 1, NOW(), NOW())
ON CONFLICT DO NOTHING;

INSERT INTO device_channel (id, tenant_id, device_id, channel_code, channel_name, stream_url, status, created_at, updated_at)
VALUES 
(1001, 1, 100, 'ch1', '大厅主码流', 'rtsp://192.168.1.100:554/stream1', 1, NOW(), NOW()),
(1002, 1, 100, 'ch2', '大厅子码流', 'rtsp://192.168.1.100:554/stream2', 1, NOW(), NOW())
ON CONFLICT DO NOTHING;

INSERT INTO record_plan (tenant_id, channel_id, plan_name, record_type, retain_days, status)
VALUES (1, 1001, '大厅全天录像', 1, 7, 1)
ON CONFLICT DO NOTHING;

INSERT INTO record_file (tenant_id, channel_id, start_time, end_time, file_path, file_size)
VALUES 
(1, 1001, NOW() - INTERVAL '2 hour', NOW() - INTERVAL '1 hour', '1/1001/demo_seg1.mp4', 104857600),
(1, 1001, NOW() - INTERVAL '1 hour', NOW() - INTERVAL '30 minute', '1/1001/demo_seg2.mp4', 52428800)
ON CONFLICT DO NOTHING;

INSERT INTO alarm_record (tenant_id, channel_id, alarm_type, alarm_content, alarm_time, status)
VALUES (1, 1001, 'motion', '大厅检测到移动', NOW() - INTERVAL '10 minute', 0)
ON CONFLICT DO NOTHING;

INSERT INTO billing_usage (tenant_id, usage_type, usage_value, usage_time, biz_id)
VALUES 
(1, 'channel', 2, NOW(), 'demo'),
(1, 'storage', 10.5, NOW(), 'demo')
ON CONFLICT DO NOTHING;

INSERT INTO sys_org (tenant_id, parent_id, org_name, org_code, sort, status)
VALUES (1, 0, '总部', 'HQ', 1, 1)
ON CONFLICT DO NOTHING;

INSERT INTO sys_role (tenant_id, role_code, role_name, remark, status)
VALUES (1, 'admin', '管理员', '演示管理员觙色', 1)
ON CONFLICT DO NOTHING;

CREATE TABLE IF NOT EXISTS sys_audit_log (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       BIGINT,
    user_id         BIGINT,
    action          VARCHAR(128),
    resource        VARCHAR(128),
    detail          TEXT,
    ip              VARCHAR(64),
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS shedlock (
    name VARCHAR(64) NOT NULL PRIMARY KEY,
    lock_until TIMESTAMP NOT NULL,
    locked_at TIMESTAMP NOT NULL,
    locked_by VARCHAR(255) NOT NULL
);

INSERT INTO billing_tenant_package (tenant_id, package_id, start_time, status)
SELECT 1, id, NOW(), 1 FROM billing_package WHERE package_code='basic' LIMIT 1
ON CONFLICT DO NOTHING;
