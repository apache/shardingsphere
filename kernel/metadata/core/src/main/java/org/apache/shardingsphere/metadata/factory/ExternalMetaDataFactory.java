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

package org.apache.shardingsphere.metadata.factory;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.config.database.DatabaseConfiguration;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.infra.database.DatabaseTypeEngine;
import org.apache.shardingsphere.infra.database.core.type.checker.DatabaseTypeChecker;
import org.apache.shardingsphere.infra.datasource.state.DataSourceStateManager;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * External meta data factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ExternalMetaDataFactory {
    
    /**
     * Create database meta data for db.
     *
     * @param databaseName database name
     * @param databaseConfig database configuration
     * @param props configuration properties
     * @param instanceContext instance context
     * @return database meta data
     * @throws SQLException SQL exception
     */
    public static ShardingSphereDatabase create(final String databaseName, final DatabaseConfiguration databaseConfig,
                                                final ConfigurationProperties props, final InstanceContext instanceContext) throws SQLException {
        return ShardingSphereDatabase.create(databaseName, DatabaseTypeEngine.getProtocolType(databaseName, databaseConfig, props),
                DatabaseTypeEngine.getStorageTypes(databaseName, databaseConfig), databaseConfig, props, instanceContext);
    }
    
    /**
     * Create databases meta data for db.
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
        Map<String, ShardingSphereDatabase> result = new ConcurrentHashMap<>(databaseConfigMap.size() + protocolType.getSystemDatabaseSchemaMap().size(), 1F);
        result.putAll(createGenericDatabases(databaseConfigMap, protocolType, props, instanceContext));
        result.putAll(createSystemDatabases(databaseConfigMap, protocolType, props));
        return result;
    }
    
    private static Map<String, ShardingSphereDatabase> createGenericDatabases(final Map<String, DatabaseConfiguration> databaseConfigMap, final DatabaseType protocolType,
                                                                              final ConfigurationProperties props, final InstanceContext instanceContext) throws SQLException {
        Map<String, ShardingSphereDatabase> result = new HashMap<>(databaseConfigMap.size(), 1F);
        for (Entry<String, DatabaseConfiguration> entry : databaseConfigMap.entrySet()) {
            String databaseName = entry.getKey();
            if (!entry.getValue().getDataSources().isEmpty() || !protocolType.getSystemSchemas().contains(databaseName)) {
                Map<String, DatabaseType> storageTypes = DatabaseTypeEngine.getStorageTypes(entry.getKey(), entry.getValue());
                DatabaseTypeChecker.checkSupportedStorageTypes(DataSourceStateManager.getInstance()
                        .getEnabledDataSourceMap(databaseName, entry.getValue().getDataSources()), databaseName, storageTypes);
                result.put(databaseName.toLowerCase(), ShardingSphereDatabase.create(databaseName, protocolType, storageTypes, entry.getValue(), props, instanceContext));
            }
        }
        return result;
    }
    
    private static Map<String, ShardingSphereDatabase> createSystemDatabases(final Map<String, DatabaseConfiguration> databaseConfigMap, final DatabaseType protocolType,
                                                                             final ConfigurationProperties props) {
        Map<String, ShardingSphereDatabase> result = new HashMap<>(protocolType.getSystemDatabaseSchemaMap().size(), 1F);
        for (String each : protocolType.getSystemDatabaseSchemaMap().keySet()) {
            if (!databaseConfigMap.containsKey(each) || databaseConfigMap.get(each).getDataSources().isEmpty()) {
                result.put(each.toLowerCase(), ShardingSphereDatabase.create(each, protocolType, props));
            }
        }
        return result;
    }
}
