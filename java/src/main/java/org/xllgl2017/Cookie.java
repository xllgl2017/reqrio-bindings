package org.xllgl2017;

/**
 * HTTP cookie model.
 */
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

    /**
     * Create a cookie.
     * @param name Cookie name.
     * @param value Cookie value.
     */
    public Cookie(String name, String value) {
        this.name = name;
        this.value = value;
    }

    /**
     * Get cookie value.
     * @return Cookie value.
     */
    public String getValue() {
        return value;
    }

    /**
     * Get cookie name.
     * @return Cookie name.
     */
    public String getName() {
        return name;
    }

    /**
     * Get cookie max age.
     * @return Age in seconds.
     */
    public int getAge() {
        return age;
    }

    /**
     * Get cookie domain.
     * @return Domain.
     */
    public String getDomain() {
        return domain;
    }

    /**
     * Get cookie path.
     * @return Path.
     */
    public String getPath() {
        return path;
    }

    /**
     * Check HttpOnly flag.
     * @return true if HttpOnly.
     */
    public boolean isHttpOnly() {
        return httpOnly;
    }

    /**
     * Check Secure flag.
     * @return true if secure.
     */
    public boolean isSecure() {
        return secure;
    }

    /**
     * Get expiration value.
     * @return Expires value.
     */
    public String getExpires() {
        return expires;
    }

    /**
     * Get SameSite value.
     * @return SameSite value.
     */
    public String getSameSite() {
        return sameSite;
    }

    /**
     * Check ICPSP flag.
     * @return true if ICPSP.
     */
    public boolean isIcpsp() {
        return icpsp;
    }
}
