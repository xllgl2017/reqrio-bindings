package org.xllgl2017;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

import static org.xllgl2017.ReqrioLibrary.REQRIO;

public class HttpFile implements AutoCloseable {
    private Pointer raw;

    public HttpFile() {
        this.raw = REQRIO.HttpFile_new();
    }


    public void addFile(String path) throws Exception {
        this.addFile(path, "file");
    }

    public void addFile(String path, String fieldName) throws Exception {
        this.addFile(path, fieldName, null);
    }

    /// @param path         :文件路径，相对路径或绝对路径
    /// @param fieldName    :文件所在表单的属性名
    /// @param content_type :文件内容类型
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

    public void setRaw(Pointer raw) {
        this.raw = raw;
    }

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
