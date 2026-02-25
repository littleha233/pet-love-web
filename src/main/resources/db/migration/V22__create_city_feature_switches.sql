CREATE TABLE IF NOT EXISTS city_feature_switches (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    city_code VARCHAR(32) NOT NULL,
    city_name VARCHAR(64) NOT NULL,
    feature_key VARCHAR(64) NOT NULL,
    is_enabled TINYINT(1) NOT NULL DEFAULT 1,
    allow_read TINYINT(1) NOT NULL DEFAULT 1,
    allow_write TINYINT(1) NOT NULL DEFAULT 1,
    notice_text VARCHAR(255),
    effective_from DATETIME(3),
    effective_to DATETIME(3),
    updated_by_admin_id BIGINT,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    CONSTRAINT uk_city_feature_switch UNIQUE (city_code, feature_key),
    CONSTRAINT fk_city_feature_switch_updated_by_admin_id FOREIGN KEY (updated_by_admin_id) REFERENCES admin_users(id)
);

CREATE INDEX idx_city_feature_switch_feature
    ON city_feature_switches(feature_key, is_enabled);
