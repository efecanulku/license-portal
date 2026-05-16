"""
Gün 19 akış testleri: raporlama (bayi sayısı, süresi dolan/yaklaşan).
Çalıştırma: python scripts/test_license_day19.py
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


def main():
    print("=== Gün 19 — Raporlama ===\n")

    admin = login("admin", "password")
    dealer = login("dealer", "password")
    ok("login", admin is not None and dealer is not None)
    if not admin or not dealer:
        sys.exit(1)

    print("[1] Admin raporları")
    c1, b1 = fetch(admin, "/reports")
    ok("admin rapor index", c1 == 200 and "Bayi bazlı" in b1)
    c2, b2 = fetch(admin, "/reports/dealer-count")
    ok("admin bayi sayısı", c2 == 200 and "Lisans sayısı" in b2 and "Toplam" in b2)
    c3, b3 = fetch(admin, "/reports/expiring")
    ok("admin süresi dolan", c3 == 200 and "Süresi dolan" in b3 and "withinDays" in b3)
    c4, b4 = fetch(admin, "/reports/expiring?status=EXPIRED&withinDays=30")
    ok("admin expiring filtre", c4 == 200 and "EXPIRED" in b4 or "Süresi doldu" in b4)

    print("\n[2] Bayi raporları")
    c5, b5 = fetch(dealer, "/reports")
    ok("dealer rapor index", c5 == 200 and "Lisans özetim" in b5)
    c6, b6 = fetch(dealer, "/reports/dealer-count")
    ok("dealer özet", c6 == 200 and "Lisans sayısı" in b6)
    c7, b7 = fetch(dealer, "/reports/expiring?status=ALL&withinDays=30")
    ok("dealer expiring", c7 == 200 and "Yalnızca sizin" in b7)

    print("\n[3] Yetki")
    c8, b8 = fetch(dealer, "/reports/product-count")
    ok("dealer admin-only rapora erişemez", c8 == 403 or "Yetkisiz" in b8 or "403" in b8)

    print(f"\n=== Sonuç: {PASS} geçti, {FAIL} kaldı ===")
    sys.exit(1 if FAIL else 0)


if __name__ == "__main__":
    main()
