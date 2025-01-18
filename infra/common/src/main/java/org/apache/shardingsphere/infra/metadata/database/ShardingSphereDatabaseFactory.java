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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.config.database.DatabaseConfiguration;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.DatabaseTypeEngine;
import org.apache.shardingsphere.infra.database.core.metadata.database.system.SystemDatabase;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstanceContext;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;

import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * ShardingSphere database factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShardingSphereDatabaseFactory {
    
    /**
     * Create database.
     *
     * @param databaseName database name
     * @param databaseConfig database configuration
     * @param props configuration properties
     * @param computeNodeInstanceContext compute node instance context
     * @return created database
     * @throws SQLException SQL exception
     */
    public static ShardingSphereDatabase create(final String databaseName, final DatabaseConfiguration databaseConfig,
                                                final ConfigurationProperties props, final ComputeNodeInstanceContext computeNodeInstanceContext) throws SQLException {
        DatabaseType protocolType = DatabaseTypeEngine.getProtocolType(databaseConfig, props);
        return ShardingSphereDatabase.create(databaseName, protocolType, databaseConfig, props, computeNodeInstanceContext);
    }
    
    /**
     * Create database.
     *
     * @param databaseName database name
     * @param databaseConfig database configuration
     * @param schemas schemas
     * @param props configuration properties
     * @param computeNodeInstanceContext compute node instance context
     * @return created database
     */
    public static ShardingSphereDatabase create(final String databaseName, final DatabaseConfiguration databaseConfig, final Collection<ShardingSphereSchema> schemas,
                                                final ConfigurationProperties props, final ComputeNodeInstanceContext computeNodeInstanceContext) {
        DatabaseType protocolType = DatabaseTypeEngine.getProtocolType(databaseConfig, props);
        return ShardingSphereDatabase.create(databaseName, protocolType, databaseConfig, computeNodeInstanceContext, schemas);
    }
    
    /**
     * Create databases.
     *
     * @param databaseConfigMap database configuration map
     * @param schemas schemas
     * @param props properties
     * @param computeNodeInstanceContext compute node instance context
     * @return created databases
     */
    public static Map<String, ShardingSphereDatabase> create(final Map<String, DatabaseConfiguration> databaseConfigMap,
                                                             final Map<String, Collection<ShardingSphereSchema>> schemas,
                                                             final ConfigurationProperties props, final ComputeNodeInstanceContext computeNodeInstanceContext) {
        DatabaseType protocolType = DatabaseTypeEngine.getProtocolType(databaseConfigMap, props);
        return databaseConfigMap.entrySet().stream()
                .collect(Collectors.toMap(entry -> entry.getKey().toLowerCase(),
                        entry -> create(entry.getKey(), entry.getValue(), protocolType, schemas.get(entry.getKey()), props, computeNodeInstanceContext),
                        (a, b) -> b, () -> new ConcurrentHashMap<>(databaseConfigMap.size(), 1F)));
    }
    
    private static ShardingSphereDatabase create(final String databaseName, final DatabaseConfiguration databaseConfig, final DatabaseType protocolType,
                                                 final Collection<ShardingSphereSchema> schemas, final ConfigurationProperties props,
                                                 final ComputeNodeInstanceContext computeNodeInstanceContext) {
        return databaseConfig.getStorageUnits().isEmpty()
                ? ShardingSphereDatabase.create(databaseName, protocolType, props)
                : create(databaseName, databaseConfig, schemas, props, computeNodeInstanceContext);
    }
    
    /**
     * Create databases.
     *
     * @param databaseConfigMap database configuration map
     * @param props properties
     * @param computeNodeInstanceContext compute node instance context
     * @return created databases
     * @throws SQLException SQL exception
     */
    public static Map<String, ShardingSphereDatabase> create(final Map<String, DatabaseConfiguration> databaseConfigMap,
                                                             final ConfigurationProperties props, final ComputeNodeInstanceContext computeNodeInstanceContext) throws SQLException {
        DatabaseType protocolType = DatabaseTypeEngine.getProtocolType(databaseConfigMap, props);
        SystemDatabase systemDatabase = new SystemDatabase(protocolType);
        Map<String, ShardingSphereDatabase> result = new ConcurrentHashMap<>(databaseConfigMap.size() + systemDatabase.getSystemDatabaseSchemaMap().size(), 1F);
        result.putAll(createGenericDatabases(databaseConfigMap, protocolType, systemDatabase, props, computeNodeInstanceContext));
        result.putAll(createSystemDatabases(databaseConfigMap, protocolType, systemDatabase, props));
        return result;
    }
    
    private static Map<String, ShardingSphereDatabase> createGenericDatabases(final Map<String, DatabaseConfiguration> databaseConfigMap,
                                                                              final DatabaseType protocolType, final SystemDatabase systemDatabase,
                                                                              final ConfigurationProperties props, final ComputeNodeInstanceContext computeNodeInstanceContext) throws SQLException {
        Map<String, ShardingSphereDatabase> result = new HashMap<>(databaseConfigMap.size(), 1F);
        for (Entry<String, DatabaseConfiguration> entry : databaseConfigMap.entrySet()) {
            String databaseName = entry.getKey();
            if (!entry.getValue().getStorageUnits().isEmpty() || !systemDatabase.getSystemSchemas().contains(databaseName)) {
                result.put(databaseName.toLowerCase(), ShardingSphereDatabase.create(databaseName, protocolType, entry.getValue(), props, computeNodeInstanceContext));
            }
        }
        return result;
    }
    
    private static Map<String, ShardingSphereDatabase> createSystemDatabases(final Map<String, DatabaseConfiguration> databaseConfigMap, final DatabaseType protocolType,
                                                                             final SystemDatabase systemDatabase, final ConfigurationProperties props) {
        Map<String, ShardingSphereDatabase> result = new HashMap<>(systemDatabase.getSystemDatabaseSchemaMap().size(), 1F);
        for (String each : systemDatabase.getSystemDatabaseSchemaMap().keySet()) {
            if (!databaseConfigMap.containsKey(each) || databaseConfigMap.get(each).getStorageUnits().isEmpty()) {
                result.put(each.toLowerCase(), ShardingSphereDatabase.create(each, protocolType, props));
            }
        }
        return result;
    }
}
