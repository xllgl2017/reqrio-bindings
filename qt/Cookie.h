//
// Created by XLX on 2026/1/21.
//

#ifndef REQRIO_QT_COOKIE_H
#define REQRIO_QT_COOKIE_H
#include <QJsonObject>
#include <qstring.h>


/// Parsed cookie value used in response headers.
class Cookie {
    QString name;
    QString value;
    int age = 0;
    QString domain;
    QString path;
    bool httpOnly = false;
    bool secure = false;
    QString expires;
    QString sameSite;
    bool icpsp = false;

public:
    /// Create an empty cookie.
    explicit Cookie();

    /// Create a cookie from a JSON object.
    /// @param cookie: cookie data.
    explicit Cookie(const QJsonObject& cookie);

    /// Create a cookie with a name and value.
    /// @param name: cookie name.
    /// @param value: cookie value.
    explicit Cookie(QString name, QString value);

    /// Return the cookie name.
    QString getName();

    /// Return the cookie value.
    QString getValue();
};


#endif //REQRIO_QT_COOKIE_H
