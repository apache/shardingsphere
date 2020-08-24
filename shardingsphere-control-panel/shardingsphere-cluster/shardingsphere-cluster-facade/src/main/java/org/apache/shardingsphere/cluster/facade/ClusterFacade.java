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

package org.apache.shardingsphere.cluster.facade;

import com.google.common.base.Joiner;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.cluster.configuration.config.ClusterConfiguration;
import org.apache.shardingsphere.cluster.facade.init.ClusterInitFacade;
import org.apache.shardingsphere.cluster.heartbeat.ClusterHeartbeatInstance;
import org.apache.shardingsphere.cluster.heartbeat.response.HeartbeatResponse;
import org.apache.shardingsphere.cluster.heartbeat.response.HeartbeatResult;
import org.apache.shardingsphere.cluster.state.ClusterStateInstance;
import org.apache.shardingsphere.cluster.state.DataSourceState;
import org.apache.shardingsphere.cluster.state.InstanceState;
import org.apache.shardingsphere.cluster.state.enums.NodeState;
import org.apache.shardingsphere.kernel.context.SchemaContext;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Cluster facade.
 */
@Slf4j
public final class ClusterFacade {
    
    private ClusterHeartbeatInstance clusterHeartbeatInstance;
    
    private ClusterStateInstance clusterStateInstance;
    
    /**
     * Init cluster facade.
     *
     * @param clusterConfiguration cluster configuration
     */
    public void init(final ClusterConfiguration clusterConfiguration) {
        clusterHeartbeatInstance = ClusterHeartbeatInstance.getInstance();
        clusterHeartbeatInstance.init(clusterConfiguration.getHeartbeat());
        clusterStateInstance = ClusterStateInstance.getInstance();
    }
    
    /**
     * Close cluster heartbeat instance.
     */
    public void close() {
        clusterHeartbeatInstance.close();
    }
    
    /**
     * Detect heartbeat.
     *
     * @param schemaContexts schema contexts
     */
    public void detectHeartbeat(final Map<String, SchemaContext> schemaContexts) {
        if (ClusterInitFacade.isEnabled()) {
            HeartbeatResponse heartbeatResponse = clusterHeartbeatInstance.detect(schemaContexts);
            reportHeartbeat(heartbeatResponse);
        }
    }
    
    private void reportHeartbeat(final HeartbeatResponse heartBeatResponse) {
        clusterStateInstance.persistInstanceState(buildInstanceState(heartBeatResponse));
    }
    
    /**
     * Get cluster facade instance.
     *
     * @return cluster facade instance
     */
    public static ClusterFacade getInstance() {
        return ClusterFacadeHolder.INSTANCE;
    }
    
    private InstanceState buildInstanceState(final HeartbeatResponse heartbeatResponse) {
        InstanceState instanceState = clusterStateInstance.loadInstanceState();
        return new InstanceState(instanceState.getState(), buildDataSourceStateMap(instanceState, heartbeatResponse));
    }
    
    private Map<String, DataSourceState> buildDataSourceStateMap(final InstanceState instanceState, final HeartbeatResponse heartbeatResponse) {
        Map<String, DataSourceState> result = new HashMap<>();
        heartbeatResponse.getHeartbeatResultMap().forEach((key, value) -> buildDataSourceState(key, value, result, instanceState));
        return result;
    }
    
    private void buildDataSourceState(final String schemaName, final Collection<HeartbeatResult> heartbeatResults,
                                      final Map<String, DataSourceState> dataSourceStateMap, final InstanceState instanceState) {
        heartbeatResults.forEach(each -> {
            String dataSourceName = Joiner.on(".").join(schemaName, each.getDataSourceName());
            DataSourceState dataSourceState = null == instanceState.getDataSources()
                    || null == instanceState.getDataSources().get(dataSourceName) ? new DataSourceState()
                    : instanceState.getDataSources().get(dataSourceName);
            dataSourceState.setState(each.isDisabled() ? NodeState.DISABLED : each.isEnable() ? NodeState.ONLINE : NodeState.OFFLINE);
            dataSourceState.setLastConnect(each.getDetectTimeStamp());
            dataSourceStateMap.put(dataSourceName, dataSourceState);
        });
    }
    
    private static final class ClusterFacadeHolder {
        
        private static final ClusterFacade INSTANCE = new ClusterFacade();
    }
}
