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
import org.apache.shardingsphere.infra.database.type.DatabaseTypeRecognizer;
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
import java.util.Properties;

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
    
    private final ExecutorEngine executorEngine;
    
    public MetaDataContextsBuilder(final Collection<RuleConfiguration> globalRuleConfigs, final Properties props) {
        this.globalRuleConfigs = globalRuleConfigs;
        this.props = new ConfigurationProperties(props);
        executorEngine = ExecutorEngine.createExecutorEngineWithSize(this.props.<Integer>getValue(ConfigurationPropertyKey.KERNEL_EXECUTOR_SIZE));
    }
    
    /**
     * Add database information.
     * 
     * @param databaseName schema name
     * @param databaseType database type
     * @param databaseConfig database configuration
     * @param props properties
     * @throws SQLException SQL exception
     */
    public void addDatabase(final String databaseName, final DatabaseType databaseType, final DatabaseConfiguration databaseConfig, final Properties props) throws SQLException {
        Collection<ShardingSphereRule> databaseRules = getDatabaseRules(databaseName, databaseConfig, props);
        ShardingSphereDatabase database = DatabaseLoader.load(databaseName, databaseType, databaseConfig.getDataSources(), databaseRules, props);
        databaseConfigMap.put(databaseName, databaseConfig);
        databaseRulesMap.put(databaseName, databaseRules);
        databaseMap.put(databaseName, database);
    }
    
    /**
     * Add system schemas.
     *
     * @param databaseType database type
     */
    public void addSystemDatabases(final DatabaseType databaseType) {
        for (String each : databaseType.getSystemDatabaseSchemaMap().keySet()) {
            if (databaseMap.containsKey(each)) {
                continue;
            }
            databaseMap.put(each, DatabaseLoader.load(each, databaseType));
        }
    }
    
    private Collection<ShardingSphereRule> getDatabaseRules(final String databaseName, final DatabaseConfiguration databaseConfig, final Properties props) {
        return SchemaRulesBuilder.buildRules(databaseName, databaseConfig, new ConfigurationProperties(props));
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
        return new MetaDataContexts(metaDataPersistService, metaDataMap, globalMetaData, executorEngine, OptimizerContextFactory.create(metaDataMap, globalMetaData), props);
    }
    
    private Map<String, ShardingSphereMetaData> getMetaDataMap() throws SQLException {
        DatabaseType defaultDatabaseType = DatabaseTypeRecognizer.getDatabaseType(databaseConfigMap, props);
        Map<String, ShardingSphereMetaData> result = new HashMap<>(databaseMap.size(), 1);
        for (Entry<String, ShardingSphereDatabase> entry : databaseMap.entrySet()) {
            String databaseName = entry.getKey();
            // TODO support database and schema configuration separately
            DatabaseConfiguration databaseConfig = databaseConfigMap.getOrDefault(databaseName, new DataSourceProvidedDatabaseConfiguration(new LinkedHashMap<>(), new LinkedList<>()));
            Collection<ShardingSphereRule> rules = databaseRulesMap.getOrDefault(databaseName, new LinkedList<>());
            result.put(databaseName, ShardingSphereMetaData.create(databaseName, entry.getValue().getSchemas(), databaseConfig, rules, defaultDatabaseType));
        }
        return result;
    }
    
    /**
     * Get schema map.
     * 
     * @param databaseName database name
     * @return ShardingSphere schema
     */
    public ShardingSphereSchema getSchemaMap(final String databaseName) {
        // TODO remove these logic when mode support persist ShardingSphereDatabase
        return databaseMap.get(databaseName).getSchemas().get(databaseName);
    }
}
