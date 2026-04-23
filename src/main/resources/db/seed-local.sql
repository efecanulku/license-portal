-- Local seed data (profile: local)
-- Not: Bu seed sadece hızlı geliştirme içindir, prod ortamda kullanılmamalıdır.

INSERT INTO dealers (name, contact_name, email, phone, active, created_at)
SELECT 'Demo Bayi', 'Demo Yetkili', 'dealer@example.com', '+90 555 000 00 00', TRUE, NOW()
WHERE NOT EXISTS (SELECT 1 FROM dealers WHERE name = 'Demo Bayi');

-- BCrypt hash for the string "password" (dev only)
-- (Bu hash gizli değildir; sadece local geliştirme kolaylığıdır.)
INSERT INTO users (username, password_hash, full_name, email, role, dealer_id, active, created_at)
VALUES
  ('admin', '$2a$10$7EqJtq98hPqEX7fNZaFWoOhi5pP7uHhD8bM0X8fKQ2x3x6J0gXHq2', 'Admin User', 'admin@example.com', 'ADMIN', NULL, TRUE, NOW()),
  ('dealer', '$2a$10$7EqJtq98hPqEX7fNZaFWoOhi5pP7uHhD8bM0X8fKQ2x3x6J0gXHq2', 'Dealer User', 'dealer.user@example.com', 'BAYI',
    (SELECT id FROM dealers WHERE name='Demo Bayi' ORDER BY id ASC LIMIT 1),
    TRUE, NOW());

