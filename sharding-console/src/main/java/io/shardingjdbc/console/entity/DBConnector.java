package io.shardingjdbc.console.entity;

import io.shardingjdbc.console.constant.ConnDriver;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnector {

    /**
     * to get db connection.
     * @param username username
     * @param password pwd
     * @param url db url
     * @param driverDB driver
     * @return Connection or null
     */
    public static Connection getConnection(final String username, final String password, final String url, final String driverDB) {
        ConnDriver driver = ConnDriver.valueOf(driverDB + "Driver");
        try {
            System.out.println(driver.getDriverName());
            Class.forName(driver.getDriverName());
            DriverManager.setLoginTimeout(5);
            Connection conn = DriverManager.getConnection("jdbc:" + driver.getDbName() + "://" + url, username, password);
            return conn;
        } catch (ClassNotFoundException | SQLException e) {
            System.out.println(e);
            return null;
        }
    }
}