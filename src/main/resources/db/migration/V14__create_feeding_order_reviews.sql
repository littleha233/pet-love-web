CREATE TABLE IF NOT EXISTS feeding_order_reviews (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL UNIQUE,
    owner_user_id BIGINT NOT NULL,
    provider_user_id BIGINT NOT NULL,
    rating_overall INT NOT NULL,
    rating_timeliness INT,
    rating_cleanliness INT,
    rating_attitude INT,
    content VARCHAR(1000),
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    CONSTRAINT fk_feeding_order_reviews_order_id FOREIGN KEY (order_id) REFERENCES feeding_orders(id),
    CONSTRAINT fk_feeding_order_reviews_owner_user_id FOREIGN KEY (owner_user_id) REFERENCES users(id),
    CONSTRAINT fk_feeding_order_reviews_provider_user_id FOREIGN KEY (provider_user_id) REFERENCES users(id)
);

CREATE INDEX idx_feeding_order_reviews_provider
    ON feeding_order_reviews(provider_user_id, created_at);
