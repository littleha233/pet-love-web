CREATE TABLE IF NOT EXISTS complaint_tickets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ticket_no VARCHAR(32) NOT NULL UNIQUE,
    reporter_user_id BIGINT NOT NULL,
    target_type VARCHAR(32) NOT NULL,
    target_id BIGINT,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    evidence_file_ids JSON,
    contact_mobile VARCHAR(32),
    contact_mobile_masked VARCHAR(32),
    priority VARCHAR(16) NOT NULL,
    status VARCHAR(32) NOT NULL,
    assigned_admin_id BIGINT,
    triage_note VARCHAR(255),
    resolution_note VARCHAR(255),
    last_reply_at DATETIME(3),
    handled_at DATETIME(3),
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    CONSTRAINT fk_complaint_tickets_reporter_user_id FOREIGN KEY (reporter_user_id) REFERENCES users(id),
    CONSTRAINT fk_complaint_tickets_assigned_admin_id FOREIGN KEY (assigned_admin_id) REFERENCES admin_users(id)
);

CREATE INDEX idx_complaint_tickets_reporter_status
    ON complaint_tickets(reporter_user_id, status);

CREATE INDEX idx_complaint_tickets_target
    ON complaint_tickets(target_type, target_id);

CREATE INDEX idx_complaint_tickets_status_priority
    ON complaint_tickets(status, priority);

CREATE INDEX idx_complaint_tickets_last_reply_at
    ON complaint_tickets(last_reply_at);

CREATE INDEX idx_complaint_tickets_created_at
    ON complaint_tickets(created_at);
