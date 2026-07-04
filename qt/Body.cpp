//
// Created by XLX on 2026/5/12.
//

#include "Body.h"
#include <QFile>


#include <QJsonDocument>

#include "util.h"

Body::Body(bindings::Body *raw_ptr) {
    this->raw_ptr = raw_ptr;
}


Body::Body(QObject *parent) : QObject(parent) {
    this->raw_ptr = bindings::Body_none();
}

Body::Body(const QByteArray &bytes, const QString &contentType, QObject *parent) : QObject(parent) {
    char *err = nullptr;
    this->raw_ptr = bindings::Body_new(
        reinterpret_cast<const uint8_t *>(bytes.constData()),
        static_cast<size_t>(bytes.length()),
        contentType.toUtf8(), &err
    );
    util::check_err(err);
}


Body::Body(const QString &text, QObject *parent) : Body(text.toUtf8(), "text/plain", parent) {
}

Body::Body(const QJsonDocument &json, QObject *parent) : Body(json.toJson(), "application/json", parent) {
}

Body::Body(const QMap<QString, QString> &forms, QObject *parent)
    : Body(
        util::encode_url_form(forms),
        "application/x-www-form-urlencoded",
        parent) {
}

bindings::Body *Body::take() {
    bindings::Body *raw = this->raw_ptr;
    this->raw_ptr = nullptr;
    return raw;
}

Body *Body::fromFiles(const QList<QString>& paths, const QJsonDocument &data) {
    const auto http_file = bindings::HttpFile_new();
    for (const QString &path: paths) {
        QFile file(path);
        char *err = nullptr;
        const auto form = bindings::FileForm_new(path.toUtf8(), file.fileName().toUtf8(), nullptr, &err);
        try {
            util::check_err(err);
            bindings::HttpFile_add_form(http_file, form);
        } catch ([[maybe_unused]] const std::runtime_error &e) {
            bindings::HttpFile_drop(http_file);
            throw;
        }
    }
    char *err = nullptr;
    const auto raw = bindings::Body_new_files(http_file, data.toJson().data(), &err);
    return new Body(raw);
}
