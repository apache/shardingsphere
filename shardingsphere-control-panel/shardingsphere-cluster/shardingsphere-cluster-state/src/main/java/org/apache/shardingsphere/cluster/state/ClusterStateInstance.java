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
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.apache.shardingsphere.orchestration.core.facade.ShardingOrchestrationFacade;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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
     * Persist instance state.
     *
     * @param instanceState instance state
     */
    public void persistInstanceState(final InstanceState instanceState) {
        Preconditions.checkNotNull(instanceState, "instance state can not be null.");
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
     * Load instance state by instance id.
     *
     * @param instanceId instance id
     * @return instance state
     */
    private InstanceState loadInstanceState(final String instanceId) {
        String instanceData = ShardingOrchestrationFacade.getInstance().getRegistryCenter().loadInstanceData(instanceId);
        Preconditions.checkState(!Strings.isNullOrEmpty(instanceData), "Can not load instance state of '%s' from registry center", instanceId);
        return YamlEngine.unmarshal(instanceData, InstanceState.class);
    }
    
    /**
     * Load all instance states.
     *
     * @return all instance states
     */
    public Map<String, InstanceState> loadAllInstanceStates() {
        Collection<String> instances = ShardingOrchestrationFacade.getInstance().getRegistryCenter().loadAllInstances();
        Map<String, InstanceState> instanceStateMap = new HashMap<>();
        instances.forEach(each -> {
            instanceStateMap.put(each, loadInstanceState(each));
        });
        return instanceStateMap;
    }
    
    private static class ClusterStateInstanceHolder {
        
        private static final ClusterStateInstance INSTANCE = new ClusterStateInstance();
    }
}
