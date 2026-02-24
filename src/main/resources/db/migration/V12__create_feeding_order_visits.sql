CREATE TABLE IF NOT EXISTS feeding_order_visits (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    visit_index INT NOT NULL,
    planned_start_at DATETIME(3) NOT NULL,
    planned_end_at DATETIME(3) NOT NULL,
    status VARCHAR(32) NOT NULL,
    actual_start_at DATETIME(3),
    actual_end_at DATETIME(3),
    food_done TINYINT(1) NOT NULL DEFAULT 0,
    water_done TINYINT(1) NOT NULL DEFAULT 0,
    litter_done TINYINT(1) NOT NULL DEFAULT 0,
    play_done TINYINT(1) NOT NULL DEFAULT 0,
    health_observation VARCHAR(255),
    visit_note TEXT,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    CONSTRAINT uk_feeding_order_visits_order_index UNIQUE (order_id, visit_index),
    CONSTRAINT fk_feeding_order_visits_order_id FOREIGN KEY (order_id) REFERENCES feeding_orders(id)
);

CREATE INDEX idx_feeding_order_visits_order_status
    ON feeding_order_visits(order_id, status);

CREATE INDEX idx_feeding_order_visits_planned_start
    ON feeding_order_visits(planned_start_at);
