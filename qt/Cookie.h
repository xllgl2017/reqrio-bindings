//
// Created by XLX on 2026/1/21.
//

#ifndef REQRIO_QT_COOKIE_H
#define REQRIO_QT_COOKIE_H
#include <QJsonObject>
#include <qstring.h>


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
    explicit Cookie();

    explicit Cookie(const QJsonObject& cookie);

    explicit Cookie(QString name, QString value);

    QString getName();

    QString getValue();
};


#endif //REQRIO_QT_COOKIE_H
