# License Portal

Spring Boot + Thymeleaf tabanlı lisans yönetim portalı (Java 17, PostgreSQL, Flyway).

> İş gereksinimleri: `internal-docs/lisans_portalı.md` (git'e alınmaz).

## Gereksinimler

- Java 17
- Maven 3.9+
- PostgreSQL 14+

## Kurulum (local)

### 1. Veritabanı

PostgreSQL'de boş bir veritabanı oluşturun:

```sql
CREATE DATABASE license_portal;
```

### 2. Ortam değişkenleri

```powershell
copy env.example .env
```

`.env` içinde en az şunları doldurun:

| Değişken | Açıklama |
|----------|----------|
| `SPRING_DATASOURCE_URL` | JDBC URL (varsayılan: `jdbc:postgresql://localhost:5432/license_portal`) |
| `SPRING_DATASOURCE_USERNAME` | DB kullanıcısı |
| `SPRING_DATASOURCE_PASSWORD` | DB şifresi |
| `LICENSE_PORTAL_MASTER_KEY` | Ürün secret AES şifrelemesi (üretimde güçlü, rastgele bir değer) |
| `SPRING_PROFILES_ACTIVE` | `local` (seed ve `.env` import için) |

Ayrıntı: [`docs/ENV.md`](docs/ENV.md)

### 3. Uygulamayı çalıştırma

**PowerShell (önerilen):**

```powershell
.\scripts\run-local.ps1
```

**Manuel:**

```powershell
cd c:\path\to\license-portal
Get-Content .env | ForEach-Object {
  if ($_ -match '^\s*([^#][^=]+)=(.*)$') {
    [Environment]::SetEnvironmentVariable($matches[1].Trim(), $matches[2].Trim(), 'Process')
  }
}
mvn spring-boot:run "-Dspring-boot.run.profiles=local"
```

İlk açılışta Flyway migration'ları çalışır; `local` profilinde `db/seed-local.sql` demo kullanıcıları ekler.

Uygulama: **http://localhost:8080**

### 4. Demo giriş

| Kullanıcı | Şifre | Rol |
|-----------|-------|-----|
| `admin` | `password` | ADMIN — tüm modüller |
| `dealer` | `password` | BAYI — Demo Bayi |

> Bu hesaplar yalnızca `local` profil seed'indedir; production'da kullanmayın.

## Hızlı demo akışı

**Admin**

1. Giriş: `admin` / `password`
2. Ürünler → ürün ekle (secret kaydedilir, şifreli)
3. Bayiler → bayi + ürün yetkisi + bayi kullanıcısı
4. Kurumlar → kurum ekle
5. Lisans üret → sonuç ekranından anahtarı kopyala
6. Raporlar → bayi sayısı / süresi dolan-yaklaşan

**Bayi**

1. Giriş: `dealer` / `password`
2. Kurumlarım → kurum ekle
3. Lisans üret (yalnızca yetkili ürünler)
4. Lisanslarım / Raporlar

## Otomatik akış testleri

Sunucu çalışırken (`http://localhost:8080`):

```powershell
python scripts/test_license_day15.py
python scripts/test_license_day16.py
python scripts/test_license_day17.py
python scripts/test_license_day18.py
python scripts/test_license_day19.py
python scripts/test_isolation.py
```

## Proje yapısı (özet)

```
src/main/java/.../controller/admin|dealer/
src/main/java/.../service/
src/main/java/.../repository/
src/main/resources/templates/
src/main/resources/db/migration/
scripts/                    # Akış testleri + run-local.ps1
```

## Dokümanlar

- [`docs/PLANNING.md`](docs/PLANNING.md) — 20 iş günü milestone
- [`docs/BRANCHING.md`](docs/BRANCHING.md) — branch / PR
- [`docs/ENV.md`](docs/ENV.md) — gizli bilgi yönetimi

## Notlar

- Lisans üretim algoritması şirketten netleşene kadar `LicenseGenerator` placeholder (HMAC tabanlı); imza değişince sınıf güncellenir.
- `.env` ve `internal-docs/` commit edilmez.
