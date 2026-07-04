//
// Created by XLX on 2026/5/17.
//

#ifndef REQRIO_QT_H2SETTINGTYPE_H
#define REQRIO_QT_H2SETTINGTYPE_H

enum H2Setting {
    HeaderTableSize = 0x1,
    EnablePush = 0x2,
    MaxConcurrentStreams = 0x3,
    InitialWindowSize = 0x4,
    MaxFrameSize = 0x5,
    MaxHeaderListSize = 0x6,
    // Reserved { flag: u16, value: u32 },
};

#endif //REQRIO_QT_H2SETTINGTYPE_H
