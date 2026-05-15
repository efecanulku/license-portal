"""
Gün 16 akış testleri: lisans üretimi, DB kaydı, sonuç ekranı.
Çalıştırma: python scripts/test_license_day16.py
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


def generate_license(opener, base_path: str, customer_id: str, product_id: str):
    code, _, html = fetch(opener, base_path + "/new")
    token = csrf(html)
    if not token:
        return None, html
    valid_until = (date.today() + timedelta(days=400)).isoformat()
    fields = {
        "_csrf": token,
        "customerId": customer_id,
        "productId": product_id,
        "systemKey": "DAY16-FLOW-TEST",
        "licenseOwnerDescription": "otomatik test",
        "validUntil": valid_until,
    }
    data = urllib.parse.urlencode(fields).encode()
    code2, url2, body2 = fetch(opener, base_path + "/new", method="POST", data=data)
    return code2, url2, body2


def main():
    print("=== Gün 16 — Lisans üretim akışı ===\n")

    admin = login("admin", "password")
    dealer = login("dealer", "password")
    ok("login", admin is not None and dealer is not None)
    if not admin or not dealer:
        sys.exit(1)

    print("[1] Admin lisans üretimi")
    _, _, form_html = fetch(admin, "/admin/licenses/new")
    customers = select_options(form_html, "customerId")
    products = select_options(form_html, "productId")
    ok("admin form data", bool(customers and products))

    if customers and products:
        code, url, body = generate_license(admin, "/admin/licenses", customers[0], products[0])
        ok("admin POST redirect", code == 200 and "/admin/licenses/" in url, url)
        ok("admin sonuç ekranı", "Lisans üretildi" in body and "licenseKey" in body)
        ok("admin kopyala butonu", "copyLicenseBtn" in body)
        m = re.search(r"/admin/licenses/(\d+)", url)
        license_id = m.group(1) if m else None
        if license_id:
            code2, _, body2 = fetch(dealer, f"/dealer/licenses/{license_id}")
            same_dealer_view = "Lisans üretildi" in body2 and "Yetkisiz" not in body2
            ok("dealer lisans detayı (aynı bayi ise görünür)", same_dealer_view or "Yetkisiz" in body2)
            code3, _, body3 = fetch(dealer, "/dealer/licenses/999999")
            ok(
                "dealer bilinmeyen lisans engeli",
                "Yetkisiz" in body3 or "bulunamadı" in body3.lower() or "Bulunamadı" in body3 or code3 == 403,
            )

    print("\n[2] Bayi lisans üretimi")
    _, _, dform = fetch(dealer, "/dealer/licenses/new")
    dc = select_options(dform, "customerId")
    dp = select_options(dform, "productId")
    ok("dealer form seçenekleri", bool(dc and dp), f"kurum={len(dc)} ürün={len(dp)}")
    if dc and dp:
        code, url, body = generate_license(dealer, "/dealer/licenses", dc[0], dp[0])
        ok("dealer POST sonuç", code == 200 and "/dealer/licenses/" in url, url)
        ok("dealer lisans anahtarı", "font-monospace" in body or "licenseKey" in body)

    print("\n[3] Veritabanı")
    try:
        import os
        import subprocess

        env = os.environ.copy()
        env_file = os.path.join(os.path.dirname(__file__), "..", ".env")
        if os.path.isfile(env_file):
            with open(env_file, encoding="utf-8") as f:
                for line in f:
                    if "=" in line and not line.strip().startswith("#"):
                        k, v = line.strip().split("=", 1)
                        env[k] = v
        url = env.get("SPRING_DATASOURCE_URL", "")
        m = re.search(r"postgresql://([^:]+):(\d+)/(\w+)", url.replace("jdbc:", ""))
        if m:
            host, port, db = m.group(1), m.group(2), m.group(3)
            env["PGPASSWORD"] = env.get("SPRING_DATASOURCE_PASSWORD", "")
            r = subprocess.run(
                ["psql", "-h", host, "-p", port, "-U", env.get("SPRING_DATASOURCE_USERNAME", "postgres"),
                 "-d", db, "-tAc", "SELECT COUNT(*) FROM licenses WHERE system_key='DAY16-FLOW-TEST';"],
                capture_output=True, text=True, timeout=10, env=env,
            )
            if r.returncode == 0:
                count = r.stdout.strip()
                ok("licenses tablosuna kayıt", int(count) >= 1, f"count={count}")
            else:
                print(f"  SKIP psql: {r.stderr.strip()[:100]}")
    except FileNotFoundError:
        print("  SKIP psql")
    except Exception as e:
        print(f"  SKIP psql: {e}")

    print(f"\n=== Sonuç: {PASS} geçti, {FAIL} kaldı ===")
    sys.exit(1 if FAIL else 0)


if __name__ == "__main__":
    main()
