const CompressionMethod = {
    NULL: 0,
    DEFLATE: 1,
    BROTLI: 2,
    GZIP: 0xFFFF,
    ZSTD: 0xFFFE
}

module.exports = {CompressionMethod}