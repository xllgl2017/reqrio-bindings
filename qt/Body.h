//
// Created by XLX on 2026/5/12.
//

#ifndef REQRIO_QT_BODY_H
#define REQRIO_QT_BODY_H
#include <qobject.h>

#include "bindings.h"


/// HTTP request body helper for raw, text, JSON, form, and file uploads.
class Body : QObject {
    Q_OBJECT

    bindings::Body *raw_ptr;

private:
    explicit Body(bindings::Body *raw_ptr);

public:
    /// Create an empty body.
    /// @param parent: parent QObject.
    explicit Body(QObject *parent = nullptr);

    /// Create a raw byte body.
    /// @param bytes: raw content bytes.
    /// @param contentType: content-type header.
    /// @param parent: parent QObject.
    Body(const QByteArray &bytes, const QString &contentType, QObject *parent = nullptr);

    /// Create a text body.
    /// @param text: request text.
    /// @param parent: parent QObject.
    explicit Body(const QString &text, QObject *parent = nullptr);

    /// Create a JSON body.
    /// @param json: JSON document.
    /// @param parent: parent QObject.
    explicit Body(const QJsonDocument &json, QObject *parent = nullptr);

    /// Create a form body.
    /// @param forms: form field map.
    /// @param parent: parent QObject.
    explicit Body(const QMap<QString, QString> &forms, QObject *parent = nullptr);

    /// Return and detach the native body pointer.
    bindings::Body *take();

    /// Build a multipart upload body from files.
    /// @param paths: file paths.
    /// @param data: additional form data.
    static Body *fromFiles(const QList<QString>& paths, const QJsonDocument &data);
};


#endif //REQRIO_QT_BODY_H
