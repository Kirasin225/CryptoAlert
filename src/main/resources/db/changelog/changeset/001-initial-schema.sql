-- liquibase formatted sql

-- changeset roman_diakov:001-1
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    chat_id BIGINT NOT NULL UNIQUE,
    username VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS alerts (
     id BIGSERIAL PRIMARY KEY,
     user_id BIGINT NOT NULL REFERENCES users(id),
     symbol VARCHAR(10) NOT NULL,
     target_price DECIMAL(20, 8) NOT NULL,
     initial_price DECIMAL(20, 8),
     created_at TIMESTAMP DEFAULT NOW()
);
