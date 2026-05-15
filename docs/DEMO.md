# Uçtan Uca Demo Senaryoları (Gün 20 — Blok A.3)

Sunucu: `http://localhost:8080` · Profil: `local` · Detay: [`README.md`](../README.md)

## Ön koşullar

- [ ] PostgreSQL çalışıyor, `license_portal` DB var
- [ ] `.env` dolu (`LICENSE_PORTAL_MASTER_KEY` dahil)
- [ ] `.\scripts\run-local.ps1` ile uygulama ayakta
- [ ] Seed kullanıcılar: `admin` / `dealer`, şifre `password`

**Otomatik duman testi (isteğe bağlı):**

```powershell
python scripts/test_e2e_smoke.py
```

---

## Senaryo A — Admin tam akış

Amaç: Ürün → bayi yetkisi → kurum → lisans → rapor zincirini doğrulamak.

### A1. Giriş ve ana sayfa

| Adım | İşlem | Beklenen |
|------|--------|----------|
| A1.1 | `/login` → `admin` / `password` | Ana sayfaya yönlendirme |
| A1.2 | Ana sayfa kartları | Ürünler, Bayiler, Kurumlar, Lisanslar, Raporlar linkleri görünür |
| A1.3 | Sidebar | Rol: ADMIN, yönetim menüsü açık |

### A2. Ürün (secret şifreli)

| Adım | İşlem | Beklenen |
|------|--------|----------|
| A2.1 | **Ürünler** → **Yeni ürün** | Form açılır |
| A2.2 | Ad: `Demo Ürün`, Kod: `DEMO_E2E`, Secret: `test-secret-123` | — |
| A2.3 | Kaydet | Listede ürün görünür |
| A2.4 | Liste sayfası | **Secret sütunu yok**; şifreli alan HTML’de görünmez |

### A3. Bayi ve yetki

| Adım | İşlem | Beklenen |
|------|--------|----------|
| A3.1 | **Bayiler** → Demo Bayi detay / düzenle | Demo Bayi kaydı var (seed) |
| A3.2 | Bayi → **Ürün yetkileri** | Yetki listesi |
| A3.3 | `DEMO_E2E` (veya mevcut ürün) için yetki ver | Yetki satırı eklenir |
| A3.4 | Bayi kullanıcıları | `dealer` kullanıcısı aktif |

### A4. Kurum

| Adım | İşlem | Beklenen |
|------|--------|----------|
| A4.1 | **Kurumlar** → **Yeni kurum** | Form |
| A4.2 | Ad: `E2E Test Hastanesi`, bayi: Demo Bayi | Kayıt oluşur |
| A4.3 | Listede kurum | Filtre / listede görünür |

> Admin lisans üretiminde kurumun **bağlı bayisi** olmalıdır (`created_by_dealer`).

### A5. Lisans üretimi ve sonuç

| Adım | İşlem | Beklenen |
|------|--------|----------|
| A5.1 | **Lisans üret** | Form: kurum, ürün, sistem anahtarı, tarih |
| A5.2 | Kurum + ürün seç, sistem anahtarı: `E2E-SYS-001`, geçerlilik: gelecek tarih | — |
| A5.3 | **Lisans üret** | Sonuç / detay sayfası |
| A5.4 | Sonuç ekranı | “Lisans üretildi”, anahtar büyük font, **Kopyala** çalışır |
| A5.5 | **Lisanslar** listesi | Yeni kayıt, filtreler çalışır |

### A6. Raporlar (admin)

| Adım | İşlem | Beklenen |
|------|--------|----------|
| A6.1 | Ürün bazlı lisans sayısı | Ürün kodu + adet |
| A6.2 | Bayi bazlı lisans sayısı | Demo Bayi satırı, sayı ≥ 1 |
| A6.3 | Müşteri bazlı lisans özeti | Kurum başına adet |
| A6.4 | Demo / üretim dağılımı | Demo + üretim kartları, ilerleme çubuğu |
| A6.5 | Süresi dolan / yaklaşan | Filtre, tablo, sayfalama |

### A7. Çıkış

| Adım | İşlem | Beklenen |
|------|--------|----------|
| A7.1 | **Çıkış** | Login sayfası |
| A7.2 | `/admin/products` (girişsiz) | Login’e yönlendirme |

---

## Senaryo B — Bayi tam akış

Amaç: Yetkili ürün, kendi kurumu, kendi lisansı ve kendi raporu.

### B1. Giriş

| Adım | İşlem | Beklenen |
|------|--------|----------|
| B1.1 | `dealer` / `password` | Ana sayfa, BAYI menüsü |
| B1.2 | Ana sayfa | Kurumlarım, Lisanslarım, Lisans üret, Raporlar |

### B2. Kurum

| Adım | İşlem | Beklenen |
|------|--------|----------|
| B2.1 | **Kurumlarım** → **Yeni** | Form |
| B2.2 | `E2E Bayi Kurumu` kaydet | Listede “Sizin oluşturduğunuz kurumlar” |
| B2.3 | `/admin/customers` (URL) | **Yetkisiz** (manuel URL denemesi) |

### B3. Lisans üretimi

| Adım | İşlem | Beklenen |
|------|--------|----------|
| B3.1 | **Lisans üret** | Ürün dropdown: yalnızca yetkili ürünler |
| B3.2 | Kendi kurum + yetkili ürün, `E2E-DEALER-SYS` | Üretim başarılı |
| B3.3 | Sonuç | Kopyala + özet bilgiler |
| B3.4 | **Lisanslarım** | Yalnızca bu kullanıcının ürettikleri |

### B4. Raporlar (bayi)

| Adım | İşlem | Beklenen |
|------|--------|----------|
| B4.1 | Lisans özetim | Tek satır, sayı ≥ 1 |
| B4.2 | Müşteri bazlı özet | Kendi kurumlarınız |
| B4.3 | Demo / üretim dağılımı | Sadece sizin ürettikleriniz |
| B4.4 | Süresi dolan / yaklaşan | “Yalnızca sizin ürettikleriniz” |
| B4.5 | `/admin/reports/product-count` | Yetkisiz |

### B5. Negatif — başkasının verisi

| Adım | İşlem | Beklenen |
|------|--------|----------|
| B5.1 | Admin’in ürettiği bir lisans ID’si ile `/dealer/licenses/{id}` | Erişim yok / yetkisiz |
| B5.2 | Başka bayinin kurumunu düzenleme (varsa id=1) | Kurum bulunamadı / yetkisiz |

---

## Senaryo C — Hızlı regresyon (5 dk)

Teslim öncesi minimum kontrol:

- [ ] `python scripts/test_e2e_smoke.py` → 0 fail
- [ ] `python scripts/test_security_checklist.py` → 0 fail
- [ ] Admin: bir lisans üret + kopyala
- [ ] Bayi: lisans listesi admin listesinden farklı (daha az kayıt)
- [ ] Ürün listesinde secret görünmüyor

---

## Demo sırası önerisi (sunum)

1. **Admin (8 dk):** A2 ürün → A3 yetki → A4 kurum → A5 lisans + kopyala → A6 rapor özeti  
2. **Bayi (5 dk):** B2 kurum → B3 lisans → B4 rapor  
3. **Güvenlik (2 dk):** Bayi ile `/admin/products` → Yetkisiz; ürün listesinde secret yok  

---

## Sorun giderme

| Belirti | Olası neden |
|---------|-------------|
| Login olmuyor | `local` profil, seed çalıştı mı; PostgreSQL + `.env` |
| Lisans üretilemiyor (admin) | Kurumun bayisi yok; ürün pasif |
| Bayi ürün göremiyor | Admin’den ürün yetkisi verilmemiş |
| Master key hatası | `LICENSE_PORTAL_MASTER_KEY` `.env` içinde |

---

## İlgili dokümanlar

- [`SECURITY_CHECKLIST.md`](SECURITY_CHECKLIST.md) — güvenlik maddeleri  
- [`ENV.md`](ENV.md) — ortam değişkenleri  
- [`PLANNING.md`](PLANNING.md) — 20 gün planı  
