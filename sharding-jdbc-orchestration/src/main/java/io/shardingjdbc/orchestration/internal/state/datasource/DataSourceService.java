/*
 * Copyright 1999-2015 dangdang.com.
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

package io.shardingjdbc.orchestration.internal.state.datasource;

import io.shardingjdbc.core.api.config.MasterSlaveRuleConfiguration;
import io.shardingjdbc.core.api.config.ShardingRuleConfiguration;
import io.shardingjdbc.orchestration.internal.config.ConfigurationService;
import io.shardingjdbc.orchestration.internal.state.StateNode;
import io.shardingjdbc.orchestration.internal.state.StateNodeStatus;
import io.shardingjdbc.orchestration.reg.api.RegistryCenter;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

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
    
    private Collection<String> getDisabledDataSourceNames() {
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
}
