package io.shardingjdbc.console.entity;

import io.shardingjdbc.console.constant.ConnDriver;

import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnector {

    public static Connection getConnection(String username,String password, String url, String driverDB)
    {
        ConnDriver driver = ConnDriver.valueOf(driverDB+"Driver");
        try {
            System.out.println(driver.getDriverName());
            Class.forName(driver.getDriverName());
            DriverManager.setLoginTimeout(5);
            Connection conn = DriverManager.getConnection("jdbc:"+driver.getDbName()+"://" + url, username, password);
            return conn;
        }catch (Exception e){
            System.out.println(e);
            return null; }

    }

}