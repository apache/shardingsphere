/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingjdbc.dbtest.common;

import io.shardingjdbc.core.constant.DatabaseType;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ConfigRuntime {

    /**
     * Returns all databases.
     * @return databases
     */
    public static Set<String> getDbs() {
        Map<String, String> dbs = ConfigUtils.getDatas("database");
        Set<String> result = new HashSet<>();
        for (Map.Entry<String, String> each : dbs.entrySet()) {
            String[] dbsts = each.getKey().split("\\.");
            result.add(dbsts[1]);
        }
        return result;
    }

    /**
     *  Jdbc Config.
     * @param key key
     * @return database data
     */
    private static String getJdbcConfig(final String key) {
        return ConfigUtils.getString("database." + key, "");
    }

    /**
     *  query Driver Class Name.
     * @param type Database Type
     * @return  Driver Class Name
     */
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

    /**
     * query jdbc url.
     * @param type Database Type
     * @param dbName database name
     * @return url
     */
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

    /**
     * query database username.
     * @param type Database Type
     * @return username
     */
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

    /**
     * query database password.
     * @param type Database Type
     * @return password
     */
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

    /**
     * query database password.
     * @param type Database Type
     * @return Defualt database
     */
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

    /**
     * query Assert Path.
     * @return Assert Path
     */
    public static String getAssertPath() {
        return ConfigUtils.getString("assert.path", null);
    }

    /**
     * initialized.
     * @return initialized
     */
    public static boolean isInitialized() {
        return Boolean.valueOf(ConfigUtils.getString("initialized", "false"));
    }
}
