//
// Created by XLX on 2026/5/12.
//

#include "Url.h"

#include "util.h"

Url::Url(const QString &url, const QString &sni, QObject *parent) : Url(url, parent) {
    bindings::Url_set_sni(this->raw_ptr, sni.toUtf8());
}

Url::Url(const QString &url, QObject *parent) : QObject(parent) {
    char *err = nullptr;
    this->raw_ptr = bindings::Url_new(url.toUtf8(), &err);
    util::check_err(err);
}

void Url::addParam(const QString &name, const QString &value) const {
    char *err = bindings::Url_add_param(this->raw_ptr, name.toUtf8(), value.toUtf8());
    util::check_err(err);
}

void Url::removeParam(const QString &name) const {
    char *err = bindings::Url_remove_param(this->raw_ptr, name.toUtf8());
    util::check_err(err);
}

bindings::Url *Url::take() {
    bindings::Url *raw = this->raw_ptr;
    this->raw_ptr = nullptr;
    return raw;
}

Url::~Url() {
    if (this->raw_ptr == nullptr)return;
    bindings::Url_drop(this->raw_ptr);
    this->raw_ptr = nullptr;
}
