-- Add parent_lesson_id column to lesson table
-- TEXT/VIDEO lessons: parent_lesson_id = NULL
-- Quiz lessons: parent_lesson_id = ID of parent TEXT/VIDEO lesson
ALTER TABLE lesson ADD COLUMN parent_lesson_id UUID;

ALTER TABLE lesson ADD CONSTRAINT fk_lesson_parent
    FOREIGN KEY (parent_lesson_id) REFERENCES lesson(lesson_id) ON DELETE CASCADE;
