package org.xllgl2017;

import com.google.gson.Gson;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;
import org.jetbrains.annotations.NotNull;
import org.xllgl2017.tls.ALPN;
import org.xllgl2017.tls.Version;

import java.util.HashMap;

import static org.xllgl2017.ReqrioLibrary.REQRIO;

/**
 * HTTP session for sending and receiving requests.
 */
public class Session implements AutoCloseable {
    private Pointer req;

    /**
     * Create a session with a negotiated ALPN value.
     * @param alpn ALPN protocol.
     */
    public Session(ALPN alpn) throws Exception {
        this();
        this.setALPN(alpn);
    }

    /**
     * Create a default session.
     */
    public Session() {
        this(false);
    }

    /**
     * Create a session.
     * @param ignore_hdr_sort Whether to skip the built-in header ordering.
     */
    public Session(boolean ignore_hdr_sort) {
        this.req = REQRIO.ScReq_new(ignore_hdr_sort);
    }

    private Pointer pointer() throws Exception {
        if (this.req == null) throw new Exception("Session had close");
        return this.req;
    }

    /**
     * Enable or disable certificate validation.
     * @param verify true to verify the server certificate chain.
     */
    public void setVerify(boolean verify) throws Exception {
        util.check_err(REQRIO.ScReq_set_verify(this.pointer(), verify));
    }

    /**
     * Enable or disable automatic redirect following.
     * @param auto_redirect true to follow redirects automatically.
     */
    public void setAutoRedirect(boolean auto_redirect) throws Exception {
        util.check_err(REQRIO.ScReq_set_redirect(this.pointer(), auto_redirect));
    }

    /**
     * Set the TLS key log path.
     * @param path Output path for TLS key logs.
     */
    public void setKeyLog(String path) throws Exception {
        util.check_err(REQRIO.ScReq_set_key_log(this.pointer(), path));
    }

    /**
     * Replace all request headers from a JSON string.
     * @param header JSON header string.
     */
    public void setHeaders(String header) throws Exception {
        util.check_err(REQRIO.ScReq_set_header_json(this.pointer(), header));
    }

    /**
     * Set request headers from a map.
     * @param headers Header key-value pairs.
     */
    public void setHeaders(HashMap<String, String> headers) throws Exception {
        for (String name : headers.keySet()) {
            this.setHeader(name, headers.get(name));
        }
    }

    /**
     * Set a single request header.
     * @param name Header name.
     * @param value Header value.
     */
    public void setHeader(String name, String value) throws Exception {
        this.setHeader(name, value, false);
    }

    /**
     * Set a single request header with optional reverse mode.
     * @param name Header name.
     * @param value Header value.
     * @param reversed If true, the header is left empty and not sent.
     */
    public void setHeader(String name, String value, boolean reversed) throws Exception {
        if (name.equalsIgnoreCase("cookie")) {
            this.setCookie(value);
        } else {
            util.check_err(REQRIO.ScReq_add_header(this.pointer(), name, value, reversed));
        }
    }

    /**
     * Remove a request header.
     * @param name Header name to remove.
     */
    public void removeHeader(String name) throws Exception {
        util.check_err(REQRIO.ScReq_remove_header(this.pointer(), name));
    }

    /**
     * Set request headers from a Headers object.
     * @param headers Header container.
     */
    public void setHeaders(Headers headers) throws Exception {
        HashMap<String, String> keys = headers.getKeys();
        for (String name : keys.keySet()) {
            this.setHeader(name, keys.get(name));
        }
        for (Cookie cookie : headers.getCookies()) {
            util.check_err(REQRIO.ScReq_add_cookie(this.pointer(), cookie.getName(), cookie.getValue()));
        }
    }

    /**
     * Set a custom TLS fingerprint.
     * @param fingerprint Fingerprint instance.
     */
    public void setFingerprint(Fingerprint fingerprint) throws Exception {
        util.check_err(REQRIO.ScReq_set_fingerprint(this.pointer(), fingerprint.getRaw()));
        fingerprint.drop();
    }

    private void setALPN(ALPN alpn) throws Exception {
        util.check_err(REQRIO.ScReq_set_alpn(this.pointer(), alpn.getValue()));
    }

    /**
     * Set the TLS handshake version.
     * @param ver TLS version.
     */
    public void setVersion(Version ver) throws Exception {
        util.check_err(REQRIO.ScReq_set_version(this.pointer(), ver.value));
    }

    /**
     * Set a proxy address.
     * @param proxy Proxy URL, for example http://127.0.0.1:10280 or socks5://127.0.0.1:10279.
     */
    public void setProxy(String proxy) throws Exception {
        util.check_err(REQRIO.ScReq_set_proxy(this.pointer(), proxy));
    }

    /**
     * Configure request timeout parameters.
     * @param timeout Timeout object.
     */
    public void setTimeout(Timeout timeout) throws Exception {
        Gson gson = new Gson();
        util.check_err(REQRIO.ScReq_set_timeout(this.pointer(), gson.toJson(timeout)));
    }

    /**
     * Set a Cookie header value.
     * @param cookie Cookie string.
     */
    public void setCookie(String cookie) throws Exception {
        util.check_err(REQRIO.ScReq_set_cookie(this.pointer(), cookie));
    }

    /**
     * Add a cookie.
     * @param name Cookie name.
     * @param value Cookie value.
     */
    public void addCookie(String name, String value) throws Exception {
        util.check_err(REQRIO.ScReq_add_cookie(this.pointer(), name, value));
    }

    /**
     * Close the current TLS stream.
     */
    public void closeStream() throws Exception {
        util.check_err(REQRIO.ScReq_close_stream(this.pointer()));
    }

    /**
     * Reconnect the session.
     */
    public void reconnect() throws Exception {
        util.check_err(REQRIO.ScReq_reconnect(this.pointer()));
    }

    /**
     * Connect to a URL without sending an HTTP request.
     * @param url Target URL.
     * @param sni Custom SNI value.
     */
    public void connect(String url, String sni) throws Exception {
        util.check_err(REQRIO.ScReq_connect(this.pointer(), url, sni));
    }

    /**
     * Connect to a URL without sending an HTTP request.
     * @param url Target URL.
     */
    public void connect(String url) throws Exception {
        this.connect(url, null);
    }

    /**
     * Send an HTTP request and return the request id.
     * @param method HTTP method.
     * @param url Request URL.
     * @param body Request body.
     * @return Request session id.
     */
    public int send(@NotNull Method method, @NotNull Url url, @NotNull Body body) throws Exception {
        try (url; body) {
            PointerByReference err = new PointerByReference();
            int sid = REQRIO.ScReq_send(this.pointer(), method.getValue(), url.pointer(), body.pointer(), err);
            url.setRaw(null);
            body.setRaw(null);
            util.check_err_pointer(err);
            return sid;
        }
    }

    /**
     * Send an HTTP request with an empty body.
     * @param method HTTP method.
     * @param url Request URL.
     * @return Request session id.
     */
    public int send(@NotNull Method method, @NotNull Url url) throws Exception {
        Body body = new Body();
        try (body) {
            return this.send(method, url, body);
        }
    }

    /**
     * Send a request and stream the response.
     * @param method HTTP method.
     * @param url Request URL.
     * @param body Request body.
     * @return Response object.
     */
    public Response send_stream(@NotNull Method method, @NotNull Url url, @NotNull Body body) throws Exception {
        int sid = this.send(method, url, body);
        return this.recv(sid, true);
    }

    /**
     * Send a request and stream the response with an empty body.
     * @param method HTTP method.
     * @param url Request URL.
     * @return Response object.
     */
    public Response send_stream(@NotNull Method method, @NotNull Url url) throws Exception {
        Body body = new Body();
        try (body) {
            return this.send_stream(method, url, body);
        }
    }

    /**
     * Receive a response for a request id.
     * @param sid Request session id.
     * @param stream Whether to stream the response.
     * @return Response object.
     */
    public Response recv(int sid, boolean stream) throws Exception {
        PointerByReference err = new PointerByReference();
        Pointer pointer = REQRIO.ScReq_recv(this.pointer(), sid, stream, err);
        util.check_err_pointer(err);
        return new Response(pointer, this.pointer(), stream);
    }

    /**
     * Receive a non-streaming response for a request id.
     * @param sid Request session id.
     * @return Response object.
     */
    public Response recv(int sid) throws Exception {
        return this.recv(sid, false);
    }

    /**
     * Send a GET request.
     * @param url Request URL.
     * @param body Request body.
     * @return Response object.
     */
    public Response get(Url url, Body body) throws Exception {
        return this.recv(this.send(Method.GET, url, body), false);
    }

    /**
     * Send a GET request.
     * @param url Request URL string.
     * @param body Request body.
     * @return Response object.
     */
    public Response get(String url, Body body) throws Exception {
        try (body) {
            return this.get(new Url(url), body);
        }
    }

    /**
     * Send a GET request with an empty body.
     * @param url Request URL.
     * @return Response object.
     */
    public Response get(Url url) throws Exception {
        return this.get(url, new Body());
    }

    /**
     * Send a GET request with an empty body.
     * @param url Request URL string.
     * @return Response object.
     */
    public Response get(String url) throws Exception {
        return this.get(url, new Body());
    }

    /**
     * Send a POST request.
     * @param url Request URL.
     * @param body Request body.
     * @return Response object.
     */
    public Response post(Url url, Body body) throws Exception {
        return this.recv(this.send(Method.POST, url, body), false);
    }

    /**
     * Send a POST request.
     * @param url Request URL string.
     * @param body Request body.
     * @return Response object.
     */
    public Response post(String url, Body body) throws Exception {
        try (body) {
            return this.post(new Url(url), body);
        }
    }

    /**
     * Send a PUT request.
     * @param url Request URL.
     * @param body Request body.
     * @return Response object.
     */
    public Response put(Url url, Body body) throws Exception {
        return this.recv(this.send(Method.PUT, url, body), false);
    }

    /**
     * Send a PUT request.
     * @param url Request URL string.
     * @param body Request body.
     * @return Response object.
     */
    public Response put(String url, Body body) throws Exception {
        try (body) {
            return this.put(new Url(url), body);
        }
    }

    /**
     * Send an OPTIONS request.
     * @param url Request URL.
     * @param body Request body.
     * @return Response object.
     */
    public Response options(Url url, Body body) throws Exception {
        return this.recv(this.send(Method.OPTIONS, url, body), false);
    }

    /**
     * Send an OPTIONS request.
     * @param url Request URL string.
     * @param body Request body.
     * @return Response object.
     */
    public Response options(String url, Body body) throws Exception {
        try (body) {
            return this.options(new Url(url), body);
        }
    }

    /**
     * Send a HEAD request.
     * @param url Request URL.
     * @param body Request body.
     * @return Response object.
     */
    public Response head(Url url, Body body) throws Exception {
        return this.recv(this.send(Method.HEAD, url, body), false);
    }

    /**
     * Send a HEAD request.
     * @param url Request URL string.
     * @param body Request body.
     * @return Response object.
     */
    public Response head(String url, Body body) throws Exception {
        try (body) {
            return this.head(new Url(url), body);
        }
    }

    /**
     * Send a DELETE request.
     * @param url Request URL.
     * @param body Request body.
     * @return Response object.
     */
    public Response delete(Url url, Body body) throws Exception {
        return this.recv(this.send(Method.DELETE, url, body), false);
    }

    /**
     * Send a DELETE request.
     * @param url Request URL string.
     * @param body Request body.
     * @return Response object.
     */
    public Response delete(String url, Body body) throws Exception {
        try (body) {
            return this.delete(new Url(url), body);
        }
    }

    /**
     * Send a TRACE request.
     * @param url Request URL.
     * @param body Request body.
     * @return Response object.
     */
    public Response trace(Url url, Body body) throws Exception {
        return this.recv(this.send(Method.TRACE, url, body), false);
    }

    /**
     * Send a TRACE request.
     * @param url Request URL string.
     * @param body Request body.
     * @return Response object.
     */
    public Response trace(String url, Body body) throws Exception {
        try (body) {
            return this.trace(new Url(url), body);
        }
    }

    /**
     * Send a PATCH request.
     * @param url Request URL.
     * @param body Request body.
     * @return Response object.
     */
    public Response patch(Url url, Body body) throws Exception {
        return this.recv(this.send(Method.PATCH, url, body), false);
    }

    /**
     * Send a PATCH request.
     * @param url Request URL string.
     * @param body Request body.
     * @return Response object.
     */
    public Response patch(String url, Body body) throws Exception {
        try (body) {
            return this.patch(new Url(url), body);
        }
    }

    /**
     * Send a custom query request.
     * @param url Request URL.
     * @param body Request body.
     * @return Response object.
     */
    public Response query(Url url, Body body) throws Exception {
        return this.recv(this.send(Method.QUERY, url, body), false);
    }

    /**
     * Send a custom query request.
     * @param url Request URL string.
     * @param body Request body.
     * @return Response object.
     */
    public Response query(String url, Body body) throws Exception {
        try (body) {
            return this.query(new Url(url), body);
        }
    }

    @Override
    public void close() {
        if (this.req == null) return;
        REQRIO.ScReq_drop(this.req);
        this.req = null;
    }


}
