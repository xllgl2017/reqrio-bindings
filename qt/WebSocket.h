//
// Created by XLX on 2026/2/13.
//

#ifndef REQRIO_QT_WEBSOCKET_H
#define REQRIO_QT_WEBSOCKET_H
#include <QJsonObject>
#include <qstring.h>

#include "bindings.h"


/// WebSocket client wrapper for opening and sending frames.
class WebSocket {
    bindings::WsBuilder *builder = nullptr;
    bindings::WS_SOCKET *ws = nullptr;

public:
    /// Create a WebSocket client.
    /// @param url: WebSocket URL.
    explicit WebSocket(const QString &url);

    ~WebSocket();

    /// Add a handshake header.
    /// @param name: header name.
    /// @param value: header value.
    void addHeader(const QString &name, const QString &value) const;

    /// Set the proxy URL.
    /// @param proxy: proxy address.
    void setProxy(const QString &proxy) const;

    /// Set the WebSocket URL.
    /// @param url: target WebSocket URL.
    void setUrl(const QString &url) const;

    /// Set the WebSocket URI path.
    /// @param uri: endpoint path.
    void setUri(const QString &uri) const;

    /// Open the connection.
    void open();

    /// Open a raw WebSocket connection.
    /// @param url: WebSocket URL.
    /// @param raw: raw handshake payload.
    void openRaw(const QString &url, const QString &raw);

    /// Read one incoming WebSocket frame.
    QJsonObject read() const;

    /// Send a WebSocket frame.
    /// @param opcode: frame opcode.
    /// @param mask: whether to mask the payload.
    /// @param msg: message text.
    void write(int opcode, bool mask, const QString &msg) const;
};


#endif //REQRIO_QT_WEBSOCKET_H
