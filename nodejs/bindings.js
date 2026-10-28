const ffi = require('ffi-napi');
const ref = require('ref-napi');
const path = require("path");

const voidPtr = ref.refType(ref.types.void);
const charPtr = ref.refType(ref.types.char);
const charPtrPtr = ref.refType(charPtr);
const uint8Ptr = ref.refType(ref.types.uint8);
const uint16Ptr = ref.refType(ref.types.uint16);
const sizeTPtr = ref.refType(ref.types.size_t);

/**
 * Load the native reqrio library.
 * @returns {object} Loaded ffi library instance.
 */
function binding_library() {
    let libname;
    if (process.platform === "win32") {
        libname = "reqrio"
    } else if (process.platform === "linux") {
        libname = "libreqrio"
    } else if (process.platform === "darwin") {
        libname = "libreqrio"
    } else {
        throw "unsupported system platform"
    }
    let libpath = path.join(__dirname, libname)
    return ffi.Library(libpath, {
        // === ScReq ===
        ScReq_new: [voidPtr, ['bool']],
        ScReq_set_header_json: ['pointer', [voidPtr, 'string']],
        ScReq_add_header: [charPtr, [voidPtr, 'string', 'string', 'bool']],
        ScReq_remove_header: [charPtr, [voidPtr, 'string']],
        ScReq_set_alpn: [charPtr, [voidPtr, 'string']],
        ScReq_set_verify: [charPtr, [voidPtr, 'bool']],
        ScReq_set_redirect: [charPtr, [voidPtr, 'bool']],
        ScReq_set_key_log: [charPtr, [voidPtr, 'string']],
        ScReq_set_fingerprint: [charPtr, [voidPtr, voidPtr]],
        ScReq_set_proxy: [charPtr, [voidPtr, 'string']],
        ScReq_set_version: [charPtr, [voidPtr, 'uint16']],
        ScReq_set_timeout: [charPtr, [voidPtr, 'string']],
        ScReq_set_cookie: [charPtr, [voidPtr, 'string']],
        ScReq_add_cookie: [charPtr, [voidPtr, 'string', 'string']],
        ScReq_send: ['uint64', [voidPtr, 'int', voidPtr, voidPtr, charPtrPtr]],
        ScReq_recv: [voidPtr, [voidPtr, 'uint64', 'bool', charPtrPtr]],
        ScReq_recv_stream: [voidPtr, [voidPtr, 'uint64', voidPtr, charPtrPtr]],
        ScReq_reconnect: [charPtr, [voidPtr]],
        ScReq_connect: [charPtr, [voidPtr, 'string', "string"]],
        ScReq_close_stream: [charPtr, [voidPtr]],
        ScReq_drop: ['void', [voidPtr]],

        // === Fingerprint ===
        Fingerprint_new: [voidPtr, ['uint16', 'uint16', 'string']],
        Fingerprint_from_builtin: [voidPtr, ['string', 'bool', 'string', charPtrPtr]],
        Fingerprint_from_idx: [voidPtr, ['size_t', 'string', 'bool', charPtrPtr]],
        Fingerprint_add_cipher_suite: ['void', [voidPtr, 'uint16']],
        Fingerprint_add_ext: ['void', [voidPtr, 'uint16']],
        Fingerprint_add_ext_alpn: ['void', [voidPtr, 'uint16', 'string']],
        Fingerprint_add_ext_version: ['void', [voidPtr, 'uint16', 'uint16']],
        Fingerprint_add_ext_curve: ['void', [voidPtr, 'uint16', 'uint16']],
        Fingerprint_add_ext_compress: ['void', [voidPtr, 'uint16', 'uint16']],
        Fingerprint_add_ext_psk_mode: ['void', [voidPtr, 'uint16', 'uint8']],
        Fingerprint_add_ext_padding: ['void', [voidPtr, 'uint16', 'size_t']],
        Fingerprint_add_ext_bytes: ['void', [voidPtr, 'uint16', 'pointer', 'size_t']],
        Fingerprint_add_ext_algorithm: ['void', [voidPtr, 'uint16', 'uint16']],
        Fingerprint_add_ext_ec_point: ['void', [voidPtr, 'uint16', 'uint8']],
        Fingerprint_clear_h2_frame: ['void', [voidPtr]],
        Fingerprint_add_h2_setting: ['void', [voidPtr, 'uint16', 'uint32']],
        Fingerprint_set_h2_window_size: ['void', [voidPtr, 'uint32']],
        Fingerprint_set_h2_priority: ['void', [voidPtr, 'bool', 'uint8']],
        Fingerprint_add_h2_frame: ['void', [voidPtr, 'uint8', uint8Ptr, 'size_t']],
        Fingerprint_clean_h3_frame: ['void', [voidPtr]],
        Fingerprint_add_h3_setting: ['void', ['uint64', 'uint64']],
        Fingerprint_add_h3_frame: ['void', ['uint64', uint8Ptr, 'size_t']],
        Fingerprint_drop: ['void', [voidPtr]],
        Fingerprint_from_ja3: [voidPtr, ['string', 'string', charPtrPtr]],
        Fingerprint_from_ja4: [voidPtr, ['string', 'string', charPtrPtr]],
        Fingerprint_from_client_hello: [voidPtr, [uint8Ptr, 'size_t', 'string', charPtrPtr]],
        Fingerprint_random: [voidPtr, ['string', charPtrPtr]],
        Fingerprint_custom: [voidPtr, ['string', 'string', charPtrPtr]],

        // === Body ===
        Body_new: [voidPtr, [uint8Ptr, 'size_t', 'string', charPtrPtr]],
        Body_none: [voidPtr, []],
        Body_new_files: [voidPtr, [voidPtr, 'string', charPtrPtr]],
        Body_drop: ['void', [voidPtr]],

        // === HttpFile ===
        HttpFile_new: [voidPtr, []],
        HttpFile_add_form: [charPtr, [voidPtr, voidPtr]],
        HttpFile_drop: ['void', [voidPtr]],

        // === FileForm ===
        FileForm_new: [voidPtr, ['string', 'string', 'string', charPtrPtr]],

        // === Response ===
        Response_status_code: ['uint16', [voidPtr, charPtrPtr]],
        Response_bytes: [uint8Ptr, [voidPtr, sizeTPtr, charPtrPtr]],
        Response_get_header: [charPtr, [voidPtr, 'string', charPtrPtr]],
        Response_cookies: [charPtr, [voidPtr, charPtrPtr]],
        Response_sid: ['uint64', [voidPtr, charPtrPtr]],
        Response_drop: ['void', [voidPtr]],

        // === Url ===
        Url_new: [voidPtr, ['string', charPtrPtr]],
        Url_add_param: [charPtr, [voidPtr, 'string', 'string']],
        Url_remove_param: [charPtr, [voidPtr, 'string']],
        Url_set_sni: [charPtr, [voidPtr, 'string']],
        Url_drop: ['void', [voidPtr]],

        // === WebSocket ===
        ws_open: [voidPtr, [charPtr, charPtr]],
        ws_open_raw: [voidPtr, [charPtr, charPtr]],
        ws_read: [charPtr, [voidPtr]],
        ws_write: ['int', [voidPtr, 'int', 'bool', charPtr]],
        ws_close: ['void', [voidPtr]],

        // === Utility ===
        char_free: ['void', [charPtr]],
        u8_free: ['void', ['pointer', sizeTPtr]]
    });
}

/**
 * Read a native C string and free this ptr while ptr is not null and free is true.
 * @param {object} library Native library handle.
 * @param {Buffer} ptr Pointer to the C string.
 * @param {boolean} [free=false] Whether to free the native buffer after reading.
 * @returns {string|undefined} Decoded string.
 */
function read_c_str(library, ptr, free) {
    if (ptr === null) return;
    let c_str = ref.readCString(ptr, 0);
    if (free) library.char_free(ptr);
    return c_str;
}

/**
 * Allocate a pointer slot for native error data.
 * @returns {Buffer} Pointer reference buffer.
 */
function ref_char_ptr() {
    return ref.alloc(charPtr)
}

/**
 * Throw a JS error when a native call returns an error pointer.
 * @param {object} library Native library handle.
 * @param {Buffer} err Error pointer.
 * @param {Function|null} [func=null] Cleanup callback when an error is raised.
 */
function check_error(library, err, func = null) {
    if (ref.isNull(err)) return;
    let msg = read_c_str(library, err, true);
    if (func) func()
    throw new Error(msg)
}


module.exports = {
    binding_library,
    ref,
    read_c_str,
    ref_char_ptr,
    check_error,
}
