const {check_error, ref_char_ptr} = require('./bindings')

class Url {

    /**
     * Initialize a URL wrapper.
     * @param {object} library Native library handle.
     * @param {string} base_url Base URL string.
     * @param {object|null} [params=null] Query params object.
     * @param {string|null} [sni=null] Custom SNI value.
     */
    constructor(library, base_url, params = null, sni = null) {
        this.library = library;
        const errPtr = ref_char_ptr();
        this.ptr = this.library.Url_new(base_url, errPtr);
        check_error(this.library, errPtr.deref());
        if (params !== undefined && params !== null) {
            for (const [key, value] of Object.entries(params)) {
                this.add_param(key, value.toString());
            }
        }
        if (sni !== undefined && sni !== null) {
            this.set_sni(sni)
        }
    }

    /**
     * Add a query parameter.
     * @param {string} name Parameter name.
     * @param {string} value Parameter value.
     */
    add_param(name, value) {
        const err = this.library.Url_add_param(this.ptr, name, value);
        check_error(this.library, err, this.drop);
    }

    /**
     * Remove a query parameter.
     * @param {string} name Parameter name.
     */
    remove_param(name) {
        const err = this.library.Url_remove_param(this.ptr, name);
        check_error(err, this.drop);
    }

    /**
     * Set the SNI for this URL.
     * @param {string} sni Server name indication value.
     */
    set_sni(sni) {
        const err = this.library.Url_set_sni(this.ptr, sni);
        check_error(err, this.drop);
    }

    /**
     * Take ownership of the native pointer.
     * @returns {object} Native URL pointer.
     */
    take() {
        let res = this.ptr;
        this.ptr = null;
        return res;
    }

    /**
     * Release the native URL resource.
     */
    drop() {
        if (this.ptr == null) return;
        this.library.Url_drop(this.ptr);
        this.ptr = null;
    }
}

module.exports = {Url}

