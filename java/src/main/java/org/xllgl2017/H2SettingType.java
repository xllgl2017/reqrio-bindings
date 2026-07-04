package org.xllgl2017;

public enum H2SettingType {
    HeaderTableSize("HeaderTableSize"),
    EnablePush("EnablePush"),
    MaxConcurrentStreams("MaxConcurrentStreams"),
    InitialWindowSize("InitialWindowSize"),
    MaxFrameSize("MaxFrameSize"),
    MaxHeaderListSize("MaxHeaderListSize"),
    Reversed("Reversed");
    final String value;

    H2SettingType(String value) {
        this.value = value;
    }
}
