CREATE TABLE IF NOT EXISTS user_verifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    verification_type VARCHAR(32) NOT NULL,
    status VARCHAR(32) NOT NULL,
    submit_version INT NOT NULL DEFAULT 1,

    real_name VARCHAR(64),
    id_no_masked VARCHAR(64),
    id_no_hash VARCHAR(128),
    id_front_file_id BIGINT,
    id_back_file_id BIGINT,
    holding_id_file_id BIGINT,

    provider_experience_years INT,
    provider_intro TEXT,
    provider_service_pet_types JSON,
    provider_service_city_code VARCHAR(32),
    provider_capability_tags JSON,
    supporting_file_ids JSON,

    reject_reason_code VARCHAR(64),
    reject_reason_text VARCHAR(255),
    reviewed_by_admin_id BIGINT,
    reviewed_at DATETIME(3),

    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),

    CONSTRAINT uk_user_verifications_user_type UNIQUE (user_id, verification_type),
    CONSTRAINT fk_user_verifications_user_id FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_user_verifications_id_front_file_id FOREIGN KEY (id_front_file_id) REFERENCES file_objects(id),
    CONSTRAINT fk_user_verifications_id_back_file_id FOREIGN KEY (id_back_file_id) REFERENCES file_objects(id),
    CONSTRAINT fk_user_verifications_holding_id_file_id FOREIGN KEY (holding_id_file_id) REFERENCES file_objects(id),
    CONSTRAINT fk_user_verifications_reviewed_by_admin_id FOREIGN KEY (reviewed_by_admin_id) REFERENCES admin_users(id)
);

CREATE INDEX idx_user_verifications_status_type ON user_verifications(status, verification_type);
CREATE INDEX idx_user_verifications_user_id ON user_verifications(user_id);
CREATE INDEX idx_user_verifications_updated_at ON user_verifications(updated_at);
