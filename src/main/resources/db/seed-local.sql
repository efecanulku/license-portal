-- Local seed data (profile: local)
-- Not: Bu seed sadece hızlı geliştirme içindir, prod ortamda kullanılmamalıdır.

INSERT INTO dealers (name, contact_name, email, phone, active, created_at)
SELECT 'Demo Bayi', 'Demo Yetkili', 'dealer@example.com', '+90 555 000 00 00', TRUE, NOW()
WHERE NOT EXISTS (SELECT 1 FROM dealers WHERE name = 'Demo Bayi');

-- Local dev password: "password" (BCrypt pgcrypto ile üretilir)
INSERT INTO users (username, password_hash, full_name, email, role, dealer_id, active, created_at)
VALUES ('admin', crypt('password', gen_salt('bf', 10)), 'Admin User', 'admin@example.com', 'ADMIN', NULL, TRUE, NOW())
ON CONFLICT (username) DO NOTHING;

INSERT INTO users (username, password_hash, full_name, email, role, dealer_id, active, created_at)
VALUES ('dealer', crypt('password', gen_salt('bf', 10)), 'Dealer User', 'dealer.user@example.com', 'BAYI',
        (SELECT id FROM dealers WHERE name='Demo Bayi' ORDER BY id ASC LIMIT 1),
        TRUE, NOW())
ON CONFLICT (username) DO NOTHING;

