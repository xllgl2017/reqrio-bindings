const {
    binding_library,
    Method,
    make_ScReq_callback,
    check_error, ref_char_ptr
} = require("./bindings");
const {Response} = require("./Response")
const {Body} = require('./body')
const {Url} = require('./Url')
const {Fingerprint} = require("./fingerprint")

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
     * @param {boolean} rand_tls use a random TLS fingerprint
     * @param {string|null} token authentication token
     * @param {boolean} ignore_hdr_sort ignore the built-in request header ordering
     **/
    constructor(
        alpn,
        headers = {},
        verify = true,
        auto_redirect = true,
        key_log = null,
        rand_tls = false,
        token = null,
        ignore_hdr_sort = false,
    ) {
        this.library = binding_library();
        this.req = this.library.ScReq_new(ignore_hdr_sort);
        if (alpn) check_error(this.library, this.library.ScReq_set_alpn(this.req, alpn));
        check_error(this.library, this.library.ScReq_set_verify(this.req, verify), this.close);
        check_error(this.library, this.library.ScReq_set_redirect(this.req, auto_redirect), this.close);
        if (key_log !== null) this.set_key_log(key_log, this.close)
        if (rand_tls && token !== null) this.use_random_tls(token)
        this.set_headers(headers)
    }

    set_key_log(key_log, func = null) {
        check_error(this.library, this.library.ScReq_set_key_log(this.req, key_log), func)
    }

    _set_fingerprint(finger) {
        check_error(this.library, this.library.ScReq_set_fingerprint(this.req, finger))
    }

    /**Set a custom TLS fingerprint.
     * @param {Fingerprint} fingerprint
     **/
    set_fingerprint(fingerprint) {
        check_error(this.library, this.library.ScReq_set_fingerprint(this.req, fingerprint.ptr));
        fingerprint.ptr = null;
    }


    /**Use a built-in TLS fingerprint.
     * @param {string} name fingerprint name
     * @param {string} token authentication token
     * @param {boolean} sec_full whether to use the full sec-* header set
     **/
    set_finger_built(name, token, sec_full) {
        const errPtr = ref_char_ptr();
        let fingerprint = this.library.Fingerprint_from_builtin(name, sec_full, token, errPtr)
        check_error(this.library, errPtr.deref())
        check_error(this.library, this.library.ScReq_set_fingerprint(this.req, fingerprint));
    }

    /**Use a preconfigured fingerprint by index.
     * @param {int} idx fingerprint index
     * @param {string} auth authorization token
     * @param {boolean} sec_full whether to use the full sec-* header set
     **/
    set_finger_idx(idx, auth, sec_full) {
        const errPtr = ref_char_ptr();
        let fingerprint = this.library.Fingerprint_from_idx(idx, auth, sec_full, errPtr)
        check_error(this.library, errPtr.deref())
        check_error(this.library, this.library.ScReq_set_fingerprint(this.req, fingerprint));
    }

    /**Use a random TLS fingerprint.
     * @param {string} token authentication token
     **/
    use_random_tls(token) {
        check_error(this.library, this.library.Fingerprint_random(this.req, token))
    }

    /**Set a JA3 fingerprint.
     * @param {string} ja3 JA3 fingerprint string
     * @param {string} token authentication token
     **/
    set_ja3(ja3, token) {
        const errPtr = ref_char_ptr();
        const finger = this.library.Fingerprint_from_ja3(ja3, token, errPtr);
        check_error(this.library, errPtr.deref());
        this._set_fingerprint(finger)
    }

    /**Set a JA4 fingerprint.
     * @param {string} ja4 JA4 fingerprint string
     * @param {string} token authentication token
     **/
    set_ja4(ja4, token) {
        const errPtr = ref_char_ptr();
        const finger = this.library.Fingerprint_from_ja4(ja4, token, errPtr);
        check_error(this.library, errPtr.deref());
        this._set_fingerprint(finger)
    }

    /**Set a fingerprint from a raw ClientHello payload.
     * @param {Uint8Array} client_hello raw ClientHello bytes
     * @param {string} token authentication token
     **/
    set_finger_by_client_hello(client_hello, token) {
        const errPtr = ref_char_ptr();
        const finger = this.library.Fingerprint_from_client_hello(client_hello, client_hello.length, token, errPtr);
        check_error(this.library, errPtr.deref());
        this._set_fingerprint(finger)
    }


    /**Set a custom fingerprint generated from an object payload.
     * @param {object} custom custom fingerprint config
     * @param {string} token authentication token
     **/
    set_finger_by_custom(custom, token) {
        const errPtr = ref_char_ptr();
        const finger = this.library.Fingerprint_custom(JSON.stringify(custom), token, errPtr);
        check_error(this.library, errPtr.deref());
        this._set_fingerprint(finger)
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
        check_error(this.library, library.ScReq_remove_header(this.req, name))
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

    set_callback(func) {
        let callback = make_ScReq_callback(func);
        check_error(this.library, this.library.ScReq_set_callback(this.req, callback))
    }

    /**Send a request.
     * @param {Method} method HTTP method
     * @param {string} url_str request URL
     * @param {object} options request body and transport options
     */
    send(method, url_str, options = {}) {
        const {data, json, files, bytes, ct, params, sni, stream} = options;
        const url = new Url(this.library, url_str, params, sni)


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
            url.close();
            throw e;
        }
        let stream1 = stream === null ? false : stream;

        // Send request
        const errPtr = ref_char_ptr();
        const respPtr = this.library.ScReq_do_http(this.req, method, url.ptr, body_ptr, stream1, errPtr);
        url.ptr = null;
        body_ptr = null;
        check_error(this.library, errPtr.deref())
        return new Response(this.library, respPtr, this.req);
    }

    get(url, options) {
        return this.send(Method.GET, url, options);
    }

    post(url, options) {
        return this.send(Method.POST, url, options);
    }

    put(url, options) {
        return this.send(Method.PUT, url, options);
    }

    head(url, options) {
        return this.send(Method.HEAD, url, options);
    }

    delete(url, options) {
        return this.send(Method.DELETE, url, options);
    }

    options(url, options) {
        return this.send(Method.OPTIONS, url, options);
    }

    trace(url, options) {
        return this.send(Method.TRACE, url, options);
    }

    patch(url, options) {
        return this.send(Method.PATCH, url, options);
    }

    query(url, options) {
        return this.send(Method.QUERY, url, options);
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
