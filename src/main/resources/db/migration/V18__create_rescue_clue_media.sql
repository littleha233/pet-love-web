CREATE TABLE IF NOT EXISTS rescue_clue_media (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    clue_id BIGINT NOT NULL,
    file_object_id BIGINT NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    CONSTRAINT fk_rescue_clue_media_clue_id FOREIGN KEY (clue_id) REFERENCES rescue_clues(id),
    CONSTRAINT fk_rescue_clue_media_file_object_id FOREIGN KEY (file_object_id) REFERENCES file_objects(id)
);

CREATE INDEX idx_rescue_clue_media_clue_sort
    ON rescue_clue_media(clue_id, sort_order);
