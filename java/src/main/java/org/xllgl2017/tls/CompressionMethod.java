package org.xllgl2017.tls;

public enum CompressionMethod {
    NULL(0),
    DEFLATE(1),
    BROTLI(2),
    GZIP(0xFFFF),
    ZSTD(0xFFFE);

    public final int value;

    CompressionMethod(int value) {
        this.value = value;
    }
}
