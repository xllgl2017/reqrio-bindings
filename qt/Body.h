//
// Created by XLX on 2026/5/12.
//

#ifndef REQRIO_QT_BODY_H
#define REQRIO_QT_BODY_H
#include <qobject.h>

#include "bindings.h"


class Body : QObject {
    Q_OBJECT

    bindings::Body *raw_ptr;

private:
    explicit Body(bindings::Body *raw_ptr);

public:
    explicit Body(QObject *parent = nullptr);

    Body(const QByteArray &bytes, const QString &contentType, QObject *parent = nullptr);

    explicit Body(const QString &text, QObject *parent = nullptr);

    explicit Body(const QJsonDocument &json, QObject *parent = nullptr);

    explicit Body(const QMap<QString, QString> &forms, QObject *parent = nullptr);

    bindings::Body *take();

    static Body *fromFiles(const QList<QString>& paths, const QJsonDocument &data);
};


#endif //REQRIO_QT_BODY_H
