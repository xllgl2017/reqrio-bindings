package org.xllgl2017.tls;

public enum Version {
    TLCP(0x101),
    TLS_1_0(0x301),
    TLS_1_1(0x302),
    TLS_1_2(0x303),
    TLS_1_3(0x304);

    public final int value;

    Version(int value) {
        this.value = value;
    }
}
