const {library} = require("./bindings");

const registry = new FinalizationRegistry(ws => {
    if (ws && !ws.isNull()) {
        library.ws_close(ws)
    }
})

class Websocket {
    constructor() {
        this.build = library.ws_build();
        this.ws = null;
    }

    /**
     * Open WebSocket connection with a Url object pointer
     * @param {string} url - url
     * @param {Object} hdr - header
     */
    open(url, hdr) {
        this.ws = library.ws_open(url, JSON.stringify(hdr ? hdr : {}));
        if (!this.ws || this.ws.isNull()) throw new Error("connect error")
        registry.register(this, this.ws)
    }

    /**
     * Open WebSocket connection directly with URL string
     * @param {string} url - WebSocket URL
     * @param {string} context - optional context string
     */
    open_raw(url, context = "") {
        this.ws = library.ws_open_raw(url, context)
        if (!this.ws || this.ws.isNull()) throw new Error("connect error")
        registry.register(this, this.ws)
    }

    read() {
        const frameStr = library.ws_read(this.ws)
        if (frameStr === null || frameStr === undefined) {
            throw new Error("ws_read returned null");
        }
        return JSON.parse(frameStr)
    }

    write(opcode, mask, msg) {
        let ret = library.ws_write(this.ws, opcode, mask, msg)
        if (ret === -1) throw new Error("ws write error")
    }

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