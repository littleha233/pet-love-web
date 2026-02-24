CREATE TABLE IF NOT EXISTS pets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    owner_user_id BIGINT NOT NULL,
    pet_type VARCHAR(16) NOT NULL,
    name VARCHAR(64),
    gender VARCHAR(16),
    age_months INT,
    breed VARCHAR(128),
    weight_kg DECIMAL(5,2),
    neutered_status VARCHAR(16),
    vaccinated_status VARCHAR(16),
    health_note TEXT,
    temperament_tags JSON,
    special_care_note TEXT,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    CONSTRAINT fk_pets_owner_user_id FOREIGN KEY (owner_user_id) REFERENCES users(id)
);

CREATE INDEX idx_pets_owner_user_id ON pets(owner_user_id);
CREATE INDEX idx_pets_pet_type ON pets(pet_type);

CREATE TABLE IF NOT EXISTS pet_media (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    pet_id BIGINT NOT NULL,
    file_object_id BIGINT NOT NULL,
    media_type VARCHAR(16) NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    CONSTRAINT fk_pet_media_pet_id FOREIGN KEY (pet_id) REFERENCES pets(id),
    CONSTRAINT fk_pet_media_file_object_id FOREIGN KEY (file_object_id) REFERENCES file_objects(id)
);

CREATE INDEX idx_pet_media_pet_id_sort ON pet_media(pet_id, sort_order);
