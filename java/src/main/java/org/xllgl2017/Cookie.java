package org.xllgl2017;

public class Cookie {
    private final String name;
    private final String value;
    private int age;
    private String domain;
    private String path;
    private boolean httpOnly;
    private boolean secure;
    private String expires;
    private String sameSite;
    private boolean icpsp;

    public Cookie(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    public String getDomain() {
        return domain;
    }

    public String getPath() {
        return path;
    }

    public boolean isHttpOnly() {
        return httpOnly;
    }

    public boolean isSecure() {
        return secure;
    }

    public String getExpires() {
        return expires;
    }

    public String getSameSite() {
        return sameSite;
    }

    public boolean isIcpsp() {
        return icpsp;
    }
}
