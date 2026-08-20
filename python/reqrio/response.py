import ctypes
import json
from ctypes import c_char_p, byref, c_size_t

from reqrio import util
from reqrio.bindings import DLL
from reqrio.stream import StreamChunk


class Response:
    def __init__(self, resp: ctypes.c_void_p, req):
        self.raw = resp
        self.req = req
        self.req_free = False
        return

    def statue_code(self) -> int:
        err = c_char_p()
        code = DLL.Response_status_code(self.raw, byref(err))
        err, msg = util.check_char_err(err)
        if err: raise Exception(msg)
        return code

    def get_header(self, name: str, default: str = None) -> str:
        err = c_char_p()
        ptr = DLL.Response_get_header(self.raw, name.encode('utf-8'), byref(err))
        err, msg = util.check_char_err(err)
        if err and 'not found' in msg and default is not None:
            return default
        elif err:
            raise Exception(msg)
        res = ctypes.cast(ptr, c_char_p).value.decode('utf-8')
        DLL.char_free(ptr)
        return res

    def location(self) -> str:
        return self.get_header("location")

    def cookies(self):
        err = c_char_p()
        ptr = DLL.Response_cookies(self.raw, byref(err))
        err, msg = util.check_char_err(err)
        if err: raise Exception(msg)
        res = ctypes.cast(ptr, c_char_p).value
        if res is None:
            DLL.char_free(ptr)
            return []
        else:
            res = json.loads(res)
            return res

    def bytes(self) -> bytes:
        err = c_char_p()
        len = c_size_t()
        ptr = DLL.Response_bytes(self.raw, byref(len), byref(err))
        err, msg = util.check_char_err(err)
        if err: raise Exception(msg)
        res = ctypes.string_at(ptr, len.value)
        return res

    def json(self) -> dict:
        return json.loads(self.bytes())

    def text(self) -> str:
        return self.bytes().decode('utf-8')

    def chunks(self) -> StreamChunk:
        err = c_char_p()
        sid = DLL.Response_sid(self.raw, byref(err))
        err, msg = util.check_char_err(err)
        if err: raise Exception(msg)
        chunk = StreamChunk(sid, self.req)
        return chunk

    def __del__(self):
        if hasattr(self, 'raw') and self.raw:
            DLL.Response_drop(self.raw)
            self.raw = None
        if hasattr(self, 'req') and self.req and self.req_free:
            DLL.ScReq_drop(self.req)
            self.req = None
