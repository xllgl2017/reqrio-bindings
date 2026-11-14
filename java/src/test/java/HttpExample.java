import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.xllgl2017.*;
import org.xllgl2017.tls.ALPN;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;

public class HttpExample {

    static void assertTrue(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
    }

    static void assertEq(Object got, Object want, String msg) {
        if (!String.valueOf(got).equals(String.valueOf(want))) {
            throw new AssertionError(msg + ": got=" + got + ", want=" + want);
        }
    }

    static void assertJson(JsonElement got, JsonElement want, String msg) {
        if (got == null || !got.equals(want)) {
            throw new AssertionError(msg + ": got=" + got + ", want=" + want);
        }
    }

    /**
     * Testing different types of requests
     */
    static void httpGetMatrix(Session session, ALPN ignoredAlpn) throws Exception {
        int[] sids = {
                session.send(Method.GET, "https://httpbin.reqrio.com"),                   // 0 首页
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
                session.send(Method.GET, "https://httpbin.reqrio.com/etag/reqrio"),       // 16 ETag
        };

        // /home
        Response home = session.recv(sids[0]);
        assertEq(home.statusCode(), 200, "home status");
        assertTrue(home.getHeader("content-type").startsWith("text/html"), "home content-type");
        assertTrue(home.bytes().length > 0, "home empty body");
        assertTrue(home.text().toLowerCase().contains("httpbin"), "home body");

        // /get
        JsonObject get = session.recv(sids[1]).json().getAsJsonObject();
        assertEq(get.get("url").getAsString(), "https://httpbin.reqrio.com/get", "url");
        assertEq(get.get("method").getAsString(), "GET", "method");
        assertTrue(get.get("headers").isJsonObject(), "headers");

        // /headers
        JsonObject headers = session.recv(sids[2]).json().getAsJsonObject();
        assertTrue(headers.toString().toLowerCase().contains("httpbin.reqrio.com"), "headers");

        // /user-agent
        JsonObject ua = session.recv(sids[3]).json().getAsJsonObject();
        assertTrue(ua.toString().toLowerCase().contains("reqrio"), "user-agent");

        // /ip
        JsonObject ip = session.recv(sids[4]).json().getAsJsonObject();
        assertTrue(!ip.get("origin").toString().isEmpty(), "origin");

        // /uuid
        JsonObject uuid = session.recv(sids[5]).json().getAsJsonObject();
        assertEq(uuid.get("uuid").getAsString().length(), 36, "uuid length");

        // /json
        JsonObject doc = session.recv(sids[6]).json().getAsJsonObject();
        assertTrue(doc.get("slideshow").isJsonObject(), "slideshow");
        assertTrue(doc.getAsJsonObject("slideshow").get("slides").isJsonArray(), "slides");

        // /html
        Response html = session.recv(sids[7]);
        assertTrue(html.getHeader("content-type").startsWith("text/html"), "html content-type");
        assertTrue(html.text().toLowerCase().contains("<html"), "html body");

        // /xml
        Response xml = session.recv(sids[8]);
        assertTrue(xml.getHeader("content-type").startsWith("application/xml"), "xml content-type");
        assertTrue(xml.text().contains("<slideshow"), "xml body");

        // /robots.txt
        Response robots = session.recv(sids[9]);
        assertTrue(robots.getHeader("content-type").startsWith("text/plain"), "robots content-type");
        assertTrue(robots.bytes().length > 0, "robots body");

        // /image/png
        Response png = session.recv(sids[10]);
        assertTrue(png.getHeader("content-type").startsWith("image/png"), "png content-type");
        byte[] pngBytes = png.bytes();
        assertTrue((pngBytes[0] & 0xFF) == 0x89 && pngBytes[1] == 'P' && pngBytes[2] == 'N' && pngBytes[3] == 'G', "not png");

        // gzip / deflate / brotli
        assertEq(session.recv(sids[11]).json().getAsJsonObject().get("gzipped").getAsBoolean(), true, "gzip");
        assertEq(session.recv(sids[12]).json().getAsJsonObject().get("deflated").getAsBoolean(), true, "deflate");
        assertEq(session.recv(sids[13]).json().getAsJsonObject().get("brotli").getAsBoolean(), true, "brotli");

        // /stream/5
        Response stream = session.recv(sids[14]);
        assertEq(stream.statusCode(), 200, "stream status");
        String text = stream.text();
        assertEq(text.trim().split("\n").length, 5, "stream lines");
        assertTrue(text.contains("\"id\""), "stream body");

        // 15. /bytes/2048
        Response body = session.recv(sids[15]);
        assertEq(body.statusCode(), 200, "bytes status");
        assertEq(body.bytes().length, 2048, "bytes length");

        // 16. /etag/reqrio
        Response etag = session.recv(sids[16]);
        assertEq(etag.statusCode(), 200, "etag status");
        assertEq(etag.getHeader("etag"), "reqrio", "etag");

        // SNI：IP + sni
        int sid = session.send(Method.GET, new Url("https://178.236.41.45/get", "httpbin.reqrio.com"), new Body());
        assertEq(session.recv(sid).statusCode(), 200, "sni status");
    }

    /**
     * Request method testing
     */
    static void httpMethod(Session session) throws Exception {
        JsonObject postJson = new JsonObject();
        postJson.addProperty("name", "reqrio");
        postJson.addProperty("num", 7);
        postJson.addProperty("ok", true);

        JsonObject putJson = new JsonObject();
        putJson.addProperty("k", "v");

        int getSid = session.send(Method.GET, "https://httpbin.reqrio.com/get");
        int postSid = session.send(Method.POST, "https://httpbin.reqrio.com/post", new Body(postJson));
        int putSid = session.send(Method.PUT, "https://httpbin.reqrio.com/put", new Body(putJson));
        int headSid = session.send(Method.HEAD, "https://httpbin.reqrio.com/get");
        int deleteSid = session.send(Method.DELETE, "https://httpbin.reqrio.com/delete", new Body("bye"));
        int optionsSid = session.send(Method.OPTIONS, "https://httpbin.reqrio.com/anything");
        session.send(Method.TRACE, "https://httpbin.reqrio.com/get");
        session.send(Method.PATCH, "https://httpbin.reqrio.com/patch", new Body("x=1"));
        session.send(Method.QUERY, "https://httpbin.reqrio.com/query", new Body("x=1"));

        // GET
        assertEq(session.recv(getSid).json().getAsJsonObject().get("method").getAsString(), "GET", "GET");

        // POST
        Response post = session.recv(postSid);
        JsonObject postBody = post.json().getAsJsonObject();
        assertEq(postBody.get("method").getAsString(), "POST", "POST");
        JsonElement postData = new Gson().fromJson(postBody.get("data").getAsString(), JsonElement.class);
        assertJson(postData, postJson, "POST data");

        // PUT
        Response put = session.recv(putSid);
        JsonObject putBody = put.json().getAsJsonObject();
        assertEq(putBody.get("method").getAsString(), "PUT", "PUT");
        JsonElement putData = new Gson().fromJson(putBody.get("data").getAsString(), JsonElement.class);
        assertJson(putData, putJson, "PUT data");

        // HEAD
        Response head = session.recv(headSid);
        assertEq(head.statusCode(), 200, "HEAD status");
        assertTrue(!head.getHeader("content-length").isEmpty(), "HEAD content-length");
        assertEq(head.bytes().length, 0, "HEAD body");

        // DELETE
        JsonObject del = session.recv(deleteSid).json().getAsJsonObject();
        assertEq(del.get("method").getAsString(), "DELETE", "DELETE");
        assertEq(del.get("data").getAsString(), "bye", "DELETE data");

        // OPTIONS
        Response options = session.recv(optionsSid);
        assertEq(options.statusCode(), 200, "OPTIONS status");
        assertTrue(options.getHeader("access-control-allow-methods")
                .contains("GET, POST, PUT, DELETE, PATCH, OPTIONS"), "OPTIONS allow");

        // TRACE-405 / CONNECT-405 / PATCH-405 / QUERY-不通
        //todo()
    }

    /**
     * Authentication Request Test
     */
    static void httpAuth(Session s) throws Exception {
        // 先发送未认证请求
        int sidUnAuth = s.send(Method.GET, "https://httpbin.reqrio.com/basic-auth/user/passwd");
        s.setHeader("Authorization", "Basic dXNlcjpwYXNzd2Q=");
        int sidAuth = s.send(Method.GET, "https://httpbin.reqrio.com/basic-auth/user/passwd");
        int sidBearer = s.send(Method.GET, "https://httpbin.reqrio.com/bearer");

        // 未认证
        assertEq(s.recv(sidUnAuth).statusCode(), 401, "un auth");
        // 已认证
        Response auth = s.recv(sidAuth);
        assertEq(auth.statusCode(), 200, "auth");
        assertEq(auth.json().getAsJsonObject().get("authenticated").getAsBoolean(), true, "authenticated");
        // 未认证
        assertEq(s.recv(sidBearer).statusCode(), 401, "bearer");
    }

    /**
     * Request status testing
     */
    static void httpStatus(Session s) throws Exception {
        int[] codes = {200, 201, 202, 204, 206, 301, 302, 304, 400, 401, 403, 404, 418, 500, 502, 503};
        List<Integer> sids = new ArrayList<>();
        for (int code : codes) {
            sids.add(s.send(Method.GET, "https://httpbin.reqrio.com/status/" + code));
        }
        for (int i = 0; i < codes.length; i++) {
            assertEq(s.recv(sids.get(i)).statusCode(), codes[i], "status " + codes[i]);
        }
    }

    /**
     * Request parameter testing
     */
    static void queryParam(Session s) throws Exception {
        Url u1 = new Url("https://httpbin.reqrio.com/get");
        u1.addParam("q", "'reqrio'");
        u1.addParam("v", "\"reqrio\"");
        u1.addParam("n", "42");
        u1.addParam("b", "true");

        Url u2 = new Url("https://httpbin.reqrio.com/get?existing=1");
        u2.addParam("added", "2");

        Url u3 = new Url("https://httpbin.reqrio.com/get");
        u3.addParam("cn", "中文");
        u3.addParam("sp", "a b&c=d");
        u3.addParam("empty", "");

        Url u4 = new Url("https://httpbin.reqrio.com/get");
        u4.addParam("f", "1.5");
        u4.addParam("arr", "x,y");

        int sid1 = s.send(Method.GET, u1);
        int sid2 = s.send(Method.GET, u2);
        int sid3 = s.send(Method.GET, u3);
        int sid4 = s.send(Method.GET, u4);

        // P1
        JsonObject args = s.recv(sid1).json().getAsJsonObject().getAsJsonObject("args");
        assertEq(args.get("q").getAsString(), "'reqrio'", "q");
        assertEq(args.get("v").getAsString(), "\"reqrio\"", "v");
        assertEq(args.get("n").getAsString(), "42", "n");
        assertEq(args.get("b").getAsString(), "true", "b");

        // P2
        args = s.recv(sid2).json().getAsJsonObject().getAsJsonObject("args");
        assertEq(args.get("existing").getAsString(), "1", "existing");
        assertEq(args.get("added").getAsString(), "2", "added");

        // P3
        args = s.recv(sid3).json().getAsJsonObject().getAsJsonObject("args");
        assertEq(args.get("cn").getAsString(), "中文", "cn");
        assertEq(args.get("sp").getAsString(), "a b&c=d", "sp");
        assertEq(args.get("empty").getAsString(), "", "empty");

        // P4
        args = s.recv(sid4).json().getAsJsonObject().getAsJsonObject("args");
        assertEq(args.get("f").getAsString(), "1.5", "f");
        assertEq(args.get("arr").getAsString(), "x,y", "arr");
    }

    /// Redirection test
    /// * Non automatic redirection can be done using send
    /// * Automatic redirection cannot use send and recv
    static void httpRedirect(Session session) throws Exception {
        session.setAutoRedirect(false);
        int sid1 = session.send(Method.GET, "https://httpbin.reqrio.com/redirect/2");
        int sid2 = session.send(Method.GET, "https://httpbin.reqrio.com/relative-redirect/1");
        int sid3 = session.send(Method.GET, "https://httpbin.reqrio.com/redirect-to?url=/get&status_code=307");
        int sid4 = session.send(Method.GET, "https://httpbin.reqrio.com/absolute-redirect/2");

        Response r1 = session.recv(sid1);
        assertEq(r1.statusCode(), 302, "r1 status");
        assertEq(r1.location(), "/relative-redirect/1", "r1 location");

        Response r2 = session.recv(sid2);
        assertEq(r2.statusCode(), 302, "r2 status");
        assertEq(r2.location(), "/get", "r2 location");

        Response r3 = session.recv(sid3);
        assertEq(r3.statusCode(), 307, "r3 status");
        assertEq(r3.location(), "/get", "r3 location");

        Response r4 = session.recv(sid4);
        assertEq(r4.statusCode(), 302, "r4 status");
        assertTrue(r4.location().startsWith("https://"), "r4 location");

        // 自动重定向
        session.setAutoRedirect(true);
        assertEq(session.get("https://httpbin.reqrio.com/redirect/2").statusCode(), 200, "auto redirect 302");
        assertEq(session.get("https://httpbin.reqrio.com/relative-redirect/1").statusCode(), 200, "auto redirect relative");
        assertEq(session.get("https://httpbin.reqrio.com/redirect-to?url=/get&status_code=307").statusCode(), 200, "auto redirect 307");

        // todo(): set_max_redirect_exceeds
    }

    /**
     * Request Cookie Test
     */
    static void httpCookie(Session s) throws Exception {
        s.setAutoRedirect(false);
        Response r1 = s.get("https://httpbin.reqrio.com/cookies/set?k=v&k2=v2");
        assertEq(r1.statusCode(), 302, "set cookie status");
        assertEq(r1.location(), "/cookies", "set cookie location");
        boolean k = false, k2 = false;
        for (Cookie cookie : r1.getCookies()) {
            if ("k".equals(cookie.getName()) && "v".equals(cookie.getValue())) k = true;
            if ("k2".equals(cookie.getName()) && "v2".equals(cookie.getValue())) k2 = true;
        }
        assertTrue(k && k2, "set cookie values");

        // todo(): remove_cookie
        Response r2 = s.get("https://httpbin.reqrio.com/cookies/delete?k");
        assertEq(r2.statusCode(), 302, "delete cookie status");

        s.setAutoRedirect(true);
        JsonObject cookies = s.get("https://httpbin.reqrio.com/cookies").json().getAsJsonObject().getAsJsonObject("cookies");
        assertEq(cookies.get("k2").getAsString(), "v2", "jar k2");
        if (cookies.has("k")) assertEq(cookies.get("k").getAsString(), "", "jar k");
    }

    /**
     * Request Header Test
     */
    static void httpHeader(Session session) throws Exception {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("User-Agent", "reqrio/reqrio-test");
        headers.put("X-Reqrio-Test", "hello");
        headers.put("Accept-Language", "zh-CN,zh;q=0.9");
        headers.put("X-Multi", "a");
        headers.put("X-Int", "1");
        headers.put("X-Bool", "true");
        headers.put("X-Object", "{\"k\":\"v\"}");
        headers.put("X-Array", "[\"1\", 1, true]");
        session.setHeaders(headers);

        int sid1 = session.send(Method.GET, "https://httpbin.reqrio.com/headers");
        int sid2 = session.send(Method.GET, "https://httpbin.reqrio.com/user-agent");
        JsonObject withHeader = new JsonObject();
        withHeader.addProperty("with", "header");
        int sid3 = session.send(Method.POST, "https://httpbin.reqrio.com/post", new Body(withHeader));

        // Append + Overwrite
        session.setHeader("X-Late-Header", "late");
        session.setHeader("X-Reqrio-Test", "overwritten");
        int sid4 = session.send(Method.GET, "https://httpbin.reqrio.com/headers");
        int sid5 = session.send(Method.GET, "https://httpbin.reqrio.com/anything?phase=late");

        JsonObject h1 = new Gson().fromJson(
                session.recv(sid1).text().toLowerCase(), JsonObject.class).getAsJsonObject("headers");
        assertEq(h1.get("user-agent").getAsString(), "reqrio/reqrio-test", "user-agent");
        assertEq(h1.get("x-reqrio-test").getAsString(), "hello", "x-reqrio-test");
        assertEq(h1.get("accept-language").getAsString(), "zh-cn,zh;q=0.9", "accept-language");
        assertEq(h1.get("x-multi").getAsString(), "a", "x-multi");
        assertEq(h1.get("x-int").getAsString(), "1", "x-int");
        assertEq(h1.get("x-bool").getAsString(), "true", "x-bool");
        assertEq(h1.get("x-object").getAsString(), "{\"k\":\"v\"}", "x-object");
        assertEq(h1.get("x-array").getAsString(), "[\"1\", 1, true]", "x-array");

        JsonObject h2 = session.recv(sid2).json().getAsJsonObject();
        assertEq(h2.get("user-agent").getAsString(), "reqrio/reqrio-test", "user-agent echoed");

        JsonObject h3 = session.recv(sid3).json().getAsJsonObject();
        assertJson(new Gson().fromJson(h3.get("data").getAsString(), JsonElement.class), withHeader, "post data");

        for (int sid : new int[]{sid4, sid5}) {
            String header_str = session.recv(sid).text().toLowerCase();
            JsonObject header = new Gson().fromJson(header_str, JsonObject.class).getAsJsonObject("headers");
            assertEq(header.get("user-agent").getAsString(), "reqrio/reqrio-test", "user-agent");
            assertEq(header.get("x-reqrio-test").getAsString(), "overwritten", "x-reqrio-test");
            assertEq(header.get("accept-language").getAsString(), "zh-cn,zh;q=0.9", "accept-language");
            assertEq(header.get("x-multi").getAsString(), "a", "x-multi");
            assertEq(header.get("x-int").getAsString(), "1", "x-int");
            assertEq(header.get("x-bool").getAsString(), "true", "x-bool");
            assertEq(header.get("x-object").getAsString(), "{\"k\":\"v\"}", "x-object");
            assertEq(header.get("x-array").getAsString(), "[\"1\", 1, true]", "x-array");
            assertEq(header.get("x-late-header").getAsString(), "late", "x-late-header");
        }
    }

    static void httpStream(Session session) throws Exception {
        int sid1 = session.send(Method.GET, "https://httpbin.reqrio.com/bytes/16384");
        int sid2 = session.send(Method.GET, "https://httpbin.reqrio.com/stream/3");

        int total = 0, chunks = 0;
        for (byte[] chunk : session.recv(sid1, true).chunks()) {
            assertTrue(chunk.length > 0, "empty chunk");
            total += chunk.length;
            chunks++;
        }
        assertEq(total, 16384, "stream total");
        assertTrue(chunks >= 1, "chunks");

        for (byte[] chunk : session.recv(sid2, true).chunks()) {
            String text = new String(chunk);
            assertTrue(text.contains("\n"), "stream chunk");
        }
        assertTrue(sid1 != sid2, "sid");
    }

    /**
     * Form + JSON + File + Text + Bytes
     */
    static void httpBody(Session session) throws Exception {
        JsonObject d1 = new JsonObject();
        d1.addProperty("str1", "s");
        d1.addProperty("str2", "'s'");
        d1.addProperty("str3", "\"s\"");
        d1.addProperty("int", 0);
        d1.addProperty("bool", true);
        JsonObject obj = new JsonObject();
        obj.addProperty("data", "value");
        d1.add("object", obj);
        JsonArray array = new JsonArray();
        array.add("2");
        array.add(0);
        array.add(true);
        JsonObject nested = new JsonObject();
        nested.addProperty("k", "v");
        array.add(nested);
        d1.add("array", array);

        HashMap<String, String> d2 = new HashMap<>();
        d2.put("k1", "v1");
        d2.put("k2", "v2");
        String text = "sdfhsdjkfsdljhkgs;gfkgsdfhsdasugdsadgsauydgtasdfytsad";
        byte[] data = new byte[4096];
        for (int i = 0; i < data.length; i++) data[i] = (byte) i;

        Path dir = Files.createTempDirectory("reqrio-http-test-");
        Path path1 = dir.resolve("f1.bin");
        Path path2 = dir.resolve("f2.bin");
        Files.write(path1, data);
        byte[] head = new byte[1024];
        System.arraycopy(data, 0, head, 0, head.length);
        Files.write(path2, head);

        // form
        int f1Sid = session.send(Method.POST, "https://httpbin.reqrio.com/post", new Body(d2));
        // json
        int jsonSid = session.send(Method.POST, "https://httpbin.reqrio.com/post", new Body(d1));
        // bytes
        int bytesSid = session.send(Method.POST, "https://httpbin.reqrio.com/post",
                new Body(data, "application/octet-stream"));
        // text
        int textSid = session.send(Method.POST, "https://httpbin.reqrio.com/post", new Body(text));
        // file
        HttpFile file = new HttpFile();
        file.addFile(path1.toString(), "file", "application/octet-stream");
        int fileSid = session.send(Method.POST, "https://httpbin.reqrio.com/post", new Body(file));
        // file + form
        HttpFile file2 = new HttpFile();
        file2.addFile(path1.toString(), "file", "application/octet-stream");
        int fileFormSid = session.send(Method.POST, "https://httpbin.reqrio.com/post", new Body(file2, d2));
        // files + form + params
        HttpFile files = new HttpFile();
        files.addFile(path1.toString(), "file1", "application/octet-stream");
        files.addFile(path2.toString(), "file2", "application/octet-stream");
        Url url = new Url("https://httpbin.reqrio.com/post");
        for (String key : d2.keySet()) url.addParam(key, d2.get(key));
        int filesSid = session.send(Method.POST, url, new Body(files, d2));

        // form
        JsonObject r1 = session.recv(f1Sid).json().getAsJsonObject();
        assertJson(r1.getAsJsonObject("form"), new Gson().fromJson(new Gson().toJson(d2), JsonObject.class), "form");
        JsonObject h1 = r1.getAsJsonObject("headers");
        if (h1.get("content-type") == null)
            assertEq(h1.get("Content-Type").getAsString(), "application/x-www-form-urlencoded", "");
        else
            assertEq(h1.get("content-type").getAsString(), "application/x-www-form-urlencoded", "");

        // json
        JsonObject r2 = session.recv(jsonSid).json().getAsJsonObject();
        assertJson(r2.getAsJsonObject("json"), d1, "json");
        JsonObject h2 = r2.getAsJsonObject("headers");
        if (h2.get("content-type") == null)
            assertEq(h2.get("Content-Type").getAsString(), "application/json", "");
        else
            assertEq(h2.get("content-type").getAsString(), "application/json", "");

        // bytes
        JsonObject r3 = session.recv(bytesSid).json().getAsJsonObject();
        JsonObject h3 = r3.getAsJsonObject("headers");
        if (h3.get("content-type") == null)
            assertEq(h3.get("Content-Type").getAsString(), "application/octet-stream", "");
        else
            assertEq(h3.get("content-type").getAsString(), "application/octet-stream", "");
        String dd = r3.get("data").getAsString().replace("data:application/octet-stream;base64,", "");
        assertTrue(java.util.Arrays.equals(Base64.getDecoder().decode(dd), data), "bytes body");

        // text
        JsonObject r4 = session.recv(textSid).json().getAsJsonObject();
        JsonObject h4 = r4.getAsJsonObject("headers");
        if (h4.get("content-type") == null)
            assertEq(h4.get("Content-Type").getAsString(), "text/plain", "");
        else
            assertEq(h4.get("content-type").getAsString(), "text/plain", "");
        assertEq(r4.get("data").getAsString(), text, "text body");

        // file
        JsonObject r5 = session.recv(fileSid).json().getAsJsonObject();
        JsonObject h5 = r5.getAsJsonObject("headers");
        if (h5.get("content-type") == null)
            assertTrue(h5.get("Content-Type").getAsString().startsWith("multipart/form-data; boundary=----WebKitFormBoundary"), "");
        else
            assertTrue(h5.get("content-type").getAsString().startsWith("multipart/form-data; boundary=----WebKitFormBoundary"), "");
        String d5 = r5.get("files").getAsJsonObject().get("file").getAsString().replace("data:application/octet-stream;base64,", "");
        assertTrue(java.util.Arrays.equals(Base64.getDecoder().decode(d5), data), "bytes body");

        // file + form
        JsonObject r6 = session.recv(fileFormSid).json().getAsJsonObject();
        assertJson(r6.getAsJsonObject("form"), new Gson().fromJson(new Gson().toJson(d2), JsonObject.class), "form+file");

        // files + form + params
        JsonObject r7 = session.recv(filesSid).json().getAsJsonObject();

        String d7 = r7.get("files").getAsJsonObject().get("file1").getAsString().replace("data:application/octet-stream;base64,", "");
        assertTrue(java.util.Arrays.equals(Base64.getDecoder().decode(d7), data), "bytes body");

        String d8 = r7.get("files").getAsJsonObject().get("file2").getAsString().replace("data:application/octet-stream;base64,", "");
        assertTrue(java.util.Arrays.equals(Base64.getDecoder().decode(d8), head), "bytes body");

        assertJson(r7.getAsJsonObject("form"), new Gson().fromJson(new Gson().toJson(d2), JsonObject.class), "form");
        assertJson(r7.getAsJsonObject("args"), new Gson().fromJson(new Gson().toJson(d2), JsonObject.class), "args");
    }

    /**
     * 并发：一次性发送多个请求，乱序接收
     */
    static void httpConcurrency(Session session) throws Exception {
        List<Integer> sids = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            sids.add(session.send(Method.GET, "https://httpbin.reqrio.com/get?i=" + i));
        }
        sids.add(session.send(Method.POST, "https://httpbin.reqrio.com/post", new Body("h2-body")));
        sids.add(session.send(Method.GET, "https://httpbin.reqrio.com/gzip"));
        sids.add(session.send(Method.GET, "https://httpbin.reqrio.com/brotli"));
        sids.add(session.send(Method.GET, "https://httpbin.reqrio.com/bytes/4096"));

        java.util.Random random = new java.util.Random();
        while (!sids.isEmpty()) {
            int pos = random.nextInt(sids.size());
            Response res = session.recv(sids.remove(pos));
            assertEq(res.statusCode(), 200, "concurrency status");
        }
    }

}


