//
// Created by XLX on 2026/1/1.
//

#ifndef UNTITLED_REQRIO_H
#define UNTITLED_REQRIO_H

#include "Timeout.h"
#include "bindings.h"
#include "Body.h"
#include "Fingerprint.h"
#include "Response.h"
#include "Url.h"

using namespace std;

extern "C" {
///=========================>[ScReq]<=====================
struct ScReq;

ScReq *ScReq_new(bool ignore_hdr_sort);

char *ScReq_set_header_json(ScReq *req, const char *header);

char *ScReq_add_header(ScReq *req, const char *key, const char *value, bool reversed);

char *ScReq_remove_header(ScReq *req, const char *key);

char *ScReq_set_alpn(ScReq *req, const char *alpn);

char *ScReq_set_verify(ScReq *req, bool verify);

char *ScReq_set_redirect(ScReq *req, bool redirect);

char *ScReq_set_key_log(ScReq *req, const char *key_log);

char *ScReq_set_fingerprint(ScReq *req, FingerInner *fingerprint);

char *ScReq_set_version(ScReq *req, Version version);

char *ScReq_set_proxy(ScReq *req, const char *proxy);

char *ScReq_set_timeout(ScReq *req, const char *timeout);

char *ScReq_set_cookie(ScReq *req, const char *cookie);

char *ScReq_add_cookie(ScReq *req, const char *name, const char *value);

uint64_t ScReq_send(ScReq *req, Method method, UrlInner *url, bindings::Body *body, char **err);

RespInner *ScReq_do_http(ScReq *req, Method method, UrlInner *url, bindings::Body *body, char **err);

RespInner *ScReq_recv(ScReq *req, uint64_t sid, bool stream, char **err);

char *ScReq_reconnect(ScReq *req);

char *ScReq_connect(ScReq *req, const char *url, const char *sni);

char *ScReq_close_stream(ScReq *req);

void ScReq_drop(ScReq *req);
}

typedef QString ALPN;

static ALPN HTTP11 = "http/1.1";
static ALPN HTTP20 = "h2";
static ALPN HTTP30 = "h3";

/// Reusable HTTP client session for sending requests and handling responses.
class Session : QObject {
    Q_OBJECT
    ScReq *req = nullptr;

    Response request(Method method, Url *url, Body *body) const;

public:
    /// Create a session with default settings.
    /// @param ignore_hdr_sort: ignore the built-in request header ordering.
    explicit Session(bool ignore_hdr_sort = false, QObject *parent = nullptr);

    ~Session() override;

    /// Create a session with explicit request configuration.
    /// @param alpn: HTTP protocol version; fingerprint ALPN overrides this when set.
    /// @param verify: whether to validate the server certificate chain.
    /// @param auto_redirect: whether to follow redirect responses automatically.
    /// @param ignore_hdr_sort: ignore the built-in request header ordering.
    /// @param parent: parent QObject.
    explicit Session(const ALPN &alpn, bool verify = true, bool auto_redirect = true, bool ignore_hdr_sort = false,
                     QObject *parent = nullptr);

    /// Replace all request headers with a JSON object.
    /// @param header: header map in JSON format.
    void setHeader(const QJsonDocument &header) const;

    /// Add a single request header; existing values are overwritten.
    /// @param name: header name.
    /// @param value: header value.
    /// @param reversed: whether to keep the header order reversed.
    void addHeader(const QString &name, const QString &value, bool reversed = false) const;

    /// Set the ALPN value for the session.
    /// @param alpn: negotiated protocol.
    void setAlpn(const ALPN &alpn) const;

    /// Enable or disable certificate verification.
    /// @param verify: certificate validation flag.
    void setVerify(bool verify) const;

    /// Enable or disable automatic redirect following.
    /// @param auto_redirect: redirect behavior.
    void setRedirect(bool auto_redirect) const;

    /// Set the TLS key log export path.
    /// @param key_log: key log file path.
    void setKeyLog(const QString &key_log) const;

    /// Set the TLS handshake version.
    /// @param version: TLS version.
    void setVersion(Version version) const;

    /// Set the HTTP proxy URL.
    /// @param proxy: proxy address such as http://127.0.0.1:1025.
    void setProxy(const QString &proxy) const;

    /// Set connection and read/write timeout limits.
    /// @param timeout: timeout configuration object.
    void setTimeout(const Timeout &timeout) const;

    /// Set the Cookie header string.
    /// @param cookie: cookie string.
    void setCookie(const QString &cookie) const;

    /// Add a single cookie.
    /// @param name: cookie name.
    /// @param value: cookie value.
    void addCookie(const QString &name, const QString &value) const;

    /// Set the TLS fingerprint used by this session.
    /// @param fingerprint: fingerprint instance.
    void setFingerprint(Fingerprint *fingerprint) const;

    /// Open a TCP/TLS connection to the given URL.
    /// @param url: target URL.
    /// @param sni: custom SNI hostname.
    void connect(const QString &url, const QString &sni = "") const;

    /// Reconnect the current session.
    void reconnect() const;

    /// Close the active stream.
    void close_stream() const;

    /// Send an HTTP request and return the request id.
    /// @param method: HTTP method.
    /// @param url: request URL object.
    /// @param body: request body.
    [[nodiscard]] uint64_t send(Method method, Url *url, Body *body) const;

    /// Receive a response by request id.
    /// @param sid: request id.
    /// @param stream: whether to read as a streaming response.
    [[nodiscard]] Response recv(uint64_t sid, bool stream = false) const;

    /// Send a GET request.
    /// @param url: request URL.
    /// @param body: request body.
    [[nodiscard]] Response get(Url *, Body *) const;

    /// Send a GET request.
    /// @param url: request URL string.
    /// @param body: request body.
    [[nodiscard]] Response get(const QString &, Body *) const;

    /// Send a GET request without a body.
    /// @param url: request URL string.
    [[nodiscard]] Response get(const QString &) const;

    /// Send a POST request.
    /// @param url: request URL.
    /// @param body: request body.
    [[nodiscard]] Response post(Url *, Body *) const;

    /// Send a POST request with a raw body.
    /// @param url: request URL string.
    /// @param body: request body.
    [[nodiscard]] Response post(const QString &, Body *) const;

    /// Send a POST request with a JSON body.
    /// @param url: request URL string.
    /// @param body: JSON payload.
    [[nodiscard]] Response post(const QString &, const QJsonDocument &) const;

    /// Send a POST request with form fields.
    /// @param url: request URL string.
    /// @param forms: form name/value pairs.
    [[nodiscard]] Response post(const QString &, const QMap<QString, QString> &) const;

    /// Send a PUT request.
    /// @param url: request URL.
    /// @param body: request body.
    [[nodiscard]] Response put(Url *, Body *) const;

    /// Send an OPTIONS request.
    /// @param url: request URL.
    /// @param body: request body.
    [[nodiscard]] Response options(Url *, Body *) const;

    /// Send a HEAD request.
    /// @param url: request URL.
    /// @param body: request body.
    [[nodiscard]] Response head(Url *, Body *) const;

    /// Send a DELETE request.
    /// @param url: request URL.
    /// @param body: request body.
    [[nodiscard]] Response delete_(Url *, Body *) const;

    /// Send a TRACE request.
    /// @param url: request URL.
    /// @param body: request body.
    [[nodiscard]] Response trace(Url *, Body *) const;

    /// Send a PATCH request.
    /// @param url: request URL.
    /// @param body: request body.
    [[nodiscard]] Response patch(Url *, Body *) const;

    /// Send a custom QUERY request.
    /// @param url: request URL.
    /// @param body: request body.
    [[nodiscard]] Response query(Url *, Body *) const;
};


#endif //UNTITLED_REQRIO_H
