CREATE TABLE IF NOT EXISTS sms_code_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    mobile VARCHAR(32) NOT NULL,
    biz_type VARCHAR(32) NOT NULL,
    code_hash VARCHAR(128) NOT NULL,
    status VARCHAR(16) NOT NULL,

    provider VARCHAR(32),
    provider_template_code VARCHAR(64),
    provider_message_id VARCHAR(128),

    expire_at DATETIME(3) NOT NULL,
    verified_at DATETIME(3),

    attempt_count INT NOT NULL DEFAULT 0,
    max_attempts INT NOT NULL DEFAULT 5,

    client_ip VARCHAR(64),
    device_id VARCHAR(128),

    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
);

CREATE INDEX idx_sms_code_mobile_biz_status_created
    ON sms_code_records (mobile, biz_type, status, created_at);
CREATE INDEX idx_sms_code_mobile_created
    ON sms_code_records (mobile, created_at);
CREATE INDEX idx_sms_code_ip_created
    ON sms_code_records (client_ip, created_at);
CREATE INDEX idx_sms_code_expire_at
    ON sms_code_records (expire_at);
CREATE INDEX idx_sms_code_status_created
    ON sms_code_records (status, created_at);
