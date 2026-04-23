# Branching / PR Akışı

## Hedef
- `main`: her zaman deploy edilebilir, stabil.
- `develop`: günlük entegrasyon (opsiyonel ama staj projelerinde çok faydalı).
- `feature/*`: tek bir iş paketi (örn. `feature/security-login`).
- `fix/*`: bug fix.

## Önerilen akış
1. `main` → `develop` oluştur.
2. Her iş için `develop` üstünden `feature/*` aç.
3. İş bitince PR ile `develop`'a merge et.
4. Milestone sonunda `develop` → `main` PR/merge.

## PR kuralları (hafif ama disiplinli)
- Küçük PR: tek amaç, 200–500 satır civarı hedef.
- PR açıklaması:
  - **Summary**: 2–3 madde
  - **Test plan**: nasıl test edildi?
- Gizli bilgi kontrolü: `.env`, `internal-docs/`, anahtarlar kesinlikle yok.

## Örnek branch isimleri
- `feature/flyway-migrations`
- `feature/security-form-login`
- `feature/admin-product-crud`
- `feature/license-generation`

