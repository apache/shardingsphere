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

import com.google.common.eventbus.Subscribe;
import com.google.common.util.concurrent.MoreExecutors;
import io.shardingsphere.core.api.config.ShardingRuleConfiguration;
import io.shardingsphere.core.constant.ConnectionMode;
import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.constant.ShardingProperties;
import io.shardingsphere.core.constant.ShardingPropertiesConstant;
import io.shardingsphere.core.constant.TransactionType;
import io.shardingsphere.core.metadata.ShardingMetaData;
import io.shardingsphere.core.metadata.datasource.ShardingDataSourceMetaData;
import io.shardingsphere.core.metadata.table.ShardingTableMetaData;
import io.shardingsphere.core.metadata.table.executor.TableMetaDataInitializer;
import io.shardingsphere.core.rule.DataSourceParameter;
import io.shardingsphere.core.rule.MasterSlaveRule;
import io.shardingsphere.core.rule.ProxyAuthority;
import io.shardingsphere.core.rule.ShardingRule;
import io.shardingsphere.core.transaction.spi.TransactionManager;
import io.shardingsphere.jdbc.orchestration.internal.OrchestrationProxyConfiguration;
import io.shardingsphere.jdbc.orchestration.internal.eventbus.ProxyEventBusEvent;
import io.shardingsphere.proxy.backend.jdbc.datasource.JDBCBackendDataSource;
import io.shardingsphere.proxy.util.ProxyTransactionLoader;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.ExecutorService;

/**
 * Sharding rule registry.
 *
 * @author zhangliang
 * @author zhangyonglun
 * @author panjuan
 * @author zhaojun
 * @author wangkai
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public final class RuleRegistry {
    
    private static final RuleRegistry INSTANCE = new RuleRegistry();
    
    private ShardingRule shardingRule;
    
    private MasterSlaveRule masterSlaveRule;
    
    private JDBCBackendDataSource backendDataSource;
    
    private Map<String, DataSourceParameter> dataSourceConfigurationMap;
    
    private boolean showSQL;
    
    private ConnectionMode connectionMode;
    
    private int acceptorSize;
    
    private int executorSize;
    
    private BackendNIOConfiguration backendNIOConfig;
    
    private TransactionType transactionType;
    
    private TransactionManager transactionManager;
    
    private ProxyAuthority proxyAuthority;
    
    private ShardingMetaData metaData;
    
    /**
     * Get instance of sharding rule registry.
     *
     * @return instance of sharding rule registry
     */
    public static RuleRegistry getInstance() {
        return INSTANCE;
    }
    
    /**
     * Initialize rule registry.
     *
     * @param config yaml proxy configuration
     */
    public synchronized void init(final OrchestrationProxyConfiguration config) {
        Properties properties = null == config.getShardingRule() ? config.getMasterSlaveRule().getProps() : config.getShardingRule().getProps();
        ShardingProperties shardingProperties = new ShardingProperties(null == properties ? new Properties() : properties);
        showSQL = shardingProperties.getValue(ShardingPropertiesConstant.SQL_SHOW);
        connectionMode = ConnectionMode.valueOf(shardingProperties.<String>getValue(ShardingPropertiesConstant.CONNECTION_MODE));
        transactionType = TransactionType.valueOf(shardingProperties.<String>getValue(ShardingPropertiesConstant.PROXY_TRANSACTION_MODE));
        transactionManager = ProxyTransactionLoader.load(transactionType);
        acceptorSize = shardingProperties.getValue(ShardingPropertiesConstant.ACCEPTOR_SIZE);
        executorSize = shardingProperties.getValue(ShardingPropertiesConstant.EXECUTOR_SIZE);
        // TODO :jiaqi force off use NIO for backend, this feature is not complete yet
        boolean useNIO = false;
//        boolean proxyBackendUseNio = shardingProperties.getValue(ShardingPropertiesConstant.PROXY_BACKEND_USE_NIO);
        int databaseConnectionCount = shardingProperties.getValue(ShardingPropertiesConstant.PROXY_BACKEND_MAX_CONNECTIONS);
        int connectionTimeoutSeconds = shardingProperties.getValue(ShardingPropertiesConstant.PROXY_BACKEND_CONNECTION_TIMEOUT_SECONDS);
        backendNIOConfig = new BackendNIOConfiguration(useNIO, databaseConnectionCount, connectionTimeoutSeconds);
        shardingRule = new ShardingRule(
                null == config.getShardingRule() ? new ShardingRuleConfiguration() : config.getShardingRule().getShardingRuleConfiguration(), config.getDataSources().keySet());
        if (null != config.getMasterSlaveRule()) {
            masterSlaveRule = new MasterSlaveRule(config.getMasterSlaveRule().getMasterSlaveRuleConfiguration());
        }
        // TODO :jiaqi only use JDBC need connect db via JDBC, netty style should use SQL packet to get metadata
        backendDataSource = new JDBCBackendDataSource(transactionType, config.getDataSources());
        dataSourceConfigurationMap = config.getDataSources();
        proxyAuthority = config.getProxyAuthority();
    }
    
    /**
     * Initialize rule registry.
     *
     * @param executorService executor service
     */
    public void initShardingMetaData(final ExecutorService executorService) {
        ShardingDataSourceMetaData shardingDataSourceMetaData = new ShardingDataSourceMetaData(getDataSourceURLs(dataSourceConfigurationMap), shardingRule, DatabaseType.MySQL);
        ShardingTableMetaData shardingTableMetaData = new ShardingTableMetaData(
                new TableMetaDataInitializer(MoreExecutors.listeningDecorator(executorService), new ProxyTableMetaDataConnectionManager(backendDataSource)).load(shardingRule));
        metaData = new ShardingMetaData(shardingDataSourceMetaData, shardingTableMetaData);
    }
    
    private static Map<String, String> getDataSourceURLs(final Map<String, DataSourceParameter> dataSourceParameters) {
        Map<String, String> result = new LinkedHashMap<>(dataSourceParameters.size(), 1);
        for (Entry<String, DataSourceParameter> entry : dataSourceParameters.entrySet()) {
            result.put(entry.getKey(), entry.getValue().getUrl());
        }
        return result;
    }
    
    /**
     * Judge is master slave only.
     * 
     * @return is master slave only
     */
    public boolean isMasterSlaveOnly() {
        return shardingRule.getTableRules().isEmpty() && null != masterSlaveRule;
    }
    
    /**
     * Renew rule registry.
     *
     * @param proxyEventBusEvent proxy event bus event.
     */
    @Subscribe
    public void renew(final ProxyEventBusEvent proxyEventBusEvent) {
        init(new OrchestrationProxyConfiguration(proxyEventBusEvent.getDataSources(), proxyEventBusEvent.getOrchestrationConfig()));
    }
}
