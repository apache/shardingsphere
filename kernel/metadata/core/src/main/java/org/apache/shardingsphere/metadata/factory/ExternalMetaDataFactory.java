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

import org.apache.shardingsphere.infra.config.database.DatabaseConfiguration;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeEngine;
import org.apache.shardingsphere.infra.database.type.dialect.SQL92DatabaseType;
import org.apache.shardingsphere.infra.exception.UnsupportedStorageTypeException;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * External meta data factory.
 */
public final class ExternalMetaDataFactory {
    
    private static final Collection<String> MOCKED_URL = Arrays.asList("jdbc:fixture", "jdbc:mock");
    
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
        Map<String, ShardingSphereDatabase> result = new ConcurrentHashMap<>(databaseConfigMap.size() + protocolType.getSystemDatabaseSchemaMap().size(), 1);
        result.putAll(createGenericDatabases(databaseConfigMap, protocolType, props, instanceContext));
        result.putAll(createSystemDatabases(databaseConfigMap, protocolType));
        return result;
    }
    
    private static Map<String, ShardingSphereDatabase> createGenericDatabases(final Map<String, DatabaseConfiguration> databaseConfigMap, final DatabaseType protocolType,
                                                                              final ConfigurationProperties props, final InstanceContext instanceContext) throws SQLException {
        Map<String, ShardingSphereDatabase> result = new HashMap<>(databaseConfigMap.size(), 1);
        for (Entry<String, DatabaseConfiguration> entry : databaseConfigMap.entrySet()) {
            String databaseName = entry.getKey();
            if (!entry.getValue().getDataSources().isEmpty() || !protocolType.getSystemSchemas().contains(databaseName)) {
                Map<String, DatabaseType> storageTypes = DatabaseTypeEngine.getStorageTypes(entry.getKey(), entry.getValue());
                checkSupportedStorageTypes(entry.getValue().getDataSources(), databaseName, storageTypes);
                result.put(databaseName.toLowerCase(), ShardingSphereDatabase.create(databaseName, protocolType, storageTypes, entry.getValue(), props, instanceContext));
            }
        }
        return result;
    }
    
    private static void checkSupportedStorageTypes(final Map<String, DataSource> dataSources, final String databaseName, final Map<String, DatabaseType> storageTypes) throws SQLException {
        if (dataSources.isEmpty()) {
            return;
        }
        try (Connection connection = dataSources.values().iterator().next().getConnection()) {
            String url = connection.getMetaData().getURL();
            if (MOCKED_URL.stream().anyMatch(url::startsWith)) {
                return;
            }
        }
        storageTypes.forEach((key, value) -> ShardingSpherePreconditions.checkState(!SQL92DatabaseType.class.equals(value.getClass()), () -> new UnsupportedStorageTypeException(databaseName, key)));
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
