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

/// Request URL wrapper with query parameters and optional SNI.
class Url : QObject {
    Q_OBJECT

    UrlInner *raw_ptr;

public:
    /// Create a URL wrapper.
    /// @param url: target URL.
    /// @param parent: parent QObject.
    explicit Url(const QString &url, QObject *parent = nullptr);

    /// Create a URL wrapper with a custom SNI host.
    /// @param url: target URL.
    /// @param sni: custom SNI hostname.
    /// @param parent: parent QObject.
    explicit Url(const QString &url, const QString &sni, QObject *parent = nullptr);

    /// Add a query parameter.
    /// @param name: parameter name.
    /// @param value: parameter value.
    void addParam(const QString &name, const QString &value) const;

    /// Remove a query parameter by name.
    /// @param name: parameter name.
    void removeParam(const QString &name) const;

    /// Return and detach the native pointer.
    UrlInner *take();

    ~Url() override;
};


#endif //REQRIO_QT_URL_H
