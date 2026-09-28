//
// Created by XLX on 2026/1/1.
//

#include "Response.h"

#include <QJsonArray>
#include <QJsonDocument>
#include <QJsonObject>

#include "Body.h"
#include "util.h"


Response::Response(RespInner *ptr, ScReq *req, QObject *parent) : QObject(parent) {
    this->raw_ptr = ptr;
    this->req_ptr = req;
}

int Response::statusCode() const {
    return this->raw_ptr->status;
}

QByteArray Response::bytes() const {
    const size_t size = this->raw_ptr->body.end - this->raw_ptr->body.start;
    const uint8_t *ptr = this->raw_ptr->body.ptr + this->raw_ptr->body.start;
    return QByteArray::fromRawData(reinterpret_cast<const char *>(ptr), static_cast<int>(size));
}

QString Response::text() const {
    const QByteArray bytes = this->bytes();
    return QString::fromUtf8(bytes);
}

QJsonDocument Response::json() const {
    const QByteArray bytes = this->bytes();
    return QJsonDocument::fromJson(bytes);
}

QString Response::getHeader(const QString &name) const {
    char *err = nullptr;
    char *value = Response_get_header(this->raw_ptr, name.toUtf8(), &err);
    util::check_err(err);
    QString qvalue = QString::fromUtf8(value);
    bindings::char_free(value);
    return qvalue;
}

QList<Cookie> Response::cookies() const {
    char *err = nullptr;
    char *cookies_ptr = Response_cookies(this->raw_ptr, &err);
    util::check_err(err);
    const QString cookie = QString::fromUtf8(cookies_ptr);
    bindings::char_free(cookies_ptr);
    QJsonArray cookies = QJsonDocument::fromJson(cookie.toUtf8()).array();
    QList<Cookie> result;
    for (QJsonValueRef ck: cookies) {
        result.append(Cookie(ck.toObject()));
    }
    return result;
}


Response::ChunkRange Response::chunks() {
    this->read_stream = true;
    char *err = nullptr;
    uint64_t sid = Response_sid(raw_ptr, &err);
    util::check_err(err);
    return {this->req_ptr, sid};
}

Response::ChunkIterator::ChunkIterator(ScReq *req, const uint64_t sid, const bool hasNext) {
    this->req = req;
    this->sid = sid;
    this->hasNext = hasNext;
    if (this->hasNext) {
        char *err = nullptr;
        this->ptr = ScReq_recv_stream(this->req, this->sid, &this->size, &err);
        util::check_err(err);
        if (this->ptr == nullptr)this->hasNext = false;
    }
}

QByteArray Response::ChunkIterator::operator*() const {
    return QByteArray::fromRawData(reinterpret_cast<const char *>(ptr), static_cast<int>(this->size));
}

Response::ChunkIterator &Response::ChunkIterator::operator++() {
    char *err = nullptr;
    this->ptr = ScReq_recv_stream(this->req, this->sid, &this->size, &err);
    util::check_err(err);
    if (this->ptr == nullptr)this->hasNext = false;

    return *this;
}

Response::~Response() {
    if (this->raw_ptr == nullptr) { return; }
    if (!this->read_stream)Response_drop(this->raw_ptr);
    this->raw_ptr = nullptr;
}
