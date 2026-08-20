package org.xllgl2017;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.LongByReference;
import com.sun.jna.ptr.PointerByReference;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import static org.xllgl2017.ReqrioLibrary.REQRIO;

public class Response {
    private final int statusCode;
    private final Headers headers = new Headers();
    private final byte[] body;
    private final int sid;
    private final Pointer req;


    public Response(Pointer raw, Pointer req) throws Exception {
        try {
            this.req = req;
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
            this.sid = REQRIO.Response_sid(raw, err);
            util.check_err_pointer(err);
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

    public Iterable<byte[]> chunks() {
        return () -> new Iterator<>() {
            boolean hashNext = true;
            byte[] chunk;

            @Override
            public boolean hasNext() {
                LongByReference len = new LongByReference();
                PointerByReference err = new PointerByReference();
                Pointer ptr = REQRIO.ScReq_recv_stream(req, sid, len, err);
                try {
                    util.check_err_pointer(err);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                if (ptr == Pointer.NULL) {
                    hashNext = false;
                } else {
                    this.chunk = ptr.getByteArray(0, (int) len.getValue());
                }
                return hashNext;
            }

            @Override
            public byte[] next() {
                if (!hashNext){
                    throw new NoSuchElementException();
                }
                return this.chunk;
            }
        };
    }

    public int getSid() {
        return sid;
    }
}


