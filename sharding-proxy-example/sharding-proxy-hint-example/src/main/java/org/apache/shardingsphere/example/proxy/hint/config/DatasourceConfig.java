package org.apache.shardingsphere.example.proxy.hint.config;

import org.apache.shardingsphere.core.yaml.config.YamlConfiguration;

public class DatasourceConfig implements YamlConfiguration {
    private String driverClassName;
    private String jdbcUrl;
    private String username;
    private String password;
    
    public String getDriverClassName() {
        return driverClassName;
    }
    
    public void setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
    }
    
    public String getJdbcUrl() {
        return jdbcUrl;
    }
    
    public void setJdbcUrl(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
}
