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
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rule.builder.database.DatabaseRulesBuilder;
import org.apache.shardingsphere.metadata.persist.NewMetaDataPersistService;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * TODO replace the old implementation after meta data refactor completed
 * New internal meta data factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class NewInternalMetaDataFactory {
    
    /**
     * Create database meta data for governance center.
     *
     * @param databaseName database name
     * @param persistService meta data persist service
     * @param databaseConfig database configuration
     * @param props configuration properties
     * @param instanceContext instance context
     * @return database meta data
     */
    public static ShardingSphereDatabase create(final String databaseName, final NewMetaDataPersistService persistService, final DatabaseConfiguration databaseConfig,
                                                final ConfigurationProperties props, final InstanceContext instanceContext) {
        return ShardingSphereDatabase.create(databaseName, DatabaseTypeEngine.getProtocolType(databaseName, databaseConfig, props), databaseConfig,
                DatabaseRulesBuilder.build(databaseName, databaseConfig, instanceContext), persistService.getDatabaseMetaDataService().loadSchemas(databaseName));
    }
    
    /**
     * Create databases meta data for governance center.
     *
     * @param persistService meta data persist service
     * @param databaseConfigMap database configuration map
     * @param props properties
     * @param instanceContext instance context
     * @return databases
     */
    public static Map<String, ShardingSphereDatabase> create(final NewMetaDataPersistService persistService, final Map<String, DatabaseConfiguration> databaseConfigMap,
                                                             final ConfigurationProperties props, final InstanceContext instanceContext) {
        return createDatabases(persistService, databaseConfigMap, DatabaseTypeEngine.getProtocolType(databaseConfigMap, props), props, instanceContext);
    }
    
    private static Map<String, ShardingSphereDatabase> createDatabases(final NewMetaDataPersistService persistService, final Map<String, DatabaseConfiguration> databaseConfigMap,
                                                                       final DatabaseType protocolType, final ConfigurationProperties props, final InstanceContext instanceContext) {
        Map<String, ShardingSphereDatabase> result = new ConcurrentHashMap<>(databaseConfigMap.size(), 1F);
        for (Entry<String, DatabaseConfiguration> entry : databaseConfigMap.entrySet()) {
            String databaseName = entry.getKey();
            if (entry.getValue().getDataSources().isEmpty()) {
                result.put(databaseName.toLowerCase(), ShardingSphereDatabase.create(databaseName, protocolType, props));
            } else {
                result.put(databaseName.toLowerCase(), create(databaseName, persistService, entry.getValue(), props, instanceContext));
            }
        }
        return result;
    }
}
