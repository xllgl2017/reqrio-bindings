const ref = require("ref-napi");

class Fingerprint {
    constructor(library, token) {
        this.library = library;
        this.ptr = this.library.Fingerprint_new(token);
    }

    add_cipher_suite(suite) {
        this.library.Fingerprint_add_cipher_suite(this.ptr, suite);
    }

    add_extension(ext_type) {
        this.library.Fingerprint_add_ext(this.ptr, ext_type);
    }

    /**
     * @param {number} ext_type
     * @param {array} alps
     */
    add_extension_alps(ext_type, alps) {
        for (const alpn of alps) {
            this.library.Fingerprint_add_ext_alpn(this.ptr, ext_type, alpn)
        }
    }

    /**
     * @param {number} ext_type
     * @param {array} versions
     */
    add_extension_versions(ext_type, versions) {
        for (const version of versions) {
            this.library.Fingerprint_add_ext_version(this.ptr, ext_type, version);
        }
    }

    /**
     * @param {number} ext_type
     * @param {array} curves
     */
    add_extension_curves(ext_type, curves) {
        for (const curve of curves) {
            this.library.Fingerprint_add_ext_curve(this.ptr, ext_type, curve);
        }
    }

    /**
     * @param {number} ext_type
     * @param {Uint16Array} methods
     */
    add_extension_compress(ext_type, methods) {
        for (const method of methods) {
            this.library.Fingerprint_add_ext_compress(this.ptr, ext_type, method);
        }
    }

    add_extension_psk_mode(ext_type, mode) {
        this.library.Fingerprint_add_ext_psk_mode(this.ptr, ext_type, mode);
    }

    /**
     * @param {number} ext_type
     * @param {number} padding
     */
    add_extension_padding(ext_type, padding) {
        this.library.Fingerprint_add_ext_padding(this.ptr, ext_type, padding);
    }

    /**
     * @param {number} ext_type
     * @param {array} bytes
     */
    add_extension_bytes(ext_type, bytes) {
        const bytes_u8 = Uint8Array.from(bytes)
        this.library.Fingerprint_add_ext_bytes(this.ptr, ext_type, bytes_u8, bytes_u8.length);
    }

    /**
     * @param {number} ext_type
     * @param {array} algorithms
     */
    add_extension_algorithms(ext_type, algorithms) {
        for (const algorithm of algorithms) {
            this.library.Fingerprint_add_ext_algorithm(this.ptr, ext_type, algorithm);
        }
    }

    /**
     * @param {number} ext_type
     * @param {array} points
     */
    add_extension_ec_point(ext_type, points) {
        for (const point of points) {
            this.library.Fingerprint_add_ext_ec_point(this.ptr, ext_type, point);
        }
    }

    add_h2_setting(flag, value) {
        this.library.Fingerprint_add_h2_setting(this.ptr, flag, value);
    }

    set_h2_window_size(size) {
        this.library.Fingerprint_set_h2_window_size(this.ptr, size);
    }

    /**
     * @param {boolean} priority
     * @param {number} weight
     */
    set_h2_priority(priority, weight) {
        this.library.Fingerprint_set_h2_priority(this.ptr, priority, weight);
    }

    close() {
        if (this.ptr === null) return
        this.library.Fingerprint_drop(this.ptr);
        this.ptr = null;
    }
}

module.exports = {Fingerprint}