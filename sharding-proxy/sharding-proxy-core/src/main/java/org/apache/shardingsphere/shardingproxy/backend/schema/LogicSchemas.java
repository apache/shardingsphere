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

package org.apache.shardingsphere.shardingproxy.backend.schema;

import com.google.common.base.Strings;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.api.config.RuleConfiguration;
import org.apache.shardingsphere.api.config.masterslave.MasterSlaveRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.ShardingRuleConfiguration;
import org.apache.shardingsphere.core.constant.properties.ShardingPropertiesConstant;
import org.apache.shardingsphere.orchestration.internal.eventbus.ShardingOrchestrationEventBus;
import org.apache.shardingsphere.orchestration.internal.registry.config.event.SchemaAddedEvent;
import org.apache.shardingsphere.orchestration.internal.registry.config.event.SchemaDeletedEvent;
import org.apache.shardingsphere.shardingproxy.config.yaml.YamlDataSourceParameter;
import org.apache.shardingsphere.shardingproxy.runtime.GlobalRegistry;
import org.apache.shardingsphere.shardingproxy.util.DataSourceConverter;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Logic schemas.
 *
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public final class LogicSchemas {
    
    private static final LogicSchemas INSTANCE = new LogicSchemas();
    
    private final EventBus eventBus = ShardingOrchestrationEventBus.getInstance();
    
    private final Map<String, LogicSchema> logicSchemas = new ConcurrentHashMap<>();
    
    /**
     * Get instance of proxy context.
     *
     * @return instance of proxy context.
     */
    public static LogicSchemas getInstance() {
        return INSTANCE;
    }
    
    /**
     * Register listener.
     */
    public void register() {
        eventBus.register(this);
    }
    
    /**
     * Initialize proxy context.
     *
     * @param schemaDataSources data source map
     * @param schemaRules schema rule map
     */
    public void init(final Map<String, Map<String, YamlDataSourceParameter>> schemaDataSources, final Map<String, RuleConfiguration> schemaRules) {
        init(schemaDataSources, schemaRules, false);
    }
    
    /**
     * Initialize proxy context.
     *
     * @param schemaDataSources data source map
     * @param schemaRules schema rule map
     * @param isUsingRegistry is using registry or not
     */
    public void init(final Map<String, Map<String, YamlDataSourceParameter>> schemaDataSources, 
                     final Map<String, RuleConfiguration> schemaRules, final boolean isUsingRegistry) {
        initSchemas(schemaDataSources, schemaRules, isUsingRegistry);
    }
    
    private void initSchemas(final Map<String, Map<String, YamlDataSourceParameter>> schemaDataSources, final Map<String, RuleConfiguration> schemaRules, final boolean isUsingRegistry) {
        for (Entry<String, RuleConfiguration> entry : schemaRules.entrySet()) {
            logicSchemas.put(entry.getKey(), createLogicSchema(entry.getKey(), schemaDataSources, entry.getValue(), isUsingRegistry));
        }
    }
    
    private LogicSchema createLogicSchema(final String schemaName, 
                                          final Map<String, Map<String, YamlDataSourceParameter>> schemaDataSources, final RuleConfiguration ruleConfiguration, final boolean isUsingRegistry) {
        boolean isCheckingMetaData = GlobalRegistry.getInstance().getShardingProperties().getValue(ShardingPropertiesConstant.CHECK_TABLE_METADATA_ENABLED);
        return ruleConfiguration instanceof ShardingRuleConfiguration
                ? new ShardingSchema(schemaName, schemaDataSources.get(schemaName), (ShardingRuleConfiguration) ruleConfiguration, isCheckingMetaData, isUsingRegistry) 
                : new MasterSlaveSchema(schemaName, schemaDataSources.get(schemaName), (MasterSlaveRuleConfiguration) ruleConfiguration, isUsingRegistry);
    }
    
    /**
     * Check schema exists.
     *
     * @param schema schema
     * @return schema exists or not
     */
    public boolean schemaExists(final String schema) {
        return logicSchemas.keySet().contains(schema);
    }
    
    /**
     * Get logic schema.
     *
     * @param schemaName schema name
     * @return sharding schema
     */
    public LogicSchema getLogicSchema(final String schemaName) {
        return Strings.isNullOrEmpty(schemaName) ? null : logicSchemas.get(schemaName);
    }
    
    /**
     * Get schema names.
     *
     * @return schema names
     */
    public List<String> getSchemaNames() {
        return new LinkedList<>(logicSchemas.keySet());
    }
    
    /**
     * Renew to add new schema.
     *
     * @param schemaAddedEvent schema add changed event
     */
    @Subscribe
    public synchronized void renew(final SchemaAddedEvent schemaAddedEvent) {
        logicSchemas.put(schemaAddedEvent.getShardingSchemaName(), createLogicSchema(schemaAddedEvent.getShardingSchemaName(), 
                Collections.singletonMap(schemaAddedEvent.getShardingSchemaName(), DataSourceConverter.getDataSourceParameterMap(schemaAddedEvent.getDataSourceConfigurations())), 
                schemaAddedEvent.getRuleConfiguration(), true));
    }
    
    /**
     * Renew to delete new schema.
     *
     * @param schemaDeletedEvent schema delete changed event
     */
    @Subscribe
    public synchronized void renew(final SchemaDeletedEvent schemaDeletedEvent) {
        logicSchemas.remove(schemaDeletedEvent.getShardingSchemaName());
    }
}
