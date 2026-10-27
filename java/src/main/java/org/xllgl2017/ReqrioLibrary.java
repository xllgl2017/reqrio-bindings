package org.xllgl2017;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.LongByReference;
import com.sun.jna.ptr.PointerByReference;

import java.io.IOException;

public interface ReqrioLibrary extends Library {
    ReqrioLibrary REQRIO = ReqrioLibrary.loadLibrary();

    Pointer ScReq_new(boolean ignore_hdr_sort);

    Pointer ScReq_set_header_json(Pointer req, String header);

    Pointer ScReq_add_header(Pointer req, String key, String value, boolean reversed);

    Pointer ScReq_remove_header(Pointer req, String key);

    Pointer ScReq_set_alpn(Pointer req, String alpn);

    Pointer ScReq_set_verify(Pointer req, boolean verify);

    Pointer ScReq_set_redirect(Pointer req, boolean redirect);

    Pointer ScReq_set_key_log(Pointer req, String path);

    Pointer ScReq_set_fingerprint(Pointer req, Pointer fingerprint);

    Pointer ScReq_set_proxy(Pointer req, String proxy);

    Pointer ScReq_set_timeout(Pointer req, String timeout);

    Pointer ScReq_set_version(Pointer req, int ver);

    Pointer ScReq_set_cookie(Pointer req, String cookie);

    Pointer ScReq_add_cookie(Pointer req, String name, String value);

    Pointer ScReq_reconnect(Pointer req);

    Pointer ScReq_connect(Pointer req, String url, String sni);

    Pointer ScReq_do_http(Pointer req, int method, Pointer url, Pointer body, boolean stream, PointerByReference err);

    Pointer ScReq_recv_stream(Pointer req, int sid, LongByReference len, PointerByReference err);

    Pointer ScReq_close_stream(Pointer req);

    void ScReq_drop(Pointer req);

    void char_free(Pointer ptr);

    /// =========================>[Url]<==========================

    Pointer Url_new(String base_url, PointerByReference err);

    Pointer Url_add_param(Pointer url, String name, String value);

    Pointer Url_remove_param(Pointer url, String name);

    Pointer Url_set_sni(Pointer url, String sni);

    void Url_drop(Pointer url);

    /// ========================>[Body]<===========================
    Pointer Body_new(byte[] data, int len, String content_type, PointerByReference err);

    Pointer Body_none();

    Pointer Body_new_files(Pointer file, String data, PointerByReference err);

    Pointer HttpFile_new();

    Pointer HttpFile_add_form(Pointer file, Pointer form);

    Pointer FileForm_new(String path, String field, String content_type, PointerByReference err);

    void HttpFile_drop(Pointer file);

    void Body_drop(Pointer body);

    /// ======================>[Response]<=======================
    int Response_status_code(Pointer resp, PointerByReference err);

    Pointer Response_bytes(Pointer resp, LongByReference len, PointerByReference err);

    Pointer Response_get_header(Pointer resp, String name, PointerByReference err);

    Pointer Response_cookies(Pointer resp, PointerByReference err);

    Pointer Response_header_keys(Pointer resp, PointerByReference err);

    int Response_sid(Pointer resp, PointerByReference err);

    void Response_drop(Pointer resp);

    /// =======================>[Fingerprint]<=======================
    Pointer Fingerprint_new(int rv, int mv, String token);

    Pointer Fingerprint_from_builtin(String name, boolean sec_full, String token, PointerByReference err);

    Pointer Fingerprint_from_idx(int idx, String auth, boolean sec_full, PointerByReference err);

    Pointer Fingerprint_from_ja3(String ja3, String token, PointerByReference err);

    Pointer Fingerprint_from_ja4(String ja4, String token, PointerByReference err);

    Pointer Fingerprint_from_client_hello(byte[] client_hello, int len, String token, PointerByReference err);

    Pointer Fingerprint_random(String token, PointerByReference err);

    void Fingerprint_add_cipher_suite(Pointer finger, int suite);

    void Fingerprint_add_ext(Pointer finger, int typ);

    void Fingerprint_add_ext_alpn(Pointer finger, int typ, String alpn);

    void Fingerprint_add_ext_version(Pointer finger, int typ, int ver);

    void Fingerprint_add_ext_curve(Pointer finger, int typ, int curve);

    void Fingerprint_add_ext_compress(Pointer finger, int typ, int method);

    void Fingerprint_add_ext_psk_mode(Pointer finger, int typ, int mode);

    void Fingerprint_add_ext_padding(Pointer finger, int typ, int padding);

    void Fingerprint_add_ext_bytes(Pointer finger, int typ, byte[] bs, int len);

    void Fingerprint_add_ext_algorithm(Pointer finger, int typ, int algo);

    void Fingerprint_add_ext_ec_point(Pointer finger, int typ, int point);

    void Fingerprint_clear_h2_frame(Pointer finger);

    void Fingerprint_add_h2_setting(Pointer finger, int flag, int value);

    void Fingerprint_add_h2_frame(Pointer finger, int typ, byte[] data, int data_len);

    void Fingerprint_clean_h3_frame(Pointer finger);

    void Fingerprint_add_h3_setting(Pointer finger, int flag, int value);

    void Fingerprint_add_h3_frame(Pointer finger, int typ, byte[] data, int data_len);

    void Fingerprint_set_h2_window_size(Pointer finger, int size);

    void Fingerprint_set_h2_priority(Pointer finger, boolean priority, int weight);

    void Fingerprint_drop(Pointer finger);



    /// =======================>[WebSocket]<=======================
    Pointer ws_open(String url, String header);

    Pointer ws_open_raw(String url, String raw);

    Pointer ws_read(Pointer ws);

    int ws_write(Pointer ws, int opcode, boolean mask, String msg);

    void ws_close(Pointer ws);

    Pointer url_encode(String value);

    static ReqrioLibrary loadLibrary() {
        try {
            String os_name = System.getProperty("os.name").toLowerCase();
            String dll_name;
            if (os_name.contains("win"))
                dll_name = "reqrio.dll";
            else if (os_name.contains("mac"))
                dll_name = "libreqrio.dylib";
            else if (os_name.contains("nux") || os_name.contains("nix"))
                dll_name = "libreqrio.so";
            else throw new Exception("unsupported system");
            String tmp_dir = System.getProperty("java.io.tmpdir");
            java.io.File dll_file = new java.io.File(tmp_dir, dll_name);
            try {
                java.io.InputStream in = ReqrioLibrary.class.getResourceAsStream("/" + dll_name);
                java.io.OutputStream out = new java.io.FileOutputStream(dll_file);
                byte[] buffer = new byte[4096];
                int read;
                if (in != null) {
                    while ((read = in.read(buffer)) != -1) {
                        out.write(buffer, 0, read);
                    }
                }
                out.flush();
                out.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
//            System.out.println(dll_file);
            return Native.load(dll_file.getAbsolutePath(), ReqrioLibrary.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}