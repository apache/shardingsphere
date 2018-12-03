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

package io.shardingsphere.orchestration.internal.registry.state.service;

import io.shardingsphere.orchestration.internal.registry.config.service.ConfigurationService;
import io.shardingsphere.orchestration.internal.registry.state.node.StateNode;
import io.shardingsphere.orchestration.internal.registry.state.node.StateNodeStatus;
import io.shardingsphere.orchestration.internal.registry.state.schema.OrchestrationShardingSchema;
import io.shardingsphere.orchestration.internal.registry.state.schema.OrchestrationShardingSchemaGroup;
import io.shardingsphere.orchestration.reg.api.RegistryCenter;

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
     * Initialize data sources node.
     */
    public void initDataSourcesNode() {
        regCenter.persist(stateNode.getDataSourcesNodeFullRootPath(), "");
    }
    
    /**
     * Get disabled slave orchestration sharding schema group.
     *
     * @return disabled slave orchestration sharding schema group
     */
    public OrchestrationShardingSchemaGroup getDisabledSlaveSchemaGroup() {
        OrchestrationShardingSchemaGroup result = new OrchestrationShardingSchemaGroup();
        OrchestrationShardingSchemaGroup slaveGroup = configService.getAllSlaveDataSourceNames();
        for (String each : regCenter.getChildrenKeys(stateNode.getDataSourcesNodeFullRootPath())) {
            if (StateNodeStatus.DISABLED.toString().equalsIgnoreCase(regCenter.get(stateNode.getDataSourcesNodeFullPath(each)))) {
                OrchestrationShardingSchema orchestrationShardingSchema = new OrchestrationShardingSchema(each);
                if (slaveGroup.getDataSourceNames(orchestrationShardingSchema.getSchemaName()).contains(orchestrationShardingSchema.getDataSourceName())) {
                    result.add(orchestrationShardingSchema);
                }
            }
        }
        return result;
    }
    
    /**
     * Get disabled slave sharding schema.
     * @param dataSourceNodeFullPath data source node full path
     * @return optional of OrchestrationShardingSchema
     */
    public OrchestrationShardingSchema getDisabledSlaveShardingSchema(final String dataSourceNodeFullPath) {
        String schemaDataSource = dataSourceNodeFullPath.replace(stateNode.getDataSourcesNodeFullRootPath() + '/', "");
        OrchestrationShardingSchema result = new OrchestrationShardingSchema(schemaDataSource);
        OrchestrationShardingSchemaGroup slaveGroup = configService.getAllSlaveDataSourceNames();
        return slaveGroup.getDataSourceNames(result.getSchemaName()).contains(result.getDataSourceName()) ? result : new OrchestrationShardingSchema(result.getSchemaName(), "");
    }
}
