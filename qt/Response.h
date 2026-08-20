//
// Created by XLX on 2026/1/1.
//

#ifndef REQRIO_RESPONSE_H
#define REQRIO_RESPONSE_H

#include "bindings.h"
#include "Cookie.h"


class Response : QObject {
    Q_OBJECT

    bindings::Response *raw_ptr;
    bindings::ScReq *req_ptr;

public:
    explicit Response(bindings::Response *ptr, bindings::ScReq *req, QObject *parent = nullptr);

    ~Response() override;

    [[nodiscard]] int statusCode() const;

    [[nodiscard]] QByteArray bytes() const;

    [[nodiscard]] QString text() const;

    [[nodiscard]] QJsonDocument json() const;

    [[nodiscard]] QString getHeader(const QString &name) const;

    [[nodiscard]] QList<Cookie> cookies() const;

    class ChunkIterator {
    public:
        using iterator_category = std::forward_iterator_tag;
        using value_type = QByteArray;
        using difference_type = std::ptrdiff_t;
        using pointer = const QByteArray *;
        using reference = const QByteArray &;

    private:
        uint64_t sid;
        bindings::ScReq *req;
        const uint8_t *ptr = {};
        size_t size = 0;
        bool hasNext;

    public:
        ChunkIterator(bindings::ScReq *req, uint64_t sid, bool hasNext);

        QByteArray operator*() const;

        ChunkIterator &operator++();

        bool operator!=(const ChunkIterator &other) const {
            return this->hasNext != other.hasNext;
        }
    };

    class ChunkRange {
        bindings::ScReq *req_ptr;
        uint64_t sid;

    public:
        ChunkRange(bindings::ScReq *req, uint64_t sid) {
            this->req_ptr = req;
            this->sid = sid;
        }

        ChunkIterator begin() const {
            return ChunkIterator(this->req_ptr, this->sid, true);
        }

        ChunkIterator end() const {
            return ChunkIterator(this->req_ptr, this->sid, false);
        }
    };

    ChunkRange chunks() const;
};


#endif //REQRIO_RESPONSE_H
