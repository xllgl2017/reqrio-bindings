import ctypes
import json
from typing import Dict

from reqrio.bindings import DLL
from reqrio import util
from reqrio.stream import StreamChunk
from reqrio.buffer import Buffer, Buf

# ==========================>Response<=============================

DLL.Response_status_code.argtypes = [ctypes.c_void_p, ctypes.POINTER(ctypes.c_char_p)]
DLL.Response_status_code.restype = ctypes.c_uint16

DLL.Response_bytes.argtypes = [ctypes.c_void_p, ctypes.POINTER(ctypes.c_size_t), ctypes.POINTER(ctypes.c_char_p)]
DLL.Response_bytes.restype = ctypes.POINTER(ctypes.c_ubyte)

DLL.Response_get_header.argtypes = [ctypes.c_void_p, ctypes.c_char_p, ctypes.POINTER(ctypes.c_char_p)]
DLL.Response_get_header.restype = ctypes.c_void_p

DLL.Response_cookies.argtypes = [ctypes.c_void_p, ctypes.POINTER(ctypes.c_char_p)]
DLL.Response_cookies.restype = ctypes.c_void_p

DLL.Response_sid.argtypes = [ctypes.c_void_p, ctypes.POINTER(ctypes.c_char_p)]
DLL.Response_sid.restype = ctypes.c_uint64

DLL.Response_drop.argtypes = [ctypes.c_void_p]


class RespInner(ctypes.Structure):
    sid: int
    method: int
    status: int
    alpn: Buf
    body: Buffer
    _fields_ = [
        ("sid", ctypes.c_uint64),
        ("status", ctypes.c_uint16),
        ("alpn", Buf),
        ("body", Buffer)
    ]


class HdrInner(Dict):
    def __init__(self, resp):
        super().__init__()
        self.resp = resp

    def __getitem__(self, key):
        return self.resp.get_header(key)

    def get(self, key, default: None = None):
        return self.resp.get_header(key, default)

    def __str__(self) -> str:
        return f'Headers{self.resp}'


class Response:
    """HTTP response wrapper returned by the session."""

    sid: int
    method: int
    status_code: int
    alpn: Buf
    body: Buffer
    text: str
    headers: HdrInner

    def __init__(self, ptr, req, stream=False):
        """Create a response object from a native pointer.

        Args:
            ptr: native response pointer.
            req: native session pointer.
            stream: whether the response is streamed.
        """
        self.__ptr__ = ptr
        self.__inner__ = self.__ptr__.contents
        self.__req__ = req
        self.__req_free__ = False
        self.__read_stream__ = stream

    def __getattr__(self, item):
        if item == "alpn":
            return self.__inner__.alpn
        elif item == "body":
            return self.__inner__.body
        elif item == "status_code":
            return self.__inner__.status
        elif item == "sid":
            return self.__inner__.sid
        elif item == "method":
            return self.__inner__.method
        elif item == 'text':
            return self.bytes().decode('utf-8')
        elif item == 'headers':
            return HdrInner(self)

        return None

    def __del__(self):
        if hasattr(self, '__ptr__') and self.__ptr__ and not self.__read_stream__:
            DLL.Response_drop(self.__ptr__)
            self.__ptr__ = None
        if hasattr(self, '__req__') and self.__req__ and self.__req_free__:
            DLL.ScReq_drop(self.__req__)
            self.__req__ = None

    def get_header(self, name: str, default: str = None) -> str:
        """Get a response header value.

        Args:
            name: header name.
            default: fallback value when header is missing.
        """
        err = ctypes.c_char_p()
        ptr = DLL.Response_get_header(self.__ptr__, name.encode('utf-8'), ctypes.byref(err))
        err, msg = util.check_char_err(err)
        if err and 'not found' in msg and default is not None:
            return default
        elif err:
            raise Exception(msg)
        res = ctypes.cast(ptr, ctypes.c_char_p).value.decode('utf-8')
        DLL.char_free(ptr)
        return res

    def location(self) -> str:
        """Return the Location header value."""
        return self.get_header("location")

    def cookies(self):
        """Return cookies included in the response."""
        err = ctypes.c_char_p()
        ptr = DLL.Response_cookies(self.__ptr__, ctypes.byref(err))
        err, msg = util.check_char_err(err)
        if err: raise Exception(msg)
        res = ctypes.cast(ptr, ctypes.c_char_p).value
        if res is None:
            DLL.char_free(ptr)
            return []
        else:
            res = json.loads(res)
            return res

    def bytes(self) -> bytes:
        """Return the raw response body bytes."""
        return self.body.as_bytes()

    def json(self) -> dict:
        """Parse the response body as JSON."""
        return json.loads(self.bytes())

    def chunks(self) -> StreamChunk:
        """Return a stream chunk iterator for this response."""
        err = ctypes.c_char_p()
        sid = DLL.Response_sid(self.__ptr__, ctypes.byref(err))
        err, msg = util.check_char_err(err)
        if err: raise Exception(msg)
        chunk = StreamChunk(sid, self.__req__)
        return chunk
