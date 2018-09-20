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

package io.shardingsphere.jdbc.orchestration.internal.state.datasource;

import io.shardingsphere.core.api.config.MasterSlaveRuleConfiguration;
import io.shardingsphere.core.yaml.YamlRuleConfiguration;
import io.shardingsphere.core.api.config.ShardingRuleConfiguration;
import io.shardingsphere.core.rule.DataSourceParameter;
import io.shardingsphere.jdbc.orchestration.internal.config.ConfigurationService;
import io.shardingsphere.jdbc.orchestration.internal.state.StateNode;
import io.shardingsphere.jdbc.orchestration.internal.state.StateNodeStatus;
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
     * @return available data sources
     */
    public Map<String, DataSource> getAvailableDataSources() {
        Map<String, DataSource> result = configService.loadDataSourceMap();
        Collection<String> disabledDataSourceNames = getDisabledDataSourceNames();
        for (String each : disabledDataSourceNames) {
            result.remove(each);
        }
        return result;
    }
    
    /**
     * Get proxy available data source parameters.
     *
     * @return available data source parameters
     */
    public Map<String, Map<String, DataSourceParameter>> getProxyAvailableDataSourceParameters() {
        Map<String, Map<String, DataSourceParameter>> schemaDatasources = configService.loadProxyDataSources();
        Map<String, Collection<String>> disabledDataSourceNames = getProxyDisabledDataSourceNames();
        for (Entry<String, Collection<String>> each : disabledDataSourceNames.entrySet()) {
            for (String disabledDataSourceName : each.getValue()) {
                schemaDatasources.get(each.getKey()).remove(disabledDataSourceName);
            }
            
        }
        return schemaDatasources;
    }
    
    /**
     * Get available sharding rule configuration.
     *
     * @return available sharding rule configuration
     */
    public ShardingRuleConfiguration getAvailableShardingRuleConfiguration() {
        ShardingRuleConfiguration result = configService.loadShardingRuleConfiguration();
        Collection<String> disabledDataSourceNames = getDisabledDataSourceNames();
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
     * @return available master-slave rule configuration
     */
    public MasterSlaveRuleConfiguration getAvailableMasterSlaveRuleConfiguration() {
        MasterSlaveRuleConfiguration result = configService.loadMasterSlaveRuleConfiguration();
        Collection<String> disabledDataSourceNames = getDisabledDataSourceNames();
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
        Map<String, YamlRuleConfiguration> schemaRuleMap = configService.loadProxyConfiguration();
        Map<String, Collection<String>> disabledDataSourceNames = getProxyDisabledDataSourceNames();
        for (Entry<String, Collection<String>> each : disabledDataSourceNames.entrySet()) {
            for (String disabledDataSourceName : each.getValue()) {
                if (null != schemaRuleMap.get(each.getKey()).getMasterSlaveRule()) {
                    schemaRuleMap.get(each.getKey()).getMasterSlaveRule().getSlaveDataSourceNames().remove(disabledDataSourceName);
                }
            }
        }
        return schemaRuleMap;
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
     * Get proxy disabled data source names.
     *
     * @return disabled data source names
     */
    public Map<String, Collection<String>> getProxyDisabledDataSourceNames() {
        Map<String, Collection<String>> result = new LinkedHashMap<>();
        String dataSourcesNodePath = stateNode.getDataSourcesNodeFullPath();
        List<String> schemaDataSources = regCenter.getChildrenKeys(dataSourcesNodePath);
        for (String each : schemaDataSources) {
            if (StateNodeStatus.DISABLED.toString().equalsIgnoreCase(regCenter.get(dataSourcesNodePath + "/" + each))) {
                int pos = each.indexOf(".");
                String schema = each.substring(0, pos);
                String datasource = each.substring(pos + 1);
                if (!result.containsKey(schema)) {
                    result.put(schema, new LinkedList<String>());
                }
                result.get(schema).add(datasource);
            }
        }
        return result;
    }
    
}
