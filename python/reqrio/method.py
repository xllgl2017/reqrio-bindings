from enum import Enum


class Method(Enum):
    GET = 0
    POST = 1
    PUT = 2
    HEAD = 3
    DELETE = 4
    OPTIONS = 5
    TRACE = 6
    CONNECT = 7
    PATCH = 8
