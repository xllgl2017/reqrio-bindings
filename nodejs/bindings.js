const ffi = require('ffi-napi');
const ref = require('ref-napi');
const path = require("path");

const voidPtr = ref.refType(ref.types.void);
const charPtr = ref.refType(ref.types.char);
const charPtrPtr = ref.refType(charPtr);
const uint8Ptr = ref.refType(ref.types.uint8);
const uint16Ptr = ref.refType(ref.types.uint16);
const sizeTPtr = ref.refType(ref.types.size_t);

const Method = {
    GET: 0,
    POST: 1,
    PUT: 2,
    HEAD: 3,
    DELETE: 4,
    OPTIONS: 5,
    TRACE: 6,
    CONNECT: 7,
    PATCH: 8,
    QUERY: 9,
}

const CipherSuite = {
    TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256: 0xc02b,
    TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384: 0xc02c,
    TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256: 0xc023,
    TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384: 0xc024,
    TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA: 0xc009,
    TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA: 0xc00a,
    TLS_ECDHE_ECDSA_WITH_CHACHA20_POLY1305_SHA256: 0xcca9,

    TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256: 0xc02f,
    TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384: 0xc030,
    TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256: 0xc027,
    TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384: 0xc028,
    TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA: 0xc013,
    TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA: 0xc014,
    TLS_ECDHE_RSA_WITH_CHACHA20_POLY1305_SHA256: 0xcca8,

    TLS_DHE_RSA_WITH_AES_128_GCM_SHA256: 0x009e,
    TLS_DHE_RSA_WITH_AES_256_GCM_SHA384: 0x009f,
    TLS_DHE_RSA_WITH_AES_128_CBC_SHA256: 0x0067,
    TLS_DHE_RSA_WITH_AES_256_CBC_SHA256: 0x006b,
    TLS_DHE_RSA_WITH_AES_128_CBC_SHA: 0x0033,
    TLS_DHE_RSA_WITH_AES_256_CBC_SHA: 0x0039,
    TLS_DHE_RSA_WITH_CHACHA20_POLY1305_SHA256: 0xccaa,

    TLS_RSA_WITH_AES_128_GCM_SHA256: 0x009c,
    TLS_RSA_WITH_AES_256_GCM_SHA384: 0x009d,
    TLS_RSA_WITH_AES_128_CBC_SHA256: 0x003c,
    TLS_RSA_WITH_AES_256_CBC_SHA256: 0x003d,
    TLS_RSA_WITH_AES_128_CBC_SHA: 0x002f,
    TLS_RSA_WITH_AES_256_CBC_SHA: 0x0035,

    TLS_AES_128_GCM_SHA256: 0x1301,
    TLS_AES_256_GCM_SHA384: 0x1302,
    TLS_CHACHA20_POLY1305_SHA256: 0x1303,

    TLS_EMPTY_RENEGOTIATION_INFO_SCSV: 0x00ff,
}

const ExtensionType = {
    ServerName: 0x0,
    StatusRequest: 0x5,
    SupportedGroup: 0xa,
    EcPointFormats: 0xb,
    SignatureAlgorithms: 0xd,
    ApplicationLayerProtocolNegotiation: 0x10,
    SignedCertificateTimestamp: 0x12,
    Padding: 0x15,
    EncryptTheMac: 0x16,
    ExtendMasterSecret: 0x17,
    SessionTicket: 0x23,
    CompressionCertificate: 0x1b,
    SupportedVersions: 0x2b,
    PskKeyExchangeMode: 0x2d,
    PostHandshakeAuth: 0x31,
    KeyShare: 0x33,
    RenegotiationInfo: 0xff01,
    EncryptedClientHello: 0xfe0d,
    ApplicationSetting: 0x44cd,
    PreSharedKey: 0x29,
    ApplicationSettingOld: 0x4469,
}

const Group = {
    X25519: 0x1d,
    X448: 0x1e,
    X25519MLKEM768: 0x11ec,
    Secp256r1: 0x0017,
    Secp384r1: 0x0018,
    Secp521r1: 0x0019,
    FFDHE2048: 0x0100,
    FFDHE3072: 0x0101,
    FFDHE4096: 0x0102,
    FFDHE6144: 0x0103,
    FFDHE8192: 0x0104,
}

const EcPointFormat = {
    UNCOMPRESSED: 0,
    ANSI_X962_PRIME: 1,
    ANSI_X962_CHAR2: 2
}

const Algorithm = {
    RSA_PKCS1_SHA1: 0x0201,
    RSA_PKCS1_SHA256: 0x0401,
    RSA_PKCS1_SHA384: 0x0501,
    RSA_PKCS1_SHA512: 0x0601,
    RSA_PSS_RSAE_SHA256: 0x0804,
    RSA_PSS_RSAE_SHA384: 0x0805,
    RSA_PSS_RSAE_SHA512: 0x0806,
    RSA_PSS_PSS_SHA256: 0x0807,
    RSA_PSS_PSS_SHA384: 0x0808,
    RSA_PSS_PSS_SHA512: 0x0809,
    ED25519: 0x080A,
    ED448: 0x080B,
    ECDSA_SHA1: 0x0203,
    ECDSA_SECP256R1_SHA256: 0x0403,
    ECDSA_SECP384R1_SHA384: 0x0503,
    ECDSA_SECP521R1_SHA512: 0x0603,
    SHA1_DSA: 0x0202,
    SHA224_RSA: 0x0301,
    SHA224_DSA: 0x0302,
    SHA224_ECDSA: 0x0303,
    SHA256_DSA: 0x0402,
    SHA384_DSA: 0x0502,
    SHA512_DSA: 0x0602,
}

const Version = {
    TLS_1_0: 0x301,
    TLS_1_1: 0x302,
    TLS_1_2: 0x303,
    TLS_1_3: 0x304
}

const H2Setting = {
    HeaderTableSize: 0x1,
    EnablePush: 0x2,
    MaxConcurrentStreams: 0x3,
    InitialWindowSize: 0x4,
    MaxFrameSize: 0x5,
    MaxHeaderListSize: 0x6,
}

function binding_library() {
    let libname;
    if (process.platform === "win32") {
        libname = "reqrio"
    } else if (process.platform === "linux") {
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
        ScReq_set_timeout: [charPtr, [voidPtr, 'string']],
        ScReq_set_cookie: [charPtr, [voidPtr, 'string']],
        ScReq_add_cookie: [charPtr, [voidPtr, 'string', 'string']],
        ScReq_stream_io: [voidPtr, [voidPtr, 'int', voidPtr, voidPtr, charPtrPtr]],
        ScReq_reconnect: [charPtr, [voidPtr]],
        ScReq_connect: [charPtr, [voidPtr, 'string', "string"]],
        ScReq_close_stream: [charPtr, [voidPtr]],
        ScReq_set_callback: [charPtr, [voidPtr, 'pointer']],
        ScReq_drop: ['void', [voidPtr]],

        // === Fingerprint ===
        // === Fingerprint ===
        Fingerprint_new: [voidPtr, ['string']],
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
        Fingerprint_add_h2_setting: ['void', [voidPtr, 'uint16', 'uint32']],
        Fingerprint_set_h2_window_size: ['void', [voidPtr, 'uint32']],
        Fingerprint_set_h2_priority: ['void', [voidPtr, 'bool', 'uint8']],
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
        Response_drop: ['void', [voidPtr]],

        // === Url ===
        Url_new: [voidPtr, ['string', charPtrPtr]],
        Url_add_param: [charPtr, [voidPtr, 'string', 'string']],
        Url_remove_param: [charPtr, [voidPtr, 'string']],
        Url_set_sni: [charPtr, [voidPtr, 'string']],
        Url_drop: ['void', [voidPtr]],

        // === WebSocket ===
        ws_build: [voidPtr, []],
        ws_add_header: ['int', [voidPtr, charPtr, charPtr]],
        ws_set_proxy: ['int', [voidPtr, charPtr]],
        ws_set_uri: ['int', [voidPtr, charPtr]],
        ws_open: [voidPtr, [voidPtr, voidPtr]],
        ws_open_raw: [voidPtr, [charPtr, charPtr]],
        ws_read: [charPtr, [voidPtr]],
        ws_write: ['int', [voidPtr, 'int', 'bool', charPtr]],
        ws_close: ['void', [voidPtr]],

        // === Utility ===
        char_free: ['void', [charPtr]],
        u8_free: ['void', ['pointer', sizeTPtr]]
    });
}

function read_c_str(library, ptr, free) {
    if (ptr === null) return;
    let c_str = ref.readCString(ptr, 0);
    if (free) library.char_free(ptr);
    return c_str;
}

function ref_char_ptr() {
    return ref.alloc(charPtr)
}

function make_ScReq_callback(func) {
    return ffi.Callback("void", ["pointer", "uint32"], function (ptr, len) {
        const buffer = ref.reinterpret(ptr, len);
        const data = Buffer.from(buffer);
        func(data)
    })
}


function check_error(library, err, func = null) {
    if (ref.isNull(err)) return;
    let msg = read_c_str(library, err, true);
    if (func) func()
    throw new Error(msg)
}


module.exports = {
    binding_library,
    Method,
    ref,
    make_ScReq_callback,
    read_c_str,
    ref_char_ptr,
    check_error,
    CipherSuite,
    ExtensionType,
    Group,
    EcPointFormat,
    Algorithm,
    Version,
    H2Setting
}
