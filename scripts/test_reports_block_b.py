"""
Blok B — Kalan raporlar (ürün, müşteri, demo dağılımı).
Çalıştırma: python scripts/test_reports_block_b.py
"""
import re
import sys
import http.cookiejar
import urllib.parse
import urllib.request

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


def fetch(opener, path: str):
    req = urllib.request.Request(BASE + path)
    try:
        resp = opener.open(req)
        body = resp.read(120000).decode("utf-8", errors="replace")
        return resp.getcode(), body
    except urllib.error.HTTPError as e:
        body = e.read(120000).decode("utf-8", errors="replace") if e.fp else ""
        return e.code, body


def denied(code: int, body: str) -> bool:
    return code == 403 or "Yetkisiz" in body


def main():
    print("=== Blok B — Ek raporlar ===\n")

    admin = login("admin", "password")
    dealer = login("dealer", "password")
    ok("login", admin is not None and dealer is not None)
    if not admin or not dealer:
        sys.exit(1)

    print("[1] Admin raporları")
    paths = [
        ("/admin/reports/product-count", ["Ürün bazlı", "Lisans sayısı"]),
        ("/admin/reports/customer-summary", ["Müşteri bazlı", "Kurum"]),
        ("/admin/reports/demo-distribution", ["Demo", "üretim"]),
    ]
    for path, markers in paths:
        code, body = fetch(admin, path)
        ok(f"admin {path}", code == 200 and all(m in body for m in markers), f"HTTP {code}")

    print("\n[2] Bayi raporları")
    paths_d = [
        ("/dealer/reports/customer-summary", ["Müşteri bazlı", "Kurum"]),
        ("/dealer/reports/demo-distribution", ["Demo", "Toplam"]),
    ]
    for path, markers in paths_d:
        code, body = fetch(dealer, path)
        ok(f"dealer {path}", code == 200 and all(m in body for m in markers), f"HTTP {code}")

    print("\n[3] Bayi ürün raporu engeli")
    code, body = fetch(dealer, "/admin/reports/product-count")
    ok("dealer product-count engelli", denied(code, body) or code == 403)

    print("\n[4] Rapor index linkleri")
    _, body = fetch(admin, "/admin/reports")
    ok("admin index 5 rapor", body.count("list-group-item") >= 5)
    _, body2 = fetch(dealer, "/dealer/reports")
    ok("dealer index 4 rapor", body2.count("list-group-item") >= 4)

    print(f"\n=== Sonuç: {PASS} geçti, {FAIL} kaldı ===")
    sys.exit(1 if FAIL else 0)


if __name__ == "__main__":
    main()
