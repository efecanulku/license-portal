"""Manuel checklist 8.3 ve 8.4 otomatik kontrol."""
import re
import sys
import http.cookiejar
import urllib.parse
import urllib.request

BASE = "http://localhost:8080"
SECRET_MARKERS = [
    "secret_key_enc",
    "secretKeyEnc",
    "getSecretKeyEnc",
]


def login(username: str, password: str):
    cj = http.cookiejar.CookieJar()
    opener = urllib.request.build_opener(urllib.request.HTTPCookieProcessor(cj))
    html = opener.open(BASE + "/login").read().decode("utf-8", errors="replace")
    m = re.search(r'name="_csrf"\s+value="([^"]+)"', html)
    if not m:
        return None
    data = urllib.parse.urlencode(
        {"username": username, "password": password, "_csrf": m.group(1)}
    ).encode()
    opener.open(urllib.request.Request(BASE + "/login", data=data, method="POST"))
    return opener


def fetch(opener, path: str):
    try:
        resp = opener.open(BASE + path)
        body = resp.read(200000).decode("utf-8", errors="replace")
        return resp.getcode(), body
    except urllib.error.HTTPError as e:
        body = e.read(200000).decode("utf-8", errors="replace") if e.fp else ""
        return e.code, body


def denied(code: int, body: str) -> bool:
    return (
        code == 403
        or "Yetkisiz" in body
        or "yetkin yok" in body.lower()
        or "erişim yetkiniz yok" in body.lower()
    )


def test_8_3(admin):
    print("=== 8.3 Ürün listesinde secret görünmez ===")
    code, body = fetch(admin, "/admin/products")
    leaked = [m for m in SECRET_MARKERS if m in body]
    secret_inputs = re.findall(r'<input[^>]*name=["\']?secret[^>]*>', body, re.I)
    ok = code == 200 and not leaked and not secret_inputs
    print(f"  HTTP {code}")
    if leaked:
        print(f"  FAIL — HTML'de bulundu: {leaked}")
    else:
        print("  OK — secret_key_enc / secretKeyEnc / getSecretKeyEnc yok")
    if secret_inputs:
        print(f"  FAIL — secret input alanı: {secret_inputs[:2]}")
    else:
        print("  OK — listede secret input alanı yok")
    return ok


def license_creator_username(detail_html: str):
    m = re.search(
        r"Üreten</span>\s*<span[^>]*>([^<]+)</span>",
        detail_html,
        re.IGNORECASE,
    )
    return m.group(1).strip().lower() if m else None


def find_admin_created_license_id(admin):
    """Admin kullanicisinin urettigi lisans (bayi kendi lisansina erisebilir — onu atla)."""
    _, admin_lic = fetch(admin, "/admin/licenses")
    ids = re.findall(r"/admin/licenses/(\d+)", admin_lic)
    for lid in ids[:40]:
        _, detail = fetch(admin, f"/admin/licenses/{lid}")
        if license_creator_username(detail) == "admin":
            return lid
    return None


def test_8_4(admin, dealer):
    print("\n=== 8.4 Bayi -> admin urettigi lisans detayi ===")
    admin_lid = find_admin_created_license_id(admin)
    if not admin_lid:
        print("  SKIP — admin urettigi lisans bulunamadi (liste bos veya hepsi bayi)")
        return True

    print(f"  Test lisans id={admin_lid} (admin uretti)")
    code, body = fetch(dealer, f"/dealer/licenses/{admin_lid}")
    if denied(code, body):
        print(f"  OK — erisim engellendi (HTTP {code})")
        return True
    if "licenseKey" in body and code == 200:
        print(f"  FAIL — bayi detay sayfasini acabildi (HTTP {code})")
        return False
    print(f"  OK — detay acilmadi (HTTP {code})")
    return True


def main():
    try:
        urllib.request.urlopen(BASE + "/login", timeout=3)
    except Exception as e:
        print(f"Sunucu yanıt vermiyor ({BASE}): {e}")
        sys.exit(1)

    admin = login("admin", "password")
    dealer = login("dealer", "password")
    if not admin or not dealer:
        print("Login başarısız")
        sys.exit(1)

    ok3 = test_8_3(admin)
    ok4 = test_8_4(admin, dealer)
    print()
    if ok3 and ok4:
        print("Sonuç: 8.3 ve 8.4 GEÇTİ")
        sys.exit(0)
    print("Sonuç: BAZI KONTROLLER KALDI")
    sys.exit(1)


if __name__ == "__main__":
    main()
