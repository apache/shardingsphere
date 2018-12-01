/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.shardingproxy.runtime;

import com.google.common.base.Strings;
import com.google.common.eventbus.Subscribe;
import io.shardingsphere.api.ConfigMapContext;
import io.shardingsphere.api.config.MasterSlaveRuleConfiguration;
import io.shardingsphere.api.config.RuleConfiguration;
import io.shardingsphere.api.config.ShardingRuleConfiguration;
import io.shardingsphere.core.constant.properties.ShardingProperties;
import io.shardingsphere.core.constant.properties.ShardingPropertiesConstant;
import io.shardingsphere.core.constant.transaction.TransactionType;
import io.shardingsphere.core.rule.Authentication;
import io.shardingsphere.core.rule.DataSourceParameter;
import io.shardingsphere.orchestration.internal.config.event.AuthenticationChangedEvent;
import io.shardingsphere.orchestration.internal.config.event.PropertiesChangedEvent;
import io.shardingsphere.orchestration.internal.eventbus.ShardingOrchestrationEventBus;
import io.shardingsphere.orchestration.internal.state.event.CircuitStateChangedEvent;
import io.shardingsphere.shardingproxy.runtime.schema.LogicSchema;
import io.shardingsphere.shardingproxy.runtime.schema.MasterSlaveSchema;
import io.shardingsphere.shardingproxy.runtime.schema.ShardingSchema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
    
    private final Map<String, LogicSchema> logicSchemas = new ConcurrentHashMap<>();
    
    private ShardingProperties shardingProperties;
    
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
        ShardingOrchestrationEventBus.getInstance().register(this);
    }
    
    /**
     * Initialize proxy context.
     *
     * @param schemaDataSources data source map
     * @param schemaRules schema rule map
     * @param authentication authentication
     * @param configMap config map
     * @param props properties
     */
    public void init(final Map<String, Map<String, DataSourceParameter>> schemaDataSources,
                     final Map<String, RuleConfiguration> schemaRules, final Authentication authentication, final Map<String, Object> configMap, final Properties props) {
        init(schemaDataSources, schemaRules, authentication, configMap, props, false);
    }
    
    /**
     * Initialize proxy context.
     *
     * @param schemaDataSources data source map
     * @param schemaRules schema rule map
     * @param authentication authentication
     * @param configMap config map
     * @param props properties
     * @param isUsingRegistry is using registry or not
     */
    public void init(final Map<String, Map<String, DataSourceParameter>> schemaDataSources, final Map<String, RuleConfiguration> schemaRules, 
                     final Authentication authentication, final Map<String, Object> configMap, final Properties props, final boolean isUsingRegistry) {
        if (!configMap.isEmpty()) {
            ConfigMapContext.getInstance().getConfigMap().putAll(configMap);
        }
        shardingProperties = new ShardingProperties(null == props ? new Properties() : props);
        this.authentication = authentication;
        initSchema(schemaDataSources, schemaRules, isUsingRegistry);
    }
    
    private void initSchema(final Map<String, Map<String, DataSourceParameter>> schemaDataSources, final Map<String, RuleConfiguration> schemaRules, final boolean isUsingRegistry) {
        for (Entry<String, RuleConfiguration> entry : schemaRules.entrySet()) {
            String schemaName = entry.getKey();
            logicSchemas.put(schemaName, createLogicSchema(schemaName, schemaDataSources, entry.getValue(), isUsingRegistry));
        }
    }
    
    private LogicSchema createLogicSchema(final String schemaName,
                                          final Map<String, Map<String, DataSourceParameter>> schemaDataSources, final RuleConfiguration ruleConfiguration, final boolean isUsingRegistry) {
        return ruleConfiguration instanceof ShardingRuleConfiguration
                ? new ShardingSchema(schemaName, schemaDataSources.get(schemaName), (ShardingRuleConfiguration) ruleConfiguration, isUsingRegistry)
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
    public void renew(final PropertiesChangedEvent propertiesChangedEvent) {
        shardingProperties = new ShardingProperties(propertiesChangedEvent.getProps());
    }
    
    /**
     * Renew authentication.
     *
     * @param authenticationEvent authentication event
     */
    @Subscribe
    public void renew(final AuthenticationChangedEvent authenticationEvent) {
        authentication = authenticationEvent.getAuthentication();
    }
    
    /**
     * Renew circuit breaker data source names.
     *
     * @param circuitStateChangedEvent circuit state changed event
     */
    @Subscribe
    public void renewCircuitBreakerDataSourceNames(final CircuitStateChangedEvent circuitStateChangedEvent) {
        isCircuitBreak = circuitStateChangedEvent.isCircuitBreak();
    }
}
