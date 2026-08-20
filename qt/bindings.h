#pragma once

#include <cstdint>

enum ALPN {
    HTTP20,
    HTTP11
};

enum Method {
    GET = 0,
    POST = 1,
    PUT = 2,
    HEAD = 3,
    DELETE = 4,
    OPTIONS = 5,
    TRACE = 6,
    CONNECT = 7,
    PATCH = 8,
    QUERY = 9
};

namespace bindings {
    extern "C" {
    ///=========================>[Url]<=====================
    struct Url;

    Url *Url_new(const char *url, char **err);

    char *Url_add_param(Url *url, const char *name, const char *value);

    char *Url_remove_param(Url *url, const char *name);

    char *Url_set_sni(Url *url, const char *sni);

    void Url_drop(Url *url);

    ///=========================>[Response]<=====================
    struct Response;

    uint16_t Response_status_code(const Response *response, char **err);

    uint8_t *Response_bytes(Response *response, size_t *len, char **err);

    char *Response_get_header(const Response *response, const char *name, char **err);

    char *Response_cookies(const Response *response, char **err);

    uint64_t Response_sid(const Response *response, char **err);

    void Response_drop(Response *response);

    ///=========================>[Body]<=====================
    struct Body;

    Body *Body_new(const uint8_t *data, size_t len, const char *ty, char **err);

    Body *Body_none();

    struct HttpFile;

    Body *Body_new_files(HttpFile *files, const char *data, char **err);

    HttpFile *HttpFile_new();

    struct FileForm;

    char *HttpFile_add_form(HttpFile *file, FileForm *form);

    FileForm *FileForm_new(const char *path, const char *field_name, const char *filetype, char **err);

    void HttpFile_drop(HttpFile *file);

    void Body_drop(Body *body);

    ///=========================>[Fingerprint]<=====================
    struct Fingerprint;

    Fingerprint *Fingerprint_from_ja3(const char *ja3, const char *token, char **err);

    Fingerprint *Fingerprint_from_ja4(const char *ja4, const char *token, char **err);

    Fingerprint *Fingerprint_from_client_hello(const uint8_t *u8, size_t len, const char *token, char **err);

    Fingerprint *Fingerprint_random(const char *token, char **err);

    Fingerprint *Fingerprint_custom(const char *custom, const char *token, char **err);

    Fingerprint *Fingerprint_new(const char *token);

    void Fingerprint_add_cipher_suite(Fingerprint *fingerprint, uint16_t suite);

    void Fingerprint_add_ext(Fingerprint *fingerprint, uint16_t ext_typ);

    void Fingerprint_add_ext_alpn(Fingerprint *fingerprint, uint16_t ext_typ, const char *alpn);

    void Fingerprint_add_ext_version(Fingerprint *fingerprint, uint16_t ext_typ, uint16_t version);

    void Fingerprint_add_ext_curve(Fingerprint *fingerprint, uint16_t ext_typ, uint16_t curve);

    void Fingerprint_add_ext_compress(Fingerprint *fingerprint, uint16_t ext_typ, uint16_t compress);

    void Fingerprint_add_ext_psk_mode(Fingerprint *fingerprint, uint16_t ext_typ, uint8_t mode);

    void Fingerprint_add_ext_padding(Fingerprint *fingerprint, uint16_t ext_typ, size_t padding);

    void Fingerprint_add_ext_bytes(Fingerprint *fingerprint, uint16_t ext_typ, const uint8_t *bytes, size_t len);

    void Fingerprint_add_ext_algorithm(Fingerprint *fingerprint, uint16_t ext_typ, uint16_t algo);

    void Fingerprint_add_ext_ec_point(Fingerprint *fingerprint, uint16_t ext_typ, uint8_t point);

    void Fingerprint_add_h2_setting(Fingerprint *fingerprint, uint16_t flag, uint32_t value);

    void Fingerprint_set_h2_window_size(Fingerprint *fingerprint, uint32_t size);

    void Fingerprint_set_h2_priority(Fingerprint *fingerprint, bool priority, uint8_t weight);

    void Fingerprint_drop(Fingerprint *fingerprint);

    ///=========================>[ScReq]<=====================
    struct ScReq;

    ScReq *ScReq_new(bool ignore_hdr_sort);

    char *ScReq_set_header_json(ScReq *req, const char *header);

    char *ScReq_add_header(ScReq *req, const char *key, const char *value, bool reversed);

    char *ScReq_remove_header(ScReq *req, const char *key);

    char *ScReq_set_alpn(ScReq *req, const char *alpn);

    char *ScReq_set_verify(ScReq *req, bool verify);

    char *ScReq_set_redirect(ScReq *req, bool redirect);

    char *ScReq_set_key_log(ScReq *req, const char *key_log);

    char *ScReq_set_fingerprint(ScReq *req, Fingerprint *fingerprint);

    char *ScReq_set_proxy(ScReq *req, const char *proxy);

    char *ScReq_set_timeout(ScReq *req, const char *timeout);

    char *ScReq_set_cookie(ScReq *req, const char *cookie);

    char *ScReq_add_cookie(ScReq *req, const char *name, const char *value);

    Response *ScReq_do_http(ScReq *req, Method method, Url *url, Body *body, bool stream, char **err);

    const uint8_t *ScReq_recv_stream(ScReq *req, uint64_t sid, size_t *len, char **err);

    char *ScReq_reconnect(ScReq *req);

    char *ScReq_connect(ScReq *req, const char *url, const char *sni);

    char *ScReq_close_stream(ScReq *req);

    void ScReq_drop(ScReq *req);


    void char_free(char *p);

    struct WsBuilder;

    WsBuilder *ws_build();

    int ws_add_header(WsBuilder *builder, const char *name, const char *value);

    int ws_set_proxy(WsBuilder *builder, const char *proxy);

    int ws_set_url(WsBuilder *builder, const char *url);

    int ws_set_uri(WsBuilder *builder, const char *uri);

    struct WS_SOCKET;

    WS_SOCKET *ws_open(WsBuilder *builder);

    WS_SOCKET *ws_open_raw(const char *url, const char *raw);

    char *ws_read(WS_SOCKET *ws);

    int ws_write(WS_SOCKET *ws, int opcode, bool mask, const char *msg);

    void ws_close(WS_SOCKET *ws);

    char *url_encode(const char *str);
    }
}
