//
// Created by XLX on 2026/6/13.
//
#include "util.h"

QString util::alpn_str(const ALPN alpn){
    switch (alpn) {
        case HTTP20:
            return "h2";
        case HTTP11:
            return "http/1.1";
    }
    return "";
}