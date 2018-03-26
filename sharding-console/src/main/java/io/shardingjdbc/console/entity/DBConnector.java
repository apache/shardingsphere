package io.shardingjdbc.console.entity;

import io.shardingjdbc.console.constant.JdbcDriver;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnector {

    /**
     * get db connection.
     * @param username username
     * @param password pwd
     * @param url db url
     * @param driverDB driver
     * @return Connection or null
     */
    public static Connection getConnection(final String username, final String password, final String url, final String driverDB) {
        JdbcDriver driver = JdbcDriver.valueOf(driverDB + "Driver");
        try {
            Class.forName(driver.getDriverName());
            DriverManager.setLoginTimeout(5);
            Connection connection = DriverManager.getConnection("jdbc:" + driver.getDbName() + "://" + url, username, password);
            return connection;
        } catch (ClassNotFoundException | SQLException ex) {
            return null;
        }
    }
}