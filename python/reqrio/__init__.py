from reqrio.alpn import ALPN
from reqrio.response import Response
from reqrio._session import Session
from reqrio.http import Method, H2Setting
from reqrio.websocket import WebSocket, WsOpCode, WsFrame
from reqrio.cipher import CipherType, Cipher
from reqrio.hash import HashType, Hasher, Hmac
from reqrio.b64 import Base64, b64encode, b64decode
from typing import Union
from reqrio.rcode import url_encode, url_decode, hex_encode, hex_decode
from reqrio.tls import CipherSuite, SignatureAlgorithm, CompressionMethod, NamedCurve, Version, EcPointFormat, \
    ExtensionType, PskMode
from reqrio._finger import Fingerprint


# pyinstaller.exe -F --collect-binaries reqrio .\1.py


def _pyinstaller_hooks_dir():
    """Return PyInstaller hook directory for bundled assets.

    Returns:
        list[str]: hook directory path list.
    """
    from pathlib import Path
    return [str(Path(__file__).with_name("hooks").resolve())]


def send(
        method: Method,
        url: str,
        params: dict = None,

        headers: dict = None,
        alpn=ALPN.HTTP11,
        verify: bool = True,
        proxy: str = None,
        key_log: str = None,

        ja3: str = None,
        ja4: str = None,
        client_hello: bytes = None,
        random_tls: bool = False,
        custom_tls: dict = None,
        builtin_name: str = None,
        idx: int = None,
        token: str = "",
        stream=False,
        ignore_hdr_sort: bool = False,

        **kwargs
):
    fingerprint = None
    if ja3 is not None:
        fingerprint = Fingerprint.from_ja3(ja3, token)
    if ja4 is not None:
        fingerprint = Fingerprint.from_ja4(ja4, token)
    if client_hello is not None:
        fingerprint = Fingerprint.from_client_hello(client_hello, token)
    if random_tls:
        fingerprint = Fingerprint.from_random(token)
    if custom_tls is not None:
        fingerprint = Fingerprint.from_custom(custom_tls, token)
    if builtin_name is not None:
        fingerprint = Fingerprint.from_builtin(builtin_name, token)
    if idx is not None:
        fingerprint = Fingerprint.from_idx(idx, token)
    req = Session(headers, alpn, verify, proxy, key_log, fingerprint, ignore_hdr_sort)
    from reqrio.bindings import DLL
    if stream:
        call_func = DLL.ScReq_send
    else:
        call_func = DLL.ScReq_do_http
    res = req.send(method, url, params, call_func=call_func, **kwargs)
    if stream:
        resp = req.recv(res, stream)
        resp.__req_free__ = True
        req.hid = None
        return resp
    else:
        return Response(res, req.hid, stream)


def get(url: str, headers: dict = None, data: dict = None, json: dict = None, params: dict = None,
        **kwargs) -> Response:
    """Send a GET request.

    Args:
        url: request URL.
        headers: request headers.
        data: form data.
        json: JSON payload.
        params: URL query params.
        **kwargs: extra session options.
    """
    return send(Method.GET, url, params, headers, data=data, json=json, **kwargs)


def post(url: str, headers: dict = None, data: dict = None, json: dict = None, params: dict = None,
         **kwargs) -> Response:
    """Send a POST request.

    Args:
        url: request URL.
        headers: request headers.
        data: form data.
        json: JSON payload.
        params: URL query params.
        **kwargs: extra session options.
    """
    return send(Method.POST, url, params, headers, data=data, json=json, **kwargs)


def en_b64(ct: CipherType, data: Union[str, bytes], key: Union[str, bytes], iv: Union[str, bytes] = None) -> str:
    cipher = Cipher(ct, key, iv)
    en_bs = cipher.encrypt(data)
    return b64encode(en_bs)


def de_b64(ct: CipherType, data: str, key: Union[str, bytes], iv: Union[str, bytes] = None) -> bytes:
    de_bs = b64decode(data)
    cipher = Cipher(ct, key, iv)
    return cipher.decrypt(de_bs)


def en_hex(ct: CipherType, data: Union[str, bytes], key: Union[str, bytes], iv: Union[str, bytes] = None) -> str:
    cipher = Cipher(ct, key, iv)
    en_bs = cipher.encrypt(data)
    return hex_encode(en_bs)


def de_hex(ct: CipherType, data: str, key: Union[str, bytes], iv: Union[str, bytes] = None) -> bytes:
    de_bs = hex_decode(data)
    cipher = Cipher(ct, key, iv)
    return cipher.decrypt(de_bs)
