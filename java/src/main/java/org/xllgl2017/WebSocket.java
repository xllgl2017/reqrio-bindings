package org.xllgl2017;

import com.sun.jna.Pointer;

public class WebSocket {
    private static final ReqrioLibrary INSTANCE = ReqrioLibrary.loadLibrary();
    private final Pointer builder;
    private Pointer ws;

    public WebSocket(String url) throws Exception {
        this.builder = INSTANCE.ws_build();
        this.set_url(url);
    }

    public void addHeader(String name, String value) throws Exception {
        int ret = INSTANCE.ws_add_header(this.builder, name, value);
        if (ret == -1) throw new Exception("add header error");
    }


    public void setProxy(String proxy) throws Exception {
        int ret = INSTANCE.ws_set_proxy(this.builder, proxy);
        if (ret == -1) throw new Exception("set proxy error");
    }

    public void set_url(String url) throws Exception {
        int ret = INSTANCE.ws_set_url(this.builder, url);
        if (ret == -1) throw new Exception("set uri error");
    }

    public void set_uri(String uri) throws Exception {
        int ret = INSTANCE.ws_set_uri(this.builder, uri);
        if (ret == -1) throw new Exception("set uri error");
    }

    public void open() throws Exception {
        this.ws = INSTANCE.ws_open(this.builder);
        if (this.ws == null) throw new Exception("connect fail");
    }

    public void openRaw(String url, String raw) {
        this.ws = INSTANCE.ws_open_raw(url, raw);
    }

    public String read() {
        Pointer rd = INSTANCE.ws_read(this.ws);
        String res = rd.getString(0);
        INSTANCE.char_free(rd);
        return res;
    }

    public void write(int opcode, boolean mask, String msg) throws Exception {
        int ret = INSTANCE.ws_write(this.ws, opcode, mask, msg);
        if (ret == -1) throw new Exception("write error");
    }

    public void close() {
        INSTANCE.ws_close(this.ws);
    }

}
