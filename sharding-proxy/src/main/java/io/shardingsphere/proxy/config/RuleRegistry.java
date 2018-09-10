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

import io.shardingsphere.core.api.config.ProxySchemaRule;
import io.shardingsphere.core.api.config.ShardingRuleConfiguration;
import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.constant.properties.ShardingProperties;
import io.shardingsphere.core.constant.properties.ShardingPropertiesConstant;
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
import java.util.Properties;

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
    
    private ShardingRule shardingRule;
    
    private MasterSlaveRule masterSlaveRule;
    
    private JDBCBackendDataSource backendDataSource;
    
    private Map<String, DataSourceParameter> dataSourceConfigurationMap;
    
    private int maxConnectionsSizePerQuery;
    
    private BackendNIOConfiguration backendNIOConfig;
    
    private ShardingMetaData metaData;
    
    private String schemaName;
    
    @Setter
    private Collection<String> disabledDataSourceNames = new LinkedList<>();
    
    public RuleRegistry(final Map<String, DataSourceParameter> dataSources, final ProxySchemaRule rule, final String schemaName) {
        init(dataSources, rule, schemaName);
    }
    
    /**
     * Initialize rule registry.
     *
     * @param dataSources data sources
     * @param rule        proxy rule configuration
     * @param schemaName  schema name
     */
    public synchronized void init(final Map<String, DataSourceParameter> dataSources, final ProxySchemaRule rule, final String schemaName) {
        Properties properties = null == rule.getShardingRule() ? rule.getMasterSlaveRule().getProps() : rule.getShardingRule().getProps();
        ShardingProperties shardingProperties = new ShardingProperties(null == properties ? new Properties() : properties);
        maxConnectionsSizePerQuery = shardingProperties.getValue(ShardingPropertiesConstant.MAX_CONNECTIONS_SIZE_PER_QUERY);
        int databaseConnectionCount = shardingProperties.getValue(ShardingPropertiesConstant.PROXY_BACKEND_MAX_CONNECTIONS);
        int connectionTimeoutSeconds = shardingProperties.getValue(ShardingPropertiesConstant.PROXY_BACKEND_CONNECTION_TIMEOUT_SECONDS);
        backendNIOConfig = new BackendNIOConfiguration(databaseConnectionCount, connectionTimeoutSeconds);
        shardingRule = new ShardingRule(
                null == rule.getShardingRule() ? new ShardingRuleConfiguration() : rule.getShardingRule().getShardingRuleConfiguration(), dataSources.keySet());
        if (null != rule.getMasterSlaveRule()) {
            masterSlaveRule = new MasterSlaveRule(rule.getMasterSlaveRule().getMasterSlaveRuleConfiguration());
        }
        // TODO :jiaqi only use JDBC need connect db via JDBC, netty style should use SQL packet to get metadata
        dataSourceConfigurationMap = dataSources;
        backendDataSource = new JDBCBackendDataSource(this);
        this.schemaName = schemaName;
    }
    
    /**
     * Initialize sharding meta data.
     *
     * @param executeEngine sharding execute engine
     */
    public void initShardingMetaData(final ShardingExecuteEngine executeEngine) {
        metaData = new ShardingMetaData(getDataSourceURLs(dataSourceConfigurationMap), shardingRule, 
                DatabaseType.MySQL, executeEngine, new ProxyTableMetaDataConnectionManager(backendDataSource), maxConnectionsSizePerQuery);
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
     * Get available data source map.
     *
     * @return available data source map
     */
    public Map<String, DataSourceParameter> getDataSourceConfigurationMap() {
        if (!getDisabledDataSourceNames().isEmpty()) {
            return getAvailableDataSourceConfigurationMap();
        }
        return dataSourceConfigurationMap;
    }
    
    private Map<String, DataSourceParameter> getAvailableDataSourceConfigurationMap() {
        Map<String, DataSourceParameter> result = new LinkedHashMap<>(dataSourceConfigurationMap);
        for (String each : disabledDataSourceNames) {
            result.remove(each);
        }
        return result;
    }
}
