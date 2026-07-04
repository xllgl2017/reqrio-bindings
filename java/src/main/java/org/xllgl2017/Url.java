package org.xllgl2017;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

import java.util.HashMap;

import static org.xllgl2017.ReqrioLibrary.REQRIO;

public class Url implements AutoCloseable {
    private Pointer raw;

    public Pointer pointer() throws Exception {
        if (this.raw == null) throw new Exception("Url had dropped");
        return this.raw;
    }

    /// 初始化
    ///
    /// @param url :请求地址
    public Url(String url) throws Exception {
        PointerByReference err = new PointerByReference();
        this.raw = REQRIO.Url_new(url, err);
        util.check_err_pointer(err);
    }

    /// @param sni :域名
    public Url(String url, String sni) throws Exception {
        this(url);
        this.setSni(sni);
    }

    /// @param params url请求参数，value应为未编码
    public Url(String url_str, HashMap<String, String> params) throws Exception {
        this(url_str);
        for (String key : params.keySet()) {
            this.addParam(key, params.get(key));
        }
    }

    /// 添加一个请求参数，若这个值已存在则会被覆盖
    ///
    /// @param name  :参数名
    /// @param value :参数值
    public void addParam(String name, String value) throws Exception {
        try {
            util.check_err(REQRIO.Url_add_param(this.pointer(), name, value));
        } catch (Exception e) {
            close();
            throw e;
        }
    }

    /// 删除一个参数
    ///
    /// @param name :待删除的参数名
    public void removeParam(String name) throws Exception {
        try {
            util.check_err(REQRIO.Url_remove_param(this.pointer(), name));
        } catch (Exception e) {
            close();
            throw e;
        }
    }

    public void setRaw(Pointer raw) {
        this.raw = raw;
    }

    /// @param sni :域名，在使用ip url时设置
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
