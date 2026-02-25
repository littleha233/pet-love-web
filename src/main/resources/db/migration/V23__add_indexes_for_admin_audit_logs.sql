CREATE INDEX idx_admin_audit_action_created
    ON admin_audit_logs(action, created_at);

CREATE INDEX idx_admin_audit_target
    ON admin_audit_logs(target_type, target_id);

CREATE INDEX idx_admin_audit_created
    ON admin_audit_logs(created_at);
