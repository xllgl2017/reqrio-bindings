//
// Created by XLX on 2026/5/15.
//

#ifndef REQRIO_QT_FINGERPRINT_H
#define REQRIO_QT_FINGERPRINT_H

#include "bindings.h"
#include "QObject"
#include "util.h"

extern "C"{
    ///=========================>[Fingerprint]<=====================
    struct FingerInner;

    FingerInner *Fingerprint_from_ja3(const char *ja3, const char *token, char **err);

    FingerInner *Fingerprint_from_ja4(const char *ja4, const char *token, char **err);

    FingerInner *Fingerprint_from_client_hello(const uint8_t *u8, size_t len, const char *token, char **err);

    FingerInner *Fingerprint_random(const char *token, char **err);

    FingerInner *Fingerprint_custom(const char *custom, const char *token, char **err);

    FingerInner *Fingerprint_new(const char *token);

    void Fingerprint_add_cipher_suite(FingerInner *fingerprint, uint16_t suite);

    void Fingerprint_add_ext(FingerInner *fingerprint, uint16_t ext_typ);

    void Fingerprint_add_ext_alpn(FingerInner *fingerprint, uint16_t ext_typ, const char *alpn);

    void Fingerprint_add_ext_version(FingerInner *fingerprint, uint16_t ext_typ, uint16_t version);

    void Fingerprint_add_ext_curve(FingerInner *fingerprint, uint16_t ext_typ, uint16_t curve);

    void Fingerprint_add_ext_compress(FingerInner *fingerprint, uint16_t ext_typ, uint16_t compress);

    void Fingerprint_add_ext_psk_mode(FingerInner *fingerprint, uint16_t ext_typ, uint8_t mode);

    void Fingerprint_add_ext_padding(FingerInner *fingerprint, uint16_t ext_typ, size_t padding);

    void Fingerprint_add_ext_bytes(FingerInner *fingerprint, uint16_t ext_typ, const uint8_t *bytes, size_t len);

    void Fingerprint_add_ext_algorithm(FingerInner *fingerprint, uint16_t ext_typ, uint16_t algo);

    void Fingerprint_add_ext_ec_point(FingerInner *fingerprint, uint16_t ext_typ, uint8_t point);

    void Fingerprint_add_h2_setting(FingerInner *fingerprint, uint16_t flag, uint32_t value);

    void Fingerprint_set_h2_window_size(FingerInner *fingerprint, uint32_t size);

    void Fingerprint_set_h2_priority(FingerInner *fingerprint, bool priority, uint8_t weight);

    void Fingerprint_drop(FingerInner *fingerprint);}


class Fingerprint : QObject {
    Q_OBJECT

    FingerInner *raw_ptr;

public:
    explicit Fingerprint(FingerInner *, QObject *parent = nullptr);

    explicit Fingerprint(const QString &token, QObject *parent = nullptr);

    ~Fingerprint() override;

    FingerInner *take();

    void addCipherSuites(const QVector<uint16_t> &suites) const;

    void addCipherSuite(uint16_t suite) const;

    void addExtension(uint16_t typ) const;

    void addExtensionALPN(uint16_t typ, const QVector<QString> &alps) const;

    void addExtensionVersion(uint16_t typ, const QVector<uint16_t> &versions) const;

    void addExtensionGroup(uint16_t typ, const QVector<uint16_t> &groups) const;

    void addExtensionCompress(uint16_t typ, const QVector<uint16_t> &methods) const;

    void addExtensionEcPoint(uint16_t typ, const QVector<uint8_t> &points) const;

    void addExtensionAlgorithm(uint16_t typ, const QVector<uint16_t> &algorithms) const;

    void addExtension(uint16_t typ, const QByteArray &bytes) const;

    void addExtensionPadding(uint16_t typ, size_t padding) const;

    void addH2Setting(uint16_t flag, uint32_t value) const;

    void setH2WindowSize(uint32_t value) const;

    void setH2Priority(bool priority, uint8_t weight) const;

    static Fingerprint *fromJa3(const QString &ja3, const QString &token, QObject *parent = nullptr);

    static Fingerprint *fromJa4(const QString &ja4, const QString &token, QObject *parent = nullptr);

    static Fingerprint *fromClientHello(const QByteArray &bs, const QString &token, QObject *parent = nullptr);

    static Fingerprint *fromCustom(const QJsonObject &, const QString &token, QObject *parent = nullptr);

    static Fingerprint *random(const QString &token, QObject *parent = nullptr);
};


#endif //REQRIO_QT_FINGERPRINT_H
