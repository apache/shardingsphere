package io.shardingjdbc.console.entity;

import io.shardingjdbc.console.constant.JdbcDriver;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Database connector.
 * 
 * @author panjuan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DBConnector {
    
    /**
     * Get database connection.
     * 
     * @param username username
     * @param password password
     * @param url database url
     * @param dbDriver database driver
     * @return connection or null
     * @throws ClassNotFoundException class not found exception
     * @throws SQLException SQL exception
     */
    public static Connection getConnection(final String username, final String password, final String url, final String dbDriver) throws ClassNotFoundException, SQLException {
        JdbcDriver driver = JdbcDriver.valueOf(dbDriver + "Driver");
        Class.forName(driver.getDriverName());
        DriverManager.setLoginTimeout(5);
        return DriverManager.getConnection("jdbc:" + driver.getDbName() + "://" + url, username, password);
    }
}
