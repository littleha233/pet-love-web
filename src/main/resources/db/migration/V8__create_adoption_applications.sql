CREATE TABLE IF NOT EXISTS adoption_applications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id BIGINT NOT NULL,
    applicant_user_id BIGINT NOT NULL,
    message TEXT NOT NULL,
    living_env_note TEXT,
    pet_experience_note TEXT,
    status VARCHAR(32) NOT NULL,
    handled_by_user_id BIGINT,
    handled_at DATETIME(3),
    decision_note VARCHAR(255),
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    CONSTRAINT uk_post_applicant UNIQUE (post_id, applicant_user_id),
    CONSTRAINT fk_adoption_applications_post_id FOREIGN KEY (post_id) REFERENCES adoption_posts(id),
    CONSTRAINT fk_adoption_applications_applicant_user_id FOREIGN KEY (applicant_user_id) REFERENCES users(id),
    CONSTRAINT fk_adoption_applications_handled_by_user_id FOREIGN KEY (handled_by_user_id) REFERENCES users(id)
);

CREATE INDEX idx_adoption_applications_post_status ON adoption_applications(post_id, status);
CREATE INDEX idx_adoption_applications_applicant_status ON adoption_applications(applicant_user_id, status);
