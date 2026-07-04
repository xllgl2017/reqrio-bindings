//
// Created by XLX on 2026/1/1.
//

#include "Response.h"

#include <qdebug.h>
#include <QJsonArray>
#include <QJsonDocument>
#include <QJsonObject>

#include "util.h"


Response::Response(bindings::Response *ptr, QObject *parent) : QObject(parent) {
    this->raw_ptr = ptr;
}

int Response::statusCode() const {
    char *err = nullptr;
    const int code = bindings::Response_status_code(this->raw_ptr, &err);
    util::check_err(err);
    return code;
}

QByteArray Response::bytes() const {
    char *err = nullptr;
    size_t len = 0;
    uint8_t *ptr = bindings::Response_bytes(this->raw_ptr, &len, &err);
    util::check_err(err);
    QByteArray bytes = QByteArray::fromRawData(reinterpret_cast<char *>(ptr), static_cast<int>(len));
    return bytes;
}

QString Response::text() const {
    QByteArray bytes = this->bytes();
    return QString::fromUtf8(bytes);
}

QJsonDocument Response::json() const {
    QByteArray bytes = this->bytes();
    return QJsonDocument::fromJson(bytes);
}

QString Response::getHeader(const QString &name) const {
    char *err = nullptr;
    char *value = bindings::Response_get_header(this->raw_ptr, name.toUtf8(), &err);
    util::check_err(err);
    QString qvalue = QString::fromUtf8(value);
    bindings::char_free(value);
    return qvalue;
}

QList<Cookie> Response::cookies() const {
    char *err = nullptr;
    char *cookies_ptr = bindings::Response_cookies(this->raw_ptr, &err);
    util::check_err(err);
    QString cookie = QString::fromUtf8(cookies_ptr);
    bindings::char_free(cookies_ptr);
    QJsonArray cookies = QJsonDocument::fromJson(cookie.toUtf8()).array();
    QList<Cookie> result;
    for (QJsonValueRef ck: cookies) {
        result.append(Cookie(ck.toObject()));
    }
    return result;
}


Response::~Response() {
    if (this->raw_ptr == nullptr) { return; }
    bindings::Response_drop(this->raw_ptr);
    this->raw_ptr = nullptr;
}
