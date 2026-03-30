CREATE TABLE quiz_attempts (
    attempt_id BIGSERIAL PRIMARY KEY,
    user_id UUID NOT NULL,
    lesson_id UUID NOT NULL,
    score FLOAT NOT NULL,
    is_passed BOOLEAN NOT NULL,
    attempted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_attempt_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_attempt_lesson FOREIGN KEY (lesson_id) REFERENCES lesson(lesson_id) ON DELETE CASCADE
);
