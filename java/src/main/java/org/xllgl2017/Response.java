package org.xllgl2017;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.LongByReference;
import com.sun.jna.ptr.PointerByReference;

import java.util.ArrayList;
import java.util.List;

import static org.xllgl2017.ReqrioLibrary.REQRIO;

public class Response {
    private final int statusCode;
    private final Headers headers = new Headers();
    private final byte[] body;


    public Response(Pointer raw) throws Exception {
        try {
            // status code
            PointerByReference err = new PointerByReference();
            this.statusCode = REQRIO.Response_status_code(raw, err);
            util.check_err_pointer(err);

            //header
            Pointer key_ptr = REQRIO.Response_header_keys(raw, err);
            util.check_err_pointer(err);
            String[] keys = key_ptr.getString(0).split(",,,,");
            REQRIO.char_free(key_ptr);
            for (String key : keys) {
                Pointer ptr = REQRIO.Response_get_header(raw, key, err);
                util.check_err_pointer(err);
                this.headers.addHeader(key, ptr.getString(0));
                REQRIO.char_free(ptr);
            }

            //cookies
            Pointer ptr = REQRIO.Response_cookies(raw, err);
            util.check_err_pointer(err);
            String cookie_str = ptr.getString(0);
            REQRIO.char_free(ptr);
            Gson gson = new Gson();
            ArrayList<Cookie> cookies = gson.fromJson(cookie_str, new TypeToken<ArrayList<Cookie>>() {
            }.getType());
            this.headers.setCookies(cookies);

            //body
            LongByReference len = new LongByReference();
            Pointer body_ptr = REQRIO.Response_bytes(raw, len, err);
            util.check_err_pointer(err);
            this.body = body_ptr.getByteArray(0, (int) len.getValue());
        } finally {
            REQRIO.Response_drop(raw);
        }
    }

    public int statusCode() {
        return this.statusCode;

    }

    public String getHeader(String name) {
        return this.headers.get(name);
    }

    public String location() {
        return this.getHeader("location");
    }

    public byte[] bytes() {
        return this.body;
    }

    public String text() {
        return new String(this.bytes());
    }

    public JsonElement json() {
        Gson gson = new Gson();
        return gson.fromJson(this.text(), JsonElement.class);
    }

    public List<Cookie> getCookies() {
        return headers.getCookies();
    }

}
