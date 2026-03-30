CREATE TABLE enrollments (
    enrollment_id BIGSERIAL PRIMARY KEY,
    user_id UUID NOT NULL,
    course_id UUID NOT NULL,
    progress_percent FLOAT DEFAULT 0.0,      -- % hoàn thành khóa học (mặc định là 0)
    ai_dropout_risk_score FLOAT,
    enrolled_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_user_course UNIQUE (user_id, course_id),
    CONSTRAINT fk_enrollment_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_enrollment_course FOREIGN KEY (course_id) REFERENCES courses(course_id) ON DELETE CASCADE
);
