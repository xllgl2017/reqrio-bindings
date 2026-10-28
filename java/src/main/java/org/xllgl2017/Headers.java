package org.xllgl2017;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * HTTP header storage.
 */
public class Headers {
    private final HashMap<String, String> keys;
    private List<Cookie> cookies;

    /**
     * Create an empty header container.
     */
    public Headers() {
        this.keys = new HashMap<>();
        this.cookies = new ArrayList<>();
    }

    /**
     * Add a header entry.
     * @param name Header name.
     * @param value Header value.
     */
    public void addHeader(String name, String value) {
        this.keys.put(name, value);
    }

    /**
     * Get all cookies.
     * @return Cookie list.
     */
    public List<Cookie> getCookies() {
        return cookies;
    }

    /**
     * Add a cookie object.
     * @param cookie Cookie object.
     */
    public void addCookie(Cookie cookie) {
        this.cookies.add(cookie);
    }

    /**
     * Add a cookie by name and value.
     * @param name Cookie name.
     * @param value Cookie value.
     */
    public void addCookie(String name, String value) {
        this.cookies.add(new Cookie(name, value));
    }

    /**
     * Parse a cookie string.
     * @param cookies Cookie string.
     */
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

    /**
     * Replace the cookie list.
     * @param cookies Cookie list.
     */
    public void setCookies(ArrayList<Cookie> cookies) {
        this.cookies = cookies;
    }

    /**
     * Get the raw header map.
     * @return Header map.
     */
    public HashMap<String, String> getKeys() {
        return keys;
    }

    /**
     * Get a header value ignoring case.
     * @param name Header name.
     * @return Header value or null.
     */
    public String get(String name) {
        for (String key : this.keys.keySet()) {
            if (key.equalsIgnoreCase(name)) return this.keys.get(key);
        }
        return null;
    }

    /**
     * Get a header value or a default value.
     * @param name Header name.
     * @param d Default value.
     * @return Header value or default.
     */
    public String getOrDefault(String name, String d) {
        String res = this.get(name);
        if (res == null) return d;
        return res;
    }
}
