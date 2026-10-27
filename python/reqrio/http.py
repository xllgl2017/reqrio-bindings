from enum import IntEnum


class H2Setting:
    HeaderTableSize = "HeaderTableSize"
    EnablePush = "EnablePush"
    MaxConcurrentStreams = "MaxConcurrentStreams"
    InitialWindowSize = "InitialWindowSize"
    MaxFrameSize = "MaxFrameSize"
    MaxHeaderListSize = "MaxHeaderListSize"
    Reserved = "Reserved"

class Method(IntEnum):
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