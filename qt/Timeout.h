//
// Created by XLX on 2026/1/21.
//

#ifndef REQRIO_QT_TIMEOUT_H
#define REQRIO_QT_TIMEOUT_H
#include <QJsonObject>


/// Timeout settings for connect, read, write, and retry behavior.
class Timeout {
    int connect = 3000;
    int read = 3000;
    int write = 3000;
    int handle = 30000;
    int connect_times = 3;
    int handle_times = 3;

public:
    /// Create timeout settings.
    /// @param connect: connect timeout in milliseconds.
    /// @param read: read timeout in milliseconds.
    /// @param write: write timeout in milliseconds.
    /// @param handle: request handle timeout in milliseconds.
    /// @param connect_times: connect retry count.
    /// @param handle_times: request retry count.
    Timeout(int connect = 3000, int read = 3000, int write = 3000, int handle = 30000, int connect_times = 3,
            int handle_times = 3);

    /// Convert settings to a JSON object.
    QJsonObject toJson() const;
};


#endif //REQRIO_QT_TIMEOUT_H
