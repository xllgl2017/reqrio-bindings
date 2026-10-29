const {check_error, ref_char_ptr} = require('./bindings');

class Body {
    /**
     * Create a request body from raw bytes.
     * @param {object} library Native library handle.
     * @param {Uint8Array} data Raw request bytes.
     * @param {string} content_type Content-Type header.
     * @returns {object} Native body pointer.
     */
    static new(library, data, content_type) {
        const errPtr = ref_char_ptr();
        const bodyPtr = library.Body_new(data, data.length, content_type, errPtr);
        check_error(library, errPtr.deref())
        return bodyPtr;
    }

    /**
     * Create a text/plain request body.
     * @param {object} library Native library handle.
     * @param {string} text Plain text content.
     * @returns {object} Native body pointer.
     */
    static new_text(library, text) {
        const data = new TextEncoder().encode(text);
        return Body.new(library, data, 'text/plain')
    }

    /**
     * Create a form-encoded request body.
     * @param {object} library Native library handle.
     * @param {object} form Form fields map.
     * @param {string} [ct="application/x-www-form-urlencoded"] Content type.
     * @returns {object} Native body pointer.
     */
    static new_form(library, form, ct = "application/x-www-form-urlencoded") {
        let keys = Object.keys(form);
        let res = "";
        for (let i = 0; i < keys.length; i++) {
            res += keys[i];
            res += "=";
            res += encodeURIComponent(form[keys[i]]);
            res += "&";
        }
        if (res.endsWith("&")) {
            res = res.substring(0, res.length - 1);
        }
        return Body.new(library, new TextEncoder().encode(res), ct)
    }

    /**
     * Create a JSON request body.
     * @param {object} library Native library handle.
     * @param {object} json JSON payload.
     * @param {string} [ct='application/json'] Content type.
     * @returns {object} Native body pointer.
     */
    static new_json(library, json, ct = 'application/json') {
        const data = new TextEncoder().encode(JSON.stringify(json));
        return Body.new(library, data, ct)
    }

    /**
     * Create an empty request body.
     * @param {object} library Native library handle.
     * @returns {object} Empty body pointer.
     */
    static none(library) {
        return library.Body_none();
    }

    /**
     * Create a multipart form request body.
     * @param {object} library Native library handle.
     * @param {Array<object>} files File descriptors.
     * @param {object|null} [data=null] Extra form fields.
     * @returns {object} Native body pointer.
     */
    static new_files(library, files, data = null) {
        const http_file = library.HttpFile_new();
        for (const file of files) {
            let errPtr = ref_char_ptr();
            const form = library.FileForm_new(file["path"], file["field_name"], file["filetype"]);
            try {
                check_error(library, errPtr.deref())
                check_error(library, library.HttpFile_add_form(http_file, form))
            } catch (e) {
                library.HttpFile_drop(file)
            }
        }
        let dataPtr = null;
        if (data !== null && data !== undefined) dataPtr = JSON.stringify(dataPtr)

        const errPtr = ref_char_ptr();
        const bodyPtr = library.Body_new_files(http_file, dataPtr, errPtr);
        check_error(library, errPtr);
        return bodyPtr;
    }

}

module.exports = {Body}