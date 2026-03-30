CREATE TABLE questions (
    question_id BIGSERIAL PRIMARY KEY,
    lesson_id UUID NOT NULL, -- Chỉ những lesson có content_type = 'quiz' mới có câu hỏi ở đây
    question_text TEXT NOT NULL,
    points INT DEFAULT 10,

    CONSTRAINT fk_question_lesson FOREIGN KEY (lesson_id) REFERENCES lesson(lesson_id) ON DELETE CASCADE
);

CREATE TABLE question_options (
    option_id BIGSERIAL PRIMARY KEY,
    question_id BIGINT NOT NULL,
    option_text TEXT NOT NULL,
    is_correct BOOLEAN DEFAULT false, 

    CONSTRAINT fk_option_question FOREIGN KEY (question_id) REFERENCES questions(question_id) ON DELETE CASCADE
);
