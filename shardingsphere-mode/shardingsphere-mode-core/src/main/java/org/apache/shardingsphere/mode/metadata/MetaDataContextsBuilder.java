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

package org.apache.shardingsphere.mode.metadata;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.database.DatabaseConfiguration;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeEngine;
import org.apache.shardingsphere.infra.federation.optimizer.context.OptimizerContextFactory;
import org.apache.shardingsphere.infra.metadata.ShardingSphereDatabaseMetaData;
import org.apache.shardingsphere.infra.metadata.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.rule.builder.global.GlobalRulesBuilder;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;

import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Meta data contexts builder.
 */
@RequiredArgsConstructor
public final class MetaDataContextsBuilder {
    
    private final Map<String, DatabaseConfiguration> databaseConfigMap;
    
    private final Collection<RuleConfiguration> globalRuleConfigs;
    
    private final ConfigurationProperties props;
    
    /**
     * Build meta data contexts.
     * 
     * @param metaDataPersistService persist service
     * @exception SQLException SQL exception
     * @return meta data contexts
     */
    public MetaDataContexts build(final MetaDataPersistService metaDataPersistService) throws SQLException {
        DatabaseType frontendDatabaseType = DatabaseTypeEngine.getFrontendDatabaseType(databaseConfigMap, props);
        DatabaseType backendDatabaseType = DatabaseTypeEngine.getBackendDatabaseType(databaseConfigMap);
        Map<String, ShardingSphereDatabaseMetaData> databaseMetaDataMap = getDatabaseMetaDataMap(frontendDatabaseType, backendDatabaseType);
        ShardingSphereRuleMetaData globalMetaData = new ShardingSphereRuleMetaData(globalRuleConfigs, GlobalRulesBuilder.buildRules(globalRuleConfigs, databaseMetaDataMap));
        return new MetaDataContexts(metaDataPersistService, databaseMetaDataMap, globalMetaData, OptimizerContextFactory.create(databaseMetaDataMap, globalMetaData), props);
    }
    
    private Map<String, ShardingSphereDatabaseMetaData> getDatabaseMetaDataMap(final DatabaseType frontendDatabaseType, final DatabaseType backendDatabaseType) throws SQLException {
        Map<String, ShardingSphereDatabaseMetaData> result = new HashMap<>(databaseConfigMap.size() + frontendDatabaseType.getSystemDatabaseSchemaMap().size(), 1);
        result.putAll(getGenericDatabaseMetaDataMap(frontendDatabaseType, backendDatabaseType));
        result.putAll(getSystemDatabaseMetaDataMap(frontendDatabaseType));
        return result;
    }
    
    private Map<String, ShardingSphereDatabaseMetaData> getGenericDatabaseMetaDataMap(final DatabaseType frontendDatabaseType, final DatabaseType backendDatabaseType) throws SQLException {
        Map<String, ShardingSphereDatabaseMetaData> result = new HashMap<>(databaseConfigMap.size(), 1);
        for (Entry<String, DatabaseConfiguration> entry : databaseConfigMap.entrySet()) {
            String databaseName = entry.getKey();
            if (!frontendDatabaseType.getSystemSchemas().contains(databaseName)) {
                result.put(databaseName, ShardingSphereDatabaseMetaData.create(databaseName, frontendDatabaseType, backendDatabaseType, entry.getValue(), props));
            }
        }
        return result;
    }
    
    private Map<String, ShardingSphereDatabaseMetaData> getSystemDatabaseMetaDataMap(final DatabaseType frontendDatabaseType) throws SQLException {
        Map<String, ShardingSphereDatabaseMetaData> result = new HashMap<>(frontendDatabaseType.getSystemDatabaseSchemaMap().size(), 1);
        for (String each : frontendDatabaseType.getSystemDatabaseSchemaMap().keySet()) {
            result.put(each, ShardingSphereDatabaseMetaData.create(each, frontendDatabaseType));
        }
        return result;
    }
}
