from _ctypes import byref, addressof
from ctypes import c_ubyte, POINTER, c_size_t
from enum import Enum
from typing import Union

from reqrio.bindings import DLL
from reqrio import util


class HashType(Enum):
    MD5 = 0
    Sha1 = 1
    Sha224 = 2
    Sha256 = 3
    Sha384 = 4
    Sha512 = 5


class Hasher:
    def __init__(self, ht: HashType):
        self.hasher = DLL.Hasher_new(ht.value)
        if self.hasher is None:
            raise Exception('new hasher error')

    def update(self, data: Union[str, bytes]):
        data_len, data_u8 = util.str_bytes_to_u8(data)
        ret = DLL.Hasher_update(self.hasher, data_u8, data_len)
        if ret == -1: raise Exception("hasher update error")

    def finalize(self) -> bytes:
        out_ptr = POINTER(c_ubyte)()
        out_len = c_size_t()
        ret = DLL.Hasher_finalize(self.hasher, byref(out_ptr), byref(out_len))
        try:
            if ret == -1:
                raise Exception("finalize error")
            array_bytes = c_ubyte * out_len.value
            byte_array = array_bytes.from_address(addressof(out_ptr.contents))
            self.hasher = None
            return bytes(byte_array)
        finally:
            DLL.u8_free(out_ptr, out_len)

    def __del__(self):
        if hasattr(self, "hasher") and self.hasher:
            DLL.Hasher_free(self.hasher)
            self.hasher = None


class Hmac:
    def __init__(self, key: Union[str, bytes], ht: HashType):
        key_len, key_u8 = util.str_bytes_to_u8(key)
        self.hmac = DLL.Hmac_new(key_u8, key_len, ht.value)
        if self.hmac is None:
            raise Exception('new hasher error')

    def update(self, data: Union[str, bytes]):
        data_len, data_u8 = util.str_bytes_to_u8(data)
        ret = DLL.Hmac_update(self.hmac, data_u8, data_len)
        if ret == -1: raise Exception("hasher update error")

    def finalize(self) -> bytes:
        out_ptr = POINTER(c_ubyte)()
        out_len = c_size_t()
        ret = DLL.Hmac_finalize(self.hmac, byref(out_ptr), byref(out_len))
        try:
            if ret == -1:
                raise Exception("finalize error")
            array_bytes = c_ubyte * out_len.value
            byte_array = array_bytes.from_address(addressof(out_ptr.contents))
            self.hmac = None
            return bytes(byte_array)
        finally:
            DLL.u8_free(out_ptr, out_len)

    def __del__(self):
        if hasattr(self, "hasher") and self.hmac:
            DLL.Hmac_free(self.hmac)
            self.hmac = None
