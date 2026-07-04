from _ctypes import addressof, byref
from ctypes import c_ubyte, string_at, POINTER, c_size_t

from reqrio.bindings import DLL
from reqrio import util
from typing import Union


class Base64:
    def __init__(self):
        self.b64 = DLL.Base64_new()

    def encode(self, data: Union[str, bytes]) -> str:
        data_len, data_u8 = util.str_bytes_to_u8(data)
        ptr = DLL.Base64_encode(self.b64, data_u8, data_len)
        if ptr is None:
            raise Exception("base64 encode error")
        bs = string_at(ptr).decode('utf-8')
        DLL.char_free(ptr)
        return bs

    def decode(self, data: str) -> bytes:
        data_len, data_u8 = util.str_to_u8(data)
        out_ptr = POINTER(c_ubyte)()
        out_len = c_size_t()
        ret = DLL.Base64_decode(self.b64, data_u8, data_len, byref(out_ptr), byref(out_len))
        try:
            if ret == -1:
                raise Exception("base64 decode error")
            array_bytes = c_ubyte * out_len.value
            byte_array = array_bytes.from_address(addressof(out_ptr.contents))
            return bytes(byte_array)
        finally:
            DLL.u8_free(out_ptr, out_len)

    def __del__(self):
        if hasattr(self, 'b64') and self.b64:
            DLL.Base64_free(self.b64)
            self.b64 = None


def b64encode(data: Union[str, bytes]) -> str:
    b64 = Base64()
    return b64.encode(data)


def b64decode(data: str) -> bytes:
    b64 = Base64()
    return b64.decode(data)
