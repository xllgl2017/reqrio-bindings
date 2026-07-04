### reqrio is an HTTP request library designed for fast, simple, and convenient HTTP request usage.

* Features: Low copy, high concurrency, low overhead

* Supports TLS fingerprinting, which can be configured via hexadecimal, Ja3, or Ja4 TLS handshake settings (*
  *subscription only**).

* Ensures **request header order** (see [Request Header Order Table](#request-header-order-table)), consistent with
  browsers.

* Uses **BoringSSL** to implement TLS, consistent with browsers like Chrome and Edge.

**Note:** To compile, cgo must be turned on, that is, set CGO-ENABLED=1

### Low-Copy

`reqrio` is a low copy request sending engine used to efficiently encrypt user or file data over TLS and send it to TCP.
`reqrio`
Convert user input data such as form data, json, bytes, text, etc. into bytes for storage, and only copy once during TLS
encryption, while only the data is processed in other stages
Borrow (borrowing). File uploads are read through into_deader to reduce memory overhead

```text

        Form  ┌────────┐encode->bytes ┌──────────┐             ┌──────────┐
 User ───────►│        │─────────────►│          │             │          │
        Json  │ ScReq  │  into_bytes  │  Request │ copy slice  │ fragment │ write ┌───────┐
              │ AcReq  │              │  borrow  │────────────►│  TLS     │──────►│  TCP  │
       Files  │(Engine)│ into_reader  │  reader  │             │ Encrypt  │       └───────┘
 User ───────►│        │─────────────►│          │             │          │
              └────────┘              └──────────┘             └──────────┘
```

### Request Header Order Table

| No. | HTTP/2.0                    | HTTP/1.1                  |
|:----|:----------------------------|:--------------------------|
| 1   | cache-control               | Host                      |
| 2   | sec-ch-ua                   | Connection                |
| 3   | sec-ch-ua-mobile            | Content-Length            |
| 4   | sec-ch-ua-full-version      | Authorization             |
| 5   | sec-ch-ua-arch              | Content-Type              |
| 6   | sec-ch-ua-platform          | Cache-Control             |
| 7   | sec-ch-ua-platform-version  | sec-ch-ua                 |
| 8   | sec-ch-ua-model             | sec-ch-ua-mobile          |
| 9   | sec-ch-ua-bitness           | sec-ch-ua-platform        |
| 10  | sec-ch-ua-full-version-list | Upgrade-Insecure-Requests |
| 11  | upgrade-insecure-requests   | User-Agent                |
| 12  | user-agent                  | Accept                    |
| 13  | accept                      | Sec-Fetch-Site            |
| 14  | origin                      | Sec-Fetch-Mode            |
| 15  | sec-fetch-site              | Sec-Fetch-User            |
| 16  | sec-fetch-mode              | Sec-Fetch-Dest            |
| 17  | sec-fetch-user              | Sec-Fetch-Storage-Access  |
| 18  | sec-fetch-dest              | Referer                   |
| 19  | sec-fetch-storage-access    | Accept-Encoding           |
| 20  | referer                     | Accept-Language           |
| 21  | accept-encoding             | Cookie                    |
| 22  | accept-language             | Origin                    |
| 23  | cookie                      |                           |
| 24  | priority                    |                           |
|     | //unknown                   |                           |
| 25  | content-encoding            |                           |
| 26  | content-type                |                           |
| 27  | authorization               |                           |
| 28  | content-type                |                           |

### Usage examples:

* go Example

```go
package main

import (
	"fmt"
	"reqrio-go/reqrio"
)

func main() {
	session := reqrio.NewSession()
	e1 := session.SetAlpn(reqrio.HTTP20)
	if e1 != nil {
		println(e1.Error())
	}
	headers := `{
	"User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36 Edg/120.0.0.0",
	"Accept": "*/*",
	"Sec-Fetch-Site": "none",
	"Sec-Fetch-Mode": "navigate",
	"Sec-Fetch-Dest": "document",
	"sec-fetch-user": "?1",
	"upgrade-insecure-requests": "1",
	"sec-ch-ua": "\"Not_A Brand\";v=\"8\", \"Chromium\";v=\"120\", \"Microsoft Edge\";v=\"120\"",
	"sec-ch-ua-mobile": "?0",
	"sec-ch-ua-platform": "\"Windows\"",
	"Accept-Language": "zh-CN,zh;q=0.9",
	"Accept-Encoding": "gzip,deflate,br,zstd",
	"Cache-Control": "no-cache",
	"Connection": "keep-alive"
}`
	err1 := session.SetHeaderJson(headers)
	if err1 != nil {
		println(err1.Error())
	}
	err := session.SetUrl("https://m.so.com")
	if err != nil {
		println(err.Error())
		return
	}
	resp, err := session.Get()
	if err != nil {
		println(err.Error())
		return
	}
	//println(resp.Text())
	fmt.Printf("%#v\n", resp.Header())
	session.Close()
}

```