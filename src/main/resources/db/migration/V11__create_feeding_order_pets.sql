CREATE TABLE IF NOT EXISTS feeding_order_pets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    pet_id BIGINT NOT NULL,
    pet_name_snapshot VARCHAR(64),
    pet_type_snapshot VARCHAR(16) NOT NULL,
    pet_gender_snapshot VARCHAR(16),
    age_months_snapshot INT,
    breed_snapshot VARCHAR(128),
    special_care_note_snapshot TEXT,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    CONSTRAINT uk_feeding_order_pets_order_pet UNIQUE (order_id, pet_id),
    CONSTRAINT fk_feeding_order_pets_order_id FOREIGN KEY (order_id) REFERENCES feeding_orders(id),
    CONSTRAINT fk_feeding_order_pets_pet_id FOREIGN KEY (pet_id) REFERENCES pets(id)
);

CREATE INDEX idx_feeding_order_pets_order_id
    ON feeding_order_pets(order_id);

CREATE INDEX idx_feeding_order_pets_pet_id
    ON feeding_order_pets(pet_id);
