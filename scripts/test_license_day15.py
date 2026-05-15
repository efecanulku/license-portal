"""
Gün 15 akış testleri: licenses şeması, form GET/POST, rol bazlı ürün listesi.
Çalıştırma: python scripts/test_license_day15.py
Önkoşul: http://localhost:8080 ayakta, seed kullanıcılar (admin/dealer, password).
"""
import re
import sys
import http.cookiejar
import urllib.error
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
        return None, None
    token = m.group(1)
    data = urllib.parse.urlencode(
        {"username": username, "password": password, "_csrf": token}
    ).encode()
    opener.open(urllib.request.Request(BASE + "/login", data=data, method="POST"))
    return opener, token


def fetch(opener, path: str, method="GET", data=None):
    req = urllib.request.Request(BASE + path, data=data, method=method)
    try:
        resp = opener.open(req)
        body = resp.read(120000).decode("utf-8", errors="replace")
        return resp.getcode(), resp.geturl(), body
    except urllib.error.HTTPError as e:
        body = e.read(120000).decode("utf-8", errors="replace") if e.fp else ""
        return e.code, e.geturl(), body


def csrf_from_html(html: str):
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
    return re.findall(r'<option[^>]*value="(\d+)"[^>]*>([^<]*)</option>', block.group(1))


def post_license_form(opener, path: str, fields: dict):
    code, _, html = fetch(opener, path)
    token = csrf_from_html(html)
    if not token:
        return code, html, False
    payload = {"_csrf": token, **fields}
    data = urllib.parse.urlencode(payload).encode()
    code2, url2, body2 = fetch(
        opener, path, method="POST", data=data
    )
    return code2, body2, "flashSuccess" in body2 or "Form doğrulandı" in body2


def main():
    print("=== Gün 15 — Lisans form akış testleri ===\n")

    # --- Erişim ---
    print("[1] Sayfa erişimi")
    admin_op, _ = login("admin", "password")
    ok("admin login", admin_op is not None)
    dealer_op, _ = login("dealer", "password")
    ok("dealer login", dealer_op is not None)

    if not admin_op or not dealer_op:
        print("\nUygulama veya giriş başarısız; test durduruldu.")
        sys.exit(1)

    code_a, _, body_a = fetch(admin_op, "/admin/licenses/new")
    ok("admin GET /admin/licenses/new", code_a == 200 and "Lisans Üret" in body_a)
    ok("admin form alanları", all(
        x in body_a for x in ["customerId", "productId", "systemKey", "validUntil", "demo"]
    ))
    ok("admin restrictedProducts yok", "yetkili olduğunuz aktif ürünler" not in body_a)

    code_d, _, body_d = fetch(dealer_op, "/dealer/licenses/new")
    ok("dealer GET /dealer/licenses/new", code_d == 200 and "Lisans Üret" in body_d)
    ok("dealer restrictedProducts metni", "yetkili olduğunuz aktif ürünler" in body_d.lower())

    code_cross, _, body_cross = fetch(dealer_op, "/admin/licenses/new")
    forbidden = "Yetkisiz" in body_cross or code_cross == 403
    ok("dealer admin lisans sayfası engelli", forbidden)

    code_cross2, _, body_cross2 = fetch(admin_op, "/dealer/licenses/new")
    forbidden2 = "Yetkisiz" in body_cross2 or code_cross2 == 403
    ok("admin dealer lisans sayfası engelli", forbidden2)

    # --- Dropdown içerikleri ---
    print("\n[2] Ürün / kurum listeleri")
    admin_products = select_options(body_a, "productId")
    admin_customers = select_options(body_a, "customerId")
    dealer_products = select_options(body_d, "productId")
    dealer_customers = select_options(body_d, "customerId")

    ok("admin en az bir ürün", len(admin_products) >= 1, f"count={len(admin_products)}")
    ok("admin en az bir kurum", len(admin_customers) >= 1, f"count={len(admin_customers)}")
    ok("dealer en az bir kurum (own)", len(dealer_customers) >= 1, f"count={len(dealer_customers)}")

    if admin_products and dealer_products:
        admin_ids = {p[0] for p in admin_products}
        dealer_ids = {p[0] for p in dealer_products}
        ok(
            "dealer ürün ⊆ admin (yetki filtresi)",
            dealer_ids <= admin_ids,
            f"dealer={len(dealer_ids)} admin={len(admin_ids)}",
        )
        if dealer_ids != admin_ids:
            ok("dealer ürün sayısı ≤ admin", len(dealer_ids) <= len(admin_ids))

    # --- Geçerli POST ---
    print("\n[3] Form POST")
    valid_until = (date.today() + timedelta(days=365)).isoformat()
    if admin_products and admin_customers:
        fields = {
            "customerId": admin_customers[0][0],
            "productId": admin_products[0][0],
            "systemKey": "DAY15-TEST-KEY",
            "licenseOwnerDescription": "otomatik test",
            "validUntil": valid_until,
        }
        code_p, body_p, success = post_license_form(
            admin_op, "/admin/licenses/new", fields
        )
        ok("admin geçerli POST", code_p == 200 and success, f"http={code_p}")
        ok("licenses tablosuna yazılmamalı (Gün 16)", "license_key" not in body_p.lower() or "DAY15-GENERATED" not in body_p)

    if dealer_products and dealer_customers:
        fields_d = {
            "customerId": dealer_customers[0][0],
            "productId": dealer_products[0][0],
            "systemKey": "DAY15-DEALER-KEY",
            "validUntil": valid_until,
        }
        _, body_dp, success_d = post_license_form(
            dealer_op, "/dealer/licenses/new", fields_d
        )
        ok("dealer geçerli POST (yetkili ürün)", success_d)

    # --- Geçersiz POST ---
    print("\n[4] Validasyon")
    if admin_customers and admin_products:
        bad = {
            "customerId": admin_customers[0][0],
            "productId": admin_products[0][0],
            "systemKey": "",
            "validUntil": valid_until,
        }
        _, body_bad, success_bad = post_license_form(admin_op, "/admin/licenses/new", bad)
        ok("boş systemKey reddedilir", not success_bad and ("systemKey" in body_bad or "boş" in body_bad.lower() or "must not be blank" in body_bad.lower()))

    if admin_products and admin_customers and dealer_products:
        # Bayi, yetkisiz ürün seçerse (admin listesinden farklı id)
        unauthorized = None
        dealer_set = {p[0] for p in dealer_products}
        for pid, _ in admin_products:
            if pid not in dealer_set:
                unauthorized = pid
                break
        if unauthorized and dealer_customers:
            bad_d = {
                "customerId": dealer_customers[0][0],
                "productId": unauthorized,
                "systemKey": "HACK-KEY",
                "validUntil": valid_until,
            }
            _, body_ud, success_ud = post_license_form(
                dealer_op, "/dealer/licenses/new", bad_d
            )
            ok(
                "dealer yetkisiz ürün reddedilir",
                not success_ud and ("geçersiz" in body_ud.lower() or "Yetkisiz" in body_ud or "yetkiniz" in body_ud.lower()),
            )
        else:
            print("  SKIP dealer yetkisiz ürün (tüm aktif ürünler bayide yetkili)")

    # --- DB: licenses boş kalmalı ---
    print("\n[5] Veritabanı (psql)")
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
            user = env.get("SPRING_DATASOURCE_USERNAME", "postgres")
            pw = env.get("SPRING_DATASOURCE_PASSWORD", "")
            env["PGPASSWORD"] = pw
            r = subprocess.run(
                [
                    "psql",
                    "-h", host,
                    "-p", port,
                    "-U", user,
                    "-d", db,
                    "-tAc",
                    "SELECT COUNT(*) FROM licenses; SELECT version FROM flyway_schema_history ORDER BY installed_rank DESC LIMIT 1;",
                ],
                capture_output=True,
                text=True,
                timeout=10,
                env=env,
            )
            if r.returncode == 0:
                lines = [x.strip() for x in r.stdout.strip().split("\n") if x.strip()]
                count = lines[0] if lines else "?"
                flyway = lines[1] if len(lines) > 1 else "?"
                ok("flyway schema v6", flyway == "6", f"version={flyway}")
                ok("licenses kayıt yok (Gün 15)", count == "0", f"count={count}")
            else:
                print(f"  SKIP psql: {r.stderr.strip()[:120]}")
        else:
            print("  SKIP psql: JDBC URL parse edilemedi")
    except FileNotFoundError:
        print("  SKIP psql: psql yüklü değil")
    except Exception as e:
        print(f"  SKIP psql: {e}")

    print(f"\n=== Sonuç: {PASS} geçti, {FAIL} kaldı ===")
    sys.exit(1 if FAIL else 0)


if __name__ == "__main__":
    main()
