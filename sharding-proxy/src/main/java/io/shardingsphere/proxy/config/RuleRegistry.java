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
@NoArgsConstructor
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
        Properties properties = config.getShardingRule().getProps();
        ShardingProperties shardingProperties = new ShardingProperties(null == properties ? new Properties() : properties);
        showSQL = shardingProperties.getValue(ShardingPropertiesConstant.SQL_SHOW);
        proxyMode = ProxyMode.valueOf(shardingProperties.<String>getValue(ShardingPropertiesConstant.PROXY_MODE));
        transactionType = TransactionType.valueOf(shardingProperties.<String>getValue(ShardingPropertiesConstant.PROXY_TRANSACTION_MODE));
        maxWorkingThreads = shardingProperties.getValue(ShardingPropertiesConstant.PROXY_MAX_WORKING_THREADS);
        shardingRule = config.obtainShardingRule(Collections.<String>emptyList());
        masterSlaveRule = config.obtainMasterSlaveRule();
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
        assignOrchestrationFacade(config);
    }
    
    private void assignOrchestrationFacade(final YamlProxyConfiguration yamlProxyConfiguration) {
        Optional<OrchestrationConfiguration> orchestrationConfig = yamlProxyConfiguration.obtainOrchestrationConfiguration();
        if (orchestrationConfig.isPresent()) {
            orchestrationFacade = new OrchestrationFacade(orchestrationConfig.get());
            orchestrationFacade.init(yamlProxyConfiguration);
        }
    }
    
    /**
     * Judge is master slave only.
     * 
     * @return is master slave only
     */
    public boolean isMasterSlaveOnly() {
        return shardingRule.getTableRules().isEmpty() && !masterSlaveRule.getMasterDataSourceName().isEmpty();
    }
}
