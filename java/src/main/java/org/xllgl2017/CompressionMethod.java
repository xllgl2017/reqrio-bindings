package org.xllgl2017;

public enum CompressionMethod {
    NULL(0),
    DEFLATE(1),
    BROTLI(2),
    GZIP(0xFFFF),
    ZSTD(0xFFFE);

    final int value;

    CompressionMethod(int value) {
        this.value = value;
    }
}
