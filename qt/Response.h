//
// Created by XLX on 2026/1/1.
//

#ifndef REQRIO_RESPONSE_H
#define REQRIO_RESPONSE_H

#include "bindings.h"
#include "Cookie.h"
#include "buffer.h"

typedef struct ScReq ScReq;

extern "C" {
///=========================>[Response]<=====================
struct RespInner {
    uint64_t sid;
    Method method;
    uint16_t status;
    Buf alpn;
    Buffer body;
};

uint16_t Response_status_code(const RespInner *response, char **err);

uint8_t *Response_bytes(RespInner *response, size_t *len, char **err);

char *Response_get_header(const RespInner *response, const char *name, char **err);

char *Response_cookies(const RespInner *response, char **err);

uint64_t Response_sid(const RespInner *response, char **err);

void Response_drop(RespInner *RespInner);

const uint8_t *ScReq_recv_stream(ScReq *req, uint64_t sid, size_t *len, char **err);
}


class Response : QObject {
    Q_OBJECT

    RespInner *raw_ptr;
    ScReq *req_ptr;
    bool read_stream = false;

public:
    explicit Response(RespInner *ptr, ScReq *req, QObject *parent = nullptr);

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
        ScReq *req;
        const uint8_t *ptr = {};
        size_t size = 0;
        bool hasNext;

    public:
        ChunkIterator(ScReq *req, uint64_t sid, bool hasNext);

        QByteArray operator*() const;

        ChunkIterator &operator++();

        bool operator!=(const ChunkIterator &other) const {
            return this->hasNext != other.hasNext;
        }
    };

    class ChunkRange {
        ScReq *req_ptr;
        uint64_t sid;

    public:
        ChunkRange(ScReq *req, uint64_t sid) {
            this->req_ptr = req;
            this->sid = sid;
        }

        [[nodiscard]] ChunkIterator begin() const {
            return {this->req_ptr, this->sid, true};
        }

        [[nodiscard]] ChunkIterator end() const {
            return {this->req_ptr, this->sid, false};
        }
    };

    [[nodiscard]] ChunkRange chunks();
};


#endif //REQRIO_RESPONSE_H
