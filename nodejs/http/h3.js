const H3SettingType = {
    MaxTableCapacity: 1,
    MaxFieldSectionSize: 6,
    BlockedStreams: 7,
    EnableDatagram: 0x33
}

const H3FrameType = {
    Data: 0,
    Header: 1,
    Setting: 4,
    PriorityUpdate: 0xf0700
}

module.exports = {
    H3SettingType,
    H3FrameType
}