const {
    binding_library,
    check_error,
    ref_char_ptr
} = require("./bindings");
const {Response} = require("./Response")
const {Body} = require('./body')
const {Url} = require('./Url')
const {Fingerprint} = require("./fingerprint")
const {Method} = require("./http/method")

const ALPN = Object.freeze({
    HTTP10: "http/1.0",
    HTTP11: "http/1.1",
    HTTP20: "h2",
    HTTP30: "h3"
})


class Session {
    /**Initialize a session.
     * @param {ALPN} alpn negotiated protocol version for the request; this is overridden when a custom fingerprint is set.
     * @param {object} headers
     * @param {boolean} verify whether to validate the server certificate chain
     * @param {boolean} auto_redirect whether to automatically follow redirect links
     * @param {string|null} key_log path to export TLS keys
     * @param {boolean} ignore_hdr_sort ignore the built-in request header ordering
     **/
    constructor(
        alpn,
        headers = {},
        verify = true,
        auto_redirect = true,
        key_log = null,
        ignore_hdr_sort = false,
    ) {
        this.library = binding_library();
        this.req = this.library.ScReq_new(ignore_hdr_sort);
        if (alpn) check_error(this.library, this.library.ScReq_set_alpn(this.req, alpn));
        check_error(this.library, this.library.ScReq_set_verify(this.req, verify), this.close);
        check_error(this.library, this.library.ScReq_set_redirect(this.req, auto_redirect), this.close);
        if (key_log !== null) this.set_key_log(key_log, this.close)
        this.set_headers(headers)
    }

    set_key_log(key_log, func = null) {
        check_error(this.library, this.library.ScReq_set_key_log(this.req, key_log), func)
    }

    /**Set a custom TLS fingerprint.
     * @param {Fingerprint} fingerprint
     **/
    set_fingerprint(fingerprint) {
        check_error(this.library, this.library.ScReq_set_fingerprint(this.req, fingerprint.ptr));
        fingerprint.ptr = null;
    }

    /**Set all request headers in a single call.
     * @param {object} header
     **/
    set_headers(header) {
        let header_str = JSON.stringify(header);
        check_error(this.library, this.library.ScReq_set_header_json(this.req, header_str));
    }

    /**Add a request header.
     * @param {string} name
     * @param {string} value
     * @param {boolean} reversed when true, the value is left empty and the header is not sent
     **/
    add_header(name, value, reversed = false) {
        check_error(this.library, this.library.ScReq_add_header(this.req, name, value, reversed));
    }

    /**Remove a request header.
     * @param {string} name
     **/
    remove_header(name) {
        check_error(this.library, this.library.ScReq_remove_header(this.req, name))
    }

    /**Set an HTTP or SOCKS proxy.
     * @param {string} proxy example: http://127.0.0.1:8080 or socks5://127.0.0.1:8080
     **/
    set_proxy(proxy) {
        check_error(this.library, this.library.ScReq_set_proxy(this.req, proxy))
    }

    /**Set timeout values for connection, read, write, and overall handling.
     * @param {object} timeout
     **/
    set_timeout(timeout) {
        let timeout_str = JSON.stringify(timeout);
        check_error(this.library, this.library.ScReq_set_timeout(this.req, timeout_str))
    }

    /**
     * Set the TLS handshake version.
     * @param {Version} ver
     * */
    set_version(ver) {
        check_error(this.library, this.library.ScReq_set_version(this.req, ver))
    }

    /**Set a Cookie header.
     * @param {string} cookie example: name1=a; name2=fdg
     **/
    set_cookie(cookie) {
        check_error(this.library, this.library.ScReq_set_cookie(this.req, cookie))
    }

    /**Add a single cookie.
     * @param {string} name
     * @param {string} value
     **/
    add_cookie(name, value) {
        check_error(this.library, this.library.ScReq_add_cookie(this.req, name, value))
    }

    reconnect() {
        check_error(this.library, this.library.ScReq_reconnect(this.req));
    }

    /**Connect to a URL without sending an HTTP request.
     * @param {string} url
     * @param {string|null} sni
     **/
    connect(url, sni = null) {
        check_error(this.library, this.library.ScReq_connect(this.req, url, sni))
    }


    close_stream() {
        check_error(this.library, this.library.ScReq_close_stream(this.req))
    }

    /**Send a request.
     * @param {Method} method HTTP method
     * @param {string|Url} url request URL
     * @param {object} options request body and transport options
     */
    send(method, url, options = {}) {
        const {data, json, files, bytes, ct, params, sni} = options;
        if (typeof url === "string") {
            url = new Url(this.library, url, params, sni)
        }


        // Create Body
        let body_ptr = null;
        try {
            if (bytes !== undefined && bytes !== null) {
                body_ptr = Body.new(this.library, bytes, ct || "application/octet-stream");
            } else if (json !== undefined && json !== null) {
                body_ptr = Body.new_json(this.library, json, ct || "application/json")
            } else if (data !== undefined && data !== null) {
                body_ptr = Body.new_form(this.library, data, ct || "application/x-www-form-urlencoded");
            } else if (files !== undefined && files !== null) {
                body_ptr = Body.new_files(files, data)
            } else {
                body_ptr = Body.none(this.library);
            }
        } catch (e) {
            url.drop();
            throw e;
        }

        // Send request
        const errPtr = ref_char_ptr();
        const sid = this.library.ScReq_send(this.req, method, url.take(), body_ptr, errPtr);
        check_error(this.library, errPtr.deref())
        return sid;
    }

    recv(sid, stream) {
        stream = stream === null || stream === undefined ? false : stream
        console.log(stream)
        const errPtr = ref_char_ptr();
        let respPtr = this.library.ScReq_recv(this.req, sid, stream, errPtr);
        return new Response(this.library, respPtr, this.req);
    }

    get(url, options = {}) {
        return this.recv(this.send(Method.GET, url, options), options.stream)
    }

    post(url, options = {}) {
        return this.recv(this.send(Method.POST, url, options), options.stream)
    }

    put(url, options = {}) {
        return this.recv(this.send(Method.PUT, url, options), options.stream)
    }

    head(url, options = {}) {
        return this.recv(this.send(Method.HEAD, url, options), options.stream)
    }

    delete(url, options = {}) {
        return this.recv(this.send(Method.DELETE, url, options), options.stream)
    }

    options(url, options = {}) {
        return this.recv(this.send(Method.OPTIONS, url, options), options.stream)
    }

    trace(url, options = {}) {
        return this.recv(this.send(Method.TRACE, url, options), options.stream)
    }

    patch(url, options = {}) {
        return this.recv(this.send(Method.PATCH, url, options), options.stream)
    }

    query(url, options = {}) {
        return this.recv(this.send(Method.QUERY, url, options), options.stream)
    }

    close() {
        // registry.unregister(this);
        if (this.req == null) return
        this.library.ScReq_drop(this.req);
        this.req = null;
    }
}

module.exports = {
    Session, ALPN, Method
}
