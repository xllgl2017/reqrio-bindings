//
// Created by XLX on 2026/1/1.
//

#ifndef UNTITLED_REQRIO_H
#define UNTITLED_REQRIO_H

#include "Response.h"
#include "Timeout.h"
#include "bindings.h"
#include "Body.h"
#include "Fingerprint.h"
#include "Url.h"

using namespace std;


class Session : QObject {
    Q_OBJECT
    bindings::ScReq *req = nullptr;

public:
    explicit Session(bool ignore_hdr_sort = false, QObject *parent = nullptr);

    ~Session() override;

    /// 构造session
    ///@param alpn :HTTP协议版本。在使用指纹时，指纹中的ALPN会把这个参数覆盖
    ///@param verify :是否对服务器证书链进行验证
    ///@param auto_redirect :是否对重定向连接进行自动跳转
    ///@param parent
    ///@param ignore_hdr_sort: 忽略内置的请求头排序
    explicit Session(ALPN alpn, bool verify = true, bool auto_redirect = true, bool ignore_hdr_sort = false,
                     QObject *parent = nullptr);

    /// 设置请求头
    ///@param header :请求头，JSON格式。
    void setHeader(const QJsonDocument &header) const;

    /// 添加一个请求头，已存在的请求头将会被覆盖
    ///@param name:参数名
    ///@param value:值，应为未编码
    ///@param reversed: 为true时，值为空会发送，false时值为空不发送
    void addHeader(const QString &name, const QString &value, bool reversed = false) const;

    /// 设置请求的版本
    void setAlpn(ALPN alpn) const;

    /// 设置是否对服务器证书链进行验证
    void setVerify(bool verify) const;

    ///设置对重定向连接是否进制自动跳转
    void setRedirect(bool auto_redirect) const;

    ///设置TLS密钥导出的位置，优先于环境变量SSLKEYLOGFILE
    ///@param key_log:导出路径
    void setKeyLog(const QString &key_log) const;

    /// 设置代理，示例:
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

    // void setCallback(bindings::Callback callback) const;

    /// 发送HTTP请求
    /// @param method:请求的方法
    /// @param url:请求的Url
    /// @param body:请求的Body
    [[nodiscard]] Response send(Method method, Url *url, Body *body) const;

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
