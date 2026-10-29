const {library, ref, check_error, ref_char_ptr, read_c_str} = require("./bindings");


class Response {
    /**
     * Create a response wrapper.
     * @param {object} library Native library handle.
     * @param {object} respPtr Native response pointer.
     * @param {object} reqPtr Native request pointer.
     * @param {boolean} stream stream request
     */
    constructor(library, respPtr, reqPtr, stream ) {
        this.library = library;
        this.ptr = respPtr;
        this.req = reqPtr;
        this.stream = stream;
    }

    /**
     * Get the HTTP status code.
     * @returns {number} Status code.
     */
    status_code() {
        const errPtr = ref_char_ptr();
        const status_code = this.library.Response_status_code(this.ptr, errPtr);
        check_error(this.library, errPtr.deref())
        return status_code
    }

    /**
     * Get a response header by name.
     * @param {string} name Header name.
     * @returns {string|undefined} Header value.
     */
    get_header(name) {
        const errPtr = ref_char_ptr();
        const ptr = this.library.Response_get_header(this.ptr, name, errPtr);
        check_error(this.library, errPtr.deref())
        let res = read_c_str(this.library, ptr);
        this.library.char_free(ptr);
        return res;
    }

    /**
     * Get all cookies from the response.
     * @returns {object} Cookie map.
     */
    cookies() {
        const errPtr = ref_char_ptr();
        const ptr = this.library.Response_cookies(this.ptr, errPtr);
        check_error(this.library, errPtr.deref());
        let res = read_c_str(this.library, ptr);
        this.library.char_free(ptr);
        return JSON.parse(res)
    }

    /**
     * Read the raw response body as bytes.
     * @returns {Buffer} Response bytes.
     */
    bytes() {
        const errPtr = ref_char_ptr();
        const lenPtr = Buffer.alloc(8);
        const ptr = this.library.Response_bytes(this.ptr, lenPtr, errPtr);
        check_error(this.library, errPtr.deref());
        const len = Number(lenPtr.readBigUInt64LE(0));
        return Buffer.from(ref.reinterpret(ptr, len))
    }

    /**
     * Read the response body as UTF-8 text.
     * @returns {string} Response text.
     */
    text() {
        return this.bytes().toString('utf8');
    }

    /**
     * Parse the response body as JSON.
     * @returns {any} Parsed JSON value.
     */
    json() {
        return JSON.parse(this.text())
    }

    /**
     * Free the native response resource.
     */
    close() {
        // registry.unregister(this)
        if (this.ptr == null || this.stream) return;
        this.library.Response_drop(this.ptr);
        this.ptr = null;
    }

    /**
     * Iterate through streamed response chunks.
     * @returns {IterableIterator<Buffer>} Stream chunk iterator.
     */
    chunks() {
        class StreamChunk {
            constructor(sid, reqPtr, library) {
                this.sid = sid;
                this.reqPtr = reqPtr;
                this.library = library;
            }

            * [Symbol.iterator]() {
                while (true) {
                    const errPtr = ref_char_ptr();
                    const lenPtr = Buffer.alloc(8);
                    const ptr = this.library.ScReq_recv_stream(this.reqPtr, this.sid, lenPtr, errPtr);
                    check_error(this.library, errPtr.deref());
                    if (ref.isNull(ptr)) {
                        break
                    } else {
                        const len = Number(lenPtr.readBigUInt64LE(0));
                        yield Buffer.from(ref.reinterpret(ptr, len))
                    }
                }
            }
        }

        const errPtr = ref_char_ptr();
        const sid = this.library.Response_sid(this.ptr, errPtr);
        check_error(this.library, errPtr.deref());
        return new StreamChunk(sid, this.req, this.library)
    }

}


module.exports = {
    Response
}