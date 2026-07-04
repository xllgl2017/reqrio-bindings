const {library, ref, check_error, ref_char_ptr, read_c_str} = require("./bindings");

//
// const registry = new FinalizationRegistry(resp => {
//     console.log(3434);
//     library.Response_drop(resp);
//     resp = null;
// })


class Response {
    constructor(library, respPtr) {
        this.library = library;
        this.ptr = respPtr;
        // registry.register(this, this.ptr);
    }

    status_code() {
        const errPtr = ref_char_ptr();
        const status_code = this.library.Response_status_code(this.ptr, errPtr);
        check_error(this.library, errPtr.deref())
        return status_code
    }

    /**获取响应头
     * @param {string} name
     **/
    get_header(name) {
        const errPtr = ref_char_ptr();
        const ptr = this.library.Response_get_header(this.ptr, name, errPtr);
        check_error(this.library, errPtr.deref())
        let res = read_c_str(this.library, ptr);
        this.library.char_free(ptr);
        return res;
    }

    cookies() {
        const errPtr = ref_char_ptr();
        const ptr = this.library.Response_cookies(this.ptr, errPtr);
        check_error(this.library, errPtr.deref());
        let res = read_c_str(this.library, ptr);
        this.library.char_free(ptr);
        return JSON.parse(res)
    }

    bytes() {
        const errPtr = ref_char_ptr();
        const lenPtr = Buffer.alloc(8);
        const ptr = library.Response_bytes(this.ptr, lenPtr, errPtr);
        check_error(this.library, errPtr.deref());
        const len = Number(lenPtr.readBigUInt64LE(0));
        return Buffer.from(ref.reinterpret(ptr, len))
    }


    text() {
        return this.bytes().toString('utf8');
    }

    json() {
        return JSON.parse(this.text())
    }

    /**
     * Free the response resource
     */
    close() {
        // registry.unregister(this)
        if (this.ptr == null) return;
        this.library.Response_drop(this.ptr);
        this.ptr = null;
    }

}


module.exports = {
    Response
}