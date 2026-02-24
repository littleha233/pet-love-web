CREATE TABLE IF NOT EXISTS feeding_visit_media (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    visit_id BIGINT NOT NULL,
    file_object_id BIGINT NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    CONSTRAINT fk_feeding_visit_media_visit_id FOREIGN KEY (visit_id) REFERENCES feeding_order_visits(id),
    CONSTRAINT fk_feeding_visit_media_file_object_id FOREIGN KEY (file_object_id) REFERENCES file_objects(id)
);

CREATE INDEX idx_feeding_visit_media_visit_sort
    ON feeding_visit_media(visit_id, sort_order);
