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

package org.apache.shardingsphere.mode.metadata.factory;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.config.database.DatabaseConfiguration;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.DatabaseTypeEngine;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstanceContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Internal meta data factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class InternalMetaDataFactory {
    
    /**
     * Create database meta data from governance center.
     *
     * @param databaseName database name
     * @param persistService meta data persist service
     * @param databaseConfig database configuration
     * @param props configuration properties
     * @param computeNodeInstanceContext compute node instance context
     * @return database
     */
    public static ShardingSphereDatabase create(final String databaseName, final MetaDataPersistService persistService, final DatabaseConfiguration databaseConfig,
                                                final ConfigurationProperties props, final ComputeNodeInstanceContext computeNodeInstanceContext) {
        DatabaseType protocolType = DatabaseTypeEngine.getProtocolType(databaseConfig, props);
        return ShardingSphereDatabase.create(databaseName,
                protocolType, databaseConfig, computeNodeInstanceContext, persistService.getDatabaseMetaDataFacade().getSchema().load(databaseName));
    }
    
    /**
     * Create databases meta data from governance center.
     *
     * @param persistService meta data persist service
     * @param databaseConfigMap database configuration map
     * @param props properties
     * @param computeNodeInstanceContext compute node instance context
     * @return databases
     */
    public static Map<String, ShardingSphereDatabase> create(final MetaDataPersistService persistService, final Map<String, DatabaseConfiguration> databaseConfigMap,
                                                             final ConfigurationProperties props, final ComputeNodeInstanceContext computeNodeInstanceContext) {
        return createDatabases(persistService, databaseConfigMap, DatabaseTypeEngine.getProtocolType(databaseConfigMap, props), props, computeNodeInstanceContext);
    }
    
    private static Map<String, ShardingSphereDatabase> createDatabases(final MetaDataPersistService persistService, final Map<String, DatabaseConfiguration> databaseConfigMap,
                                                                       final DatabaseType protocolType, final ConfigurationProperties props,
                                                                       final ComputeNodeInstanceContext computeNodeInstanceContext) {
        Map<String, ShardingSphereDatabase> result = new ConcurrentHashMap<>(databaseConfigMap.size(), 1F);
        for (Entry<String, DatabaseConfiguration> entry : databaseConfigMap.entrySet()) {
            String databaseName = entry.getKey();
            result.put(databaseName.toLowerCase(), entry.getValue().getStorageUnits().isEmpty()
                    ? ShardingSphereDatabase.create(databaseName, protocolType, props)
                    : create(databaseName, persistService, entry.getValue(), props, computeNodeInstanceContext));
        }
        return result;
    }
}
