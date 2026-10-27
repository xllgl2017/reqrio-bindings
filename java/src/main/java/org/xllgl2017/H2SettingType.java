package org.xllgl2017;

public enum H2SettingType {
    HeaderTableSize(1),
    EnablePush(2),
    MaxConcurrentStreams(3),
    InitialWindowSize(4),
    MaxFrameSize(5),
    MaxHeaderListSize(6);
    public final int value;

    H2SettingType(int value) {
        this.value = value;
    }
}


