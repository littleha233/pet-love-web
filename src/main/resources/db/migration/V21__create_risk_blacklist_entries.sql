CREATE TABLE IF NOT EXISTS risk_blacklist_entries (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    subject_type VARCHAR(32) NOT NULL,
    subject_value VARCHAR(128) NOT NULL,
    scope_type VARCHAR(32) NOT NULL,
    scope_value VARCHAR(64) NOT NULL,
    action_mode VARCHAR(16) NOT NULL,
    reason_code VARCHAR(64) NOT NULL,
    reason_note VARCHAR(255),
    start_at DATETIME(3) NOT NULL,
    end_at DATETIME(3),
    status VARCHAR(16) NOT NULL,
    created_by_admin_id BIGINT,
    updated_by_admin_id BIGINT,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    CONSTRAINT fk_risk_blacklist_created_by_admin_id FOREIGN KEY (created_by_admin_id) REFERENCES admin_users(id),
    CONSTRAINT fk_risk_blacklist_updated_by_admin_id FOREIGN KEY (updated_by_admin_id) REFERENCES admin_users(id)
);

CREATE INDEX idx_risk_blacklist_subject
    ON risk_blacklist_entries(subject_type, subject_value);

CREATE INDEX idx_risk_blacklist_scope_status
    ON risk_blacklist_entries(scope_type, scope_value, status);

CREATE INDEX idx_risk_blacklist_active_time
    ON risk_blacklist_entries(status, start_at, end_at);
