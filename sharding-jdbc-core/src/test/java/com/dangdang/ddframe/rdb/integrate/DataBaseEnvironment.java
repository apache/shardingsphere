/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.rdb.integrate;

import com.dangdang.ddframe.rdb.sharding.constant.DatabaseType;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public final class DataBaseEnvironment {
    
    private static final int INIT_CAPACITY = 3;
    
    private static final Map<DatabaseType, Class<?>> DRIVER_CLASS_NAME = new HashMap<>(INIT_CAPACITY);
    
    private static final Map<DatabaseType, String> URL = new HashMap<>(INIT_CAPACITY);
    
    private static final Map<DatabaseType, String> USERNAME = new HashMap<>(INIT_CAPACITY);
    
    private static final Map<DatabaseType, String> PASSWORD = new HashMap<>(INIT_CAPACITY);
    
    @Getter
    private final DatabaseType databaseType;
    
    public DataBaseEnvironment(final DatabaseType databaseType) {
        this.databaseType = databaseType;
        fillData();
    }
    
    private void fillData() {
        DRIVER_CLASS_NAME.put(DatabaseType.H2, org.h2.Driver.class);
        URL.put(DatabaseType.H2, "jdbc:h2:mem:%s;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MYSQL");
        USERNAME.put(DatabaseType.H2, "sa");
        PASSWORD.put(DatabaseType.H2, "");
        
        DRIVER_CLASS_NAME.put(DatabaseType.MySQL, com.mysql.jdbc.Driver.class);
        URL.put(DatabaseType.MySQL, "jdbc:mysql://localhost:3306/%s");
        USERNAME.put(DatabaseType.MySQL, "root");
        PASSWORD.put(DatabaseType.MySQL, "");
    
        DRIVER_CLASS_NAME.put(DatabaseType.PostgreSQL, org.postgresql.Driver.class);
        URL.put(DatabaseType.PostgreSQL, "jdbc:postgresql://localhost:5432/%s");
        USERNAME.put(DatabaseType.PostgreSQL, "postgres");
        PASSWORD.put(DatabaseType.PostgreSQL, "");
    }
    
    public String getDriverClassName() {
        return DRIVER_CLASS_NAME.get(databaseType).getName();
    }
    
    public String getURL(final String dbName) {
        return String.format(URL.get(databaseType), dbName);
    }
    
    public String getUsername() {
        return USERNAME.get(databaseType);
    }
    
    public String getPassword() {
        return PASSWORD.get(databaseType);
    }
}
