from _ctypes import byref, addressof
from ctypes import string_at, c_ubyte, POINTER, c_size_t
from typing import Union

from reqrio.bindings import DLL
from reqrio import util


def url_encode(url: str) -> str:
    ptr = DLL.url_encode(url.encode('utf-8'))
    if ptr is None:
        raise Exception("url encode error")
    bs = string_at(ptr).decode('utf-8')
    DLL.char_free(ptr)
    return bs


def url_decode(url: str) -> str:
    ptr = DLL.url_decode(url.encode('utf-8'))
    if ptr is None:
        raise Exception("url encode error")
    bs = string_at(ptr).decode('utf-8')
    DLL.char_free(ptr)
    return bs


def hex_encode(data: Union[str, bytes]) -> str:
    data_len, data_u8 = util.str_bytes_to_u8(data)
    ptr = DLL.hex_encode(data_u8, data_len)
    if ptr is None:
        raise Exception("url encode error")
    bs = string_at(ptr).decode('utf-8')
    DLL.char_free(ptr)
    return bs


def hex_decode(data: str) -> bytes:
    out_ptr = POINTER(c_ubyte)()
    out_len = c_size_t()
    ret = DLL.hex_decode(data.encode('utf-8'), byref(out_ptr), byref(out_len))
    try:
        if ret == -1:
            raise Exception("decrypt error")
        array_bytes = c_ubyte * out_len.value
        byte_array = array_bytes.from_address(addressof(out_ptr.contents))
        return bytes(byte_array)
    finally:
        DLL.u8_free(out_ptr, out_len)
