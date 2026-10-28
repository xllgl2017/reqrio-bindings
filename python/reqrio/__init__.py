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

        data: dict = None,
        json: dict = None,
        bytes: bytes = None,
        text: str = None,
        files: list[dict[str, str]] = None,
        content_type: str = None,

        auto_redirect: bool = True,
        ja3: str = None,
        ja4: str = None,
        client_hello: bytes = None,
        random_tls: bool = False,
        custom_tls: dict = None,
        builtin_name: str = None,
        idx: int = None,
        token: str = "",
        sni: str = None,
        stream=False,
        ignore_hdr_sort: bool = False,
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
    sid = req.pre_send(method, url, params, data, json, bytes, text, sni, files, content_type,
                       auto_redirect=auto_redirect)
    resp = req.recv(sid, stream)
    if stream:
        resp.__req_free__ = True
        req.hid = None
    else:
        req.close()
    return resp


def get(url: str, headers: dict = None, data: dict = None, json: dict = None, params: dict = None,
        **kwargs) -> Response:
    return send(Method.GET, url, params, headers, data=data, json=json, **kwargs)


def post(url: str, headers: dict = None, data: dict = None, json: dict = None, params: dict = None,
         **kwargs) -> Response:
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
