const {library, check_error, ref_char_ptr} = require('./bindings')

class Url {

    /**Initialize a URL wrapper.
     * @param library
     * @param {string} base_url
     * @param {object|null} params
     * @param {string|null} sni
     **/
    constructor(library, base_url, params = null, sni = null) {
        this.library = library;
        const errPtr = ref_char_ptr();
        this.ptr = this.library.Url_new(base_url, errPtr);
        check_error(this.library, errPtr.deref());
        if (params !== undefined && params !== null) {
            for (const [key, value] of Object.entries(params)) {
                this.add_param(key, JSON.stringify(value));
            }
        }
        if (sni !== undefined && sni !== null) {
            this.set_sni(sni)
        }
    }

    /**Add a query parameter.
     * @param {string} name
     * @param {string} value
     **/
    add_param(name, value) {
        const err = this.library.Url_add_param(this.ptr, name, value);
        check_error(this.library, err, this.close);
    }

    /**Remove a query parameter.
     * @param {string} name
     **/
    remove_param(name) {
        const err = this.library.Url_remove_param(this.ptr, name);
        check_error(err, this.close);
    }

    /**Set the SNI for this URL; useful when the URL points to an IP address.
     * @param {string} sni
     **/
    set_sni(sni) {
        const err = this.library.Url_set_sni(this.ptr, sni);
        check_error(err, this.close);
    }

    close() {
        if (this.ptr == null) return;
        this.library.Url_drop(this.ptr);
        this.ptr = null;
    }
}

module.exports = {Url}

