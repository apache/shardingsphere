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
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.config.schema.SchemaConfiguration;
import org.apache.shardingsphere.infra.executor.kernel.ExecutorEngine;
import org.apache.shardingsphere.infra.federation.optimizer.context.OptimizerContextFactory;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.schema.loader.SchemaLoader;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.builder.global.GlobalRulesBuilder;
import org.apache.shardingsphere.infra.rule.builder.schema.SchemaRulesBuilder;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;

import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

/**
 * Meta data contexts builder.
 */
public final class MetaDataContextsBuilder {
    
    private final Map<String, SchemaConfiguration> schemaConfigMap = new LinkedHashMap<>();
    
    private final Map<String, Collection<ShardingSphereRule>> schemaRulesMap = new LinkedHashMap<>();
    
    @Getter
    private final Map<String, ShardingSphereSchema> schemaMap = new LinkedHashMap<>();
    
    private final Collection<RuleConfiguration> globalRuleConfigs;
    
    private final ConfigurationProperties props;
    
    private final ExecutorEngine executorEngine;
    
    public MetaDataContextsBuilder(final Collection<RuleConfiguration> globalRuleConfigs, final Properties props) {
        this.globalRuleConfigs = globalRuleConfigs;
        this.props = new ConfigurationProperties(props);
        executorEngine = new ExecutorEngine(this.props.<Integer>getValue(ConfigurationPropertyKey.KERNEL_EXECUTOR_SIZE));
    }
    
    /**
     * Add schema information.
     * 
     * @param schemaName schema name
     * @param schemaConfig schema configuration
     * @param props properties
     * @throws SQLException SQL exception
     */
    public void addSchema(final String schemaName, final SchemaConfiguration schemaConfig, final Properties props) throws SQLException {
        Collection<ShardingSphereRule> schemaRules = getSchemaRules(schemaName, schemaConfig, props);
        ShardingSphereSchema schema = SchemaLoader.load(schemaConfig.getDataSources(), schemaRules, props);
        schemaConfigMap.put(schemaName, schemaConfig);
        schemaRulesMap.put(schemaName, schemaRules);
        schemaMap.put(schemaName, schema);
    }
    
    private Collection<ShardingSphereRule> getSchemaRules(final String schemaName, final SchemaConfiguration schemaConfig, final Properties props) {
        return SchemaRulesBuilder.buildRules(schemaName, schemaConfig, new ConfigurationProperties(props));
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
        Map<String, ShardingSphereMetaData> result = new HashMap<>(schemaConfigMap.size(), 1);
        for (Entry<String, ? extends SchemaConfiguration> entry : schemaConfigMap.entrySet()) {
            String schemaName = entry.getKey();
            result.put(schemaName, ShardingSphereMetaData.create(schemaName, schemaMap.get(schemaName), entry.getValue(), schemaRulesMap.get(schemaName)));
        }
        return result;
    }
}
