//
// Created by XLX on 2026/5/16.
//

#ifndef REQRIO_QT_GROUP_H
#define REQRIO_QT_GROUP_H

enum Group {
    X25519 = 0x1d,
    X448 = 0x1e,
    X25519MLKEM768 = 0x11ec,
    Secp256r1 = 0x0017,
    Secp384r1 = 0x0018,
    Secp521r1 = 0x0019,
    FFDHE2048 = 0x0100,
    FFDHE3072 = 0x0101,
    FFDHE4096 = 0x0102,
    FFDHE6144 = 0x0103,
    FFDHE8192 = 0x0104,
};

#endif //REQRIO_QT_GROUP_H
