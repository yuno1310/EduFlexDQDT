CREATE TABLE learner_profile (
    learner_id UUID PRIMARY KEY,
    total_xp INT DEFAULT 0,
    level INT DEFAULT 1,
    current_streak INT DEFAULT 0,
    hearts_remaining INT DEFAULT 5,

    CONSTRAINT fk_learner_user FOREIGN KEY (learner_id) REFERENCES users(user_id) ON DELETE CASCADE
);
