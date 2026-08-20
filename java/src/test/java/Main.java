import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.apache.commons.codec.binary.Hex;
import org.xllgl2017.*;

import java.util.HashMap;
import java.util.List;


public class Main {
    static String TOKEN = "<token>";

    private static Headers getHeaders() {
        Headers headers = new Headers();
        headers.addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7");
        headers.addHeader("Accept-Encoding", "gzip, deflate, br, zstd");
        headers.addHeader("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8,en-GB;q=0.7,en-US;q=0.6");
        headers.addHeader("Cache-Control", "no-cache");
        headers.addHeader("Connection", "keep-alive");
        headers.addHeader("Host", "m.so.com");
        headers.addHeader("Pragma", "no-cache");
        headers.addHeader("Sec-Fetch-Dest", "document");
        headers.addHeader("Sec-Fetch-Mode", "navigate");
        headers.addHeader("Sec-Fetch-Site", "none");
        headers.addHeader("Sec-Fetch-User", "?1");
        headers.addHeader("Upgrade-Insecure-Requests", "1");
        headers.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36 Edg/143.0.0.0");
        headers.addHeader("sec-ch-ua", "\"Microsoft Edge\";v=\"143\", \"Chromium\";v=\"143\", \"Not A(Brand\";v=\"24\"");
        headers.addHeader("sec-ch-ua-mobile", "?0");
        headers.addHeader("sec-ch-ua-platform", "\"Windows\"");
        //添加cookie，也可以用reqrio.setCookie
        headers.setCookies("__guid=15015764.1071255116101212729.1764940193317.2156; env_webp=1; _S=pvc5q7leemba50e4kn4qis4b95; QiHooGUID=4C8051464B2D97668E3B21198B9CA207.1766289287750; count=1; so-like-red=2; webp=1; so_huid=114r0SZFiQcJKtA38GZgwZg%2Fdit1cjUGuRcsIL2jTn4%2FE%3D; __huid=114r0SZFiQcJKtA38GZgwZg%2Fdit1cjUGuRcsIL2jTn4%2FE%3D; gtHuid=1");
        return headers;
    }


    public static void get() throws Exception {
        try (Session session = new Session(ALPN.HTTP20)) {
            session.connect("https://www.baidu.com");
            session.setHeaders(getHeaders());
            Body body = new Body();
            ///get with str
            Response resp = session.get("https://www.baidu.com", body);
            System.out.println("code: " + resp.statusCode());
            System.out.println("len: " + resp.bytes().length);
            List<Cookie> cookies = resp.getCookies();
            for (Cookie cookie : cookies) {
                System.out.println("Set-Cookie: " + cookie.getName() + "=" + cookie.getValue());
            }
            System.out.println("header: " + resp.getHeader("server"));
            System.out.println("body: " + resp.text().substring(0, 100));

            session.removeHeader("accept");
            ///get with [Url]
            session.reconnect();
            Url url = new Url("https://www.baidu.com");
            Response resp1 = session.get(url, new Body());
            System.out.println("code: " + resp1.statusCode());
            System.out.println("len: " + resp1.bytes().length);

            /// get with [Url] and sni
            Url url1 = new Url("https://183.2.172.177", "www.baidu.com");
            Response resp2 = session.get(url1);
            System.out.println("code: " + resp2.statusCode());
            System.out.println("len: " + resp2.bytes().length);

            session.closeStream();
        }
    }

    public static void post_json() throws Exception {
        Session session = new Session(ALPN.HTTP11);
        session.connect("https://www.baidu.com", "www.baidu.com");
        session.setHeaders(getHeaders());
        JsonObject obj = new JsonObject();
        obj.add("field1", new JsonPrimitive(1));
        obj.add("field2", new JsonPrimitive("1234"));
        JsonObject obj1 = new JsonObject();
        obj1.add("key1", new JsonPrimitive("value1"));
        obj1.add("key2", new JsonPrimitive("value2"));
        obj.add("field3", obj1);
        Response resp = session.post("https://www.baidu.com", new Body(obj));
        System.out.println("code: " + resp.statusCode());
        System.out.println("len: " + resp.bytes().length);
        session.close();
    }

    public static void post_form() throws Exception {
        Session session = new Session(ALPN.HTTP11);
        session.setHeaders(getHeaders());
        HashMap<String, String> obj = new HashMap<>();
        obj.put("field1", "1");
        obj.put("field2", "1234");
        obj.put("field3", "{\"key1\": \"value1\", \"key2\": \"value2\"}");
        Response resp = session.post("https://www.baidu.com", new Body(obj));
        System.out.println("code: " + resp.statusCode());
        System.out.println("len: " + resp.bytes().length);
        session.close();
    }

    public static void post_bytes() throws Exception {
        Session session = new Session(ALPN.HTTP11);
        session.setHeaders(getHeaders());
        byte[] bs = "test byte body".getBytes();
        Response resp = session.post("https://www.baidu.com", new Body(bs, "text/plain"));
        System.out.println("code: " + resp.statusCode());
        System.out.println("len: " + resp.bytes().length);

        Response resp1 = session.post("https://www.baidu.com", new Body("test byte body"));
        System.out.println("code: " + resp1.statusCode());
        System.out.println("len: " + resp1.bytes().length);
        session.close();
    }

    public static void post_file() throws Exception {
        Session session = new Session(ALPN.HTTP11);
        session.setHeaders(getHeaders());
        HttpFile file = new HttpFile();
        file.addFile("../../2.log");
        Response resp = session.post("https://www.baidu.com", new Body());
        System.out.println("code: " + resp.statusCode());
        System.out.println("len: " + resp.bytes().length);
        session.close();
    }

    public static void put() throws Exception {
        Session session = new Session(ALPN.HTTP20);
        session.setHeaders(getHeaders());
        Url url = new Url("https://www.baidu.com");
        url.addParam("p1", "v1");
        url.addParam("p2", "v2");
        url.addParam("p3", "{'sdfdsf':34}");
        Response resp = session.put(url, new Body());
        session.close();
        System.out.println("code: " + resp.statusCode());
        System.out.println("len: " + resp.bytes().length);

    }

    public static void send() throws Exception {
        Session session = new Session();
        session.setHeaders(getHeaders());
        Body body = new Body();
        Response resp = session.send(Method.GET, new Url("https://www.baidu.com"), body);
        System.out.println("code: " + resp.statusCode());
        System.out.println("len: " + resp.bytes().length);
        session.close();
    }

    static CustomFingerprint getFinger() {
        CustomFingerprint finger = new CustomFingerprint();

        finger.addSuite(0x4a4a);
        finger.addSuite(CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256);
        finger.addSuite(CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384);
        finger.addSuite(CipherSuite.TLS_ECDHE_RSA_WITH_CHACHA20_POLY1305_SHA256);
        finger.addSuite(CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA);
        finger.addSuite(CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA);
        finger.addSuite(CipherSuite.TLS_AES_128_GCM_SHA256);
        finger.addSuite(CipherSuite.TLS_AES_256_GCM_SHA384);
        finger.addSuite(CipherSuite.TLS_CHACHA20_POLY1305_SHA256);

        finger.addExtension(ExtensionType.StatusRequest);
        finger.addExtension(ExtensionType.SupportedGroup);
        finger.addExtension(ExtensionType.EcPointFormats);
        finger.addExtension(ExtensionType.SignatureAlgorithms);
        finger.addExtension(ExtensionType.SignedCertificateTimestamp);
        finger.addExtension(ExtensionType.ExtendMasterSecret);
        finger.addExtension(ExtensionType.CompressionCertificate);
        finger.addExtension(ExtensionType.SessionTicket);
        finger.addExtension(ExtensionType.SupportedVersions);
        finger.addExtension(ExtensionType.PskKeyExchangeMode);
        finger.addExtension(ExtensionType.KeyShare);
        finger.addExtension(ExtensionType.ApplicationSetting);
        finger.addExtension(ExtensionType.ServerName);
        finger.addExtension(ExtensionType.ApplicationLayerProtocolNegotiation);
        JsonArray v1 = new JsonArray();
        v1.add(0);
        finger.addExtension(0xeaea, v1);

        finger.addSupportedGroup(SupportGroup.X25519);
        finger.addSupportedGroup(SupportGroup.Secp256r1);
        finger.addSupportedGroup(SupportGroup.Secp384r1);
        finger.addSupportedGroup(SupportGroup.Secp521r1);

        finger.addAlgorithm(Algorithm.RSA_PKCS1_SHA1);
        finger.addAlgorithm(Algorithm.RSA_PKCS1_SHA256);
        finger.addAlgorithm(Algorithm.RSA_PKCS1_SHA384);
        finger.addAlgorithm(Algorithm.RSA_PKCS1_SHA512);
        finger.addAlgorithm(Algorithm.ECDSA_SECP256R1_SHA256);
        finger.addAlgorithm(Algorithm.ECDSA_SECP384R1_SHA384);
        finger.addAlgorithm(Algorithm.ECDSA_SECP521R1_SHA512);
        finger.addAlgorithm(Algorithm.RSA_PSS_PSS_SHA256);
        finger.addAlgorithm(Algorithm.RSA_PSS_PSS_SHA384);
        finger.addAlgorithm(Algorithm.RSA_PSS_PSS_SHA512);
        finger.addAlgorithm(Algorithm.RSA_PSS_RSAE_SHA256);
        finger.addAlgorithm(Algorithm.RSA_PSS_RSAE_SHA384);
        finger.addAlgorithm(Algorithm.RSA_PSS_RSAE_SHA512);

        finger.addSupportedVersion(Version.TLS_1_3);
        finger.addSupportedVersion(Version.TLS_1_2);

        finger.addEcPointFormat(EcPointFormat.UNCOMPRESSED);

        finger.addCompressionCertificate(CompressionMethod.NULL);

        finger.addKeyShare(0xdada);
        finger.addKeyShare(SupportGroup.X25519);


        finger.addSetting(H2SettingType.HeaderTableSize, 65536);
        finger.addSetting(H2SettingType.EnablePush, 0);
        finger.addSetting(H2SettingType.InitialWindowSize, 6291456);
        finger.addSetting(H2SettingType.MaxHeaderListSize, 242144);

        finger.setWindowSize(2147418112);
        finger.setPriority(true, 147);
        return finger;
    }

    public static void custom_finger() throws Exception {
        Session session = new Session(ALPN.HTTP20);
        session.setFingerprint(Fingerprint.fromCustom(getFinger(), TOKEN));
        session.setHeaders(getHeaders());
        Response resp = session.get("https://www.baidu.com");
        System.out.println("code: " + resp.statusCode());
        System.out.println("len: " + resp.bytes().length);
        session.close();

    }

    public static void ja3() throws Exception {
        Session session = new Session(ALPN.HTTP20);
        String ja3 = "771,4865-4866-4867-49195-49199-49196-49200-52393-52392-49171-49172-156-157-47-53,0-23-65281-10-11-35-16-5-13-18-51-45-43-27-17513-21,29-23-24,0";
        session.setFingerprint(Fingerprint.fromJa3(ja3, TOKEN));
        session.setHeaders(getHeaders());
        Response resp = session.get("https://www.baidu.com");
        System.out.println("code: " + resp.statusCode());
        System.out.println("len: " + resp.bytes().length);
        session.close();
    }

    public static void ja4() throws Exception {
        Session session = new Session(ALPN.HTTP20);
        String ja4 = "t13d1516h2_002f,0035,009c,009d,1301,1302,1303,c013,c014,c02b,c02c,c02f,c030,cca8,cca9_0005,000a,000b,000d,0012,0017,001b,0023,002b,002d,0033,44cd,fe0d,ff01_0403,0804,0401,0503,0805,0501,0806,0601";
        session.setFingerprint(Fingerprint.fromJa4(ja4, TOKEN));
        session.setHeaders(getHeaders());
        Response resp = session.get("https://www.baidu.com");
        System.out.println("code: " + resp.statusCode());
        System.out.println("len: " + resp.bytes().length);
        session.close();
    }

    public static void random_tls() throws Exception {
        Session session = new Session(ALPN.HTTP20);
        session.setFingerprint(Fingerprint.random(TOKEN));
        session.setHeaders(getHeaders());
        Response resp = session.get("https://www.baidu.com");
        System.out.println("code: " + resp.statusCode());
        System.out.println("len: " + resp.bytes().length);
        session.close();
    }

    public static void ua_tls() throws Exception {
        Session session = new Session(ALPN.HTTP20);
        byte[] client_hello = Hex.decodeHex("16030106b2010006ae0303f0aed3d4d9fac0e8d4ff98981a90257765d203b4ce089c591e86d8e7ec8ab90a204803c2150a14429bfe6536328fe11cfd4034264fa2a3a443c5972eeeb93d427100206a6a130113021303c02bc02fc02cc030cca9cca8c013c014009c009d002f0035010006453a3a000000230000001b0003020002ff010001000000000e000c0000093338686d7a672e636e0005000501000000000017000044cd00050003026832fe0d00ba00000100010900208e3fc249e1ce71ff4aefb0970b38167b6b7de98537b874130ba4e284e15f1c4f00909540fc3a77fcc8f96d51ff9144785ccf114d3618d9a77b0e88f54d4dd1279083483e0ad83a4f25e55951194048709bf0842651d940c291569b9cfe1323d6fc2d31348ccaaa7b79271fc41af0975d94f7a826819154e05f6f90bdaa4e2b215894ccd36f748ded2bcae0a61aa101a7187588c2b45b51d076356d0e47728974d6d1cdd2b3ce4a8e5e8f70a79fb8f288c868000b00020100002d00020101000a000c000a3a3a11ec001d00170018001200000010000e000c02683208687474702f312e31003304ef04ed3a3a00010011ec04c05b20439ba8b50e3a5800981889512ab253cd2f1ba1488613fbd79f43813c08e34ed45330a62991a6b37890d54d2d0c089251b146acace84512c031c74ac6a2ac6345b6668629aa143357b45921916de02ac5cc8d57e1ca9882ccad900640a1b51c587de3291a2f15ad67e180b79b442fe4606de978f7a27591a41ffcd91116c50703c45531999c9d377a173c249ef747a60a81158c0d3ef709b9b5a38af61b6b5c9740c343f7322b6510a60797cb39148ba310413b688354bb0b2e395dbf3935fd0a797d7b5e94acab23a95c163238dd1bc9b8b420599a0efd4726e85a0783fc8506436c3eb89ee96008b0c9c5a2047a2415bbb5a2768d7c8d58384644d5473de96721b24a3fc82ee68cc0a3a43cc73467ec515a3ac1a79b9070f4e4aad61ac50c7b4e9b125f66cba026807cdad5a43e4a5cfa2ac521801616bb58ea068689c15afd4592b26545c3a8c638800a3429c32237a902f1a605458935391c4d352a211cb2122203f9ea38e3d44b29741502bb57c7850ffaf36ab0db72ac9c0fc0ba309661096bc550d86b442beca080c0602e02a54ed2171e58b0b82582c568a5b1407d8d35448cf907a43575aed4c5371595d1456f29778c892325d4d785a3a384a30b838e6b0d59990ca54ba52369c4faf835a2f50cbd504f7d38cdc4047bf7acae92090cf121180096a513dc4cadf290641ab6e4375aa477395b8902b74c39e62b945a09438d83b1d41ac2f204c4614425bed86e221c60c8520e1c3233e5ccb53c228c0d525fb7823d9d9c4337e36785eb61590794f9565b3dd2722a2834b536be157a307d928d7f910167a314b8705bdddc1b4c9c139a5320380910b1263b40a6c6065c84266a2c036a19d3a51f5edbb8eaf3cb1e8295ef1ab978f5306da9b11a5a3df473bbd2acca084a4c4bba0bc478630283b0e6910bde3052c6f58300703a6e9524381b4cc1b247236acc1c0bae6cb69c463c29811b04d93a589ba36d30c9b4d1fb234368a9b3e94abaf419a220af730917488bc9be585f7111c9a13a8544969bf3e397b1f2ceba0ca7f21785531a3f7856248f54a5bd854124b21e1e75c366e8b5293130bdb902db0a05e9803c3d7827d5cc26046815102c3713b4a14ef63aed3163319244995a6524dbabfaf93ed8a95e08641377683dd6b3b05084bf48f77d47904d09656d4a19b457d84bcfd77a4c433393bbb43f09931cf4896cf891990c9363202467b6193ea6b8bd493733235c93c118feb808b1d9b38cc7862c744342e2baeeec6299d0a21898aa9576ae61b2703a5b072521166f6693aa4b5e6148ad4e7c21a21a7972a0c8c3f986e95392ed2b15e51a5f2e5b90e4766320513e3bfa4d67688fb6c547147c47aa71c04095336b11b32b52a6c9d047a1357eece2688efb2045184653a480ef15a3fb8c4851d8c0407b24a87b55fd36af59b18fff38b183b6256e15c161395a46f62ce1b0af240319dec84d3aa04e2773ac289b393160683e901b2b622d615b2719b06cc12bae79fca101e737a91434c8e0828cc6a71b740216964a06a9952d9c54f24743b1b9c4fc9475554aa8a87719ccd7ae40374c87d8018937c7b6007e028b348e884d201087416396ec3237b61319e0f40e436a6a1dc75f2486a68c60c27f719d251a9d73b3de3bd91858d3f3d4043384f7ad42422b47b96bdd03b5556f8107232953dad801970157aa95971638e2908d55001d0020552cb65392fdab1ff61dd3b43c895fdf782c61bb6f05519f2b7d9e28facfd25e000d0012001004030804040105030805050108060601002b000706dada030403031a1a000100");
        session.setFingerprint(Fingerprint.fromClientHello(client_hello, TOKEN));
        session.setHeaders(getHeaders());
        session.setHeader("sec-ch-ua", "\"Microsoft Edge\";v=\"131\", \"Chromium\";v=\"131\", \"Not_A Brand\";v=\"24\"");
        session.setHeader("sec-ch-mobile", "?0");
        session.setHeader("sec-ch-ua-platform", "\"Linux\"");
        session.setHeader("user-agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36 Edg/131.0.0.0");
        Response resp = session.get("https://www.baidu.com");
        System.out.println("code: " + resp.statusCode());
        System.out.println("len: " + resp.bytes().length);
        session.close();
    }

    public static void flow_reader() throws Exception {
        Session session = new Session(ALPN.HTTP20);
        session.setHeaders(getHeaders());
        session.setHeader("sec-ch-ua", "\"Microsoft Edge\";v=\"131\", \"Chromium\";v=\"131\", \"Not_A Brand\";v=\"24\"");
        session.setHeader("sec-ch-mobile", "?0");
        session.setHeader("sec-ch-ua-platform", "\"Linux\"");
        session.setHeader("user-agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36 Edg/131.0.0.0");
        Response resp = session.send(Method.GET, new Url("https://ms.bdimg.com/pacific/0/pic/-742236409_-1564646186.png?x=0&y=0&h=340&w=510&vh=340.00&vw=510.00&oh=340.00&ow=510.00"), new Body(), true);
        System.out.println("code: " + resp.statusCode());
        for (byte[] chunk : resp.chunks()) {
            System.out.println(chunk.length);
        }
        session.close();
    }


    public static void main(String[] args) throws Exception {
        get();
        post_json();
        post_form();
        post_bytes();
        post_file();
        put();
        send();
        custom_finger();
        ja3();
        ja4();
        random_tls();
        ua_tls();
        flow_reader();
    }
}

