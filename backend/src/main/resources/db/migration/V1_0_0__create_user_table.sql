CREATE TABLE users (
    user_id VARCHAR PRIMARY KEY,
    email VARCHAR NOT NULL,
    password_hash VARCHAR,
    full_name VARCHAR NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE
);
