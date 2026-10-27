package org.xllgl2017.tls;

public enum PskMode {
    PSK_DHE_KE(1);
    public final int value;

    PskMode(int value) {
        this.value = value;
    }
}
