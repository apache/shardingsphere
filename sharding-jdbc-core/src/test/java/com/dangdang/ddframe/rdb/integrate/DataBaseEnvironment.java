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

import java.util.HashMap;
import java.util.Map;

import com.dangdang.ddframe.rdb.sharding.constant.DatabaseType;

import lombok.Getter;

public final class DataBaseEnvironment {
    
    private static final Map<DatabaseType, Class<?>> DRIVER_CLASS_NAME = new HashMap<>(2);
    
    private static final Map<DatabaseType, String> URL = new HashMap<>(2);
    
    private static final Map<DatabaseType, String> USERNAME = new HashMap<>(2);
    
    private static final Map<DatabaseType, String> PASSWORD = new HashMap<>(2);
    
    @Getter
    private final DatabaseType databaseType;
    
    public DataBaseEnvironment(final DatabaseType databaseType) {
        this.databaseType = databaseType;
        fillData();
    }
    
    private void fillData() {
        DRIVER_CLASS_NAME.put(DatabaseType.H2, org.h2.Driver.class);
        DRIVER_CLASS_NAME.put(DatabaseType.MySQL, com.mysql.jdbc.Driver.class);
        URL.put(DatabaseType.H2, "jdbc:h2:mem:%s;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MYSQL");
        URL.put(DatabaseType.MySQL, "jdbc:mysql://localhost:3306/%s");
        USERNAME.put(DatabaseType.H2, "sa");
        USERNAME.put(DatabaseType.MySQL, "root");
        PASSWORD.put(DatabaseType.H2, "");
        PASSWORD.put(DatabaseType.MySQL, "");
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
