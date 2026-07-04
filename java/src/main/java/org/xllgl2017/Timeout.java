package org.xllgl2017;

public class Timeout {
    /// 建立连接超时时间
    public int connect = 3000;
    /// 单次 tcp 读取超时时间
    public int read = 3000;
    /// 单次 tcp 写出超时时间
    public int write = 3000;
    /// 处理请求超时时间
    public int handle = 30000;
    /// 尝试请求的次数
    public int connect_times = 3;
    /// 尝试连接的次数
    public int handle_times = 3;

    public Timeout() {
    }

    public Timeout(int connect, int read, int write, int handle, int connect_times, int handle_times) {
        this.connect = connect;
        this.read = read;
        this.write = write;
        this.handle = handle;
        this.connect_times = connect_times;
        this.handle_times = handle_times;
    }

    public Timeout(int timeout, int handle) {
        this.connect = timeout;
        this.read = timeout;
        this.write = timeout;
        this.handle_times = handle;
        this.connect_times = handle;
    }

    public void setConnect(int connect) {
        this.connect = connect;
    }

    public void setRead(int read) {
        this.read = read;
    }

    public void setWrite(int write) {
        this.write = write;
    }

    public void setHandle(int handle) {
        this.handle = handle;
    }

    public void setConnect_times(int connect_times) {
        this.connect_times = connect_times;
    }

    public void setHandle_times(int handle_times) {
        this.handle_times = handle_times;
    }


}
