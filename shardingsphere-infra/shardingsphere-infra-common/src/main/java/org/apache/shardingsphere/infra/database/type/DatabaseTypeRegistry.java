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
import org.apache.shardingsphere.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.spi.type.typed.TypedSPI;
import org.apache.shardingsphere.spi.type.typed.TypedSPIRegistry;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Database type registry.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DatabaseTypeRegistry {
    
    private static final String DEFAULT_DATABASE_TYPE = "MySQL";
    
    static {
        ShardingSphereServiceLoader.register(DatabaseType.class);
    }
    
    /**
     * Get name of trunk database type.
     * 
     * @param databaseType database type
     * @return name of trunk database type
     */
    public static String getTrunkDatabaseTypeName(final DatabaseType databaseType) {
        return databaseType instanceof BranchDatabaseType ? ((BranchDatabaseType) databaseType).getTrunkDatabaseType().getType() : databaseType.getType();
    }
    
    /**
     * Get trunk database type.
     *
     * @param name database name 
     * @return trunk database type
     */
    public static DatabaseType getTrunkDatabaseType(final String name) {
        DatabaseType databaseType = TypedSPIRegistry.getRegisteredService(DatabaseType.class, name);
        return databaseType instanceof BranchDatabaseType ? ((BranchDatabaseType) databaseType).getTrunkDatabaseType() : getActualDatabaseType(name);
    }
    
    /**
     * Get actual database type.
     *
     * @param name database name 
     * @return actual database type
     */
    public static DatabaseType getActualDatabaseType(final String name) {
        return TypedSPIRegistry.getRegisteredService(DatabaseType.class, name);
    }
    
    /**
     * Get database type by URL.
     * 
     * @param url database URL
     * @return database type
     */
    public static DatabaseType getDatabaseTypeByURL(final String url) {
        Collection<DatabaseType> databaseType = ShardingSphereServiceLoader.getServiceInstances(DatabaseType.class);
        return databaseType.stream().filter(each -> matchURLs(url, each)).findAny().orElseGet(() -> TypedSPIRegistry.getRegisteredService(DatabaseType.class, "SQL92"));
    }
    
    private static boolean matchURLs(final String url, final DatabaseType databaseType) {
        return databaseType.getJdbcUrlPrefixes().stream().anyMatch(url::startsWith);
    }
    
    /**
     * Get default database type.
     * 
     * @return default database type
     */
    public static DatabaseType getDefaultDatabaseType() {
        return TypedSPIRegistry.getRegisteredService(DatabaseType.class, DEFAULT_DATABASE_TYPE);
    }
    
    /**
     * Get all database type names.
     *
     * @return database type names
     */
    public static Collection<String> getDatabaseTypeNames() {
        return ShardingSphereServiceLoader.getServiceInstances(DatabaseType.class).stream().map(TypedSPI::getType).collect(Collectors.toList());
    }
}
