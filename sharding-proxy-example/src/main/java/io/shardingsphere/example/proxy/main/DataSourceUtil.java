package io.shardingsphere.example.proxy.main;

import org.apache.commons.dbcp.BasicDataSource;

import javax.sql.DataSource;

public final class DataSourceUtil {
    
    public static DataSource createDataSource(final String ip, final int port) {
        BasicDataSource result = new BasicDataSource();
        result.setDriverClassName(com.mysql.jdbc.Driver.class.getName());
        result.setUrl(String.format("jdbc:mysql://%s:%d/sharding_db?useServerPrepStmts=true&cachePrepStmts=true", ip, port));
        result.setUsername("root");
        result.setPassword("root");
        return result;
    }
}
