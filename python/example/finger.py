import os

import reqrio
from reqrio import *

try:
    with open("../../../TOKEN", 'r', encoding="utf-8") as f:
        TOKEN = f.read()
except:
    TOKEN = os.environ.get("REQRIO_TOKEN", "")

try:
    with open("../../../AUTH", 'r', encoding="utf-8") as f:
        AUTH = f.read()
except:
    AUTH = os.environ.get("REQRIO_AUTH", "")

headers = {
    "Accept": "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7",
    "Accept-Encoding": "gzip, deflate, br, zstd",
    "Accept-Language": "zh-CN,zh;q=0.9,en;q=0.8,en-GB;q=0.7,en-US;q=0.6",
    "Cache-Control": "no-cache",
    "Connection": "keep-alive",
    "Cookie": "__guid=15015764.1071255116101212729.1764940193317.2156; env_webp=1; _S=pvc5q7leemba50e4kn4qis4b95; QiHooGUID=4C8051464B2D97668E3B21198B9CA207.1766289287750; count=1; so-like-red=2; webp=1; so_huid=114r0SZFiQcJKtA38GZgwZg%2Fdit1cjUGuRcsIL2jTn4%2FE%3D; __huid=114r0SZFiQcJKtA38GZgwZg%2Fdit1cjUGuRcsIL2jTn4%2FE%3D; gtHuid=1",
    "Host": "m.so.com",
    "Pragma": "no-cache",
    "Sec-Fetch-Dest": "document",
    "Sec-Fetch-Mode": "navigate",
    "Sec-Fetch-Site": "none",
    "Sec-Fetch-User": "?1",
    "Upgrade-Insecure-Requests": 1,
    "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36 Edg/143.0.0.0",
    "sec-ch-ua": '"Microsoft Edge";v="143", "Chromium";v="143", "Not A(Brand";v="24"',
    "sec-ch-ua-mobile": "?0",
    "sec-ch-ua-platform": '"Windows"'
}


# ====================================>TLS Example<======================================

def ua_tls():
    print("================>[TLS WITH UA]<==================")
    fingerprint = {
        "sec-ch-ua": "\"Microsoft Edge\";v=\"131\", \"Chromium\";v=\"131\", \"Not_A Brand\";v=\"24\"",
        "sec-ch-ua_mobile": "?0",
        "sec-ch-ua_platform": "\"Linux\"",
        "tls_finger": "1603010200010001fc03035596282fcd88555b2a7ef1660eb03023c5de801e47c7573978ecf738dd429089204004a86612be14e1c7dd386ed33a81dc43d719f3e2ca636c4954106c25eea7a800202a2a130113021303c02bc02fc02cc030cca9cca8c013c014009c009d002f003501000193aaaa000000000018001600001368352e6d6f757461693531392e636f6d2e636e00170000ff01000100000a000a00082a2a001d00170018000b00020100002300000010000e000c02683208687474702f312e31000500050100000000000d0012001004030804040105030805050108060601001200000033002b00292a2a000100001d002029464d3800337987dd96c99a6faf6f6d0dfcf82276b0a9934d3fe6535b6bc567002d00020101002b0007065a5a03040303001b00030200024469000500030268323a3a000100001500c400000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000",
        "user-agent": "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36 Edg/131.0.0.0"
    }
    client_hello = hex_decode("1603010200010001fc03035596282fcd88555b2a7ef1660eb03023c5de801e47c7573978ecf738dd429089204004a86612be14e1c7dd386ed33a81dc43d719f3e2ca636c4954106c25eea7a800202a2a130113021303c02bc02fc02cc030cca9cca8c013c014009c009d002f003501000193aaaa000000000018001600001368352e6d6f757461693531392e636f6d2e636e00170000ff01000100000a000a00082a2a001d00170018000b00020100002300000010000e000c02683208687474702f312e31000500050100000000000d0012001004030804040105030805050108060601001200000033002b00292a2a000100001d002029464d3800337987dd96c99a6faf6f6d0dfcf82276b0a9934d3fe6535b6bc567002d00020101002b0007065a5a03040303001b00030200024469000500030268323a3a000100001500c400000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000")[5:]
    session = Session(headers, alpn=ALPN.HTTP20, fingerprint=Fingerprint.from_client_hello(client_hello, TOKEN))
    resp = session.get('https://www.baidu.com')
    print('code: ', resp.status_code)
    print(resp.alpn)
    print('body: ', len(resp.bytes()))

    resp = reqrio.get('https://www.baidu.com', headers, client_hello=client_hello, token=TOKEN)
    print('code: ', resp.status_code)
    print('body: ', len(resp.bytes()))


def ja3():
    print("================>[JA3 TLS]<==================")
    ja3_str = "771,4865-4866-4867-49195-49199-49196-49200-52393-52392-49171-49172-156-157-47-53,0-23-65281-10-11-35-16-5-13-18-51-45-43-27-17513-21,29-23-24,0"
    session = Session(fingerprint=Fingerprint.from_ja3(ja3_str, TOKEN))
    resp = session.get('https://www.baidu.com')
    print('code: ', resp.status_code)
    print('body: ', len(resp.bytes()))

    resp = reqrio.get('https://www.baidu.com', headers, ja3=ja3_str, token=TOKEN)
    print('code: ', resp.status_code)
    print('body: ', len(resp.bytes()))


def ja4():
    print("================>[JA4 TLS]<==================")
    ja4_str = "t13d1516h2_002f,0035,009c,009d,1301,1302,1303,c013,c014,c02b,c02c,c02f,c030,cca8,cca9_0005,000a,000b,000d,0012,0017,001b,0023,002b,002d,0033,44cd,fe0d,ff01_0403,0804,0401,0503,0805,0501,0806,0601"
    session = Session(fingerprint=Fingerprint.from_ja4(ja4_str, TOKEN))
    resp = session.get('https://www.baidu.com')
    print('code: ', resp.status_code)
    print('body: ', len(resp.bytes()))


def rand_tls():
    print("================>[RANDOM TLS]<==================")
    session = Session(fingerprint=Fingerprint.from_random(TOKEN))
    resp = session.get('https://www.baidu.com')
    print('code: ', resp.status_code)
    print('body: ', len(resp.bytes()))


def custom_finger():
    print("================>[CUSTOM TLS]<==================")
    finger = {
        "record_version": Version.TLS_1_2,
        "message_version": Version.TLS_1_2,
        "suites": [
            CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,
            CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384,
            CipherSuite.TLS_ECDHE_RSA_WITH_CHACHA20_POLY1305_SHA256,
            CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA,
            CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA,
            CipherSuite.TLS_AES_128_GCM_SHA256,
            CipherSuite.TLS_AES_256_GCM_SHA384,
            CipherSuite.TLS_CHACHA20_POLY1305_SHA256,
        ],
        "extensions": {
            ExtensionType.StatusRequest: None,
            ExtensionType.SupportedGroup: [
                NamedCurve.X25519,
                NamedCurve.Secp256r1,
                NamedCurve.Secp384r1,
                NamedCurve.Secp521r1,
            ],
            ExtensionType.EcPointFormats: [EcPointFormat.UNCOMPRESSED],
            ExtensionType.SignatureAlgorithms: [
                SignatureAlgorithm.RSA_PKCS1_SHA1,
                SignatureAlgorithm.RSA_PKCS1_SHA256,
                SignatureAlgorithm.RSA_PKCS1_SHA384,
                SignatureAlgorithm.RSA_PKCS1_SHA512,
                SignatureAlgorithm.ECDSA_SECP256R1_SHA256,
                SignatureAlgorithm.ECDSA_SECP384R1_SHA384,
                SignatureAlgorithm.ECDSA_SECP521R1_SHA512,
                SignatureAlgorithm.RSA_PSS_PSS_SHA256,
                SignatureAlgorithm.RSA_PSS_PSS_SHA384,
                SignatureAlgorithm.RSA_PSS_PSS_SHA512,
                SignatureAlgorithm.RSA_PSS_RSAE_SHA256,
                SignatureAlgorithm.RSA_PSS_RSAE_SHA384,
                SignatureAlgorithm.RSA_PSS_RSAE_SHA512,
            ],
            ExtensionType.SignedCertificateTimestamp: None,
            ExtensionType.ExtendMasterSecret: None,
            ExtensionType.CompressionCertificate: [CompressionMethod.NULL],
            ExtensionType.SessionTicket: None,
            ExtensionType.SupportedVersions: [
                0xeaea,
                Version.TLS_1_3,
                Version.TLS_1_2
            ],
            ExtensionType.PskKeyExchangeMode: [PskMode.PSK_DHE_KE],
            ExtensionType.KeyShare: [
                NamedCurve.X25519,
                NamedCurve.Secp256r1,
            ],
            ExtensionType.ApplicationSetting: [
                ALPN.HTTP20,
                ALPN.HTTP11
            ],
            ExtensionType.ServerName: None,
            ExtensionType.ApplicationLayerProtocolNegotiation: [
                ALPN.HTTP20,
                ALPN.HTTP11
            ],
            ExtensionType.Padding: 12,
            0xdada: [0]
        },
        # http2 frame setting
        "settings": {
            H2Setting.HeaderTableSize: 65536,
            H2Setting.EnablePush: 0,
            H2Setting.InitialWindowSize: 6291456,
            H2Setting.MaxHeaderListSize: 242144
        },
        "window_size": 2147418112,
        "weight": 147,
        "priority": True

    }
    fingerprint = Fingerprint.from_custom(finger, TOKEN)
    session = Session(headers, fingerprint=fingerprint)
    resp = session.get("https://www.baidu.com")
    print('code: ', resp.status_code)
    print('body: ', len(resp.bytes()))


def builtin_finger():
    session = Session(fingerprint=Fingerprint.from_builtin("win-x64-edge-156", TOKEN))
    resp = session.get("https://www.baidu.com")
    print(resp.status_code, len(resp.text))
    import os
    os.remove("builtin.dat")


def finger_idx():
    session = Session(fingerprint=Fingerprint.from_idx(0, AUTH))
    resp = session.get("https://www.baidu.com")
    print(resp.text)
    print(resp.status_code, len(resp.text))


def h3_req():
    # ja4_str = "t13d1516h2_002f,0035,009c,009d,1301,1302,1303,c013,c014,c02b,c02c,c02f,c030,cca8,cca9_0005,000a,000b,000d,0012,0017,001b,0023,002b,002d,0033,44cd,fe0d,ff01_0403,0804,0401,0503,0805,0501,0806,0601"
    Fingerprint.new_custom(Version.TLS_1_0, Version.TLS_1_2, TOKEN)
    session = Session(headers, alpn=ALPN.HTTP30)
    resp = session.get("https://www.bing.com")
    print(resp.statue_code)


def tlcp():
    print("================>[TLCP]<==================")
    print(TOKEN)
    fingerprint = Fingerprint.new_custom(Version.TLCP, Version.TLCP, TOKEN)
    fingerprint.add_cipher_suite(CipherSuite.ECC_SM4_CBC_SM3)
    fingerprint.add_extension(0xdada)
    fingerprint.add_extension(ExtensionType.ServerName)
    fingerprint.add_extension(0x8a8a, bytes([0]))
    session = Session(headers, alpn=ALPN.HTTP20, fingerprint=fingerprint)
    resp = session.get("https://test.gmssl.cn")
    print(resp.headers)
    print(resp.headers["date"])
    print(resp.status_code)


ua_tls()
ja3()
ja4()
rand_tls()
custom_finger()
builtin_finger()
tlcp()
finger_idx()
# h3_req()
