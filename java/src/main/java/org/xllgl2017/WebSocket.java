package org.xllgl2017;

import com.sun.jna.Pointer;

/**
 * WebSocket client wrapper.
 */
public class WebSocket {
    private static final ReqrioLibrary INSTANCE = ReqrioLibrary.loadLibrary();
    private Pointer ws;

    /**
     * Open a WebSocket connection.
     * @param url WebSocket URL.
     * @param header Header JSON string.
     */
    public WebSocket(String url,String header) throws Exception {
        this.ws=INSTANCE.ws_open(url,header);
    }

    /**
     * Open a raw WebSocket connection.
     * @param url WebSocket URL.
     * @param raw Raw connection context.
     */
    public void openRaw(String url, String raw) {
        this.ws = INSTANCE.ws_open_raw(url, raw);
    }

    /**
     * Read one WebSocket frame.
     * @return Frame payload as a string.
     */
    public String read() {
        Pointer rd = INSTANCE.ws_read(this.ws);
        String res = rd.getString(0);
        INSTANCE.char_free(rd);
        return res;
    }

    /**
     * Write a WebSocket frame.
     * @param opcode Frame opcode.
     * @param mask Whether to mask the payload.
     * @param msg Message content.
     */
    public void write(int opcode, boolean mask, String msg) throws Exception {
        int ret = INSTANCE.ws_write(this.ws, opcode, mask, msg);
        if (ret == -1) throw new Exception("write error");
    }

    /**
     * Close the WebSocket connection.
     */
    public void close() {
        INSTANCE.ws_close(this.ws);
    }

}
