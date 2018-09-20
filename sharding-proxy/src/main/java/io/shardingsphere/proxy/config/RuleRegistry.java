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

import io.shardingsphere.core.yaml.YamlRuleConfiguration;
import io.shardingsphere.core.api.config.ShardingRuleConfiguration;
import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.executor.ShardingExecuteEngine;
import io.shardingsphere.core.metadata.ShardingMetaData;
import io.shardingsphere.core.rule.DataSourceParameter;
import io.shardingsphere.core.rule.MasterSlaveRule;
import io.shardingsphere.core.rule.ShardingRule;
import io.shardingsphere.proxy.backend.jdbc.datasource.JDBCBackendDataSource;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Sharding rule registry.
 *
 * @author zhangliang
 * @author zhangyonglun
 * @author panjuan
 * @author zhaojun
 * @author wangkai
 */
@Getter
public final class RuleRegistry {
    
    private final String schemaName;
    
    private final Map<String, DataSourceParameter> dataSources;
    
    private final ShardingRule shardingRule;
    
    private final MasterSlaveRule masterSlaveRule;
    
    private final JDBCBackendDataSource backendDataSource;
    
    private ShardingMetaData metaData;
    
    @Setter
    private Collection<String> disabledDataSourceNames = new LinkedList<>();
    
    public RuleRegistry(final String schemaName, final Map<String, DataSourceParameter> dataSources, final YamlRuleConfiguration rule) {
        this.schemaName = schemaName;
        // TODO :jiaqi only use JDBC need connect db via JDBC, netty style should use SQL packet to get metadata
        this.dataSources = dataSources;
        shardingRule = new ShardingRule(null == rule.getShardingRule() ? new ShardingRuleConfiguration() : rule.getShardingRule().getShardingRuleConfiguration(), dataSources.keySet());
        masterSlaveRule = null == rule.getMasterSlaveRule() ? null : new MasterSlaveRule(rule.getMasterSlaveRule().getMasterSlaveRuleConfiguration());
        backendDataSource = new JDBCBackendDataSource(this);
    }
    
    /**
     * Initialize sharding meta data.
     *
     * @param executeEngine sharding execute engine
     */
    public void initShardingMetaData(final ShardingExecuteEngine executeEngine) {
        metaData = new ShardingMetaData(getDataSourceURLs(dataSources), shardingRule, 
                DatabaseType.MySQL, executeEngine, new ProxyTableMetaDataConnectionManager(backendDataSource), ProxyContext.getInstance().getMaxConnectionsSizePerQuery());
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
    
    /**
     * Get available data source map.
     *
     * @return available data source map
     */
    public Map<String, DataSourceParameter> getDataSources() {
        if (!getDisabledDataSourceNames().isEmpty()) {
            return getAvailableDataSourceConfigurationMap();
        }
        return dataSources;
    }
    
    private Map<String, DataSourceParameter> getAvailableDataSourceConfigurationMap() {
        Map<String, DataSourceParameter> result = new LinkedHashMap<>(dataSources);
        for (String each : disabledDataSourceNames) {
            result.remove(each);
        }
        return result;
    }
}
