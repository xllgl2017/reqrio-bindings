//
// Created by XLX on 2026/1/21.
//

#include "Timeout.h"

Timeout::Timeout(int connect, int read, int write, int handle, int connect_times, int handle_times) {
    this->connect = connect;
    this->read = read;
    this->write = write;
    this->handle = handle;
    this->connect_times = connect_times;
    this->handle_times = handle_times;
}

QJsonObject Timeout::toJson() const {
    QJsonObject timeout;
    timeout.insert("connect", this->connect);
    timeout.insert("read", this->read);
    timeout.insert("write", this->write);
    timeout.insert("handle", this->handle);
    timeout.insert("connect_times", this->connect_times);
    timeout.insert("handle_times", this->handle_times);
    return timeout;
}
