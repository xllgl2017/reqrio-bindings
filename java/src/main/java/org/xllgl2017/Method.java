package org.xllgl2017;

public enum Method {
    GET(0),
    POST(1),
    PUT(2),
    HEAD(3),
    DELETE(4),
    OPTIONS(5),
    TRACE(6),
    CONNECT(7),
    PATCH(8);

    final int value;

    Method(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

}
