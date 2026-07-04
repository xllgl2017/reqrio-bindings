# reqrio-py

`reqrio-py` is the Python binding for `reqrio`, a lightweight and high-concurrency HTTP request library.

## Features

- High-performance HTTP/HTTPS request engine with low-copy semantics.
- Built-in TLS support using BoringSSL.
- Supports HTTP/1.1 and HTTP/2.0 via `ALPN`.
- Supports custom headers, cookies, timeouts, proxies, and certificate verification.
- Supports form data, JSON, text, bytes, and multipart file uploads.
- Includes a WebSocket client wrapper.

## Installation

```bash
pip install reqrio
```

> Python 3.9 or newer is required.

## Quick Start

```python
import reqrio

session = reqrio.Session(
    headers={
        "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64)...",
        "Accept": "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
    },
    alpn=reqrio.ALPN.HTTP20,
    verify=True,
)

session.set_timeout(3000, 3000, 3000, 30000)

response = session.get("https://www.example.com")
print(response.statue_code())
print(response.text())

session.close()
```

## API Reference

### `reqrio.Session`

Create a session:

```python
session = reqrio.Session(
    headers=None,
    alpn=reqrio.ALPN.HTTP11,
    verify=True,
    proxy=None,
    key_log=None,
    ja3=None,
    ja4=None,
    client_hello=None,
    random_tls=False,
    custom_tls=None,
    token="",
)
```

Common methods:

- `session.set_headers(headers: dict)`
- `session.add_header(name: str, value: str)`
- `session.remove_header(name: str)`
- `session.set_timeout(connect, read, write, handle, connect_times=3, handle_times=3)`
- `session.set_cookie(cookie: str)`
- `session.add_cookie(name: str, value: str)`
- `session.get(url, params=None, data=None, json=None, bytes=None, text=None, **kwargs)`
- `session.post(url, params=None, data=None, json=None, bytes=None, text=None, **kwargs)`
- `session.open_stream(method, url, params=None, data=None, json=None, bytes=None, text=None, **kwargs)`
- `session.close()`

### Helper functions

`reqrio` also offers shortcut functions for single requests:

```python
resp = reqrio.get(
    "https://www.example.com",
    headers={"User-Agent": "..."},
    params={"q": "test"},
)
print(resp.statue_code())

resp = reqrio.post(
    "https://www.example.com/api",
    headers={"Content-Type": "application/json"},
    json={"key": "value"},
)
print(resp.statue_code())
```

### WebSocket support

```python
from reqrio import WebSocket, WsOpCode

headers = {
    "Origin": "https://example.com",
    "User-Agent": "Mozilla/5.0...",
}

ws = WebSocket(
    "wss://example.com/",
    uri="wss://example.com/api/ws",
    headers=headers,
)
ws.open()

while True:
    frame = ws.read()
    if frame.opcode == WsOpCode.PING:
        ws.write(WsOpCode.PONG, frame.payload)
    else:
        print(frame.payload.decode("utf-8"))

ws.close()
```

## Request Body Types

Supported request bodies:

- `data={...}` for form data
- `json={...}` for JSON payloads
- `text="..."` for plain text
- `bytes=b"..."` for raw bytes
- `files=[...]` for multipart file upload

File upload example:

```python
files = [
    {
        "path": "./example.txt",
        "field_name": "file",
        "filetype": "text/plain",
    }
]

response = session.post(
    "https://www.example.com/upload",
    data={"name": "test"},
    files=files,
)
print(response.statue_code())
```

## TLS Fingerprinting and Advanced Options

`reqrio.Session` supports advanced TLS configuration:

- `ja3`: JA3 fingerprint string
- `ja4`: JA4 fingerprint string
- `client_hello`: raw TLS ClientHello bytes
- `random_tls`: random TLS fingerprint
- `custom_tls`: custom TLS configuration dictionary
- `token`: fingerprint authentication token

Additional options:

- `verify=False` to disable certificate verification
- `proxy` for HTTP/SOCKS proxy support
- `key_log` to save TLS key material for debugging

## Notes

- `reqrio-py` is designed to reuse connections when possible.
- Be sure to call `session.close()` to release resources.

## License

Apache-2.0
