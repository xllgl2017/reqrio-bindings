package org.xllgl2017;

public enum H3SettingType {
    MaxTableCapacity(1),
    MaxFieldSectionSize(6),
    BlockedStreams(7),
    EnableDatagram(0x33);


    public final int value;

    H3SettingType(int value) {
        this.value = value;
    }
    }
