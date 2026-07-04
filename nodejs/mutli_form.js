import {library} from "./bindings";

class HttpFile {
    constructor() {
        this.ptr = library.HttpFile_new();
        if (!this.ptr || this.ptr.isNull()) throw new Error("HttpFile_new failed");
    }

    add_form(file_form_ptr) {
        call_char_func(library.HttpFile_add_form, this.ptr, file_form_ptr);
    }

    close() {
        if (this.ptr && !this.ptr.isNull()) {
            library.HttpFile_drop(this.ptr);
            this.ptr = null;
        }
    }

    /**
     * Create a FileForm for use with HttpFile
     */
    static new_file_form(path, field_name, filetype) {
        const errPtr = Buffer.alloc(8);
        const formPtr = library.FileForm_new(path, field_name, filetype, errPtr);
        const errMsg = read_err_output(errPtr);
        if (errMsg) throw new Error(`FileForm_new failed: ${errMsg}`);
        if (!formPtr || formPtr.isNull()) throw new Error("FileForm_new returned null");
        return formPtr;
    }
}