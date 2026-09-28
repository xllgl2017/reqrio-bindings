const koffi = require('koffi')
const Buf = koffi.struct('Buf', {
    typ: 'int32_t',
    _pad: 'uint32_t',
    cap: 'size_t',
    ptr: 'uint8_t *',
    len: 'size_t'
});
const lib=koffi.load()