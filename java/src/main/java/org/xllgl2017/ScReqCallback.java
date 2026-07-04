package org.xllgl2017;

import com.sun.jna.Pointer;

public interface ScReqCallback extends com.sun.jna.Callback {
    void invoke(Pointer msg, int len);
}
