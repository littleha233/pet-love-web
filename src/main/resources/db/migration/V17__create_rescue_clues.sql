CREATE TABLE IF NOT EXISTS rescue_clues (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    clue_no VARCHAR(32) NOT NULL UNIQUE,
    reporter_user_id BIGINT NOT NULL,
    city_code VARCHAR(32) NOT NULL,
    city_name VARCHAR(64) NOT NULL,
    district_name VARCHAR(64),
    location_text VARCHAR(255) NOT NULL,
    geo_lat DECIMAL(10,7),
    geo_lng DECIMAL(10,7),
    pet_type VARCHAR(16),
    estimated_count INT,
    urgency_level VARCHAR(16) NOT NULL,
    condition_tags JSON,
    description TEXT NOT NULL,
    contact_name VARCHAR(64) NOT NULL,
    contact_mobile VARCHAR(32) NOT NULL,
    contact_mobile_masked VARCHAR(32) NOT NULL,
    status VARCHAR(32) NOT NULL,
    triage_note VARCHAR(255),
    resolution_note VARCHAR(255),
    handled_by_admin_id BIGINT,
    handled_at DATETIME(3),
    suggested_resource_ids JSON,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    CONSTRAINT fk_rescue_clues_reporter_user_id FOREIGN KEY (reporter_user_id) REFERENCES users(id),
    CONSTRAINT fk_rescue_clues_handled_by_admin_id FOREIGN KEY (handled_by_admin_id) REFERENCES admin_users(id)
);

CREATE INDEX idx_rescue_clues_reporter_status
    ON rescue_clues(reporter_user_id, status);

CREATE INDEX idx_rescue_clues_city_status
    ON rescue_clues(city_code, status);

CREATE INDEX idx_rescue_clues_urgency_status
    ON rescue_clues(urgency_level, status);

CREATE INDEX idx_rescue_clues_created_at
    ON rescue_clues(created_at);
