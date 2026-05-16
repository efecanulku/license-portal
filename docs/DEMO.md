# Demo ve Uçtan Uca Test Senaryoları

AVKAR Lisans Portalı için sunum, QA ve teslim öncesi doğrulama rehberi.

| | |
|---|---|
| **Uygulama** | http://localhost:8080 |
| **Profil** | `local` (seed + `.env`) |
| **Kurulum** | [`README.md`](../README.md) |

---

## Ön koşullar

Aşağıdakiler tamamlanmadan senaryolara geçmeyin.

- [ ] PostgreSQL çalışıyor; `license_portal` veritabanı oluşturuldu
- [ ] `copy env.example .env` yapıldı; `LICENSE_PORTAL_MASTER_KEY` ve DB bilgileri dolduruldu
- [ ] `.\scripts\run-local.ps1` ile uygulama ayakta
- [ ] Tarayıcıda http://localhost:8080/login açılıyor

**Demo hesapları** (yalnızca `local` profil):

| Kullanıcı | Şifre | Rol |
|-----------|-------|-----|
| `admin` | `password` | Yönetici |
| `dealer` | `password` | Bayi (Demo Bayi) |

**Hızlı otomasyon (isteğe bağlı, ~1 dk):**

```powershell
python scripts/test_e2e_smoke.py
python scripts/test_security_checklist.py
```

Her ikisinde de son satırda **0 kaldı** beklenir.

---

## Senaryo A — Yönetici (Admin) tam akış

**Amaç:** Ürün → bayi yetkisi → kurum → lisans üretimi → rapor zincirini uçtan uca doğrulamak.

**Süre:** yaklaşık 15–20 dakika (ilk kez) · sunumda 8–10 dakika

### A1. Giriş ve arayüz

| # | İşlem | Beklenen |
|---|--------|----------|
| A1.1 | `/login` → `admin` / `password` | AVKAR temalı giriş sonrası ana sayfa |
| A1.2 | Ana sayfa | “AVKAR Lisans Portalı”, yönetim kartları (Ürünler, Bayiler, …) |
| A1.3 | Sol menü | Rol: Yönetici; Ürünler, Bayiler, Kurumlar, Lisanslar, Raporlar |
| A1.4 | Çıkış → tekrar giriş | Oturum düzgün kapanır / açılır |

### A2. Ürün yönetimi (secret güvenliği)

| # | İşlem | Beklenen |
|---|--------|----------|
| A2.1 | **Ürünler** → **Yeni ürün** | Form açılır |
| A2.2 | Ad: `Demo Ürün`, Kod: `DEMO_E2E`, Secret: `test-secret-123` | — |
| A2.3 | Kaydet | Listede ürün görünür |
| A2.4 | Liste sayfası | **Secret sütunu yok**; kaynakta `secret_key_enc` görünmez |
| A2.5 | Düzenle → secret boş bırak | Mevcut secret korunur |
| A2.6 | Arama kutusuna `DEMO` yaz | Filtrelenmiş liste |

### A3. Bayi ve ürün yetkisi

| # | İşlem | Beklenen |
|---|--------|----------|
| A3.1 | **Bayiler** → **Demo Bayi** (seed) | Kayıt listede |
| A3.2 | **Ürün yetkileri** | Yetki listesi; `DEMO_E2E` için **Yetki ver** |
| A3.3 | Başarı bildirimi | **Tek** yeşil uyarı (çift bildirim olmamalı) |
| A3.4 | **Kullanıcılar** | `dealer` kullanıcısı listede; arama kutusu çalışır |

### A4. Kurum

| # | İşlem | Beklenen |
|---|--------|----------|
| A4.1 | **Kurumlar** → **Yeni kurum** | Form |
| A4.2 | Ad: `E2E Test Hastanesi`, bağlı bayi: **Demo Bayi** | Kayıt OK |
| A4.3 | Liste / arama | Kurum görünür, arama çalışır |
| A4.4 | (İsteğe bağlı) Düzenle → ikinci bayi bağla | Çoklu bayi testi için hazır |

> Admin lisans üretiminde kurumun **en az bir bağlı bayisi** olmalıdır.

### A5. Lisans üretimi (kritik)

| # | İşlem | Beklenen |
|---|--------|----------|
| A5.1 | **Lisans üret** | Kurum arama, ürün, sistem anahtarı, tarih, demo kutusu |
| A5.2 | Kurum ara → `E2E Test Hastanesi` seç | Yeşil “Seçili: …” metni |
| A5.3 | Kuruma birden fazla bayi bağlıysa | “Lisansı kaydedecek bayi” açılır; birini seçin |
| A5.4 | Tek bayili kurumda | Bayi alanı gizli veya otomatik |
| A5.5 | Ürün: `DEMO_E2E`, sistem: `E2E-SYS-001`, gelecek tarih → **Lisans üret** | Başarı |
| A5.6 | Sonuç ekranı | “Lisans başarıyla oluşturuldu”, büyük anahtar, **Kopyala** |
| A5.7 | **Lisanslar** | Yeni kayıt; filtreler ve arama (`q`) çalışır |
| A5.8 | Detay | Kurum, ürün, bayi, tarih, **Üreten: admin** doğru |

**Not:** Üretilen anahtar şu an **placeholder algoritma** ile üretilir. Gerçek yazılımla uyum şirket algoritması entegre edildikten sonra test edilir.

### A6. Raporlar

Menüden **Raporlar** veya doğrudan URL.

| # | Sayfa | URL | Kontrol |
|---|--------|-----|---------|
| A6.1 | Rapor ana | `/reports` | Kartlar; admin için “ürün sayısı” linki |
| A6.2 | Ürün bazlı | `/reports/product-count` | Tablo + bar grafik (taşmamalı) |
| A6.3 | Bayi bazlı | `/reports/dealer-count` | Demo Bayi satırı, sayı ≥ 1 |
| A6.4 | Müşteri özeti | `/reports/customer-summary` | Kurum + adet; grafik okunaklı |
| A6.5 | Demo dağılım | `/reports/demo-distribution` | Özet kartlar + donut sığmalı |
| A6.6 | Süresi dolan | `/reports/expiring` | Filtre (ör. 400 gün), tablo, sayfalama; **500 hatası olmamalı** |

Ana sayfada “yakında bitecek lisans” uyarısı varsa linke tıklayın → expiring raporu açılmalı.

### A7. Çıkış ve koruma

| # | İşlem | Beklenen |
|---|--------|----------|
| A7.1 | **Çıkış** | Login sayfası |
| A7.2 | Giriş yapmadan `/admin/products` | Login'e yönlendirme |

---

## Senaryo B — Bayi tam akış

**Amaç:** Yetkili ürün, kendi kurumu, kendi lisansı ve kendi raporları; yetkisiz URL'lerin engellenmesi.

Önce **çıkış** yapın; `dealer` / `password` ile giriş yapın.

**Süre:** yaklaşık 10–15 dakika · sunumda 5–7 dakika

### B1. Giriş ve menü

| # | İşlem | Beklenen |
|---|--------|----------|
| B1.1 | `dealer` / `password` | Bayi paneli / ana sayfa |
| B1.2 | Sol menü | **Ürünler / Bayiler / admin Kurumlar yok** |
| B1.3 | Görünen modüller | Kurumlarım, Lisanslarım, Lisans üret, Raporlar |

### B2. Kurum

| # | İşlem | Beklenen |
|---|--------|----------|
| B2.1 | **Kurumlarım** | Alt başlık: “Bayinize bağlı kurumlar” |
| B2.2 | **Yeni kurum** → `E2E Bayi Kurumu` | Kayıt listede |
| B2.3 | URL: `/admin/customers` | Yetkisiz (403 veya login) |

### B3. Lisans

| # | İşlem | Beklenen |
|---|--------|----------|
| B3.1 | **Lisans üret** | Ürün listesinde yalnızca yetkili ürünler (`DEMO_E2E` vb.) |
| B3.2 | Kendi kurum + ürün, `E2E-DEALER-SYS`, üret | Başarı + kopyala |
| B3.3 | **Lisanslarım** | Yalnızca bu kullanıcının (`dealer`) ürettikleri |
| B3.4 | Detayda **Üreten: dealer** | Kendi kaydı |

### B4. Raporlar (bayi kapsamı)

| # | İşlem | Beklenen |
|---|--------|----------|
| B4.1 | `/reports` | Açılır; bayi verisi |
| B4.2 | Müşteri özeti, demo dağılım, süresi dolan | Sadece bayi kapsamındaki sayılar |
| B4.3 | `/reports/product-count` | **Yetkisiz** (admin raporu) |

### B5. Negatif testler (güvenlik)

| # | İşlem | Beklenen |
|---|--------|----------|
| B5.1 | Admin’in ürettiği lisans: detayda **Üreten: admin** olan bir `id` ile `/dealer/licenses/{id}` | Erişim yok |
| B5.2 | Bayi kendi ürettiği lisans `id` ile aynı URL | **Açılır** (bu normaldir) |
| B5.3 | `/admin/products` | Yetkisiz |

Otomasyon: `python scripts/test_manual_8_3_8_4.py`

---

## Senaryo C — Teslim öncesi hızlı regresyon (~10 dk)

- [ ] `python scripts/test_e2e_smoke.py` → 0 fail
- [ ] `python scripts/test_security_checklist.py` → 0 fail
- [ ] Admin: bir lisans üret + panoya kopyala
- [ ] Bayi lisans listesi, admin listesinden farklı (daha az kayıt)
- [ ] Ürün listesi kaynağında secret yok
- [ ] `git status` → `.env` ve `internal-docs/` yok

---

## Önerilen sunum sırası (~15 dk)

1. **Admin (8 dk):** Ürün + secret güvenliği → bayi yetkisi → kurum → lisans üret + kopyala → bir rapor özeti  
2. **Bayi (5 dk):** Kurum → lisans → rapor  
3. **Güvenlik (2 dk):** Bayi ile `/admin/products` → yetkisiz; ürün listesinde secret yok  

---

## Sorun giderme

| Belirti | Olası neden | Çözüm |
|---------|-------------|--------|
| Login olmuyor | Profil / DB / seed | `SPRING_PROFILES_ACTIVE=local`, PostgreSQL, `.env` |
| Port 8080 dolu | Eski Java süreci | Görev yöneticisinden `java.exe` kapatın veya mevcut oturumu kullanın |
| Master key hatası | Eksik env | `.env` → `LICENSE_PORTAL_MASTER_KEY` |
| Admin: “bayi seçin” | Çoklu bayi | Kurum düzenlemeden bayi bağlayın veya formda bayi seçin |
| Bayi ürün göremiyor | Yetki yok | Admin → Bayi → Ürün yetkileri |
| `/reports/expiring` 500 | Geçici / filtre | Sayfayı yenileyin; gün değerini düşürün; log kontrol |
| Grafik taşması | Önbellek | Ctrl+F5 |

---

## Şirkete iletilecek bilinçli sınırlar

| Konu | Durum |
|------|--------|
| **LicenseGenerator** | Placeholder; gerçek algoritma şirketten bekleniyor |
| **Demo hesaplar** | Yalnızca `local` profil seed |
| **Gizli dosyalar** | `.env`, `internal-docs/` commit edilmez |

---

## İlgili dokümanlar

- [`SECURITY_CHECKLIST.md`](SECURITY_CHECKLIST.md) — güvenlik maddeleri ve otomasyon  
- [`ENV.md`](ENV.md) — ortam değişkenleri  
- [`README.md`](../README.md) — kurulum ve proje özeti  
- [`PLANNING.md`](PLANNING.md) — geliştirme planı  
