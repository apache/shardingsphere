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

import io.shardingsphere.api.config.MasterSlaveRuleConfiguration;
import io.shardingsphere.api.config.RuleConfiguration;
import io.shardingsphere.api.config.ShardingRuleConfiguration;
import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.executor.ShardingExecuteEngine;
import io.shardingsphere.core.metadata.ShardingMetaData;
import io.shardingsphere.core.rule.DataSourceParameter;
import io.shardingsphere.core.rule.MasterSlaveRule;
import io.shardingsphere.core.rule.ShardingRule;
import io.shardingsphere.orchestration.internal.rule.OrchestrationMasterSlaveRule;
import io.shardingsphere.orchestration.internal.rule.OrchestrationShardingRule;
import io.shardingsphere.shardingproxy.backend.BackendExecutorContext;
import io.shardingsphere.shardingproxy.backend.jdbc.datasource.JDBCBackendDataSource;
import io.shardingsphere.shardingproxy.runtime.metadata.ProxyTableMetaDataConnectionManager;
import lombok.Getter;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Sharding schema.
 *
 * @author zhangliang
 * @author zhangyonglun
 * @author panjuan
 * @author zhaojun
 * @author wangkai
 */
@Getter
public final class ShardingSchema {
    
    private final String name;
    
    private final Map<String, DataSourceParameter> dataSources;
    
    private final ShardingRule shardingRule;
    
    private final MasterSlaveRule masterSlaveRule;
    
    private final JDBCBackendDataSource backendDataSource;
    
    private final ShardingMetaData metaData;
    
    public ShardingSchema(final String name, final Map<String, DataSourceParameter> dataSources, final RuleConfiguration ruleConfiguration, final boolean isUsingRegistry) {
        this.name = name;
        // TODO :jiaqi only use JDBC need connect db via JDBC, netty style should use SQL packet to get metadata
        this.dataSources = dataSources;
        shardingRule = ruleConfiguration instanceof ShardingRuleConfiguration ? getShardingRule((ShardingRuleConfiguration) ruleConfiguration, isUsingRegistry) : null;
        masterSlaveRule = ruleConfiguration instanceof MasterSlaveRuleConfiguration ? getMasterSlaveRule((MasterSlaveRuleConfiguration) ruleConfiguration, isUsingRegistry) : null;
        metaData = ruleConfiguration instanceof ShardingRuleConfiguration ? getShardingMetaData(BackendExecutorContext.getInstance().getExecuteEngine()) : null;
        backendDataSource = new JDBCBackendDataSource(dataSources);
    }
    
    private ShardingRule getShardingRule(final ShardingRuleConfiguration shardingRule, final boolean isUsingRegistry) {
        return isUsingRegistry ? new OrchestrationShardingRule(shardingRule, dataSources.keySet()) : new ShardingRule(shardingRule, dataSources.keySet());
    }
    
    private MasterSlaveRule getMasterSlaveRule(final MasterSlaveRuleConfiguration masterSlaveRule, final boolean isUsingRegistry) {
        return isUsingRegistry ? new OrchestrationMasterSlaveRule(masterSlaveRule) : new MasterSlaveRule(masterSlaveRule);
    }
    
    private ShardingMetaData getShardingMetaData(final ShardingExecuteEngine executeEngine) {
        return new ShardingMetaData(getDataSourceURLs(dataSources), shardingRule,
                DatabaseType.MySQL, executeEngine, new ProxyTableMetaDataConnectionManager(backendDataSource), GlobalRegistry.getInstance().getMaxConnectionsSizePerQuery());
    }
    
    private Map<String, String> getDataSourceURLs(final Map<String, DataSourceParameter> dataSourceParameters) {
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
}
