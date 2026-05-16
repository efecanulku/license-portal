"""
Gün 20 — Güvenlik checklist (otomatik).
Çalıştırma: python scripts/test_security_checklist.py
"""
import re
import sys
import http.cookiejar
import urllib.parse
import urllib.request

BASE = "http://localhost:8080"
PASS = 0
FAIL = 0

ADMIN_PATHS = [
    "/admin/products",
    "/admin/dealers",
    "/admin/licenses",
]

DEALER_PATHS = [
    "/dealer/customers",
    "/dealer/licenses",
]

SECRET_MARKERS = [
    "secret_key_enc",
    "secretKeyEnc",
    "getSecretKeyEnc",
]


def ok(name: str, cond: bool, detail: str = ""):
    global PASS, FAIL
    if cond:
        PASS += 1
        print(f"  OK  {name}" + (f" — {detail}" if detail else ""))
    else:
        FAIL += 1
        print(f" FAIL {name}" + (f" — {detail}" if detail else ""))


def login(username: str, password: str):
    cj = http.cookiejar.CookieJar()
    opener = urllib.request.build_opener(urllib.request.HTTPCookieProcessor(cj))
    html = opener.open(BASE + "/login").read().decode("utf-8", errors="replace")
    m = re.search(r'name="_csrf"\s+value="([^"]+)"', html)
    if not m:
        return None
    token = m.group(1)
    data = urllib.parse.urlencode(
        {"username": username, "password": password, "_csrf": token}
    ).encode()
    opener.open(urllib.request.Request(BASE + "/login", data=data, method="POST"))
    return opener


def fetch(opener, path: str):
    req = urllib.request.Request(BASE + path)
    try:
        resp = opener.open(req)
        body = resp.read(120000).decode("utf-8", errors="replace")
        return resp.getcode(), resp.geturl(), body
    except urllib.error.HTTPError as e:
        body = e.read(120000).decode("utf-8", errors="replace") if e.fp else ""
        return e.code, e.geturl(), body


def fetch_anon(path: str):
    req = urllib.request.Request(BASE + path)
    try:
        resp = urllib.request.urlopen(req)
        body = resp.read(120000).decode("utf-8", errors="replace")
        return resp.getcode(), resp.geturl(), body
    except urllib.error.HTTPError as e:
        body = e.read(120000).decode("utf-8", errors="replace") if e.fp else ""
        return e.code, e.geturl(), body


def denied(code: int, body: str) -> bool:
    return code == 403 or "Yetkisiz" in body or "yetkin yok" in body.lower()


def secret_leaked(body: str) -> bool:
    return any(m in body for m in SECRET_MARKERS)


def main():
    print("=== Gün 20 — Güvenlik checklist ===\n")

    print("[1] Kimlik doğrulama")
    code, url, body = fetch_anon("/admin/products")
    needs_login = "login" in url.lower() or "Giriş" in body or code in (401, 403)
    ok("anon /admin/products korumalı", needs_login, f"HTTP {code} url={url}")

    admin = login("admin", "password")
    dealer = login("dealer", "password")
    ok("demo login", admin is not None and dealer is not None)
    if not admin or not dealer:
        sys.exit(1)

    print("\n[2] Rol erişimi — BAYI admin URL")
    for path in ADMIN_PATHS:
        code, _, body = fetch(dealer, path)
        ok(f"dealer {path} engelli", denied(code, body), f"HTTP {code}")

    print("\n[3] Rol erişimi — ADMIN dealer URL")
    for path in DEALER_PATHS:
        code, _, body = fetch(admin, path)
        ok(f"admin {path} engelli", denied(code, body), f"HTTP {code}")

    print("\n[4] Bayi izolasyonu")
    _, _, dc_body = fetch(dealer, "/dealer/customers")
    ok(
        "dealer kurum listesi kendi",
        "Bayinize bağlı kurumlar" in dc_body or "Kurumlarım" in dc_body,
    )

    code, _, body = fetch(dealer, "/dealer/customers/1/edit")
    blocked = denied(code, body) or "Kurum bulunamadı" in body
    ok("dealer başka kurum düzenleyemez (id=1)", blocked, f"HTTP {code}")

    _, _, admin_lic = fetch(admin, "/admin/licenses")
    admin_ids = re.findall(r"/admin/licenses/(\d+)", admin_lic)
    blocked = False
    for lid in admin_ids[:30]:
        code, _, body = fetch(dealer, f"/dealer/licenses/{lid}")
        if denied(code, body) or "erişim yetkiniz yok" in body.lower():
            ok("dealer admin lisans detayı engelli", True, f"id={lid} HTTP {code}")
            blocked = True
            break
    if admin_ids and not blocked:
        ok(
            "dealer admin lisans detayı engelli",
            False,
            "bayi listedeki örnek lisansların tamamına erişebildi",
        )
    elif not admin_ids:
        print("  SKIP dealer lisans detayı — admin listede kayıt yok")

    print("\n[5] Secret görünmezliği")
    _, _, prod_list = fetch(admin, "/admin/products")
    ok("ürün listesinde secret_key_enc yok", not secret_leaked(prod_list))
    code, _, body = fetch(dealer, "/admin/products/new")
    ok("dealer ürün ekleme engelli", denied(code, body), f"HTTP {code}")

    print("\n[6] Rapor izolasyonu")
    code, _, body = fetch(dealer, "/reports/product-count")
    ok("dealer ürün sayısı raporu engelli", denied(code, body), f"HTTP {code}")
    code2, _, body2 = fetch(dealer, "/reports")
    ok("dealer kendi raporları açılır", code2 == 200 and "Raporlar" in body2)

    print(f"\n=== Sonuç: {PASS} geçti, {FAIL} kaldı ===")
    print("Manuel maddeler: docs/SECURITY_CHECKLIST.md §5–6")
    sys.exit(1 if FAIL else 0)


if __name__ == "__main__":
    main()
