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

package io.shardingsphere.proxy.config;

import com.google.common.base.Strings;
import com.google.common.eventbus.Subscribe;
import io.shardingsphere.core.api.config.ProxySchemaRule;
import io.shardingsphere.core.api.config.ProxyServerConfiguration;
import io.shardingsphere.core.constant.ConnectionMode;
import io.shardingsphere.core.constant.properties.ShardingProperties;
import io.shardingsphere.core.constant.properties.ShardingPropertiesConstant;
import io.shardingsphere.core.constant.transaction.TransactionType;
import io.shardingsphere.core.event.ShardingEventBusInstance;
import io.shardingsphere.jdbc.orchestration.internal.event.state.ProxyDisabledStateEventBusEvent;
import io.shardingsphere.core.executor.ShardingExecuteEngine;
import io.shardingsphere.core.rule.DataSourceParameter;
import io.shardingsphere.core.rule.ProxyAuthority;
import io.shardingsphere.jdbc.orchestration.internal.event.config.ProxyConfigurationEventBusEvent;
import io.shardingsphere.jdbc.orchestration.internal.event.state.CircuitStateEventBusEvent;
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
 * Proxy context.
 *
 * @author chenqingyang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public final class ProxyContext {
    
    private static final ProxyContext INSTANCE = new ProxyContext();
    
    private List<String> schemaNames = new LinkedList<>();
    
    private Map<String, RuleRegistry> ruleRegistryMap = new ConcurrentHashMap<>();
    
    private ProxyAuthority proxyAuthority;
    
    private boolean showSQL;
    
    private boolean useNIO;
    
    private int maxConnectionsSizePerQuery;
    
    private int acceptorSize;
    
    private int executorSize;
    
    private ConnectionMode connectionMode;
    
    private TransactionType transactionType;
    
    private boolean isCircuitBreak;
    
    /**
     * Get instance of proxy context.
     *
     * @return instance of proxy context.
     */
    public static ProxyContext getInstance() {
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
     * @param serverConfig server configuration
     * @param schemaDataSources data source map
     * @param schemaRules schema rule map
     */
    public synchronized void init(final ProxyServerConfiguration serverConfig, final Map<String, Map<String, DataSourceParameter>> schemaDataSources, final Map<String, ProxySchemaRule> schemaRules) {
        initServerConfiguration(serverConfig);
        for (Entry<String, ProxySchemaRule> entry : schemaRules.entrySet()) {
            schemaNames.add(entry.getKey());
            ruleRegistryMap.put(entry.getKey(), new RuleRegistry(schemaDataSources.get(entry.getKey()), entry.getValue(), entry.getKey()));
        }
    }
    
    private void initServerConfiguration(final ProxyServerConfiguration serverConfiguration) {
        Properties properties = serverConfiguration.getProps();
        ShardingProperties shardingProperties = new ShardingProperties(null == properties ? new Properties() : properties);
        connectionMode = ConnectionMode.valueOf(shardingProperties.<String>getValue(ShardingPropertiesConstant.CONNECTION_MODE));
        maxConnectionsSizePerQuery = shardingProperties.getValue(ShardingPropertiesConstant.MAX_CONNECTIONS_SIZE_PER_QUERY);
        // TODO just config proxy.transaction.enable here, in future(3.1.0)
        transactionType = shardingProperties.<Boolean>getValue(ShardingPropertiesConstant.PROXY_TRANSACTION_ENABLED) ? TransactionType.XA : TransactionType.LOCAL;
        showSQL = shardingProperties.getValue(ShardingPropertiesConstant.SQL_SHOW);
        acceptorSize = shardingProperties.getValue(ShardingPropertiesConstant.ACCEPTOR_SIZE);
        executorSize = shardingProperties.getValue(ShardingPropertiesConstant.EXECUTOR_SIZE);
        // TODO :jiaqi force off use NIO for backend, this feature is not complete yet
        useNIO = false;
        // boolean proxyBackendUseNio = shardingProperties.getValue(ShardingPropertiesConstant.PROXY_BACKEND_USE_NIO);
        proxyAuthority = serverConfiguration.getProxyAuthority();
    }
    
    /**
     * Initialize sharding meta data.
     *
     * @param executeEngine sharding execute engine
     */
    public void initShardingMetaData(final ShardingExecuteEngine executeEngine) {
        for (RuleRegistry each : ruleRegistryMap.values()) {
            each.initShardingMetaData(executeEngine);
        }
    }
    
    /**
     * check schema exists.
     *
     * @param schema schema
     * @return schema exists or not
     */
    public boolean schemaExists(final String schema) {
        return schemaNames.contains(schema);
    }
    
    /**
     * get rule registry of schema.
     *
     * @param schema schema
     * @return rule registry of schema
     */
    public RuleRegistry getRuleRegistry(final String schema) {
        return Strings.isNullOrEmpty(schema) ? null : ruleRegistryMap.get(schema);
    }
    
    /**
     * get a default schema.
     *
     * @return schema
     */
    public String getDefaultSchema() {
        return schemaNames.get(0);
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
     * Renew proxy configuration.
     *
     * @param proxyConfigurationEventBusEvent proxy event bus event.
     */
    @Subscribe
    public void renew(final ProxyConfigurationEventBusEvent proxyConfigurationEventBusEvent) {
        initServerConfiguration(proxyConfigurationEventBusEvent.getServerConfiguration());
        for (Entry<String, RuleRegistry> entry : ruleRegistryMap.entrySet()) {
            String schemaName = entry.getKey();
            RuleRegistry ruleRegistry = entry.getValue();
            ruleRegistry.getBackendDataSource().close();
            ruleRegistry.init(proxyConfigurationEventBusEvent.getSchemaDataSourceMap().get(schemaName), proxyConfigurationEventBusEvent.getSchemaShardingRuleMap().get(schemaName), schemaName);
        }
    }
    
    /**
     * Renew disabled data source names.
     *
     * @param disabledStateEventBusEvent jdbc disabled event bus event
     */
    @Subscribe
    public void renewDisabledDataSourceNames(final ProxyDisabledStateEventBusEvent disabledStateEventBusEvent) {
        for (Entry<String, RuleRegistry> entry : ruleRegistryMap.entrySet()) {
            entry.getValue().setDisabledDataSourceNames(disabledStateEventBusEvent.getDisabledSchemaDataSourceMap().get(entry.getKey()));
        }
    }
}
