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

/**
 * HTTP response wrapper.
 */
public class Response {
    private final int statusCode;
    private final Headers headers = new Headers();
    private final byte[] body;
    private final int sid;
    private final Pointer req;

    /**
     * Create a response object from native data.
     * @param raw Native response pointer.
     * @param req Native request pointer.
     * @param stream Whether the response is streamed.
     */
    public Response(Pointer raw, Pointer req, boolean stream) throws Exception {
        try {
            this.req = req;
            PointerByReference err = new PointerByReference();
            this.statusCode = REQRIO.Response_status_code(raw, err);
            util.check_err_pointer(err);

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

            Pointer ptr = REQRIO.Response_cookies(raw, err);
            util.check_err_pointer(err);
            String cookie_str = ptr.getString(0);
            REQRIO.char_free(ptr);
            Gson gson = new Gson();
            ArrayList<Cookie> cookies = gson.fromJson(cookie_str, new TypeToken<ArrayList<Cookie>>() {
            }.getType());
            this.headers.setCookies(cookies);

            LongByReference len = new LongByReference();
            Pointer body_ptr = REQRIO.Response_bytes(raw, len, err);
            util.check_err_pointer(err);
            this.body = body_ptr.getByteArray(0, (int) len.getValue());
            this.sid = REQRIO.Response_sid(raw, err);
            util.check_err_pointer(err);
        } finally {
            if (!stream) REQRIO.Response_drop(raw);
        }
    }

    /**
     * Get the HTTP status code.
     * @return Status code.
     */
    public int statusCode() {
        return this.statusCode;

    }

    /**
     * Get response header by name.
     * @param name Header name.
     * @return Header value or null.
     */
    public String getHeader(String name) {
        return this.headers.get(name);
    }

    /**
     * Get the Location header value.
     * @return Redirect location or null.
     */
    public String location() {
        return this.getHeader("location");
    }

    /**
     * Get the full response body as bytes.
     * @return Body bytes.
     */
    public byte[] bytes() {
        return this.body;
    }

    /**
     * Get the response body as text.
     * @return Body text.
     */
    public String text() {
        return new String(this.bytes());
    }

    /**
     * Parse the response body as JSON.
     * @return JSON element.
     */
    public JsonElement json() {
        Gson gson = new Gson();
        return gson.fromJson(this.text(), JsonElement.class);
    }

    /**
     * Get all cookies in the response.
     * @return Cookie list.
     */
    public List<Cookie> getCookies() {
        return headers.getCookies();
    }

    /**
     * Stream response chunks.
     * @return Iterable chunk stream.
     */
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
                if (!hashNext) {
                    throw new NoSuchElementException();
                }
                return this.chunk;
            }
        };
    }

    /**
     * Get the request session id.
     * @return Session id.
     */
    public int getSid() {
        return sid;
    }
}


