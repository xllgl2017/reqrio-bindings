from enum import IntEnum


class Algorithm(IntEnum):
    RSA_PKCS1_SHA1 = 0x0201,
    RSA_PKCS1_SHA256 = 0x0401,
    RSA_PKCS1_SHA384 = 0x0501,
    RSA_PKCS1_SHA512 = 0x0601,
    RSA_PSS_RSAE_SHA256 = 0x0804,
    RSA_PSS_RSAE_SHA384 = 0x0805,
    RSA_PSS_RSAE_SHA512 = 0x0806,
    RSA_PSS_PSS_SHA256 = 0x0807,
    RSA_PSS_PSS_SHA384 = 0x0808,
    RSA_PSS_PSS_SHA512 = 0x0809,
    ED25519 = 0x080A,
    ED448 = 0x080B,
    ECDSA_SHA1 = 0x0203,
    ECDSA_SECP256R1_SHA256 = 0x0403,
    ECDSA_SECP384R1_SHA384 = 0x0503,
    ECDSA_SECP521R1_SHA512 = 0x0603,
    SHA1_DSA = 0x0202,
    SHA224_RSA = 0x0301,
    SHA224_DSA = 0x0302,
    SHA224_ECDSA = 0x0303,
    SHA256_DSA = 0x0402,
    SHA384_DSA = 0x0502,
    SHA512_DSA = 0x0602,


class CompressionMethod(IntEnum):
    NULL = 0,
    DEFLATE = 1,
    BROTLI = 2,
    GZIP = 0xFFFF,
    ZSTD = 0xFFFE


class Group(IntEnum):
    X25519 = 0x1d,
    X448 = 0x1e,
    X25519MLKEM768 = 0x11ec,
    Secp256r1 = 0x0017,
    Secp384r1 = 0x0018,
    Secp521r1 = 0x0019,
    FFDHE2048 = 0x0100,
    FFDHE3072 = 0x0101,
    FFDHE4096 = 0x0102,
    FFDHE6144 = 0x0103,
    FFDHE8192 = 0x0104,


class Version(IntEnum):
    TLS_1_0 = 0x301,
    TLS_1_1 = 0x302,
    TLS_1_2 = 0x303,
    TLS_1_3 = 0x304


class EcPointFormat(IntEnum):
    UNCOMPRESSED = 0,
    ANSI_X962_PRIME = 1,
    ANSI_X962_CHAR2 = 2


class ExtensionType:
    ServerName = 0x0
    StatusRequest = 0x5
    SupportedGroup = 0xa
    EcPointFormats = 0xb
    SignatureAlgorithms = 0xd
    ApplicationLayerProtocolNegotiation = 0x10
    SignedCertificateTimestamp = 0x12
    Padding = 0x15
    EncryptTheMac = 0x16
    ExtendMasterSecret = 0x17
    SessionTicket = 0x23
    CompressionCertificate = 0x1b
    SupportedVersions = 0x2b
    PskKeyExchangeMode = 0x2d
    PostHandshakeAuth = 0x31
    KeyShare = 0x33
    RenegotiationInfo = 0xff01
    EncryptedClientHello = 0xfe0d
    ApplicationSetting = 0x44cd
    PreSharedKey = 0x29
    ApplicationSettingOld = 0x4469


class CipherSuite(IntEnum):
    # ecdhe-ecdhe
    TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256 = 0xc02b,
    TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384 = 0xc02c,
    TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256 = 0xc023,
    TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384 = 0xc024,
    TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA = 0xc009,
    TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA = 0xc00a,
    TLS_ECDHE_ECDSA_WITH_CHACHA20_POLY1305_SHA256 = 0xcca9,

    # ecdhe-rsa
    TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256 = 0xc02f,
    TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384 = 0xc030,
    TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256 = 0xc027,
    TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384 = 0xc028,
    TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA = 0xc013,
    TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA = 0xc014,
    TLS_ECDHE_RSA_WITH_CHACHA20_POLY1305_SHA256 = 0xcca8,

    # dhe-rsa
    TLS_DHE_RSA_WITH_AES_128_GCM_SHA256 = 0x009e,
    TLS_DHE_RSA_WITH_AES_256_GCM_SHA384 = 0x009f,
    TLS_DHE_RSA_WITH_AES_128_CBC_SHA256 = 0x0067,
    TLS_DHE_RSA_WITH_AES_256_CBC_SHA256 = 0x006b,
    TLS_DHE_RSA_WITH_AES_128_CBC_SHA = 0x0033,
    TLS_DHE_RSA_WITH_AES_256_CBC_SHA = 0x0039,
    TLS_DHE_RSA_WITH_CHACHA20_POLY1305_SHA256 = 0xccaa,

    # rsa
    TLS_RSA_WITH_AES_128_GCM_SHA256 = 0x009c,
    TLS_RSA_WITH_AES_256_GCM_SHA384 = 0x009d,
    TLS_RSA_WITH_AES_128_CBC_SHA256 = 0x003c,
    TLS_RSA_WITH_AES_256_CBC_SHA256 = 0x003d,
    TLS_RSA_WITH_AES_128_CBC_SHA = 0x002f,
    TLS_RSA_WITH_AES_256_CBC_SHA = 0x0035,

    # tls1.3
    TLS_AES_128_GCM_SHA256 = 0x1301,
    TLS_AES_256_GCM_SHA384 = 0x1302,
    TLS_CHACHA20_POLY1305_SHA256 = 0x1303,

    TLS_EMPTY_RENEGOTIATION_INFO_SCSV = 0x00ff,


class H2Setting:
    HeaderTableSize = "HeaderTableSize"
    EnablePush = "EnablePush"
    MaxConcurrentStreams = "MaxConcurrentStreams"
    InitialWindowSize = "InitialWindowSize"
    MaxFrameSize = "MaxFrameSize"
    MaxHeaderListSize = "MaxHeaderListSize"
    Reserved = "Reserved"
