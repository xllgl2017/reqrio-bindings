package org.xllgl2017;

public enum H3FrameType {
    Data(0),
    Header(1),
    Setting(4),
    PriorityUpdate(0xf0700);

    public final int value;

    H3FrameType(int value) {
        this.value = value;
    }
}
