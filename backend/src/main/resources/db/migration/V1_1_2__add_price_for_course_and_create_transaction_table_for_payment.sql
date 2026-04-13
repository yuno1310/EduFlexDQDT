ALTER TABLE courses ADD COLUMN price BIGINT DEFAULT 0;

CREATE TABLE transactions (
    transaction_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(user_id),
    course_id UUID REFERENCES courses(course_id),
    amount BIGINT NOT NULL,
    payment_method VARCHAR(50) DEFAULT 'MOCK_PAYMENT',
    status VARCHAR(50) DEFAULT 'SUCCESS', 
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
