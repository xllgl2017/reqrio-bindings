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

    constructor(ptr, library) {
        this.library = library;
        this.ptr = ptr;
    }

    /**
     * @param {Version} rv :record version
     * @param {Version} mv : message version
     * @param {string} token :auth token
     * @param library :
     */

    static fromCustom(rv, mv, token, library) {
        let ptr = library.Fingerprint_new(rv, mv, token);
        return new Fingerprint(ptr, library)
    }

    /**Set a fingerprint from a raw ClientHello payload.
     * @param {Uint8Array} client_hello raw ClientHello bytes
     * @param {string} token authentication token
     * @param {Library} library dll
     **/
    static fromClientHello(client_hello, token, library) {
        const errPtr = ref_char_ptr();
        const finger = library.Fingerprint_from_client_hello(client_hello, client_hello.length, token, errPtr);
        check_error(library, errPtr.deref());
        return new Fingerprint(finger, library)

    }

    /**Set a JA3 fingerprint.
     * @param {string} ja3 JA3 fingerprint string
     * @param {string} token authentication token
     * @param {Library} library dll
     **/
    static fromJa3(ja3, token, library) {
        const errPtr = ref_char_ptr();
        const finger = library.Fingerprint_from_ja3(ja3, token, errPtr);
        check_error(library, errPtr.deref());
        return new Fingerprint(finger, library)
    }

    /**Set a JA4 fingerprint.
     * @param {string} ja4 JA4 fingerprint string
     * @param {string} token authentication token
     * @param {Library} library dll
     **/
    static fromJa4(ja4, token, library) {
        const errPtr = ref_char_ptr();
        const finger = library.Fingerprint_from_ja4(ja4, token, errPtr);
        check_error(library, errPtr.deref());
        return new Fingerprint(finger, library)
    }

    /**Use a random TLS fingerprint.
     * @param {string} token authentication token
     * @param {Library} library dll
     **/
    static fromRandom(token, library) {
        const errPtr = ref_char_ptr();
        let ptr = library.Fingerprint_random(token, errPtr);
        check_error(library, errPtr.deref())
        return new Fingerprint(ptr, library)
    }

    /**Use a built-in TLS fingerprint.
     * @param {string} name fingerprint name
     * @param {string} token authentication token
     * @param {boolean} sec_full whether to use the full sec-* header set
     * @param {Library} library dll
     **/
    static fromFingerBuiltin(name, token, sec_full, library) {
        const errPtr = ref_char_ptr();
        let ptr = library.Fingerprint_from_builtin(name, sec_full, token, errPtr)
        check_error(library, errPtr.deref())
        return new Fingerprint(ptr, library)
    }


    /**Use a preconfigured fingerprint by index.
     * @param {int} idx fingerprint index
     * @param {string} auth authorization token
     * @param {boolean} sec_full whether to use the full sec-* header set
     * @param {Library} library dll
     **/
    static fromFingerIdx(idx, auth, sec_full, library) {
        const errPtr = ref_char_ptr();
        let ptr = library.Fingerprint_from_idx(idx, auth, sec_full, errPtr)
        check_error(library, errPtr.deref())
        return new Fingerprint(ptr, library)

    }

    /**添加CipherSuite
     * @param {CipherSuite|number} suite
     **/
    addCipherSuite(suite) {
        this.library.Fingerprint_add_cipher_suite(this.ptr, suite);
    }


    /**添加Extension
     * @param {ExtensionType|number} ext_type
     **/
    addExtension(ext_type) {
        this.library.Fingerprint_add_ext(this.ptr, ext_type);
    }

    /**
     * @param {ExtensionType|number} ext_type
     * @param {Array<string>} alps
     */
    addExtensionAlps(ext_type, alps) {
        for (const alpn of alps) {
            this.library.Fingerprint_add_ext_alpn(this.ptr, ext_type, alpn)
        }
    }

    /**
     * @param {ExtensionType|number} ext_type
     * @param {Array<string>} versions
     */
    addExtensionVersions(ext_type, versions) {
        for (const version of versions) {
            this.library.Fingerprint_add_ext_version(this.ptr, ext_type, version);
        }
    }

    /**
     * @param {ExtensionType|number} ext_type
     * @param {Array<number|NamedCurve>} curves
     */
    addExtensionCurves(ext_type, curves) {
        for (const curve of curves) {
            this.library.Fingerprint_add_ext_curve(this.ptr, ext_type, curve);
        }
    }

    /**
     * @param {number} ext_type
     * @param {Array<number|CompressionMethod>} methods
     */
    addExtensionCompress(ext_type, methods) {
        for (const method of methods) {
            this.library.Fingerprint_add_ext_compress(this.ptr, ext_type, method);
        }
    }

    addExtensionPskMode(ext_type, mode) {
        this.library.Fingerprint_add_ext_psk_mode(this.ptr, ext_type, mode);
    }

    /**
     * @param {number} ext_type
     * @param {number} padding
     */
    addExtensionPadding(ext_type, padding) {
        this.library.Fingerprint_add_ext_padding(this.ptr, ext_type, padding);
    }

    /**
     * @param {number} ext_type
     * @param {array} bytes
     */
    addExtensionBytes(ext_type, bytes) {
        const bytes_u8 = Uint8Array.from(bytes)
        this.library.Fingerprint_add_ext_bytes(this.ptr, ext_type, bytes_u8, bytes_u8.length);
    }

    /**
     * @param {number} ext_type
     * @param {Array<SignatureAlgorithm|number>} algorithms
     */
    addExtensionAlgorithms(ext_type, algorithms) {
        for (const algorithm of algorithms) {
            this.library.Fingerprint_add_ext_algorithm(this.ptr, ext_type, algorithm);
        }
    }

    /**
     * @param {number} ext_type
     * @param {Array<EcPointFormat|number>} points
     */
    addExtensionEcPoint(ext_type, points) {
        for (const point of points) {
            this.library.Fingerprint_add_ext_ec_point(this.ptr, ext_type, point);
        }
    }

    /**
     * @param {H2SettingType|number} flag
     * @param {number} value
     */
    addH2Setting(flag, value) {
        this.library.Fingerprint_add_h2_setting(this.ptr, flag, value);
    }

    /**
     * @param {number} size
     */
    setH2WindowSize(size) {
        this.library.Fingerprint_set_h2_window_size(this.ptr, size);
    }

    /**
     * @param {boolean} priority
     * @param {number} weight
     */
    setH2Priority(priority, weight) {
        this.library.Fingerprint_set_h2_priority(this.ptr, priority, weight);
    }

    clearH2Frame() {
        this.library.Fingerprint_clear_h2_frame(this.ptr)
    }

    /**
     * @param {H2FrameType|number} typ
     * @param {array} data
     */
    addH2Frame(typ, data) {
        let u8 = Uint8Array.from(data)
        this.library.Fingerprint_add_h2_frame(this.ptr, typ, u8, u8.length)
    }

    clearH3Frame() {
        this.library.Fingerprint_clean_h3_frame(this.ptr)
    }

    /**
     * @param {H3SettingType|number} flag
     * @param {number} value
     */
    addH3Setting(flag, value) {
        this.library.Fingerprint_add_h3_setting(this.ptr, flag, value)
    }

    /**
     * @param {H3FrameType|number} typ
     * @param {array} data
     */
    addH3Frame(typ, data) {
        let u8 = Uint8Array.from(data)
        this.library.Fingerprint_add_h3_frame(this.ptr, typ, u8, u8.length)
    }


    close() {
        if (this.ptr === null) return
        this.library.Fingerprint_drop(this.ptr);
        this.ptr = null;
    }
}

module.exports = {Fingerprint}