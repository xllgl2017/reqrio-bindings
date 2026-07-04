//
// Created by XLX on 2026/5/15.
//

#ifndef REQRIO_QT_FINGERPRINT_H
#define REQRIO_QT_FINGERPRINT_H

#include "bindings.h"
#include "QObject"
#include "util.h"


class Fingerprint : QObject {
    Q_OBJECT

    bindings::Fingerprint *raw_ptr;

public:
    explicit Fingerprint(bindings::Fingerprint *, QObject *parent = nullptr);

    explicit Fingerprint(const QString &token, QObject *parent = nullptr);

    ~Fingerprint() override;

    bindings::Fingerprint *take();

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
