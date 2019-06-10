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

package org.apache.shardingsphere.spi;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * Database types.
 * 
 * @author zhangliang
 */
public final class DatabaseTypes {
    
    private static final Map<String, DbType> DATABASE_TYPES = new HashMap<>();
    
    static {
        for (DbType each : ServiceLoader.load(DbType.class)) {
            DATABASE_TYPES.put(each.getName(), each);
        }
    }
    
    /**
     * Get database types.
     * 
     * @return database types
     */
    public static Collection<DbType> getDatabaseTypes() {
        return DATABASE_TYPES.values();
    }
    
    /**
     * Get actual database type.
     *
     * @param name database name 
     * @return actual database type
     */
    public static DbType getActualDatabaseType(final String name) {
        if (!DATABASE_TYPES.containsKey(name)) {
            throw new UnsupportedOperationException(String.format("Unsupported database: '%s'", name));
        }
        return DATABASE_TYPES.get(name);
    }
    
    /**
     * Get trunk database type.
     *
     * @param name database name 
     * @return trunk database type
     */
    public static DbType getTrunkDatabaseType(final String name) {
        return DATABASE_TYPES.get(name) instanceof BranchDatabaseType ? ((BranchDatabaseType) DATABASE_TYPES.get(name)).getTrunkDatabaseType() : getActualDatabaseType(name);
    }
    
    /**
     * Get actual database type.
     *
     * @param productName database product name 
     * @return actual database type
     */
    public static DbType getActualDatabaseTypeByProductName(final String productName) {
        for (DbType each : DATABASE_TYPES.values()) {
            if (each.getProductName().equals(productName)) {
                return each;
            }
        }
        throw new UnsupportedOperationException(String.format("Unsupported database from product name: '%s'", productName));
    }
}
