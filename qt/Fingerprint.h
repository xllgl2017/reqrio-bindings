//
// Created by XLX on 2026/5/15.
//

#ifndef REQRIO_QT_FINGERPRINT_H
#define REQRIO_QT_FINGERPRINT_H

#include "bindings.h"
#include "QObject"
#include "util.h"
#include "tls/Version.h"

extern "C" {
///=========================>[Fingerprint]<=====================
struct FingerInner;

FingerInner *Fingerprint_from_builtin(const char *name, bool sec_full, const char *token, char **err);

FingerInner *Fingerprint_from_idx(size_t idx, const char *auth, bool sec_full, char **err);

FingerInner *Fingerprint_from_ja3(const char *ja3, const char *token, char **err);

FingerInner *Fingerprint_from_ja4(const char *ja4, const char *token, char **err);

FingerInner *Fingerprint_from_client_hello(const uint8_t *u8, size_t len, const char *token, char **err);

FingerInner *Fingerprint_random(const char *token, char **err);

FingerInner *Fingerprint_custom(const char *custom, const char *token, char **err);

FingerInner *Fingerprint_new(Version rv, Version mv, const char *token);

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

void Fingerprint_clear_h2_frame(FingerInner *fingerprint);

void Fingerprint_add_h2_setting(FingerInner *fingerprint, uint16_t flag, uint32_t value);

void Fingerprint_set_h2_window_size(FingerInner *fingerprint, uint32_t size);

void Fingerprint_set_h2_priority(FingerInner *fingerprint, bool priority, uint8_t weight);

void Fingerprint_add_h2_frame(FingerInner *fingerprint, uint8_t typ, const uint8_t *data, size_t data_len);

void Fingerprint_clean_h3_frame(FingerInner *fingerprint);

void Fingerprint_add_h3_setting(FingerInner *fingerprint, uint64_t flag, uint64_t value);

void Fingerprint_add_h3_frame(FingerInner *fingerprint, uint64_t typ, const uint8_t *data, size_t data_len);

void Fingerprint_drop(FingerInner *fingerprint);
}


/// TLS fingerprint builder for JA3/JA4, ClientHello, and custom TLS profiles.
class Fingerprint : QObject {
    Q_OBJECT

    FingerInner *raw_ptr;

public:
    /// Create a fingerprint from a native pointer.
    /// @param raw: native fingerprint pointer.
    /// @param parent: parent QObject.
    explicit Fingerprint(FingerInner *, QObject *parent = nullptr);

    /// Create a fingerprint with explicit TLS version range.
    /// @param rv: record version.
    /// @param mv: message version.
    /// @param token: auth token.
    /// @param parent: parent QObject.
    explicit Fingerprint(Version rv, Version mv, const QString &token, QObject *parent = nullptr);

    ~Fingerprint() override;

    /// Return and detach the native pointer.
    FingerInner *take();

    /// Add one or more cipher suites.
    /// @param suites: cipher suite values.
    void addCipherSuites(const QVector<uint16_t> &suites) const;

    /// Add a cipher suite.
    /// @param suite: cipher suite value.
    void addCipherSuite(uint16_t suite) const;

    /// Add a generic TLS extension.
    /// @param typ: extension type.
    void addExtension(uint16_t typ) const;

    /// Add ALPN extension values.
    /// @param typ: extension type.
    /// @param alps: ALPN strings.
    void addExtensionALPN(uint16_t typ, const QVector<QString> &alps) const;

    /// Add supported TLS versions.
    /// @param typ: extension type.
    /// @param versions: version values.
    void addExtensionVersion(uint16_t typ, const QVector<uint16_t> &versions) const;

    /// Add named groups.
    /// @param typ: extension type.
    /// @param groups: curve values.
    void addExtensionGroup(uint16_t typ, const QVector<uint16_t> &groups) const;

    /// Add compression methods.
    /// @param typ: extension type.
    /// @param methods: compression values.
    void addExtensionCompress(uint16_t typ, const QVector<uint16_t> &methods) const;

    /// Add EC point formats.
    /// @param typ: extension type.
    /// @param points: point values.
    void addExtensionEcPoint(uint16_t typ, const QVector<uint8_t> &points) const;

    /// Add signature algorithms.
    /// @param typ: extension type.
    /// @param algorithms: algorithm values.
    void addExtensionAlgorithm(uint16_t typ, const QVector<uint16_t> &algorithms) const;

    /// Add raw extension bytes.
    /// @param typ: extension type.
    /// @param bytes: extension payload.
    void addExtension(uint16_t typ, const QByteArray &bytes) const;

    /// Add padding to an extension.
    /// @param typ: extension type.
    /// @param padding: padding length.
    void addExtensionPadding(uint16_t typ, size_t padding) const;

    /// Clear HTTP/2 frame entries.
    void clearH2Frame() const;

    /// Add an HTTP/2 setting.
    /// @param flag: setting key.
    /// @param value: setting value.
    void addH2Setting(uint16_t flag, uint32_t value) const;

    /// Set the HTTP/2 window size.
    /// @param value: window size.
    void setH2WindowSize(uint32_t value) const;

    /// Configure HTTP/2 priority.
    /// @param priority: whether priority is enabled.
    /// @param weight: stream weight.
    void setH2Priority(bool priority, uint8_t weight) const;

    /// Clear HTTP/3 frame entries.
    void clearH3Frame() const;

    /// Add an HTTP/3 setting.
    /// @param flag: setting key.
    /// @param value: setting value.
    void addH3Setting(uint64_t flag, uint64_t value) const;

    /// Add an HTTP/3 frame.
    /// @param typ: frame type.
    /// @param data: frame payload.
    void addH3Frame(uint64_t typ, QByteArray data) const;

    /// Build a built-in fingerprint by name.
    /// @param name: built-in fingerprint name.
    /// @param sec_full: use full security settings.
    /// @param token: auth token.
    /// @param parent: parent QObject.
    static Fingerprint *fromBuiltin(const QString &name, bool sec_full, const QString &token,
                                    QObject *parent = nullptr);

    /// Build a fingerprint by index.
    /// @param idx: fingerprint index.
    /// @param sec_full: use full security settings.
    /// @param auth: auth token.
    /// @param parent: parent QObject.
    static Fingerprint *fromIdx(size_t idx, bool sec_full, const QString &auth, QObject *parent = nullptr);

    /// Build a fingerprint from a JA3 string.
    /// @param ja3: JA3 value.
    /// @param token: auth token.
    /// @param parent: parent QObject.
    static Fingerprint *fromJa3(const QString &ja3, const QString &token, QObject *parent = nullptr);

    /// Build a fingerprint from a JA4 string.
    /// @param ja4: JA4 value.
    /// @param token: auth token.
    /// @param parent: parent QObject.
    static Fingerprint *fromJa4(const QString &ja4, const QString &token, QObject *parent = nullptr);

    /// Build a fingerprint from ClientHello bytes.
    /// @param bs: raw ClientHello payload.
    /// @param token: auth token.
    /// @param parent: parent QObject.
    static Fingerprint *fromClientHello(const QByteArray &bs, const QString &token, QObject *parent = nullptr);

    /// Build a custom fingerprint from a JSON object.
    /// @param config: fingerprint configuration.
    /// @param token: auth token.
    /// @param parent: parent QObject.
    static Fingerprint *fromCustom(const QJsonObject &, const QString &token, QObject *parent = nullptr);

    /// Build a random fingerprint.
    /// @param token: auth token.
    /// @param parent: parent QObject.
    static Fingerprint *random(const QString &token, QObject *parent = nullptr);
};


#endif //REQRIO_QT_FINGERPRINT_H
