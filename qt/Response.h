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

public:
    explicit Response(bindings::Response *ptr, QObject *parent = nullptr);

    ~Response() override;

    [[nodiscard]] int statusCode() const;

    [[nodiscard]] QByteArray bytes() const;

    [[nodiscard]] QString text() const;

    [[nodiscard]] QJsonDocument json() const;

    [[nodiscard]] QString getHeader(const QString &name) const;

    [[nodiscard]] QList<Cookie> cookies() const;
};


#endif //REQRIO_RESPONSE_H
