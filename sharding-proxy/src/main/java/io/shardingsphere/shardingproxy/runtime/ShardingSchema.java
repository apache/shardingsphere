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

import io.shardingsphere.api.config.ShardingRuleConfiguration;
import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.executor.ShardingExecuteEngine;
import io.shardingsphere.core.metadata.ShardingMetaData;
import io.shardingsphere.core.rule.DataSourceParameter;
import io.shardingsphere.core.rule.MasterSlaveRule;
import io.shardingsphere.core.rule.ShardingRule;
import io.shardingsphere.core.yaml.YamlRuleConfiguration;
import io.shardingsphere.orchestration.internal.rule.OrchestrationMasterSlaveRule;
import io.shardingsphere.orchestration.internal.rule.OrchestrationShardingRule;
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
    
    private ShardingMetaData metaData;
    
    public ShardingSchema(final String name, final Map<String, DataSourceParameter> dataSources, final YamlRuleConfiguration rule, final boolean isUsingRegistry) {
        this.name = name;
        // TODO :jiaqi only use JDBC need connect db via JDBC, netty style should use SQL packet to get metadata
        this.dataSources = dataSources;
        shardingRule = getShardingRule(rule, isUsingRegistry);
        masterSlaveRule = getMasterSlaveRule(rule, isUsingRegistry);
        backendDataSource = new JDBCBackendDataSource(dataSources);
    }
    
    private ShardingRule getShardingRule(final YamlRuleConfiguration rule, final boolean isUsingRegistry) {
        return isUsingRegistry ? new OrchestrationShardingRule(null == rule.getShardingRule() ? new ShardingRuleConfiguration() : rule.getShardingRule().getShardingRuleConfiguration(), dataSources.keySet())
                : new ShardingRule(null == rule.getShardingRule() ? new ShardingRuleConfiguration() : rule.getShardingRule().getShardingRuleConfiguration(), dataSources.keySet());
    }
    
    private MasterSlaveRule getMasterSlaveRule(final YamlRuleConfiguration rule, final boolean isUsingRegistry) {
        if (null == rule.getMasterSlaveRule()) {
            return null;
        }
        return isUsingRegistry ? new OrchestrationMasterSlaveRule(rule.getMasterSlaveRule().getMasterSlaveRuleConfiguration()) : new MasterSlaveRule(rule.getMasterSlaveRule().getMasterSlaveRuleConfiguration());
    }
    
    /**
     * Initialize sharding meta data.
     *
     * @param executeEngine sharding execute engine
     */
    public void initShardingMetaData(final ShardingExecuteEngine executeEngine) {
        metaData = new ShardingMetaData(getDataSourceURLs(dataSources), shardingRule, 
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
