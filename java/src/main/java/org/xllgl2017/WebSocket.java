package org.xllgl2017;

import com.sun.jna.Pointer;

public class WebSocket {
    private static final ReqrioLibrary INSTANCE = ReqrioLibrary.loadLibrary();
    private Pointer ws;

    public WebSocket(String url,String header) throws Exception {
        this.ws=INSTANCE.ws_open(url,header);
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
