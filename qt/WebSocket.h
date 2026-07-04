//
// Created by XLX on 2026/2/13.
//

#ifndef REQRIO_QT_WEBSOCKET_H
#define REQRIO_QT_WEBSOCKET_H
#include <QJsonObject>
#include <qstring.h>

#include "bindings.h"


class WebSocket {
    bindings::WsBuilder *builder = nullptr;
    bindings::WS_SOCKET *ws = nullptr;

public:
    explicit WebSocket(const QString &url);

    ~WebSocket();

    void addHeader(const QString &name, const QString &value) const;

    void setProxy(const QString &proxy) const;

    void setUrl(const QString &url) const;

    void setUri(const QString &uri) const;

    void open();

    void openRaw(const QString &url, const QString &raw);

    QJsonObject read() const;

    void write(int opcode, bool mask, const QString &msg) const;
};


#endif //REQRIO_QT_WEBSOCKET_H
