package org.xllgl2017;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

import static org.xllgl2017.ReqrioLibrary.REQRIO;

/**
 * Multipart upload file wrapper.
 */
public class HttpFile implements AutoCloseable {
    private Pointer raw;

    /**
     * Create an empty file upload container.
     */
    public HttpFile() {
        this.raw = REQRIO.HttpFile_new();
    }

    /**
     * Add a file with the default field name.
     * @param path File path.
     */
    public void addFile(String path) throws Exception {
        this.addFile(path, "file");
    }

    /**
     * Add a file with a custom field name.
     * @param path File path.
     * @param fieldName Form field name.
     */
    public void addFile(String path, String fieldName) throws Exception {
        this.addFile(path, fieldName, null);
    }

    /**
     * Add a file with a custom field name and content type.
     * @param path File path.
     * @param fieldName Form field name.
     * @param content_type MIME content type.
     */
    public void addFile(String path, String fieldName, String content_type) throws Exception {
        PointerByReference err = new PointerByReference();
        Pointer form = REQRIO.FileForm_new(path, fieldName, content_type, err);
        try {
            util.check_err_pointer(err);
            util.check_err(REQRIO.HttpFile_add_form(this.raw, form));
        } catch (Exception e) {
            close();
            throw e;
        }
    }

    /**
     * Set the native pointer.
     * @param raw Native pointer.
     */
    public void setRaw(Pointer raw) {
        this.raw = raw;
    }

    /**
     * Get the native pointer.
     * @return Native pointer.
     */
    public Pointer getRaw() {
        return raw;
    }

    @Override
    public void close() {
        if (this.raw == null) return;
        REQRIO.HttpFile_drop(this.raw);
        this.raw = null;
    }
}
