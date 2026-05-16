-- Kurum–bayi çoklu ilişki (döküman §2.5)
CREATE TABLE IF NOT EXISTS customer_dealers (
    customer_id BIGINT NOT NULL REFERENCES customers (id) ON DELETE CASCADE,
    dealer_id BIGINT NOT NULL REFERENCES dealers (id) ON DELETE CASCADE,
    linked_at TIMESTAMP,
    PRIMARY KEY (customer_id, dealer_id)
);

CREATE INDEX IF NOT EXISTS idx_customer_dealers_dealer_id ON customer_dealers (dealer_id);

INSERT INTO customer_dealers (customer_id, dealer_id, linked_at)
SELECT c.id, c.created_by_dealer_id, COALESCE(c.created_at, NOW())
FROM customers c
WHERE c.created_by_dealer_id IS NOT NULL
ON CONFLICT (customer_id, dealer_id) DO NOTHING;
