//
// Created by XLX on 2026/5/16.
//

#ifndef REQRIO_QT_EXTENSIONTYPE_H
#define REQRIO_QT_EXTENSIONTYPE_H

enum ExtensionType {
    ServerName = 0x0,
    StatusRequest = 0x5,
    SupportedGroup = 0xa,
    EcPointFormats = 0xb,
    SignatureAlgorithms = 0xd,
    ApplicationLayerProtocolNegotiation = 0x10,
    SignedCertificateTimestamp = 0x12,
    Padding = 0x15,
    EncryptTheMac = 0x16,
    ExtendMasterSecret = 0x17,
    SessionTicket = 0x23,
    CompressionCertificate = 0x1b,
    SupportedVersions = 0x2b,
    PskKeyExchangeMode = 0x2d,
    PostHandshakeAuth = 0x31,
    KeyShare = 0x33,
    RenegotiationInfo = 0xff01,
    EncryptedClientHello = 0xfe0d,
    ApplicationSetting = 0x44cd,
    PreSharedKey = 0x29,
    ApplicationSettingOld = 0x4469
};
#endif //REQRIO_QT_EXTENSIONTYPE_H
