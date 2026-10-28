from ctypes import c_char_p, POINTER, c_void_p

from _ctypes import byref

from reqrio import util
from reqrio.bindings import DLL

# ===========================>Url<===================================
DLL.Url_new.argtypes = [c_char_p, POINTER(c_char_p)]
DLL.Url_new.restype = c_void_p

DLL.Url_add_param.argtypes = [c_void_p, c_char_p, c_char_p]
DLL.Url_add_param.restype = c_void_p

DLL.Url_set_sni.argtypes = [c_void_p, c_char_p]
DLL.Url_set_sni.restype = c_void_p

DLL.Url_remove_param.argtypes = [c_void_p, c_char_p]
DLL.Url_remove_param.restype = c_void_p

DLL.Url_drop.argtypes = [c_void_p]


class Url:
    """Wrapper for a request URL and its query parameters."""

    def __init__(self, url: str, params: dict = None, sni: str = None):
        """Create a URL wrapper.

        Args:
            url: target URL.
            params: query parameters.
            sni: custom SNI host for IP-based requests.
        """
        err = c_char_p()
        self._raw = DLL.Url_new(url.encode('utf-8'), byref(err))
        util.check_char_err(err)
        if sni is not None:
            self.set_sni(sni)
        if params is not None:
            self.set_params(params)
        return

    def set_params(self, params: dict):
        """Add a batch of query parameters.

        Args:
            params: query parameter mapping.
        """
        for name in params.keys():
            value = str(params[name])
            self.add_param(name, value)

    def add_param(self, name: str, value: str, equal_sign: bool = True):
        """Add one query parameter.

        Args:
            name: parameter name.
            value: parameter value.
            equal_sign: whether to include the equals sign.
        """
        err = DLL.Url_add_param(self._raw, name.encode('utf-8'), value.encode('utf-8'), equal_sign)
        util.check_char_err(err)

    def remove_param(self, name: str):
        """Remove one query parameter.

        Args:
            name: parameter name to remove.
        """
        err = DLL.Url_remove_param(self._raw, name.encode('utf-8'))
        util.check_char_err(err)

    def set_sni(self, sni: str):
        """Set the SNI host used for the URL.

        Args:
            sni: SNI host name.
        """
        err = DLL.Url_set_sni(self._raw, sni.encode('utf-8'))
        util.check_char_err(err)

    def take(self):
        """Return and detach the native pointer."""
        ptr = self._raw
        self._raw = None
        return ptr

    def __del__(self):
        if hasattr(self, '_raw') and self._raw:
            DLL.Url_drop(self._raw)
            self._raw = None
