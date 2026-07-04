//
// Created by XLX on 2026/1/21.
//

#ifndef REQRIO_QT_TIMEOUT_H
#define REQRIO_QT_TIMEOUT_H
#include <QJsonObject>


class Timeout {
    int connect = 3000;
    int read = 3000;
    int write = 3000;
    int handle = 30000;
    int connect_times = 3;
    int handle_times = 3;

public:
    Timeout(int connect = 3000, int read = 3000, int write = 3000, int handle = 30000, int connect_times = 3,
            int handle_times = 3);

    QJsonObject toJson() const;
};


#endif //REQRIO_QT_TIMEOUT_H
