const {library} = require("./bindings");

const registry = new FinalizationRegistry(ws => {
    if (ws && !ws.isNull()) {
        library.ws_close(ws)
    }
})

class Websocket {
    /**
     * Create a WebSocket client.
     */
    constructor() {
        this.build = library.ws_build();
        this.ws = null;
    }

    /**
     * Open a WebSocket connection with a URL pointer.
     * @param {string} url WebSocket URL.
     * @param {Object} [hdr={}] Custom headers.
     */
    open(url, hdr) {
        this.ws = library.ws_open(url, JSON.stringify(hdr ? hdr : {}));
        if (!this.ws || this.ws.isNull()) throw new Error("connect error")
        registry.register(this, this.ws)
    }

    /**
     * Open a WebSocket connection directly from a URL string.
     * @param {string} url WebSocket URL.
     * @param {string} [context=""] Optional connection context.
     */
    open_raw(url, context = "") {
        this.ws = library.ws_open_raw(url, context)
        if (!this.ws || this.ws.isNull()) throw new Error("connect error")
        registry.register(this, this.ws)
    }

    /**
     * Read one WebSocket frame.
     * @returns {object} Parsed frame payload.
     */
    read() {
        const frameStr = library.ws_read(this.ws)
        if (frameStr === null || frameStr === undefined) {
            throw new Error("ws_read returned null");
        }
        return JSON.parse(frameStr)
    }

    /**
     * Send a WebSocket frame.
     * @param {number} opcode Frame opcode.
     * @param {boolean} mask Whether to mask payload.
     * @param {string} msg Message payload.
     */
    write(opcode, mask, msg) {
        let ret = library.ws_write(this.ws, opcode, mask, msg)
        if (ret === -1) throw new Error("ws write error")
    }

    /**
     * Close the WebSocket connection.
     */
    close() {
        registry.unregister(this);
        if (this.ws && !this.ws.isNull()) {
            library.ws_close(this.ws);
            this.ws = null;
        }
    }
}


module.exports = {
    Websocket
}