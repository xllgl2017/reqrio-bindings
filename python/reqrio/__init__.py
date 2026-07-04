from reqrio.alpn import ALPN
from reqrio.response import Response
from reqrio._session import Session
from reqrio.method import Method
from reqrio.websocket import WebSocket, WsOpCode, WsFrame
from reqrio.cipher import CipherType, Cipher
from reqrio.hash import HashType, Hasher, Hmac
from reqrio.b64 import Base64, b64encode, b64decode
from typing import Union
from reqrio.rcode import url_encode, url_decode, hex_encode, hex_decode
from reqrio._finger import Algorithm, CompressionMethod, Group, Version, EcPointFormat, ExtensionType, CipherSuite, \
    H2Setting


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
        token: str = "",
        sni: str = None

):
    req = Session(headers, alpn, verify, proxy, key_log, ja3, ja4, client_hello, random_tls, custom_tls, token)
    resp = req.pre_send(method, url, params, data, json, bytes, text, files, content_type, auto_redirect=auto_redirect,
                        sni=sni)
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
