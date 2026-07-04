package org.xllgl2017;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

import static org.xllgl2017.ReqrioLibrary.REQRIO;

public class util {
    public static void check_err_pointer(PointerByReference ptr) throws Exception {
        check_err(ptr.getValue());
    }

    public static void check_err(Pointer err) throws Exception {
        if (err == null) return;
        String err_msg = err.getString(0, "utf-8");
        REQRIO.char_free(err);
        throw new Exception(err_msg);
    }
}
