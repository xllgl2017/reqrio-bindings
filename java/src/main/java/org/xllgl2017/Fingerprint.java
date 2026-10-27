package org.xllgl2017;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;
import org.xllgl2017.tls.*;

import static org.xllgl2017.ReqrioLibrary.REQRIO;

public class Fingerprint {
    private Pointer raw;

    public Fingerprint(Pointer raw) {
        this.raw = raw;
    }

    public Fingerprint(Version rv, Version mv, String token) {
        this(REQRIO.Fingerprint_new(rv.value, mv.value, token));
    }

    /// @param token :认证token，联系客服
    public static Fingerprint random(String token) throws Exception {
        PointerByReference err = new PointerByReference();
        if (token == null) token = "";
        Fingerprint fingerprint = new Fingerprint(REQRIO.Fingerprint_random(token, err));
        util.check_err_pointer(err);
        return fingerprint;
    }

    /// @param ja3   :TLS JA3，可使用Wireshark抓取
    /// @param token :认证token，联系客服
    public static Fingerprint fromJa3(String ja3, String token) throws Exception {
        PointerByReference err = new PointerByReference();
        if (token == null) token = "";
        Fingerprint fingerprint = new Fingerprint(REQRIO.Fingerprint_from_ja3(ja3, token, err));
        util.check_err_pointer(err);
        return fingerprint;
    }

    /// @param ja4   :TLS JA4，可使用Wireshark抓取
    /// @param token :认证token，联系客服
    public static Fingerprint fromJa4(String ja4, String token) throws Exception {
        PointerByReference err = new PointerByReference();
        if (token == null) token = "";
        Fingerprint fingerprint = new Fingerprint(REQRIO.Fingerprint_from_ja4(ja4, token, err));
        util.check_err_pointer(err);
        return fingerprint;
    }

    /// @param client_hello :TLS ClientHello二进制，可使用Wireshark抓取
    /// @param token        :认证token，联系客服
    public static Fingerprint fromClientHello(byte[] client_hello, String token) throws Exception {
        PointerByReference err = new PointerByReference();
        if (token == null) token = "";
        Pointer pointer = REQRIO.Fingerprint_from_client_hello(client_hello, client_hello.length, token, err);
        util.check_err_pointer(err);
        return new Fingerprint(pointer);
    }


    /// 使用某个内置指纹
    ///
    /// @param name     :指纹名称
    /// @param token    :认证token
    /// @param sec_full :是否使用完整的sec请求头
    public static Fingerprint fromBuiltin(String name, boolean sec_full, String token) throws Exception {
        PointerByReference err = new PointerByReference();
        if (token == null) token = "";
        Pointer pointer = REQRIO.Fingerprint_from_builtin(name, sec_full, token, err);
        util.check_err_pointer(err);
        return new Fingerprint(pointer);
    }

    /// 使用某个指纹
    ///
    /// @param idx       :索引，超出上限自动重选，index%len。
    /// @param auth      : 指纹认证
    /// @param sec_full: 是否使用完整的sec请求头
    public static Fingerprint fromIdx(int idx, String auth, boolean sec_full) throws Exception {
        PointerByReference err = new PointerByReference();
        Pointer pointer = REQRIO.Fingerprint_from_idx(idx, auth, sec_full, err);
        util.check_err_pointer(err);
        return new Fingerprint(pointer);
    }

    public void addSuite(CipherSuite suite) {
        this.addSuite(suite.value);
    }

    public void addSuite(int suite) {
        REQRIO.Fingerprint_add_cipher_suite(this.raw, suite);
    }

    public void addSupportedGroup(SupportGroup group) {
        this.addSupportedGroup(group.value);
    }

    public void addSupportedGroup(int group) {
        REQRIO.Fingerprint_add_ext_curve(this.raw, ExtensionType.SupportedGroup.value, group);
    }

    public void addAlgorithm(Algorithm algorithm) {
        this.addAlgorithm(algorithm.value);
    }

    public void addAlgorithm(int algorithm) {
        REQRIO.Fingerprint_add_ext_algorithm(this.raw, ExtensionType.SignatureAlgorithms.value, algorithm);
    }

    public void addSupportedVersion(Version version) {
        this.addSupportedVersion(version.value);
    }

    public void addSupportedVersion(int version) {
        REQRIO.Fingerprint_add_ext_version(this.raw, ExtensionType.SupportedVersions.value, version);
    }

    public void addKeyShare(SupportGroup group) {
        this.addKeyShare(group.value);
    }

    public void addKeyShare(int group) {
        REQRIO.Fingerprint_add_ext_curve(this.raw, ExtensionType.KeyShare.value, group);
    }

    public void addEcPointFormat(EcPointFormat format) {
        this.addEcPointFormat(format.value);
    }

    public void addEcPointFormat(int format) {
        REQRIO.Fingerprint_add_ext_ec_point(this.raw, ExtensionType.EcPointFormats.value, format);
    }

    public void addCompressionCertificate(CompressionMethod method) {
        this.addCompressionCertificate(method.value);
    }

    public void addCompressionCertificate(int method) {
        REQRIO.Fingerprint_add_ext_compress(this.raw, ExtensionType.CompressionCertificate.value, method);
    }

    public void addApplicationLayerProtocolNegotiation(ALPN alpn) {
        this.addApplicationLayerProtocolNegotiation(alpn.getValue());
    }

    public void addApplicationLayerProtocolNegotiation(String alpn) {
        REQRIO.Fingerprint_add_ext_alpn(this.raw, ExtensionType.ApplicationLayerProtocolNegotiation.value, alpn);
    }

    public void addApplicationSetting(ALPN alpn) {
        this.addApplicationSetting(alpn.getValue());
    }

    public void addApplicationSetting(String alpn) {
        REQRIO.Fingerprint_add_ext_alpn(this.raw, ExtensionType.ApplicationSetting.value, alpn);
    }

    public void addApplicationSettingOld(ALPN alpn) {
        this.addApplicationSettingOld(alpn.getValue());
    }

    public void addApplicationSettingOld(String alpn) {
        REQRIO.Fingerprint_add_ext_alpn(this.raw, ExtensionType.ApplicationSettingOld.value, alpn);
    }

    public void addPskMode(PskMode mode) {
        this.addPskMode(mode.value);
    }

    public void addPskMode(int mode) {
        REQRIO.Fingerprint_add_ext_psk_mode(this.raw, ExtensionType.PskKeyExchangeMode.value, mode);
    }

    public void addPadding(int size) {
        REQRIO.Fingerprint_add_ext_padding(this.raw, ExtensionType.Padding.value, size);
    }

    public void addExtension(ExtensionType typ) {
        this.addExtension(typ.value);
    }

    public void addExtension(int typ) {
        REQRIO.Fingerprint_add_ext(this.raw, typ);
    }

    public void addExtension(ExtensionType typ, byte[] data) {
        this.addExtension(typ.value, data);
    }

    public void addExtension(int typ, byte[] data) {
        REQRIO.Fingerprint_add_ext_bytes(this.raw, typ, data, data.length);
    }

    public void addH2Setting(H2SettingType typ, int value) {
        this.addH2Setting(typ.value, value);
    }

    public void addH2Setting(int typ, int value) {
        REQRIO.Fingerprint_add_h2_setting(this.raw, typ, value);
    }

    public void addH2Frame(H2FrameType typ, byte[] data) {
        this.addH2Frame(typ.value, data);
    }

    public void addH2Frame(int typ, byte[] data) {
        REQRIO.Fingerprint_add_h2_frame(this.raw, typ, data, data.length);
    }

    public void clearH2Frames() {
        REQRIO.Fingerprint_clear_h2_frame(this.raw);
    }

    public void setH2WindowSize(int size) {
        REQRIO.Fingerprint_set_h2_window_size(this.raw, size);
    }

    public void setH2Priority(boolean priority, int weight) {
        REQRIO.Fingerprint_set_h2_priority(this.raw, priority, weight);
    }

    public void clearH3Frames() {
        REQRIO.Fingerprint_clean_h3_frame(this.raw);
    }

    public void addH3Setting(H3SettingType typ, int value) {
        this.addH3Setting(typ.value, value);
    }

    public void addH3Setting(int typ, int value) {
        REQRIO.Fingerprint_add_h3_setting(this.raw, typ, value);
    }

    public void addH3Frame(H3FrameType typ, byte[] data) {
        this.addH3Frame(typ.value, data);
    }

    public void addH3Frame(int typ, byte[] data) {
        REQRIO.Fingerprint_add_h3_frame(this.raw, typ, data, data.length);
    }


    public Pointer getRaw() throws Exception {
        if (raw == null) throw new Exception("This Fingerprint had dropped");
        return raw;
    }

    public void drop() {
        this.raw = null;
    }
}
