ALTER TABLE user_profiles
    ADD COLUMN is_real_name_verified TINYINT(1) NOT NULL DEFAULT 0;

ALTER TABLE user_profiles
    ADD COLUMN is_provider_verified TINYINT(1) NOT NULL DEFAULT 0;

CREATE INDEX idx_user_profiles_verify_flags ON user_profiles(is_real_name_verified, is_provider_verified);
