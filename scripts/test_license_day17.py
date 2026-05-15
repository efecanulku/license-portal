"""
Gün 17: lisans listeleme, filtreler, detay, bayi izolasyonu.
Çalıştırma: python scripts/test_license_day17.py
"""
import re
import sys
import http.cookiejar
import urllib.parse
import urllib.request

BASE = "http://localhost:8080"
PASS = FAIL = 0


def ok(name, cond, detail=""):
    global PASS, FAIL
    if cond:
        PASS += 1
        print(f"  OK  {name}" + (f" — {detail}" if detail else ""))
    else:
        FAIL += 1
        print(f" FAIL {name}" + (f" — {detail}" if detail else ""))


def login(user, pwd):
    cj = http.cookiejar.CookieJar()
    op = urllib.request.build_opener(urllib.request.HTTPCookieProcessor(cj))
    html = op.open(BASE + "/login").read().decode("utf-8", errors="replace")
    m = re.search(r'name="_csrf"\s+value="([^"]+)"', html)
    if not m:
        return None
    data = urllib.parse.urlencode({"username": user, "password": pwd, "_csrf": m.group(1)}).encode()
    op.open(urllib.request.Request(BASE + "/login", data=data, method="POST"))
    return op


def fetch(op, path):
    try:
        r = op.open(BASE + path)
        return r.getcode(), r.read(120000).decode("utf-8", errors="replace")
    except urllib.error.HTTPError as e:
        return e.code, (e.read(120000).decode("utf-8", errors="replace") if e.fp else "")


def count_table_rows(html):
    return len(re.findall(r'btn btn-sm btn-outline-primary[^>]*>Detay</a>', html))


def main():
    print("=== Gün 17 — Lisans listeleme testleri ===\n")

    admin = login("admin", "password")
    dealer = login("dealer", "password")
    ok("login", admin and dealer)
    if not admin or not dealer:
        sys.exit(1)

    print("[1] Admin liste ve filtreler")
    c, html = fetch(admin, "/admin/licenses")
    ok("admin GET /admin/licenses", c == 200 and "Lisanslar" in html)
    rows_all = count_table_rows(html)
    ok("admin listede kayit var", rows_all >= 1, f"rows={rows_all}")

    c2, html2 = fetch(admin, "/admin/licenses?dealerId=1")
    rows_dealer = count_table_rows(html2)
    ok("admin bayi filtresi", rows_dealer >= 1 and rows_dealer <= rows_all, f"filtered={rows_dealer}")

    c3, html3 = fetch(admin, "/admin/licenses/1")
    ok("admin detay", c3 == 200 and "licenseKey" in html3 and "Kopyala" in html3)

    print("\n[2] Bayi — kendi lisanslari")
    c4, html4 = fetch(dealer, "/dealer/licenses")
    ok("dealer GET /dealer/licenses", c4 == 200 and "Lisanslar" in html4)
    rows_bayi = count_table_rows(html4)
    ok("bayi listesi admin listesinden kucuk veya esit", rows_bayi <= rows_all, f"bayi={rows_bayi} admin={rows_all}")

    c5, html5 = fetch(dealer, "/dealer/licenses/3")
    blocked = "Yetkisiz" in html5 or "bulunamad" in html5.lower() or c5 == 403
    ok("bayi admin lisansina erisemez (id=3)", blocked)

    if rows_bayi > 0:
        m = re.search(r'/dealer/licenses/(\d+)', html4)
        if m:
            own_id = m.group(1)
            c6, html6 = fetch(dealer, f"/dealer/licenses/{own_id}")
            ok("bayi kendi lisans detayi", c6 == 200 and "licenseKey" in html6, f"id={own_id}")

    print("\n[3] Erisim engeli")
    c7, html7 = fetch(dealer, "/admin/licenses")
    ok("bayi admin listesi engelli", "Yetkisiz" in html7 or c7 == 403)

    print(f"\n=== Sonuc: {PASS} gecti, {FAIL} kaldi ===")
    sys.exit(1 if FAIL else 0)


if __name__ == "__main__":
    main()
