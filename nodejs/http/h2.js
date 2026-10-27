const H2SettingType = {
    HeaderTableSize: 0x1,
    EnablePush: 0x2,
    MaxConcurrentStreams: 0x3,
    InitialWindowSize: 0x4,
    MaxFrameSize: 0x5,
    MaxHeaderListSize: 0x6,
}

const H2FrameType = {
    Data: 0x00,
    Headers: 0x01,
    Priority: 0x02,
    RstStream: 0x03,
    Settings: 0x04,
    PushPromise: 0x05,
    Ping: 0x06,
    Goaway: 0x07,
    WindowUpdate: 0x08,
    Continuation: 0x09
}

module.exports = {
    H2SettingType,
    H2FrameType
}