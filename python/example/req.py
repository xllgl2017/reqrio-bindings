import base64
import json
import os
import random
import shutil
import tempfile

import reqrio
from reqrio import Method, url_decode, Session


def http_get_matrix(session: Session, alpn: str):
    """Testing different types of requests"""
    sids = [
        # 0 首页
        session.send(Method.GET, "https://httpbin.reqrio.com"),
        # 1 查询串回显
        session.send(Method.GET, "https://httpbin.reqrio.com/get"),
        # 2 请求头回显
        session.send(Method.GET, "https://httpbin.reqrio.com/headers"),
        # 3 UA 回显
        session.send(Method.GET, "https://httpbin.reqrio.com/user-agent"),
        # 4 出口 IP
        session.send(Method.GET, "https://httpbin.reqrio.com/ip"),
        # 5 uuid
        session.send(Method.GET, "https://httpbin.reqrio.com/uuid"),
        # 6 JSON 文档
        session.send(Method.GET, "https://httpbin.reqrio.com/json"),
        # 7 HTML 文档
        session.send(Method.GET, "https://httpbin.reqrio.com/html"),
        # 8 XML 文档
        session.send(Method.GET, "https://httpbin.reqrio.com/xml"),
        # 9 纯文本
        session.send(Method.GET, "https://httpbin.reqrio.com/robots.txt"),
        # 10 二进制图片
        session.send(Method.GET, "https://httpbin.reqrio.com/image/png"),
        # 11 gzip
        session.send(Method.GET, "https://httpbin.reqrio.com/gzip"),
        # 12 deflate
        session.send(Method.GET, "https://httpbin.reqrio.com/deflate"),
        # 13 brotli
        session.send(Method.GET, "https://httpbin.reqrio.com/brotli"),
        # 14 chunked JSON 流
        session.send(Method.GET, "https://httpbin.reqrio.com/stream/5"),
        # 15 定长原始字节
        session.send(Method.GET, "https://httpbin.reqrio.com/bytes/2048"),
        # 16 ETag
        session.send(Method.GET, "https://httpbin.reqrio.com/etag/reqrio"),
    ]

    # 0. 首页：状态行 + ALPN + Content-Type + body
    home = session.recv(sids[0])
    assert home.status_code == 200
    assert str(home.alpn) == alpn
    assert home.get_header("content-type") == "text/html"
    assert len(home.bytes()) > 0
    assert "httpbin" in home.text.lower()

    # /get
    get = session.recv(sids[1]).json()
    assert get["url"] == "https://httpbin.reqrio.com/get", get
    assert get["method"] == "GET", get
    assert isinstance(get["headers"], dict), get

    # /headers
    headers = session.recv(sids[2]).json()
    assert "httpbin.reqrio.com" in json.dumps(headers).lower(), headers

    # /user-agent
    ua = session.recv(sids[3]).json()
    assert "reqrio" in json.dumps(ua).lower(), ua

    # /ip
    ip = session.recv(sids[4]).json()
    assert len(str(ip["origin"])) > 0, ip

    # /uuid
    uuid = session.recv(sids[5]).json()
    assert len(uuid["uuid"]) == 36, uuid

    # /json
    doc = session.recv(sids[6]).json()
    assert isinstance(doc["slideshow"], dict), doc
    assert isinstance(doc["slideshow"]["slides"], list), doc

    # /html
    html = session.recv(sids[7])
    assert html.get_header("content-type").startswith("text/html")
    assert "<html" in html.text.lower()

    # /xml
    xml = session.recv(sids[8], False)
    assert xml.get_header("content-type") == "application/xml"
    assert "<slideshow" in xml.text

    # /robots.txt
    robots = session.recv(sids[9], False)
    assert robots.get_header("content-type") == "text/plain"
    assert len(robots.bytes()) > 0

    # /image/png：二进制响应
    png = session.recv(sids[10], False)
    assert png.get_header("content-type") == "image/png"
    assert png.bytes().startswith(b"\x89PNG")

    # gzip / deflate / brotli
    assert session.recv(sids[11], False).json()["gzipped"] is True
    assert session.recv(sids[12], False).json()["deflated"] is True
    assert session.recv(sids[13], False).json()["brotli"] is True

    # /stream/5：chunked 分块，5 行 JSON
    stream = session.recv(sids[14], False)
    assert stream.status_code == 200
    text = stream.text
    assert len(text.splitlines()) == 5, text
    assert '"id"' in text

    # 15. /bytes/2048：原始字节长度精确匹配
    body = session.recv(sids[15], False)
    assert body.status_code == 200
    assert len(body.bytes()) == 2048

    # 16. /etag/reqrio：响应头可读
    etag = session.recv(sids[16], False)
    assert etag.status_code == 200
    assert etag.get_header("etag") == "reqrio"

    # SNI：IP 直连 + 自定义 sni
    sid = session.send(Method.GET, "https://178.236.41.45/get", sni="httpbin.reqrio.com")
    assert session.recv(sid, False).status_code == 200


def http_method(session: Session):
    """Request method testing"""
    post_json = {"name": "reqrio", "num": 7, "ok": True}
    sids = [
        session.send(Method.GET, "https://httpbin.reqrio.com/get"),
        session.send(Method.POST, "https://httpbin.reqrio.com/post", json=post_json),
        session.send(Method.PUT, "https://httpbin.reqrio.com/put", json={"k": "v"}),
        session.send(Method.HEAD, "https://httpbin.reqrio.com/get"),
        session.send(Method.DELETE, "https://httpbin.reqrio.com/delete", text="bye"),
        session.send(Method.OPTIONS, "https://httpbin.reqrio.com/anything"),
        session.send(Method.TRACE, "https://httpbin.reqrio.com/get"),
        session.send(Method.CONNECT, "https://httpbin.reqrio.com/connect"),
        session.send(Method.PATCH, "https://httpbin.reqrio.com/patch", text="x=1"),
        session.send(Method.QUERY, "https://httpbin.reqrio.com/query", text="x=1"),
    ]

    # GET
    get = session.recv(sids[0], False).json()
    assert get["method"] == "GET", get

    # POST
    post = session.recv(sids[1], False).json()
    assert post["method"] == "POST", post
    assert json.loads(post["data"]) == post_json, post

    # PUT
    put = session.recv(sids[2], False).json()
    assert put["method"] == "PUT", put
    assert json.loads(put["data"]) == {"k": "v"}

    # HEAD
    head = session.recv(sids[3], False)
    assert head.status_code == 200
    assert int(head.get_header("content-length")) > 0
    assert len(head.bytes()) == 0

    # DELETE
    delete = session.recv(sids[4], False).json()
    assert delete["method"] == "DELETE"
    assert delete["data"] == "bye"

    # OPTIONS
    options = session.recv(sids[5], False)
    assert options.status_code == 200
    allow = options.get_header("access-control-allow-methods")
    assert "GET, POST, PUT, DELETE, PATCH, OPTIONS" in allow


def http_status(session: Session):
    """Request status testing"""
    codes = [200, 201, 202, 204, 206, 301, 302, 304, 400, 401, 403, 404, 418, 500, 502, 503]
    sids = [session.send(Method.GET, f"https://httpbin.reqrio.com/status/{code}") for code in codes]
    for code, sid in zip(codes, sids):
        res = session.recv(sid, False)
        assert res.status_code == code, f"{res.status_code} != {code}"


def http_auth(session: Session):
    """Authentication Request Test"""
    # 先发送未认证请求
    sid_un_auth = session.send(Method.GET, "https://httpbin.reqrio.com/basic-auth/user/passwd")
    session.add_header("Authorization", "Basic dXNlcjpwYXNzd2Q=")
    sids = [
        sid_un_auth,
        session.send(Method.GET, "https://httpbin.reqrio.com/basic-auth/user/passwd"),
        session.send(Method.GET, "https://httpbin.reqrio.com/bearer"),
    ]

    # 未认证
    auth = session.recv(sids[0], False)
    assert auth.status_code == 401, auth.status_code
    # 已认证
    auth = session.recv(sids[1], False)
    assert auth.status_code == 200, auth.status_code
    assert auth.json()["authenticated"] is True
    # 未认证
    auth = session.recv(sids[2], False)
    assert auth.status_code == 401, auth.status_code


def query_param(session: Session):
    """Request parameter testing"""
    p1 = {"q": "'reqrio'", "v": '"reqrio"', "n": "42", "b": "true"}
    p2 = {"added": "2"}
    p3 = {"cn": "中文", "sp": "a b&c=d", "empty": ""}
    p4 = {"f": "1.5", "arr": "x,y"}

    sids = [
        session.send(Method.GET, "https://httpbin.reqrio.com/get", params=p1),
        session.send(Method.GET, "https://httpbin.reqrio.com/get?existing=1", params=p2),
        session.send(Method.GET, "https://httpbin.reqrio.com/get", params=p3),
        session.send(Method.GET, "https://httpbin.reqrio.com/get", params=p4),
    ]

    # P1
    j = session.recv(sids[0], False).json()
    assert j["args"]["q"] == "'reqrio'", j
    assert j["args"]["v"] == '"reqrio"', j
    assert j["args"]["n"] == "42", j
    assert j["args"]["b"] == "true", j
    assert url_decode(j["url"]).startswith("https://httpbin.reqrio.com/get?"), j["url"]

    # P2
    j = session.recv(sids[1], False).json()
    assert j["args"]["existing"] == "1", j
    assert j["args"]["added"] == "2", j
    assert url_decode(j["url"]).startswith("https://httpbin.reqrio.com/get?"), j["url"]

    # P3
    j = session.recv(sids[2], False).json()
    assert j["args"]["cn"] == "中文", j
    assert j["args"]["sp"] == "a b&c=d", j
    assert j["args"]["empty"] == "", j
    # 这里不对（与 Rust 版本一致，暂不校验 url）
    # assert url_decode(j["url"]) == ...

    # P4
    j = session.recv(sids[3], False).json()
    assert j["args"]["f"] == "1.5", j
    assert j["args"]["arr"] == "x,y", j


def http_redirect(s):
    """Redirection test
    """
    urls = [
        "https://httpbin.reqrio.com/redirect/2",
        "https://httpbin.reqrio.com/relative-redirect/1",
        "https://httpbin.reqrio.com/redirect-to?url=/get&status_code=307",
        "https://httpbin.reqrio.com/absolute-redirect/2",
    ]
    sids = [s.send(Method.GET, url, auto_redirect=False) for url in urls]

    r1 = s.recv(sids[0], False)
    assert r1.status_code == 302, r1.status_code
    assert r1.location() == "/relative-redirect/1", r1.location()

    r2 = s.recv(sids[1], False)
    assert r2.status_code == 302, r2.status_code
    assert r2.location() == "/get", r2.location()

    r3 = s.recv(sids[2], False)
    assert r3.status_code == 307, r3.status_code
    assert r3.location() == "/get", r3.location()

    r4 = s.recv(sids[3], False)
    assert r4.status_code == 302, r4.status_code
    assert r4.location().startswith("https://"), r4.location()

    # auto redirect
    r5 = s.get("https://httpbin.reqrio.com/redirect/2")
    assert r5.status_code == 200, r5.status_code

    r6 = s.get("https://httpbin.reqrio.com/relative-redirect/1")
    assert r6.status_code == 200, r6.status_code

    r7 = s.get("https://httpbin.reqrio.com/redirect-to?url=/get&status_code=307")
    assert r7.status_code == 200, r7.status_code
    # todo:补充set_max_redirect_exceeds后需要


def http_cookie(session: Session):
    """Request Cookie Test"""
    r1 = session.get("https://httpbin.reqrio.com/cookies/set?k=v&k2=v2", auto_redirect=False)
    assert r1.status_code == 302, r1.status_code
    assert r1.location() == "/cookies", r1.location()
    cks = r1.cookies()
    assert any(c["name"] == "k" and c["value"] == "v" for c in cks), cks
    assert any(c["name"] == "k2" and c["value"] == "v2" for c in cks), cks

    r2 = session.get("https://httpbin.reqrio.com/cookies/delete?k", auto_redirect=False)
    assert r2.status_code == 302, r2.status_code

    r3 = session.get("https://httpbin.reqrio.com/cookies").json()
    # todo: 增加remove_cookie后需要补充
    assert r3["cookies"].get("k2") == "v2", r3
    assert r3["cookies"].get("k", "") == "", r3


def http_header(session: Session):
    """Request Header Test"""
    session.set_headers({
        "User-Agent": "reqrio/{},reqrio-test".format(getattr(reqrio, "__version__", "test")),
        "X-Reqrio-Test": "hello",
        "Accept-Language": "zh-CN,zh;q=0.9",
        "X-Multi": "a",
        "X-Int": "1",
        "X-Bool": "true",
        "X-Object": json.dumps({"k": "v"}, separators=(",", ":")),
        "X-Array": json.dumps(["1", 1, True], separators=(",", ":")),
    })
    sids = [
        session.send(Method.GET, "https://httpbin.reqrio.com/headers"),
        session.send(Method.GET, "https://httpbin.reqrio.com/user-agent"),
        session.send(Method.POST, "https://httpbin.reqrio.com/post", json={"with": "header"}),
    ]
    # Append + Overwrite
    session.add_header("X-Late-Header", "late")
    session.add_header("X-Reqrio-Test", "overwritten")
    sids.extend([
        session.send(Method.GET, "https://httpbin.reqrio.com/headers"),
        session.send(Method.GET, "https://httpbin.reqrio.com/anything?phase=late"),
    ])

    h1 = session.recv(sids[0], False).text.lower()
    header = json.loads(h1)["headers"]
    ua = header["user-agent"]
    assert ua.endswith(",reqrio-test"), ua
    assert header["x-reqrio-test"] == "hello", header
    assert header["accept-language"] == "zh-cn,zh;q=0.9", header
    assert header["x-multi"] == "a", header
    assert header["x-int"] == "1", header
    assert header["x-bool"] == "true", header
    assert json.loads(header["x-object"]) == {"k": "v"}, header
    assert json.loads(header["x-array"]) == ["1", 1, True], header

    h2 = session.recv(sids[1], False).json()
    assert h2["user-agent"] == ua, h2

    h3 = session.recv(sids[2], False).json()
    assert json.loads(h3["data"]) == {"with": "header"}, h3

    for sid in sids[3:5]:
        res = session.recv(sid, False).text.lower()
        header = json.loads(res)["headers"]
        assert header["user-agent"] == ua, header
        assert header["x-reqrio-test"] == "overwritten", header
        assert header["accept-language"] == "zh-cn,zh;q=0.9", header
        assert header["x-multi"] == "a", header
        assert header["x-int"] == "1", header
        assert header["x-bool"] == "true", header
        assert json.loads(header["x-object"]) == {"k": "v"}, header
        assert json.loads(header["x-array"]) == ["1", 1, True], header
        assert header["x-late-header"] == "late", header


def http_stream(session: Session):
    """Stream：next_chunk / chunks()"""
    sid1 = session.send(Method.GET, "https://httpbin.reqrio.com/bytes/16384")
    sid2 = session.send(Method.GET, "https://httpbin.reqrio.com/stream/3")

    total = 0
    chunks = 0
    for chunk in session.recv(sid1, True).chunks():
        assert len(chunk) > 0
        total += len(chunk)
        chunks += 1
    assert total == 16384, total
    assert chunks >= 1
    for chunk in session.recv(sid2, True).chunks():
        assert b"\n" in chunk, chunk

    assert sid1 != sid2


def http_body(session: Session):
    """Form + JSON + File + Text + Bytes"""
    d1 = {
        "str1": "s",
        "str2": "'s'",
        "str3": '"s"',
        "int": 0,
        "bool": True,
        "object": {"data": "value"},
        "array": ["2", 0, True, {"k": "v"}],
    }
    d2 = {"k1": "v1", "k2": "v2"}
    text = "sdfhsdjkfsdljhkgs;gfkgsdfhsdasugdsadgsauydgtasdfytsad"
    data = os.urandom(4096)

    tmpdir = tempfile.mkdtemp(prefix="reqrio-http-test-")
    try:
        path1 = os.path.join(tmpdir, "f1.bin")
        path2 = os.path.join(tmpdir, "f2.bin")
        with open(path1, "wb") as f:
            f.write(data)
        with open(path2, "wb") as f:
            f.write(data[:1024])

        ftype = "application/octet-stream"
        f1 = [{"path": path1, "field_name": "file", "filetype": ftype}]
        f3 = [{"path": path1, "field_name": "file1", "filetype": ftype},
              {"path": path2, "field_name": "file2", "filetype": ftype}]

        sids = [
            # form
            session.send(Method.POST, "https://httpbin.reqrio.com/post", data=d2),
            # json
            session.send(Method.POST, "https://httpbin.reqrio.com/post", json=d1),
            # bytes
            session.send(Method.POST, "https://httpbin.reqrio.com/post", bytes=data),
            # text
            session.send(Method.POST, "https://httpbin.reqrio.com/post", text=text),
            # file
            session.send(Method.POST, "https://httpbin.reqrio.com/post", files=f1),
            # file + form
            session.send(Method.POST, "https://httpbin.reqrio.com/post", data=d2, files=f1),
            # files + form + params
            session.send(Method.POST, "https://httpbin.reqrio.com/post", params=d2, data=d2, files=f3),
        ]

        # form
        r2 = session.recv(sids[0], False).json()
        assert r2["form"] == d2, r2
        if r2["headers"].get("content-type") is None:
            assert r2["headers"]["Content-Type"] == "application/x-www-form-urlencoded"
        else:
            assert r2["headers"]["content-type"] == "application/x-www-form-urlencoded"

        # json
        r3 = session.recv(sids[1], False).json()
        assert r3["json"] == d1, r3
        if r3["headers"].get("content-type") is None:
            assert r3["headers"]["Content-Type"] == "application/json"
        else:
            assert r3["headers"]["content-type"] == "application/json"

        # bytes
        r4 = session.recv(sids[2], False).json()
        if r4["headers"].get("content-type") is None:
            assert r4["headers"]["Content-Type"] == "application/octet-stream"
        else:
            assert r4["headers"]["content-type"] == "application/octet-stream"
        d = base64.b64decode(r4["data"].replace("data:application/octet-stream;base64,", ""))
        assert d == data

        # text
        r5 = session.recv(sids[3], False).json()
        if r5["headers"].get("content-type") is None:
            assert r5["headers"]["Content-Type"] == "text/plain"
        else:
            assert r5["headers"]["content-type"] == "text/plain"
        assert r5["data"] == text, r5

        # file
        r6 = session.recv(sids[4], False).json()
        if r6["headers"].get("content-type") is None:
            assert r6["headers"]["Content-Type"].startswith("multipart/form-data; boundary=----WebKitFormBoundary")
        else:
            assert r6["headers"]["content-type"].startswith("multipart/form-data; boundary=----WebKitFormBoundary")
        d = base64.b64decode(r6["files"]["file"].replace("data:application/octet-stream;base64,", ""))
        assert d == data

        # file + form
        r7 = session.recv(sids[5], False).json()
        if r7["headers"].get("content-type") is None:
            assert r7["headers"]["Content-Type"].startswith("multipart/form-data; boundary=----WebKitFormBoundary")
        else:
            assert r7["headers"]["content-type"].startswith("multipart/form-data; boundary=----WebKitFormBoundary")
        d = base64.b64decode(r7["files"]["file"].replace("data:application/octet-stream;base64,", ""))
        assert d == data
        assert r7["form"] == d2

        # files + form + params
        r8 = session.recv(sids[6], False).json()
        if r8["headers"].get("content-type") is None:
            assert r8["headers"]["Content-Type"].startswith("multipart/form-data; boundary=----WebKitFormBoundary")
        else:
            assert r8["headers"]["content-type"].startswith("multipart/form-data; boundary=----WebKitFormBoundary")
        d = base64.b64decode(r8["files"]["file1"].replace("data:application/octet-stream;base64,", ""))
        assert d == data
        d = base64.b64decode(r8["files"]["file2"].replace("data:application/octet-stream;base64,", ""))
        assert d == data[:1024]
        assert r8["form"] == d2, r8
        assert r8["args"] == d2, r8
    finally:
        shutil.rmtree(tmpdir, ignore_errors=True)


def http_concurrency(session: Session):
    sids = [session.send(Method.GET, f"https://httpbin.reqrio.com/get?i={i}") for i in range(6)]
    sids.append(session.send(Method.POST, "https://httpbin.reqrio.com/post", text="h2-body"))
    sids.append(session.send(Method.GET, "https://httpbin.reqrio.com/gzip"))
    sids.append(session.send(Method.GET, "https://httpbin.reqrio.com/brotli"))
    sids.append(session.send(Method.GET, "https://httpbin.reqrio.com/bytes/4096"))

    while sids:
        pos = random.randrange(len(sids))
        res = session.recv(sids.pop(pos), False)
        assert res.status_code == 200, res.status_code


def http_one_shot_api():
    res = reqrio.get("https://httpbin.reqrio.com/get")
    assert res.status_code == 200, res.status_code
    assert res.json()["method"] == "GET", res.json()

    res = reqrio.post("https://httpbin.reqrio.com/post", json={"x": 1})
    assert res.status_code == 200, res.status_code
    assert res.json()["json"]["x"] == 1, res.json()

    res = reqrio.send(Method.PUT, "https://httpbin.reqrio.com/put", text="raw")
    assert res.status_code == 200, res.status_code
    assert res.json()["data"] == "raw", res.json()
