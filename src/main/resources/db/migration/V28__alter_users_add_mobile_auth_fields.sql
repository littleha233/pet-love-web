SET @add_mobile_verified_at := (
    SELECT IF(
        EXISTS(
            SELECT 1 FROM information_schema.columns
            WHERE table_schema = DATABASE()
              AND table_name = 'users'
              AND column_name = 'mobile_verified_at'
        ),
        'SELECT 1',
        'ALTER TABLE users ADD COLUMN mobile_verified_at DATETIME(3) NULL COMMENT ''手机号验证通过时间'' AFTER mobile'
    )
);

PREPARE stmt_mobile_verified_at FROM @add_mobile_verified_at;
EXECUTE stmt_mobile_verified_at;
DEALLOCATE PREPARE stmt_mobile_verified_at;

SET @add_register_channel := (
    SELECT IF(
        EXISTS(
            SELECT 1 FROM information_schema.columns
            WHERE table_schema = DATABASE()
              AND table_name = 'users'
              AND column_name = 'register_channel'
        ),
        'SELECT 1',
        'ALTER TABLE users ADD COLUMN register_channel VARCHAR(32) NULL COMMENT ''注册渠道：MOBILE_SMS/EMAIL_OTP/...'' AFTER mobile_verified_at'
    )
);

PREPARE stmt_register_channel FROM @add_register_channel;
EXECUTE stmt_register_channel;
DEALLOCATE PREPARE stmt_register_channel;
