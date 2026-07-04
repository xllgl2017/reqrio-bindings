package org.xllgl2017;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;
import org.jetbrains.annotations.NotNull;

import static org.xllgl2017.ReqrioLibrary.REQRIO;

import java.io.StringWriter;
import java.util.HashMap;

public class Body implements AutoCloseable {
    private Pointer raw;

    public Pointer pointer() throws Exception {
        if (this.raw == null) throw new Exception("Body had dropped");
        return this.raw;
    }

    public Body() {
        this.raw = REQRIO.Body_none();
    }

    /// @param body        :请求体二进制
    /// @param contentType : 请求头类型
    public Body(byte[] body, String contentType) throws Exception {
        PointerByReference err = new PointerByReference();
        this.raw = REQRIO.Body_new(body, body.length, contentType, err);
        util.check_err_pointer(err);
    }

    /// JSON 数据格式(application/json)
    ///
    /// @param json :json请求头
    public Body(@NotNull JsonElement json) throws Exception {
        this(json.toString().getBytes(), "application/json");
    }

    /// Urlencoded 表单数据格式(application/x-www-form-urlencoded)
    ///
    /// @param forms :表单数据，key和value都应该未进行编码
    public Body(@NotNull HashMap<String, String> forms) throws Exception {
        StringWriter writer = new StringWriter();
        for (String key : forms.keySet()) {
            writer.write(key);
            writer.write("=");
            Pointer ptr = REQRIO.url_encode(forms.get(key));
            if (ptr == null) throw new Exception("value encode error");
            writer.write(ptr.getString(0));
            REQRIO.char_free(ptr);
            writer.write("&");
        }
        String encoded = writer.toString();
        if (encoded.endsWith("&")) {
            encoded = encoded.substring(0, encoded.length() - 1);
        }
        byte[] form_bytes = encoded.getBytes();
        PointerByReference err = new PointerByReference();
        this.raw = REQRIO.Body_new(form_bytes, form_bytes.length, "application/x-www-form-urlencoded", err);
        util.check_err_pointer(err);

    }

    /// 文本数据格式(text/plain)
    ///
    /// @param text :请求体文本
    public Body(@NotNull String text) throws Exception {
        this(text.getBytes(), "text/plain");
    }

    public Body(HttpFile file) throws Exception {
        this(file, new HashMap<>());
    }

    public void setRaw(Pointer raw) {
        this.raw = raw;
    }

    /// @param data 表单其他字段
    public Body(HttpFile file, HashMap<String, String> data) throws Exception {
        PointerByReference err = new PointerByReference();
        Gson gson = new Gson();
        this.raw = REQRIO.Body_new_files(file.getRaw(), gson.toJson(data), err);
        util.check_err_pointer(err);
    }

    @Override
    public void close() {
        if (this.raw == null) return;
        REQRIO.Body_drop(this.raw);
        this.raw = null;
    }
}
