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


class Session : QObject {
    Q_OBJECT
    ScReq *req = nullptr;

public:
    explicit Session(bool ignore_hdr_sort = false, QObject *parent = nullptr);

    ~Session() override;

    /// Construct a session.
    ///@param alpn: HTTP protocol version. When a TLS fingerprint is used, the ALPN in the fingerprint overrides this parameter.
    ///@param verify: whether to validate the server certificate chain
    ///@param auto_redirect: whether to follow redirect responses automatically
    ///@param parent
    ///@param ignore_hdr_sort: ignore the built-in request header ordering
    explicit Session(const ALPN &alpn, bool verify = true, bool auto_redirect = true, bool ignore_hdr_sort = false,
                     QObject *parent = nullptr);

    /// Set request headers.
    ///@param header: request headers in JSON format.
    void setHeader(const QJsonDocument &header) const;

    /// Add a request header. If the header already exists, it will be overwritten.
    ///@param name: parameter name
    ///@param value: header value; should be unencoded
    ///@param reversed: if true, the header is sent even when the value is empty; if false, it is omitted when empty
    void addHeader(const QString &name, const QString &value, bool reversed = false) const;

    /// Set the request protocol version.
    void setAlpn(const ALPN &alpn) const;

    /// Set whether to validate the server certificate chain.
    void setVerify(bool verify) const;

    /// Set whether redirects should be followed automatically.
    void setRedirect(bool auto_redirect) const;

    /// Set the TLS key export path, taking precedence over the SSLKEYLOGFILE environment variable.
    ///@param key_log: export path
    void setKeyLog(const QString &key_log) const;

    /// Set the TLS handshake version.
    ///@param version: TLS version
    void setVersion(Version version) const;

    /// Set the proxy. Example:
    ///
    /// http://127.0.0.1:1025
    ///
    /// socks5://127.0.0.1:1025
    void setProxy(const QString &proxy) const;

    void setTimeout(const Timeout &timeout) const;

    void setCookie(const QString &cookie) const;

    void addCookie(const QString &name, const QString &value) const;

    void setFingerprint(Fingerprint *fingerprint) const;

    void connect(const QString &url, const QString &sni = "") const;

    void reconnect() const;

    void close_stream() const;

    /// Send an HTTP request.
    /// @param method: HTTP method
    /// @param url: request URL
    /// @param body: request body

    [[nodiscard]] uint64_t send(Method method, Url *url, Body *body) const;

    /// @param stream: whether to use a streaming request
    [[nodiscard]] Response recv(uint64_t sid, bool stream = false) const;

    [[nodiscard]] Response get(Url *, Body *) const;

    [[nodiscard]] Response get(const QString &, Body *) const;

    [[nodiscard]] Response get(const QString &) const;

    [[nodiscard]] Response post(Url *, Body *) const;

    [[nodiscard]] Response post(const QString &, Body *) const;

    [[nodiscard]] Response post(const QString &, const QJsonDocument &) const;

    [[nodiscard]] Response post(const QString &, const QMap<QString, QString> &) const;

    [[nodiscard]] Response put(Url *, Body *) const;

    [[nodiscard]] Response options(Url *, Body *) const;

    [[nodiscard]] Response head(Url *, Body *) const;

    [[nodiscard]] Response delete_(Url *, Body *) const;

    [[nodiscard]] Response trace(Url *, Body *) const;

    [[nodiscard]] Response patch(Url *, Body *) const;

    [[nodiscard]] Response query(Url *, Body *) const;
};


#endif //UNTITLED_REQRIO_H
