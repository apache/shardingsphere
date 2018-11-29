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

package io.shardingsphere.orchestration.internal.state.service;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import io.shardingsphere.api.config.MasterSlaveRuleConfiguration;
import io.shardingsphere.api.config.ShardingRuleConfiguration;
import io.shardingsphere.core.config.DataSourceConfiguration;
import io.shardingsphere.orchestration.internal.config.service.ConfigurationService;
import io.shardingsphere.orchestration.internal.state.node.StateNode;
import io.shardingsphere.orchestration.internal.state.node.StateNodeStatus;
import io.shardingsphere.orchestration.internal.state.schema.OrchestrationSchema;
import io.shardingsphere.orchestration.internal.state.schema.OrchestrationSchemaGroup;
import io.shardingsphere.orchestration.reg.api.RegistryCenter;

import java.util.Collection;
import java.util.Map;

/**
 * Data source service.
 *
 * @author caohao
 * @author zhangliang
 * @author panjuan
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
        regCenter.persist(stateNode.getDataSourcesNodeFullRootPath(), "");
    }
    
    /**
     * Get available data source configurations.
     *
     * @param shardingSchemaName sharding schema name
     * @return available data sources
     */
    public Map<String, DataSourceConfiguration> getAvailableDataSourceConfigurations(final String shardingSchemaName) {
        Map<String, DataSourceConfiguration> result = configService.loadDataSourceConfigurations(shardingSchemaName);
        if (!getDisabledSlaveDataSourceNames().containsKey(shardingSchemaName)) {
            return result;
        }
        for (String each : getDisabledSlaveDataSourceNames().get(shardingSchemaName)) {
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
        Preconditions.checkState(null != result && !result.getTableRuleConfigs().isEmpty(), "Missing the sharding rule configuration on registry center.");
        if (!getDisabledSlaveDataSourceNames().containsKey(shardingSchemaName)) {
            return result;
        }
        for (String each : getDisabledSlaveDataSourceNames().get(shardingSchemaName)) {
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
        if (!getDisabledSlaveDataSourceNames().containsKey(shardingSchemaName)) {
            return result;
        }
        for (String each : getDisabledSlaveDataSourceNames().get(shardingSchemaName)) {
            result.getSlaveDataSourceNames().remove(each);
        }
        return result;
    }
    
    /**
     * Get disabled slave data source names.
     *
     * @return disabled slave data source names
     */
    public Map<String, Collection<String>> getDisabledSlaveDataSourceNames() {
        Map<String, Collection<String>> result = getDisableOrchestrationSchemaGroup().getSchemaGroup();
        Map<String, Collection<String>> slaveDataSourceNamesMap = configService.getAllSlaveDataSourceNames();
        for (String each : result.keySet()) {
            result.get(each).retainAll(slaveDataSourceNamesMap.get(each));
        }
        return result;
    }
    
    private OrchestrationSchemaGroup getDisableOrchestrationSchemaGroup() {
        OrchestrationSchemaGroup result = new OrchestrationSchemaGroup();
        for (String each : regCenter.getChildrenKeys(stateNode.getDataSourcesNodeFullRootPath())) {
            if (StateNodeStatus.DISABLED.toString().equalsIgnoreCase(regCenter.get(stateNode.getDataSourcesNodeFullPath(each)))) {
                result.add(new OrchestrationSchema(each));
            }
        }
        return result;
    }
}
