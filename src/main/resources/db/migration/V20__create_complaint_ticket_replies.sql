CREATE TABLE IF NOT EXISTS complaint_ticket_replies (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ticket_id BIGINT NOT NULL,
    author_type VARCHAR(16) NOT NULL,
    author_user_id BIGINT,
    author_admin_id BIGINT,
    content TEXT NOT NULL,
    is_internal_note TINYINT(1) NOT NULL DEFAULT 0,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    CONSTRAINT fk_complaint_ticket_replies_ticket_id FOREIGN KEY (ticket_id) REFERENCES complaint_tickets(id),
    CONSTRAINT fk_complaint_ticket_replies_author_user_id FOREIGN KEY (author_user_id) REFERENCES users(id),
    CONSTRAINT fk_complaint_ticket_replies_author_admin_id FOREIGN KEY (author_admin_id) REFERENCES admin_users(id)
);

CREATE INDEX idx_complaint_ticket_replies_ticket_id
    ON complaint_ticket_replies(ticket_id, created_at);
