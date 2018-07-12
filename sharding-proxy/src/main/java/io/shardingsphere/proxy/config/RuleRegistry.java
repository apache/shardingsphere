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

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
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
import io.shardingsphere.jdbc.orchestration.api.config.OrchestrationConfiguration;
import io.shardingsphere.jdbc.orchestration.internal.OrchestrationFacade;
import io.shardingsphere.proxy.backend.common.ProxyMode;
import io.shardingsphere.proxy.metadata.ProxyShardingMetaData;
import io.shardingsphere.proxy.yaml.YamlProxyConfiguration;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
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
@NoArgsConstructor
@Getter
public final class RuleRegistry implements AutoCloseable {
    
    private static final RuleRegistry INSTANCE = new RuleRegistry();
    
    private ShardingRule shardingRule;
    
    private MasterSlaveRule masterSlaveRule;
    
    private boolean isOnlyMasterSlave;
    
    private boolean withoutJdbc;
    
    private Map<String, DataSource> dataSourceMap;
    
    private Map<String, DataSourceParameter> dataSourceConfigurationMap;
    
    private ShardingMetaData shardingMetaData; 

    private int maxWorkingThreads = Runtime.getRuntime().availableProcessors() * 2;
    
    private ListeningExecutorService executorService;
    
    private ProxyMode proxyMode;
    
    private boolean showSQL;
    
    private TransactionType transactionType;
    
    private ProxyAuthority proxyAuthority;
    
    private OrchestrationFacade orchestrationFacade;
    
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
    public void init(final YamlProxyConfiguration config) {
        transactionType = TransactionType.findByValue(config.getTransactionMode());
        dataSourceMap = ProxyRawDataSourceFactory.create(transactionType, config);
        shardingRule = config.obtainShardingRule(Collections.<String>emptyList());
        masterSlaveRule = config.obtainMasterSlaveRule();
        isOnlyMasterSlave = shardingRule.getTableRules().isEmpty() && !masterSlaveRule.getMasterDataSourceName().isEmpty();
        withoutJdbc = config.isWithoutJdbc();
        dataSourceConfigurationMap = new HashMap<>(128, 1);
        for (Map.Entry<String, DataSourceParameter> entry : config.getDataSources().entrySet()) {
            if (withoutJdbc) {
                dataSourceConfigurationMap.put(entry.getKey(), entry.getValue());
            }
        }
        Properties properties = config.getShardingRule().getProps();
        ShardingProperties shardingProperties = new ShardingProperties(null == properties ? new Properties() : properties);
        proxyMode = ProxyMode.valueOf(shardingProperties.<String>getValue(ShardingPropertiesConstant.PROXY_MODE));
        maxWorkingThreads = config.getMaxWorkingThreads();
        executorService = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(maxWorkingThreads));
        showSQL = shardingProperties.getValue(ShardingPropertiesConstant.SQL_SHOW);
        shardingDataSourceMetaData = new ShardingDataSourceMetaData(dataSourceMap, DatabaseType.MySQL);
        shardingMetaData = new ProxyShardingMetaData(executorService, dataSourceMap);
        if (!isOnlyMasterSlave) {
            shardingMetaData.init(shardingRule);
        }
        proxyAuthority = config.getProxyAuthority();
        Preconditions.checkNotNull(proxyAuthority.getUsername(), "Invalid configuration for proxyAuthority.");
        assignOrchestrationFacade(config);
    }
    
    private void assignOrchestrationFacade(final YamlProxyConfiguration yamlProxyConfiguration) {
        Optional<OrchestrationConfiguration> orchestrationConfig = yamlProxyConfiguration.obtainOrchestrationConfiguration();
        if (orchestrationConfig.isPresent()) {
            orchestrationFacade = new OrchestrationFacade(orchestrationConfig.get());
            orchestrationFacade.init(yamlProxyConfiguration);
        }
    }
    
    @Override
    public void close() {
        if (null != orchestrationFacade) {
            orchestrationFacade.close();
        }
    }
}
