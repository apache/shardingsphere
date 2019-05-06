/*
 * Copyright 2016-2018 shardingsphere.io.
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

package io.shardingsphere.shardingjdbc.common.env;

import io.shardingsphere.core.constant.DatabaseType;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public final class DatabaseEnvironment {
    
    private static final int INIT_CAPACITY = 5;
    
    private static final Map<DatabaseType, String> DRIVER_CLASS_NAME = new HashMap<>(INIT_CAPACITY);
    
    private static final Map<DatabaseType, String> URL = new HashMap<>(INIT_CAPACITY);
    
    private static final Map<DatabaseType, String> USERNAME = new HashMap<>(INIT_CAPACITY);
    
    private static final Map<DatabaseType, String> PASSWORD = new HashMap<>(INIT_CAPACITY);
    
    private static final Map<DatabaseType, String> SCHEMA = new HashMap<>(INIT_CAPACITY);
    
    @Getter
    private final DatabaseType databaseType;
    
    public DatabaseEnvironment(final DatabaseType databaseType) {
        this.databaseType = databaseType;
        fillData();
    }
    
    private void fillData() {
        fillH2();
        fillMySQL();
        fillPostgreSQL();
        fillSQLServer();
        fillOracle();
    }
    
    private void fillH2() {
        DRIVER_CLASS_NAME.put(DatabaseType.H2, "org.h2.Driver");
        URL.put(DatabaseType.H2, "jdbc:h2:mem:%s;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL");
        USERNAME.put(DatabaseType.H2, "sa");
        PASSWORD.put(DatabaseType.H2, "");
        SCHEMA.put(DatabaseType.H2, null);
    }
    
    private void fillMySQL() {
        DRIVER_CLASS_NAME.put(DatabaseType.MySQL, "com.mysql.jdbc.Driver");
        URL.put(DatabaseType.MySQL, "jdbc:mysql://db.mysql:3306/%s?serverTimezone=UTC&useSSL=false");
        USERNAME.put(DatabaseType.MySQL, "root");
        PASSWORD.put(DatabaseType.MySQL, "");
        SCHEMA.put(DatabaseType.MySQL, null);
    }
    
    private void fillPostgreSQL() {
        DRIVER_CLASS_NAME.put(DatabaseType.PostgreSQL, "org.postgresql.Driver");
        URL.put(DatabaseType.PostgreSQL, "jdbc:postgresql://db.psql:5432/%s");
        USERNAME.put(DatabaseType.PostgreSQL, "postgres");
        PASSWORD.put(DatabaseType.PostgreSQL, "");
        SCHEMA.put(DatabaseType.PostgreSQL, null);
    }
    
    private void fillSQLServer() {
        DRIVER_CLASS_NAME.put(DatabaseType.SQLServer, "com.microsoft.sqlserver.jdbc.SQLServerDriver");
        URL.put(DatabaseType.SQLServer, "jdbc:sqlserver://db.mssql:1433;DatabaseName=%s");
        USERNAME.put(DatabaseType.SQLServer, "sa");
        PASSWORD.put(DatabaseType.SQLServer, "Jdbc1234");
        SCHEMA.put(DatabaseType.SQLServer, null);
    }
    
    private void fillOracle() {
        DRIVER_CLASS_NAME.put(DatabaseType.Oracle, "oracle.jdbc.driver.OracleDriver");
        URL.put(DatabaseType.Oracle, "jdbc:oracle:thin:@db.oracle:1521:test");
        USERNAME.put(DatabaseType.Oracle, "jdbc");
        PASSWORD.put(DatabaseType.Oracle, "jdbc");
        SCHEMA.put(DatabaseType.Oracle, "%s");
    }
    
    /**
     * Get driver class name.
     * 
     * @return driver class name
     */
    public String getDriverClassName() {
        return DRIVER_CLASS_NAME.get(databaseType);
    }
    
    /**
     * Get URL.
     * 
     * @param dbName database name
     * @return database URL
     */
    public String getURL(final String dbName) {
        return String.format(URL.get(databaseType), dbName);
    }
    
    /**
     * Get username.
     * 
     * @return username
     */
    public String getUsername() {
        return USERNAME.get(databaseType);
    }
    
    /**
     * Get password.
     * 
     * @return password
     */
    public String getPassword() {
        return PASSWORD.get(databaseType);
    }
    
    /**
     * Get schema.
     * 
     * @param dbName database name
     * @return schema
     */
    public String getSchema(final String dbName) {
        return null == SCHEMA.get(databaseType) ? null : String.format(SCHEMA.get(databaseType), dbName);
    }
}
