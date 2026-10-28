from enum import IntEnum


class H2Setting:
    """Common HTTP/2 setting names."""

    HeaderTableSize = "HeaderTableSize"
    EnablePush = "EnablePush"
    MaxConcurrentStreams = "MaxConcurrentStreams"
    InitialWindowSize = "InitialWindowSize"
    MaxFrameSize = "MaxFrameSize"
    MaxHeaderListSize = "MaxHeaderListSize"
    Reserved = "Reserved"


class Method(IntEnum):
    """HTTP methods supported by the request session."""

    GET = 0
    POST = 1
    PUT = 2
    HEAD = 3
    DELETE = 4
    OPTIONS = 5
    TRACE = 6
    CONNECT = 7
    PATCH = 8
    QUERY = 9