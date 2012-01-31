package com.tcs.igrid.register.beans;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * @ inadareishvili
 */
public class Configuration {

    private String name = null;
    private String driver = null;
    private String user = null;
    private String password = null;
    private String uri = null;
    private int poolMinSize = 0;
    private int poolMaxSize = 0;
    private static final Log LOG = LogFactory.getLog(Configuration.class);

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getPoolMaxSize() {
        return poolMaxSize;
    }

    public void setPoolMaxSize(int poolMaxSize) {
        this.poolMaxSize = poolMaxSize;
    }

    public int getPoolMinSize() {
        return poolMinSize;
    }

    public void setPoolMinSize(int poolMinSize) {
        this.poolMinSize = poolMinSize;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    @Override
    public String toString() {
        ReflectionToStringBuilder tsb = new ReflectionToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE);
        return tsb.toString();
    }
}
