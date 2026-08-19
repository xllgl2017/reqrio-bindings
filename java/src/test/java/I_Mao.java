import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.xllgl2017.*;

import java.util.concurrent.ThreadLocalRandom;

public class I_Mao {
    static int[] VALUES = {0x0a0a, 0x1a1a, 0x2a2a, 0x3a3a, 0x4a4a, 0x5a5a, 0x6a6a, 0x7a7a, 0x8a8a, 0x9a9a, 0xaaaa, 0xbaba, 0xcaca, 0xeaea, 0xfafa};

    static Fingerprint buildFingerprint(String domain) throws Exception {
        CustomFingerprint finger = new CustomFingerprint();
        finger.addSuite(VALUES[ThreadLocalRandom.current().nextInt(VALUES.length)]);
        finger.addSuite(CipherSuite.TLS_AES_128_GCM_SHA256);
        finger.addSuite(CipherSuite.TLS_AES_256_GCM_SHA384);
        finger.addSuite(CipherSuite.TLS_CHACHA20_POLY1305_SHA256);
        finger.addSuite(CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256);
        finger.addSuite(CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256);
        finger.addSuite(CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384);
        finger.addSuite(CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384);
        finger.addSuite(CipherSuite.TLS_ECDHE_ECDSA_WITH_CHACHA20_POLY1305_SHA256);
        finger.addSuite(CipherSuite.TLS_ECDHE_RSA_WITH_CHACHA20_POLY1305_SHA256);
        finger.addSuite(CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA);
        finger.addSuite(CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA);
        finger.addSuite(CipherSuite.TLS_RSA_WITH_AES_128_GCM_SHA256);
        finger.addSuite(CipherSuite.TLS_RSA_WITH_AES_256_GCM_SHA384);
        finger.addSuite(CipherSuite.TLS_RSA_WITH_AES_128_CBC_SHA);
        finger.addSuite(CipherSuite.TLS_RSA_WITH_AES_256_CBC_SHA);

        finger.addExtension(VALUES[ThreadLocalRandom.current().nextInt(VALUES.length)]);
        finger.addExtension(ExtensionType.ServerName);
        finger.addExtension(ExtensionType.ExtendMasterSecret);
        finger.addExtension(ExtensionType.RenegotiationInfo);
        finger.addExtension(ExtensionType.SupportedGroup);
        finger.addExtension(ExtensionType.EcPointFormats);
        finger.addExtension(ExtensionType.SessionTicket);
        finger.addExtension(ExtensionType.ApplicationLayerProtocolNegotiation);
        finger.addExtension(ExtensionType.StatusRequest);
        finger.addExtension(ExtensionType.SignatureAlgorithms);
        finger.addExtension(ExtensionType.SignedCertificateTimestamp);
        finger.addExtension(ExtensionType.KeyShare);
        finger.addExtension(ExtensionType.PskKeyExchangeMode);
        finger.addExtension(ExtensionType.SupportedVersions);
        finger.addExtension(ExtensionType.CompressionCertificate);
        finger.addExtension(ExtensionType.ApplicationSettingOld);
        JsonArray er2 = new JsonArray();
        er2.add(0);
        finger.addExtension(VALUES[ThreadLocalRandom.current().nextInt(VALUES.length)], er2);
        finger.addExtension(ExtensionType.Padding, new JsonPrimitive(196 + 19 - domain.length()));

        int group = VALUES[ThreadLocalRandom.current().nextInt(VALUES.length)];
        finger.addSupportedGroup(group);
        finger.addSupportedGroup(SupportGroup.X25519);
        finger.addSupportedGroup(SupportGroup.Secp256r1);
        finger.addSupportedGroup(SupportGroup.Secp384r1);

        finger.addEcPointFormat(EcPointFormat.UNCOMPRESSED);

        finger.addAlgorithm(Algorithm.ECDSA_SECP256R1_SHA256);
        finger.addAlgorithm(Algorithm.RSA_PSS_RSAE_SHA256);
        finger.addAlgorithm(Algorithm.RSA_PKCS1_SHA256);
        finger.addAlgorithm(Algorithm.ECDSA_SECP384R1_SHA384);
        finger.addAlgorithm(Algorithm.RSA_PSS_RSAE_SHA384);
        finger.addAlgorithm(Algorithm.RSA_PKCS1_SHA384);
        finger.addAlgorithm(Algorithm.RSA_PSS_RSAE_SHA512);
        finger.addAlgorithm(Algorithm.RSA_PKCS1_SHA512);

        finger.addSupportedVersion(VALUES[ThreadLocalRandom.current().nextInt(VALUES.length)]);
        finger.addSupportedVersion(Version.TLS_1_3);
        finger.addSupportedVersion(Version.TLS_1_2);
//        finger.addSupportedVersion(Version.TLS_1_1);
//        finger.addSupportedVersion(Version.TLS_1_0);

        finger.addKeyShare(group);
        finger.addKeyShare(SupportGroup.X25519);

        finger.setPskMode(1);

        finger.addApplicationLayerProtocolNegotiation(ALPN.HTTP20);
        finger.addApplicationLayerProtocolNegotiation(ALPN.HTTP11);

        finger.addApplicationSettingOld(ALPN.HTTP20);
        finger.addCompressionCertificate(CompressionMethod.BROTLI);

        return Fingerprint.fromCustom(finger, "");
    }


    static Session buildSession() throws Exception {
        //忽略内置请求头排序
        Session session = new Session(true);
        session.setVerify(true);
        session.setFingerprint(buildFingerprint(SNI));
        //重建内置请求头顺序
        session.setHeader("Host", "h5.moutai519.com.cn", true);
        session.setHeader("Connection", "keep-alive", true);
        session.setHeader("Content-Length", "0", true);
        session.setHeader("MT-V", "", true);
        session.setHeader("MT-Device-ID", "", true);
        session.setHeader("Content-Web-Bb", "", true);
        session.setHeader("x-csrf-token", "");
        session.setHeader("MT-APP-Version", "1.9.9");
        session.setHeader("Sdk-Ver-Bb", "V3.5.0_20260626.1_imaotai");
        session.setHeader("content-type", "", true);
        session.setHeader("Accept", "application/json, text/javascript, */*; q=0.01");
        session.setHeader("Content-Hh-Bb", "", true);
        session.setHeader("X-Requested-With", "XMLHttpRequest");
        session.setHeader("MT-Info", "", true);
        session.setHeader("User-Agent", "<UserAgent>");
        session.setHeader("MT-K", "", true);
        session.setHeader("Origin", "https://h5.moutai519.com.cn");
        session.setHeader("Sec-Fetch-Site", "same-origin");
        session.setHeader("Sec-Fetch-Mode", "cors");
        session.setHeader("Sec-Fetch-Dest", "empty");
        session.setHeader("Referer", "", true);
        session.setHeader("Accept-Encoding", "gzip, deflate");
        session.setHeader("Accept-Language", "zh-CN,zh;q=0.9,en-US;q=0.8,en;q=0.7");
        session.setHeader("Cookie", "test=fds", true);
        return session;
    }

    static String SNI = "h5.moutai519.com.cn";

    public static void main(String[] args) throws Exception {
        Session session = buildSession();
        session.connect("https://h5.moutai519.com.cn", SNI);
        session.setKeyLog("../../2.log");
        Url url = new Url("https://h5.moutai519.com.cn/xhr/front/trade/priority/rushPurchase/hot/branch/one", SNI);
        JsonObject data = new JsonObject();
        data.add("actParam", new JsonPrimitive("iPezxmLCzfvUGKCKcEMfOwjAdR1fl+/cZal5SWZktnOYtjMqUI9dPMOA84mX46pxT7M5+DWE3JiP4T0Ot2a3TCkldHRIef4vWsZx5dYoVYCXw8JU2y/9zdGM1usXoo5xQmwAlZWcfTtVb3g8B++gxK1s0iWVg02LzzIRdoYf44cGQKJctHMCi1wCuaolVDnDnofWDojVgj2rxURA0zXjnkCfwRUd0i2XV6e87S5EEH7bxKCTSal4NUEt6A4HsYZJ097wUEvvBM/yXob//9cakYn4FpRT0nsNg3VadSAgI8GXx3UV79XmT8NTli9hOuQHH6+Dk2jrYMdAZgZQH9C+6bO7kDP+apXkOBUSQirjh1reHX1BjLDVQPCNstGzu5j6U2yQyhwCNPenmPU7T8c07nYlgPueEN1mM+jH9oGyIRT1nHPDBoLCHBrT/5nP3CXHyE/umYx9HoWjXHDRGfgChquBgq68asSHHZnFL2yvouw="));
        data.add("v", new JsonPrimitive("002"));
        Response response = session.post(url, new Body(data));
        System.out.println(response.statusCode());
        System.out.println(response.text());
        session.close();


    }
}
