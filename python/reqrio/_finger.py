from _ctypes import byref
from ctypes import c_char_p, POINTER, c_void_p, c_ubyte, c_size_t, c_bool, c_uint16, c_uint8, c_uint32, c_uint64
from reqrio.tls import CipherSuite, SignatureAlgorithm, CompressionMethod, NamedCurve, Version, EcPointFormat, \
    ExtensionType, PskMode

from reqrio.bindings import DLL
from reqrio import util
from typing import Union

# ==========================>Fingerprint<=============================
DLL.Fingerprint_new.argtypes = [c_uint16, c_uint16, c_char_p]
DLL.Fingerprint_new.restype = c_void_p

DLL.Fingerprint_from_builtin.argtypes = [c_char_p, c_bool, c_char_p, POINTER(c_char_p)]
DLL.Fingerprint_from_builtin.restype = c_void_p

DLL.Fingerprint_from_idx.argtypes = [c_size_t, c_char_p, c_bool, POINTER(c_char_p)]
DLL.Fingerprint_from_idx.restype = c_void_p

DLL.Fingerprint_add_cipher_suite.argtypes = [c_void_p, c_uint16]
DLL.Fingerprint_add_ext.argtypes = [c_void_p, c_uint16]
DLL.Fingerprint_add_ext_alpn.argtypes = [c_void_p, c_uint16, c_char_p]
DLL.Fingerprint_add_ext_version.argtypes = [c_void_p, c_uint16, c_uint16]
DLL.Fingerprint_add_ext_curve.argtypes = [c_void_p, c_uint16, c_uint16]
DLL.Fingerprint_add_ext_compress.argtypes = [c_void_p, c_uint16, c_uint16]
DLL.Fingerprint_add_ext_psk_mode.argtypes = [c_void_p, c_uint16, c_uint8]
DLL.Fingerprint_add_ext_padding.argtypes = [c_void_p, c_uint16, c_size_t]
DLL.Fingerprint_add_ext_bytes.argtypes = [c_void_p, c_uint16, POINTER(c_ubyte), c_size_t]
DLL.Fingerprint_add_ext_algorithm.argtypes = [c_void_p, c_uint16, c_uint16]
DLL.Fingerprint_add_ext_ec_point.argtypes = [c_void_p, c_uint16, c_uint8]
DLL.Fingerprint_clear_h2_frame.argtypes = [c_void_p]
DLL.Fingerprint_add_h2_setting.argtypes = [c_void_p, c_uint16, c_uint32]
DLL.Fingerprint_add_h2_frame.argtypes = [c_void_p, c_uint8, POINTER(c_ubyte), c_size_t]
DLL.Fingerprint_set_h2_window_size.argtypes = [c_void_p, c_uint32]
DLL.Fingerprint_set_h2_priority.argtypes = [c_void_p, c_bool, c_uint8]
DLL.Fingerprint_clean_h3_frame.argtypes = [c_void_p]
DLL.Fingerprint_add_h3_setting.argtypes = [c_void_p, c_uint64, c_uint64]
DLL.Fingerprint_add_h3_frame.argtypes = [c_void_p, c_uint64, POINTER(c_ubyte), c_size_t]

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

DLL.Fingerprint_drop.argtypes = [c_void_p]


class Fingerprint:
    """TLS fingerprint builder and mutator."""

    def __init__(self, ptr):
        """Create a Fingerprint from an existing native pointer.

        Args:
            ptr: native fingerprint pointer.
        """
        self._raw = ptr

    @staticmethod
    def from_ja3(ja3: str, token: str):
        """Build a fingerprint from a JA3 string.

        Args:
            ja3: JA3 value.
            token: auth token.
        """
        err = c_char_p()
        ptr = DLL.Fingerprint_from_ja3(ja3.encode('utf-8'), token.encode('utf-8'), byref(err))
        err, msg = util.check_char_err(err)
        if err: raise Exception(msg)
        return Fingerprint(ptr)

    @staticmethod
    def from_ja4(ja4: str, token: str):
        err = c_char_p()
        ptr = DLL.Fingerprint_from_ja4(ja4.encode('utf-8'), token.encode('utf-8'), byref(err))
        err, msg = util.check_char_err(err)
        if err: raise Exception(msg)
        return Fingerprint(ptr)

    @staticmethod
    def from_client_hello(client_hello: bytes, token: str):
        """Build a fingerprint from ClientHello bytes.

        Args:
            client_hello: raw ClientHello payload.
            token: auth token.
        """
        err = c_char_p()
        dl, data = util.bytes_to_u8(client_hello)
        ptr = DLL.Fingerprint_from_client_hello(data, dl, token.encode('utf-8'), byref(err))
        err, msg = util.check_char_err(err)
        if err: raise Exception(msg)
        return Fingerprint(ptr)

    @staticmethod
    def from_random(token: str):
        err = c_char_p()
        ptr = DLL.Fingerprint_random(token.encode('utf-8'), byref(err))
        err, msg = util.check_char_err(err)
        if err: raise Exception(msg)
        return Fingerprint(ptr)

    @staticmethod
    def from_custom(custom: dict, token: str):
        """Build a custom fingerprint from a dict config.

        Args:
            custom: fingerprint config.
            token: auth token.
        """
        import json
        err = c_char_p()
        custom = json.dumps(custom)
        print(custom)
        ptr = DLL.Fingerprint_custom(custom.encode('utf-8'), token.encode('utf-8'), byref(err))
        err, msg = util.check_char_err(err)
        if err: raise Exception(msg)
        return Fingerprint(ptr)

    @staticmethod
    def from_builtin(name: str, token: str, sec_full=False):
        err = c_char_p()
        name = name.encode('utf-8')
        token = token.encode('utf-8')
        ptr = DLL.Fingerprint_from_builtin(name, sec_full, token, byref(err))
        err, msg = util.check_char_err(err)
        if err: raise Exception(msg)
        return Fingerprint(ptr)

    @staticmethod
    def from_idx(idx: int, auth: str, sec_full=False):
        """Load a fingerprint by index.

        Args:
            idx: fingerprint index.
            auth: auth token.
            sec_full: whether to include full sec headers.
        """
        err = c_char_p()
        ptr = DLL.Fingerprint_from_idx(idx, auth.encode('utf-8'), sec_full, byref(err))
        err, msg = util.check_char_err(err)
        if err: raise Exception(msg)
        return Fingerprint(ptr)

    @staticmethod
    def new_custom(rv: Version, mv: Version, token: str):
        """Create a new custom fingerprint.

        Args:
            rv: record version.
            mv: message version.
            token: auth token.
        """
        ptr = DLL.Fingerprint_new(rv.value, mv.value, token.encode('utf-8'))
        return Fingerprint(ptr)

    def add_cipher_suite(self, suite: Union[CipherSuite, int]):
        """Add a cipher suite.

        Args:
            suite: cipher suite enum or integer value.
        """
        if type(suite) == CipherSuite:
            DLL.Fingerprint_add_cipher_suite(self._raw, c_uint16(suite.value))
        else:
            DLL.Fingerprint_add_cipher_suite(self._raw, c_uint16(suite))

    def add_extension(self, typ: Union[ExtensionType, int], data: bytes = None):
        if data is None:
            if type(typ) == ExtensionType:
                DLL.Fingerprint_add_ext(self._raw, c_uint16(typ.value))
            else:
                DLL.Fingerprint_add_ext(self._raw, c_uint16(typ))
        else:
            bl, u8 = util.bytes_to_u8(data)
            if type(typ) == ExtensionType:
                DLL.Fingerprint_add_ext_bytes(self._raw, typ.value, u8, bl)
            else:
                DLL.Fingerprint_add_ext_bytes(self._raw, typ, u8, bl)

    def add_application_layer_protocol_negotiation(self, alpn: str):
        """Add an ALPN value as an extension.

        Args:
            alpn: ALPN string.
        """
        typ = ExtensionType.ApplicationLayerProtocolNegotiation
        DLL.Fingerprint_add_ext_alpn(self._raw, typ, alpn.encode('utf-8'))

    def add_application_settings(self, alpn: str):
        """Add an application setting value.

        Args:
            alpn: setting string.
        """
        typ = ExtensionType.ApplicationSetting
        DLL.Fingerprint_add_ext_alpn(self._raw, typ, alpn.encode('utf-8'))

    def add_application_setting_old(self, alpn: str):
        typ = ExtensionType.ApplicationSettingOld
        DLL.Fingerprint_add_ext_alpn(self._raw, typ, alpn.encode('utf-8'))

    def add_supported_version(self, version: Union[Version, int]):
        """Add a supported TLS version.

        Args:
            version: version enum or integer.
        """
        if type(version) == Version:
            DLL.Fingerprint_add_ext_version(self._raw, ExtensionType.SupportedVersions.value, version.value)
        else:
            DLL.Fingerprint_add_ext_version(self._raw, ExtensionType.SupportedVersions.value, version)

    def add_supported_group(self, curve: Union[NamedCurve, int]):
        if type(curve) == NamedCurve:
            DLL.Fingerprint_add_ext_curve(self._raw, ExtensionType.SupportedGroup.value, curve.value)
        else:
            DLL.Fingerprint_add_ext_curve(self._raw, ExtensionType.SupportedGroup.value, curve)

    def add_key_share(self, curve: Union[NamedCurve, int]):
        if type(curve) == NamedCurve:
            DLL.Fingerprint_add_ext_curve(self._raw, ExtensionType.KeyShare.value, curve.value)
        else:
            DLL.Fingerprint_add_ext_curve(self._raw, ExtensionType.KeyShare.value, curve)

    def add_compress_method(self, method: Union[CompressionMethod, int]):
        if type(method) == CompressionMethod:
            DLL.Fingerprint_add_ext_compress(self._raw, ExtensionType.CompressionCertificate.value, method.value)
        else:
            DLL.Fingerprint_add_ext_compress(self._raw, ExtensionType.CompressionCertificate.value, method)

    def add_psk_mode(self, mode: Union[PskMode, int]):
        if type(mode) == PskMode:
            DLL.Fingerprint_add_ext_psk_mode(self._raw, ExtensionType.PskKeyExchangeMode.value, mode.value)
        else:
            DLL.Fingerprint_add_ext_psk_mode(self._raw, ExtensionType.PskKeyExchangeMode.value, mode)

    def add_padding(self, size: int):
        DLL.Fingerprint_add_ext_padding(self._raw, ExtensionType.Padding.value, size)

    def add_signature_algorithm(self, algorithm: Union[SignatureAlgorithm, int]):
        if type(algorithm) == SignatureAlgorithm:
            DLL.Fingerprint_add_ext_algorithm(self._raw, ExtensionType.SignatureAlgorithms.value, algorithm.value)
        else:
            DLL.Fingerprint_add_ext_algorithm(self._raw, ExtensionType.SignatureAlgorithms.value, algorithm)

    def add_ec_point_format(self, point: Union[EcPointFormat, int]):
        if type(point) == EcPointFormat:
            DLL.Fingerprint_add_ext_ec_point(self._raw, ExtensionType.EcPointFormats.value, point.value)
        else:
            DLL.Fingerprint_add_ext_ec_point(self._raw, ExtensionType.EcPointFormats.value, point)

    def clear_h2_frame(self):
        DLL.Fingerprint_clear_h2_frame(self._raw)

    def add_h2_setting(self, flag: int, value: int):
        DLL.Fingerprint_add_h2_setting(self._raw, flag, value)

    def add_h2_frame(self, typ: int, data: bytes):
        bl, u8 = util.bytes_to_u8(data)
        DLL.Fingerprint_add_h2_frame(self._raw, typ, u8, bl)

    def set_h2_window_size(self, size: int):
        DLL.Fingerprint_set_h2_window_size(self._raw, size)

    def set_h2_priority(self, priority: bool, weight: int):
        DLL.Fingerprint_set_h2_priority(self._raw, priority, weight)

    def clear_h3_frame(self):
        DLL.Fingerprint_clean_h3_frame(self._raw)

    def add_h3_setting(self, flag: int, value: int):
        DLL.Fingerprint_add_h3_setting(self._raw, flag, value)

    def add_h3_frame(self, typ: int, data: bytes):
        bl, u8 = util.bytes_to_u8(data)
        DLL.Fingerprint_add_h3_frame(self._raw, typ, u8, bl)

    def take(self):
        ptr = self._raw
        self._raw = None
        return ptr

    def __del__(self):
        if hasattr(self, '_raw') and self._raw:
            DLL.Fingerprint_drop(self._raw)
            self._raw = None
