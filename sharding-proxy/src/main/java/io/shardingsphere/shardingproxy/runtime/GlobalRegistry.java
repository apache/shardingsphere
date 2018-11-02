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
import io.shardingsphere.api.config.RuleConfiguration;
import io.shardingsphere.core.constant.ShardingConstant;
import io.shardingsphere.core.constant.properties.ShardingProperties;
import io.shardingsphere.core.constant.properties.ShardingPropertiesConstant;
import io.shardingsphere.core.constant.transaction.TransactionType;
import io.shardingsphere.core.event.ShardingEventBusInstance;
import io.shardingsphere.core.rule.Authentication;
import io.shardingsphere.core.rule.DataSourceParameter;
import io.shardingsphere.core.rule.MasterSlaveRule;
import io.shardingsphere.orchestration.internal.event.config.MasterSlaveConfigurationChangedEvent;
import io.shardingsphere.orchestration.internal.event.config.ShardingConfigurationChangedEvent;
import io.shardingsphere.orchestration.internal.event.state.CircuitStateEventBusEvent;
import io.shardingsphere.orchestration.internal.event.state.DisabledStateEventBusEvent;
import io.shardingsphere.orchestration.internal.rule.OrchestrationMasterSlaveRule;
import io.shardingsphere.orchestration.internal.rule.OrchestrationShardingRule;
import io.shardingsphere.shardingproxy.runtime.nio.BackendNIOConfiguration;
import io.shardingsphere.shardingproxy.util.DataSourceConverter;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Collection;
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
    
    private final List<String> schemaNames = new LinkedList<>();
    
    private final Map<String, ShardingSchema> shardingSchemas = new ConcurrentHashMap<>();
    
    private final Authentication authentication;
    
    private final ShardingProperties shardingProperties;
    
    private final BackendNIOConfiguration backendNIOConfig;
    
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
        ShardingEventBusInstance.getInstance().register(this);
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
    public void init(final Map<String, Map<String, DataSourceParameter>> schemaDataSources,
                     final Map<String, RuleConfiguration> schemaRules, final Authentication authentication, final Map<String, Object> configMap, final Properties props, final boolean isUsingRegistry) {
        if (!configMap.isEmpty()) {
            ConfigMapContext.getInstance().getConfigMap().putAll(configMap);
        }
        initServerConfiguration(authentication, props);
        for (Entry<String, RuleConfiguration> entry : schemaRules.entrySet()) {
            String schemaName = entry.getKey();
            schemaNames.add(schemaName);
            shardingSchemas.put(schemaName, new ShardingSchema(schemaName, schemaDataSources.get(schemaName), entry.getValue(), isUsingRegistry));
        }
    }
    
    private void initServerConfiguration(final Authentication authentication, final Properties props) {
        ShardingProperties shardingProperties = new ShardingProperties(null == props ? new Properties() : props);
        maxConnectionsSizePerQuery = shardingProperties.getValue(ShardingPropertiesConstant.MAX_CONNECTIONS_SIZE_PER_QUERY);
        // TODO just config proxy.transaction.enable here, in future(3.1.0)
        transactionType = shardingProperties.<Boolean>getValue(ShardingPropertiesConstant.PROXY_TRANSACTION_ENABLED) ? TransactionType.XA : TransactionType.LOCAL;
        openTracingEnable = shardingProperties.<Boolean>getValue(ShardingPropertiesConstant.PROXY_OPENTRACING_ENABLED);
        showSQL = shardingProperties.getValue(ShardingPropertiesConstant.SQL_SHOW);
        acceptorSize = shardingProperties.getValue(ShardingPropertiesConstant.ACCEPTOR_SIZE);
        executorSize = shardingProperties.getValue(ShardingPropertiesConstant.EXECUTOR_SIZE);
        // TODO :jiaqi force off use NIO for backend, this feature is not complete yet
        useNIO = false;
        // boolean proxyBackendUseNio = shardingProperties.getValue(ShardingPropertiesConstant.PROXY_BACKEND_USE_NIO);
        int databaseConnectionCount = shardingProperties.getValue(ShardingPropertiesConstant.PROXY_BACKEND_MAX_CONNECTIONS);
        int connectionTimeoutSeconds = shardingProperties.getValue(ShardingPropertiesConstant.PROXY_BACKEND_CONNECTION_TIMEOUT_SECONDS);
        backendNIOConfig = new BackendNIOConfiguration(databaseConnectionCount, connectionTimeoutSeconds);
        this.authentication = authentication;
    }
    
    /**
     * Check schema exists.
     *
     * @param schema schema
     * @return schema exists or not
     */
    public boolean schemaExists(final String schema) {
        return schemaNames.contains(schema);
    }
    
    /**
     * Get sharding schema.
     *
     * @param schemaName schema name
     * @return sharding schema
     */
    public ShardingSchema getShardingSchema(final String schemaName) {
        return Strings.isNullOrEmpty(schemaName) ? null : shardingSchemas.get(schemaName);
    }
    
    /**
     * Renew sharding configuration.
     *
     * @param shardingEvent sharding event.
     */
    @Subscribe
    public void renew(final ShardingConfigurationChangedEvent shardingEvent) {
        initServerConfiguration(shardingEvent.getAuthentication(), shardingEvent.getProps());
        for (Entry<String, ShardingSchema> entry : shardingSchemas.entrySet()) {
            entry.getValue().getBackendDataSource().close();
        }
        shardingSchemas.remove(shardingEvent.getSchemaName());
        shardingSchemas.put(shardingEvent.getSchemaName(), new ShardingSchema(shardingEvent.getSchemaName(), DataSourceConverter.getDataSourceParameterMap(shardingEvent.getDataSourceConfigurations()),
                shardingEvent.getShardingRule().getShardingRuleConfig(), true));
    }
    
    /**
     * Renew master-slave configuration.
     *
     * @param masterSlaveEvent master-slave event.
     */
    @Subscribe
    public void renew(final MasterSlaveConfigurationChangedEvent masterSlaveEvent) {
        initServerConfiguration(masterSlaveEvent.getAuthentication(), masterSlaveEvent.getProps());
        for (Entry<String, ShardingSchema> entry : shardingSchemas.entrySet()) {
            entry.getValue().getBackendDataSource().close();
        }
        shardingSchemas.remove(masterSlaveEvent.getSchemaName());
        shardingSchemas.put(masterSlaveEvent.getSchemaName(), new ShardingSchema(masterSlaveEvent.getSchemaName(),
                DataSourceConverter.getDataSourceParameterMap(masterSlaveEvent.getDataSourceConfigurations()), masterSlaveEvent.getMasterSlaveRuleConfig(), true));
    }
    
    /**
     * Renew circuit breaker dataSource names.
     *
     * @param circuitStateEventBusEvent jdbc circuit event bus event
     */
    @Subscribe
    public void renewCircuitBreakerDataSourceNames(final CircuitStateEventBusEvent circuitStateEventBusEvent) {
        isCircuitBreak = circuitStateEventBusEvent.isCircuitBreak();
    }
    
    /**
     * Renew disabled data source names.
     *
     * @param disabledStateEventBusEvent jdbc disabled event bus event
     */
    @Subscribe
    public void renewDisabledDataSourceNames(final DisabledStateEventBusEvent disabledStateEventBusEvent) {
        Map<String, Collection<String>> disabledSchemaDataSourceMap = disabledStateEventBusEvent.getDisabledSchemaDataSourceMap();
        for (String each : disabledSchemaDataSourceMap.keySet()) {
            DisabledStateEventBusEvent eventBusEvent = new DisabledStateEventBusEvent(Collections.singletonMap(ShardingConstant.LOGIC_SCHEMA_NAME, disabledSchemaDataSourceMap.get(each)));
            renewShardingSchema(each, eventBusEvent);
        }
    }
    
    private void renewShardingSchema(final String each, final DisabledStateEventBusEvent eventBusEvent) {
        if (shardingSchemas.get(each).isMasterSlaveOnly()) {
            renewShardingSchemaWithMasterSlaveRule(shardingSchemas.get(each), eventBusEvent);
        } else {
            renewShardingSchemaWithShardingRule(shardingSchemas.get(each), eventBusEvent);
        }
    }
    
    private void renewShardingSchemaWithShardingRule(final ShardingSchema shardingSchema, final DisabledStateEventBusEvent disabledEvent) {
        for (MasterSlaveRule each : ((OrchestrationShardingRule) shardingSchema.getShardingRule()).getMasterSlaveRules()) {
            ((OrchestrationMasterSlaveRule) each).renew(disabledEvent);
        }
    }
    
    private void renewShardingSchemaWithMasterSlaveRule(final ShardingSchema shardingSchema, final DisabledStateEventBusEvent disabledEvent) {
        ((OrchestrationMasterSlaveRule) shardingSchema.getMasterSlaveRule()).renew(disabledEvent);
    }
}
