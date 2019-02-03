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

package org.apache.shardingsphere.shardingproxy.runtime;

import com.google.common.base.Strings;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.api.config.RuleConfiguration;
import org.apache.shardingsphere.api.config.masterslave.MasterSlaveRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.ShardingRuleConfiguration;
import org.apache.shardingsphere.core.constant.DatabaseType;
import org.apache.shardingsphere.core.constant.properties.ShardingProperties;
import org.apache.shardingsphere.core.constant.properties.ShardingPropertiesConstant;
import org.apache.shardingsphere.core.rule.Authentication;
import org.apache.shardingsphere.orchestration.internal.eventbus.ShardingOrchestrationEventBus;
import org.apache.shardingsphere.orchestration.internal.registry.config.event.AuthenticationChangedEvent;
import org.apache.shardingsphere.orchestration.internal.registry.config.event.PropertiesChangedEvent;
import org.apache.shardingsphere.orchestration.internal.registry.config.event.SchemaAddedEvent;
import org.apache.shardingsphere.orchestration.internal.registry.config.event.SchemaDeletedEvent;
import org.apache.shardingsphere.orchestration.internal.registry.state.event.CircuitStateChangedEvent;
import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.recognizer.JDBCURLRecognizerEngine;
import org.apache.shardingsphere.shardingproxy.config.yaml.YamlDataSourceParameter;
import org.apache.shardingsphere.shardingproxy.runtime.schema.LogicSchema;
import org.apache.shardingsphere.shardingproxy.runtime.schema.MasterSlaveSchema;
import org.apache.shardingsphere.shardingproxy.runtime.schema.ShardingSchema;
import org.apache.shardingsphere.shardingproxy.util.DataSourceConverter;
import org.apache.shardingsphere.transaction.ShardingTransactionManagerEngine;
import org.apache.shardingsphere.transaction.core.TransactionType;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Global registry.
 *
 * @author chenqingyang
 * @author panjuan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public final class GlobalRegistry {
    
    private static final GlobalRegistry INSTANCE = new GlobalRegistry();
    
    private final EventBus eventBus = ShardingOrchestrationEventBus.getInstance();
    
    private final Map<String, LogicSchema> logicSchemas = new ConcurrentHashMap<>();
    
    private DatabaseType databaseType;
    
    private ShardingProperties shardingProperties = new ShardingProperties(new Properties());
    
    private ShardingTransactionManagerEngine shardingTransactionManagerEngine = new ShardingTransactionManagerEngine();
    
    private Authentication authentication;
    
    private boolean isCircuitBreak;
    
    /**
     * Get instance of proxy context.
     *
     * @return instance of proxy context.
     */
    public static GlobalRegistry getInstance() {
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
     * @param authentication authentication
     * @param props properties
     */
    public void init(final Map<String, Map<String, YamlDataSourceParameter>> schemaDataSources,
                     final Map<String, RuleConfiguration> schemaRules, final Authentication authentication, final Properties props) {
        init(schemaDataSources, schemaRules, authentication, props, false);
    }
    
    /**
     * Initialize proxy context.
     *
     * @param schemaDataSources data source map
     * @param schemaRules schema rule map
     * @param authentication authentication
     * @param props properties
     * @param isUsingRegistry is using registry or not
     */
    public void init(final Map<String, Map<String, YamlDataSourceParameter>> schemaDataSources, 
                     final Map<String, RuleConfiguration> schemaRules, final Authentication authentication, final Properties props, final boolean isUsingRegistry) {
        if (null != props) {
            shardingProperties = new ShardingProperties(props);
        }
        databaseType = JDBCURLRecognizerEngine.getDatabaseType(schemaDataSources.values().iterator().next().values().iterator().next().getUrl());
        this.authentication = authentication;
        initSchema(schemaDataSources, schemaRules, isUsingRegistry);
    }
    
    private void initSchema(final Map<String, Map<String, YamlDataSourceParameter>> schemaDataSources, final Map<String, RuleConfiguration> schemaRules, final boolean isUsingRegistry) {
        for (Entry<String, RuleConfiguration> entry : schemaRules.entrySet()) {
            String schemaName = entry.getKey();
            logicSchemas.put(schemaName, createLogicSchema(schemaName, schemaDataSources, entry.getValue(), isUsingRegistry));
        }
    }
    
    private LogicSchema createLogicSchema(final String schemaName,
                                          final Map<String, Map<String, YamlDataSourceParameter>> schemaDataSources, final RuleConfiguration ruleConfiguration, final boolean isUsingRegistry) {
        return ruleConfiguration instanceof ShardingRuleConfiguration
                ? new ShardingSchema(schemaName, schemaDataSources.get(schemaName), (ShardingRuleConfiguration) ruleConfiguration, 
                shardingProperties.<Boolean>getValue(ShardingPropertiesConstant.CHECK_TABLE_METADATA_ENABLED), isUsingRegistry) 
                : new MasterSlaveSchema(schemaName, schemaDataSources.get(schemaName), (MasterSlaveRuleConfiguration) ruleConfiguration, isUsingRegistry);
    }
    
    /**
     * Get transaction type.
     *
     * @return transaction type
     */
    public TransactionType getTransactionType() {
        return TransactionType.valueOf((String) shardingProperties.getValue(ShardingPropertiesConstant.PROXY_TRANSACTION_TYPE));
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
     * Renew properties.
     *
     * @param propertiesChangedEvent properties changed event
     */
    @Subscribe
    public synchronized void renew(final PropertiesChangedEvent propertiesChangedEvent) {
        shardingProperties = new ShardingProperties(propertiesChangedEvent.getProps());
    }
    
    /**
     * Renew authentication.
     *
     * @param authenticationChangedEvent authentication changed event
     */
    @Subscribe
    public synchronized void renew(final AuthenticationChangedEvent authenticationChangedEvent) {
        authentication = authenticationChangedEvent.getAuthentication();
    }
    
    /**
     * Renew circuit breaker state.
     *
     * @param circuitStateChangedEvent circuit state changed event
     */
    @Subscribe
    public synchronized void renew(final CircuitStateChangedEvent circuitStateChangedEvent) {
        isCircuitBreak = circuitStateChangedEvent.isCircuitBreak();
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
