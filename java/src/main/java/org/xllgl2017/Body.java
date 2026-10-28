package org.xllgl2017;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;
import org.jetbrains.annotations.NotNull;

import static org.xllgl2017.ReqrioLibrary.REQRIO;

import java.io.StringWriter;
import java.util.HashMap;

/**
 * HTTP request body wrapper.
 */
public class Body implements AutoCloseable {
    private Pointer raw;

    /**
     * Get the native pointer for the body.
     * @return Native body pointer.
     */
    public Pointer pointer() throws Exception {
        if (this.raw == null) throw new Exception("Body had dropped");
        return this.raw;
    }

    /**
     * Create an empty body.
     */
    public Body() {
        this.raw = REQRIO.Body_none();
    }

    /**
     * Create a body from raw bytes.
     * @param body Raw request bytes.
     * @param contentType Content-Type header.
     */
    public Body(byte[] body, String contentType) throws Exception {
        PointerByReference err = new PointerByReference();
        this.raw = REQRIO.Body_new(body, body.length, contentType, err);
        util.check_err_pointer(err);
    }

    /**
     * Create a JSON body.
     * @param json JSON payload.
     */
    public Body(@NotNull JsonElement json) throws Exception {
        this(json.toString().getBytes(), "application/json");
    }

    /**
     * Create a form body.
     * @param forms Form fields map.
     */
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

    /**
     * Create a plain-text body.
     * @param text Text payload.
     */
    public Body(@NotNull String text) throws Exception {
        this(text.getBytes(), "text/plain");
    }

    /**
     * Create a multipart body from a file.
     * @param file File to upload.
     */
    public Body(HttpFile file) throws Exception {
        this(file, new HashMap<>());
    }

    /**
     * Set the native raw body pointer.
     * @param raw Native pointer.
     */
    public void setRaw(Pointer raw) {
        this.raw = raw;
    }

    /**
     * Create a multipart body from a file and extra form data.
     * @param file File to upload.
     * @param data Extra form fields.
     */
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
