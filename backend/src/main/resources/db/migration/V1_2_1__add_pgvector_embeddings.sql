-- Enable pgvector extension (Supabase has it pre-installed)
CREATE EXTENSION IF NOT EXISTS vector;

-- Add embedding columns (384 dimensions for all-MiniLM-L6-v2)
ALTER TABLE lesson ADD COLUMN IF NOT EXISTS embedding vector(384);
ALTER TABLE courses ADD COLUMN IF NOT EXISTS embedding vector(384);

-- Indexes for fast cosine similarity search
CREATE INDEX IF NOT EXISTS idx_lesson_embedding
  ON lesson USING ivfflat (embedding vector_cosine_ops) WITH (lists = 50);

CREATE INDEX IF NOT EXISTS idx_courses_embedding
  ON courses USING ivfflat (embedding vector_cosine_ops) WITH (lists = 50);

-- Search function: returns ranked results from both lessons and courses
CREATE OR REPLACE FUNCTION search_content(
  query_embedding vector(384),
  match_threshold float DEFAULT 0.5,
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
  (SELECT 'lesson'::text, l.lesson_id, l.title, c.title,
          (1 - (l.embedding <=> query_embedding))::float
   FROM lesson l JOIN courses c ON l.course_id = c.course_id
   WHERE l.embedding IS NOT NULL
     AND 1 - (l.embedding <=> query_embedding) > match_threshold)
  UNION ALL
  (SELECT 'course'::text, c.course_id, c.title, c.title,
          (1 - (c.embedding <=> query_embedding))::float
   FROM courses c
   WHERE c.embedding IS NOT NULL
     AND 1 - (c.embedding <=> query_embedding) > match_threshold)
  ORDER BY similarity DESC
  LIMIT match_count;
$$;
