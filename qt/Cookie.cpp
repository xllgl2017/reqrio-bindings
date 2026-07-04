//
// Created by XLX on 2026/1/21.
//

#include "Cookie.h"

Cookie::Cookie() = default;

Cookie::Cookie(QString name, QString value) {
    this->name = std::move(name);
    this->value = std::move(value);
}

Cookie::Cookie(const QJsonObject& cookie) {
    this->name = cookie.value("name").toString();
    this->value = cookie.value("value").toString();
    this->age = cookie.value("age").toInt();
    this->domain = cookie.value("domain").toString();
    this->path = cookie.value("path").toString();
    this->httpOnly = cookie.value("http_only").toBool();
    this->secure = cookie.value("secure").toBool();
    this->expires = cookie.value("expires").toString();
    this->sameSite = cookie.value("same_site").toString();
    this->icpsp = cookie.value("icpsp").toBool();
}

QString Cookie::getName() {
    return this->name;
}

QString Cookie::getValue() {
    return this->value;
}
