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

import com.google.common.base.Preconditions;
import com.google.common.eventbus.Subscribe;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import io.shardingsphere.core.api.config.MasterSlaveRuleConfiguration;
import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.constant.ShardingProperties;
import io.shardingsphere.core.constant.ShardingPropertiesConstant;
import io.shardingsphere.core.constant.TransactionType;
import io.shardingsphere.core.metadata.ShardingMetaData;
import io.shardingsphere.core.metadata.datasource.ShardingDataSourceMetaData;
import io.shardingsphere.core.rule.DataSourceParameter;
import io.shardingsphere.core.rule.MasterSlaveRule;
import io.shardingsphere.core.rule.ProxyAuthority;
import io.shardingsphere.core.rule.ShardingRule;
import io.shardingsphere.jdbc.orchestration.internal.OrchestrationProxyConfiguration;
import io.shardingsphere.jdbc.orchestration.internal.eventbus.ProxyEventBusEvent;
import io.shardingsphere.proxy.backend.common.ProxyMode;
import io.shardingsphere.proxy.metadata.ProxyShardingMetaData;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.Executors;

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
    
    private final boolean withoutJdbc = false;
    
    private ShardingRule shardingRule;
    
    private MasterSlaveRule masterSlaveRule;
    
    private Map<String, DataSource> dataSourceMap;
    
    private Map<String, DataSourceParameter> dataSourceConfigurationMap;
    
    private ProxyAuthority proxyAuthority;
    
    private ShardingMetaData shardingMetaData;
    
    private ListeningExecutorService executorService;
    
    private boolean showSQL;
    
    private ProxyMode proxyMode;
    
    private TransactionType transactionType;
    
    private int maxWorkingThreads;
    
    private ShardingDataSourceMetaData shardingDataSourceMetaData;
    
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
        Properties properties = config.getShardingRule().getProps();
        ShardingProperties shardingProperties = new ShardingProperties(null == properties ? new Properties() : properties);
        showSQL = shardingProperties.getValue(ShardingPropertiesConstant.SQL_SHOW);
        proxyMode = ProxyMode.valueOf(shardingProperties.<String>getValue(ShardingPropertiesConstant.PROXY_MODE));
        transactionType = TransactionType.valueOf(shardingProperties.<String>getValue(ShardingPropertiesConstant.PROXY_TRANSACTION_MODE));
        maxWorkingThreads = shardingProperties.getValue(ShardingPropertiesConstant.PROXY_MAX_WORKING_THREADS);
        shardingRule = new ShardingRule(config.getShardingRule().getShardingRuleConfiguration(), config.getDataSources().keySet());
        masterSlaveRule = null == config.getMasterSlaveRule().getMasterDataSourceName()
                ? new MasterSlaveRule(new MasterSlaveRuleConfiguration("", "", Collections.singletonList(""), null))
                : new MasterSlaveRule(config.getMasterSlaveRule().getMasterSlaveRuleConfiguration());
        dataSourceMap = ProxyRawDataSourceFactory.create(transactionType, config);
        dataSourceConfigurationMap = new HashMap<>(128, 1);
        if (withoutJdbc) {
            for (Entry<String, DataSourceParameter> entry : config.getDataSources().entrySet()) {
                dataSourceConfigurationMap.put(entry.getKey(), entry.getValue());
            }
        }
        executorService = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(maxWorkingThreads));
        shardingDataSourceMetaData = new ShardingDataSourceMetaData(dataSourceMap, DatabaseType.MySQL);
        shardingMetaData = new ProxyShardingMetaData(executorService, dataSourceMap);
        if (!isMasterSlaveOnly()) {
            shardingMetaData.init(shardingRule);
        }
        proxyAuthority = config.getProxyAuthority();
        Preconditions.checkNotNull(proxyAuthority.getUsername(), "Invalid configuration for proxy authority.");
    }
    
    /**
     * Judge is master slave only.
     * 
     * @return is master slave only
     */
    public boolean isMasterSlaveOnly() {
        return shardingRule.getTableRules().isEmpty() && !masterSlaveRule.getMasterDataSourceName().isEmpty();
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
