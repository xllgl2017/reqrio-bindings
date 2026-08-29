import json
from ctypes import string_at
from enum import Enum
from reqrio.bindings import DLL


class WsOpCode(Enum):
    CONTINUATION = 0x0
    TEXT = 0x1
    BINARY = 0x2
    CLOSE = 0x8
    PING = 0x9
    PONG = 0xA


class WsFrame:
    def __init__(self, data):
        frame = json.loads(data)
        if frame['opcode'] == 0x0:
            self.opcode = WsOpCode.CONTINUATION
        elif frame['opcode'] == 0x1:
            self.opcode = WsOpCode.TEXT
        elif frame['opcode'] == 0x2:
            self.opcode = WsOpCode.BINARY
        elif frame['opcode'] == 0x8:
            self.opcode = WsOpCode.CLOSE
        elif frame['opcode'] == 0x9:
            self.opcode = WsOpCode.PING
        elif frame['opcode'] == 0xA:
            self.opcode = WsOpCode.PONG
        self.payload = bytes(frame['payload'])


class WebSocket:
    def __init__(self, url: str, uri: str = None, headers: dict[str, str] = None, proxy: str = None):
        self.url = url
        self.uri = uri
        self.headers = headers
        self.proxy = proxy
        self.ws = None

    @staticmethod
    def connect(url: str, header=None):
        ws = WebSocket(url, header)
        if ws.headers is None:
            ws.headers = {}
        hdr = json.dumps(header)
        ws.ws = DLL.ws_open(ws.url.encode('utf-8'), hdr.encode('utf-8'))
        if ws.ws is None: raise Exception("connect fail!")
        return ws

    def open_raw(self, context: str):
        self.ws = DLL.ws_open_raw(self.url.encode('utf-8'), context.encode('utf-8'))

    def read(self) -> WsFrame:
        ptr = DLL.ws_read(self.ws)
        bs = string_at(ptr).decode('utf-8')
        DLL.char_free(ptr)
        return WsFrame(bs)

    def write(self, opcode: WsOpCode, bs: bytes):
        r = DLL.ws_write(self.ws, opcode.value, True, bs)
        if r == -1: raise Exception("ws写帧失败")

    def close(self):
        DLL.ws_close(self.ws)
        self.ws = None

    def __del__(self):
        if hasattr(self, "ws") and self.ws:
            DLL.ws_close(self.ws)
            self.ws = None
