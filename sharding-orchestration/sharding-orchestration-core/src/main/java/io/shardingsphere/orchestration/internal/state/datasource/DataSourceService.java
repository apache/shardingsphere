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

package io.shardingsphere.orchestration.internal.state.datasource;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import io.shardingsphere.api.config.MasterSlaveRuleConfiguration;
import io.shardingsphere.api.config.ShardingRuleConfiguration;
import io.shardingsphere.core.constant.ShardingConstant;
import io.shardingsphere.core.rule.DataSourceParameter;
import io.shardingsphere.core.yaml.YamlRuleConfiguration;
import io.shardingsphere.orchestration.internal.config.ConfigurationService;
import io.shardingsphere.orchestration.internal.state.StateNode;
import io.shardingsphere.orchestration.internal.state.StateNodeStatus;
import io.shardingsphere.orchestration.reg.api.RegistryCenter;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Data source service.
 *
 * @author caohao
 * @author zhangliang
 */
public final class DataSourceService {
    
    private final StateNode stateNode;
    
    private final RegistryCenter regCenter;
    
    private final ConfigurationService configService;
    
    public DataSourceService(final String name, final RegistryCenter regCenter) {
        stateNode = new StateNode(name);
        this.regCenter = regCenter;
        configService = new ConfigurationService(name, regCenter);
    }
    
    /**
     * Persist master-salve data sources node.
     */
    public void persistDataSourcesNode() {
        regCenter.persist(stateNode.getDataSourcesNodeFullPath(), "");
    }
    
    /**
     * Get available data sources.
     *
     * @param shardingSchemaName sharding schema name
     * @return available data sources
     */
    public Map<String, DataSource> getAvailableDataSources(final String shardingSchemaName) {
        Map<String, DataSource> result = configService.loadDataSources(shardingSchemaName);
        Collection<String> disabledDataSourceNames = getProxyDisabledDataSourceNames().get(shardingSchemaName);
        if (null == disabledDataSourceNames) {
            return result;
        }
        for (String each : disabledDataSourceNames) {
            result.remove(each);
        }
        return result;
    }
    
    /**
     * Get available data source parameters.
     *
     * @param shardingSchemaName sharding schema name
     * @return available data source parameters
     */
    public Map<String, DataSourceParameter> getAvailableDataSourceParameters(final String shardingSchemaName) {
        Map<String, DataSourceParameter> result = configService.loadDataSourceParameters(shardingSchemaName);
        Collection<String> disabledDataSourceNames = getProxyDisabledDataSourceNames().get(shardingSchemaName);
        if (null == disabledDataSourceNames) {
            return result;
        }
        for (String each : disabledDataSourceNames) {
            result.remove(each);
        }
        return result;
    }
    
    /**
     * Get available sharding rule configuration.
     *
     * @param shardingSchemaName sharding schema name
     * @return available sharding rule configuration
     */
    public ShardingRuleConfiguration getAvailableShardingRuleConfiguration(final String shardingSchemaName) {
        ShardingRuleConfiguration result = configService.loadShardingRuleConfiguration(shardingSchemaName);
        Preconditions.checkState(null != result && !result.getTableRuleConfigs().isEmpty(), "Missing the sharding rule configuration on register center");
        Collection<String> disabledDataSourceNames = getProxyDisabledDataSourceNames().get(shardingSchemaName);
        if (null == disabledDataSourceNames) {
            return result;
        }
        for (String each : disabledDataSourceNames) {
            for (MasterSlaveRuleConfiguration masterSlaveRuleConfig : result.getMasterSlaveRuleConfigs()) {
                masterSlaveRuleConfig.getSlaveDataSourceNames().remove(each);
            }
        }
        return result;
    }
    
    /**
     * Get available master-slave rule configuration.
     *
     * @param shardingSchemaName sharding schema name
     * @return available master-slave rule configuration
     */
    public MasterSlaveRuleConfiguration getAvailableMasterSlaveRuleConfiguration(final String shardingSchemaName) {
        MasterSlaveRuleConfiguration result = configService.loadMasterSlaveRuleConfiguration(shardingSchemaName);
        Preconditions.checkState(null != result && !Strings.isNullOrEmpty(result.getMasterDataSourceName()), "No available master slave rule configuration to load.");
        Collection<String> disabledDataSourceNames = getProxyDisabledDataSourceNames().get(shardingSchemaName);
        if (null == disabledDataSourceNames) {
            return result;
        }
        for (String each : disabledDataSourceNames) {
            result.getSlaveDataSourceNames().remove(each);
        }
        return result;
    }
    
    /**
     * Get available proxy rule configuration.
     *
     * @return available yaml proxy configuration
     */
    public Map<String, YamlRuleConfiguration> getAvailableYamlProxyConfiguration() {
        Map<String, YamlRuleConfiguration> result = new LinkedHashMap<>();
        for (String each : configService.getAllShardingSchemaNames()) {
            YamlRuleConfiguration yamlRuleConfig = new YamlRuleConfiguration();
            if (configService.isShardingRule(each)) {
                yamlRuleConfig.setShardingRule(configService.loadShardingRuleConfiguration(each));
            } else {
                yamlRuleConfig.setMasterSlaveRule(configService.loadMasterSlaveRuleConfiguration(each));
            }
        }
        Map<String, Collection<String>> disabledDataSourceNames = getProxyDisabledDataSourceNames();
        for (Entry<String, Collection<String>> each : disabledDataSourceNames.entrySet()) {
            for (String disabledDataSourceName : each.getValue()) {
                if (null != result.get(each.getKey()).getMasterSlaveRule()) {
                    result.get(each.getKey()).getMasterSlaveRule().getSlaveDataSourceNames().remove(disabledDataSourceName);
                }
            }
        }
        return result;
    }
    
    /**
     * Get disabled data source names.
     *
     * @return disabled data source names
     */
    public Collection<String> getDisabledDataSourceNames() {
        Collection<String> result = new HashSet<>();
        String dataSourcesNodePath = stateNode.getDataSourcesNodeFullPath();
        List<String> dataSources = regCenter.getChildrenKeys(dataSourcesNodePath);
        for (String each : dataSources) {
            if (StateNodeStatus.DISABLED.toString().equalsIgnoreCase(regCenter.get(dataSourcesNodePath + "/" + each))) {
                result.add(each);
            }
        }
        return result;
    }
    
    /**
     * Get disabled data source names.
     *
     * @return disabled data source names
     */
    public Map<String, Collection<String>> getProxyDisabledDataSourceNames() {
        Map<String, Collection<String>> result = new LinkedHashMap<>();
        String dataSourcesNodePath = stateNode.getDataSourcesNodeFullPath();
        Collection<String> schemaDataSources = regCenter.getChildrenKeys(dataSourcesNodePath);
        for (String each : schemaDataSources) {
            if (!StateNodeStatus.DISABLED.toString().equalsIgnoreCase(regCenter.get(dataSourcesNodePath + "/" + each))) {
                continue;
            }
            String schemaName;
            String dataSourceName;
            if (each.contains(".")) {
                int position = each.indexOf(".");
                schemaName = each.substring(0, position);
                dataSourceName = each.substring(position + 1);
            } else {
                schemaName = ShardingConstant.LOGIC_SCHEMA_NAME;
                dataSourceName = each;
            }
            if (!result.containsKey(schemaName)) {
                result.put(schemaName, new LinkedList<String>());
            }
            result.get(schemaName).add(dataSourceName);
        }
        return result;
    }
}
