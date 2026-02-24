CREATE TABLE IF NOT EXISTS adoption_posts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    publisher_user_id BIGINT NOT NULL,
    pet_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    city_code VARCHAR(32) NOT NULL,
    city_name VARCHAR(64) NOT NULL,
    district_name VARCHAR(64),
    status VARCHAR(32) NOT NULL,
    submit_version INT NOT NULL DEFAULT 1,
    reject_reason_code VARCHAR(64),
    reject_reason_text VARCHAR(255),
    reviewed_by_admin_id BIGINT,
    reviewed_at DATETIME(3),
    published_at DATETIME(3),
    closed_at DATETIME(3),
    view_count INT NOT NULL DEFAULT 0,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    CONSTRAINT fk_adoption_posts_publisher_user_id FOREIGN KEY (publisher_user_id) REFERENCES users(id),
    CONSTRAINT fk_adoption_posts_pet_id FOREIGN KEY (pet_id) REFERENCES pets(id),
    CONSTRAINT fk_adoption_posts_reviewed_by_admin_id FOREIGN KEY (reviewed_by_admin_id) REFERENCES admin_users(id)
);

CREATE INDEX idx_adoption_posts_city_status ON adoption_posts(city_code, status);
CREATE INDEX idx_adoption_posts_publisher_status ON adoption_posts(publisher_user_id, status);
CREATE INDEX idx_adoption_posts_pet_id ON adoption_posts(pet_id);
CREATE INDEX idx_adoption_posts_updated_at ON adoption_posts(updated_at);
