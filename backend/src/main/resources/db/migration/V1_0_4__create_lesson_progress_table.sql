CREATE TABLE lesson_progress (
    progress_id BIGSERIAL PRIMARY KEY,
    user_id UUID NOT NULL,
    lesson_id UUID NOT NULL,
    is_completed BOOLEAN DEFAULT false,
    completed_at TIMESTAMP,

    CONSTRAINT uq_user_lesson UNIQUE (user_id, lesson_id),
    CONSTRAINT fk_progress_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_progress_lesson FOREIGN KEY (lesson_id) REFERENCES lesson(lesson_id) ON DELETE CASCADE
);
