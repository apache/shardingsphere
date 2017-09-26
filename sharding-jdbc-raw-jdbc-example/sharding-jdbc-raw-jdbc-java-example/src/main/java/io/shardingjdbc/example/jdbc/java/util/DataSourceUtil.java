package io.shardingjdbc.example.jdbc.java.util;

import org.apache.commons.dbcp.BasicDataSource;

import javax.sql.DataSource;

public class DataSourceUtil {
     
    private static final String URL_PREFIX = "jdbc:mysql://localhost:3306/";
    
    private static final String USER_NAME = "root";
    
    private static final String PASSWORD = "";
    
    public static DataSource createDataSource(final String dataSourceName) {
        BasicDataSource result = new BasicDataSource();
        result.setDriverClassName(com.mysql.jdbc.Driver.class.getName());
        result.setUrl(URL_PREFIX + dataSourceName);
        result.setUsername(USER_NAME);
        result.setPassword(PASSWORD);
        return result;
    }
}
