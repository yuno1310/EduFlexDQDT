-- High-traffic lookup indexes used by catalog, progress, reviews and RAG retrieval.
CREATE INDEX IF NOT EXISTS idx_courses_status ON courses (lower(status));
CREATE INDEX IF NOT EXISTS idx_lesson_course_id ON lesson (course_id);
CREATE INDEX IF NOT EXISTS idx_enrollments_course_id ON enrollments (course_id);
CREATE INDEX IF NOT EXISTS idx_lesson_progress_user_id ON lesson_progress (user_id);
CREATE INDEX IF NOT EXISTS idx_quiz_attempts_user_lesson ON quiz_attempts (user_id, lesson_id);
CREATE INDEX IF NOT EXISTS idx_course_reviews_course_created ON course_reviews (course_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_daily_quest_progress_user_date
  ON user_daily_quest_progress (user_id, quest_date);
CREATE INDEX IF NOT EXISTS idx_users_email_lower ON users (lower(email));
