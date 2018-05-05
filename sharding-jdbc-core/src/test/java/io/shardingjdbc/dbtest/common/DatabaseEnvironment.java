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
import lombok.Getter;
import org.apache.commons.dbcp2.BasicDataSource;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class DatabaseEnvironment {
    
    private static final int INIT_CAPACITY = 5;
    
    private static final Map<DatabaseType, String> DRIVER_CLASS_NAME = new HashMap<>(INIT_CAPACITY);
    
    private static final Map<DatabaseType, String> URL = new HashMap<>(INIT_CAPACITY);
    
    private static final Map<DatabaseType, String> USERNAME = new HashMap<>(INIT_CAPACITY);
    
    private static final Map<DatabaseType, String> PASSWORD = new HashMap<>(INIT_CAPACITY);
    
    private static final Map<DatabaseType, String> SCHEMA = new HashMap<>(INIT_CAPACITY);
    
    private static final Map<DatabaseType, String> DATABASE_NAME = new HashMap<>(INIT_CAPACITY);
    
    @Getter
    private final DatabaseType databaseType;
    
    public DatabaseEnvironment(final DatabaseType databaseType) {
        this.databaseType = databaseType;
        fillData();
    }
    
    private void fillData() {
        DRIVER_CLASS_NAME.put(DatabaseType.H2, org.h2.Driver.class.getName());
        URL.put(DatabaseType.H2, "jdbc:h2:mem:%s;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL");
        USERNAME.put(DatabaseType.H2, "sa");
        PASSWORD.put(DatabaseType.H2, "");
        SCHEMA.put(DatabaseType.H2, null);
        DATABASE_NAME.put(DatabaseType.H2, "mydb");
        
        DRIVER_CLASS_NAME.put(DatabaseType.MySQL, com.mysql.jdbc.Driver.class.getName());
        URL.put(DatabaseType.MySQL, "jdbc:mysql://db.mysql:3306/%s");
        USERNAME.put(DatabaseType.MySQL, "root");
        PASSWORD.put(DatabaseType.MySQL, "");
        SCHEMA.put(DatabaseType.MySQL, null);
        DATABASE_NAME.put(DatabaseType.MySQL, "mysql");
        
        DRIVER_CLASS_NAME.put(DatabaseType.PostgreSQL, org.postgresql.Driver.class.getName());
        URL.put(DatabaseType.PostgreSQL, "jdbc:postgresql://db.psql:5432/%s");
        USERNAME.put(DatabaseType.PostgreSQL, "postgres");
        PASSWORD.put(DatabaseType.PostgreSQL, "");
        SCHEMA.put(DatabaseType.PostgreSQL, null);
        DATABASE_NAME.put(DatabaseType.PostgreSQL, "PotgreSQL");
        
        DRIVER_CLASS_NAME.put(DatabaseType.SQLServer, com.microsoft.sqlserver.jdbc.SQLServerDriver.class.getName());
        URL.put(DatabaseType.SQLServer, "jdbc:sqlserver://db.mssql:1433;DatabaseName=%s");
        USERNAME.put(DatabaseType.SQLServer, "sa");
        PASSWORD.put(DatabaseType.SQLServer, "Jdbc1234");
        SCHEMA.put(DatabaseType.SQLServer, null);
        DATABASE_NAME.put(DatabaseType.SQLServer, "master");
        
        DRIVER_CLASS_NAME.put(DatabaseType.Oracle, "oracle.jdbc.driver.OracleDriver");
        URL.put(DatabaseType.Oracle, "jdbc:oracle:thin:@db.oracle:8521:%s");
        USERNAME.put(DatabaseType.Oracle, "jdbc");
        PASSWORD.put(DatabaseType.Oracle, "jdbc");
        SCHEMA.put(DatabaseType.Oracle, "%s");
        DATABASE_NAME.put(DatabaseType.Oracle, "sys");
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
        return String.format(URL.get(databaseType), null == dbName ? DATABASE_NAME.get(databaseType) : dbName);
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
     * Create data source.
     * 
     * @param dbName database name
     * @return data source
     */
    public DataSource createDataSource(final String dbName) {
        BasicDataSource result = new BasicDataSource();
        DatabaseEnvironment dbEnv = new DatabaseEnvironment(databaseType);
        result.setDriverClassName(dbEnv.getDriverClassName());
        result.setUrl(dbEnv.getURL(dbName));
        result.setUsername(dbEnv.getUsername());
        result.setPassword(dbEnv.getPassword());
        result.setMaxTotal(1);
        if (DatabaseType.Oracle == databaseType) {
            result.setConnectionInitSqls(Collections.singleton("ALTER SESSION SET CURRENT_SCHEMA = " + dbName));
        }
        return result;
    }
}
