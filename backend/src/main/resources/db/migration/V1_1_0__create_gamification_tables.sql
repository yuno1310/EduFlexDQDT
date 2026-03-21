-- =============================================
-- Gamification tables: XP, Streak, Badges
-- =============================================

-- 1. gamification_stats: 1-to-1 with users
CREATE TABLE gamification_stats (
    id              BIGSERIAL PRIMARY KEY,
    user_id         UUID NOT NULL UNIQUE,
    xp              INT NOT NULL DEFAULT 0,
    level           INT NOT NULL DEFAULT 1,
    streak_days     INT NOT NULL DEFAULT 0,
    last_study_date DATE,
    CONSTRAINT fk_gamification_user
        FOREIGN KEY (user_id) REFERENCES users(user_id)
        ON DELETE CASCADE
);

-- 2. badges: badge templates
CREATE TABLE badges (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR NOT NULL UNIQUE,
    description     VARCHAR(500),
    icon_url        VARCHAR,
    condition_type  VARCHAR(50) NOT NULL
);

-- 3. user_badges: many-to-many (user <-> badge)
CREATE TABLE user_badges (
    id          BIGSERIAL PRIMARY KEY,
    user_id     UUID NOT NULL,
    badge_id    BIGINT NOT NULL,
    earned_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_badge_user
        FOREIGN KEY (user_id) REFERENCES users(user_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_user_badge_badge
        FOREIGN KEY (badge_id) REFERENCES badges(id)
        ON DELETE CASCADE,
    CONSTRAINT uq_user_badge UNIQUE (user_id, badge_id)
);

-- Seed default badges
INSERT INTO badges (name, description, icon_url, condition_type) VALUES
    ('First Login',   'Welcome! You logged in for the first time.',  NULL, 'FIRST_LOGIN'),
    ('7-Day Streak',  'Studied for 7 consecutive days!',             NULL, 'STREAK_7'),
    ('30-Day Streak', 'Studied for 30 consecutive days!',            NULL, 'STREAK_30'),
    ('500 XP',        'Earned 500 experience points!',               NULL, 'XP_500'),
    ('1000 XP',       'Earned 1000 experience points!',              NULL, 'XP_1000');
