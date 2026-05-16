# AVKAR Lisans Portalı

Spring Boot 3 tabanlı web uygulaması: ürün tanımları, bayi ve kurum yönetimi, lisans üretimi ve raporlama. Arayüz Thymeleaf + Bootstrap; veri katmanı PostgreSQL ve Flyway migration.

> İş gereksinimleri şirket içi dokümanda tutulur (`internal-docs/` — repoya alınmaz).

---

## Özellikler (özet)

| Alan | Admin (ADMIN) | Bayi (BAYI) |
|------|---------------|-------------|
| Ürünler | Tanım, secret (şifreli), aktif/pasif | — |
| Bayiler | Bayi ve kullanıcı yönetimi, ürün yetkileri | — |
| Kurumlar | Tüm kurumlar, çoklu bayi bağlantısı | Bayiye bağlı kurumlar |
| Lisanslar | Üretim, liste, filtre, detay | Yalnızca kendi ürettikleri |
| Raporlar | Tüm raporlar + ürün sayısı | Bayi kapsamı (ürün sayısı hariç) |

---

## Gereksinimler

- **Java 17**
- **Maven 3.9+**
- **PostgreSQL 14+**
- **Python 3** (isteğe bağlı — otomatik duman/güvenlik testleri için)

---

## Hızlı başlangıç (local)

### 1. Veritabanı

PostgreSQL'de boş bir veritabanı oluşturun:

```sql
CREATE DATABASE license_portal;
```

### 2. Ortam değişkenleri

```powershell
copy env.example .env
```

`.env` dosyasını düzenleyin:

| Değişken | Açıklama |
|----------|----------|
| `SPRING_DATASOURCE_URL` | JDBC URL (ör. `jdbc:postgresql://localhost:5432/license_portal`) |
| `SPRING_DATASOURCE_USERNAME` | Veritabanı kullanıcısı |
| `SPRING_DATASOURCE_PASSWORD` | Veritabanı şifresi |
| `LICENSE_PORTAL_MASTER_KEY` | Ürün secret AES anahtarı — güçlü, rastgele bir değer kullanın |
| `SPRING_PROFILES_ACTIVE` | `local` (seed kullanıcıları ve `.env` yüklemesi için) |

Ayrıntı: [`docs/ENV.md`](docs/ENV.md)

### 3. Uygulamayı çalıştırma

**Önerilen (PowerShell):**

```powershell
.\scripts\run-local.ps1
```

İlk çalıştırmada Flyway migration'ları uygulanır; `local` profilde demo verileri yüklenir.

Tarayıcı: **http://localhost:8080**

### 4. Demo hesapları

| Kullanıcı | Şifre | Rol |
|-----------|-------|-----|
| `admin` | `password` | Yönetici — tüm modüller |
| `dealer` | `password` | Bayi — Demo Bayi |

> Bu hesaplar yalnızca **`local`** profil seed'indedir. Production ortamında kullanmayın.

---

## Demo ve test

| Amaç | Doküman / komut |
|------|------------------|
| Adım adım sunum / QA senaryoları | [`docs/DEMO.md`](docs/DEMO.md) |
| Güvenlik kontrol listesi | [`docs/SECURITY_CHECKLIST.md`](docs/SECURITY_CHECKLIST.md) |
| Uçtan uca duman testi | `python scripts/test_e2e_smoke.py` |
| Güvenlik otomasyonu | `python scripts/test_security_checklist.py` |

Sunucu çalışırken (`http://localhost:8080`) yukarıdaki Python komutlarını proje kökünden çalıştırın.

---

## Manuel çalıştırma (alternatif)

```powershell
Get-Content .env | ForEach-Object {
  if ($_ -match '^\s*([^#][^=]+)=(.*)$') {
    [Environment]::SetEnvironmentVariable($matches[1].Trim(), $matches[2].Trim(), 'Process')
  }
}
mvn spring-boot:run "-Dspring-boot.run.profiles=local"
```

---

## Derleme

```powershell
mvn compile
```

---

## Proje yapısı

```
src/main/java/.../controller/admin|dealer|report/
src/main/java/.../service/
src/main/java/.../license/LicenseGenerator.java   # placeholder — şirket algoritması
src/main/resources/templates/
src/main/resources/db/migration/
scripts/              # run-local.ps1, otomatik testler
docs/                 # DEMO, ENV, güvenlik, planlama
env.example           # örnek ortam dosyası (commit edilir)
.env                  # gerçek değerler — commit EDİLMEZ
```

---

## Teslim notları (şirket)

1. **LicenseGenerator** — Lisans anahtarı üretimi şu an **placeholder** (HMAC tabanlı demo). Gerçek algoritma şirketten geldiğinde yalnızca `LicenseGenerator.java` güncellenecek; portal akışı hazır.
2. **Demo kullanıcılar** — `admin` / `dealer` ve seed verileri yalnızca `local` profilde.
3. **Gizli dosyalar** — `.env` ve `internal-docs/` asla commit edilmez (`.gitignore`).

---

## Sık sorunlar

| Belirti | Çözüm |
|---------|--------|
| Port 8080 dolu | Önceki `java.exe` sürecini kapatın; veya uygulama zaten çalışıyorsa doğrudan tarayıcıdan açın |
| Login olmuyor | `SPRING_PROFILES_ACTIVE=local`, PostgreSQL ayakta, `.env` dolu |
| Master key hatası | `.env` içinde `LICENSE_PORTAL_MASTER_KEY` tanımlı olsun |
| Admin lisans: bayi seçin | Kuruma en az bir bayi bağlayın; çoklu bayide üretim formunda bayi seçin |
| Bayi ürün göremiyor | Admin → Bayi → Ürün yetkileri |

---

## Dokümantasyon

| Dosya | İçerik |
|-------|--------|
| [`docs/DEMO.md`](docs/DEMO.md) | Uçtan uca admin ve bayi demo senaryoları |
| [`docs/ENV.md`](docs/ENV.md) | Ortam değişkenleri ve gizli bilgi kuralları |
| [`docs/SECURITY_CHECKLIST.md`](docs/SECURITY_CHECKLIST.md) | Rol, izolasyon, secret kontrolleri |
| [`docs/PLANNING.md`](docs/PLANNING.md) | Geliştirme milestone planı |
| [`docs/BRANCHING.md`](docs/BRANCHING.md) | Branch ve PR kuralları |

---

## Lisans

Şirket içi proje — dağıtım koşulları işveren politikasına tabidir.
