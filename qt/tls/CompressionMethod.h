//
// Created by XLX on 2026/5/16.
//

#ifndef REQRIO_QT_COMPRESSIONMETHOD_H
#define REQRIO_QT_COMPRESSIONMETHOD_H

enum CompressionMethod {
    NUL = 0,
    DEFLATE = 1,
    BROTLI = 2,
    GZIP = 0xFFFF,
    ZSTD = 0xFFFE
};
#endif //REQRIO_QT_COMPRESSIONMETHOD_H
