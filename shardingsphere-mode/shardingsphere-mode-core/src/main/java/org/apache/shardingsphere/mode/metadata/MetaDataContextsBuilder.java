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
import org.apache.shardingsphere.infra.config.database.impl.DataSourceProvidedDatabaseConfiguration;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeEngine;
import org.apache.shardingsphere.infra.executor.kernel.ExecutorEngine;
import org.apache.shardingsphere.infra.federation.optimizer.context.OptimizerContextFactory;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.loader.DatabaseLoader;
import org.apache.shardingsphere.infra.metadata.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.builder.global.GlobalRulesBuilder;
import org.apache.shardingsphere.infra.rule.builder.schema.SchemaRulesBuilder;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;

import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
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
        Map<String, ShardingSphereMetaData> databaseMetaDataMap = getDatabaseMetaDataMap(frontendDatabaseType, backendDatabaseType);
        ShardingSphereRuleMetaData globalMetaData = new ShardingSphereRuleMetaData(globalRuleConfigs, GlobalRulesBuilder.buildRules(globalRuleConfigs, databaseMetaDataMap));
        ExecutorEngine executorEngine = ExecutorEngine.createExecutorEngineWithSize(props.<Integer>getValue(ConfigurationPropertyKey.KERNEL_EXECUTOR_SIZE));
        return new MetaDataContexts(metaDataPersistService, databaseMetaDataMap, globalMetaData, executorEngine, OptimizerContextFactory.create(databaseMetaDataMap, globalMetaData), props);
    }
    
    private Map<String, ShardingSphereMetaData> getDatabaseMetaDataMap(final DatabaseType frontendDatabaseType, final DatabaseType backendDatabaseType) throws SQLException {
        Map<String, ShardingSphereMetaData> result = new HashMap<>(databaseConfigMap.size() + frontendDatabaseType.getSystemDatabaseSchemaMap().size(), 1);
        result.putAll(getGenericDatabaseMetaDataMap(frontendDatabaseType, backendDatabaseType));
        result.putAll(getSystemDatabaseMetaDataMap(frontendDatabaseType));
        return result;
    }
    
    private Map<String, ShardingSphereMetaData> getGenericDatabaseMetaDataMap(final DatabaseType frontendDatabaseType, final DatabaseType backendDatabaseType) throws SQLException {
        Map<String, ShardingSphereMetaData> result = new HashMap<>(databaseConfigMap.size(), 1);
        for (Entry<String, DatabaseConfiguration> entry : databaseConfigMap.entrySet()) {
            String databaseName = entry.getKey();
            if (!frontendDatabaseType.getSystemSchemas().contains(databaseName)) {
                result.put(databaseName, createMetaData(databaseName, frontendDatabaseType, backendDatabaseType, entry.getValue()));
            }
        }
        return result;
    }
    
    private ShardingSphereMetaData createMetaData(final String databaseName,
                                                  final DatabaseType frontendDatabaseType, final DatabaseType backendDatabaseType, final DatabaseConfiguration databaseConfig) throws SQLException {
        Collection<ShardingSphereRule> databaseRules = SchemaRulesBuilder.buildRules(databaseName, databaseConfig, props);
        ShardingSphereDatabase database = DatabaseLoader.load(databaseName, frontendDatabaseType, backendDatabaseType, databaseConfig.getDataSources(), databaseRules, props);
        return ShardingSphereMetaData.create(frontendDatabaseType, database, databaseConfig, databaseRules);
    }
    
    private Map<String, ShardingSphereMetaData> getSystemDatabaseMetaDataMap(final DatabaseType frontendDatabaseType) throws SQLException {
        Map<String, ShardingSphereMetaData> result = new HashMap<>(frontendDatabaseType.getSystemDatabaseSchemaMap().size(), 1);
        for (String each : frontendDatabaseType.getSystemDatabaseSchemaMap().keySet()) {
            result.put(each, createSystemMetaData(frontendDatabaseType, each));
        }
        return result;
    }
    
    private ShardingSphereMetaData createSystemMetaData(final DatabaseType frontendDatabaseType, final String each) throws SQLException {
        ShardingSphereDatabase database = DatabaseLoader.load(each, frontendDatabaseType);
        DatabaseConfiguration databaseConfig = new DataSourceProvidedDatabaseConfiguration(new LinkedHashMap<>(), new LinkedList<>());
        return ShardingSphereMetaData.create(frontendDatabaseType, database, databaseConfig, new LinkedList<>());
    }
}
