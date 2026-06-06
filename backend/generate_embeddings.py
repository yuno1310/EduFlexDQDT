#!/usr/bin/env python3
"""
Generate embeddings for all NULL embedding rows in lesson and courses tables.
Uses fastembed (all-MiniLM-L6-v2, 384 dims) — lightweight, no torch needed.

Install deps:
  pip install fastembed psycopg2-binary
"""

import psycopg2
from fastembed import TextEmbedding

DB_URL = "postgresql://postgres.pkcsfpmyyzilmeolygor:141592654Duy**@aws-1-ap-southeast-1.pooler.supabase.com:5432/postgres"

MODEL_NAME = "sentence-transformers/all-MiniLM-L6-v2"

def vec_to_pg(v):
    return "[" + ",".join(str(x) for x in v) + "]"

def main():
    print(f"Loading model {MODEL_NAME}...")
    model = TextEmbedding(model_name=MODEL_NAME)

    conn = psycopg2.connect(DB_URL)
    cur = conn.cursor()

    # --- COURSES ---
    cur.execute("SELECT course_id, title, description FROM courses WHERE embedding IS NULL")
    courses = cur.fetchall()
    print(f"Found {len(courses)} courses with NULL embedding")

    for course_id, title, description in courses:
        text = f"{title}. {description or ''}".strip()
        emb = list(model.embed([text]))[0].tolist()
        cur.execute(
            "UPDATE courses SET embedding = %s::vector WHERE course_id = %s",
            (vec_to_pg(emb), course_id)
        )
        print(f"  [course] {title[:60]}")

    # --- LESSONS ---
    cur.execute("SELECT lesson_id, title, content FROM lesson WHERE embedding IS NULL")
    lessons = cur.fetchall()
    print(f"Found {len(lessons)} lessons with NULL embedding")

    for lesson_id, title, content in lessons:
        text = f"{title}. {content or ''}".strip()
        emb = list(model.embed([text]))[0].tolist()
        cur.execute(
            "UPDATE lesson SET embedding = %s::vector WHERE lesson_id = %s",
            (vec_to_pg(emb), lesson_id)
        )
        print(f"  [lesson] {title[:60]}")

    conn.commit()
    cur.close()
    conn.close()
    print("Done.")

if __name__ == "__main__":
    main()
