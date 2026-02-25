CREATE TABLE IF NOT EXISTS rescue_guides (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    scenario_code VARCHAR(64) NOT NULL,
    title VARCHAR(200) NOT NULL,
    summary VARCHAR(500),
    content_md MEDIUMTEXT NOT NULL,
    city_code VARCHAR(32),
    tags JSON,
    sort_order INT NOT NULL DEFAULT 0,
    status VARCHAR(32) NOT NULL,
    version INT NOT NULL DEFAULT 1,
    published_at DATETIME(3),
    created_by_admin_id BIGINT,
    updated_by_admin_id BIGINT,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    CONSTRAINT fk_rescue_guides_created_by_admin_id FOREIGN KEY (created_by_admin_id) REFERENCES admin_users(id),
    CONSTRAINT fk_rescue_guides_updated_by_admin_id FOREIGN KEY (updated_by_admin_id) REFERENCES admin_users(id)
);

CREATE INDEX idx_rescue_guides_status_city_sort
    ON rescue_guides(status, city_code, sort_order);

CREATE INDEX idx_rescue_guides_scenario_status
    ON rescue_guides(scenario_code, status);
