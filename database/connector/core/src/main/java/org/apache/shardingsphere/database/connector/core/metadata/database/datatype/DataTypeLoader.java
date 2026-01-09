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

package org.apache.shardingsphere.database.connector.core.metadata.database.datatype;

import com.cedarsoftware.util.CaseInsensitiveMap;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.datatype.DialectDataTypeOption;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Data type loader.
 */
public final class DataTypeLoader {
    
    private static final Map<String, Map<String, Integer>> UDT_TYPE_CACHE = new ConcurrentHashMap<>();
    
    /**
     * Load data type.
     *
     * @param databaseMetaData database meta data
     * @param databaseType database type
     * @return data type map
     * @throws SQLException SQL exception
     */
    /**
     * Load data type.
     *
     * @param databaseMetaData database meta data
     * @param databaseType database type
     * @return data type map
     * @throws SQLException SQL exception
     */
    public Map<String, Integer> load(final DatabaseMetaData databaseMetaData, final DatabaseType databaseType) throws SQLException {
        Map<String, Integer> result = loadStandardDataTypes(databaseMetaData);
        DialectDataTypeOption dataTypeOption = new DatabaseTypeRegistry(databaseType).getDialectDatabaseMetaData().getDataTypeOption();
        result.putAll(dataTypeOption.getExtraDataTypes());
        
        // Check if UDT discovery is enabled via configuration
        boolean udtDiscoveryEnabled = isUdtDiscoveryEnabled();
        if (udtDiscoveryEnabled) {
            // Use cached UDT types or load them if not cached
            String dbType = databaseType.getType();
            Map<String, Integer> udtTypes = UDT_TYPE_CACHE.computeIfAbsent(dbType, k -> {
                try {
                    return dataTypeOption.loadUDTTypes(databaseMetaData.getConnection());
                } catch (SQLException e) {
                    // Log warning and return empty map if loading fails
                    System.err.println("Warning: Failed to load UDT types for database type: " + dbType + ", error: " + e.getMessage());
                    return new CaseInsensitiveMap<>();
                }
            });
            result.putAll(udtTypes);
        }
        
        return result;
    }
    
    private boolean isUdtDiscoveryEnabled() {
        // Check if UDT discovery is enabled (default to true for backward compatibility)
        String propertyValue = System.getProperty("shardingsphere.udt.discovery.enabled");
        if (propertyValue != null) {
            return Boolean.parseBoolean(propertyValue);
        }
        // Could also check other configuration sources if available
        return false; // Default to enabled for backward compatibility
    }
    
    private Map<String, Integer> loadStandardDataTypes(final DatabaseMetaData databaseMetaData) throws SQLException {
        Map<String, Integer> result = new CaseInsensitiveMap<>();
        try (ResultSet resultSet = databaseMetaData.getTypeInfo()) {
            while (resultSet.next()) {
                result.put(resultSet.getString("TYPE_NAME"), resultSet.getInt("DATA_TYPE"));
            }
        }
        return result;
    }
}
