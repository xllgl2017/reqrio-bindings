const ref = require("ref-napi");
const {ref_char_ptr, check_error} = require('./bindings')
const {Library} = require('ffi-napi');
const {H2SettingType, H2FrameType} = require("./http/h2")
const {ExtensionType} = require("./tls/extension")
const {CipherSuite} = require("./tls/suite");
const {NamedCurve} = require("./tls/group");
const {Version} = require("./tls/version")
const {SignatureAlgorithm} = require("./tls/algorithm")
const {EcPointFormat} = require("./tls/format")
const {CompressionMethod} = require("./tls/compress")
const {H3FrameType, H3SettingType} = require("./http/h3")

class Fingerprint {

    /**
     * Create a fingerprint wrapper.
     * @param {object} ptr Native fingerprint pointer.
     * @param {object} library Native library handle.
     */
    constructor(ptr, library) {
        this.library = library;
        this.ptr = ptr;
    }

    /**
     * Build a custom fingerprint.
     * @param {Version} rv Record version.
     * @param {Version} mv Message version.
     * @param {string} token Auth token.
     * @param {object} library Native library handle.
     * @returns {Fingerprint} Fingerprint instance.
     */
    static fromCustom(rv, mv, token, library) {
        let ptr = library.Fingerprint_new(rv, mv, token);
        return new Fingerprint(ptr, library)
    }

    /**
     * Create a fingerprint from a raw ClientHello payload.
     * @param {Uint8Array} client_hello Raw ClientHello bytes.
     * @param {string} token Auth token.
     * @param {object} library Native library handle.
     * @returns {Fingerprint} Fingerprint instance.
     */
    static fromClientHello(client_hello, token, library) {
        const errPtr = ref_char_ptr();
        const finger = library.Fingerprint_from_client_hello(client_hello, client_hello.length, token, errPtr);
        check_error(library, errPtr.deref());
        return new Fingerprint(finger, library)

    }

    /**
     * Create a JA3 fingerprint.
     * @param {string} ja3 JA3 value.
     * @param {string} token Auth token.
     * @param {object} library Native library handle.
     * @returns {Fingerprint} Fingerprint instance.
     */
    static fromJa3(ja3, token, library) {
        const errPtr = ref_char_ptr();
        const finger = library.Fingerprint_from_ja3(ja3, token, errPtr);
        check_error(library, errPtr.deref());
        return new Fingerprint(finger, library)
    }

    /**
     * Create a JA4 fingerprint.
     * @param {string} ja4 JA4 value.
     * @param {string} token Auth token.
     * @param {object} library Native library handle.
     * @returns {Fingerprint} Fingerprint instance.
     */
    static fromJa4(ja4, token, library) {
        const errPtr = ref_char_ptr();
        const finger = library.Fingerprint_from_ja4(ja4, token, errPtr);
        check_error(library, errPtr.deref());
        return new Fingerprint(finger, library)
    }

    /**
     * Create a random fingerprint.
     * @param {string} token Auth token.
     * @param {object} library Native library handle.
     * @returns {Fingerprint} Fingerprint instance.
     */
    static fromRandom(token, library) {
        const errPtr = ref_char_ptr();
        let ptr = library.Fingerprint_random(token, errPtr);
        check_error(library, errPtr.deref())
        return new Fingerprint(ptr, library)
    }

    /**
     * Create a built-in fingerprint.
     * @param {string} name Fingerprint name.
     * @param {string} token Auth token.
     * @param {boolean} sec_full Whether to use the full sec-* header set.
     * @param {object} library Native library handle.
     * @returns {Fingerprint} Fingerprint instance.
     */
    static fromFingerBuiltin(name, token, sec_full, library) {
        const errPtr = ref_char_ptr();
        let ptr = library.Fingerprint_from_builtin(name, sec_full, token, errPtr)
        check_error(library, errPtr.deref())
        return new Fingerprint(ptr, library)
    }

    /**
     * Create a fingerprint by index.
     * @param {number} idx Fingerprint index.
     * @param {string} auth Auth token.
     * @param {boolean} sec_full Whether to use the full sec-* header set.
     * @param {object} library Native library handle.
     * @returns {Fingerprint} Fingerprint instance.
     */
    static fromFingerIdx(idx, auth, sec_full, library) {
        const errPtr = ref_char_ptr();
        let ptr = library.Fingerprint_from_idx(idx, auth, sec_full, errPtr)
        check_error(library, errPtr.deref())
        return new Fingerprint(ptr, library)

    }

    /**
     * Add a cipher suite to the fingerprint.
     * @param {CipherSuite|number} suite Cipher suite ID.
     */
    addCipherSuite(suite) {
        this.library.Fingerprint_add_cipher_suite(this.ptr, suite);
    }

    /**
     * Add a TLS extension.
     * @param {ExtensionType|number} ext_type Extension type.
     */
    addExtension(ext_type) {
        this.library.Fingerprint_add_ext(this.ptr, ext_type);
    }

    /**
     * Add ALPN values to the extension.
     * @param {ExtensionType|number} ext_type Extension type.
     * @param {Array<string>} alps ALPN list.
     */
    addExtensionAlps(ext_type, alps) {
        for (const alpn of alps) {
            this.library.Fingerprint_add_ext_alpn(this.ptr, ext_type, alpn)
        }
    }

    /**
     * Add supported TLS versions to the extension.
     * @param {ExtensionType|number} ext_type Extension type.
     * @param {Array<Version|number>} versions Version list.
     */
    addExtensionVersions(ext_type, versions) {
        for (const version of versions) {
            this.library.Fingerprint_add_ext_version(this.ptr, ext_type, version);
        }
    }

    /**
     * Add named curves to the extension.
     * @param {ExtensionType|number} ext_type Extension type.
     * @param {Array<number|NamedCurve>} curves Curve list.
     */
    addExtensionCurves(ext_type, curves) {
        for (const curve of curves) {
            this.library.Fingerprint_add_ext_curve(this.ptr, ext_type, curve);
        }
    }

    /**
     * Add compression methods to the extension.
     * @param {number} ext_type Extension type.
     * @param {Array<number|CompressionMethod>} methods Method list.
     */
    addExtensionCompress(ext_type, methods) {
        for (const method of methods) {
            this.library.Fingerprint_add_ext_compress(this.ptr, ext_type, method);
        }
    }

    /**
     * Set PSK mode for an extension.
     * @param {number} ext_type Extension type.
     * @param {number} mode PSK mode value.
     */
    addExtensionPskMode(ext_type, mode) {
        this.library.Fingerprint_add_ext_psk_mode(this.ptr, ext_type, mode);
    }

    /**
     * Add padding to an extension.
     * @param {number} ext_type Extension type.
     * @param {number} padding Padding length.
     */
    addExtensionPadding(ext_type, padding) {
        this.library.Fingerprint_add_ext_padding(this.ptr, ext_type, padding);
    }

    /**
     * Add raw extension bytes.
     * @param {number} ext_type Extension type.
     * @param {Array<number>} bytes Raw byte array.
     */
    addExtensionBytes(ext_type, bytes) {
        const bytes_u8 = Uint8Array.from(bytes)
        this.library.Fingerprint_add_ext_bytes(this.ptr, ext_type, bytes_u8, bytes_u8.length);
    }

    /**
     * Add signature algorithms to the extension.
     * @param {number} ext_type Extension type.
     * @param {Array<SignatureAlgorithm|number>} algorithms Algorithm list.
     */
    addExtensionAlgorithms(ext_type, algorithms) {
        for (const algorithm of algorithms) {
            this.library.Fingerprint_add_ext_algorithm(this.ptr, ext_type, algorithm);
        }
    }

    /**
     * Add EC point formats to the extension.
     * @param {number} ext_type Extension type.
     * @param {Array<EcPointFormat|number>} points Point list.
     */
    addExtensionEcPoint(ext_type, points) {
        for (const point of points) {
            this.library.Fingerprint_add_ext_ec_point(this.ptr, ext_type, point);
        }
    }

    /**
     * Add an HTTP/2 setting.
     * @param {H2SettingType|number} flag Setting flag.
     * @param {number} value Setting value.
     */
    addH2Setting(flag, value) {
        this.library.Fingerprint_add_h2_setting(this.ptr, flag, value);
    }

    /**
     * Set the HTTP/2 window size.
     * @param {number} size Window size.
     */
    setH2WindowSize(size) {
        this.library.Fingerprint_set_h2_window_size(this.ptr, size);
    }

    /**
     * Set the HTTP/2 priority.
     * @param {boolean} priority Whether to set priority.
     * @param {number} weight Priority weight.
     */
    setH2Priority(priority, weight) {
        this.library.Fingerprint_set_h2_priority(this.ptr, priority, weight);
    }

    /**
     * Clear all HTTP/2 frames.
     */
    clearH2Frame() {
        this.library.Fingerprint_clear_h2_frame(this.ptr)
    }

    /**
     * Add an HTTP/2 frame.
     * @param {H2FrameType|number} typ Frame type.
     * @param {Array<number>} data Frame bytes.
     */
    addH2Frame(typ, data) {
        let u8 = Uint8Array.from(data)
        this.library.Fingerprint_add_h2_frame(this.ptr, typ, u8, u8.length)
    }

    /**
     * Clear all HTTP/3 frames.
     */
    clearH3Frame() {
        this.library.Fingerprint_clean_h3_frame(this.ptr)
    }

    /**
     * Add an HTTP/3 setting.
     * @param {H3SettingType|number} flag Setting flag.
     * @param {number} value Setting value.
     */
    addH3Setting(flag, value) {
        this.library.Fingerprint_add_h3_setting(this.ptr, flag, value)
    }

    /**
     * Add an HTTP/3 frame.
     * @param {H3FrameType|number} typ Frame type.
     * @param {Array<number>} data Frame bytes.
     */
    addH3Frame(typ, data) {
        let u8 = Uint8Array.from(data)
        this.library.Fingerprint_add_h3_frame(this.ptr, typ, u8, u8.length)
    }

    /**
     * Release the native fingerprint resource.
     */
    close() {
        if (this.ptr === null) return
        this.library.Fingerprint_drop(this.ptr);
        this.ptr = null;
    }
}

module.exports = {Fingerprint}