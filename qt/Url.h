//
// Created by XLX on 2026/5/12.
//

#ifndef REQRIO_QT_URL_H
#define REQRIO_QT_URL_H
#include <qobject.h>

extern "C" {
///=========================>[Url]<=====================
struct UrlInner;

UrlInner *Url_new(const char *url, char **err);

char *Url_add_param(UrlInner *url, const char *name, const char *value);

char *Url_remove_param(UrlInner *url, const char *name);

char *Url_set_sni(UrlInner *url, const char *sni);

void Url_drop(UrlInner *url);
}

class Url : QObject {
    Q_OBJECT

    UrlInner *raw_ptr;

public:
    explicit Url(const QString &url, QObject *parent = nullptr);

    explicit Url(const QString &url, const QString &sni, QObject *parent = nullptr);

    void addParam(const QString &name, const QString &value) const;

    void removeParam(const QString &name) const;

    UrlInner *take();

    ~Url() override;
};


#endif //REQRIO_QT_URL_H
