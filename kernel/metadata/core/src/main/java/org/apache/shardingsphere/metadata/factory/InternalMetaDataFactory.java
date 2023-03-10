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
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rule.builder.database.DatabaseRulesBuilder;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Internal meta data factory.
 */
public final class InternalMetaDataFactory {
    
    /**
     * Create database meta data for governance center.
     *
     * @param databaseName database name
     * @param databaseConfig database configuration
     * @param props configuration properties
     * @param instanceContext instance context
     * @return database meta data
     */
    public static ShardingSphereDatabase create(final String databaseName, final DatabaseConfiguration databaseConfig,
                                                final ConfigurationProperties props, final InstanceContext instanceContext) {
        return ShardingSphereDatabase.create(databaseName, DatabaseTypeEngine.getProtocolType(databaseName, databaseConfig, props), databaseConfig,
                DatabaseRulesBuilder.build(databaseName, databaseConfig, instanceContext), instanceContext.getModeContextManager().getSchemas(databaseName));
    }
    
    /**
     * Create databases meta data for governance center.
     *
     * @param databaseConfigMap database configuration map
     * @param props properties
     * @param instanceContext instance context
     * @return databases
     */
    public static Map<String, ShardingSphereDatabase> create(final Map<String, DatabaseConfiguration> databaseConfigMap,
                                                             final ConfigurationProperties props, final InstanceContext instanceContext) {
        return createDatabases(databaseConfigMap, DatabaseTypeEngine.getProtocolType(databaseConfigMap, props), props, instanceContext);
    }
    
    private static Map<String, ShardingSphereDatabase> createDatabases(final Map<String, DatabaseConfiguration> databaseConfigMap, final DatabaseType protocolType,
                                                                       final ConfigurationProperties props, final InstanceContext instanceContext) {
        Map<String, ShardingSphereDatabase> result = new ConcurrentHashMap<>(databaseConfigMap.size(), 1);
        for (Entry<String, DatabaseConfiguration> entry : databaseConfigMap.entrySet()) {
            String databaseName = entry.getKey();
            if (!entry.getValue().getDataSources().isEmpty()) {
                result.put(databaseName.toLowerCase(), create(databaseName, entry.getValue(), props, instanceContext));
            } else {
                result.put(databaseName.toLowerCase(), ShardingSphereDatabase.create(databaseName, protocolType));
            }
        }
        return result;
    }
}
