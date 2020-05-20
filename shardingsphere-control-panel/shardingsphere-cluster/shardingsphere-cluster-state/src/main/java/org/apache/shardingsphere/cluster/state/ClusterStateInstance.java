/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.cluster.state;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.cluster.state.enums.NodeState;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.apache.shardingsphere.orchestration.core.facade.ShardingOrchestrationFacade;

import java.util.HashMap;
import java.util.Optional;

/**
 * Cluster state instance.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ClusterStateInstance {
    
    /**
     * Get cluster state instance.
     *
     * @return cluster state instance
     */
    public static ClusterStateInstance getInstance() {
        return ClusterStateInstanceHolder.INSTANCE;
    }
    
    /**
     * Persist data source state.
     *
     * @param dataSourceName data source name
     * @param dataSourceState data source state
     */
    public void persistDataSourceState(final String dataSourceName, final DataSourceState dataSourceState) {
        String instanceData = ShardingOrchestrationFacade.getInstance().getRegistryCenter().loadInstanceData();
        InstanceState instanceState = Optional.ofNullable(YamlEngine.unmarshal(instanceData, InstanceState.class)).orElse(new InstanceState(new HashMap<>()));
        instanceState.getDataSources().put(dataSourceName, dataSourceState);
        ShardingOrchestrationFacade.getInstance().getRegistryCenter().persistInstanceData(YamlEngine.marshal(instanceState));
    }
    
    /**
     * Load instance state.
     *
     * @return instance state
     */
    public InstanceState loadInstanceState() {
        String instanceData = ShardingOrchestrationFacade.getInstance().getRegistryCenter().loadInstanceData();
        Preconditions.checkState(!Strings.isNullOrEmpty(instanceData), "Can not load instance state from registry center");
        return YamlEngine.unmarshal(instanceData, InstanceState.class);
    }
    
    /**
     * Update data source state.
     *
     * @param dataSourceName data source name
     * @param state state
     */
    public void updateState(final String dataSourceName, final NodeState state) {
        InstanceState instanceState = loadInstanceState();
        DataSourceState dataSourceState = loadDataSourceState(dataSourceName, instanceState);
        dataSourceState.setState(state);
        // TODO handle different states
        ShardingOrchestrationFacade.getInstance().getRegistryCenter().persistInstanceData(YamlEngine.marshal(instanceState));
    }
    
    /**
     * Update retry count.
     *
     * @param dataSourceName data source name
     * @param retryCount retry count
     */
    public void updateRetryCount(final String dataSourceName, final Integer retryCount) {
        InstanceState instanceState = loadInstanceState();
        DataSourceState dataSourceState = loadDataSourceState(dataSourceName, instanceState);
        dataSourceState.setRetryCount(retryCount);
        ShardingOrchestrationFacade.getInstance().getRegistryCenter().persistInstanceData(YamlEngine.marshal(instanceState));
    }
    
    /**
     * Load data source state.
     *
     * @param dataSourceName data source name
     * @param instanceState instance state
     * @return data source state
     */
    private DataSourceState loadDataSourceState(final String dataSourceName, final InstanceState instanceState) {
        DataSourceState dataSourceState = instanceState.getDataSources().get(dataSourceName);
        Preconditions.checkNotNull(dataSourceState, "Can not load data source state of %s from registry center", dataSourceName);
        return dataSourceState;
    }
    
    private static class ClusterStateInstanceHolder {
        
        private static final ClusterStateInstance INSTANCE = new ClusterStateInstance();
    }
}
