package org.xllgl2017;

public enum EcPointFormat {
    UNCOMPRESSED(0),
    ANSI_X962_PRIME(1),
    ANSI_X962_CHAR2(2);

    final int value;

    EcPointFormat(int value) {
        this.value = value;
    }
}
