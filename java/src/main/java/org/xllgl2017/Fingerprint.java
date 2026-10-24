package org.xllgl2017;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

import static org.xllgl2017.ReqrioLibrary.REQRIO;

public class Fingerprint {
    private Pointer raw;

    /// @param token :认证token，联系客服
    public static Fingerprint random(String token) throws Exception {
        Fingerprint fingerprint = new Fingerprint();
        PointerByReference err = new PointerByReference();
        if (token == null) token = "";
        fingerprint.raw = REQRIO.Fingerprint_random(token, err);
        util.check_err_pointer(err);
        return fingerprint;
    }

    /// @param ja3   :TLS JA3，可使用Wireshark抓取
    /// @param token :认证token，联系客服
    public static Fingerprint fromJa3(String ja3, String token) throws Exception {
        Fingerprint fingerprint = new Fingerprint();
        PointerByReference err = new PointerByReference();
        if (token == null) token = "";
        fingerprint.raw = REQRIO.Fingerprint_from_ja3(ja3, token, err);
        util.check_err_pointer(err);
        return fingerprint;
    }

    /// @param ja4   :TLS JA4，可使用Wireshark抓取
    /// @param token :认证token，联系客服
    public static Fingerprint fromJa4(String ja4, String token) throws Exception {
        Fingerprint fingerprint = new Fingerprint();
        PointerByReference err = new PointerByReference();
        if (token == null) token = "";
        fingerprint.raw = REQRIO.Fingerprint_from_ja4(ja4, token, err);
        util.check_err_pointer(err);
        return fingerprint;
    }

    /// @param client_hello :TLS ClientHello二进制，可使用Wireshark抓取
    /// @param token        :认证token，联系客服
    public static Fingerprint fromClientHello(byte[] client_hello, String token) throws Exception {
        Fingerprint fingerprint = new Fingerprint();
        PointerByReference err = new PointerByReference();
        if (token == null) token = "";
        fingerprint.raw = REQRIO.Fingerprint_from_client_hello(client_hello, client_hello.length, token, err);
        util.check_err_pointer(err);
        return fingerprint;
    }

    /// @param token :认证token，联系客服
    public static Fingerprint fromCustom(CustomFingerprint custom, String token) throws Exception {
        Fingerprint fingerprint = new Fingerprint();
        Gson gson = new GsonBuilder().serializeNulls().create();
        PointerByReference err = new PointerByReference();
        if (token == null) token = "";
        fingerprint.raw = REQRIO.Fingerprint_custom(gson.toJson(custom), token, err);
        util.check_err_pointer(err);
        return fingerprint;
    }

    /// 使用某个内置指纹
    /// @param name :指纹名称
    /// @param token :认证token
    /// @param sec_full :是否使用完整的sec请求头
    public static Fingerprint fromBuiltin(String name, boolean sec_full, String token) throws Exception {
        Fingerprint fingerprint = new Fingerprint();
        PointerByReference err = new PointerByReference();
        if (token == null) token = "";
        fingerprint.raw = REQRIO.Fingerprint_from_builtin(name, sec_full, token, err);
        util.check_err_pointer(err);
        return fingerprint;
    }

    /// 使用某个指纹
    /// @param idx :索引，超出上限自动重选，index%len。
    /// @param auth : 指纹认证
    /// @param sec_full: 是否使用完整的sec请求头
    public static Fingerprint fromIdx(int idx, String auth, boolean sec_full) throws Exception {
        Fingerprint fingerprint = new Fingerprint();
        PointerByReference err = new PointerByReference();
        fingerprint.raw = REQRIO.Fingerprint_from_idx(idx, auth, sec_full, err);
        util.check_err_pointer(err);
        return fingerprint;
    }

    public Pointer getRaw() throws Exception {
        if (raw == null) throw new Exception("This Fingerprint had dropped");
        return raw;
    }

    public void drop() {
        this.raw = null;
    }
}
