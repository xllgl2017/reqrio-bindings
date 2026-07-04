package org.xllgl2017;

import com.google.gson.Gson;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

import static org.xllgl2017.ReqrioLibrary.REQRIO;

public class Session implements AutoCloseable {
    private Pointer req;

    /// 初始化Session
    ///
    /// @param alpn :设置请求的版本，默认HTTP/2.0
    public Session(ALPN alpn) throws Exception {
        this();
        this.setALPN(alpn);
    }

    public Session() {
        this(false);
    }

    public Session(boolean ignore_hdr_sort) {
        this.req = REQRIO.ScReq_new(ignore_hdr_sort);
    }

    private Pointer pointer() throws Exception {
        if (this.req == null) throw new Exception("Session had close");
        return this.req;
    }

    /// 是否对证书链进行验证
    public void setVerify(boolean verify) throws Exception {
        util.check_err(REQRIO.ScReq_set_verify(this.pointer(), verify));
    }

    /// 对重定向链接是否自动跳转，默认跳转
    public void setAutoRedirect(boolean auto_redirect) throws Exception {
        util.check_err(REQRIO.ScReq_set_redirect(this.pointer(), auto_redirect));
    }

    /// 设置 TLS 导出握手中生成的通讯密钥的路径
    ///
    /// @param path :keylog导出的路径
    public void setKeyLog(String path) throws Exception {
        util.check_err(REQRIO.ScReq_set_key_log(this.pointer(), path));
    }

    /// 设置请求头
    ///
    /// @param header :是一个json字符串，例如: {"User-Agent":"xxx","Host":"xxx"}
    public void setHeaders(String header) throws Exception {
        util.check_err(REQRIO.ScReq_set_header_json(this.pointer(), header));
    }

    /// 设置请求头
    public void setHeaders(HashMap<String, String> headers) throws Exception {
        for (String name : headers.keySet()) {
            this.addHeader(name, headers.get(name));
        }
    }

    /// 添加请求头，若已存在则进行覆盖
    public void addHeader(String name, String value) throws Exception {
        this.addHeader(name, value, true);
    }

    public void addHeader(String name, String value, boolean reversed) throws Exception {
        util.check_err(REQRIO.ScReq_add_header(this.pointer(), name, value, reversed));
    }

    /// @param name :待删除的请求头名
    public void removeHeader(String name) throws Exception {
        util.check_err(REQRIO.ScReq_remove_header(this.pointer(), name));
    }


    /// 设置请求头
    public void setHeaders(Headers headers) throws Exception {
        HashMap<String, String> keys = headers.getKeys();
        for (String name : keys.keySet()) {
            this.addHeader(name, keys.get(name));
        }
        for (Cookie cookie : headers.getCookies()) {
            util.check_err(REQRIO.ScReq_add_cookie(this.pointer(), cookie.getName(), cookie.getValue()));
        }
    }

    public void setFingerprint(Fingerprint fingerprint) throws Exception {
        util.check_err(REQRIO.ScReq_set_fingerprint(this.pointer(), fingerprint.getRaw()));
        fingerprint.drop();
    }

    private void setALPN(ALPN alpn) throws Exception {
        util.check_err(REQRIO.ScReq_set_alpn(this.pointer(), alpn.getValue()));
    }


    /// 设置代理，需要在初始化时设置；后期修改需要调reconnect
    ///
    /// @param proxy :代理地址，例如:http_plain: http://127.0.0.1:10280; socks5: socks://127.0.0.1:10279
    public void setProxy(String proxy) throws Exception {
        util.check_err(REQRIO.ScReq_set_proxy(this.pointer(), proxy));
    }

    public void setTimeout(Timeout timeout) throws Exception {
        Gson gson = new Gson();
        util.check_err(REQRIO.ScReq_set_timeout(this.pointer(), gson.toJson(timeout)));
    }

    public void setCookie(String cookie) throws Exception {
        util.check_err(REQRIO.ScReq_set_cookie(this.pointer(), cookie));
    }

    public void addCookie(String name, String value) throws Exception {
        util.check_err(REQRIO.ScReq_add_cookie(this.pointer(), name, value));
    }

    /// 关闭 tls 流，将发送Alert(CloseNotify)
    public void closeStream() throws Exception {
        util.check_err(REQRIO.ScReq_close_stream(this.pointer()));
    }

    public void reconnect() throws Exception {
        util.check_err(REQRIO.ScReq_reconnect(this.pointer()));
    }

    /// 连接到目标主机，在需要提前建立连接的情况下调用
    ///
    /// @param url :目标主机地址
    /// @param sni :域名
    public void connect(String url, String sni) throws Exception {
        util.check_err(REQRIO.ScReq_connect(this.pointer(), url, sni));
    }

    public void connect(String url) throws Exception {
        this.connect(url, null);
    }

//    public void set_callback(ScReqCallback cb) throws Exception {
//        int ret = INSTANCE.ScReq_set_callback(this.req, cb);
//        if (ret == -1) throw new Exception("set callback error");
//    }


    /// 发送 HTTP 请求
    ///
    /// @param method : 请求方法
    /// @param url    :请求地址
    /// @param body   :请求体
    public Response send(@NotNull Method method, @NotNull Url url, @NotNull Body body) throws Exception {
        try (url; body) {
            PointerByReference err = new PointerByReference();
            Pointer ptr = REQRIO.ScReq_stream_io(this.pointer(), method.getValue(), url.pointer(), body.pointer(), err);
            url.setRaw(null);
            body.setRaw(null);
            util.check_err_pointer(err);
            return new Response(ptr);
        }
    }

    /// 参考 send
    public Response get(Url url, Body body) throws Exception {
        return this.send(Method.GET, url, body);
    }

    /// 参考 send
    public Response get(String url, Body body) throws Exception {
        try (body) {
            return this.get(new Url(url), body);
        }
    }

    /// 参考 send
    public Response get(Url url) throws Exception {
        return this.get(url, new Body());
    }

    /// 参考 send
    public Response get(String url) throws Exception {
        return this.get(url, new Body());
    }

    /// 参考 send
    public Response post(Url url, Body body) throws Exception {
        return this.send(Method.POST, url, body);
    }

    /// 参考 send
    public Response post(String url, Body body) throws Exception {
        try (body) {
            return this.post(new Url(url), body);
        }
    }

    /// 参考 send
    public Response put(Url url, Body body) throws Exception {
        return this.send(Method.PUT, url, body);
    }

    /// 参考 send
    public Response put(String url, Body body) throws Exception {
        try (body) {
            return this.put(new Url(url), body);
        }
    }

    /// 参考 send
    public Response options(Url url, Body body) throws Exception {
        return this.send(Method.OPTIONS, url, body);
    }

    /// 参考 send
    public Response options(String url, Body body) throws Exception {
        try (body) {
            return this.options(new Url(url), body);
        }
    }

    /// 参考 send
    public Response head(Url url, Body body) throws Exception {
        return this.send(Method.HEAD, url, body);
    }

    /// 参考 send
    public Response head(String url, Body body) throws Exception {
        try (body) {
            return this.head(new Url(url), body);
        }
    }

    /// 参考 send
    public Response delete(Url url, Body body) throws Exception {
        return this.send(Method.DELETE, url, body);
    }

    /// 参考 send
    public Response delete(String url, Body body) throws Exception {
        try (body) {
            return this.delete(new Url(url), body);
        }
    }

    /// 参考 send
    public Response trace(Url url, Body body) throws Exception {
        return this.send(Method.TRACE, url, body);
    }

    /// 参考 send
    public Response trace(String url, Body body) throws Exception {
        try (body) {
            return this.trace(new Url(url), body);
        }
    }

    /// 参考 send
    public Response patch(Url url, Body body) throws Exception {
        return this.send(Method.PATCH, url, body);
    }

    /// 参考 send
    public Response patch(String url, Body body) throws Exception {
        try (body) {
            return this.patch(new Url(url), body);
        }
    }

    @Override
    public void close() {
        if (this.req == null) return;
        REQRIO.ScReq_drop(this.req);
        this.req = null;
    }


}
