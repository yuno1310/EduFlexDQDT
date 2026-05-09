CREATE EXTENSION IF NOT EXISTS vector;

ALTER TABLE lesson ADD COLUMN IF NOT EXISTS embedding vector(384);
ALTER TABLE courses ADD COLUMN IF NOT EXISTS embedding vector(384);

CREATE INDEX IF NOT EXISTS idx_lesson_embedding
  ON lesson USING ivfflat (embedding vector_cosine_ops) WITH (lists = 50);

CREATE INDEX IF NOT EXISTS idx_courses_embedding
  ON courses USING ivfflat (embedding vector_cosine_ops) WITH (lists = 50);

CREATE OR REPLACE FUNCTION search_content(
  query_embedding vector(384),
  match_threshold float DEFAULT 0.1,
  match_count int DEFAULT 10
)
RETURNS TABLE (
  result_type text,
  result_id uuid,
  title varchar,
  course_title varchar,
  similarity float
)
LANGUAGE sql STABLE
AS $$
  SELECT * FROM (
    (SELECT 'lesson'::text, l.lesson_id, l.title, c.title,
            (1 - (l.embedding <=> query_embedding))::float AS similarity
     FROM lesson l JOIN courses c ON l.course_id = c.course_id
     WHERE l.embedding IS NOT NULL
       AND 1 - (l.embedding <=> query_embedding) > match_threshold)
    UNION ALL
    (SELECT 'course'::text, c.course_id, c.title, c.title,
            (1 - (c.embedding <=> query_embedding))::float AS similarity
     FROM courses c
     WHERE c.embedding IS NOT NULL
       AND 1 - (c.embedding <=> query_embedding) > match_threshold)
  ) sub
  ORDER BY similarity DESC
  LIMIT match_count;
$$;
