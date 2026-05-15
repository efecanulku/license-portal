"""
Gün 18 akış testleri: sonuç ekranı, kopyalama, form doğrulama ve hata mesajları.
Çalıştırma: python scripts/test_license_day18.py
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


def post_generate(opener, base_path: str, fields: dict):
    code, _, html = fetch(opener, base_path + "/new")
    token = csrf(html)
    if not token:
        return None, "", html
    fields = {"_csrf": token, **fields}
    data = urllib.parse.urlencode(fields).encode()
    code2, url2, body2 = fetch(opener, base_path + "/new", method="POST", data=data)
    return code2, url2, body2


def main():
    print("=== Gün 18 — Sonuç ekranı + kopyalama + UX ===\n")

    admin = login("admin", "password")
    ok("login", admin is not None)
    if not admin:
        sys.exit(1)

    print("[1] Sonuç ekranı (üretim sonrası)")
    _, _, form_html = fetch(admin, "/admin/licenses/new")
    customers = select_options(form_html, "customerId")
    products = select_options(form_html, "productId")
    if customers and products:
        valid_until = (date.today() + timedelta(days=400)).isoformat()
        code, url, body = post_generate(
            admin,
            "/admin/licenses",
            {
                "customerId": customers[0],
                "productId": products[0],
                "systemKey": "DAY18-RESULT-TEST",
                "validUntil": valid_until,
            },
        )
        ok("redirect sonuç", code == 200 and "/admin/licenses/" in url, url)
        ok("başarı mesajı", "Lisans üretildi" in body and "başarıyla oluşturuldu" in body)
        ok("kopyala butonu", "copyLicenseBtn" in body and "license-copy.js" in body)
        ok("aria etiket", 'aria-label="Lisans anahtarını panoya kopyala"' in body)
        ok("vurgulu kart", "border-success" in body)

    print("\n[2] Form doğrulama (boş gönderim)")
    code, _, body = post_generate(admin, "/admin/licenses", {})
    ok("boş form hata", "Lütfen formu kontrol edin" in body or "Kurum seçmelisiniz" in body)
    ok("is-invalid", "is-invalid" in body)

    print("\n[3] Geçmiş tarih")
    if customers and products:
        past = (date.today() - timedelta(days=1)).isoformat()
        _, _, body = post_generate(
            admin,
            "/admin/licenses",
            {
                "customerId": customers[0],
                "productId": products[0],
                "systemKey": "DAY18-PAST",
                "validUntil": past,
            },
        )
        ok(
            "geçmiş tarih reddi",
            "bugün veya sonrası" in body.lower() or "geçerlilik" in body.lower(),
        )

    print("\n[4] Statik kopyalama scripti")
    code, _, body = fetch(admin, "/js/license-copy.js")
    ok("license-copy.js", code == 200 and "initLicenseCopy" in body or "clipboard" in body)

    print(f"\n=== Sonuç: {PASS} geçti, {FAIL} kaldı ===")
    sys.exit(1 if FAIL else 0)


if __name__ == "__main__":
    main()
