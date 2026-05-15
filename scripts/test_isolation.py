import re
import http.cookiejar
import urllib.error
import urllib.parse
import urllib.request

BASE = "http://localhost:8080"


def login(username: str, password: str):
    cj = http.cookiejar.CookieJar()
    opener = urllib.request.build_opener(urllib.request.HTTPCookieProcessor(cj))
    html = opener.open(BASE + "/login").read().decode("utf-8", errors="replace")
    m = re.search(r'name="_csrf"\s+value="([^"]+)"', html)
    if not m:
        print("csrf not found")
        return None
    token = m.group(1)
    data = urllib.parse.urlencode(
        {"username": username, "password": password, "_csrf": token}
    ).encode()
    req = urllib.request.Request(BASE + "/login", data=data, method="POST")
    opener.open(req)
    return opener


def fetch(opener, path: str):
    try:
        resp = opener.open(BASE + path)
        return resp.getcode(), resp.geturl(), resp.read(8000).decode("utf-8", errors="replace")
    except urllib.error.HTTPError as e:
        body = e.read(8000).decode("utf-8", errors="replace") if e.fp else ""
        return e.code, e.geturl(), body


def main():
    print("=== BAYI (dealer) ===")
    op = login("dealer", "password")
    if op:
        for p in ["/dealer/customers", "/admin/customers", "/admin/products"]:
            code, url, body = fetch(op, p)
            denied = "Yetkisiz" in body
            admin_list = "bayi filtresi" in body.lower() or "Tüm kurumlar" in body
            bayi_list = "Sizin oluşturduğunuz" in body
            print(f"{p} -> HTTP {code}, forbidden={denied}, admin_list={admin_list}, bayi_list={bayi_list}")

    print("\n=== ADMIN ===")
    op = login("admin", "password")
    if op:
        for p in ["/admin/customers", "/dealer/customers"]:
            code, url, body = fetch(op, p)
            denied = "Yetkisiz" in body
            admin_list = "bayi filtresi" in body.lower()
            bayi_list = "Sizin oluşturduğunuz" in body
            print(f"{p} -> HTTP {code}, forbidden={denied}, admin_list={admin_list}, bayi_list={bayi_list}")


def test_cross_customer_edit():
    print("\n=== BAYI cross-customer edit (service isolation) ===")
    op = login("dealer", "password")
    if not op:
        return
    _, _, body = fetch(op, "/dealer/customers")
    ids = [int(x) for x in re.findall(r"/dealer/customers/(\d+)/edit", body)]
    print("own customer ids:", ids[:5])
    if ids:
        code, _, b = fetch(op, f"/dealer/customers/{ids[0]}/edit")
        print(f"own edit id={ids[0]} -> HTTP {code}, forbidden={'Yetkisiz' in b}")
    probe = 99999
    code, _, b = fetch(op, f"/dealer/customers/{probe}/edit")
    print(f"unknown id={probe} -> HTTP {code}, forbidden={'Yetkisiz' in b}, not_found={'bulunamadı' in b.lower() or 'Kurum bulunamadı' in b}")


def test_other_dealer_customer():
    print("\n=== BAYI: other dealer's customer (id=1 belongs to dealer 2) ===")
    op = login("dealer", "password")
    if not op:
        return
    code, _, body = fetch(op, "/dealer/customers/1/edit")
    blocked = "Kurum bulunamadı" in body or "Yetkisiz" in body
    form = "Kurum Düzenle" in body and "Yetkisiz" not in body
    print(f"/dealer/customers/1/edit -> HTTP {code}, blocked={blocked}, shows_form={form}")
    code2, _, body2 = fetch(op, "/dealer/customers/2/edit")
    print(f"/dealer/customers/2/edit (own) -> HTTP {code2}, form={'Kurum Düzenle' in body2}")


if __name__ == "__main__":
    main()
    test_cross_customer_edit()
    test_other_dealer_customer()
