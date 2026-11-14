///本示例由AI生成，人工查率不足10%，注意甄别
///需要人工版本可查阅其它语言的相同文件
///可查阅main.go

package main

import (
	"bytes"
	"encoding/base64"
	"encoding/json"
	"math/rand"
	"os"
	"path/filepath"
	"strconv"
	"strings"
	"testing"

	"github.com/xllgl2017/reqrio-bindings/go/reqrio"
)

// send sends one request and returns a closure that receives its response,
// keeping send and recv separated like the Rust version.
func send(t *testing.T, session *reqrio.Session, p reqrio.ConnParam) func() *reqrio.Response {
	t.Helper()
	sid, err := session.Send(p)
	if err != nil {
		t.Fatalf("send %s: %v", p.Url, err)
	}
	return func() *reqrio.Response {
		resp, rerr := session.Recv(sid, false)
		if rerr != nil {
			t.Fatalf("recv %s: %v", p.Url, rerr)
		}
		return &resp
	}
}

// sendAll sends all requests first, then returns a function to receive by index.
func sendAll(t *testing.T, session *reqrio.Session, params ...reqrio.ConnParam) func(int) *reqrio.Response {
	t.Helper()
	recs := make([]func() *reqrio.Response, 0, len(params))
	for _, p := range params {
		recs = append(recs, send(t, session, p))
	}
	return func(i int) *reqrio.Response {
		t.Helper()
		return recs[i]()
	}
}

func jsonOf(t *testing.T, resp *reqrio.Response) map[string]any {
	t.Helper()
	var m map[string]any
	if err := json.Unmarshal([]byte(textOf(t, resp)), &m); err != nil {
		t.Fatalf("parse json: %v", err)
	}
	return m
}

func headerOf(t *testing.T, resp *reqrio.Response, name string) string {
	t.Helper()
	value, err := resp.GetHeader(name)
	if err != nil {
		return ""
	}
	return value
}

// echoedHeader 在 httpbin 回显的 headers 里大小写不敏感取头。
func echoedHeader(t *testing.T, j map[string]any, name string) string {
	t.Helper()
	headers, ok := j["headers"].(map[string]any)
	if !ok {
		t.Fatalf("no headers in %v", j)
	}
	for key, value := range headers {
		if strings.EqualFold(key, name) {
			res, _ := value.(string)
			return res
		}
	}
	return ""
}

func textOf(t *testing.T, resp *reqrio.Response) string {
	t.Helper()
	text, err := resp.Text()
	if err != nil {
		t.Fatalf("read text: %v", err)
	}
	return text
}

func bytesOf(t *testing.T, resp *reqrio.Response) []byte {
	t.Helper()
	body, err := resp.Bytes()
	if err != nil {
		t.Fatalf("read bytes: %v", err)
	}
	return body
}

func jsonEq(t *testing.T, got any, want any) {
	t.Helper()
	g, err := json.Marshal(got)
	if err != nil {
		t.Fatalf("marshal got: %v", err)
	}
	w, err := json.Marshal(want)
	if err != nil {
		t.Fatalf("marshal want: %v", err)
	}
	if !bytes.Equal(g, w) {
		t.Fatalf("json mismatch:\n got=%s\nwant=%s", g, w)
	}
}

func dataUrlBytes(t *testing.T, value any) []byte {
	t.Helper()
	s, ok := value.(string)
	if !ok {
		t.Fatalf("data is not string: %#v", value)
	}
	s = strings.Replace(s, "data:application/octet-stream;base64,", "", 1)
	raw, err := base64.StdEncoding.DecodeString(s)
	if err != nil {
		t.Fatalf("base64: %v", err)
	}
	return raw
}

// httpGetMatrix Testing different types of requests
func httpGetMatrix(t *testing.T, session *reqrio.Session, _ reqrio.ALPN) {
	t.Helper()
	recv := sendAll(t, session,
		reqrio.ConnParam{Method: reqrio.GET, Url: "https://httpbin.reqrio.com"},             // 0 首页
		reqrio.ConnParam{Method: reqrio.GET, Url: "https://httpbin.reqrio.com/get"},         // 1 查询串回显
		reqrio.ConnParam{Method: reqrio.GET, Url: "https://httpbin.reqrio.com/headers"},     // 2 请求头回显
		reqrio.ConnParam{Method: reqrio.GET, Url: "https://httpbin.reqrio.com/user-agent"},  // 3 UA 回显
		reqrio.ConnParam{Method: reqrio.GET, Url: "https://httpbin.reqrio.com/ip"},          // 4 出口 IP
		reqrio.ConnParam{Method: reqrio.GET, Url: "https://httpbin.reqrio.com/uuid"},        // 5 uuid
		reqrio.ConnParam{Method: reqrio.GET, Url: "https://httpbin.reqrio.com/json"},        // 6 JSON 文档
		reqrio.ConnParam{Method: reqrio.GET, Url: "https://httpbin.reqrio.com/html"},        // 7 HTML 文档
		reqrio.ConnParam{Method: reqrio.GET, Url: "https://httpbin.reqrio.com/xml"},         // 8 XML 文档
		reqrio.ConnParam{Method: reqrio.GET, Url: "https://httpbin.reqrio.com/robots.txt"},  // 9 纯文本
		reqrio.ConnParam{Method: reqrio.GET, Url: "https://httpbin.reqrio.com/image/png"},   // 10 二进制图片
		reqrio.ConnParam{Method: reqrio.GET, Url: "https://httpbin.reqrio.com/gzip"},        // 11 gzip
		reqrio.ConnParam{Method: reqrio.GET, Url: "https://httpbin.reqrio.com/deflate"},     // 12 deflate
		reqrio.ConnParam{Method: reqrio.GET, Url: "https://httpbin.reqrio.com/brotli"},      // 13 brotli
		reqrio.ConnParam{Method: reqrio.GET, Url: "https://httpbin.reqrio.com/stream/5"},    // 14 chunked JSON 流
		reqrio.ConnParam{Method: reqrio.GET, Url: "https://httpbin.reqrio.com/bytes/2048"},  // 15 定长原始字节
		reqrio.ConnParam{Method: reqrio.GET, Url: "https://httpbin.reqrio.com/etag/reqrio"}, // 16 ETag
	)

	// 0. 首页：状态行 + Content-Type + body
	home := recv(0)
	if home.StatusCode() != 200 {
		t.Fatalf("status = %d", home.StatusCode())
	}
	if ct := headerOf(t, home, "content-type"); !strings.HasPrefix(ct, "text/html") {
		t.Fatalf("content-type = %s", ct)
	}
	if len(bytesOf(t, home)) == 0 {
		t.Fatal("empty body")
	}
	if !strings.Contains(strings.ToLower(textOf(t, home)), "httpbin") {
		t.Fatal("body does not contain httpbin")
	}

	// /get
	get := jsonOf(t, recv(1))
	if get["url"] != "https://httpbin.reqrio.com/get" {
		t.Fatalf("url = %v", get["url"])
	}
	if get["method"] != "GET" {
		t.Fatalf("method = %v", get["method"])
	}
	if _, ok := get["headers"].(map[string]any); !ok {
		t.Fatalf("headers = %v", get["headers"])
	}

	// /headers
	raw, _ := json.Marshal(jsonOf(t, recv(2)))
	if !strings.Contains(strings.ToLower(string(raw)), "httpbin.reqrio.com") {
		t.Fatalf("headers = %s", raw)
	}

	// /user-agent
	ua, _ := json.Marshal(jsonOf(t, recv(3)))
	if !strings.Contains(strings.ToLower(string(ua)), "reqrio") {
		t.Fatalf("user-agent = %s", ua)
	}

	// /ip
	ip := jsonOf(t, recv(4))
	if origin, _ := ip["origin"].(string); origin == "" {
		t.Fatalf("origin = %v", ip["origin"])
	}

	// /uuid
	uuid := jsonOf(t, recv(5))
	if id, _ := uuid["uuid"].(string); len(id) != 36 {
		t.Fatalf("uuid = %v", uuid["uuid"])
	}

	// /json
	doc := jsonOf(t, recv(6))
	slideshow, ok := doc["slideshow"].(map[string]any)
	if !ok {
		t.Fatalf("slideshow = %v", doc)
	}
	if _, ok := slideshow["slides"].([]any); !ok {
		t.Fatalf("slides = %v", doc)
	}

	// /html
	html := recv(7)
	if ct := headerOf(t, html, "content-type"); !strings.HasPrefix(ct, "text/html") {
		t.Fatalf("content-type = %s", ct)
	}
	if !strings.Contains(strings.ToLower(textOf(t, html)), "<html") {
		t.Fatal("html body error")
	}

	// /xml
	xml := recv(8)
	if ct := headerOf(t, xml, "content-type"); !strings.HasPrefix(ct, "application/xml") {
		t.Fatalf("content-type = %s", ct)
	}
	if !strings.Contains(textOf(t, xml), "<slideshow") {
		t.Fatal("xml body error")
	}

	// /robots.txt
	robots := recv(9)
	if ct := headerOf(t, robots, "content-type"); !strings.HasPrefix(ct, "text/plain") {
		t.Fatalf("content-type = %s", ct)
	}
	if len(bytesOf(t, robots)) == 0 {
		t.Fatal("empty robots.txt")
	}

	// /image/png：二进制响应
	png := recv(10)
	if ct := headerOf(t, png, "content-type"); !strings.HasPrefix(ct, "image/png") {
		t.Fatalf("content-type = %s", ct)
	}
	if !bytes.HasPrefix(bytesOf(t, png), []byte{0x89, 'P', 'N', 'G'}) {
		t.Fatal("not png data")
	}

	// gzip / deflate / brotli
	if gzipped := jsonOf(t, recv(11))["gzipped"]; gzipped != true {
		t.Fatalf("gzip = %v", gzipped)
	}
	if deflated := jsonOf(t, recv(12))["deflated"]; deflated != true {
		t.Fatalf("deflate = %v", deflated)
	}
	if brotli := jsonOf(t, recv(13))["brotli"]; brotli != true {
		t.Fatalf("brotli = %v", brotli)
	}

	// /stream/5：chunked 分块，5 行 JSON
	stream := recv(14)
	if home.StatusCode() != 200 {
		t.Fatalf("status = %d", stream.StatusCode())
	}
	text := textOf(t, stream)
	if lines := len(strings.Split(strings.TrimSpace(text), "\n")); lines != 5 {
		t.Fatalf("lines = %d, body = %s", lines, text)
	}
	if !strings.Contains(text, `"id"`) {
		t.Fatalf("body = %s", text)
	}

	// 15. /bytes/2048：原始字节长度精确匹配
	body := recv(15)
	if body.StatusCode() != 200 {
		t.Fatalf("status = %d", body.StatusCode())
	}
	if n := len(bytesOf(t, body)); n != 2048 {
		t.Fatalf("len = %d", n)
	}

	// 16. /etag/reqrio：响应头可读
	etag := recv(16)
	if etag.StatusCode() != 200 {
		t.Fatalf("status = %d", etag.StatusCode())
	}
	if v := headerOf(t, etag, "etag"); v != "reqrio" {
		t.Fatalf("etag = %s", v)
	}

	// SNI：IP 直连 + 自定义 sni
	sni, err := session.SendRequest(reqrio.ConnParam{
		Method: reqrio.GET,
		Url:    "https://178.236.41.45/get",
		Sni:    "httpbin.reqrio.com",
	})
	if err != nil {
		t.Fatalf("send sni: %v", err)
	}
	defer sni.Delete()
	if sni.StatusCode() != 200 {
		t.Fatalf("sni status = %d", sni.StatusCode())
	}
}

// httpMethod Request method testing
func httpMethod(t *testing.T, session *reqrio.Session) {
	t.Helper()
	postJson := map[string]any{"name": "reqrio", "num": 7, "ok": true}
	recv := sendAll(t, session,
		reqrio.ConnParam{Method: reqrio.GET, Url: "https://httpbin.reqrio.com/get"},
		reqrio.ConnParam{Method: reqrio.POST, Url: "https://httpbin.reqrio.com/post", Json: postJson},
		reqrio.ConnParam{Method: reqrio.PUT, Url: "https://httpbin.reqrio.com/put", Json: map[string]any{"k": "v"}},
		reqrio.ConnParam{Method: reqrio.HEAD, Url: "https://httpbin.reqrio.com/get"},
		reqrio.ConnParam{Method: reqrio.DELETE, Url: "https://httpbin.reqrio.com/delete", Bytes: []byte("bye"), ContentType: "text/plain"},
		reqrio.ConnParam{Method: reqrio.OPTIONS, Url: "https://httpbin.reqrio.com/anything"},
		reqrio.ConnParam{Method: reqrio.TRACE, Url: "https://httpbin.reqrio.com/get"},
		reqrio.ConnParam{Method: reqrio.PATCH, Url: "https://httpbin.reqrio.com/patch", Bytes: []byte("x=1"), ContentType: "text/plain"},
		reqrio.ConnParam{Method: reqrio.QUERY, Url: "https://httpbin.reqrio.com/query", Bytes: []byte("x=1"), ContentType: "text/plain"},
	)

	// GET
	if get := jsonOf(t, recv(0)); get["method"] != "GET" {
		t.Fatalf("method = %v", get["method"])
	}

	// POST（data 为 json 序列化后的字符串）
	post := jsonOf(t, recv(1))
	if post["method"] != "POST" {
		t.Fatalf("method = %v", post["method"])
	}
	var postData map[string]any
	if err := json.Unmarshal([]byte(post["data"].(string)), &postData); err != nil {
		t.Fatalf("parse post data: %v", err)
	}
	jsonEq(t, postData, postJson)

	// PUT
	put := jsonOf(t, recv(2))
	if put["method"] != "PUT" {
		t.Fatalf("method = %v", put["method"])
	}
	var putData map[string]any
	if err := json.Unmarshal([]byte(put["data"].(string)), &putData); err != nil {
		t.Fatalf("parse put data: %v", err)
	}
	jsonEq(t, putData, map[string]any{"k": "v"})

	// HEAD
	head := recv(3)
	if head.StatusCode() != 200 {
		t.Fatalf("status = %d", head.StatusCode())
	}
	if headerOf(t, head, "content-length") == "" {
		t.Fatal("no content-length")
	}
	if n := len(bytesOf(t, head)); n != 0 {
		t.Fatalf("head body len = %d", n)
	}

	// DELETE
	del := jsonOf(t, recv(4))
	if del["method"] != "DELETE" {
		t.Fatalf("method = %v", del["method"])
	}
	if del["data"] != "bye" {
		t.Fatalf("data = %v", del["data"])
	}

	// OPTIONS
	options := recv(5)
	if options.StatusCode() != 200 {
		t.Fatalf("status = %d", options.StatusCode())
	}
	if allow := headerOf(t, options, "access-control-allow-methods"); !strings.Contains(allow, "GET, POST, PUT, DELETE, PATCH, OPTIONS") {
		t.Fatalf("allow = %s", allow)
	}

	// TRACE-405 / CONNECT-405 / PATCH-405 / QUERY-不通：todo()
}

// httpStatus Request status testing
func httpStatus(t *testing.T, session *reqrio.Session) {
	t.Helper()
	codes := []int{200, 201, 202, 204, 206, 301, 302, 304, 400, 401, 403, 404, 418, 500, 502, 503}
	params := make([]reqrio.ConnParam, 0, len(codes))
	for _, code := range codes {
		params = append(params, reqrio.ConnParam{
			Method: reqrio.GET,
			Url:    "https://httpbin.reqrio.com/status/" + strconv.Itoa(code),
		})
	}
	recv := sendAll(t, session, params...)
	for i, code := range codes {
		if got := recv(i).StatusCode(); int(got) != code {
			t.Fatalf("status = %d, want %d", got, code)
		}
	}
}

// httpAuth Authentication Request Test
func httpAuth(t *testing.T, session *reqrio.Session) {
	t.Helper()
	// 先发送未认证请求
	unAuth := send(t, session, reqrio.ConnParam{Method: reqrio.GET, Url: "https://httpbin.reqrio.com/basic-auth/user/passwd"})
	if err := session.AddHeader("Authorization", "Basic dXNlcjpwYXNzd2Q=", false); err != nil {
		t.Fatalf("add header: %v", err)
	}
	auth := send(t, session, reqrio.ConnParam{Method: reqrio.GET, Url: "https://httpbin.reqrio.com/basic-auth/user/passwd"})
	bearer := send(t, session, reqrio.ConnParam{Method: reqrio.GET, Url: "https://httpbin.reqrio.com/bearer"})

	// 未认证
	if resp := unAuth(); resp.StatusCode() != 401 {
		t.Fatalf("status = %d", resp.StatusCode())
	}
	// 已认证
	resp := auth()
	if resp.StatusCode() != 200 {
		t.Fatalf("status = %d", resp.StatusCode())
	}
	if authenticated, ok := jsonOf(t, resp)["authenticated"].(bool); !ok || !authenticated {
		t.Fatal("not authenticated")
	}
	// 未认证
	if resp := bearer(); resp.StatusCode() != 401 {
		t.Fatalf("status = %d", resp.StatusCode())
	}
}

// queryParam Request parameter testing
func queryParam(t *testing.T, session *reqrio.Session) {
	t.Helper()
	p1 := map[string]string{"q": "'reqrio'", "v": "\"reqrio\"", "n": "42", "b": "true"}
	p2 := map[string]string{"added": "2"}
	p3 := map[string]string{"cn": "中文", "sp": "a b&c=d", "empty": ""}
	p4 := map[string]string{"f": "1.5", "arr": "x,y"}

	// P1
	resp, err := session.SendRequest(reqrio.ConnParam{
		Method: reqrio.GET,
		Url:    "https://httpbin.reqrio.com/get",
		Params: p1,
	})
	if err != nil {
		t.Fatalf("send: %v", err)
	}
	args := jsonOf(t, &resp)["args"].(map[string]any)
	if args["q"] != "'reqrio'" || args["v"] != "\"reqrio\"" || args["n"] != "42" || args["b"] != "true" {
		t.Fatalf("args = %v", args)
	}
	resp.Delete()

	// P2
	resp, err = session.SendRequest(reqrio.ConnParam{
		Method: reqrio.GET,
		Url:    "https://httpbin.reqrio.com/get?existing=1",
		Params: p2,
	})
	if err != nil {
		t.Fatalf("send: %v", err)
	}
	args = jsonOf(t, &resp)["args"].(map[string]any)
	if args["existing"] != "1" || args["added"] != "2" {
		t.Fatalf("args = %v", args)
	}
	resp.Delete()

	// P3
	resp, err = session.SendRequest(reqrio.ConnParam{
		Method: reqrio.GET,
		Url:    "https://httpbin.reqrio.com/get",
		Params: p3,
	})
	if err != nil {
		t.Fatalf("send: %v", err)
	}
	args = jsonOf(t, &resp)["args"].(map[string]any)
	if args["cn"] != "中文" || args["sp"] != "a b&c=d" || args["empty"] != "" {
		t.Fatalf("args = %v", args)
	}
	resp.Delete()

	// P4
	resp, err = session.SendRequest(reqrio.ConnParam{
		Method: reqrio.GET,
		Url:    "https://httpbin.reqrio.com/get",
		Params: p4,
	})
	if err != nil {
		t.Fatalf("send: %v", err)
	}
	args = jsonOf(t, &resp)["args"].(map[string]any)
	if args["f"] != "1.5" || args["arr"] != "x,y" {
		t.Fatalf("args = %v", args)
	}
	resp.Delete()
}

// httpRedirect Redirection test
// - Non automatic redirection can be done using send
// - Automatic redirection cannot use send and recv
func httpRedirect(t *testing.T, session *reqrio.Session) {
	t.Helper()
	if err := session.SetRedirect(false); err != nil {
		t.Fatalf("set redirect: %v", err)
	}
	recv := sendAll(t, session,
		reqrio.ConnParam{Method: reqrio.GET, Url: "https://httpbin.reqrio.com/redirect/2"},
		reqrio.ConnParam{Method: reqrio.GET, Url: "https://httpbin.reqrio.com/relative-redirect/1"},
		reqrio.ConnParam{Method: reqrio.GET, Url: "https://httpbin.reqrio.com/redirect-to?url=/get&status_code=307"},
		reqrio.ConnParam{Method: reqrio.GET, Url: "https://httpbin.reqrio.com/absolute-redirect/2"},
	)

	r1 := recv(0)
	if r1.StatusCode() != 302 || headerOf(t, r1, "location") != "/relative-redirect/1" {
		t.Fatalf("r1 = %d %s", r1.StatusCode(), headerOf(t, r1, "location"))
	}
	r2 := recv(1)
	if r2.StatusCode() != 302 || headerOf(t, r2, "location") != "/get" {
		t.Fatalf("r2 = %d %s", r2.StatusCode(), headerOf(t, r2, "location"))
	}
	r3 := recv(2)
	if r3.StatusCode() != 307 || headerOf(t, r3, "location") != "/get" {
		t.Fatalf("r3 = %d %s", r3.StatusCode(), headerOf(t, r3, "location"))
	}
	r4 := recv(3)
	if r4.StatusCode() != 302 || !strings.HasPrefix(headerOf(t, r4, "location"), "https://") {
		t.Fatalf("r4 = %d %s", r4.StatusCode(), headerOf(t, r4, "location"))
	}

	// 自动重定向
	if err := session.SetRedirect(true); err != nil {
		t.Fatalf("set redirect: %v", err)
	}
	for _, url := range []string{
		"https://httpbin.reqrio.com/redirect/2",
		"https://httpbin.reqrio.com/relative-redirect/1",
		"https://httpbin.reqrio.com/redirect-to?url=/get&status_code=307",
	} {
		resp, err := session.SendRequest(reqrio.ConnParam{Method: reqrio.GET, Url: url})
		if err != nil {
			t.Fatalf("send %s: %v", url, err)
		}
		if resp.StatusCode() != 200 {
			t.Fatalf("status = %d", resp.StatusCode())
		}
		resp.Delete()
	}
	// set_max_redirect_exceeds: todo()
}

// httpCookie Request Cookie Test
func httpCookie(t *testing.T, session *reqrio.Session) {
	t.Helper()
	if err := session.SetRedirect(false); err != nil {
		t.Fatalf("set redirect: %v", err)
	}
	resp, err := session.SendRequest(reqrio.ConnParam{Method: reqrio.GET, Url: "https://httpbin.reqrio.com/cookies/set?k=v&k2=v2"})
	if err != nil {
		t.Fatalf("send: %v", err)
	}
	if resp.StatusCode() != 302 {
		t.Fatalf("status = %d", resp.StatusCode())
	}
	if location := headerOf(t, &resp, "location"); location != "/cookies" {
		t.Fatalf("location = %s", location)
	}
	cookies, err := resp.Cookies()
	if err != nil {
		t.Fatalf("cookies: %v", err)
	}
	found := 0
	for _, cookie := range cookies {
		if (cookie.Name == "k" && cookie.Value == "v") || (cookie.Name == "k2" && cookie.Value == "v2") {
			found++
		}
	}
	if found != 2 {
		t.Fatalf("cookies = %+v", cookies)
	}
	resp.Delete()

	// Go 绑定未暴露 remove_cookie，这里依赖服务端 Set-Cookie 完成删除
	resp, err = session.SendRequest(reqrio.ConnParam{Method: reqrio.GET, Url: "https://httpbin.reqrio.com/cookies/delete?k"})
	if err != nil {
		t.Fatalf("send: %v", err)
	}
	if resp.StatusCode() != 302 {
		t.Fatalf("status = %d", resp.StatusCode())
	}
	resp.Delete()

	if err := session.SetRedirect(true); err != nil {
		t.Fatalf("set redirect: %v", err)
	}
	resp, err = session.SendRequest(reqrio.ConnParam{Method: reqrio.GET, Url: "https://httpbin.reqrio.com/cookies"})
	if err != nil {
		t.Fatalf("send: %v", err)
	}
	jar, _ := jsonOf(t, &resp)["cookies"].(map[string]any)
	if jar["k2"] != "v2" {
		t.Fatalf("cookies = %v", jar)
	}
	if k, ok := jar["k"]; ok && k != "" {
		t.Fatalf("cookies = %v", jar)
	}
	resp.Delete()
}

// httpHeader Request Header Test
func httpHeader(t *testing.T, session *reqrio.Session) {
	t.Helper()
	userAgent := "reqrio-test/1.0,reqrio-test"
	err := session.SetHeaders(map[string]string{
		"User-Agent":      userAgent,
		"X-Reqrio-Test":   "hello",
		"Accept-Language": "zh-CN,zh;q=0.9",
		"X-Multi":         "a",
		"X-Int":           "1",
		"X-Bool":          "true",
		"X-Object":        `{"k":"v"}`,
		"X-Array":         `["1",1,true]`,
	})
	if err != nil {
		t.Fatalf("set headers: %v", err)
	}

	r0 := send(t, session, reqrio.ConnParam{Method: reqrio.GET, Url: "https://httpbin.reqrio.com/headers"})
	r1 := send(t, session, reqrio.ConnParam{Method: reqrio.GET, Url: "https://httpbin.reqrio.com/user-agent"})
	r2 := send(t, session, reqrio.ConnParam{Method: reqrio.POST, Url: "https://httpbin.reqrio.com/post", Json: map[string]any{"with": "header"}})

	// Append + Overwrite
	if err := session.AddHeader("X-Late-Header", "late", false); err != nil {
		t.Fatalf("add header: %v", err)
	}
	if err := session.AddHeader("X-Reqrio-Test", "overwritten", false); err != nil {
		t.Fatalf("add header: %v", err)
	}
	r3 := send(t, session, reqrio.ConnParam{Method: reqrio.GET, Url: "https://httpbin.reqrio.com/headers"})
	r4 := send(t, session, reqrio.ConnParam{Method: reqrio.GET, Url: "https://httpbin.reqrio.com/anything?phase=late"})

	h1 := jsonOf(t, r0())
	if v := echoedHeader(t, h1, "user-agent"); v != userAgent {
		t.Fatalf("user-agent = %s", v)
	}
	if v := echoedHeader(t, h1, "x-reqrio-test"); v != "hello" {
		t.Fatalf("x-reqrio-test = %s", v)
	}
	if v := echoedHeader(t, h1, "accept-language"); v != "zh-CN,zh;q=0.9" {
		t.Fatalf("accept-language = %s", v)
	}
	if v := echoedHeader(t, h1, "x-multi"); v != "a" {
		t.Fatalf("x-multi = %s", v)
	}
	if v := echoedHeader(t, h1, "x-int"); v != "1" {
		t.Fatalf("x-int = %s", v)
	}
	if v := echoedHeader(t, h1, "x-bool"); v != "true" {
		t.Fatalf("x-bool = %s", v)
	}
	if v := echoedHeader(t, h1, "x-object"); v != `{"k":"v"}` {
		t.Fatalf("x-object = %s", v)
	}
	if v := echoedHeader(t, h1, "x-array"); v != `["1",1,true]` {
		t.Fatalf("x-array = %s", v)
	}

	h2 := jsonOf(t, r1())
	if v, _ := h2["user-agent"].(string); v != userAgent {
		t.Fatalf("user-agent = %s", v)
	}

	h3 := jsonOf(t, r2())
	var postData map[string]any
	if err := json.Unmarshal([]byte(h3["data"].(string)), &postData); err != nil {
		t.Fatalf("parse post data: %v", err)
	}
	jsonEq(t, postData, map[string]any{"with": "header"})

	for _, resp := range []*reqrio.Response{r3(), r4()} {
		header := jsonOf(t, resp)
		if v := echoedHeader(t, header, "user-agent"); v != userAgent {
			t.Fatalf("user-agent = %s", v)
		}
		if v := echoedHeader(t, header, "x-reqrio-test"); v != "overwritten" {
			t.Fatalf("x-reqrio-test = %s", v)
		}
		if v := echoedHeader(t, header, "accept-language"); v != "zh-CN,zh;q=0.9" {
			t.Fatalf("accept-language = %s", v)
		}
		if v := echoedHeader(t, header, "x-multi"); v != "a" {
			t.Fatalf("x-multi = %s", v)
		}
		if v := echoedHeader(t, header, "x-int"); v != "1" {
			t.Fatalf("x-int = %s", v)
		}
		if v := echoedHeader(t, header, "x-bool"); v != "true" {
			t.Fatalf("x-bool = %s", v)
		}
		if v := echoedHeader(t, header, "x-object"); v != `{"k":"v"}` {
			t.Fatalf("x-object = %s", v)
		}
		if v := echoedHeader(t, header, "x-array"); v != `["1",1,true]` {
			t.Fatalf("x-array = %s", v)
		}
		if v := echoedHeader(t, header, "x-late-header"); v != "late" {
			t.Fatalf("x-late-header = %s", v)
		}
	}
}

// httpStream 流式读取
func httpStream(t *testing.T, session *reqrio.Session) {
	t.Helper()
	resp1, err := session.SendRequest(reqrio.ConnParam{Method: reqrio.GET, Url: "https://httpbin.reqrio.com/bytes/16384", Stream: true})
	if err != nil {
		t.Fatalf("send: %v", err)
	}
	total, chunks := 0, 0
	for chunk := range resp1.Chunks() {
		if len(chunk) == 0 {
			t.Fatal("empty chunk")
		}
		total += len(chunk)
		chunks++
	}
	if err := resp1.Err(); err != nil {
		t.Fatalf("chunk err: %v", err)
	}
	if total != 16384 {
		t.Fatalf("total = %d", total)
	}
	if chunks < 1 {
		t.Fatalf("chunks = %d", chunks)
	}

	resp2, err := session.SendRequest(reqrio.ConnParam{Method: reqrio.GET, Url: "https://httpbin.reqrio.com/stream/3", Stream: true})
	if err != nil {
		t.Fatalf("send: %v", err)
	}
	for chunk := range resp2.Chunks() {
		if !bytes.Contains(chunk, []byte("\n")) {
			t.Fatalf("chunk = %q", chunk)
		}
	}
	if err := resp2.Err(); err != nil {
		t.Fatalf("chunk err: %v", err)
	}
}

// httpBody Form + JSON + File + Text + Bytes
func httpBody(t *testing.T, session *reqrio.Session) {
	t.Helper()
	d1 := map[string]any{
		"str1":   "s",
		"str2":   "'s'",
		"str3":   "\"s\"",
		"int":    0,
		"bool":   true,
		"object": map[string]any{"data": "value"},
		"array":  []any{"2", 0, true, map[string]any{"k": "v"}},
	}
	d2 := map[string]string{"k1": "v1", "k2": "v2"}
	text := "sdfhsdjkfsdljhkgs;gfkgsdfhsdasugdsadgsauydgtasdfytsad"
	data := make([]byte, 4096)
	for i := range data {
		data[i] = byte(i)
	}

	dir := t.TempDir()
	path1 := filepath.Join(dir, "f1.bin")
	path2 := filepath.Join(dir, "f2.bin")
	if err := os.WriteFile(path1, data, 0o644); err != nil {
		t.Fatalf("write file: %v", err)
	}
	if err := os.WriteFile(path2, data[:1024], 0o644); err != nil {
		t.Fatalf("write file: %v", err)
	}
	const ftype = "application/octet-stream"

	recv := sendAll(t, session,
		// form
		reqrio.ConnParam{Method: reqrio.POST, Url: "https://httpbin.reqrio.com/post", Data: d2},
		// json
		reqrio.ConnParam{Method: reqrio.POST, Url: "https://httpbin.reqrio.com/post", Json: d1},
		// bytes
		reqrio.ConnParam{Method: reqrio.POST, Url: "https://httpbin.reqrio.com/post", Bytes: data, ContentType: "application/octet-stream"},
		// text
		reqrio.ConnParam{Method: reqrio.POST, Url: "https://httpbin.reqrio.com/post", Bytes: []byte(text), ContentType: "text/plain"},
		// file
		reqrio.ConnParam{Method: reqrio.POST, Url: "https://httpbin.reqrio.com/post", Files: []reqrio.HttpFile{{Path: path1, FieldName: "file", Filetype: ftype}}},
		// file + form
		reqrio.ConnParam{Method: reqrio.POST, Url: "https://httpbin.reqrio.com/post", Files: []reqrio.HttpFile{{Path: path1, FieldName: "file", Filetype: ftype}}, Data: d2},
		// files + form + params
		reqrio.ConnParam{Method: reqrio.POST, Url: "https://httpbin.reqrio.com/post", Params: d2, Data: d2, Files: []reqrio.HttpFile{
			{Path: path1, FieldName: "file1", Filetype: ftype},
			{Path: path2, FieldName: "file2", Filetype: ftype},
		}},
	)

	// form
	r1 := jsonOf(t, recv(0))
	jsonEq(t, r1["form"], d2)
	if ct := echoedHeader(t, r1, "content-type"); ct != "application/x-www-form-urlencoded" {
		t.Fatalf("content-type = %s", ct)
	}

	// json
	r2 := jsonOf(t, recv(1))
	jsonEq(t, r2["json"], d1)
	if ct := echoedHeader(t, r2, "content-type"); ct != "application/json" {
		t.Fatalf("content-type = %s", ct)
	}

	// bytes
	r3 := jsonOf(t, recv(2))
	if ct := echoedHeader(t, r3, "content-type"); ct != "application/octet-stream" {
		t.Fatalf("content-type = %s", ct)
	}
	if !bytes.Equal(dataUrlBytes(t, r3["data"]), data) {
		t.Fatal("bytes mismatch")
	}

	// text
	r4 := jsonOf(t, recv(3))
	if ct := echoedHeader(t, r4, "content-type"); ct != "text/plain" {
		t.Fatalf("content-type = %s", ct)
	}
	if r4["data"] != text {
		t.Fatalf("data = %v", r4["data"])
	}

	// file
	r5 := jsonOf(t, recv(4))
	if ct := echoedHeader(t, r5, "content-type"); !strings.HasPrefix(ct, "multipart/form-data; boundary=----WebKitFormBoundary") {
		t.Fatalf("content-type = %s", ct)
	}
	files := r5["files"].(map[string]any)
	if !bytes.Equal(dataUrlBytes(t, files["file"]), data) {
		t.Fatal("file mismatch")
	}

	// file + form
	r6 := jsonOf(t, recv(5))
	jsonEq(t, r6["form"], d2)

	// files + form + params
	r7 := jsonOf(t, recv(6))
	files = r7["files"].(map[string]any)
	if !bytes.Equal(dataUrlBytes(t, files["file1"]), data) {
		t.Fatal("file1 mismatch")
	}
	if !bytes.Equal(dataUrlBytes(t, files["file2"]), data[:1024]) {
		t.Fatal("file2 mismatch")
	}
	jsonEq(t, r7["form"], d2)
	jsonEq(t, r7["args"], d2)
}

// httpConcurrency 并发：一次性发送多个请求，乱序接收
func httpConcurrency(t *testing.T, session *reqrio.Session) {
	t.Helper()
	params := make([]reqrio.ConnParam, 0, 10)
	for i := 0; i < 6; i++ {
		params = append(params, reqrio.ConnParam{Method: reqrio.GET, Url: "https://httpbin.reqrio.com/get?i=" + strconv.Itoa(i)})
	}
	params = append(params,
		reqrio.ConnParam{Method: reqrio.POST, Url: "https://httpbin.reqrio.com/post", Bytes: []byte("h2-body"), ContentType: "text/plain"},
		reqrio.ConnParam{Method: reqrio.GET, Url: "https://httpbin.reqrio.com/gzip"},
		reqrio.ConnParam{Method: reqrio.GET, Url: "https://httpbin.reqrio.com/brotli"},
		reqrio.ConnParam{Method: reqrio.GET, Url: "https://httpbin.reqrio.com/bytes/4096"},
	)
	recv := sendAll(t, session, params...)

	pending := make([]int, len(params))
	for i := range pending {
		pending[i] = i
	}
	for len(pending) > 0 {
		pos := rand.Intn(len(pending))
		resp := recv(pending[pos])
		if resp.StatusCode() != 200 {
			t.Fatalf("status = %d", resp.StatusCode())
		}
		pending = append(pending[:pos], pending[pos+1:]...)
	}
}
