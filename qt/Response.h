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


/// HTTP response wrapper returned by a session request.
class Response : QObject {
    Q_OBJECT

    RespInner *raw_ptr;
    ScReq *req_ptr;
    bool read_stream = false;

public:
    /// Create a response wrapper from native data.
    /// @param ptr: native response instance.
    /// @param req: owning request session.
    /// @param parent: parent QObject.
    explicit Response(RespInner *ptr, ScReq *req, QObject *parent = nullptr);

    ~Response() override;

    /// Return the HTTP status code.
    [[nodiscard]] int statusCode() const;

    /// Return the raw response body bytes.
    [[nodiscard]] QByteArray bytes() const;

    /// Return the response body as UTF-8 text.
    [[nodiscard]] QString text() const;

    /// Parse the response body as JSON.
    [[nodiscard]] QJsonDocument json() const;

    /// Read a response header by name.
    /// @param name: header name.
    [[nodiscard]] QString getHeader(const QString &name) const;

    /// Return cookies contained in the response.
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

    /// Return a streaming chunk iterator for the response body.
    [[nodiscard]] ChunkRange chunks();
};


#endif //REQRIO_RESPONSE_H
