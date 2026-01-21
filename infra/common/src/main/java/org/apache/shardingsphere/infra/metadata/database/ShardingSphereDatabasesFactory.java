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
import org.apache.shardingsphere.database.connector.core.metadata.database.system.SystemDatabase;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.config.database.DatabaseConfiguration;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstanceContext;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;

import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * ShardingSphere databases factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShardingSphereDatabasesFactory {
    
    /**
     * Create databases.
     *
     * @param databaseConfigMap database configuration map
     * @param schemas schemas
     * @param props properties
     * @param instanceContext compute node instance context
     * @param protocolType protocol type
     * @return created databases
     */
    public static Collection<ShardingSphereDatabase> create(final Map<String, DatabaseConfiguration> databaseConfigMap,
                                                            final Map<String, Collection<ShardingSphereSchema>> schemas,
                                                            final ConfigurationProperties props, final ComputeNodeInstanceContext instanceContext,
                                                            final DatabaseType protocolType) {
        return databaseConfigMap.entrySet().stream()
                .map(entry -> create(entry.getKey(), entry.getValue(), protocolType, schemas.get(entry.getKey()), props, instanceContext)).collect(Collectors.toList());
    }
    
    private static ShardingSphereDatabase create(final String databaseName, final DatabaseConfiguration databaseConfig, final DatabaseType protocolType,
                                                 final Collection<ShardingSphereSchema> schemas, final ConfigurationProperties props,
                                                 final ComputeNodeInstanceContext computeNodeInstanceContext) {
        return databaseConfig.getStorageUnits().isEmpty()
                ? ShardingSphereDatabaseFactory.create(databaseName, protocolType, props)
                : ShardingSphereDatabaseFactory.create(databaseName, protocolType, databaseConfig, computeNodeInstanceContext, schemas);
    }
    
    /**
     * Create databases.
     *
     * @param databaseConfigMap database configuration map
     * @param props properties
     * @param instanceContext compute node instance context
     * @param protocolType protocol type
     * @return created databases
     * @throws SQLException SQL exception
     */
    public static Collection<ShardingSphereDatabase> create(final Map<String, DatabaseConfiguration> databaseConfigMap,
                                                            final ConfigurationProperties props, final ComputeNodeInstanceContext instanceContext,
                                                            final DatabaseType protocolType) throws SQLException {
        SystemDatabase systemDatabase = new SystemDatabase(protocolType);
        Collection<ShardingSphereDatabase> result = new LinkedList<>();
        result.addAll(createGenericDatabases(databaseConfigMap, protocolType, systemDatabase, props, instanceContext));
        result.addAll(createSystemDatabases(databaseConfigMap, protocolType, systemDatabase, props));
        return result;
    }
    
    private static Collection<ShardingSphereDatabase> createGenericDatabases(final Map<String, DatabaseConfiguration> databaseConfigMap,
                                                                             final DatabaseType protocolType, final SystemDatabase systemDatabase,
                                                                             final ConfigurationProperties props, final ComputeNodeInstanceContext instanceContext) throws SQLException {
        Collection<ShardingSphereDatabase> result = new HashSet<>(databaseConfigMap.size(), 1F);
        for (Entry<String, DatabaseConfiguration> entry : databaseConfigMap.entrySet()) {
            String databaseName = entry.getKey();
            if (!entry.getValue().getStorageUnits().isEmpty() || !systemDatabase.getSystemSchemas().contains(databaseName)) {
                result.add(ShardingSphereDatabaseFactory.create(databaseName, protocolType, entry.getValue(), props, instanceContext));
            }
        }
        return result;
    }
    
    private static Collection<ShardingSphereDatabase> createSystemDatabases(final Map<String, DatabaseConfiguration> databaseConfigMap, final DatabaseType protocolType,
                                                                            final SystemDatabase systemDatabase, final ConfigurationProperties props) {
        Collection<ShardingSphereDatabase> result = new HashSet<>(systemDatabase.getSystemDatabases().size(), 1F);
        for (String each : systemDatabase.getSystemDatabases()) {
            if (!databaseConfigMap.containsKey(each) || databaseConfigMap.get(each).getStorageUnits().isEmpty()) {
                result.add(ShardingSphereDatabaseFactory.create(each, protocolType, props));
            }
        }
        return result;
    }
}
