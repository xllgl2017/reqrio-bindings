//
// Created by XLX on 2026/2/13.
//

#include "WebSocket.h"

#include <QJsonDocument>

WebSocket::WebSocket(const QString &url) {
    this->builder = bindings::ws_build();
    this->setUrl(url);
}


WebSocket::~WebSocket() {
    if (this->ws == nullptr)return;
    bindings::ws_close(this->ws);
}

void WebSocket::setUrl(const QString &url) const {
    bindings::ws_set_url(this->builder, url.toUtf8());
}

void WebSocket::setUri(const QString &uri) const {
    bindings::ws_set_uri(this->builder, uri.toUtf8());
}

void WebSocket::addHeader(const QString &name, const QString &value) const {
    bindings::ws_add_header(this->builder, name.toUtf8(), value.toUtf8());
}

void WebSocket::setProxy(const QString &proxy) const {
    bindings::ws_set_proxy(this->builder, proxy.toUtf8());
}

void WebSocket::open() {
    this->ws = bindings::ws_open(this->builder);
    if (this->ws == nullptr) throw std::runtime_error("Cannot open websocket connection");
}

void WebSocket::openRaw(const QString &url, const QString &raw) {
    this->ws = bindings::ws_open_raw(url.toUtf8(), raw.toUtf8());
}

QJsonObject WebSocket::read() const {
    char *ptr = bindings::ws_read(this->ws);
    QByteArray ba = QByteArray::fromStdString(ptr);
    QJsonObject obj = QJsonDocument::fromJson(ba).object();
    bindings::char_free(ptr);
    return obj;
}


void WebSocket::write(int opcode, bool mask, const QString &msg) const {
    bindings::ws_write(this->ws, opcode, mask, msg.toUtf8());
}
