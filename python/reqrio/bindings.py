import os
import sys
from ctypes import cdll, CFUNCTYPE, c_void_p, c_int, c_char, c_uint32, c_char_p, c_bool, c_ubyte, c_size_t, c_uint16, \
    c_uint64

from _ctypes import POINTER

base = os.path.dirname(__file__)
if sys.platform == 'win32':
    dll_path = os.path.join(base, 'reqrio.dll')
elif sys.platform == 'linux':
    dll_path = os.path.join(base, 'libreqrio.so')
else:
    raise Exception('unsupported platform')
DLL = cdll.LoadLibrary(dll_path)

# ==========================>Fingerprint<=============================
DLL.Fingerprint_from_ja3.argtypes = [c_char_p, c_char_p, POINTER(c_char_p)]
DLL.Fingerprint_from_ja3.restype = c_void_p

DLL.Fingerprint_from_ja4.argtypes = [c_char_p, c_char_p, POINTER(c_char_p)]
DLL.Fingerprint_from_ja4.restype = c_void_p

DLL.Fingerprint_from_client_hello.argtypes = [POINTER(c_ubyte), c_size_t, c_char_p, POINTER(c_char_p)]
DLL.Fingerprint_from_client_hello.restype = c_void_p

DLL.Fingerprint_random.argtypes = [c_char_p, POINTER(c_char_p)]
DLL.Fingerprint_random.restype = c_void_p

DLL.Fingerprint_custom.argtypes = [c_char_p, c_char_p, POINTER(c_char_p)]
DLL.Fingerprint_custom.restype = c_void_p

# ===========================>Url<===================================
DLL.Url_new.argtypes = [c_char_p, POINTER(c_char_p)]
DLL.Url_new.restype = c_void_p

DLL.Url_add_param.argtypes = [c_void_p, c_char_p, c_char_p]
DLL.Url_add_param.restype = c_void_p

DLL.Url_set_sni.argtypes = [c_void_p, c_char_p]
DLL.Url_set_sni.restype = c_void_p

DLL.Url_drop.argtypes = [c_void_p]

# ===========================>Body<===================================
DLL.Body_new.argtypes = [POINTER(c_ubyte), c_size_t, c_char_p, POINTER(c_char_p)]
DLL.Body_new.restype = c_void_p

DLL.Body_none.argtypes = []
DLL.Body_none.restype = c_void_p

DLL.Body_new_files.argtypes = [c_void_p, c_char_p, POINTER(c_char_p)]
DLL.Body_new_files.restype = c_void_p

DLL.HttpFile_new.argtypes = []
DLL.HttpFile_new.restype = c_void_p

DLL.HttpFile_add_form.argtypes = [c_void_p, c_void_p]
DLL.HttpFile_add_form.restype = c_void_p

DLL.FileForm_new.argtypes = [c_char_p, c_char_p, c_char_p, POINTER(c_char_p)]
DLL.FileForm_new.restype = c_void_p

DLL.HttpFile_drop.argtypes = [c_void_p]

DLL.Body_drop.argtypes = [c_void_p]

# ==========================>Response<=============================

DLL.Response_status_code.argtypes = [c_void_p, POINTER(c_char_p)]
DLL.Response_status_code.restype = c_uint16

DLL.Response_bytes.argtypes = [c_void_p, POINTER(c_size_t), POINTER(c_char_p)]
DLL.Response_bytes.restype = POINTER(c_ubyte)

DLL.Response_get_header.argtypes = [c_void_p, c_char_p, POINTER(c_char_p)]
DLL.Response_get_header.restype = c_void_p

DLL.Response_cookies.argtypes = [c_void_p, POINTER(c_char_p)]
DLL.Response_cookies.restype = c_void_p

DLL.Response_sid.argtypes = [c_void_p, POINTER(c_char_p)]
DLL.Response_sid.restype = c_uint64

DLL.Response_drop.argtypes = [c_void_p]

# ===========================>ScReq<===================================

# 初始化函数
DLL.ScReq_new.argtypes = [c_bool]
DLL.ScReq_new.restype = c_void_p

DLL.ScReq_set_header_json.argtypes = [c_void_p, c_char_p]
DLL.ScReq_set_header_json.restype = c_void_p

DLL.ScReq_add_header.argtypes = [c_void_p, c_char_p, c_char_p, c_bool]
DLL.ScReq_add_header.restype = c_void_p

DLL.ScReq_remove_header.argtypes = [c_void_p, c_char_p]
DLL.ScReq_remove_header.restype = c_void_p

DLL.ScReq_set_alpn.argtypes = [c_void_p, c_char_p]
DLL.ScReq_set_alpn.restype = c_void_p

DLL.ScReq_set_verify.argtypes = [c_void_p, c_bool]
DLL.ScReq_set_verify.restype = c_void_p

DLL.ScReq_set_redirect.argtypes = [c_void_p, c_bool]
DLL.ScReq_set_redirect.restype = c_void_p

DLL.ScReq_set_key_log.argtypes = [c_void_p, c_char_p]
DLL.ScReq_set_key_log.restype = c_void_p

DLL.ScReq_set_fingerprint.argtypes = [c_void_p, c_void_p]
DLL.ScReq_set_fingerprint.restype = c_void_p

DLL.ScReq_set_proxy.argtypes = [c_void_p, c_char_p]
DLL.ScReq_set_proxy.restype = c_void_p

DLL.ScReq_set_timeout.argtypes = [c_void_p, c_char_p]
DLL.ScReq_set_timeout.restype = c_void_p

DLL.ScReq_set_cookie.argtypes = [c_void_p, c_char_p]
DLL.ScReq_set_cookie.restype = c_void_p

DLL.ScReq_add_cookie.argtypes = [c_void_p, c_char_p, c_char_p]
DLL.ScReq_add_cookie.restype = c_void_p

DLL.ScReq_reconnect.argtypes = [c_void_p]
DLL.ScReq_reconnect.restype = c_void_p

DLL.ScReq_connect.argtypes = [c_void_p, c_char_p, c_char_p]
DLL.ScReq_connect.restype = c_void_p

DLL.ScReq_close_stream.argtypes = [c_void_p]
DLL.ScReq_close_stream.restype = c_void_p

DLL.ScReq_do_http.argtypes = [c_void_p, c_int, c_void_p, c_void_p, c_bool, POINTER(c_char_p)]
DLL.ScReq_do_http.restype = c_void_p

DLL.ScReq_recv_stream.argtypes = [c_void_p, c_uint64, POINTER(c_size_t), POINTER(c_char_p)]
DLL.ScReq_recv_stream.restype = c_void_p

DLL.ScReq_drop.argtypes = [c_void_p]

DLL.char_free.argtypes = [c_void_p]

# websocket
DLL.ws_build.argtypes = []
DLL.ws_build.restype = c_void_p

DLL.ws_add_header.argtypes = [c_void_p, c_char_p, c_char_p]
DLL.ws_add_header.restype = c_int

DLL.ws_set_proxy.argtypes = [c_void_p, c_char_p]
DLL.ws_set_proxy.restype = c_int

DLL.ws_set_uri.argtypes = [c_void_p, c_char_p]
DLL.ws_set_uri.restype = c_int

DLL.ws_open.argtypes = [c_void_p]
DLL.ws_open.restype = c_void_p

DLL.ws_open_raw.argtypes = [c_char_p, c_char_p]
DLL.ws_open_raw.restype = c_void_p

DLL.ws_read.argtypes = [c_void_p]
DLL.ws_read.restype = c_void_p

DLL.ws_write.argtypes = [c_void_p, c_int, c_bool, c_void_p]
DLL.ws_write.restype = c_int

DLL.ws_close.argtypes = [c_void_p]

# reqtls

DLL.Cipher_new.argtypes = [c_int]
DLL.Cipher_new.restype = c_void_p

DLL.Cipher_set_secret_key.argtypes = [
    c_void_p,
    POINTER(c_ubyte),
    c_size_t,
    POINTER(c_ubyte),
    c_size_t
]
DLL.Cipher_set_secret_key.restype = c_int

DLL.Cipher_encrypt.argtypes = [
    c_void_p,
    POINTER(c_ubyte),
    c_size_t,
    POINTER(POINTER(c_ubyte)),
    POINTER(c_size_t),
]
DLL.Cipher_encrypt.restype = c_int

DLL.Cipher_decrypt.argtypes = [
    c_void_p,
    POINTER(c_ubyte),
    c_size_t,
    POINTER(POINTER(c_ubyte)),
    POINTER(c_size_t),
]
DLL.Cipher_decrypt.restype = c_int

DLL.Cipher_free.argtypes = [c_void_p]

DLL.u8_free.argtypes = [POINTER(c_ubyte), c_size_t]
DLL.u8_free.restype = None

DLL.Hasher_new.argtypes = [c_int]
DLL.Hasher_new.restype = c_void_p

DLL.Hasher_update.argtypes = [c_void_p, POINTER(c_ubyte), c_size_t]
DLL.Hasher_update.restype = c_int

DLL.Hasher_finalize.argtypes = [c_void_p, POINTER(POINTER(c_ubyte)), POINTER(c_size_t)]
DLL.Hasher_finalize.restype = c_int

DLL.Hasher_free.argtypes = [c_void_p]

DLL.Hmac_new.argtypes = [POINTER(c_ubyte), c_size_t, c_int]
DLL.Hmac_new.restype = c_void_p

DLL.Hmac_update.argtypes = [c_void_p, POINTER(c_ubyte), c_size_t]
DLL.Hmac_update.restype = c_int

DLL.Hmac_finalize.argtypes = [c_void_p, POINTER(POINTER(c_ubyte)), POINTER(c_size_t)]
DLL.Hmac_finalize.restype = c_int

DLL.Hmac_free.argtypes = [c_void_p]

DLL.Base64_new.argtypes = []
DLL.Base64_new.restype = c_void_p

DLL.Base64_encode.argtypes = [c_void_p, POINTER(c_ubyte), c_size_t]
DLL.Base64_encode.restype = c_void_p

DLL.Base64_decode.argtypes = [c_void_p, POINTER(c_ubyte), c_size_t, POINTER(POINTER(c_ubyte)), POINTER(c_size_t)]
DLL.Base64_decode.restype = int

DLL.Base64_free.argtypes = [c_void_p]

DLL.url_encode.argtypes = [c_char_p]
DLL.url_encode.restype = c_void_p

DLL.url_decode.argtypes = [c_char_p]
DLL.url_decode.restype = c_void_p

DLL.hex_encode.argtypes = [POINTER(c_ubyte), c_size_t]
DLL.hex_encode.restype = c_void_p

DLL.hex_decode.argtypes = [c_char_p, POINTER(POINTER(c_ubyte)), POINTER(c_size_t)]
DLL.hex_decode.restype = int
