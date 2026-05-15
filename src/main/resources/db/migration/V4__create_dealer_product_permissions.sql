-- Doküman referansı: internal-docs/lisans_portalı.md
-- dealer_product_permissions tablosu

CREATE TABLE IF NOT EXISTS dealer_product_permissions (
    id BIGSERIAL PRIMARY KEY,
    dealer_id BIGINT NOT NULL REFERENCES dealers (id),
    product_id BIGINT NOT NULL REFERENCES products (id),
    granted_at TIMESTAMP,
    granted_by BIGINT REFERENCES users (id),
    CONSTRAINT uq_dealer_product_permissions_dealer_product UNIQUE (dealer_id, product_id)
);

CREATE INDEX IF NOT EXISTS idx_dpp_dealer_id ON dealer_product_permissions (dealer_id);
CREATE INDEX IF NOT EXISTS idx_dpp_product_id ON dealer_product_permissions (product_id);
