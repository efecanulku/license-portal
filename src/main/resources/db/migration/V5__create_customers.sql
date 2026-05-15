-- Doküman referansı: internal-docs/lisans_portalı.md
-- customers tablosu

CREATE TABLE IF NOT EXISTS customers (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    tax_number VARCHAR(50),
    address TEXT,
    contact_name VARCHAR(255),
    contact_email VARCHAR(255),
    contact_phone VARCHAR(50),
    created_by_dealer_id BIGINT REFERENCES dealers (id),
    created_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_customers_created_by_dealer_id ON customers (created_by_dealer_id);
CREATE INDEX IF NOT EXISTS idx_customers_name ON customers (name);
