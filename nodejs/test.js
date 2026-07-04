const {ja4, post_form, post_json, get, client_hello, ja3, custom_fingerprint} = require("./example");

async function test_ws() {
    try {
        let ws = new Websocket();
        ws.open_raw("wss://echo.websocket.org", "");
        let frame = ws.read();
        console.log("WS frame:", frame);
        ws.close();
    } catch (e) {
        console.error("WS error:", e.message);
    }
}


get();
post_form();
post_json();
ja3();
ja4();
client_hello();
custom_fingerprint()