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
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
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
        DatabaseType protocolType = DatabaseTypeEngine.getProtocolType(databaseConfigMap, props);
        DatabaseType storageType = DatabaseTypeEngine.getStorageType(databaseConfigMap);
        Map<String, ShardingSphereDatabase> databases = getDatabases(protocolType, storageType);
        ShardingSphereRuleMetaData globalMetaData = new ShardingSphereRuleMetaData(GlobalRulesBuilder.buildRules(globalRuleConfigs, databases));
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(databases, globalMetaData, props);
        return new MetaDataContexts(metaDataPersistService, metaData, OptimizerContextFactory.create(databases, globalMetaData));
    }
    
    private Map<String, ShardingSphereDatabase> getDatabases(final DatabaseType protocolType, final DatabaseType storageType) throws SQLException {
        Map<String, ShardingSphereDatabase> result = new HashMap<>(databaseConfigMap.size() + protocolType.getSystemDatabaseSchemaMap().size(), 1);
        result.putAll(getGenericDatabases(protocolType, storageType));
        result.putAll(getSystemDatabases(protocolType));
        return result;
    }
    
    private Map<String, ShardingSphereDatabase> getGenericDatabases(final DatabaseType protocolType, final DatabaseType storageType) throws SQLException {
        Map<String, ShardingSphereDatabase> result = new HashMap<>(databaseConfigMap.size(), 1);
        for (Entry<String, DatabaseConfiguration> entry : databaseConfigMap.entrySet()) {
            String databaseName = entry.getKey();
            if (!entry.getValue().getDataSources().isEmpty() || !protocolType.getSystemSchemas().contains(databaseName)) {
                result.put(databaseName, ShardingSphereDatabase.create(databaseName, protocolType, storageType, entry.getValue(), props));
            }
        }
        return result;
    }
    
    private Map<String, ShardingSphereDatabase> getSystemDatabases(final DatabaseType protocolType) throws SQLException {
        Map<String, ShardingSphereDatabase> result = new HashMap<>(protocolType.getSystemDatabaseSchemaMap().size(), 1);
        for (String each : protocolType.getSystemDatabaseSchemaMap().keySet()) {
            if (!databaseConfigMap.containsKey(each) || databaseConfigMap.get(each).getDataSources().isEmpty()) {
                result.put(each, ShardingSphereDatabase.create(each, protocolType));
            }
        }
        return result;
    }
}
