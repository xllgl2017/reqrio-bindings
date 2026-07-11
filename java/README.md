# reqrio-java

`reqrio-java` is the Java binding for `reqrio`, providing a native HTTP/HTTPS and WebSocket client with TLS fingerprint capabilities.

## Features

- Native HTTP/HTTPS client using BoringSSL via JNA.
- Supports HTTP/1.1 and HTTP/2.0 via `ALPN`.
- Custom headers, cookies, proxy support, and timeout control.
- Supports JSON, form data, text, binary bodies, and multipart file upload.
- Supports TLS fingerprinting through JA3, JA4, ClientHello, and custom fingerprint definitions.
- Includes WebSocket support.

## Requirements

- Java 11 or newer
- Maven for build and packaging

## Maven Dependency

```xml
<dependency>
  <groupId>io.github.xllgl2017</groupId>
  <artifactId>reqrio</artifactId>
  <version>0.3.0-beta1</version>
</dependency>
```

## Quick Start

```java
import org.xllgl2017.ALPN;
import org.xllgl2017.Response;
import org.xllgl2017.Session;
import org.xllgl2017.Timeout;

public class Main {
    public static void main(String[] args) throws Exception {
        Session session = new Session(ALPN.HTTP20);
        session.setVerify(true);
        session.setAutoRedirect(true);

        session.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");

        Timeout timeout = new Timeout();
        timeout.setConnect(3000);
        timeout.setRead(3000);
        timeout.setWrite(3000);
        timeout.setHandle(30000);
        session.setTimeout(timeout);

        Response response = session.get("https://www.example.com");
        System.out.println(response.statusCode());
        System.out.println(response.text());

        session.close();
    }
}
```

## API Reference

### `Session`

Constructor:

```java
Session session = new Session(ALPN alpn);
```

Common methods:

- `setVerify(boolean verify)`
- `setAutoRedirect(boolean auto_redirect)`
- `setKeyLog(String path)`
- `setHeaders(String headerJson)`
- `setHeaders(HashMap<String, String> headers)`
- `setHeaders(Headers headers)`
- `addHeader(String name, String value)`
- `removeHeader(String name)`
- `setProxy(String proxy)`
- `setTimeout(Timeout timeout)`
- `setCookie(String cookie)`
- `addCookie(String name, String value)`
- `closeStream()`
- `reconnect()`
- `connect(String host, int port)`
- `connect(String host)`
- `send(Method method, Url url, Body body)`
- `get(String url)`
- `get(String url, Body body)`
- `post(String url, Body body)`
- `put(String url, Body body)`
- `options(String url, Body body)`
- `head(String url, Body body)`
- `delete(String url, Body body)`
- `trace(String url, Body body)`
- `patch(String url, Body body)`
- `close()`

### `Body`

Construct request bodies using:

- `new Body()` for empty body
- `new Body(byte[] body, String contentType)` for raw bytes
- `new Body(JsonElement json)` for JSON bodies
- `new Body(HashMap<String, String> forms)` for URL-encoded form data
- `new Body(String text)` for plain text
- `new Body(HttpFile file)` and `new Body(HttpFile file, HashMap<String, String> data)` for multipart upload

### `Url`

Create request URLs with optional query parameters or SNI:

- `new Url(String url)`
- `new Url(String url, String sni)`
- `new Url(String url, HashMap<String, String> params)`

### `Response`

Response methods:

- `statusCode()`
- `getHeader(String name)`
- `location()`
- `bytes()`
- `text()`
- `json()`
- `getCookies()`

## Example: POST JSON

```java
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.xllgl2017.Body;
import org.xllgl2017.Response;
import org.xllgl2017.Session;
import org.xllgl2017.Url;

Session session = new Session(ALPN.HTTP20);
Body body = new Body(JsonParser.parseString("{\"name\":\"test\"}"));
Response response = session.post("https://www.example.com/api", body);
System.out.println(response.statusCode());
System.out.println(response.text());
session.close();
```

## TLS Fingerprinting

`reqrio-java` supports fingerprint configuration via `Fingerprint`:

```java
import org.xllgl2017.Fingerprint;
import org.xllgl2017.ALPN;
import org.xllgl2017.Session;

Session session = new Session(ALPN.HTTP20);
Fingerprint fingerprint = Fingerprint.fromJa3("771,4865-...", token);
session.setFingerprint(fingerprint);
```

Supported fingerprint creation methods:

- `Fingerprint.random(String token)`
- `Fingerprint.fromJa3(String ja3, String token)`
- `Fingerprint.fromJa4(String ja4, String token)`
- `Fingerprint.fromClientHello(byte[] clientHello, String token)`
- `Fingerprint.fromCustom(CustomFingerprint custom, String token)`

### Custom fingerprint

Use `CustomFingerprint` to build fingerprint JSON data and pass it to `Fingerprint.fromCustom(...)`.

## WebSocket Support

```java
import org.xllgl2017.WebSocket;

WebSocket ws = new WebSocket("wss://example.com");
ws.addHeader("User-Agent", "Mozilla/5.0...");
ws.open();
String frame = ws.read();
System.out.println(frame);
ws.close();
```

WebSocket methods:

- `addHeader(String name, String value)`
- `setProxy(String proxy)`
- `set_url(String url)`
- `set_uri(String uri)`
- `open()`
- `openRaw(String url, String raw)`
- `read()`
- `write(int opcode, boolean mask, String msg)`
- `close()`

## Notes

- Call `session.close()` to release native resources.
- Use `response.text()` or `response.json()` to read response content.
- `Body`, `Url`, and `Response` implement cleanup patterns; manage them carefully in long-lived code.

## License

Apache-2.0
