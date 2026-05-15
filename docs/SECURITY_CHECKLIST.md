# Güvenlik Kontrol Listesi (Gün 20 — Blok A.2)

Döküman: `internal-docs/lisans_portalı.md` §3, §6.

## Otomatik kontrol

Sunucu çalışırken (`http://localhost:8080`):

```powershell
python scripts/test_security_checklist.py
```

Tüm maddeler **OK** ise bu blok tamamlanmış sayılır.

---

## 1. Kimlik doğrulama ve oturum

| # | Kontrol | Beklenen | Otomatik |
|---|---------|----------|----------|
| 1.1 | Giriş yapılmadan `/admin/products` | Login sayfasına yönlendirme veya 401/403 | Evet |
| 1.2 | Geçersiz kullanıcı/şifre | Login hata mesajı, oturum açılmaz | Manuel |
| 1.3 | Çıkış (`/logout`) sonrası korumalı sayfa | Tekrar login gerekir | Manuel |

---

## 2. Rol bazlı URL erişimi (`@PreAuthorize`)

| # | Kontrol | Beklenen | Otomatik |
|---|---------|----------|----------|
| 2.1 | BAYI → `/admin/**` (ürün, bayi, lisans, rapor) | 403 / “Yetkisiz” | Evet |
| 2.2 | ADMIN → `/dealer/**` (kurum, lisans, rapor) | 403 / “Yetkisiz” | Evet |
| 2.3 | BAYI → `/dealer/**` kendi modülleri | 200, sayfa açılır | Evet |
| 2.4 | ADMIN → `/admin/**` | 200, sayfa açılır | Evet |

---

## 3. Bayi veri izolasyonu (service katmanı)

| # | Kontrol | Beklenen | Otomatik |
|---|---------|----------|----------|
| 3.1 | BAYI yalnızca kendi kurumlarını listeler | “Sizin oluşturduğunuz” metni | Evet |
| 3.2 | BAYI başka bayinin kurumunu düzenleyemez | 404 / “Kurum bulunamadı” veya yetkisiz | Evet |
| 3.3 | BAYI lisans listesi yalnızca `created_by` = kendisi | Admin’de daha fazla kayıt | Evet* |
| 3.4 | BAYI başka kullanıcının lisans detayına erişemez | Yetkisiz / 403 | Evet |
| 3.5 | BAYI admin raporlarına erişemez | 403 | Evet |

\*Gün 17 test scripti ile doğrulanır; güvenlik scripti örnek kontrol içerir.

---

## 4. Ürün secret görünmezliği

| # | Kontrol | Beklenen | Otomatik |
|---|---------|----------|----------|
| 4.1 | Ürün listesinde `secret_key_enc` yok | HTML’de şifreli alan görünmez | Evet |
| 4.2 | BAYI ürün yönetimi sayfasına erişemez | 403 | Evet |
| 4.3 | Secret yalnızca admin ürün ekleme/düzenleme formunda girilir | Liste/detayda düz metin yok | Manuel |
| 4.4 | `LICENSE_PORTAL_MASTER_KEY` repoda / commit’te yok | `.env` gitignore’da | Manuel (`git status`) |

---

## 5. CSRF ve formlar

| # | Kontrol | Beklenen | Otomatik |
|---|---------|----------|----------|
| 5.1 | POST formlarda `_csrf` hidden alanı | Var | Manuel |
| 5.2 | Lisans üret / kurum kayıt POST’ları oturum + rol ile | Yetkisiz POST reddedilir | Manuel |

---

## 6. Production hazırlığı (manuel)

- [ ] `local` profil ve `seed-local.sql` production’da kapalı
- [ ] Demo şifreler (`password`) production’da yok
- [ ] `LICENSE_PORTAL_MASTER_KEY` güçlü ve ortam değişkeninden
- [ ] HTTPS (reverse proxy) production’da zorunlu
- [ ] Spring Security debug log (`application-local`) production’da kapalı

---

## Bilinen sınırlamalar

- `LicenseGenerator` şirket algoritması gelene kadar placeholder’dır; güvenlik checklist’inden bağımsızdır.
- UI’da `sec:authorize` menüyü gizler; asıl koruma controller + service katmanındadır.
