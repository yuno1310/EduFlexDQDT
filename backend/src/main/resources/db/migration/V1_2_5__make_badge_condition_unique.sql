-- condition_type is the stable business key used by badge-award code. Collapse
-- any duplicates created by older concurrent requests before enforcing it.
WITH canonical AS (
  SELECT condition_type, MIN(id) AS keep_id
  FROM badges
  GROUP BY condition_type
), duplicate_badges AS (
  SELECT b.id AS duplicate_id, c.keep_id
  FROM badges b
  JOIN canonical c ON c.condition_type = b.condition_type
  WHERE b.id <> c.keep_id
)
INSERT INTO user_badges (user_id, badge_id, earned_at)
SELECT ub.user_id, d.keep_id, ub.earned_at
FROM user_badges ub
JOIN duplicate_badges d ON d.duplicate_id = ub.badge_id
ON CONFLICT (user_id, badge_id) DO NOTHING;

DELETE FROM user_badges ub
USING (
  SELECT b.id AS duplicate_id
  FROM badges b
  JOIN (
    SELECT condition_type, MIN(id) AS keep_id
    FROM badges
    GROUP BY condition_type
  ) c ON c.condition_type = b.condition_type
  WHERE b.id <> c.keep_id
) d
WHERE ub.badge_id = d.duplicate_id;

DELETE FROM badges b
USING (
  SELECT condition_type, MIN(id) AS keep_id
  FROM badges
  GROUP BY condition_type
) c
WHERE b.condition_type = c.condition_type
  AND b.id <> c.keep_id;

ALTER TABLE badges DROP CONSTRAINT IF EXISTS badges_name_key;
CREATE UNIQUE INDEX IF NOT EXISTS uq_badges_condition_type ON badges (condition_type);
