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
import com.google.common.base.Preconditions;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.cluster.configuration.config.ClusterConfiguration;
import org.apache.shardingsphere.cluster.heartbeat.ClusterHeartBeatInstance;
import org.apache.shardingsphere.cluster.heartbeat.response.HeartBeatResponse;
import org.apache.shardingsphere.cluster.heartbeat.response.HeartBeatResult;
import org.apache.shardingsphere.cluster.state.ClusterStateInstance;
import org.apache.shardingsphere.cluster.state.DataSourceState;
import org.apache.shardingsphere.cluster.state.InstanceState;
import org.apache.shardingsphere.cluster.state.enums.NodeState;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Cluster facade.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ClusterFacade {
    
    private ClusterHeartBeatInstance clusterHeartBeatInstance;
    
    private ClusterStateInstance clusterStateInstance;
    
    /**
     * Init cluster facade.
     *
     * @param clusterConfiguration cluster configuration
     */
    public void init(final ClusterConfiguration clusterConfiguration) {
        Preconditions.checkNotNull(clusterConfiguration, "cluster configuration can not be null.");
        clusterHeartBeatInstance = ClusterHeartBeatInstance.getInstance();
        clusterHeartBeatInstance.init(clusterConfiguration.getHeartBeat());
        clusterStateInstance = ClusterStateInstance.getInstance();
    }
    
    /**
     * Report heart beat.
     *
     * @param heartBeatResponse heart beat response
     */
    public void reportHeartBeat(final HeartBeatResponse heartBeatResponse) {
        clusterStateInstance.persistInstanceState(buildInstanceState(heartBeatResponse));
    }
    
    private InstanceState buildInstanceState(final HeartBeatResponse heartBeatResponse) {
        InstanceState instanceState = clusterStateInstance.loadInstanceState();
        return new InstanceState(instanceState.getState(), buildDataSourceStateMap(instanceState, heartBeatResponse));
    }
    
    private Map<String, DataSourceState> buildDataSourceStateMap(final InstanceState instanceState, final HeartBeatResponse heartBeatResponse) {
        Map<String, DataSourceState> dataSourceStateMap = new HashMap<>();
        heartBeatResponse.getHeartBeatResultMap().forEach((key, value) -> buildDataSourceState(key, value, dataSourceStateMap, instanceState));
        return dataSourceStateMap;
    }
    
    private void buildDataSourceState(final String schemaName, final Collection<HeartBeatResult> heartBeatResults,
                       final Map<String, DataSourceState> dataSourceStateMap, final InstanceState instanceState) {
        heartBeatResults.stream().forEach(each -> {
            String dataSourceName = Joiner.on(".").join(schemaName, each.getDataSourceName());
            DataSourceState dataSourceState = null == instanceState.getDataSources()
                    || null == instanceState.getDataSources().get(dataSourceName) ? new DataSourceState()
                    : instanceState.getDataSources().get(dataSourceName);
            if (each.getEnable()) {
                dataSourceState.setState(NodeState.ONLINE);
                dataSourceState.setLastConnect(each.getDetectTimeStamp());
            } else {
                dataSourceState.setState(NodeState.OFFLINE);
            }
            dataSourceStateMap.put(dataSourceName, dataSourceState);
        });
    }
    
    /**
     * Get instance.
     *
     * @return cluster facade instance
     */
    public static ClusterFacade getInstance() {
        return ClusterFacadeHolder.INSTANCE;
    }
    
    private static final class ClusterFacadeHolder {
        
        private static final ClusterFacade INSTANCE = new ClusterFacade();
    }
}
