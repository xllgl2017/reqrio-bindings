from ctypes import c_ubyte, POINTER, c_size_t
from enum import Enum
from typing import Union

from _ctypes import byref, addressof
from reqrio.bindings import DLL
from reqrio import util


class CipherType(Enum):
    AES_128_CBC = 0
    AES_192_CBC = 1
    AES_256_CBC = 2
    AES_128_ECB = 3
    AES_192_ECB = 4
    AES_256_ECB = 5
    AES_128_CTR = 6
    AES_192_CTR = 7
    AES_256_CTR = 8
    AES_128_GCM = 9
    AES_192_GCM = 10
    AES_256_GCM = 11
    AES_128_OFB = 12
    AES_192_OFB = 13
    AES_256_OFB = 14
    DES_CBC = 15
    DES_ECB = 16
    RC4 = 17


class Cipher:
    def __init__(self, ct: CipherType, key: Union[str, bytes], iv: Union[str, bytes] = None):
        self.cipher = DLL.Cipher_new(ct.value)
        self.set_secret_key(key, iv)

    @staticmethod
    def aes_128_cbc(key: Union[str, bytes], iv: Union[str, bytes]):
        return Cipher(CipherType.AES_128_CBC, key, iv)

    @staticmethod
    def aes_128_ecb(key: Union[str, bytes]):
        return Cipher(CipherType.AES_128_ECB, key)

    def set_secret_key(self, key: Union[str, bytes], iv: Union[str, bytes] = None):
        key_len, key_u8 = util.str_bytes_to_u8(key)
        iv_len = 0
        if iv is None:
            iv_u8 = None
        else:
            iv_len, iv_u8 = util.str_bytes_to_u8(iv)
        ret = DLL.Cipher_set_secret_key(self.cipher, key_u8, key_len, iv_u8, iv_len)
        if ret == -1:
            raise Exception("set secret key error")

    def encrypt(self, data: Union[str, bytes]) -> bytes:
        data_len, data_u8 = util.str_bytes_to_u8(data)
        out_ptr = POINTER(c_ubyte)()
        out_len = c_size_t()
        ret = DLL.Cipher_encrypt(self.cipher, data_u8, data_len, byref(out_ptr), byref(out_len))
        try:
            if ret == -1:
                raise Exception("encrypt error")
            array_bytes = c_ubyte * out_len.value
            byte_array = array_bytes.from_address(addressof(out_ptr.contents))
            return bytes(byte_array)
        finally:
            DLL.u8_free(out_ptr, out_len)

    def decrypt(self, data: Union[str, bytes]) -> bytes:
        data_len, data_u8 = util.str_bytes_to_u8(data)
        out_ptr = POINTER(c_ubyte)()
        out_len = c_size_t()
        ret = DLL.Cipher_decrypt(self.cipher, data_u8, data_len, byref(out_ptr), byref(out_len))
        try:
            if ret == -1:
                raise Exception("decrypt error")
            array_bytes = c_ubyte * out_len.value
            byte_array = array_bytes.from_address(addressof(out_ptr.contents))
            return bytes(byte_array)
        finally:
            DLL.u8_free(out_ptr, out_len)

    def __del__(self):
        if hasattr(self, "cipher") and self.cipher:
            DLL.Cipher_free(self.cipher)
            self.cipher = None
