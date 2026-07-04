//
// Created by XLX on 2026/5/12.
//

#ifndef REQRIO_QT_UNTIL_H
#define REQRIO_QT_UNTIL_H
#include <QString>
#include <stdexcept>
#include <QMap>

#include "bindings.h"

#endif //REQRIO_QT_UNTIL_H

namespace util {
    inline void check_err(char *err) {
        if (err == nullptr) { return; }
        const QString msg = QString::fromUtf8(err);
        bindings::char_free(err);
        throw std::runtime_error(msg.toStdString());
    }

    inline QByteArray encode_url_form(const QMap<QString, QString> &forms) {
        QString res;
        for (auto it = forms.cbegin(); it != forms.cend(); ++it) {
            res.append(it.key());
            res.append("=");
            char *value = bindings::url_encode(it.value().toUtf8());
            res.append(QString::fromUtf8(value));
            bindings::char_free(value);
            res.append("&");
        }
        if (res.endsWith("&"))
            res.remove(res.lastIndexOf('&'));
        return res.toUtf8();
    }

    QString alpn_str(ALPN alpn);
}
