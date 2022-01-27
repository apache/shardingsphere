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

import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.config.schema.SchemaConfiguration;
import org.apache.shardingsphere.infra.executor.kernel.ExecutorEngine;
import org.apache.shardingsphere.infra.federation.optimizer.context.OptimizerContextFactory;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.builder.global.GlobalRulesBuilder;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;

import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

/**
 * Meta data contexts builder.
 */
public final class MetaDataContextsBuilder {
    
    private final Map<String, ? extends SchemaConfiguration> schemaConfigs;
    
    private final Collection<RuleConfiguration> globalRuleConfigs;
    
    private final Map<String, ShardingSphereSchema> schemas;
    
    private final Map<String, Collection<ShardingSphereRule>> rules;
    
    private final ConfigurationProperties props;
    
    private final ExecutorEngine executorEngine;
    
    public MetaDataContextsBuilder(final Map<String, ? extends SchemaConfiguration> schemaConfigs, final Collection<RuleConfiguration> globalRuleConfigs,
                                   final Map<String, ShardingSphereSchema> schemas, final Map<String, Collection<ShardingSphereRule>> rules, final Properties props) {
        this.schemaConfigs = schemaConfigs;
        this.globalRuleConfigs = globalRuleConfigs;
        this.schemas = schemas;
        this.rules = rules;
        this.props = new ConfigurationProperties(null == props ? new Properties() : props);
        executorEngine = new ExecutorEngine(this.props.<Integer>getValue(ConfigurationPropertyKey.KERNEL_EXECUTOR_SIZE));
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
        return new MetaDataContexts(metaDataPersistService, metaDataMap, globalMetaData, executorEngine, props, OptimizerContextFactory.create(metaDataMap, globalMetaData));
    }
    
    private Map<String, ShardingSphereMetaData> getMetaDataMap() throws SQLException {
        Map<String, ShardingSphereMetaData> result = new HashMap<>(schemaConfigs.size(), 1);
        for (Entry<String, ? extends SchemaConfiguration> entry : schemaConfigs.entrySet()) {
            String schemaName = entry.getKey();
            result.put(schemaName, ShardingSphereMetaData.create(schemaName, schemas.get(schemaName), entry.getValue(), rules.get(schemaName)));
        }
        return result;
    }
}
