const {library, check_error, ref_char_ptr} = require('./bindings')

class Url {

    /**初始化Url
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

    /**添加一个查询参数
     * @param {string} name
     * @param {string} value
     **/
    add_param(name, value) {
        const err = this.library.Url_add_param(this.ptr, name, value);
        check_error(this.library, err, this.close);
    }

    /**移除一个查询参数
     * @param {string} name
     **/
    remove_param(name) {
        const err = this.library.Url_remove_param(this.ptr, name);
        check_error(err, this.close);
    }

    /**为该url设置SNI，在使用ip地址URL时使用
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

