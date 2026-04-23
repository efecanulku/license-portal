# Env / Gizli Bilgi Yönetimi

## Altın kurallar
- `.env` ve benzeri dosyalar **commit edilmez**.
- Şirketten gelen gizli dokümanlar `internal-docs/` altında tutulur ve **commit edilmez**.
- Şifreler, master key, token vb. **kod içinde hardcode edilmez**.

## Local geliştirme
- Repo içinde örnek değerler için `env.example` var.
- Gerçek değerleri:
  - Windows'ta sistem environment variable olarak, veya
  - (tercihen) `.env` dosyasında tut.

## Uygulama için kritik değişkenler
- `LICENSE_PORTAL_MASTER_KEY`: ürün secret AES encrypt/decrypt için zorunlu.
- `SPRING_DATASOURCE_*`: DB bağlantısı.

## “Yanlışlıkla sızdırmayı” önleme checklist
- `git status` çıktısında `.env` veya `internal-docs/` altında dosya görürsen: **dur**.
- Push öncesi hızlı kontrol:
  - `.gitignore` doğru mu?
  - PR diff içinde anahtar/şifre var mı?

