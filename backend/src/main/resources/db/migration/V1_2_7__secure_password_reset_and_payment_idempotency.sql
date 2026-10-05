CREATE TABLE password_reset_tokens (
    token_hash VARCHAR(64) PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    expires_at TIMESTAMP NOT NULL,
    used_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_password_reset_tokens_user
    ON password_reset_tokens(user_id, created_at DESC);

DELETE FROM transactions older
USING transactions newer
WHERE older.transaction_id < newer.transaction_id
  AND older.user_id = newer.user_id
  AND older.course_id = newer.course_id
  AND older.status = 'SUCCESS'
  AND newer.status = 'SUCCESS';

CREATE UNIQUE INDEX uq_successful_transaction_user_course
    ON transactions(user_id, course_id)
    WHERE status = 'SUCCESS';
