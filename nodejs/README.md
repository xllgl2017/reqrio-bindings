# reqrio-js

`reqrio-js` is the Node.js binding for `reqrio`, providing a lightweight, high-performance HTTP and WebSocket client.

## Features

- Native HTTP/HTTPS request engine with low-copy semantics.
- TLS support powered by BoringSSL.
- Supports HTTP/1.0, HTTP/1.1, and HTTP/2.0 via `ALPN`.
- Custom request headers, cookies, timeout settings, and proxy support.
- Supports form data, JSON, text, bytes, and multipart file uploads.
- Includes native WebSocket support.
- Exposes TLS fingerprint options with `Fingerprint` and `Session` methods.

## Installation

```bash
npm install reqrio
```

> This package relies on native bindings and is intended to be used in Node.js.

## Usage

### HTTP Requests

```js
const { Session, ALPN } = require('reqrio');

const session = new Session(
  ALPN.HTTP20,
  {
    'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)',
    Accept: 'text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8',
  },
  true,
  true,
);

session.set_timeout({
  connect: 3000,
  read: 3000,
  write: 3000,
  handle: 30000,
  connect_times: 3,
  handle_times: 3,
});

const resp = session.get('https://www.example.com');
console.log('Status:', resp.status_code());
console.log('Body:', resp.text());

session.close();
```

### Request Options

The `Session` request methods accept an options object with these fields:

- `params`: query parameters object
- `data`: form body object
- `json`: JSON body object
- `bytes`: raw bytes payload
- `files`: array of multipart file objects
- `ct`: content type string
- `sni`: custom SNI host string

### Expressing multipart file upload

```js
const files = [
  {
    path: './example.txt',
    field_name: 'file',
    filetype: 'text/plain',
  },
];

const response = session.post('https://www.example.com/upload', {
  data: { name: 'test' },
  files,
});
console.log(response.status_code());
```

## API Reference

### `Session`

Constructor:

```js
new Session(
  alpn,
  headers = {},
  verify = true,
  auto_redirect = true,
  key_log = null,
  rand_tls = false,
  token = null,
);
```

Common methods:

- `session.set_headers(headers)`
- `session.add_header(name, value)`
- `session.remove_header(name)`
- `session.set_proxy(proxy)`
- `session.set_timeout(timeout)`
- `session.set_cookie(cookie)`
- `session.add_cookie(name, value)`
- `session.reconnect()`
- `session.connect(url)`
- `session.close_stream()`
- `session.set_callback(func)`
- `session.send(method, url, options)`
- `session.get(url, options)`
- `session.post(url, options)`
- `session.put(url, options)`
- `session.head(url, options)`
- `session.delete(url, options)`
- `session.options(url, options)`
- `session.trace(url, options)`
- `session.patch(url, options)`
- `session.close()`

### `Method`

HTTP method enum exported from `Session`.

### `ALPN`

Protocol enum values:

- `ALPN.HTTP10`
- `ALPN.HTTP11`
- `ALPN.HTTP20`

### `Response`

Response methods:

- `response.status_code()`
- `response.get_header(name)`
- `response.cookies()`
- `response.bytes()`
- `response.text()`
- `response.json()`
- `response.close()`

## WebSocket Support

```js
const { Websocket } = require('reqrio');

const ws = new Websocket();
ws.set_uri('wss://example.com/api/ws');
ws.add_header('User-Agent', 'Mozilla/5.0...');
ws.set_proxy('http://127.0.0.1:1080');
ws.open_raw('wss://example.com', '');

const frame = ws.read();
console.log(frame);
ws.close();
```

### WebSocket methods

- `ws.add_header(name, value)`
- `ws.set_proxy(proxy)`
- `ws.set_uri(uri)`
- `ws.open(urlPtr)`
- `ws.open_raw(url, context)`
- `ws.read()`
- `ws.write(opcode, mask, msg)`
- `ws.close()`

## TLS Fingerprinting

`reqrio-js` exposes fingerprint support through `Session` and `Fingerprint`.

### Session fingerprint helpers

- `session.use_random_tls(token)`
- `session.set_ja3(ja3, token)`
- `session.set_ja4(ja4, token)`
- `session.set_finger_by_client_hello(client_hello, token)`
- `session.set_finger_by_custom(custom, token)`

### `Fingerprint`

Create and customize a fingerprint object:

```js
const { Session, Fingerprint } = require('reqrio');
const session = new Session(ALPN.HTTP20);
const fingerprint = new Fingerprint(session.library, token);

fingerprint.add_cipher_suite(CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256);
fingerprint.add_extension(ExtensionType.StatusRequest);
// ... more fingerprint customization ...

session.set_fingerprint(fingerprint);
```

## Example helper script

The repository includes `example.js` with sample usage for:

- GET requests
- POST form data
- POST JSON
- JA3 and JA4 fingerprint usage
- ClientHello fingerprint usage
- Custom fingerprint building

## Notes

- Remember to call `session.close()` to free native resources.
- `response.close()` should also be called when the response object is no longer needed.
- Proxy strings support standard HTTP/SOCKS formats like `http://127.0.0.1:1080`.

## License

Apache-2.0
