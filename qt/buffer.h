//
// Created by XLX on 2026/9/28.
//

#ifndef REQRIO_QT_BUFFER_H
#define REQRIO_QT_BUFFER_H
#include <cstdint>

struct Buf {
    int32_t typ; // 0=Ref 1=Vec 2=Raw
    uint32_t _pad; // 填充（Ref 时是未初始化字节，别读）
    size_t cap;
    uint8_t *ptr;
    size_t len;
};

struct Buffer {
    size_t cap;
    size_t start;
    size_t end;
    uint8_t *ptr;
    bool _rsv1;
    size_t _rsv2;
};



#endif //REQRIO_QT_BUFFER_H
