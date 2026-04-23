-- Doküman referansı: internal-docs/lisans_portalı.md
-- Minimum iskelet: users + dealers

-- Role enum (PostgreSQL)
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'user_role') THEN
        CREATE TYPE user_role AS ENUM ('ADMIN', 'BAYI');
    END IF;
END $$;

CREATE TABLE IF NOT EXISTS dealers (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    contact_name VARCHAR(255),
    email VARCHAR(255),
    phone VARCHAR(50),
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(255),
    email VARCHAR(255),
    role user_role NOT NULL,
    dealer_id BIGINT REFERENCES dealers(id),
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,

    CONSTRAINT chk_users_dealer_required_for_bayi
        CHECK (
            (role = 'ADMIN' AND dealer_id IS NULL)
            OR
            (role = 'BAYI' AND dealer_id IS NOT NULL)
        )
);

CREATE INDEX IF NOT EXISTS idx_users_dealer_id ON users (dealer_id);
