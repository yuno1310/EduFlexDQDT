CREATE TABLE daily_quests (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    description VARCHAR(255) NOT NULL,
    quest_type VARCHAR(50) NOT NULL,
    target_count INT NOT NULL,
    xp_reward INT NOT NULL
);

CREATE TABLE user_daily_quest_progress (
    id BIGSERIAL PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    quest_id BIGINT NOT NULL REFERENCES daily_quests(id) ON DELETE CASCADE,
    quest_date DATE NOT NULL DEFAULT CURRENT_DATE,
    current_count INT NOT NULL DEFAULT 0,
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    UNIQUE (user_id, quest_id, quest_date)
);

INSERT INTO daily_quests (title, description, quest_type, target_count, xp_reward) VALUES
  ('Quiz Grinder', 'Complete 10 quizzes today', 'QUIZ_COUNT', 10, 100),
  ('Time Investor', 'Study for 20 minutes today', 'STUDY_TIME', 1200, 80),
  ('Perfect Run', 'Complete 3 quizzes in a row without any wrong answer', 'PERFECT_RUN', 3, 60);
