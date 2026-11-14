// /本测试由AI生成，全部测试已通过
// /需要人工版本可查阅其它语言的相同文件
///可查阅main.go
package main

import (
	"testing"
	"time"

	"github.com/xllgl2017/reqrio-bindings/go/reqrio"
)

func newSession(t *testing.T, alpn reqrio.ALPN) *reqrio.Session {
	t.Helper()
	session := reqrio.NewSession(false)
	if err := session.SetAlpn(alpn); err != nil {
		t.Fatalf("set alpn: %v", err)
	}
	return &session
}

// TestGet Testing different types of requests
func TestGet(t *testing.T) {
	t.Parallel()
	for _, alpn := range []reqrio.ALPN{reqrio.HTTP11, reqrio.HTTP20} {
		session := newSession(t, alpn)
		httpGetMatrix(t, session, alpn)
		session.Close()
	}
}

// TestMethod Request method testing
func TestMethod(t *testing.T) {
	t.Parallel()
	for _, alpn := range []reqrio.ALPN{reqrio.HTTP11, reqrio.HTTP20} {
		session := newSession(t, alpn)
		httpMethod(t, session)
		session.Close()
	}
}

// TestStatus Request status testing
func TestStatus(t *testing.T) {
	t.Parallel()
	for _, alpn := range []reqrio.ALPN{reqrio.HTTP11, reqrio.HTTP20} {
		session := newSession(t, alpn)
		httpStatus(t, session)
		session.Close()
	}
}

// TestAuth Authentication Request Test
func TestAuth(t *testing.T) {
	t.Parallel()
	for _, alpn := range []reqrio.ALPN{reqrio.HTTP11, reqrio.HTTP20} {
		session := newSession(t, alpn)
		httpAuth(t, session)
		session.Close()
	}
}

// TestQueryParam Request parameter testing
func TestQueryParam(t *testing.T) {
	t.Parallel()
	for _, alpn := range []reqrio.ALPN{reqrio.HTTP11, reqrio.HTTP20} {
		session := newSession(t, alpn)
		queryParam(t, session)
		session.Close()
	}
}

// TestRedirect Redirection test
func TestRedirect(t *testing.T) {
	t.Parallel()
	for _, alpn := range []reqrio.ALPN{reqrio.HTTP11, reqrio.HTTP20} {
		session := newSession(t, alpn)
		httpRedirect(t, session)
		session.Close()
	}
}

// TestCookie Request Cookie Test
func TestCookie(t *testing.T) {
	t.Parallel()
	for _, alpn := range []reqrio.ALPN{reqrio.HTTP11, reqrio.HTTP20} {
		session := newSession(t, alpn)
		httpCookie(t, session)
		session.Close()
	}
}

// TestHeaders Request Header Test
func TestHeaders(t *testing.T) {
	t.Parallel()
	for _, alpn := range []reqrio.ALPN{reqrio.HTTP11, reqrio.HTTP20} {
		session := newSession(t, alpn)
		httpHeader(t, session)
		session.Close()
	}
}

// TestStream 流式请求
func TestStream(t *testing.T) {
	t.Parallel()
	for _, alpn := range []reqrio.ALPN{reqrio.HTTP11, reqrio.HTTP20} {
		session := newSession(t, alpn)
		httpStream(t, session)
		session.Close()
	}
}

// TestBody Form + JSON + File + Text + Bytes
func TestBody(t *testing.T) {
	t.Parallel()
	for _, alpn := range []reqrio.ALPN{reqrio.HTTP11, reqrio.HTTP20} {
		session := newSession(t, alpn)
		httpBody(t, session)
		session.Close()
	}
}

// TestConcurrency 并发
func TestConcurrency(t *testing.T) {
	t.Parallel()
	for _, alpn := range []reqrio.ALPN{reqrio.HTTP11, reqrio.HTTP20} {
		session := newSession(t, alpn)
		httpConcurrency(t, session)
		session.Close()
	}
}

// TestLargeBody 大 body
func TestLargeBody(t *testing.T) {
	t.Parallel()
	for _, alpn := range []reqrio.ALPN{reqrio.HTTP11, reqrio.HTTP20} {
		session := newSession(t, alpn)
		resp, err := session.SendRequest(reqrio.ConnParam{Method: reqrio.GET, Url: "https://httpbin.reqrio.com/bytes/65536"})
		if err != nil {
			t.Fatalf("send: %v", err)
		}
		if resp.StatusCode() != 200 {
			t.Fatalf("status = %d", resp.StatusCode())
		}
		if n := len(bytesOf(t, &resp)); n != 65536 {
			t.Fatalf("len = %d", n)
		}
		resp.Delete()
		session.Close()
	}
}

// TestTimeout 超时测试
func TestTimeout(t *testing.T) {
	t.Parallel()
	session := reqrio.NewSession(false)
	defer session.Close()
	if err := session.SetAlpn(reqrio.HTTP11); err != nil {
		t.Fatalf("set alpn: %v", err)
	}
	if err := session.SetTimeout(`{"connect":3000,"read":3000,"write":3000,"handle":3000,"connect_times":1,"handle_times":1}`); err != nil {
		t.Fatalf("set timeout: %v", err)
	}

	start := time.Now()
	if _, err := session.SendRequest(reqrio.ConnParam{Method: reqrio.GET, Url: "https://192.0.2.1/"}); err == nil {
		t.Fatal("should timeout")
	}
	if elapsed := time.Since(start).Milliseconds(); elapsed < 3000 || elapsed >= 6000 {
		t.Fatalf("elapsed = %d ms", elapsed)
	}

	start = time.Now()
	sid, err := session.Send(reqrio.ConnParam{Method: reqrio.GET, Url: "https://httpbin.reqrio.com/delay/10"})
	if err != nil {
		t.Fatalf("send: %v", err)
	}
	if _, err := session.Recv(sid, false); err == nil {
		t.Fatal("should timeout")
	}
	if elapsed := time.Since(start).Milliseconds(); elapsed < 3000 {
		t.Fatalf("elapsed = %d ms", elapsed)
	}
}
