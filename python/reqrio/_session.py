from ctypes import *

from reqrio import util
from reqrio.alpn import ALPN
from reqrio.bindings import DLL
from reqrio.method import Method
from reqrio.response import Response


class Session:
    def __init__(
            self,
            headers: dict = None,
            alpn: str = ALPN.HTTP11,
            verify: bool = True,
            proxy: str = None,
            key_log: str = None,
            ja3: str = None,
            ja4: str = None,
            client_hello: bytes = None,
            random_tls: bool = False,
            custom_tls: dict = None,
            token: str = "",
            ignore_hdr_sort: bool = False
    ):
        """
        :param
        :param alpn: HTTP版本
        :param verify: 是否进行证书链验证
        :param proxy: 格式:http://127.0.0.1:10000、socks5://127.0.0.1:10001、socks5://username@password127.0.0.1:10001
        :param key_log: 导出TLS握手密钥，可用于Wireshark抓包分析
        :param ja3: 使用ja3设置指纹
        :param ja4: 使用ja4设置指纹
        :param client_hello: 使用client_hello数据设置指纹
        :param random_tls: 使用随机指纹
        :param custom_tls: 使用自定义指纹，具体参数参阅https://github.com/xllgl2017/reqrio
        :param token: 修改指纹时所有的认证token
        :param ignore_hdr_sort: 忽略内置请求头顺序
        """

        self.dll = DLL
        # alpn
        self.hid = self.dll.ScReq_new(ignore_hdr_sort)
        err = self.dll.ScReq_set_alpn(self.hid, alpn.encode('utf-8'))
        err, msg = util.check_char_err(err)
        if err: raise Exception(msg)
        # verify
        self._set_fingerprint(random_tls, ja3, ja4, client_hello, custom_tls, token)
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

    def _set_fingerprint(
            self,
            random: bool = None,
            ja3: str = None,
            ja4: str = None,
            client_hello: bytes = None,
            custom: dict = None,
            token: str = ""
    ):
        err = c_char_p()
        if ja3 is not None:
            fingerprint = self.dll.Fingerprint_from_ja3(ja3.encode('utf-8'), token.encode('utf-8'), byref(err))
            err, msg = util.check_char_err(err)
            if err: raise Exception(msg)
        elif ja4 is not None:
            fingerprint = self.dll.Fingerprint_from_ja4(ja4.encode('utf-8'), token.encode('utf-8'), byref(err))
            err, msg = util.check_char_err(err)
            if err: raise Exception(msg)
        elif client_hello is not None:
            dl, data = util.bytes_to_u8(client_hello)
            fingerprint = self.dll.Fingerprint_from_client_hello(data, dl, token.encode('utf-8'), byref(err))
            err, msg = util.check_char_err(err)
            if err: raise Exception(msg)
        elif random:
            fingerprint = self.dll.Fingerprint_random(token.encode('utf-8'), byref(err))
            err, msg = util.check_char_err(err)
            if err: raise Exception(msg)
        elif custom is not None:
            import json
            custom = json.dumps(custom)
            fingerprint = self.dll.Fingerprint_custom(custom.encode('utf-8'), token.encode('utf-8'), byref(err))
            err, msg = util.check_char_err(err)
            if err: raise Exception(msg)
        else:
            return
        err, msg = util.check_char_err(self.dll.ScReq_set_fingerprint(self.hid, fingerprint))
        if err: raise Exception(msg)

    def set_cookie(self, cookie: str):
        err, msg = util.check_char_err(self.dll.ScReq_set_cookie(self.hid, cookie.encode('utf-8')))
        if err: raise Exception(msg)

    def add_cookie(self, name: str, value: str):
        err, msg = util.check_char_err(self.dll.ScReq_add_cookie(self.hid, name.encode('utf-8'), value.encode('utf-8')))
        if err: raise Exception(msg)

    def send_request(
            self,
            method: Method,
            url: str,
            body: c_void_p,
            params: dict = None,
            auto_redirect: bool = True,
            sni: str = None,
            stream: bool = False,
    ) -> Response:
        """
        :param method: 请求方法
        :param url: 请求地址
        :param body: 请求体
        :param params: 请求参数
        :param auto_redirect: 是否对重定向链接进行自动跳转，默认是
        :param sni: 域名，使用ip url时设置
        :param stream: 流式请求
        :return:
        """
        try:
            err, msg = util.check_char_err(self.dll.ScReq_set_redirect(self.hid, auto_redirect))
            if err: raise Exception(msg)
            # url
            err = c_char_p()
            url = self.dll.Url_new(url.encode('utf-8'), byref(err))
            err, msg = util.check_char_err(err)
            if sni is not None:
                err, msg = util.check_char_err(self.dll.Url_set_sni(url, sni.encode("utf-8")))
                if err: raise Exception(msg)
            if err: raise Exception(msg)
            if params is not None:
                for name in params.keys():
                    value = str(params[name])
                    err, msg = util.check_char_err(
                        self.dll.Url_add_param(url, name.encode('utf-8'), value.encode('utf-8')))
                    if err: raise Exception(msg)
            err = c_char_p()
            resp = self.dll.ScReq_do_http(self.hid, method.value, url, body, stream, byref(err))
            url = None
            body = None
            err, msg = util.check_char_err(err)
            if err: raise Exception(msg)
            return Response(resp, self.hid)
        finally:
            if type(url) == int:
                self.dll.Url_drop(url)
            self.dll.Body_drop(body)

    def pre_send(
            self,
            method: Method,
            url: str,
            params: dict = None,
            data: dict = None,
            json: dict = None,
            bytes: bytes = None,
            text: str = None,
            files: list[dict[str, str]] = None,
            content_type: str = None,
            **kwargs):
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
        return self.send_request(method, url, body, params, **kwargs)

    def get(self, url: str, params: dict = None, data: dict = None, json: dict = None,
            bytes: bytes = None, text: str = None, **kwargs) -> Response:
        return self.pre_send(Method.GET, url, params, data, json, bytes, text, **kwargs)

    def post(self, url: str, params: dict = None, data: dict = None, json: dict = None,
             bytes: bytes = None, text: str = None, **kwargs) -> Response:
        return self.pre_send(Method.POST, url, params, data, json, bytes, text, **kwargs)

    def put(self, url: str, params: dict = None, data: dict = None, json: dict = None,
            bytes: bytes = None, text: str = None, **kwargs) -> Response:
        return self.pre_send(Method.PUT, url, params, data, json, bytes, text, **kwargs)

    def head(self, url: str, params: dict = None, data: dict = None, json: dict = None,
             bytes: bytes = None, text: str = None, **kwargs) -> Response:
        return self.pre_send(Method.HEAD, url, params, data, json, bytes, text, **kwargs)

    def delete(self, url: str, params: dict = None, data: dict = None, json: dict = None,
               bytes: bytes = None, text: str = None, **kwargs) -> Response:
        return self.pre_send(Method.DELETE, url, params, data, json, bytes, text, **kwargs)

    def options(self, url: str, params: dict = None, data: dict = None, json: dict = None,
                bytes: bytes = None, text: str = None, **kwargs) -> Response:
        return self.pre_send(Method.OPTIONS, url, params, data, json, bytes, text, **kwargs)

    def trace(self, url: str, params: dict = None, data: dict = None, json: dict = None,
              bytes: bytes = None, text: str = None, **kwargs) -> Response:
        return self.pre_send(Method.TRACE, url, params, data, json, bytes, text, **kwargs)

    def patch(self, url: str, params: dict = None, data: dict = None, json: dict = None,
              bytes: bytes = None, text: str = None, **kwargs) -> Response:
        return self.pre_send(Method.PATCH, url, params, data, json, bytes, text, **kwargs)

    def query(self, url: str, params: dict = None, data: dict = None, json: dict = None,
              bytes: bytes = None, text: str = None, **kwargs) -> Response:
        return self.pre_send(Method.QUERY, url, params, data, json, bytes, text, **kwargs)

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

    # def open_stream(self, method: Method, url: str, params: dict = None, data: dict = None, json: dict = None,
    #                 bs: bytes = None, content_type: str = None):
    #     from reqrio.stream import Stream
    #     return Stream(self, method, url, params, data, json, bs, content_type)

    def close(self):
        """记得关闭资源，否则容易造成内存溢出"""
        self.dll.ScReq_drop(self.hid)
        self.hid = None

    def __del__(self):
        if hasattr(self, 'dll') and self.dll:
            self.dll.ScReq_drop(self.hid)
            self.hid = None
