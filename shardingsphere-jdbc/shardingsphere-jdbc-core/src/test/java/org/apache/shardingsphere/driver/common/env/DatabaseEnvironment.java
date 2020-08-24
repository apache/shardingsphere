/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.driver.common.env;

import lombok.Getter;
import org.apache.shardingsphere.infra.database.type.DatabaseTypes;
import org.apache.shardingsphere.infra.database.type.DatabaseType;

import java.util.HashMap;
import java.util.Map;

public final class DatabaseEnvironment {
    
    private static final int INIT_CAPACITY = 5;
    
    private static final Map<DatabaseType, String> DRIVER_CLASS_NAME = new HashMap<>(INIT_CAPACITY, 1);
    
    private static final Map<DatabaseType, String> URL = new HashMap<>(INIT_CAPACITY, 1);
    
    private static final Map<DatabaseType, String> USERNAME = new HashMap<>(INIT_CAPACITY, 1);
    
    private static final Map<DatabaseType, String> PASSWORD = new HashMap<>(INIT_CAPACITY, 1);
    
    private static final Map<DatabaseType, String> SCHEMA = new HashMap<>(INIT_CAPACITY, 1);
    
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
        DatabaseType databaseType = DatabaseTypes.getActualDatabaseType("H2");
        DRIVER_CLASS_NAME.put(databaseType, "org.h2.Driver");
        URL.put(databaseType, "jdbc:h2:mem:%s;DATABASE_TO_UPPER=false;MODE=MySQL");
        USERNAME.put(databaseType, "sa");
        PASSWORD.put(databaseType, "");
        SCHEMA.put(databaseType, null);
    }
    
    private void fillMySQL() {
        DatabaseType databaseType = DatabaseTypes.getActualDatabaseType("MySQL");
        DRIVER_CLASS_NAME.put(databaseType, "com.mysql.jdbc.Driver");
        URL.put(databaseType, "jdbc:mysql://db.mysql:3306/%s?serverTimezone=UTC&useSSL=false");
        USERNAME.put(databaseType, "root");
        PASSWORD.put(databaseType, "");
        SCHEMA.put(databaseType, null);
    }
    
    private void fillPostgreSQL() {
        DatabaseType databaseType = DatabaseTypes.getActualDatabaseType("PostgreSQL");
        DRIVER_CLASS_NAME.put(databaseType, "org.postgresql.Driver");
        URL.put(databaseType, "jdbc:postgresql://db.psql:5432/%s");
        USERNAME.put(databaseType, "postgres");
        PASSWORD.put(databaseType, "");
        SCHEMA.put(databaseType, null);
    }
    
    private void fillSQLServer() {
        DatabaseType databaseType = DatabaseTypes.getActualDatabaseType("SQLServer");
        DRIVER_CLASS_NAME.put(databaseType, "com.microsoft.sqlserver.jdbc.SQLServerDriver");
        URL.put(databaseType, "jdbc:sqlserver://db.mssql:1433;DatabaseName=%s");
        USERNAME.put(databaseType, "sa");
        PASSWORD.put(databaseType, "Jdbc1234");
        SCHEMA.put(databaseType, null);
    }
    
    private void fillOracle() {
        DatabaseType databaseType = DatabaseTypes.getActualDatabaseType("Oracle");
        DRIVER_CLASS_NAME.put(databaseType, "oracle.jdbc.driver.OracleDriver");
        URL.put(databaseType, "jdbc:oracle:thin:@db.oracle:1521/test");
        USERNAME.put(databaseType, "jdbc");
        PASSWORD.put(databaseType, "jdbc");
        SCHEMA.put(databaseType, "%s");
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
