package io.shardingjdbc.console.constant;

import lombok.Getter;

/**
 * jdbcDriver.
 *
 * @author panjuan
 */
@Getter
public enum JdbcDriver {
    
    MySQLDriver("mysql", "com.mysql.jdbc.Driver"),
    OracleDriver("oracle", "oracle.jdbc.driver.OracleDriver");
    
    private String dbName;
    private String driverName;
    
    JdbcDriver(final String dbName, final String driverName) {
        this.dbName = dbName;
        this.driverName = driverName;
    }
}
