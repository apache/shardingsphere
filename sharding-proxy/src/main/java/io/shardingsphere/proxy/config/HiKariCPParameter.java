package io.shardingsphere.proxy.config;

import lombok.Data;

/**
 * @author Pramy
 */
@Data
public class HiKariCPParameter {

    /**
     * Currently only supported mysql
     */
    private final String driverClass = "com.mysql.jdbc.Driver";

    private Boolean isAutoCommit = Boolean.TRUE;

    private int maxPoolSize = Runtime.getRuntime().availableProcessors() * 2 + 1;

    private int connectionTimeout = 30000;

    private int idleTimeout = 60000;

    private int maxLifetime = 1800000;
}
