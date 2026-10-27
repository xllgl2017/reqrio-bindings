package org.xllgl2017;

public enum H2FrameType {
    Data(0x00),
    Headers(0x01),
    Priority(0x02),
    RstStream(0x03),
    Settings(0x04),
    PushPromise(0x05),
    Ping(0x06),
    Goaway(0x07),
    WindowUpdate(0x08),
    Continuation(0x09);

    public final int value;

    H2FrameType(int value) {
        this.value = value;
    }
}


