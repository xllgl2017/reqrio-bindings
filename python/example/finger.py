import reqrio
from example.req import headers
from reqrio import *

try:
    with open("../../TOKEN", 'r', encoding="utf-8") as f:
        TOKEN = f.read()
except:
    TOKEN = ""


# ====================================>TLS Example<======================================

def ua_tls():
    print("================>[TLS WITH UA]<==================")
    fingerprint = {
        "sec-ch-ua": "\"Microsoft Edge\";v=\"131\", \"Chromium\";v=\"131\", \"Not_A Brand\";v=\"24\"",
        "sec-ch-ua_mobile": "?0",
        "sec-ch-ua_platform": "\"Linux\"",
        "tls_finger": "16030106b2010006ae0303f0aed3d4d9fac0e8d4ff98981a90257765d203b4ce089c591e86d8e7ec8ab90a204803c2150a14429bfe6536328fe11cfd4034264fa2a3a443c5972eeeb93d427100206a6a130113021303c02bc02fc02cc030cca9cca8c013c014009c009d002f0035010006453a3a000000230000001b0003020002ff010001000000000e000c0000093338686d7a672e636e0005000501000000000017000044cd00050003026832fe0d00ba00000100010900208e3fc249e1ce71ff4aefb0970b38167b6b7de98537b874130ba4e284e15f1c4f00909540fc3a77fcc8f96d51ff9144785ccf114d3618d9a77b0e88f54d4dd1279083483e0ad83a4f25e55951194048709bf0842651d940c291569b9cfe1323d6fc2d31348ccaaa7b79271fc41af0975d94f7a826819154e05f6f90bdaa4e2b215894ccd36f748ded2bcae0a61aa101a7187588c2b45b51d076356d0e47728974d6d1cdd2b3ce4a8e5e8f70a79fb8f288c868000b00020100002d00020101000a000c000a3a3a11ec001d00170018001200000010000e000c02683208687474702f312e31003304ef04ed3a3a00010011ec04c05b20439ba8b50e3a5800981889512ab253cd2f1ba1488613fbd79f43813c08e34ed45330a62991a6b37890d54d2d0c089251b146acace84512c031c74ac6a2ac6345b6668629aa143357b45921916de02ac5cc8d57e1ca9882ccad900640a1b51c587de3291a2f15ad67e180b79b442fe4606de978f7a27591a41ffcd91116c50703c45531999c9d377a173c249ef747a60a81158c0d3ef709b9b5a38af61b6b5c9740c343f7322b6510a60797cb39148ba310413b688354bb0b2e395dbf3935fd0a797d7b5e94acab23a95c163238dd1bc9b8b420599a0efd4726e85a0783fc8506436c3eb89ee96008b0c9c5a2047a2415bbb5a2768d7c8d58384644d5473de96721b24a3fc82ee68cc0a3a43cc73467ec515a3ac1a79b9070f4e4aad61ac50c7b4e9b125f66cba026807cdad5a43e4a5cfa2ac521801616bb58ea068689c15afd4592b26545c3a8c638800a3429c32237a902f1a605458935391c4d352a211cb2122203f9ea38e3d44b29741502bb57c7850ffaf36ab0db72ac9c0fc0ba309661096bc550d86b442beca080c0602e02a54ed2171e58b0b82582c568a5b1407d8d35448cf907a43575aed4c5371595d1456f29778c892325d4d785a3a384a30b838e6b0d59990ca54ba52369c4faf835a2f50cbd504f7d38cdc4047bf7acae92090cf121180096a513dc4cadf290641ab6e4375aa477395b8902b74c39e62b945a09438d83b1d41ac2f204c4614425bed86e221c60c8520e1c3233e5ccb53c228c0d525fb7823d9d9c4337e36785eb61590794f9565b3dd2722a2834b536be157a307d928d7f910167a314b8705bdddc1b4c9c139a5320380910b1263b40a6c6065c84266a2c036a19d3a51f5edbb8eaf3cb1e8295ef1ab978f5306da9b11a5a3df473bbd2acca084a4c4bba0bc478630283b0e6910bde3052c6f58300703a6e9524381b4cc1b247236acc1c0bae6cb69c463c29811b04d93a589ba36d30c9b4d1fb234368a9b3e94abaf419a220af730917488bc9be585f7111c9a13a8544969bf3e397b1f2ceba0ca7f21785531a3f7856248f54a5bd854124b21e1e75c366e8b5293130bdb902db0a05e9803c3d7827d5cc26046815102c3713b4a14ef63aed3163319244995a6524dbabfaf93ed8a95e08641377683dd6b3b05084bf48f77d47904d09656d4a19b457d84bcfd77a4c433393bbb43f09931cf4896cf891990c9363202467b6193ea6b8bd493733235c93c118feb808b1d9b38cc7862c744342e2baeeec6299d0a21898aa9576ae61b2703a5b072521166f6693aa4b5e6148ad4e7c21a21a7972a0c8c3f986e95392ed2b15e51a5f2e5b90e4766320513e3bfa4d67688fb6c547147c47aa71c04095336b11b32b52a6c9d047a1357eece2688efb2045184653a480ef15a3fb8c4851d8c0407b24a87b55fd36af59b18fff38b183b6256e15c161395a46f62ce1b0af240319dec84d3aa04e2773ac289b393160683e901b2b622d615b2719b06cc12bae79fca101e737a91434c8e0828cc6a71b740216964a06a9952d9c54f24743b1b9c4fc9475554aa8a87719ccd7ae40374c87d8018937c7b6007e028b348e884d201087416396ec3237b61319e0f40e436a6a1dc75f2486a68c60c27f719d251a9d73b3de3bd91858d3f3d4043384f7ad42422b47b96bdd03b5556f8107232953dad801970157aa95971638e2908d55001d0020552cb65392fdab1ff61dd3b43c895fdf782c61bb6f05519f2b7d9e28facfd25e000d0012001004030804040105030805050108060601002b000706dada030403031a1a000100",
        "user-agent": "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36 Edg/131.0.0.0"
    }
    client_hello = hex_decode(fingerprint["tls_finger"])
    del fingerprint["tls_finger"]
    hdr = headers.copy()
    hdr.update(fingerprint)
    session = Session(headers, alpn=ALPN.HTTP20, client_hello=client_hello, token=TOKEN)
    resp = session.get('https://www.baidu.com')
    print('code: ', resp.statue_code())
    print('body: ', len(resp.bytes()))

    resp = reqrio.get('https://www.baidu.com', headers, client_hello=client_hello, token=TOKEN)
    print('code: ', resp.statue_code())
    print('body: ', len(resp.bytes()))


def ja3():
    print("================>[JA3 TLS]<==================")
    ja3_str = "771,4865-4866-4867-49195-49199-49196-49200-52393-52392-49171-49172-156-157-47-53,0-23-65281-10-11-35-16-5-13-18-51-45-43-27-17513-21,29-23-24,0"
    session = Session(ja3=ja3_str, token=TOKEN)
    resp = session.get('https://www.baidu.com')
    print('code: ', resp.statue_code())
    print('body: ', len(resp.bytes()))

    resp = reqrio.get('https://www.baidu.com', headers, ja3=ja3_str, token=TOKEN)
    print('code: ', resp.statue_code())
    print('body: ', len(resp.bytes()))


def ja4():
    print("================>[JA4 TLS]<==================")
    ja4_str = "t13d1516h2_002f,0035,009c,009d,1301,1302,1303,c013,c014,c02b,c02c,c02f,c030,cca8,cca9_0005,000a,000b,000d,0012,0017,001b,0023,002b,002d,0033,44cd,fe0d,ff01_0403,0804,0401,0503,0805,0501,0806,0601"
    session = Session(ja4=ja4_str, token=TOKEN)
    resp = session.get('https://www.baidu.com')
    print('code: ', resp.statue_code())
    print('body: ', len(resp.bytes()))


def rand_tls():
    print("================>[RANDOM TLS]<==================")
    session = Session(random_tls=True, token=TOKEN)
    resp = session.get('https://www.baidu.com')
    print('code: ', resp.statue_code())
    print('body: ', len(resp.bytes()))


def custom_finger():
    print("================>[CUSTOM TLS]<==================")
    finger = {
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
                Group.X25519,
                Group.Secp256r1,
                Group.Secp384r1,
                Group.Secp521r1,
            ],
            ExtensionType.EcPointFormats: [EcPointFormat.UNCOMPRESSED],
            ExtensionType.SignatureAlgorithms: [
                Algorithm.RSA_PKCS1_SHA1,
                Algorithm.RSA_PKCS1_SHA256,
                Algorithm.RSA_PKCS1_SHA384,
                Algorithm.RSA_PKCS1_SHA512,
                Algorithm.ECDSA_SECP256R1_SHA256,
                Algorithm.ECDSA_SECP384R1_SHA384,
                Algorithm.ECDSA_SECP521R1_SHA512,
                Algorithm.RSA_PSS_PSS_SHA256,
                Algorithm.RSA_PSS_PSS_SHA384,
                Algorithm.RSA_PSS_PSS_SHA512,
                Algorithm.RSA_PSS_RSAE_SHA256,
                Algorithm.RSA_PSS_RSAE_SHA384,
                Algorithm.RSA_PSS_RSAE_SHA512,
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
            ExtensionType.PskKeyExchangeMode: None,
            ExtensionType.KeyShare: [
                Group.X25519,
                Group.Secp256r1,
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
    import json
    print(json.dumps(finger))
    session = Session(headers, custom_tls=finger, token=TOKEN)
    resp = session.get("https://www.baidu.com")
    print('code: ', resp.statue_code())
    print('body: ', len(resp.bytes()))
