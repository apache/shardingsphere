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

package org.apache.shardingsphere.underlying.common.database.type;

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.spi.database.type.BranchDatabaseType;
import org.apache.shardingsphere.spi.database.type.DatabaseType;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * Database types.
 * 
 * @author zhangliang
 */
public final class DatabaseTypes {
    
    private static final Map<String, DatabaseType> DATABASE_TYPES = new HashMap<>();
    
    static {
        for (DatabaseType each : ServiceLoader.load(DatabaseType.class)) {
            DATABASE_TYPES.put(each.getName(), each);
        }
    }
    
    /**
     * Get name of trunk database type.
     * 
     * @param databaseType database type
     * @return name of trunk database type
     */
    public static String getTrunkDatabaseTypeName(final DatabaseType databaseType) {
        return databaseType instanceof BranchDatabaseType ? ((BranchDatabaseType) databaseType).getTrunkDatabaseType().getName() : databaseType.getName();
    }
    
    /**
     * Get trunk database type.
     *
     * @param name database name 
     * @return trunk database type
     */
    public static DatabaseType getTrunkDatabaseType(final String name) {
        return DATABASE_TYPES.get(name) instanceof BranchDatabaseType ? ((BranchDatabaseType) DATABASE_TYPES.get(name)).getTrunkDatabaseType() : getActualDatabaseType(name);
    }
    
    /**
     * Get actual database type.
     *
     * @param name database name 
     * @return actual database type
     */
    public static DatabaseType getActualDatabaseType(final String name) {
        Preconditions.checkState(DATABASE_TYPES.containsKey(name), "Unsupported database: '%s'", name);
        return DATABASE_TYPES.get(name);
    }
    
    /**
     * Get database type by URL.
     * 
     * @param url database URL
     * @return database type
     */
    public static DatabaseType getDatabaseTypeByURL(final String url) {
        for (DatabaseType each : DATABASE_TYPES.values()) {
            if (matchStandardURL(url, each) || matchURLAlias(url, each)) {
                return each;
            }
        }
        return DATABASE_TYPES.get("SQL92");
    }
    
    private static boolean matchStandardURL(final String url, final DatabaseType databaseType) {
        return url.startsWith(String.format("jdbc:%s:", databaseType.getName().toLowerCase()));
    }
    
    private static boolean matchURLAlias(final String url, final DatabaseType databaseType) {
        for (String each : databaseType.getJdbcUrlPrefixAlias()) {
            if (url.startsWith(each)) {
                return true;
            }
        }
        return false;
    }
}
