package org.xllgl2017;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

import java.util.HashMap;

import static org.xllgl2017.ReqrioLibrary.REQRIO;

/**
 * URL wrapper for request construction.
 */
public class Url implements AutoCloseable {
    private Pointer raw;

    /**
     * Get the native URL pointer.
     * @return Native pointer.
     */
    public Pointer pointer() throws Exception {
        if (this.raw == null) throw new Exception("Url had dropped");
        return this.raw;
    }

    /**
     * Create a URL wrapper.
     * @param url Request URL.
     */
    public Url(String url) throws Exception {
        PointerByReference err = new PointerByReference();
        this.raw = REQRIO.Url_new(url, err);
        util.check_err_pointer(err);
    }

    /**
     * Create a URL wrapper and set SNI.
     * @param url Request URL.
     * @param sni SNI host name.
     */
    public Url(String url, String sni) throws Exception {
        this(url);
        this.setSni(sni);
    }

    /**
     * Create a URL wrapper with query params.
     * @param url_str Base URL.
     * @param params Query parameter map.
     */
    public Url(String url_str, HashMap<String, String> params) throws Exception {
        this(url_str);
        for (String key : params.keySet()) {
            this.addParam(key, params.get(key));
        }
    }

    /**
     * Add a query parameter.
     * @param name Parameter name.
     * @param value Parameter value.
     */
    public void addParam(String name, String value) throws Exception {
        try {
            util.check_err(REQRIO.Url_add_param(this.pointer(), name, value));
        } catch (Exception e) {
            close();
            throw e;
        }
    }

    /**
     * Remove a query parameter.
     * @param name Parameter name.
     */
    public void removeParam(String name) throws Exception {
        try {
            util.check_err(REQRIO.Url_remove_param(this.pointer(), name));
        } catch (Exception e) {
            close();
            throw e;
        }
    }

    /**
     * Set the native pointer directly.
     * @param raw Native URL pointer.
     */
    public void setRaw(Pointer raw) {
        this.raw = raw;
    }

    /**
     * Set the SNI for this URL.
     * @param sni SNI host name.
     */
    public void setSni(String sni) throws Exception {
        try {
            util.check_err(REQRIO.Url_set_sni(this.pointer(), sni));
        } catch (Exception e) {
            close();
            throw e;
        }
    }

    @Override
    public void close() {
        if (this.raw == null) return;
        REQRIO.Url_drop(this.raw);
        this.raw = null;
    }
}
