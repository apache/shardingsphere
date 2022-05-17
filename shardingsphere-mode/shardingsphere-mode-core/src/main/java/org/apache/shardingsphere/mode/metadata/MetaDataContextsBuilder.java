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

import lombok.Getter;
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
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
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
public final class MetaDataContextsBuilder {
    
    private final Map<String, DatabaseConfiguration> databaseConfigMap = new LinkedHashMap<>();
    
    private final Map<String, Collection<ShardingSphereRule>> databaseRulesMap = new LinkedHashMap<>();
    
    @Getter
    private final Map<String, ShardingSphereDatabase> databaseMap = new LinkedHashMap<>();
    
    private final Collection<RuleConfiguration> globalRuleConfigs;
    
    private final ConfigurationProperties props;
    
    public MetaDataContextsBuilder(final Map<String, ? extends DatabaseConfiguration> databaseConfigMap,
                                   final Collection<RuleConfiguration> globalRuleConfigs, final ConfigurationProperties props) throws SQLException {
        this.globalRuleConfigs = globalRuleConfigs;
        this.props = props;
        DatabaseType frontendDatabaseType = DatabaseTypeEngine.getFrontendDatabaseType(databaseConfigMap, props);
        DatabaseType backendDatabaseType = DatabaseTypeEngine.getBackendDatabaseType(databaseConfigMap);
        for (Entry<String, ? extends DatabaseConfiguration> entry : databaseConfigMap.entrySet()) {
            if (!frontendDatabaseType.getSystemSchemas().contains(entry.getKey())) {
                addDatabase(entry.getKey(), frontendDatabaseType, backendDatabaseType, entry.getValue());
            }
        }
        addSystemDatabases(frontendDatabaseType);
    }
    
    private void addDatabase(final String databaseName, final DatabaseType frontendDatabaseType, final DatabaseType backendDatabaseType,
                             final DatabaseConfiguration databaseConfig) throws SQLException {
        Collection<ShardingSphereRule> databaseRules = SchemaRulesBuilder.buildRules(databaseName, databaseConfig, props);
        ShardingSphereDatabase database = DatabaseLoader.load(databaseName, frontendDatabaseType, backendDatabaseType, databaseConfig.getDataSources(), databaseRules, props);
        databaseConfigMap.put(databaseName, databaseConfig);
        databaseRulesMap.put(databaseName, databaseRules);
        databaseMap.put(databaseName, database);
    }
    
    private void addSystemDatabases(final DatabaseType frontendDatabaseType) {
        for (String each : frontendDatabaseType.getSystemDatabaseSchemaMap().keySet()) {
            if (!databaseMap.containsKey(each)) {
                databaseMap.put(each, DatabaseLoader.load(each, frontendDatabaseType));
            }
        }
    }
    
    /**
     * Build meta data contexts.
     * 
     * @param metaDataPersistService persist service
     * @exception SQLException SQL exception
     * @return meta data contexts
     */
    public MetaDataContexts build(final MetaDataPersistService metaDataPersistService) throws SQLException {
        Map<String, ShardingSphereMetaData> metaDataMap = getMetaDataMap();
        ShardingSphereRuleMetaData globalMetaData = new ShardingSphereRuleMetaData(globalRuleConfigs, GlobalRulesBuilder.buildRules(globalRuleConfigs, metaDataMap));
        ExecutorEngine executorEngine = ExecutorEngine.createExecutorEngineWithSize(props.<Integer>getValue(ConfigurationPropertyKey.KERNEL_EXECUTOR_SIZE));
        return new MetaDataContexts(metaDataPersistService, metaDataMap, globalMetaData, executorEngine, OptimizerContextFactory.create(metaDataMap, globalMetaData), props);
    }
    
    private Map<String, ShardingSphereMetaData> getMetaDataMap() throws SQLException {
        Map<String, ShardingSphereMetaData> result = new HashMap<>(databaseMap.size(), 1);
        DatabaseType frontendDatabaseType = DatabaseTypeEngine.getFrontendDatabaseType(databaseConfigMap, props);
        for (Entry<String, ShardingSphereDatabase> entry : databaseMap.entrySet()) {
            String databaseName = entry.getKey();
            DatabaseConfiguration databaseConfig = databaseConfigMap.getOrDefault(databaseName, new DataSourceProvidedDatabaseConfiguration(new LinkedHashMap<>(), new LinkedList<>()));
            Collection<ShardingSphereRule> rules = databaseRulesMap.getOrDefault(databaseName, new LinkedList<>());
            result.put(databaseName, ShardingSphereMetaData.create(databaseName, frontendDatabaseType, entry.getValue().getSchemas(), databaseConfig, rules));
        }
        return result;
    }
}
