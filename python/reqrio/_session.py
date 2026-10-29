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

DLL.ScReq_do_http.argtypes = [c_void_p, c_int, c_void_p, c_void_p, POINTER(c_char_p)]
DLL.ScReq_do_http.restype = POINTER(reqrio.response.RespInner)


class Session:
    """Reusable HTTP client session for sending and receiving requests."""

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
        """Create a session.

        Args:
            headers: default request headers.
            alpn: negotiated ALPN value.
            verify: whether to validate certificates.
            proxy: proxy URL such as http://127.0.0.1:10000.
            key_log: TLS key log path.
            fingerprint: TLS fingerprint configuration.
            ignore_hdr_sort: whether to skip built-in header ordering.
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
        """Set connection and request timeout values.

        Args:
            connect: connect timeout in milliseconds.
            read: read timeout in milliseconds.
            write: write timeout in milliseconds.
            handle: request handle timeout in milliseconds.
            connect_times: retry count for connection attempts.
            handle_times: retry count for request handling.
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
        """Replace all request headers.

        Args:
            headers: header name/value mapping.
        """
        import json
        err, msg = util.check_char_err(self.dll.ScReq_set_header_json(self.hid, json.dumps(headers).encode('utf-8')))
        if err: raise Exception(msg)
        return

    def add_header(self, name: str, value: str, _reversed=False):
        """Add or overwrite a request header.

        Args:
            name: header name.
            value: header value.
            _reversed: whether to reverse the addition order.
        """
        err = self.dll.ScReq_add_header(self.hid, name.encode('utf-8'), value.encode('utf-8'), _reversed)
        err, msg = util.check_char_err(err)
        if err: raise Exception(msg)

    def remove_header(self, name: str):
        """Remove a request header by name.

        Args:
            name: header name.
        """
        err, msg = util.check_char_err(self.dll.ScReq_remove_header(self.hid, name.encode('utf-8')))
        if err: raise Exception(msg);

    def set_fingerprint(self, fingerprint: Fingerprint):
        """Set the TLS fingerprint used by this session.

        Args:
            fingerprint: fingerprint object.
        """
        err = self.dll.ScReq_set_fingerprint(self.hid, fingerprint.take())
        err, msg = util.check_char_err(err)
        if err: raise Exception(msg)

    def set_cookie(self, cookie: str):
        """Set the Cookie header.

        Args:
            cookie: cookie string.
        """
        err, msg = util.check_char_err(self.dll.ScReq_set_cookie(self.hid, cookie.encode('utf-8')))
        if err: raise Exception(msg)

    def add_cookie(self, name: str, value: str):
        """Add a single cookie.

        Args:
            name: cookie name.
            value: cookie value.
        """
        err, msg = util.check_char_err(self.dll.ScReq_add_cookie(self.hid, name.encode('utf-8'), value.encode('utf-8')))
        if err: raise Exception(msg)

    def __build_body(
            self,
            data: dict = None,
            json: dict = None,
            bytes: bytes = None,
            text: str = None,
            files: list[dict[str, str]] = None,
            content_type: str = None,
    ):
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
        return body

    def recv(self, sid: int, stream: bool):
        """Receive a response for a request id.

        Args:
            sid: request id.
            stream: whether to read the response as a stream.
        """
        err = c_char_p()
        resp = self.dll.ScReq_recv(self.hid, sid, stream, byref(err))
        err, msg = util.check_char_err(err)
        if err: raise Exception(msg)
        return Response(resp, self.hid, stream)

    def send(
            self,
            method: Method,
            url: Union[str, Url],
            params: dict = None,
            data: dict = None,
            json: dict = None,
            bytes: bytes = None,
            text: str = None,
            files: list[dict[str, str]] = None,
            content_type: str = None,
            auto_redirect: bool = True,
            sni: str = None,
            call_func=DLL.ScReq_send,
    ) -> Union[int, reqrio.response.RespInner]:
        """Send a request and return the request id."""
        if type(url) == str:
            url = Url(url, params, sni)
        else:
            if params is not None:
                url.set_params(params)
            if sni is not None:
                url.set_sni(sni)
        body = self.__build_body(data, json, bytes, text, files, content_type)
        try:
            err, msg = util.check_char_err(self.dll.ScReq_set_redirect(self.hid, auto_redirect))
            if err: raise Exception(msg)
            err = c_char_p()
            res = call_func(self.hid, method, url.take(), body, byref(err))
            body = None
            err, msg = util.check_char_err(err)
            if err: raise Exception(msg)
            return res
        finally:
            self.dll.Body_drop(body)

    def __request(self, method: Method, url: Union[str, Url], params: dict = None, stream: bool = False, **kwargs):
        if stream:
            sid = self.send(Method.GET, url, params, **kwargs)
            return self.recv(sid, stream)
        else:
            resp = self.send(method, url, params, call_func=self.dll.ScReq_do_http, **kwargs)
            return Response(resp, self.hid, stream)

    def get(self, url: Union[str, Url], params: dict = None, stream: bool = False, **kwargs) -> Response:
        """Send a GET request and return the response."""
        return self.__request(Method.GET, url, params, stream=stream, **kwargs)

    def post(self, url: Union[str, Url], data: dict = None, json: dict = None, bytes: bytes = None,
             text: str = None, stream: bool = False, **kwargs) -> Response:
        """Send a POST request and return the response."""
        return self.__request(Method.POST, url, data=data, json=json, bytes=bytes, text=text, stream=stream, **kwargs)

    def put(self, url: Union[str, Url], params: dict = None, data: dict = None, json: dict = None,
            bytes: bytes = None, text: str = None, stream: bool = False, **kwargs) -> Response:
        """Send a PUT request and return the response."""
        return self.__request(Method.PUT, url, data=data, json=json, bytes=bytes, text=text, stream=stream, **kwargs)

    def head(self, url: Union[str, Url], params: dict = None, data: dict = None, json: dict = None,
             bytes: bytes = None, text: str = None, stream: bool = False, **kwargs) -> Response:
        """Send a HEAD request and return the response."""
        return self.__request(Method.HEAD, url, data=data, json=json, bytes=bytes, text=text, stream=stream, **kwargs)

    def delete(self, url: Union[str, Url], params: dict = None, data: dict = None, json: dict = None,
               bytes: bytes = None, text: str = None, stream: bool = False, **kwargs) -> Response:
        """Send a DELETE request and return the response."""
        return self.__request(Method.DELETE, url, data=data, json=json, bytes=bytes, text=text, stream=stream, **kwargs)

    def options(self, url: Union[str, Url], params: dict = None, data: dict = None, json: dict = None,
                bytes: bytes = None, text: str = None, stream: bool = False, **kwargs) -> Response:
        """Send an OPTIONS request and return the response."""
        return self.__request(Method.OPTIONS, url, data=data, json=json, bytes=bytes, text=text, stream=stream, **kwargs)

    def trace(self, url: Union[str, Url], params: dict = None, data: dict = None, json: dict = None,
              bytes: bytes = None, text: str = None, stream: bool = False, **kwargs) -> Response:
        """Send a TRACE request and return the response."""
        return self.__request(Method.TRACE, url, data=data, json=json, bytes=bytes, text=text, stream=stream, **kwargs)

    def patch(self, url: Union[str, Url], params: dict = None, data: dict = None, json: dict = None,
              bytes: bytes = None, text: str = None, stream: bool = False, **kwargs) -> Response:
        """Send a PATCH request and return the response."""
        return self.__request(Method.PATCH, url, data=data, json=json, bytes=bytes, text=text, stream=stream, **kwargs)

    def query(self, url: Union[str, Url], params: dict = None, data: dict = None, json: dict = None,
              bytes: bytes = None, text: str = None, stream: bool = False, **kwargs) -> Response:
        """Send a custom QUERY request and return the response."""
        return self.__request(Method.QUERY, url, data=data, json=json, bytes=bytes, text=text, stream=stream, **kwargs)

    def reconnect(self):
        """Reconnect the session."""
        err, msg = util.check_char_err(self.dll.ScReq_reconnect(self.hid))
        if err: raise Exception(msg)

    def connect(self, url: str, sni: str = None):
        """Open a connection without sending an HTTP request.

        Args:
            url: target URL.
            sni: custom SNI host.
        """
        sni_bs = sni
        if sni is not None:
            sni_bs = sni.encode('utf-8')
        err, msg = util.check_char_err(self.dll.ScReq_connect(self.hid, url.encode('utf-8'), sni_bs))
        if err: raise Exception(msg)

    def close_stream(self):
        """Close the active TLS stream."""
        err, msg = util.check_char_err(self.dll.ScReq_close_stream(self.hid))
        if err: raise Exception(msg)

    def close(self):
        """Release native session resources."""
        self.dll.ScReq_drop(self.hid)
        self.hid = None

    def __del__(self):
        if hasattr(self, 'dll') and self.dll:
            self.dll.ScReq_drop(self.hid)
            self.hid = None
