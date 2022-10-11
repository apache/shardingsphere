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

package org.apache.shardingsphere.infra.metadata.database;

import org.apache.shardingsphere.infra.config.database.DatabaseConfiguration;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeEngine;
import org.apache.shardingsphere.infra.datasource.state.DataSourceStateManager;
import org.apache.shardingsphere.infra.instance.InstanceContext;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ShardingSphere databases factory.
 */
public final class ShardingSphereDatabasesFactory {
    
    /**
     * Create databases.
     *
     * @param databaseName database name
     * @param databaseConfig database configuration
     * @param props properties
     * @param instanceContext instance context
     * @return created database
     * @throws SQLException SQL exception
     */
    public static ShardingSphereDatabase create(final String databaseName, final DatabaseConfiguration databaseConfig,
                                                final ConfigurationProperties props, final InstanceContext instanceContext) throws SQLException {
        DatabaseType protocolType = DatabaseTypeEngine.getProtocolType(databaseName, databaseConfig, props);
        DatabaseType storageType = DatabaseTypeEngine.getDatabaseType(DataSourceStateManager.getInstance().getEnabledDataSources(databaseName, databaseConfig));
        return ShardingSphereDatabase.create(databaseName, protocolType, storageType, databaseConfig, props, instanceContext);
    }
    
    /**
     * Create databases.
     * 
     * @param databaseConfigMap database configuration map
     * @param props properties
     * @param instanceContext instance context
     * @return databases
     * @throws SQLException SQL exception
     */
    public static Map<String, ShardingSphereDatabase> create(final Map<String, DatabaseConfiguration> databaseConfigMap,
                                                             final ConfigurationProperties props, final InstanceContext instanceContext) throws SQLException {
        DatabaseType protocolType = DatabaseTypeEngine.getProtocolType(databaseConfigMap, props);
        DatabaseType storageType = DatabaseTypeEngine.getStorageType(databaseConfigMap);
        Map<String, ShardingSphereDatabase> result = new ConcurrentHashMap<>(databaseConfigMap.size() + protocolType.getSystemDatabaseSchemaMap().size(), 1);
        result.putAll(createGenericDatabases(databaseConfigMap, protocolType, storageType, props, instanceContext));
        result.putAll(createSystemDatabases(databaseConfigMap, protocolType));
        return result;
    }
    
    private static Map<String, ShardingSphereDatabase> createGenericDatabases(final Map<String, DatabaseConfiguration> databaseConfigMap, final DatabaseType protocolType,
                                                                              final DatabaseType storageType, final ConfigurationProperties props,
                                                                              final InstanceContext instanceContext) throws SQLException {
        Map<String, ShardingSphereDatabase> result = new HashMap<>(databaseConfigMap.size(), 1);
        for (Entry<String, DatabaseConfiguration> entry : databaseConfigMap.entrySet()) {
            String databaseName = entry.getKey();
            if (!entry.getValue().getDataSources().isEmpty() || !protocolType.getSystemSchemas().contains(databaseName)) {
                result.put(databaseName.toLowerCase(), ShardingSphereDatabase.create(databaseName, protocolType, storageType, entry.getValue(), props, instanceContext));
            }
        }
        return result;
    }
    
    private static Map<String, ShardingSphereDatabase> createSystemDatabases(final Map<String, DatabaseConfiguration> databaseConfigMap, final DatabaseType protocolType) {
        Map<String, ShardingSphereDatabase> result = new HashMap<>(protocolType.getSystemDatabaseSchemaMap().size(), 1);
        for (String each : protocolType.getSystemDatabaseSchemaMap().keySet()) {
            if (!databaseConfigMap.containsKey(each) || databaseConfigMap.get(each).getDataSources().isEmpty()) {
                result.put(each.toLowerCase(), ShardingSphereDatabase.create(each, protocolType));
            }
        }
        return result;
    }
}
