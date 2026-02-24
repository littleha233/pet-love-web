CREATE INDEX IF NOT EXISTS idx_users_status ON users(status);
CREATE INDEX IF NOT EXISTS idx_user_profiles_city_code ON user_profiles(city_code);
CREATE INDEX IF NOT EXISTS idx_auth_otp_target_hash ON auth_otp_codes(target_hash, purpose, status);
CREATE INDEX IF NOT EXISTS idx_auth_refresh_token_status ON auth_refresh_tokens(status, expires_at);
CREATE INDEX IF NOT EXISTS idx_file_objects_owner ON file_objects(owner_user_id, status);
CREATE INDEX IF NOT EXISTS idx_admin_users_role_status ON admin_users(role, status);
CREATE INDEX IF NOT EXISTS idx_admin_audit_admin_created ON admin_audit_logs(admin_user_id, created_at);
CREATE INDEX IF NOT EXISTS idx_cities_enabled ON cities(is_enabled);
