CREATE TABLE IF NOT EXISTS feeding_orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_no VARCHAR(32) NOT NULL UNIQUE,
    owner_user_id BIGINT NOT NULL,
    provider_user_id BIGINT NOT NULL,
    provider_profile_id BIGINT NOT NULL,
    status VARCHAR(32) NOT NULL,
    service_city_code VARCHAR(32) NOT NULL,
    service_city_name VARCHAR(64) NOT NULL,
    service_district_name VARCHAR(64),
    service_address_detail VARCHAR(255) NOT NULL,
    service_address_note VARCHAR(255),
    contact_name VARCHAR(64) NOT NULL,
    contact_mobile VARCHAR(32) NOT NULL,
    contact_mobile_masked VARCHAR(32) NOT NULL,
    service_item_tags JSON NOT NULL,
    visit_count INT NOT NULL,
    owner_note TEXT,
    requested_total_amount DECIMAL(10,2),
    quoted_total_amount DECIMAL(10,2),
    currency VARCHAR(8) NOT NULL DEFAULT 'CNY',
    provider_response_note VARCHAR(255),
    cancel_reason VARCHAR(255),
    owner_confirmed_at DATETIME(3),
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    CONSTRAINT fk_feeding_orders_owner_user_id FOREIGN KEY (owner_user_id) REFERENCES users(id),
    CONSTRAINT fk_feeding_orders_provider_user_id FOREIGN KEY (provider_user_id) REFERENCES users(id),
    CONSTRAINT fk_feeding_orders_provider_profile_id FOREIGN KEY (provider_profile_id) REFERENCES feeding_provider_profiles(id)
);

CREATE INDEX idx_feeding_orders_owner_status
    ON feeding_orders(owner_user_id, status);

CREATE INDEX idx_feeding_orders_provider_status
    ON feeding_orders(provider_user_id, status);

CREATE INDEX idx_feeding_orders_status_created_at
    ON feeding_orders(status, created_at);

CREATE INDEX idx_feeding_orders_city_status
    ON feeding_orders(service_city_code, status);
