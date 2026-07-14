const {
    binding_library,
    Method,
    ref,
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
    HTTP20: "h2"
})

// const registry = new FinalizationRegistry(req => {
//     console.log(3434);
//     library.ScReq_drop(req);
//     req = null;
// })


class Session {
    /**初始化Session
     * @param {ALPN} alpn 设置请求的版本，实际使用需和服务器协商。设置了指纹将失效
     * @param {object} headers
     * @param {boolean} verify 是否对服务器的证书链进行验证
     * @param {boolean} auto_redirect 是否对重定向链接进行自动跳转
     * @param {string|null} key_log 导出tls密钥
     * @param {boolean} rand_tls
     * @param {string|null} token 认证token
     * @param {boolean} ignore_hdr_sort 忽略内置请求头顺序
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
        console.log(111)

        if (alpn) check_error(this.library, this.library.ScReq_set_alpn(this.req, alpn));
        check_error(this.library, this.library.ScReq_set_verify(this.req, verify), this.close);
        check_error(this.library, this.library.ScReq_set_redirect(this.req, auto_redirect), this.close);
        if (key_log !== null) this.set_key_log(key_log, this.close)
        if (rand_tls && token !== null) this.use_random_tls(token)
        this.set_headers(headers)

        // registry.register(this, this.req);
    }

    set_key_log(key_log, func = null) {
        check_error(this.library, this.library.ScReq_set_key_log(this.req, key_log), func)
    }

    _set_fingerprint(finger) {
        check_error(this.library, this.library.ScReq_set_fingerprint(this.req, finger))
    }

    /**使用随机指纹
     * @param {Fingerprint} fingerprint
     **/
    set_fingerprint(fingerprint) {
        check_error(this.library, this.library.ScReq_set_fingerprint(this.req, fingerprint.ptr));
        fingerprint.ptr = null;
    }

    /**使用随机指纹
     * @param {string} token 认证token
     **/
    use_random_tls(token) {
        check_error(this.library, this.library.Fingerprint_random(this.req, token))
    }

    /**使用ja3设置指纹
     * @param {string} ja3
     * @param {string} token 认证token
     **/
    set_ja3(ja3, token) {
        const errPtr = ref_char_ptr();
        const finger = this.library.Fingerprint_from_ja3(ja3, token, errPtr);
        check_error(this.library, errPtr.deref());
        this._set_fingerprint(finger)
    }

    /**使用ja4设置指纹
     * @param {string} ja4
     * @param {string} token 认证token
     **/
    set_ja4(ja4, token) {
        const errPtr = ref_char_ptr();
        const finger = this.library.Fingerprint_from_ja4(ja4, token, errPtr);
        check_error(this.library, errPtr.deref());
        this._set_fingerprint(finger)
    }

    /**使用client hello设置指纹
     * @param {Uint8Array} client_hello
     * @param {string} token 认证token
     **/
    set_finger_by_client_hello(client_hello, token) {
        const errPtr = ref_char_ptr();
        const finger = this.library.Fingerprint_from_client_hello(client_hello, client_hello.length, token, errPtr);
        check_error(this.library, errPtr.deref());
        this._set_fingerprint(finger)
    }


    /**使用自定义指纹
     * @param {object} custom
     * @param {string} token 认证token
     **/
    set_finger_by_custom(custom, token) {
        const errPtr = ref_char_ptr();
        const finger = this.library.Fingerprint_custom(JSON.stringify(custom), token, errPtr);
        check_error(this.library, errPtr.deref());
        this._set_fingerprint(finger)
    }


    /**设置请求头
     * @param {object} header
     **/
    set_headers(header) {
        let header_str = JSON.stringify(header);
        check_error(this.library, this.library.ScReq_set_header_json(this.req, header_str));
    }

    /**添加一个请求头
     * @param {string} name
     * @param {string} value
     * @param {boolean} reversed 为true时,值为空，改请求头不发送
     **/
    add_header(name, value, reversed = false) {
        check_error(this.library, this.library.ScReq_add_header(this.req, name, value, reversed));
    }

    /**移除一个请求头
     * @param {string} name
     **/
    remove_header(name) {
        check_error(this.library, library.ScReq_remove_header(this.req, name))
    }

    /**设置代理
     * @param {string} proxy 例如：http://127.0.0.1:8080,socks5://127.0.0.1:8080
     **/
    set_proxy(proxy) {
        check_error(this.library, this.library.ScReq_set_proxy(this.req, proxy))
    }

    /**设置请求超时
     * @param {object} timeout
     **/
    set_timeout(timeout) {
        let timeout_str = JSON.stringify(timeout);
        check_error(this.library, this.library.ScReq_set_timeout(this.req, timeout_str))
    }

    /**设置Cookie
     * @param {string} cookie 例如：name1=a; name2=fdg
     **/
    set_cookie(cookie) {
        check_error(this.library, this.library.ScReq_set_cookie(this.req, cookie))
    }

    /**添加一个Cookie
     * @param {string} name
     * @param {string} value
     **/
    add_cookie(name, value) {
        check_error(this.library, this.library.ScReq_add_cookie(this.req, name, value))
    }

    reconnect() {
        check_error(this.library, this.library.ScReq_reconnect(this.req));
    }

    /**连接到某个url，不会发包
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

    /** 发送一个请求
     * @param {number} method  请求方法
     * @param {string} url_str 请求地址
     * @param {object} options 请求体
     */
    send(method, url_str, options = {}) {
        const {data, json, files, bytes, ct, params, sni} = options;
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

        // Send request
        const errPtr = ref_char_ptr();
        const respPtr = this.library.ScReq_stream_io(this.req, method, url.ptr, body_ptr, errPtr);
        url.ptr = null;
        body_ptr = null;
        check_error(this.library, errPtr.deref())
        return new Response(this.library, respPtr);
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
