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
        finger.addExtension(ExtensionType.Padding, new JsonPrimitive(192 + 19 - domain.length()));

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
        finger.addSupportedVersion(Version.TLS_1_1);
        finger.addSupportedVersion(Version.TLS_1_0);

        finger.addKeyShare(group);
        finger.addKeyShare(SupportGroup.X25519);

        finger.setPskMode(1);

        finger.addApplicationLayerProtocolNegotiation(ALPN.HTTP20);
        finger.addApplicationLayerProtocolNegotiation(ALPN.HTTP11);

        finger.addApplicationSettingOld(ALPN.HTTP20);
        finger.addCompressionCertificate(CompressionMethod.BROTLI);

        return Fingerprint.fromCustom(finger, "");
    }

    static Headers buildHeader() {
        Headers headers = new Headers();
        headers.addHeader("Connection", "keep-alive");
        headers.addHeader("MT-V", "faadf75ac25d8abe0f27732f3fj");
        headers.addHeader("MT-Device-ID", "clips_cEEkFiIXJkByQXJDJRB0R3cWIUR8TylPKRouGH5JcA==");
        headers.addHeader("Content-Web-Bb", "87e543a10f6d5cb28987e543a10f6dcbef8c47bc549abfc3ea7aac2808c4e85c97af05e45e4a24a3bc03e294b909f175e3cafa23d3baf7aa96b112e6461687d4c87b6bed34e3ee3bbec6e452d997f6cb7e3830acdffc08f5f5d52e575f1e1cc249101541f124b023b650430bafa2fb5ea6f1dd0e1cc8270a21c9830aa083cdd464cc685fbb0abcd5a92c45f67614fe53a12c49855284b3a436bfcb7591caa453956577ee9f0a5fd0ba608177035ad8e869080d8751de20fdcf75c314271cfd276bb768a9a93f554e74cd87e89288890c83c09574a967509d08104ee94365cfbe9dcd9e7ee135d383df9f71a03d27ff2282aa7b98fa893cd8ba7a2bb2a73678c365c34f79263193806257c4ac1285d5431504997a2afb641e65a6b453b0a22d6060b3b6ba5ff5a6da3d2ad38526366853ee601945f80d907bb846aa03d5eed27ca02ec6b6c2a0099096ed070a37b133bd1093ae6b13ef63c3a2424b02e8eb8ac110505dd37b4e0e1588b8366a75432db9d0d06c825708daba59aae9cbd4ca75e0");
        headers.addHeader("MT-APP-Version", "1.9.6");
        headers.addHeader("Sdk-Ver-Bb", "V3.5.0_20260403.1_imaotai");
        headers.addHeader("Accept", "application/json, text/javascript, */*; q=0.01");
        headers.addHeader("Content-Hh-Bb", "10e33ddf06731b3914edc463e80b1f5c");
        headers.addHeader("X-Requested-With", "XMLHttpRequest");
        headers.addHeader("MT-Info", "a3f9c2b8471de05f9b6c4e1287d5a9c1");
        headers.addHeader("User-Agent", "Mozilla/5.0 (Linux; Android 12; 2201123C Build/SKQ1.211006.001; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/95.0.4638.74 Mobile Safari/537.36 moutaiapp/1.9.6 device-id/d22ac8c225fbdad138a287317b258535 BS-DVID/CV1z6ADnxGokMzTGFd-1XJEXM4uku4K-yg0YsqsFywHz_46-4Wv2GYDt8gv0djt96IP9zr3LyklfrYLeYtDarYQ");
        headers.addHeader("MT-K", "1777893752116");
        headers.addHeader("Origin", "https://h5.moutai519.com.cn");
        headers.addHeader("Sec-Fetch-Site", "same-origin");
        headers.addHeader("Sec-Fetch-Mode", "cors");
        headers.addHeader("Sec-Fetch-Dest", "empty");
        headers.addHeader("Referer", "https://h5.moutai519.com.cn/mt/item/smsp-detail?appConfig=2_1_2");
        headers.addHeader("Accept-Encoding", "gzip, deflate");
        headers.addHeader("Accept-Language", "zh-CN,zh;q=0.9,en-US;q=0.8,en;q=0.7");
        headers.addHeader("Cookie", "MT-Token-Wap=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJtdCIsImV4cCI6MTc4MDM2MTk2NSwidXNlcklkIjoxMTk3MzM3NDEwLCJkZXZpY2VJZCI6ImNsaXBzX2NFRWtGaUlYSmtCeVFYSkRKUkIwUjNjV0lVUjhUeWxQS1JvdUdINUpjQT09IiwiaWF0IjoxNzc3NzY5OTY1fQ.Ej5gLah0FDO19pbdO2WzIHwYyasiGv7uAhUjQlzL7g8; MT-Device-ID-Wap=clips_cEEkFiIXJkByQXJDJRB0R3cWIUR8TylPKRouGH5JcA==; gdxidpyhxdE=MyuYse8gur39N%5CXBZ%5Cei5%2F07R6AOpH7A%2F0uuZY1EkVCYQD67Wu2a98TSEtuSpOldjk1h5TDmdXknMYC3ojc938dsbElNVSMmmTCp1DPW9OCQWvGKyTbo0VsC%2F6TNhpsiprY%5COQt3qin3rCbZN%2FhNuyvDP6TGEbHkhGV%5CWanAn2q8V0%5C2%3A1777826251350; _sdk_v_=V3.5.0_20260403.1_imaotai; _bs_device_id=bid-8867863987771-1518-ae4a; _d_u=432a1d531c62c4824f7203434b1d5a18e17c5577055af88aeb385d41d50e8ea8224ce18ff0479d10f76d8d8cb571c1fb45c4a1988b24171cf6ffdc8b7a802c31f04e71feaf35cce128630548ebd891510b6773b0b48da3a87ce2c2e622a6ded27f4770ac0a562b5adbe89495ddfc09b4d5f34e8f86e6141908308438fdc28618c0d0db3441885e75c309165af920f02d08f45085826e22c6080d4930ca5f77f8eaaec8658effecc3ebafa1ef0254aeea47aa15e726c8ad9b9ddfde0419b6618b8c54ded22dd4daa7c2266db61ddc3fd18e38301b82ab2f1a16d6913601545989a405a57928c675925b496307530d4ff6971265c3f0ce9811f8032aeb30bb8519");
        headers.addHeader("x-csrf-token", "");
        return headers;
    }

    static String SNI = "h5.moutai519.com.cn";

    public static void main(String[] args) throws Exception {
        Session session = new Session(ALPN.HTTP20);
        session.setVerify(true);
        session.setFingerprint(buildFingerprint(SNI));
        session.setHeaders(buildHeader());
        session.setKeyLog("../2.log");
        Url url = new Url("https://220.167.102.112/xhr/front/trade/priority/rushPurchase/hot/branch/one", SNI);
        JsonObject data = new JsonObject();
        data.add("actParam", new JsonPrimitive("salGYFt5S6bQg3QmZ92dY6bsH+8CAJ0R8kZwwurslmXbTh0epueTLDQriQRinhZlHxazFMfUEIr7IQzUxd3hjNAb2U/yYWciM2reBPdS+APR4IV9CE60Nb9n+Id++Pf4yYcdUEBJaTXQuMzNblj0M90JBJQbOG40L7GZCPdELGylgHK9C0F8BlSFNh80hSdRw2KmOJL2HAYyfuscHG0qTsHXECJ4+OHwBTcRt+dKDOhyl3LSAL5a8Eb3Ht1vLgleDKmr0SzyDEIVOJuSPW23F4Fmq7NPIOnPY8hL5XL3ewpBpgGWj53vu2SVi27sTRioopNhjW6J2SOQyUsHFj60EpCbMwh6++NkGum/ltUZ1OHpN7psICQQXb9FDuak/2ytffZiKbZcLWgiF+FuuB2ofXV77NfUhTTr7xO6S/J32rI="));
        Response response = session.post(url, new Body(data));
        System.out.println(response.statusCode());
        System.out.println(response.text());
        session.close();


    }
}
