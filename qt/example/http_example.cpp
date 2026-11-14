//
// Created by XLX on 2026/11/14.
//

#include "http_example.h"

#include <QJsonArray>
#include <QJsonDocument>
#include <QRandomGenerator>
#include <QTemporaryDir>


/// Testing different types of requests
void http_get_matrix(Session *session, const ALPN &alpn) {
    const QList sids = {
        session->send(GET, "https://httpbin.reqrio.com"), // 0 首页
        session->send(GET, "https://httpbin.reqrio.com/get"), // 1 查询串回显
        session->send(GET, "https://httpbin.reqrio.com/headers"), // 2 请求头回显
        session->send(GET, "https://httpbin.reqrio.com/user-agent"), // 3 UA 回显
        session->send(GET, "https://httpbin.reqrio.com/ip"), // 4 出口 IP
        session->send(GET, "https://httpbin.reqrio.com/uuid"), // 5 uuid
        session->send(GET, "https://httpbin.reqrio.com/json"), // 6 JSON 文档
        session->send(GET, "https://httpbin.reqrio.com/html"), // 7 HTML 文档
        session->send(GET, "https://httpbin.reqrio.com/xml"), // 8 XML 文档
        session->send(GET, "https://httpbin.reqrio.com/robots.txt"), // 9 纯文本
        session->send(GET, "https://httpbin.reqrio.com/image/png"), // 10 二进制图片
        session->send(GET, "https://httpbin.reqrio.com/gzip"), // 11 gzip
        session->send(GET, "https://httpbin.reqrio.com/deflate"), // 12 deflate
        session->send(GET, "https://httpbin.reqrio.com/brotli"), // 13 brotli
        session->send(GET, "https://httpbin.reqrio.com/stream/5"), // 14 chunked JSON 流
        session->send(GET, "https://httpbin.reqrio.com/bytes/2048"), // 15 定长原始字节
        session->send(GET, "https://httpbin.reqrio.com/etag/reqrio"), // 16 ETag
    };
    // 0. 首页：状态行 + Content-Type + body
    const Response home = session->recv(sids[0]);
    assert(home.statusCode()==200);
    assert(home.getHeader("content-type")=="text/html");
    assert(!home.bytes().isEmpty());
    assert(home.text().toLower().contains("httpbin"));

    // /get
    const QJsonObject get = session->recv(sids[1]).json().object();
    assert(get.value("url").toString()=="https://httpbin.reqrio.com/get");
    assert(get.value("method").toString()=="GET");
    assert(get.value("headers").isObject());

    // /headers
    const QJsonObject headers = session->recv(sids[2]).json().object();
    assert(QJsonDocument(headers).toJson().toLower().contains("httpbin.reqrio.com"));

    // /user-agent
    const QJsonObject ua = session->recv(sids[3]).json().object();
    assert(QJsonDocument(ua).toJson().toLower().contains("reqrio"));

    // /ip
    const QJsonObject ip = session->recv(sids[4]).json().object();
    assert(!ip.value("origin").toString().isEmpty());

    // /uuid
    const QJsonObject uuid = session->recv(sids[5]).json().object();
    assert(uuid.value("uuid").toString().length()==36);

    // /json
    const QJsonObject doc = session->recv(sids[6]).json().object();
    assert(doc.value("slideshow").isObject());
    assert(doc.value("slideshow").toObject().value("slides").isArray());

    // /html
    const Response html = session->recv(sids[7]);
    assert(html.getHeader("content-type")=="text/html");
    assert(html.text().toLower().contains("<html"));

    // /xml
    const Response xml = session->recv(sids[8]);
    assert(xml.getHeader("content-type")=="application/xml");
    assert(xml.text().contains("<slideshow"));

    // /robots.txt
    const Response robots = session->recv(sids[9]);
    assert(robots.getHeader("content-type")=="text/plain");
    assert(!robots.bytes().isEmpty());

    // /image/png：二进制响应
    const Response png = session->recv(sids[10]);
    assert(png.getHeader("content-type")=="image/png");
    const QByteArray pngBytes = png.bytes();
    assert(pngBytes.size() > 4 && static_cast<quint8>(pngBytes[0]) == 0x89
        && pngBytes[1] == 'P' && pngBytes[2] == 'N' && pngBytes[3] == 'G');

    // gzip
    const QJsonObject gzip = session->recv(sids[11]).json().object();
    assert(gzip.value("gzipped").toBool()==true);

    // deflate
    const QJsonObject deflate = session->recv(sids[12]).json().object();
    assert(deflate.value("deflated").toBool()==true);
    //brotli
    const QJsonObject brotli = session->recv(sids[13]).json().object();
    assert(brotli.value("brotli").toBool()==true);

    // /stream/5
    const Response stream = session->recv(sids[14]);
    assert(stream.statusCode() == 200);
    const QString text = stream.text();
    assert(text.trimmed().split("\n").size() == 5);
    assert(text.contains("\"id\""));

    // 15. /bytes/2048：原始字节长度精确匹配
    const Response body = session->recv(sids[15]);
    assert(body.statusCode() == 200);
    assert(body.bytes().size()==2048);

    // 16. /etag/reqrio：响应头可读
    const Response etag = session->recv(sids[16]);
    assert(etag.statusCode() == 200);
    assert(etag.getHeader("etag")=="reqrio");

    // SNI：IP 直连 + 自定义 sni
    auto *url = new Url("https://178.236.41.45/get", "httpbin.reqrio.com");
    const uint64_t sid = session->send(GET, url, new Body());
    assert(session->recv(sid).statusCode() == 200);
    Q_UNUSED(alpn)
}


/// Request method testing
void http_method(const Session *session) {
    QJsonObject post_json;
    post_json.insert("name", "reqrio");
    post_json.insert("num", 7);
    post_json.insert("ok", true);

    QJsonObject put_json;
    put_json.insert("k", "v");

    const uint64_t getSid = session->send(GET, "https://httpbin.reqrio.com/get");
    const uint64_t postSid = session->send(POST, "https://httpbin.reqrio.com/post", new Body(QJsonDocument(post_json)));
    const uint64_t putSid = session->send(PUT, "https://httpbin.reqrio.com/put", new Body(QJsonDocument(put_json)));
    const uint64_t headSid = session->send(HEAD, "https://httpbin.reqrio.com/get");
    const uint64_t deleteSid = session->send(DELETE, "https://httpbin.reqrio.com/delete", new Body(QString("bye")));
    const uint64_t optionsSid = session->send(OPTIONS, "https://httpbin.reqrio.com/anything");
    auto r1 = session->send(TRACE, "https://httpbin.reqrio.com/get");
    auto r2 = session->send(PATCH, "https://httpbin.reqrio.com/patch", new Body(QString("x=1")));
    auto r3 = session->send(QUERY, "https://httpbin.reqrio.com/query", new Body(QString("x=1")));
    assert(r1>0);
    assert(r2>0);
    assert(r3>0);


    // GET
    const QJsonObject get = session->recv(getSid).json().object();
    assert(get.value("method").toString()=="GET");

    // POST
    const QJsonObject post = session->recv(postSid).json().object();
    assert(post.value("method").toString()=="POST");
    const QString pd = post.value("data").toString();
    assert(pd==QJsonDocument(post_json).toJson());

    // PUT
    const QJsonObject put = session->recv(putSid).json().object();
    assert(put.value("method").toString()=="PUT");
    assert(put.value("data").toString()==QJsonDocument(put_json).toJson());


    // HEAD
    const Response head = session->recv(headSid);
    assert(head.statusCode() == 200);
    assert(!head.getHeader("content-length").isEmpty());
    assert(head.bytes().isEmpty());

    // DELETE
    const QJsonObject del = session->recv(deleteSid).json().object();
    assert(del.value("method").toString()=="DELETE");
    assert(del.value("data").toString()=="bye");

    // OPTIONS
    const Response options = session->recv(optionsSid);
    assert(options.statusCode() == 200);
    assert(options.getHeader("access-control-allow-methods").startsWith("GET, POST, PUT, DELETE, PATCH, OPTIONS"));

    // TRACE-405 / CONNECT-405 / PATCH-405 / QUERY-不通：todo()
}


/// Request status testing
void http_status(const Session *session) {
    const QList codes = {200, 201, 202, 204, 206, 301, 302, 304, 400, 401, 403, 404, 418, 500, 502, 503};
    QList<uint64_t> sids;
    for (const int code: codes) {
        sids.append(session->send(GET, "https://httpbin.reqrio.com/status/" + QString::number(code)));
    }
    for (int i = 0; i < codes.size(); i++) {
        const Response resp = session->recv(sids[i]);
        assert(resp.statusCode()==codes[i]);
    }
}

/// Authentication Request Test
void http_auth(const Session *session) {
    // 先发送未认证请求
    const uint64_t sidUnAuth = session->send(GET, "https://httpbin.reqrio.com/basic-auth/user/passwd");
    session->addHeader("Authorization", "Basic dXNlcjpwYXNzd2Q=");
    const uint64_t sidAuth = session->send(GET, "https://httpbin.reqrio.com/basic-auth/user/passwd");
    const uint64_t sidBearer = session->send(GET, "https://httpbin.reqrio.com/bearer");

    // 未认证
    const Response un_auth = session->recv(sidUnAuth);
    assert(un_auth.statusCode()==401);
    // 已认证
    const Response auth = session->recv(sidAuth);
    assert(auth.statusCode()==200);
    assert(auth.json().object().value("authenticated").toBool()==true);
    // 未认证
    assert(session->recv(sidBearer).statusCode()==401);
}

/// Request parameter testing
void query_param(const Session *session) {
    auto *u1 = new Url("https://httpbin.reqrio.com/get");
    u1->addParam("q", "'reqrio'");
    u1->addParam("v", "\"reqrio\"");
    u1->addParam("n", "42");
    u1->addParam("b", "true");

    auto *u2 = new Url("https://httpbin.reqrio.com/get?existing=1");
    u2->addParam("added", "2");

    auto *u3 = new Url("https://httpbin.reqrio.com/get");
    u3->addParam("cn", "中文");
    u3->addParam("sp", "a b&c=d");
    u3->addParam("empty", "");

    auto *u4 = new Url("https://httpbin.reqrio.com/get");
    u4->addParam("f", "1.5");
    u4->addParam("arr", "x,y");

    const uint64_t sid1 = session->send(GET, u1, new Body());
    const uint64_t sid2 = session->send(GET, u2, new Body());
    const uint64_t sid3 = session->send(GET, u3, new Body());
    const uint64_t sid4 = session->send(GET, u4, new Body());

    // P1
    QJsonObject args = session->recv(sid1).json().object().value("args").toObject();
    assert(args.value("q").toString()=="'reqrio'");
    assert(args.value("v").toString()=="\"reqrio\"");
    assert(args.value("n").toString()=="42");
    assert(args.value("b").toString()=="true");

    // P2
    args = session->recv(sid2).json().object().value("args").toObject();
    assert(args.value("existing").toString()=="1");
    assert(args.value("added").toString()=="2");

    // P3
    args = session->recv(sid3).json().object().value("args").toObject();
    assert(args.value("cn").toString()=="中文");
    assert(args.value("sp").toString()=="a b&c=d");
    assert(args.value("empty").toString()=="");

    // P4
    args = session->recv(sid4).json().object().value("args").toObject();
    assert(args.value("f").toString()=="1.5");
    assert(args.value("arr").toString()=="x,y");
}


/// Redirection test
/// * Non automatic redirection can be done using send
/// * Automatic redirection cannot use send and recv
void http_redirect(const Session *session) {
    session->setRedirect(false);
    const uint64_t sid1 = session->send(GET, "https://httpbin.reqrio.com/redirect/2");
    const uint64_t sid2 = session->send(GET, "https://httpbin.reqrio.com/relative-redirect/1");
    const uint64_t sid3 = session->send(GET, "https://httpbin.reqrio.com/redirect-to?url=/get&status_code=307");
    const uint64_t sid4 = session->send(GET, "https://httpbin.reqrio.com/absolute-redirect/2");

    const Response r1 = session->recv(sid1);
    assert(r1.statusCode()==302);
    assert(r1.getHeader("location")=="/relative-redirect/1");

    const Response r2 = session->recv(sid2);
    assert(r2.statusCode()==302);
    assert(r2.getHeader("location")=="/get");

    const Response r3 = session->recv(sid3);
    assert(r3.statusCode()==307);
    assert(r3.getHeader("location")=="/get");

    const Response r4 = session->recv(sid4);
    assert(r4.statusCode()==302);
    assert(r4.getHeader("location").startsWith("https://"));

    // 自动重定向
    session->setRedirect(true);
    assert(session->get("https://httpbin.reqrio.com/redirect/2").statusCode() == 200);
    assert(session->get("https://httpbin.reqrio.com/relative-redirect/1").statusCode() == 200);
    assert(session->get("https://httpbin.reqrio.com/redirect-to?url=/get&status_code=307").statusCode() == 200);

    // set_max_redirect_exceeds:todo()
}

/// Request Cookie Test
void http_cookie(const Session *session) {
    session->setRedirect(false);
    const Response r1 = session->get("https://httpbin.reqrio.com/cookies/set?k=v&k2=v2");
    assert(r1.statusCode()==302);
    assert(r1.getHeader("location")=="/cookies");
    bool k = false, k2 = false;
    for (Cookie cookie: r1.cookies()) {
        if (cookie.getName() == "k" && cookie.getValue() == "v") k = true;
        if (cookie.getName() == "k2" && cookie.getValue() == "v2") k2 = true;
    }
    assert(k && k2);

    // remove_cookie: todo()
    const Response r2 = session->get("https://httpbin.reqrio.com/cookies/delete?k");
    assert(r2.statusCode()==302);

    session->setRedirect(true);
    const QJsonObject res = session->get("https://httpbin.reqrio.com/cookies").json().object();
    const QJsonObject cookies = res.value("cookies").toObject();
    assert(cookies.value("k2").toString()=="v2");
    if (cookies.contains("k"))
        assert(cookies.value("k").toString()=="");
}

/// Request Header Test
void http_header(const Session *session) {
    QJsonObject header;
    header.insert("User-Agent", "reqrio-test,reqrio-test");
    header.insert("X-Reqrio-Test", "hello");
    header.insert("Accept-Language", "zh-CN,zh;q=0.9");
    header.insert("X-Multi", "a");
    header.insert("X-Int", 1);
    header.insert("X-Bool", true);
    header.insert("X-Object", QJsonObject{{"k", "v"}});
    header.insert("X-Array", QJsonArray{"1", 1, true});
    session->setHeader(QJsonDocument(header));

    const uint64_t sid1 = session->send(GET, "https://httpbin.reqrio.com/headers");
    const uint64_t sid2 = session->send(GET, "https://httpbin.reqrio.com/user-agent");
    const uint64_t sid3 = session->send(POST, "https://httpbin.reqrio.com/post",
                                        new Body(QJsonDocument(QJsonObject{{"with", "header"}})));

    // Append + Overwrite
    session->addHeader("X-Late-Header", "late");
    session->addHeader("X-Reqrio-Test", "overwritten");
    const uint64_t sid4 = session->send(GET, "https://httpbin.reqrio.com/headers");
    const uint64_t sid5 = session->send(GET, "https://httpbin.reqrio.com/anything?phase=late");

    const QString h1_str = session->recv(sid1).text().toLower();
    const QJsonObject h1 = QJsonDocument::fromJson(h1_str.toUtf8()).object().value("headers").toObject();
    assert(h1.value("user-agent").toString()=="reqrio-test,reqrio-test");
    assert(h1.value("x-reqrio-test").toString()=="hello");
    assert(h1.value("accept-language").toString()=="zh-cn,zh;q=0.9");
    assert(h1.value("x-multi").toString()=="a");
    assert(h1.value("x-int").toString()=="1");
    assert(h1.value("x-bool").toString()=="true");
    assert(h1.value("x-object").toString()=="{\"k\":\"v\"}");
    assert(h1.value("x-array").toString()=="[\"1\",1,true]");

    const QJsonObject h2 = session->recv(sid2).json().object();
    assert(h2.value("user-agent").toString()=="reqrio-test,reqrio-test");

    const QJsonObject h3 = session->recv(sid3).json().object();
    assert(QJsonDocument(h3.value("json").toObject()).toJson(QJsonDocument::Compact)=="{\"with\":\"header\"}");

    for (const uint64_t sid: {sid4, sid5}) {
        const QString res = session->recv(sid).text().toLower();
        const QJsonObject hh = QJsonDocument::fromJson(res.toUtf8()).object().value("headers").toObject();
        assert(hh.value("user-agent").toString()=="reqrio-test,reqrio-test");
        assert(hh.value("x-reqrio-test").toString()=="overwritten");
        assert(hh.value("accept-language").toString()=="zh-cn,zh;q=0.9");
        assert(hh.value("x-multi").toString()=="a");
        assert(hh.value("x-int").toString()=="1");
        assert(hh.value("x-bool").toString()=="true");
        assert(hh.value("x-object").toString()=="{\"k\":\"v\"}");
        assert(hh.value("x-array").toString()=="[\"1\",1,true]");
        assert(hh.value("x-late-header").toString()=="late");
    }
}

void http_stream(const Session *session) {
    const uint64_t sid1 = session->send(GET, "https://httpbin.reqrio.com/bytes/16384");
    const uint64_t sid2 = session->send(GET, "https://httpbin.reqrio.com/stream/3");
    assert(sid1 != sid2);

    Response r1 = session->recv(sid1, true);
    int total = 0;
    int chunks = 0;
    for (const QByteArray &chunk: r1.chunks()) {
        assert(!chunk.isEmpty());
        total += chunk.size();
        chunks++;
    }
    assert(total==16384);
    assert(chunks >= 1);

    Response r2 = session->recv(sid2, true);
    for (const QByteArray &chunk: r2.chunks()) {
        assert(chunk.contains('\n'));
    }
}

/// Form + JSON + File + Text + Bytes
void http_body(const Session *session) {
    QJsonObject d1;
    d1.insert("str1", "s");
    d1.insert("str2", "'s'");
    d1.insert("str3", "\"s\"");
    d1.insert("int", 0);
    d1.insert("bool", true);
    d1.insert("object", QJsonObject{{"data", "value"}});
    d1.insert("array", QJsonArray{"2", 0, true, QJsonObject{{"k", "v"}}});

    QJsonObject d2;
    d2.insert("k1", "v1");
    d2.insert("k2", "v2");

    QMap<QString, QString> d2_form;
    d2_form.insert("k1", "v1");
    d2_form.insert("k2", "v2");

    const QString text = "sdfhsdjkfsdljhkgs;gfkgsdfhsdasugdsadgsauydgtasdfytsad";
    QByteArray data;
    data.resize(4096);
    for (int i = 0; i < data.size(); i++) data[i] = static_cast<char>(i);

    const QTemporaryDir dir;
    assert(dir.isValid());
    const QString path1 = dir.filePath("f1.bin");
    const QString path2 = dir.filePath("f2.bin");
    {
        QFile f1(path1);
        assert(f1.open(QIODevice::WriteOnly));
        f1.write(data);
        f1.close();
        QFile f2(path2);
        assert(f2.open(QIODevice::WriteOnly));
        f2.write(data.left(1024));
        f2.close();
    }

    // form
    const uint64_t formSid = session->send(POST, "https://httpbin.reqrio.com/post", new Body(d2_form));
    // json
    const uint64_t jsonSid = session->send(POST, "https://httpbin.reqrio.com/post", new Body(QJsonDocument(d1)));
    // bytes
    const uint64_t bytesSid = session->send(POST, "https://httpbin.reqrio.com/post",
                                            new Body(data, "application/octet-stream"));
    // text
    const uint64_t textSid = session->send(POST, "https://httpbin.reqrio.com/post", new Body(QString(text)));
    // file
    auto *f1 = Body::fromFiles({path1}, {"file"});
    const uint64_t fileSid = session->send(POST, "https://httpbin.reqrio.com/post", f1);
    // // file + form
    auto *f2 = Body::fromFiles({path1}, {"file"}, QJsonDocument(d2));
    const uint64_t fileFormSid = session->send(POST, "https://httpbin.reqrio.com/post", f2);
    // files + form + params
    auto *url = new Url("https://httpbin.reqrio.com/post");
    for (auto it = d2_form.cbegin(); it != d2_form.cend(); ++it) url->addParam(it.key(), it.value());
    auto *f3 = Body::fromFiles({path1, path2}, {"file1", "file2"}, QJsonDocument(d2));
    const uint64_t filesSid = session->send(POST, url, f3);

    // form
    const QJsonObject r1 = session->recv(formSid).json().object();
    assert(r1.value("form").toObject()==d2);
    const QJsonObject h1 = r1.value("headers").toObject();
    if (h1.value("content-type").isUndefined())
        assert(h1.value("Content-Type").toString() == "application/x-www-form-urlencoded");
    else
        assert(h1.value("content-type").toString() == "application/x-www-form-urlencoded");


    // json
    const QJsonObject r2 = session->recv(jsonSid).json().object();
    assert(r2.value("json").toObject()==d1);
    const QJsonObject h2 = r2.value("headers").toObject();
    if (h2.value("content-type").isUndefined())
        assert(h2.value("Content-Type") == "application/json");
    else
        assert(h2.value("content-type") == "application/json");

    // bytes
    const QJsonObject r3 = session->recv(bytesSid).json().object();
    const QJsonObject h3 = r3.value("headers").toObject();
    if (h3.value("content-type").isUndefined())
        assert(h3.value("Content-Type") == "application/octet-stream");
    else
        assert(h3.value("content-type") == "application/octet-stream");
    const QString pd3 = r3.value("data").toString().replace("data:application/octet-stream;base64,", "");
    assert(QByteArray::fromBase64(pd3.toUtf8()) == data);

    // text
    const QJsonObject r4 = session->recv(textSid).json().object();
    const QJsonObject h4 = r4.value("headers").toObject();
    if (h4.value("content-type").isUndefined())
        assert(h4.value("Content-Type") == "text/plain");
    else
        assert(h4.value("content-type") == "text/plain");
    assert(r4.value("data").toString()==text);

    // file
    const QJsonObject r5 = session->recv(fileSid).json().object();
    const QJsonObject h5 = r5.value("headers").toObject();
    if (h5.value("content-type").isUndefined())
        assert(h5.value("Content-Type").toString().startsWith("multipart/form-data; boundary=----WebKitFormBoundary"));
    else
        assert(h5.value("content-type").toString().startsWith("multipart/form-data; boundary=----WebKitFormBoundary"));
    QString pd5 = r5.value("files").toObject().value("file").toString();
    pd5 = pd5.replace("data:application/octet-stream;base64,", "");
    assert(QByteArray::fromBase64(pd5.toUtf8()) == data);

    // file + form
    const QJsonObject r6 = session->recv(fileFormSid).json().object();
    const QJsonObject h6 = r6.value("headers").toObject();
    if (h6.value("content-type").isUndefined())
        assert(h6.value("Content-Type").toString().startsWith("multipart/form-data; boundary=----WebKitFormBoundary"));
    else
        assert(h6.value("content-type").toString().startsWith("multipart/form-data; boundary=----WebKitFormBoundary"));
    assert(r6.value("form").toObject()==d2);
    QString pd6 = r6.value("files").toObject().value("file").toString();
    pd6 = pd6.replace("data:application/octet-stream;base64,", "");
    assert(QByteArray::fromBase64(pd6.toUtf8()) == data);

    // files + form + params
    const QJsonObject r7 = session->recv(filesSid).json().object();
    const QJsonObject h7 = r6.value("headers").toObject();
    if (h7.value("content-type").isUndefined())
        assert(h7.value("Content-Type").toString().startsWith("multipart/form-data; boundary=----WebKitFormBoundary"));
    else
        assert(h7.value("content-type").toString().startsWith("multipart/form-data; boundary=----WebKitFormBoundary"));
    QString pd7 = r7.value("files").toObject().value("file1").toString();
    pd7 = pd7.replace("data:application/octet-stream;base64,", "");
    assert(QByteArray::fromBase64(pd7.toUtf8()) == data);

    QString pd8 = r7.value("files").toObject().value("file2").toString();
    pd8 = pd8.replace("data:application/octet-stream;base64,", "");
    assert(QByteArray::fromBase64(pd8.toUtf8()) == data.mid(0,1024));

    assert(r7.value("form")==d2);
    assert(r7.value("args")==d2);
}


void http_concurrency(const Session *session) {
    QList<uint64_t> sids;
    for (int i = 0; i < 6; i++) {
        sids.append(session->send(GET, "https://httpbin.reqrio.com/get?i=" + QString::number(i)));
    }
    sids.append(session->send(POST, "https://httpbin.reqrio.com/post", new Body(QString("h2-body"))));
    sids.append(session->send(GET, "https://httpbin.reqrio.com/gzip"));
    sids.append(session->send(GET, "https://httpbin.reqrio.com/brotli"));
    sids.append(session->send(GET, "https://httpbin.reqrio.com/bytes/4096"));

    while (!sids.isEmpty()) {
        const int pos = QRandomGenerator::global()->bounded(sids.size());
        const Response resp = session->recv(sids[pos]);
        assert(resp.statusCode()==200);
        sids.removeAt(pos);
    }
}
