package org.xllgl2017;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Headers {
    private final HashMap<String, String> keys;
    private List<Cookie> cookies;

    public Headers() {
        this.keys = new HashMap<>();
        this.cookies = new ArrayList<>();
    }

    public void addHeader(String name, String value) {
        this.keys.put(name, value);
    }

    public List<Cookie> getCookies() {
        return cookies;
    }

    public void addCookie(Cookie cookie) {
        this.cookies.add(cookie);
    }

    public void addCookie(String name, String value) {
        this.cookies.add(new Cookie(name, value));
    }

    public void setCookies(String cookies) {
        String[] items = cookies.split("; ");
        for (String item : items) {
            String[] kvs = item.split("=");
            if (kvs.length > 1) {
                this.addCookie(new Cookie(kvs[0], kvs[1]));
            } else {
                this.addCookie(new Cookie(kvs[0], ""));
            }
        }
    }

    public void setCookies(ArrayList<Cookie> cookies) {
        this.cookies = cookies;
    }

    public HashMap<String, String> getKeys() {
        return keys;
    }

    public String get(String name) {
        for (String key : this.keys.keySet()) {
            if (key.equalsIgnoreCase(name)) return this.keys.get(key);
        }
        return null;
    }

    public String getOrDefault(String name, String d) {
        String res = this.get(name);
        if (res == null) return d;
        return res;
    }
}
