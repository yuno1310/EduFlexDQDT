ALTER TABLE users 
ALTER COLUMN user_id TYPE UUID USING gen_random_uuid(),
ALTER COLUMN user_id SET DEFAULT gen_random_uuid();
