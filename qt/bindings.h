#pragma once

#include <cstdint>

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
