package io.shardingjdbc.dbtest.common;

import io.shardingjdbc.core.constant.DatabaseType;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ConfigRuntime {


    public static Set<String> getDbs() {
        Map<String, String> dbs = ConfigUtils.getDatas("database");
        Set<String> result = new HashSet<>();
        for (Map.Entry<String, String> each : dbs.entrySet()) {
            String[] dbsts = each.getKey().split("\\.");
            result.add(dbsts[1]);
        }
        return result;
    }

    private static String getJdbcConfig(final String key) {
        return ConfigUtils.getString("database." + key, "");
    }

    public static String getDriverClassName(final DatabaseType type) {
        switch (type) {
        case H2:
            return getJdbcConfig("h2.driver");
        case MySQL:
            return getJdbcConfig("mysql.driver");
        case Oracle:
            return getJdbcConfig("oracle.driver");
        case SQLServer:
            return getJdbcConfig("sqlserver.driver");
        case PostgreSQL:
            return getJdbcConfig("postgresql.driver");
        default:
            return getJdbcConfig("h2.driver");
        }
    }

    public static String getURL(final DatabaseType type, final String dbName) {
        switch (type) {
        case H2:
            return String.format(getJdbcConfig("h2.url"), dbName);
        case MySQL:
            return String.format(getJdbcConfig("mysql.url"), dbName);
        case Oracle:
            return String.format(getJdbcConfig("oracle.url"), dbName);
        case SQLServer:
            return String.format(getJdbcConfig("sqlserver.url"), dbName);
        case PostgreSQL:
            return String.format(getJdbcConfig("postgresql.url"), dbName);
        default:
            return String.format(getJdbcConfig("h2.url"), dbName);
        }
    }

    public static String getUsername(final DatabaseType type) {
        switch (type) {
        case H2:
            return getJdbcConfig("h2.username");
        case MySQL:
            return getJdbcConfig("mysql.username");
        case Oracle:
            return getJdbcConfig("oracle.username");
        case SQLServer:
            return getJdbcConfig("sqlserver.username");
        case PostgreSQL:
            return getJdbcConfig("postgresql.username");
        default:
            return getJdbcConfig("h2.driver");
        }
    }

    public static String getPassword(final DatabaseType type) {
        switch (type) {
        case H2:
            return getJdbcConfig("h2.password");
        case MySQL:
            return getJdbcConfig("mysql.password");
        case Oracle:
            return getJdbcConfig("oracle.password");
        case SQLServer:
            return getJdbcConfig("sqlserver.password");
        case PostgreSQL:
            return getJdbcConfig("postgresql.password");
        default:
            return getJdbcConfig("h2.driver");
        }
    }

    public static String getDefualtdb(final DatabaseType type) {
        switch (type) {
        case H2:
            return getJdbcConfig("h2.defualtdb");
        case MySQL:
            return getJdbcConfig("mysql.defualtdb");
        case Oracle:
            return getJdbcConfig("oracle.defualtdb");
        case SQLServer:
            return getJdbcConfig("sqlserver.defualtdb");
        case PostgreSQL:
            return getJdbcConfig("postgresql.defualtdb");
        default:
            return getJdbcConfig("h2.defualtdb");
        }
    }

    public static String getAssertPath() {
        return ConfigUtils.getString("assert.path", null);
    }

    public static boolean isInitialized() {
        return Boolean.valueOf(ConfigUtils.getString("initialized", "false"));
    }
}
