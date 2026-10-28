package org.xllgl2017;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;
import org.xllgl2017.tls.*;

import static org.xllgl2017.ReqrioLibrary.REQRIO;

/**
 * TLS fingerprint builder.
 */
public class Fingerprint {
    private Pointer raw;

    /**
     * Create a fingerprint wrapper from a native pointer.
     * @param raw Native fingerprint pointer.
     */
    public Fingerprint(Pointer raw) {
        this.raw = raw;
    }

    /**
     * Create a custom fingerprint.
     * @param rv Record version.
     * @param mv Message version.
     * @param token Auth token.
     */
    public Fingerprint(Version rv, Version mv, String token) {
        this(REQRIO.Fingerprint_new(rv.value, mv.value, token));
    }

    /**
     * Build a random TLS fingerprint.
     * @param token Auth token.
     * @return Fingerprint instance.
     */
    public static Fingerprint random(String token) throws Exception {
        PointerByReference err = new PointerByReference();
        if (token == null) token = "";
        Fingerprint fingerprint = new Fingerprint(REQRIO.Fingerprint_random(token, err));
        util.check_err_pointer(err);
        return fingerprint;
    }

    /**
     * Build a JA3 fingerprint.
     * @param ja3 JA3 value.
     * @param token Auth token.
     * @return Fingerprint instance.
     */
    public static Fingerprint fromJa3(String ja3, String token) throws Exception {
        PointerByReference err = new PointerByReference();
        if (token == null) token = "";
        Fingerprint fingerprint = new Fingerprint(REQRIO.Fingerprint_from_ja3(ja3, token, err));
        util.check_err_pointer(err);
        return fingerprint;
    }

    /**
     * Build a JA4 fingerprint.
     * @param ja4 JA4 value.
     * @param token Auth token.
     * @return Fingerprint instance.
     */
    public static Fingerprint fromJa4(String ja4, String token) throws Exception {
        PointerByReference err = new PointerByReference();
        if (token == null) token = "";
        Fingerprint fingerprint = new Fingerprint(REQRIO.Fingerprint_from_ja4(ja4, token, err));
        util.check_err_pointer(err);
        return fingerprint;
    }

    /**
     * Build a fingerprint from a raw ClientHello payload.
     * @param client_hello Raw ClientHello bytes.
     * @param token Auth token.
     * @return Fingerprint instance.
     */
    public static Fingerprint fromClientHello(byte[] client_hello, String token) throws Exception {
        PointerByReference err = new PointerByReference();
        if (token == null) token = "";
        Pointer pointer = REQRIO.Fingerprint_from_client_hello(client_hello, client_hello.length, token, err);
        util.check_err_pointer(err);
        return new Fingerprint(pointer);
    }

    /**
     * Use a built-in TLS fingerprint.
     * @param name Fingerprint name.
     * @param sec_full Whether to include the full sec-* header set.
     * @param token Auth token.
     * @return Fingerprint instance.
     */
    public static Fingerprint fromBuiltin(String name, boolean sec_full, String token) throws Exception {
        PointerByReference err = new PointerByReference();
        if (token == null) token = "";
        Pointer pointer = REQRIO.Fingerprint_from_builtin(name, sec_full, token, err);
        util.check_err_pointer(err);
        return new Fingerprint(pointer);
    }

    /**
     * Use a fingerprint by index.
     * @param idx Fingerprint index.
     * @param auth Auth token.
     * @param sec_full Whether to include the full sec-* header set.
     * @return Fingerprint instance.
     */
    public static Fingerprint fromIdx(int idx, String auth, boolean sec_full) throws Exception {
        PointerByReference err = new PointerByReference();
        Pointer pointer = REQRIO.Fingerprint_from_idx(idx, auth, sec_full, err);
        util.check_err_pointer(err);
        return new Fingerprint(pointer);
    }

    /**
     * Add a cipher suite.
     * @param suite Cipher suite value.
     */
    public void addSuite(CipherSuite suite) {
        this.addSuite(suite.value);
    }

    /**
     * Add a cipher suite.
     * @param suite Cipher suite value.
     */
    public void addSuite(int suite) {
        REQRIO.Fingerprint_add_cipher_suite(this.raw, suite);
    }

    /**
     * Add a supported group.
     * @param group Supported group value.
     */
    public void addSupportedGroup(SupportGroup group) {
        this.addSupportedGroup(group.value);
    }

    /**
     * Add a supported group.
     * @param group Supported group value.
     */
    public void addSupportedGroup(int group) {
        REQRIO.Fingerprint_add_ext_curve(this.raw, ExtensionType.SupportedGroup.value, group);
    }

    /**
     * Add a signature algorithm.
     * @param algorithm Algorithm value.
     */
    public void addAlgorithm(Algorithm algorithm) {
        this.addAlgorithm(algorithm.value);
    }

    /**
     * Add a signature algorithm.
     * @param algorithm Algorithm value.
     */
    public void addAlgorithm(int algorithm) {
        REQRIO.Fingerprint_add_ext_algorithm(this.raw, ExtensionType.SignatureAlgorithms.value, algorithm);
    }

    /**
     * Add a supported version.
     * @param version TLS version value.
     */
    public void addSupportedVersion(Version version) {
        this.addSupportedVersion(version.value);
    }

    /**
     * Add a supported version.
     * @param version TLS version value.
     */
    public void addSupportedVersion(int version) {
        REQRIO.Fingerprint_add_ext_version(this.raw, ExtensionType.SupportedVersions.value, version);
    }

    /**
     * Add a key share group.
     * @param group Group value.
     */
    public void addKeyShare(SupportGroup group) {
        this.addKeyShare(group.value);
    }

    /**
     * Add a key share group.
     * @param group Group value.
     */
    public void addKeyShare(int group) {
        REQRIO.Fingerprint_add_ext_curve(this.raw, ExtensionType.KeyShare.value, group);
    }

    /**
     * Add an EC point format.
     * @param format Format value.
     */
    public void addEcPointFormat(EcPointFormat format) {
        this.addEcPointFormat(format.value);
    }

    /**
     * Add an EC point format.
     * @param format Format value.
     */
    public void addEcPointFormat(int format) {
        REQRIO.Fingerprint_add_ext_ec_point(this.raw, ExtensionType.EcPointFormats.value, format);
    }

    /**
     * Add a compression method.
     * @param method Compression value.
     */
    public void addCompressionCertificate(CompressionMethod method) {
        this.addCompressionCertificate(method.value);
    }

    /**
     * Add a compression method.
     * @param method Compression value.
     */
    public void addCompressionCertificate(int method) {
        REQRIO.Fingerprint_add_ext_compress(this.raw, ExtensionType.CompressionCertificate.value, method);
    }

    /**
     * Add an ALPN value.
     * @param alpn ALPN value.
     */
    public void addApplicationLayerProtocolNegotiation(ALPN alpn) {
        this.addApplicationLayerProtocolNegotiation(alpn.getValue());
    }

    /**
     * Add an ALPN value.
     * @param alpn ALPN value.
     */
    public void addApplicationLayerProtocolNegotiation(String alpn) {
        REQRIO.Fingerprint_add_ext_alpn(this.raw, ExtensionType.ApplicationLayerProtocolNegotiation.value, alpn);
    }

    /**
     * Add an application setting value.
     * @param alpn ALPN value.
     */
    public void addApplicationSetting(ALPN alpn) {
        this.addApplicationSetting(alpn.getValue());
    }

    /**
     * Add an application setting value.
     * @param alpn ALPN value.
     */
    public void addApplicationSetting(String alpn) {
        REQRIO.Fingerprint_add_ext_alpn(this.raw, ExtensionType.ApplicationSetting.value, alpn);
    }

    /**
     * Add an old application setting value.
     * @param alpn ALPN value.
     */
    public void addApplicationSettingOld(ALPN alpn) {
        this.addApplicationSettingOld(alpn.getValue());
    }

    /**
     * Add an old application setting value.
     * @param alpn ALPN value.
     */
    public void addApplicationSettingOld(String alpn) {
        REQRIO.Fingerprint_add_ext_alpn(this.raw, ExtensionType.ApplicationSettingOld.value, alpn);
    }

    /**
     * Add a PSK mode.
     * @param mode PSK mode value.
     */
    public void addPskMode(PskMode mode) {
        this.addPskMode(mode.value);
    }

    /**
     * Add a PSK mode.
     * @param mode PSK mode value.
     */
    public void addPskMode(int mode) {
        REQRIO.Fingerprint_add_ext_psk_mode(this.raw, ExtensionType.PskKeyExchangeMode.value, mode);
    }

    /**
     * Add padding bytes.
     * @param size Padding length.
     */
    public void addPadding(int size) {
        REQRIO.Fingerprint_add_ext_padding(this.raw, ExtensionType.Padding.value, size);
    }

    /**
     * Add a raw extension.
     * @param typ Extension type.
     */
    public void addExtension(ExtensionType typ) {
        this.addExtension(typ.value);
    }

    /**
     * Add a raw extension.
     * @param typ Extension type.
     */
    public void addExtension(int typ) {
        REQRIO.Fingerprint_add_ext(this.raw, typ);
    }

    /**
     * Add raw extension bytes.
     * @param typ Extension type.
     * @param data Raw byte array.
     */
    public void addExtension(ExtensionType typ, byte[] data) {
        this.addExtension(typ.value, data);
    }

    /**
     * Add raw extension bytes.
     * @param typ Extension type.
     * @param data Raw byte array.
     */
    public void addExtension(int typ, byte[] data) {
        REQRIO.Fingerprint_add_ext_bytes(this.raw, typ, data, data.length);
    }

    /**
     * Add an HTTP/2 setting.
     * @param typ Setting type.
     * @param value Setting value.
     */
    public void addH2Setting(H2SettingType typ, int value) {
        this.addH2Setting(typ.value, value);
    }

    /**
     * Add an HTTP/2 setting.
     * @param typ Setting type.
     * @param value Setting value.
     */
    public void addH2Setting(int typ, int value) {
        REQRIO.Fingerprint_add_h2_setting(this.raw, typ, value);
    }

    /**
     * Add an HTTP/2 frame.
     * @param typ Frame type.
     * @param data Raw frame bytes.
     */
    public void addH2Frame(H2FrameType typ, byte[] data) {
        this.addH2Frame(typ.value, data);
    }

    /**
     * Add an HTTP/2 frame.
     * @param typ Frame type.
     * @param data Raw frame bytes.
     */
    public void addH2Frame(int typ, byte[] data) {
        REQRIO.Fingerprint_add_h2_frame(this.raw, typ, data, data.length);
    }

    /**
     * Clear all HTTP/2 frames.
     */
    public void clearH2Frames() {
        REQRIO.Fingerprint_clear_h2_frame(this.raw);
    }

    /**
     * Set the HTTP/2 window size.
     * @param size Window size.
     */
    public void setH2WindowSize(int size) {
        REQRIO.Fingerprint_set_h2_window_size(this.raw, size);
    }

    /**
     * Set the HTTP/2 priority weight.
     * @param priority Whether to set priority.
     * @param weight Priority weight.
     */
    public void setH2Priority(boolean priority, int weight) {
        REQRIO.Fingerprint_set_h2_priority(this.raw, priority, weight);
    }

    /**
     * Clear all HTTP/3 frames.
     */
    public void clearH3Frames() {
        REQRIO.Fingerprint_clean_h3_frame(this.raw);
    }

    /**
     * Add an HTTP/3 setting.
     * @param typ Setting type.
     * @param value Setting value.
     */
    public void addH3Setting(H3SettingType typ, int value) {
        this.addH3Setting(typ.value, value);
    }

    /**
     * Add an HTTP/3 setting.
     * @param typ Setting type.
     * @param value Setting value.
     */
    public void addH3Setting(int typ, int value) {
        REQRIO.Fingerprint_add_h3_setting(this.raw, typ, value);
    }

    /**
     * Add an HTTP/3 frame.
     * @param typ Frame type.
     * @param data Raw frame bytes.
     */
    public void addH3Frame(H3FrameType typ, byte[] data) {
        this.addH3Frame(typ.value, data);
    }

    /**
     * Add an HTTP/3 frame.
     * @param typ Frame type.
     * @param data Raw frame bytes.
     */
    public void addH3Frame(int typ, byte[] data) {
        REQRIO.Fingerprint_add_h3_frame(this.raw, typ, data, data.length);
    }

    /**
     * Get the native fingerprint pointer.
     * @return Native pointer.
     */
    public Pointer getRaw() throws Exception {
        if (raw == null) throw new Exception("This Fingerprint had dropped");
        return raw;
    }

    /**
     * Clear the native pointer reference.
     */
    public void drop() {
        this.raw = null;
    }
}
