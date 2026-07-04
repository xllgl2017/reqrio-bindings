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

    add_header(name, value) {
        let ret = library.ws_add_header(this.build, name, value)
        if (ret === -1) throw new Error("add header error")
    }

    set_proxy(proxy) {
        let ret = library.ws_set_proxy(this.build, proxy);
        if (ret === -1) throw new Error("set proxy error")
    }

    set_uri(uri) {
        let ret = library.ws_set_uri(this.build, uri)
        if (ret === -1) throw new Error("set uri error")
    }

    /**
     * Open WebSocket connection with a Url object pointer
     * @param {Buffer} urlPtr - pointer to Url object
     */
    open(urlPtr) {
        this.ws = library.ws_open(this.build, urlPtr)
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