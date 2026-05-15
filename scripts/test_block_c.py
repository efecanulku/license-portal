"""
Blok C — Sayfalama, şifre değiştirme, ana sayfa uyarısı, Chart.js.
Çalıştırma: python scripts/test_block_c.py
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
        body = resp.read(150000).decode("utf-8", errors="replace")
        return resp.getcode(), body
    except urllib.error.HTTPError as e:
        body = e.read(150000).decode("utf-8", errors="replace") if e.fp else ""
        return e.code, body


def main():
    print("=== Blok C — Stabilizasyon ===\n")

    admin = login("admin", "password")
    dealer = login("dealer", "password")
    ok("login", admin is not None and dealer is not None)
    if not admin or not dealer:
        sys.exit(1)

    print("[1] Lisans listesi sayfalama")
    for path in ("/admin/licenses", "/dealer/licenses"):
        code, body = fetch(admin if "admin" in path else dealer, path)
        ok(f"list {path}", code == 200 and "kayıt" in body, f"HTTP {code}")
        code2, body2 = fetch(admin if "admin" in path else dealer, path + "?page=0")
        ok(f"list page param {path}", code2 == 200, f"HTTP {code2}")

    print("\n[2] Şifre değiştirme sayfası")
    for opener, role in ((admin, "admin"), (dealer, "dealer")):
        code, body = fetch(opener, "/account/password")
        ok(f"password form {role}", code == 200 and "Şifre değiştir" in body and "currentPassword" in body)

    print("\n[3] Ana sayfa süre uyarısı alanı")
    code, body = fetch(admin, "/")
    ok("home admin", code == 200 and "License Portal" in body)
    ok(
        "home has expiring context or report link",
        "Süre raporuna git" in body or "expiring" in body.lower() or "rapor" in body.lower(),
    )

    print("\n[4] Demo dağılımı Chart.js")
    code, body = fetch(admin, "/admin/reports/demo-distribution")
    ok(
        "demo chart admin",
        code == 200 and "chart.js" in body.lower() and "demoChart" in body,
        f"HTTP {code}",
    )
    code, body = fetch(dealer, "/dealer/reports/demo-distribution")
    ok(
        "demo chart dealer",
        code == 200 and "chart.js" in body.lower() and "demoChart" in body,
        f"HTTP {code}",
    )

    print("\n[5] Geçersiz HTML etiketi yok (motion)")
    for path in (
        "/admin/licenses",
        "/dealer/licenses",
        "/account/password",
        "/admin/reports/demo-distribution",
    ):
        _, body = fetch(admin, path)
        ok(f"no motion tag {path}", "<motion" not in body.lower())

    print(f"\n=== Sonuç: {PASS} OK, {FAIL} FAIL ===")
    sys.exit(1 if FAIL else 0)


if __name__ == "__main__":
    main()
