//
// Created by XLX on 2026/5/12.
//

#ifndef REQRIO_QT_URL_H
#define REQRIO_QT_URL_H
#include <qobject.h>
#include "bindings.h"

class Url : QObject {
    Q_OBJECT

    bindings::Url *raw_ptr;

public:
    explicit Url(const QString &url, QObject *parent = nullptr);

    explicit Url(const QString &url, const QString &sni, QObject *parent = nullptr);

    void addParam(const QString &name, const QString &value) const;

    void removeParam(const QString &name) const;

    bindings::Url *take();

    ~Url() override;
};


#endif //REQRIO_QT_URL_H
