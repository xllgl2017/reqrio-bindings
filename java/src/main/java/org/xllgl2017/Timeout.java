package org.xllgl2017;

/**
 * Timeout configuration for connect/read/write/handle phases.
 */
public class Timeout {
    /** Connection timeout in ms. */
    public int connect = 3000;
    /** Read timeout in ms. */
    public int read = 3000;
    /** Write timeout in ms. */
    public int write = 3000;
    /** Request handle timeout in ms. */
    public int handle = 30000;
    /** Connection retry count. */
    public int connect_times = 3;
    /** Handle retry count. */
    public int handle_times = 3;

    /**
     * Create default timeout values.
     */
    public Timeout() {
    }

    /**
     * Create a timeout set with all values.
     * @param connect Connect timeout.
     * @param read Read timeout.
     * @param write Write timeout.
     * @param handle Handle timeout.
     * @param connect_times Connect retry count.
     * @param handle_times Handle retry count.
     */
    public Timeout(int connect, int read, int write, int handle, int connect_times, int handle_times) {
        this.connect = connect;
        this.read = read;
        this.write = write;
        this.handle = handle;
        this.connect_times = connect_times;
        this.handle_times = handle_times;
    }

    /**
     * Create a timeout set with one value for all phases.
     * @param timeout Shared timeout value.
     * @param handle Handle retry count.
     */
    public Timeout(int timeout, int handle) {
        this.connect = timeout;
        this.read = timeout;
        this.write = timeout;
        this.handle_times = handle;
        this.connect_times = handle;
    }

    /**
     * Set the connect timeout.
     * @param connect Connect timeout in ms.
     */
    public void setConnect(int connect) {
        this.connect = connect;
    }

    /**
     * Set the read timeout.
     * @param read Read timeout in ms.
     */
    public void setRead(int read) {
        this.read = read;
    }

    /**
     * Set the write timeout.
     * @param write Write timeout in ms.
     */
    public void setWrite(int write) {
        this.write = write;
    }

    /**
     * Set the handle timeout.
     * @param handle Handle timeout in ms.
     */
    public void setHandle(int handle) {
        this.handle = handle;
    }

    /**
     * Set the connect handle times.
     * @param connect_times handle times.
     */
    public void setConnect_times(int connect_times) {
        this.connect_times = connect_times;
    }

    /**
     * Set the handle times.
     * @param handle_times handle times.
     */
    public void setHandle_times(int handle_times) {
        this.handle_times = handle_times;
    }


}
