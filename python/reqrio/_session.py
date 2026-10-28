from ctypes import *

import reqrio
from reqrio import util
from reqrio.alpn import ALPN
from reqrio.bindings import DLL
from reqrio.http import Method
from reqrio.response import Response
from reqrio._finger import Fingerprint
from reqrio.url import Url
from typing import Union

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

DLL.ScReq_send.argtypes = [c_void_p, c_int, c_void_p, c_void_p, POINTER(c_char_p)]
DLL.ScReq_send.restype = c_uint64

DLL.ScReq_recv.argtypes = [c_void_p, c_uint64, c_bool, POINTER(c_char_p)]
DLL.ScReq_recv.restype = POINTER(reqrio.response.RespInner)

# DLL.ScReq_do_http.argtypes = [c_void_p, c_int, c_void_p, c_void_p, c_bool, POINTER(c_char_p)]
# DLL.ScReq_do_http.restype = POINTER(reqrio.response.RespInner)


class Session:
    def __init__(
            self,
            headers: dict = None,
            alpn: str = ALPN.HTTP11,
            verify: bool = True,
            proxy: str = None,
            key_log: str = None,
            fingerprint: Fingerprint = None,
            ignore_hdr_sort: bool = False,

    ):
        """
        :param
        :param alpn: HTTP版本
        :param verify: 是否进行证书链验证
        :param proxy: 格式:http://127.0.0.1:10000、socks5://127.0.0.1:10001、socks5://username@password127.0.0.1:10001
        :param key_log: 导出TLS握手密钥，可用于Wireshark抓包分析
        :param ignore_hdr_sort: 忽略内置请求头顺序
        """

        self.dll = DLL
        # alpn
        self.hid = self.dll.ScReq_new(ignore_hdr_sort)
        err = self.dll.ScReq_set_alpn(self.hid, alpn.encode('utf-8'))
        err, msg = util.check_char_err(err)
        if err: raise Exception(msg)
        # verify
        err, msg = util.check_char_err(self.dll.ScReq_set_verify(self.hid, verify))
        if err: raise Exception(msg)
        # proxy
        if proxy is not None:
            err, msg = util.check_char_err(self.dll.ScReq_set_proxy(self.hid, proxy.encode('utf-8')))
            if err: raise Exception(msg)
        # keylog
        if key_log is not None:
            err, msg = util.check_char_err(self.dll.ScReq_set_key_log(self.hid, key_log.encode("utf-8")))
            if err: raise Exception(msg)
        if headers is not None:
            self.set_headers(headers)
        if fingerprint is not None:
            self.set_fingerprint(fingerprint)

    def set_timeout(self, connect: int = 3000, read: int = 3000, write: int = 3000, handle: int = 30000,
                    connect_times: int = 3, handle_times: int = 3):
        """
        :param connect: 连接超时,默认3s
        :param read: tcp读取超时,默认3s
        :param write: tcp写出超时,默认3s
        :param handle: 发包处理超时,默认30s
        :param connect_times: 尝试连接次数,默认3次
        :param handle_times:尝试处理次数,默认3次
        :return:
        """
        timeout = {
            'connect': connect,
            'read': read,
            'write': write,
            'handle': handle,
            'connect_times': connect_times,
            'handle_times': handle_times,
        }
        import json
        err, msg = util.check_char_err(self.dll.ScReq_set_timeout(self.hid, json.dumps(timeout).encode('utf-8')))
        if err: raise Exception(msg)
        return

    def set_headers(self, headers: dict):
        import json
        err, msg = util.check_char_err(self.dll.ScReq_set_header_json(self.hid, json.dumps(headers).encode('utf-8')))
        if err: raise Exception(msg)
        return

    def add_header(self, name: str, value: str, _reversed=False):
        err = self.dll.ScReq_add_header(self.hid, name.encode('utf-8'), value.encode('utf-8'), _reversed)
        err, msg = util.check_char_err(err)
        if err: raise Exception(msg)

    def remove_header(self, name: str):
        err, msg = util.check_char_err(self.dll.ScReq_remove_header(self.hid, name.encode('utf-8')))
        if err: raise Exception(msg);

    def set_fingerprint(self, fingerprint: Fingerprint):
        err = self.dll.ScReq_set_fingerprint(self.hid, fingerprint.take())
        err, msg = util.check_char_err(err)
        if err: raise Exception(msg)

    def set_cookie(self, cookie: str):
        err, msg = util.check_char_err(self.dll.ScReq_set_cookie(self.hid, cookie.encode('utf-8')))
        if err: raise Exception(msg)

    def add_cookie(self, name: str, value: str):
        err, msg = util.check_char_err(self.dll.ScReq_add_cookie(self.hid, name.encode('utf-8'), value.encode('utf-8')))
        if err: raise Exception(msg)

    def send_inner(
            self,
            method: Method,
            url: Url,
            body: c_void_p,
            auto_redirect: bool = True,
    ) -> int:
        """
        :param method: 请求方法
        :param url: 请求地址
        :param body: 请求体
        :param params: 请求参数
        :param auto_redirect: 是否对重定向链接进行自动跳转，默认是
        :param sni: 域名，使用ip url时设置
        :return:
        """
        try:
            err, msg = util.check_char_err(self.dll.ScReq_set_redirect(self.hid, auto_redirect))
            if err: raise Exception(msg)
            err = c_char_p()
            sid = self.dll.ScReq_send(self.hid, method, url.take(), body, byref(err))
            body = None
            err, msg = util.check_char_err(err)
            if err: raise Exception(msg)
            return sid
        finally:
            self.dll.Body_drop(body)

    def pre_send(
            self,
            method: Method,
            url: Union[Url, str],
            params: dict = None,
            data: dict = None,
            json: dict = None,
            bytes: bytes = None,
            text: str = None,
            sni: str = None,
            files: list[dict[str, str]] = None,
            content_type: str = None,
            **kwargs) -> int:
        if files is not None:
            http_file = self.dll.HttpFile_new()
            for file in files:
                try:
                    err = c_char_p()
                    path = file["path"].encode('utf-8')
                    field_name = file["field_name"].encode('utf-8')
                    file_type = file["filetype"].encode('utf-8')
                    form = self.dll.FileForm_new(path, field_name, file_type, byref(err))
                    err, msg = util.check_char_err(err)
                    if err: raise Exception(msg)
                    err, msg = util.check_char_err(self.dll.HttpFile_add_form(http_file, form))
                    if err: raise Exception(msg)
                except Exception as e:
                    self.dll.HttpFile_drop(http_file)
                    raise e
            if data is not None:
                import json
                data = json.dumps(data).encode('utf-8')
            else:
                data = '{}'.encode('utf-8')
            err = c_char_p()
            body = self.dll.Body_new_files(http_file, data, byref(err))
        elif data is not None:
            bl, u8 = util.bytes_to_u8(util.urlencoded_str(data).encode('utf-8'))
            err = c_char_p()
            if content_type is None:
                content_type = 'application/x-www-form-urlencoded'
            body = self.dll.Body_new(u8, bl, content_type.encode('utf-8'), byref(err))
        elif json is not None:
            import json as j
            bl, u8 = util.str_to_u8(j.dumps(json))
            if content_type is None:
                content_type = 'application/json'
            err = c_char_p()
            body = self.dll.Body_new(u8, bl, content_type.encode('utf-8'), byref(err))
        elif text is not None:
            bl, u8 = util.str_to_u8(text)
            if content_type is None:
                content_type = 'text/plain'
            err = c_char_p()
            body = self.dll.Body_new(u8, bl, content_type.encode('utf-8'), byref(err))
        elif bytes is not None:
            bl, u8 = util.bytes_to_u8(bytes)
            if content_type is None:
                content_type = 'application/octet-stream'
            err = c_char_p()
            body = self.dll.Body_new(u8, bl, content_type.encode('utf-8'), byref(err))
        else:
            err = c_char_p()
            body = self.dll.Body_none()
        err, msg = util.check_char_err(err)
        if err: raise Exception(msg)
        if type(url) == str:
            url = Url(url, params, sni)
        else:
            if params is not None:
                url.set_params(params)
            if sni is not None:
                url.set_sni(sni)

        return self.send_inner(method, url, body, **kwargs)

    def recv(self, sid: int, stream: bool):
        err = c_char_p()
        resp = self.dll.ScReq_recv(self.hid, sid, stream, byref(err))
        err, msg = util.check_char_err(err)
        if err: raise Exception(msg)
        return Response(resp, self.hid, stream)

    def send(self, method: Method, url: Union[str, Url], params: dict = None, data: dict = None, json: dict = None,
             bytes: bytes = None, text: str = None, **kwargs):
        return self.pre_send(method, url, params, data, json, bytes, text, **kwargs)

    def get(self, url: Union[str, Url], params: dict = None, stream: bool = False, **kwargs) -> Response:
        return self.recv(self.send(Method.GET, url, params, **kwargs), stream)

    def post(self, url: Union[str, Url], data: dict = None, json: dict = None, bytes: bytes = None,
             text: str = None, stream: bool = False, **kwargs) -> Response:
        return self.recv(self.send(Method.POST, url, data=data, json=json, bytes=bytes, text=text, **kwargs), stream)

    def put(self, url: Union[str, Url], params: dict = None, data: dict = None, json: dict = None,
            bytes: bytes = None, text: str = None, stream: bool = False, **kwargs) -> Response:
        return self.recv(self.send(Method.PUT, url, params, data, json, bytes, text, **kwargs), stream)

    def head(self, url: Union[str, Url], params: dict = None, data: dict = None, json: dict = None,
             bytes: bytes = None, text: str = None, stream: bool = False, **kwargs) -> Response:
        return self.recv(self.send(Method.HEAD, url, params, data, json, bytes, text, **kwargs), stream)

    def delete(self, url: Union[str, Url], params: dict = None, data: dict = None, json: dict = None,
               bytes: bytes = None, text: str = None, stream: bool = False, **kwargs) -> Response:
        return self.recv(self.send(Method.DELETE, url, params, data, json, bytes, text, **kwargs), stream)

    def options(self, url: Union[str, Url], params: dict = None, data: dict = None, json: dict = None,
                bytes: bytes = None, text: str = None, stream: bool = False, **kwargs) -> Response:
        return self.recv(self.send(Method.OPTIONS, url, params, data, json, bytes, text, **kwargs), stream)

    def trace(self, url: Union[str, Url], params: dict = None, data: dict = None, json: dict = None,
              bytes: bytes = None, text: str = None, stream: bool = False, **kwargs) -> Response:
        return self.recv(self.send(Method.TRACE, url, params, data, json, bytes, text, **kwargs), stream)

    def patch(self, url: Union[str, Url], params: dict = None, data: dict = None, json: dict = None,
              bytes: bytes = None, text: str = None, stream: bool = False, **kwargs) -> Response:
        return self.recv(self.send(Method.PATCH, url, params, data, json, bytes, text, **kwargs), stream)

    def query(self, url: Union[str, Url], params: dict = None, data: dict = None, json: dict = None,
              bytes: bytes = None, text: str = None, stream: bool = False, **kwargs) -> Response:
        return self.recv(self.send(Method.QUERY, url, params, data, json, bytes, text, **kwargs), stream)

    def reconnect(self):
        err, msg = util.check_char_err(self.dll.ScReq_reconnect(self.hid))
        if err: raise Exception(msg)

    def connect(self, url: str, sni: str = None):
        sni_bs = sni
        if sni is not None:
            sni_bs = sni.encode('utf-8')
        err, msg = util.check_char_err(self.dll.ScReq_connect(self.hid, url.encode('utf-8'), sni_bs))
        if err: raise Exception(msg)

    def close_stream(self):
        err, msg = util.check_char_err(self.dll.ScReq_close_stream(self.hid))
        if err: raise Exception(msg)

    def close(self):
        """记得关闭资源，否则容易造成内存溢出"""
        self.dll.ScReq_drop(self.hid)
        self.hid = None

    def __del__(self):
        if hasattr(self, 'dll') and self.dll:
            self.dll.ScReq_drop(self.hid)
            self.hid = None
