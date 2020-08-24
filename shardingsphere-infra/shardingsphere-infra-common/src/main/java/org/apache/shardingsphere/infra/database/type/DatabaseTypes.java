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

package org.apache.shardingsphere.infra.database.type;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;

/**
 * Database types.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
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
        return Optional.ofNullable(DATABASE_TYPES.get(name)).orElseThrow(() -> new ShardingSphereException("Unsupported database:'%s'", name));
    }
    
    /**
     * Get database type by URL.
     * 
     * @param url database URL
     * @return database type
     */
    public static DatabaseType getDatabaseTypeByURL(final String url) {
        return DATABASE_TYPES.values().stream().filter(each -> matchURLs(url, each)).findAny().orElse(DATABASE_TYPES.get("SQL92"));
    }
    
    private static boolean matchURLs(final String url, final DatabaseType databaseType) {
        return databaseType.getJdbcUrlPrefixes().stream().anyMatch(url::startsWith);
    }
}
