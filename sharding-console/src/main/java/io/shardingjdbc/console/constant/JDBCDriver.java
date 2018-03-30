package io.shardingjdbc.console.constant;

import lombok.Getter;

/**
 * JDBC driver.
 * 
 * @author panjuan
 */
@Getter
public enum JDBCDriver {
    
    MySQLDriver("mysql", "com.mysql.jdbc.Driver"),
    OracleDriver("oracle", "oracle.jdbc.driver.OracleDriver");
    
    private String dbName;
    private String driverName;
    
    JDBCDriver(final String dbName, final String driverName) {
        this.dbName = dbName;
        this.driverName = driverName;
    }
}
