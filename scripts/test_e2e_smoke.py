"""
Gün 20 — Uçtan uca duman (smoke) testi.
Temel admin/bayi sayfaları + lisans üretim mutlu yolu (veri varsa).

Çalıştırma: python scripts/test_e2e_smoke.py
"""
import re
import sys
import http.cookiejar
import urllib.parse
import urllib.request
from datetime import date, timedelta

BASE = "http://localhost:8080"
PASS = 0
FAIL = 0


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


def fetch(opener, path: str, method="GET", data=None):
    req = urllib.request.Request(BASE + path, data=data, method=method)
    try:
        resp = opener.open(req)
        body = resp.read(120000).decode("utf-8", errors="replace")
        return resp.getcode(), resp.geturl(), body
    except urllib.error.HTTPError as e:
        body = e.read(120000).decode("utf-8", errors="replace") if e.fp else ""
        return e.code, e.geturl(), body


def csrf(html: str):
    m = re.search(r'name="_csrf"\s+value="([^"]+)"', html)
    return m.group(1) if m else None


def select_options(html: str, field_id: str):
    block = re.search(
        rf'<select[^>]*id="{field_id}"[^>]*>(.*?)</select>',
        html,
        re.DOTALL | re.IGNORECASE,
    )
    if not block:
        return []
    return re.findall(r'<option[^>]*value="(\d+)"[^>]*>', block.group(1))


def page_ok(body: str, *markers: str) -> bool:
    return all(m in body for m in markers)


def generate_license(opener, base_path: str, system_key: str):
    code, _, html = fetch(opener, base_path + "/new")
    token = csrf(html)
    customers = select_options(html, "customerId")
    products = select_options(html, "productId")
    if not token or not customers or not products:
        return False, "form verisi yok"
    valid_until = (date.today() + timedelta(days=365)).isoformat()
    fields = {
        "_csrf": token,
        "customerId": customers[0],
        "productId": products[0],
        "systemKey": system_key,
        "validUntil": valid_until,
    }
    code2, url2, body2 = fetch(
        opener, base_path + "/new", method="POST", data=urllib.parse.urlencode(fields).encode()
    )
    ok_gen = code2 == 200 and "/licenses/" in url2 and (
        "Lisans üretildi" in body2 or "licenseKey" in body2
    )
    return ok_gen, url2 if ok_gen else f"HTTP {code2}"


def main():
    print("=== Gün 20 — E2E smoke ===\n")

    print("[1] Sunucu")
    try:
        code, _, body = fetch(
            urllib.request.build_opener(urllib.request.HTTPCookieProcessor(http.cookiejar.CookieJar())),
            "/login",
        )
        ok("sunucu yanıt", code == 200 and "Giriş" in body or "login" in body.lower())
    except Exception as e:
        ok("sunucu yanıt", False, str(e))
        sys.exit(1)

    print("\n[2] Admin modülleri")
    admin = login("admin", "password")
    ok("admin login", admin is not None)
    if not admin:
        sys.exit(1)

    admin_pages = [
        ("/", ["License Portal"]),
        ("/admin/products", ["Ürünler"]),
        ("/admin/dealers", ["Bayiler"]),
        ("/admin/customers", ["Kurum"]),
        ("/admin/licenses", ["Lisans"]),
        ("/admin/reports", ["Raporlar", "Bayi bazlı"]),
    ]
    for path, markers in admin_pages:
        _, _, body = fetch(admin, path)
        ok(f"admin GET {path}", page_ok(body, *markers))

    print("\n[3] Admin lisans üretimi")
    gen_ok, detail = generate_license(admin, "/admin/licenses", "E2E-SMOKE-ADMIN")
    ok("admin lisans üret", gen_ok, detail)

    print("\n[4] Bayi modülleri")
    dealer = login("dealer", "password")
    ok("dealer login", dealer is not None)
    if not dealer:
        sys.exit(1)

    dealer_pages = [
        ("/dealer/customers", ["Kurum"]),
        ("/dealer/licenses", ["Lisans"]),
        ("/dealer/reports", ["Raporlar"]),
    ]
    for path, markers in dealer_pages:
        _, _, body = fetch(dealer, path)
        ok(f"dealer GET {path}", page_ok(body, *markers))

    print("\n[5] Bayi lisans üretimi")
    gen_ok2, detail2 = generate_license(dealer, "/dealer/licenses", "E2E-SMOKE-DEALER")
    ok("dealer lisans üret", gen_ok2, detail2)

    print("\n[6] Çapraz kontrol")
    _, _, body = fetch(dealer, "/admin/products")
    ok("dealer admin ürün engeli", "Yetkisiz" in body)

    print(f"\n=== Sonuç: {PASS} geçti, {FAIL} kaldı ===")
    print("Manuel tam senaryo: docs/DEMO.md")
    sys.exit(1 if FAIL else 0)


if __name__ == "__main__":
    main()
