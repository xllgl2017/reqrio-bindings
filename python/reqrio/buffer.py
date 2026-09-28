import ctypes


class Buf(ctypes.Structure):
    __type: int
    __cap: int
    __ptr__: int
    __len: int
    _fields_ = [
        ("__type", ctypes.c_int32),
        ("pad", ctypes.c_uint32),
        ("__cap", ctypes.c_size_t),
        ("__ptr__", ctypes.POINTER(ctypes.c_uint8)),
        ("__len__", ctypes.c_size_t)
    ]

    def as_bytes(self) -> bytes:
        return ctypes.string_at(self.__ptr__, self.__len__)


class Buffer(ctypes.Structure):
    __start__: int
    __end__: int
    __ptr__: int
    _fields_ = [
        ("capacity", ctypes.c_size_t),
        ("__start__", ctypes.c_size_t),
        ("__end__", ctypes.c_size_t),
        ("__ptr__", ctypes.POINTER(ctypes.c_uint8)),
        ("_rsv1", ctypes.c_bool),
        ("_rsv2", ctypes.c_size_t),
    ]

    def as_bytes(self) -> bytes:
        size = self.__end__ - self.__start__
        addr = ctypes.cast(self.__ptr__, ctypes.c_void_p).value + self.__start__
        return ctypes.string_at(addr, size)
