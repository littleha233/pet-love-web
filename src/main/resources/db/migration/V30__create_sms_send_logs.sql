CREATE TABLE IF NOT EXISTS sms_send_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    mobile VARCHAR(32) NOT NULL,
    biz_type VARCHAR(32) NOT NULL,

    provider VARCHAR(32) NOT NULL,
    template_code VARCHAR(64),
    sign_name VARCHAR(64),

    request_payload JSON,
    response_payload JSON,
    send_status VARCHAR(16) NOT NULL,
    error_code VARCHAR(64),
    error_message VARCHAR(255),

    provider_message_id VARCHAR(128),
    client_ip VARCHAR(64),
    device_id VARCHAR(128),

    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
);

CREATE INDEX idx_sms_send_logs_mobile_created
    ON sms_send_logs (mobile, created_at);
CREATE INDEX idx_sms_send_logs_biz_created
    ON sms_send_logs (biz_type, created_at);
CREATE INDEX idx_sms_send_logs_status_created
    ON sms_send_logs (send_status, created_at);
CREATE INDEX idx_sms_send_logs_provider_created
    ON sms_send_logs (provider, created_at);
