-- Create enum type for morning feeling
CREATE TYPE morning_feeling AS ENUM ('BAD', 'OK', 'GOOD');

-- Create sleep_logs table
CREATE TABLE sleep_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    sleep_date DATE NOT NULL,
    bedtime TIME NOT NULL,
    wake_time TIME NOT NULL,
    total_time_in_bed_minutes INTEGER NOT NULL,
    morning_feeling morning_feeling NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    
    -- Ensure one sleep log per user per date
    CONSTRAINT unique_user_sleep_date UNIQUE (user_id, sleep_date)
);

-- Create indexes for better query performance
CREATE INDEX idx_sleep_logs_user_id ON sleep_logs(user_id);
CREATE INDEX idx_sleep_logs_sleep_date ON sleep_logs(sleep_date);
CREATE INDEX idx_sleep_logs_user_date ON sleep_logs(user_id, sleep_date DESC);

