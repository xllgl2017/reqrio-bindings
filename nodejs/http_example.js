const fs = require("fs")
const os = require("os")
const path = require("path")
const {Session, ALPN, Method} = require("./index")

/** Testing different types of requests
 * @param session {Session}
 * @param _alpn {ALPN}
 * */
function http_get_matrix(session, _alpn) {
    let sids = [
        session.send(Method.GET, "https://httpbin.reqrio.com"),                   // 0 index
        session.send(Method.GET, "https://httpbin.reqrio.com/get"),               // 1 查询串回显
        session.send(Method.GET, "https://httpbin.reqrio.com/headers"),           // 2 请求头回显
        session.send(Method.GET, "https://httpbin.reqrio.com/user-agent"),        // 3 UA 回显
        session.send(Method.GET, "https://httpbin.reqrio.com/ip"),                // 4 出口 IP
        session.send(Method.GET, "https://httpbin.reqrio.com/uuid"),              // 5 uuid
        session.send(Method.GET, "https://httpbin.reqrio.com/json"),              // 6 JSON 文档
        session.send(Method.GET, "https://httpbin.reqrio.com/html"),              // 7 HTML 文档
        session.send(Method.GET, "https://httpbin.reqrio.com/xml"),               // 8 XML 文档
        session.send(Method.GET, "https://httpbin.reqrio.com/robots.txt"),        // 9 纯文本
        session.send(Method.GET, "https://httpbin.reqrio.com/image/png"),         // 10 二进制图片
        session.send(Method.GET, "https://httpbin.reqrio.com/gzip"),              // 11 gzip
        session.send(Method.GET, "https://httpbin.reqrio.com/deflate"),           // 12 deflate
        session.send(Method.GET, "https://httpbin.reqrio.com/brotli"),            // 13 brotli
        session.send(Method.GET, "https://httpbin.reqrio.com/stream/5"),          // 14 chunked JSON 流
        session.send(Method.GET, "https://httpbin.reqrio.com/bytes/2048"),        // 15 定长原始字节
        session.send(Method.GET, "https://httpbin.reqrio.com/etag/reqrio")       // 16 ETag
    ];
    const home = session.recv(sids[0]);
    assertEq(home.status_code(), 200, "home status");
    assert(home.get_header("content-type").startsWith("text/html"), "home content-type");
    assert(home.bytes().length > 0, "home empty body");
    assert(home.text().toLowerCase().includes("httpbin"), "home body");
    home.close();

    // /get
    const get = session.recv(sids[1]).json();
    assertEq(get["url"], "https://httpbin.reqrio.com/get", "url");
    assertEq(get["method"], "GET", "method");
    assert(typeof get["headers"] === "object", "headers");

    // /headers
    const headers = session.recv(sids[2]).json();
    assert(JSON.stringify(headers).toLowerCase().includes("httpbin.reqrio.com"), "headers");

    // /user-agent
    const ua = session.recv(sids[3]).json();
    assert(JSON.stringify(ua).toLowerCase().includes("reqrio"), "user-agent");

    // /ip
    const ip = session.recv(sids[4]).json();
    assert(String(ip["origin"]).length > 0, "origin");

    // /uuid
    const uuid = session.recv(sids[5]).json();
    assertEq(uuid["uuid"].length, 36, "uuid length");

    // /json
    const doc = session.recv(sids[6]).json();
    assert(typeof doc["slideshow"] === "object", "slideshow");
    assert(Array.isArray(doc["slideshow"]["slides"]), "slides");

    // /html
    const html = session.recv(sids[7]);
    assert(html.get_header("content-type").startsWith("text/html"), "html content-type");
    assert(html.text().toLowerCase().includes("<html"), "html body");
    html.close();

    // /xml
    const xml = session.recv(sids[8]);
    assert(xml.get_header("content-type").startsWith("application/xml"), "xml content-type");
    assert(xml.text().includes("<slideshow"), "xml body");
    xml.close();

    // /robots.txt
    const robots = session.recv(sids[9]);
    assert(robots.get_header("content-type").startsWith("text/plain"), "robots content-type");
    assert(robots.bytes().length > 0, "robots body");
    robots.close();

    // /image/png：二进制响应
    const png = session.recv(sids[10]);
    assert(png.get_header("content-type").startsWith("image/png"), "png content-type");
    const pngBytes = png.bytes();
    assert(pngBytes[0] === 0x89 && pngBytes[1] === 0x50 && pngBytes[2] === 0x4e && pngBytes[3] === 0x47, "不是 PNG 数据");
    png.close();

    // gzip / deflate / brotli
    assertEq(session.recv(sids[11]).json()["gzipped"], true, "gzip");
    assertEq(session.recv(sids[12]).json()["deflated"], true, "deflate");
    assertEq(session.recv(sids[13]).json()["brotli"], true, "brotli");

    // /stream/5：chunked 分块，5 行 JSON
    const stream = session.recv(sids[14]);
    assertEq(stream.status_code(), 200, "stream status");
    const text = stream.text();
    assertEq(text.trim().split("\n").length, 5, "stream lines");
    assert(text.includes("\"id\""), "stream body");
    stream.close();

    // 15. /bytes/2048：原始字节长度精确匹配
    const body = session.recv(sids[15]);
    assertEq(body.status_code(), 200, "bytes status");
    assertEq(body.bytes().length, 2048, "bytes length");
    body.close();

    // 16. /etag/reqrio：响应头可读
    const etag = session.recv(sids[16]);
    assertEq(etag.status_code(), 200, "etag status");
    assertEq(etag.get_header("etag"), "reqrio", "etag");
    etag.close();

    // SNI：IP 直连 + 自定义 sni
    const sni = session.get("https://178.236.41.45/get", {sni: "httpbin.reqrio.com"});
    assertEq(sni.status_code(), 200, "sni status");
    sni.close();
}

/**
 * Request method testing
 * @param session {Session}
 * */
function http_method(session) {
    const postJson = {name: "reqrio", num: 7, ok: true};
    let sids = [
        session.send(Method.GET, "https://httpbin.reqrio.com/get"),
        session.send(Method.POST, "https://httpbin.reqrio.com/post", {json: postJson}),
        session.send(Method.PUT, "https://httpbin.reqrio.com/put", {json: {k: "v"}}),
        session.send(Method.HEAD, "https://httpbin.reqrio.com/get"),
        session.send(Method.DELETE, "https://httpbin.reqrio.com/delete", {text: "bye"}),
        session.send(Method.OPTIONS, "https://httpbin.reqrio.com/anything"),
        session.send(Method.TRACE, "https://httpbin.reqrio.com/get"),
        session.send(Method.PATCH, "https://httpbin.reqrio.com/patch", {bytes: Buffer.from("x=1"), ct: "text/plain"}),
        session.send(Method.QUERY, "https://httpbin.reqrio.com/query", {bytes: Buffer.from("x=1"), ct: "text/plain"})
    ]

    // GET
    assertEq(session.recv(sids[0], false).json()["method"], "GET", "GET");

    // POST
    const post = session.recv(sids[1], false).json();
    assertEq(post["method"], "POST", "POST");
    assertEq(JSON.parse(post["data"]), postJson, "POST data");

    // PUT
    const put = session.recv(sids[2], false).json();
    assertEq(put["method"], "PUT", "PUT");
    assertEq(JSON.parse(put["data"]), {k: "v"}, "PUT data");

    // HEAD
    const head = session.recv(sids[3], false);
    assertEq(head.status_code(), 200, "HEAD status");
    assert(head.get_header("content-length") !== "", "HEAD content-length");
    assertEq(head.bytes().length, 0, "HEAD body");
    head.close();

    // DELETE
    const del = session.recv(sids[4], false).json();
    assertEq(del["method"], "DELETE", "DELETE");
    assertEq(del["data"], 'bye')

    // OPTIONS
    const options = session.recv(sids[5], false);
    assertEq(options.status_code(), 200, "OPTIONS status");
    assert(options.get_header("access-control-allow-methods").includes("GET, POST, PUT, DELETE, PATCH, OPTIONS"), "OPTIONS allow");
    options.close();

    //待定
    // session.recv(sids[6], false).close();
    // session.recv(sids[7], false).close();
    // session.recv(sids[8], false).close();
}

/**
 * Request status testing
 * @param session {Session}
 * */
function http_status(session) {
    const codes = [200, 201, 202, 204, 206, 301, 302, 304, 400, 401, 403, 404, 418, 500, 502, 503];
    let sids = [];
    codes.forEach(function (code) {
        sids.push(session.send(Method.GET, `https://httpbin.reqrio.com/status/${code}`))
    });
    codes.forEach((code, i) => {
        const resp = session.recv(sids[i], false);
        assertEq(resp.status_code(), code, `status ${code}`);
        resp.close();
    });
}

/**
 * Authentication Request Test
 * @param session {Session}
 * */
function http_auth(session) {
    // 先发送未认证请求
    const sidUnAuth = session.send(Method.GET, "https://httpbin.reqrio.com/basic-auth/user/passwd", {});
    session.add_header("Authorization", "Basic dXNlcjpwYXNzd2Q=");
    const sids = [
        sidUnAuth,
        session.send(Method.GET, "https://httpbin.reqrio.com/basic-auth/user/passwd", {}),
        session.send(Method.GET, "https://httpbin.reqrio.com/bearer", {}),
    ];

    // 未认证
    const unAuth = session.recv(sids[0], false);
    assertEq(unAuth.status_code(), 401, "un auth");
    unAuth.close();
    // 已认证
    const auth = session.recv(sids[1], false);
    assertEq(auth.status_code(), 200, "auth");
    assertEq(auth.json()["authenticated"], true, "authenticated");
    auth.close();
    // 未认证
    const bearer = session.recv(sids[2], false);
    assertEq(bearer.status_code(), 401, "bearer");
    bearer.close();
}

/**
 * Request parameter testing
 * @param session {Session}
 * */
function query_param(session) {
    const p1 = {q: "'reqrio'", v: '"reqrio"', n: "42", b: "true"};
    const p2 = {added: "2"};
    const p3 = {cn: "中文", sp: "a b&c=d", empty: ""};
    const p4 = {f: "1.5", arr: "x,y"};

    let sids = [
        session.send(Method.GET, "https://httpbin.reqrio.com/get", {params: p1}),
        session.send(Method.GET, "https://httpbin.reqrio.com/get?existing=1", {params: p2}),
        session.send(Method.GET, "https://httpbin.reqrio.com/get", {params: p3}),
        session.send(Method.GET, "https://httpbin.reqrio.com/get", {params: p4}),
    ]

    // P1
    let args = session.recv(sids[0], false).json()["args"];
    assertEq(args["q"], "'reqrio'", "q");
    assertEq(args["v"], '"reqrio"', "v");
    assertEq(args["n"], "42", "n");
    assertEq(args["b"], "true", "b");

    // P2
    args = session.recv(sids[1], false).json()["args"];
    assertEq(args["existing"], "1", "existing");
    assertEq(args["added"], "2", "added");

    // P3
    args = session.recv(sids[2], false).json()["args"];
    assertEq(args["cn"], "中文", "cn");
    assertEq(args["sp"], "a b&c=d", "sp");
    assertEq(args["empty"], "", "empty");

    // P4
    args = session.recv(sids[3], false).json()["args"];
    assertEq(args["f"], "1.5", "f");
    assertEq(args["arr"], "x,y", "arr");
}

/**
 *  Redirection test
 * * Non automatic redirection can be done using send
 * * Automatic redirection cannot use send and recv
 *  @param session {Session}
 */
function http_redirect(session) {
    session.set_auto_redirect(false);
    let sids = [
        session.send(Method.GET, "https://httpbin.reqrio.com/redirect/2"),
        session.send(Method.GET, "https://httpbin.reqrio.com/relative-redirect/1"),
        session.send(Method.GET, "https://httpbin.reqrio.com/redirect-to?url=/get&status_code=307"),
        session.send(Method.GET, "https://httpbin.reqrio.com/absolute-redirect/2"),
    ]

    const r1 = session.recv(sids[0], false);
    assertEq(r1.status_code(), 302, "r1 status");
    assertEq(r1.get_header("location"), "/relative-redirect/1", "r1 location");
    r1.close();

    const r2 = session.recv(sids[1], false);
    assertEq(r2.status_code(), 302, "r2 status");
    assertEq(r2.get_header("location"), "/get", "r2 location");
    r2.close();

    const r3 = session.recv(sids[2], false);
    assertEq(r3.status_code(), 307, "r3 status");
    assertEq(r3.get_header("location"), "/get", "r3 location");
    r3.close();

    const r4 = session.recv(sids[3], false);
    assertEq(r4.status_code(), 302, "r4 status");
    assert(r4.get_header("location").startsWith("https://"), "r4 location");
    r4.close();

    //todo()set_max_redirect_exceeds

    // 自动重定向
    session.set_auto_redirect(true);
    let res1 = session.get("https://httpbin.reqrio.com/redirect/2");
    assertEq(res1.status_code(), 200)
    let res2 = session.get("https://httpbin.reqrio.com/relative-redirect/1");
    assertEq(res2.status_code(), 200)
    let res3 = session.get("https://httpbin.reqrio.com/redirect-to?url=/get&status_code=307");
    assertEq(res3.status_code(), 200)
}

/**
 * Request Cookie Test
 * @param session {Session}
 * */
function http_cookie(session) {
    session.set_auto_redirect(false);
    const r1 = session.get("https://httpbin.reqrio.com/cookies/set?k=v&k2=v2");
    assertEq(r1.status_code(), 302, "set cookie status");
    assertEq(r1.get_header("location"), "/cookies", "set cookie location");
    const cks = r1.cookies();
    assert(cks.some((c) => c.name === "k" && c.value === "v"), "cookie k");
    assert(cks.some((c) => c.name === "k2" && c.value === "v2"), "cookie k2");
    r1.close();

    // todo() remove_cookie
    const r2 = session.get("https://httpbin.reqrio.com/cookies/delete?k");
    assertEq(r2.status_code(), 302, "delete cookie status");
    r2.close();

    session.set_auto_redirect(true);
    const jar = session.get("https://httpbin.reqrio.com/cookies").json()["cookies"];
    assertEq(jar["k2"], "v2", "jar k2");
    assert((jar["k"] === undefined || jar["k"] === ""), "jar k");
}

/**
 * Request Header Test
 * @param session {Session}
 * */
function http_header(session) {
    session.set_headers({
        "User-Agent": "reqrio-test,reqrio-test",
        "X-Reqrio-Test": "hello",
        "Accept-Language": "zh-CN,zh;q=0.9",
        "X-Multi": "a",
        "X-Int": "1",
        "X-Bool": "true",
        "X-Object": JSON.stringify({k: "v"}),
        "X-Array": JSON.stringify(["1", 1, true]),
    });
    let sids = [
        session.send(Method.GET, "https://httpbin.reqrio.com/headers"),
        session.send(Method.GET, "https://httpbin.reqrio.com/user-agent"),
        session.send(Method.POST, "https://httpbin.reqrio.com/post", {json: {with: "header"}}),
    ]
    // Append + Overwrite
    session.add_header("X-Late-Header", "late");
    session.add_header("X-Reqrio-Test", "overwritten");
    sids.push(session.send(Method.GET, "https://httpbin.reqrio.com/headers"))
    sids.push(session.send(Method.GET, "https://httpbin.reqrio.com/anything?phase=late"))

    const h1 = session.recv(sids[0], false).text().toLowerCase();
    const header = JSON.parse(h1)["headers"];
    assertEq(header["user-agent"], "reqrio-test,reqrio-test", "user-agent");
    assertEq(header["x-reqrio-test"], "hello", "x-reqrio-test");
    assertEq(header["accept-language"], "zh-cn,zh;q=0.9", "accept-language");
    assertEq(header["x-multi"], "a", "x-multi");
    assertEq(header["x-int"], "1", "x-int");
    assertEq(header["x-bool"], "true", "x-bool");
    assertEq(JSON.parse(header["x-object"]), {k: "v"}, "x-object");
    assertEq(JSON.parse(header["x-array"]), ["1", 1, true], "x-array");

    const h2 = session.recv(sids[1], false).json();
    assertEq(h2["user-agent"], "reqrio-test,reqrio-test", "user-agent echoed");

    const h3 = session.recv(sids[2], false).json();
    assertEq(JSON.parse(h3["data"]), {with: "header"}, "post data");

    for (const sid of [sids[3], sids[4]]) {
        const res = session.recv(sid, false).text().toLowerCase();
        const h = JSON.parse(res)["headers"];
        assertEq(h["user-agent"], "reqrio-test,reqrio-test", "user-agent");
        assertEq(h["x-reqrio-test"], "overwritten", "x-reqrio-test");
        assertEq(h["accept-language"], "zh-cn,zh;q=0.9", "accept-language");
        assertEq(h["x-multi"], "a", "x-multi");
        assertEq(h["x-int"], "1", "x-int");
        assertEq(h["x-bool"], "true", "x-bool");
        assertEq(JSON.parse(h["x-object"]), {k: "v"}, "x-object");
        assertEq(JSON.parse(h["x-array"]), ["1", 1, true], "x-array");
        assertEq(h["x-late-header"], "late", "x-late-header");
    }
}

/**
 * 流式读取
 * @param session {Session}
 * */
function http_stream(session) {
    const sid1 = session.send(Method.GET, "https://httpbin.reqrio.com/bytes/16384", {});
    const sid2 = session.send(Method.GET, "https://httpbin.reqrio.com/stream/3", {});
    assert(sid1 !== sid2, "sid");

    let total = 0, chunks = 0;
    for (const chunk of session.recv(sid1, true).chunks()) {
        assert(chunk.length > 0, "empty chunk");
        total += chunk.length;
        chunks++;
    }
    assertEq(total, 16384, "stream total");
    assert(chunks >= 1, "chunks");

    for (const chunk of session.recv(sid2, true).chunks()) {
        assert(chunk.includes(0x0a), "stream chunk");
    }
}

/**
 * Form + JSON + File + Text + Bytes
 * @param session {Session}
 * */
function http_body(session) {
    const d1 = {
        str1: "s",
        str2: "'s'",
        str3: '"s"',
        int: 0,
        bool: true,
        object: {data: "value"},
        array: ["2", 0, true, {k: "v"}],
    };
    const d2 = {k1: "v1", k2: "v2"};
    const text = "sdfhsdjkfsdljhkgs;gfkgsdfhsdasugdsadgsauydgtasdfytsad";
    const data = Buffer.alloc(4096);
    for (let i = 0; i < data.length; i++) data[i] = i & 0xff;

    // node binding uploads files by path only
    const dir = fs.mkdtempSync(path.join(os.tmpdir(), "reqrio-http-test-"));
    const path1 = path.join(dir, "f1.bin");
    const path2 = path.join(dir, "f2.bin");
    fs.writeFileSync(path1, data);
    fs.writeFileSync(path2, data.subarray(0, 1024));
    const ftype = "application/octet-stream";

    let sids = [
        // form
        session.send(Method.POST, "https://httpbin.reqrio.com/post", {data: d2}),
        // json
        session.send(Method.POST, "https://httpbin.reqrio.com/post", {json: d1}),
        // bytes
        session.send(Method.POST, "https://httpbin.reqrio.com/post", {bytes: data}),
        // text
        session.send(Method.POST, "https://httpbin.reqrio.com/post", {bytes: Buffer.from(text), ct: "text/plain"}),
        // file
        session.send(Method.POST, "https://httpbin.reqrio.com/post", {
            files: [{path: path1, field_name: "file", filetype: ftype}]
        }),
        // file + form
        session.send(Method.POST, "https://httpbin.reqrio.com/post", {
            data: d2,
            files: [{path: path1, field_name: "file", filetype: ftype}]
        }),
        // files + form + params
        session.send(Method.POST, "https://httpbin.reqrio.com/post", {
            params: d2,
            data: d2,
            files: [
                {path: path1, field_name: "file1", filetype: ftype},
                {path: path2, field_name: "file2", filetype: ftype}
            ]
        })
    ]

    // form
    const r1 = session.recv(sids[0], false).json();
    assertEq(r1["form"], d2, "form");
    if (r1.headers["content-type"] !== null && r1["headers"]["content-type"] !== undefined) {
        assertEq(r1["headers"]["content-type"], "application/x-www-form-urlencoded")
    } else {
        assertEq(r1["headers"]["Content-Type"], "application/x-www-form-urlencoded")
    }

    // json
    const r2 = session.recv(sids[1], false).json();
    assertEq(r2.json.str1, d1.str1)
    assertEq(r2.json.str2, d1.str2)
    assertEq(r2.json.str3, d1.str3)
    assertEq(r2.json.int, d1.int)
    assertEq(r2.json.bool, d1.bool)
    assertEq(r2.json.array, d1.array)
    assertEq(r2.json.object, d1.object)
    if (r2["headers"]["content-type"] !== null && r2["headers"]["content-type"] !== undefined) {
        assertEq(r2["headers"]["content-type"], "application/json")
    } else {
        assertEq(r2["headers"]["Content-Type"], "application/json")
    }

    // bytes
    const r3 = session.recv(sids[2], false).json();
    if (r3["headers"]["content-type"] !== null && r3["headers"]["content-type"] !== undefined) {
        assertEq(r3["headers"]["content-type"], "application/octet-stream")
    } else {
        assertEq(r3["headers"]["Content-Type"], "application/octet-stream")
    }
    let dd = r3["data"].replace("data:application/octet-stream;base64,", "")
    assert(Buffer.from(dd, "base64").equals(data), "bytes body");

    // text
    const r4 = session.recv(sids[3], false).json();
    if (r4["headers"]["content-type"] !== null && r4["headers"]["content-type"] !== undefined) {
        assertEq(r4["headers"]["content-type"], "text/plain")
    } else {
        assertEq(r4["headers"]["Content-Type"], "text/plain")
    }
    assertEq(r4["data"], text, "text body");

    // file
    const r5 = session.recv(sids[4], false).json();
    const ct5 = r5["headers"]["content-type"] ?? r5["headers"]["Content-Type"];
    assert(ct5.startsWith("multipart/form-data; boundary=----WebKitFormBoundary"), "file content-type");
    const file5 = r5["files"]["file"].replace("data:application/octet-stream;base64,", "");
    assert(Buffer.from(file5, "base64").equals(data), "file body");

    // file + form
    const r6 = session.recv(sids[5], false).json();
    const ct6 = r6["headers"]["content-type"] ?? r6["headers"]["Content-Type"];
    assert(ct6.startsWith("multipart/form-data; boundary=----WebKitFormBoundary"), "file content-type");
    const file6 = r6["files"]["file"].replace("data:application/octet-stream;base64,", "");
    assert(Buffer.from(file6, "base64").equals(data), "file body");
    assertEq(r6["form"], d2, "form");

    // files + form + params
    const r7 = session.recv(sids[6], false).json();
    const ct7 = r7["headers"]["content-type"] ?? r7["headers"]["Content-Type"];
    assert(ct7.startsWith("multipart/form-data; boundary=----WebKitFormBoundary"), "files content-type");
    const file7a = r7["files"]["file1"].replace("data:application/octet-stream;base64,", "");
    assert(Buffer.from(file7a, "base64").equals(data), "file1 body");
    const file7b = r7["files"]["file2"].replace("data:application/octet-stream;base64,", "");
    assert(Buffer.from(file7b, "base64").equals(data.subarray(0, 1024)), "file2 body");
    assertEq(r7["form"], d2, "form");
    assertEq(r7["args"], d2, "args");

    fs.rmSync(dir, {recursive: true, force: true});
}


/**
 * @param session {Session}
 **/
function http_concurrency(session) {
    const sids = [];
    for (let i = 0; i < 6; i++) {
        sids.push(session.send(Method.GET, `https://httpbin.reqrio.com/get?i=${i}`, {}));
    }
    sids.push(session.send(Method.POST, "https://httpbin.reqrio.com/post", {
        bytes: Buffer.from("h2-body"),
        ct: "text/plain"
    }));
    sids.push(session.send(Method.GET, "https://httpbin.reqrio.com/gzip", {}));
    sids.push(session.send(Method.GET, "https://httpbin.reqrio.com/brotli", {}));
    sids.push(session.send(Method.GET, "https://httpbin.reqrio.com/bytes/4096", {}));

    while (sids.length > 0) {
        const pos = Math.floor(Math.random() * sids.length);
        const sid = sids.splice(pos, 1)[0];
        const res = session.recv(sid, false);
        assertEq(res.status_code(), 200, "concurrency status");
        res.close();
    }
}

function assert(cond, msg) {
    if (!cond) throw new Error(msg || "assertion failed");
}

function assertEq(got, want, msg) {
    if (JSON.stringify(got) !== JSON.stringify(want)) {
        throw new Error(`${msg || "assertEq"}: got=${JSON.stringify(got)}, want=${JSON.stringify(want)}`);
    }
}


module.exports = {
    http_get_matrix,
    http_method,
    http_status,
    http_auth,
    query_param,
    http_redirect,
    http_cookie,
    http_header,
    http_stream,
    http_body,
    http_concurrency,
    assert,
    assertEq
}
