CREATE TABLE IF NOT EXISTS rescue_resources (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    resource_type VARCHAR(32) NOT NULL,
    name VARCHAR(200) NOT NULL,
    city_code VARCHAR(32) NOT NULL,
    city_name VARCHAR(64) NOT NULL,
    district_name VARCHAR(64),
    address VARCHAR(255),
    contact_phone VARCHAR(64),
    contact_wechat VARCHAR(64),
    contact_other VARCHAR(255),
    service_hours VARCHAR(128),
    service_scope VARCHAR(255),
    accept_pet_types JSON,
    capability_tags JSON,
    description TEXT,
    source_url VARCHAR(500),
    verified_at DATETIME(3),
    status VARCHAR(32) NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    created_by_admin_id BIGINT,
    updated_by_admin_id BIGINT,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    CONSTRAINT fk_rescue_resources_created_by_admin_id FOREIGN KEY (created_by_admin_id) REFERENCES admin_users(id),
    CONSTRAINT fk_rescue_resources_updated_by_admin_id FOREIGN KEY (updated_by_admin_id) REFERENCES admin_users(id)
);

CREATE INDEX idx_rescue_resources_city_type_status
    ON rescue_resources(city_code, resource_type, status);

CREATE INDEX idx_rescue_resources_status_sort
    ON rescue_resources(status, sort_order);

CREATE INDEX idx_rescue_resources_verified_at
    ON rescue_resources(verified_at);
