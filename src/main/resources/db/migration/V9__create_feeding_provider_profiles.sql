CREATE TABLE IF NOT EXISTS feeding_provider_profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    provider_user_id BIGINT NOT NULL UNIQUE,
    status VARCHAR(32) NOT NULL,
    display_name VARCHAR(64),
    headline VARCHAR(128),
    intro TEXT,
    service_city_code VARCHAR(32) NOT NULL,
    service_city_name VARCHAR(64) NOT NULL,
    service_districts JSON,
    service_pet_types JSON NOT NULL,
    service_item_tags JSON NOT NULL,
    base_price_per_visit DECIMAL(10,2),
    experience_years INT,
    max_orders_per_day INT,
    accept_notes VARCHAR(255),
    rating_avg DECIMAL(3,2) NOT NULL DEFAULT 0.00,
    rating_count INT NOT NULL DEFAULT 0,
    completed_order_count INT NOT NULL DEFAULT 0,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    CONSTRAINT fk_feeding_provider_profiles_provider_user_id FOREIGN KEY (provider_user_id) REFERENCES users(id)
);

CREATE INDEX idx_feeding_provider_profiles_city_status
    ON feeding_provider_profiles(service_city_code, status);

CREATE INDEX idx_feeding_provider_profiles_updated_at
    ON feeding_provider_profiles(updated_at);
