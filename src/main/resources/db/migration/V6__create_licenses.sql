-- Doküman referansı: internal-docs/lisans_portalı.md
-- licenses tablosu

CREATE TABLE IF NOT EXISTS licenses (
    id BIGSERIAL PRIMARY KEY,
    license_key TEXT NOT NULL,
    product_id BIGINT NOT NULL REFERENCES products (id),
    customer_id BIGINT NOT NULL REFERENCES customers (id),
    dealer_id BIGINT NOT NULL REFERENCES dealers (id),
    created_by BIGINT NOT NULL REFERENCES users (id),
    system_key VARCHAR(512) NOT NULL,
    license_owner_description TEXT,
    camera_enabled BOOLEAN DEFAULT FALSE,
    valid_until DATE NOT NULL,
    is_demo BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_licenses_dealer_id ON licenses (dealer_id);
CREATE INDEX IF NOT EXISTS idx_licenses_product_id ON licenses (product_id);
CREATE INDEX IF NOT EXISTS idx_licenses_customer_id ON licenses (customer_id);
CREATE INDEX IF NOT EXISTS idx_licenses_valid_until ON licenses (valid_until);
