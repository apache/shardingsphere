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

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.eventbus.Subscribe;
import org.apache.shardingsphere.cluster.state.enums.NodeState;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.apache.shardingsphere.orchestration.core.common.eventbus.OrchestrationEventBus;
import org.apache.shardingsphere.orchestration.core.facade.OrchestrationFacade;
import org.apache.shardingsphere.orchestration.core.registry.RegistryCenter;
import org.apache.shardingsphere.orchestration.core.registry.event.DisabledStateChangedEvent;

/**
 * Cluster state instance.
 */
public final class ClusterStateInstance {
    
    private RegistryCenter registryCenter = OrchestrationFacade.getInstance().getRegistryCenter();
    
    private ClusterStateInstance() {
        OrchestrationEventBus.getInstance().register(this);
    }
    
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
        registryCenter.persistInstanceData(YamlEngine.marshal(instanceState));
    }
    
    /**
     * Load instance state.
     *
     * @return instance state
     */
    public InstanceState loadInstanceState() {
        String instanceData = registryCenter.loadInstanceData();
        Preconditions.checkState(!Strings.isNullOrEmpty(instanceData), "Can not load instance state from registry center");
        return YamlEngine.unmarshal(instanceData, InstanceState.class);
    }
    
    /**
     * Disabled data source after state changed.
     *
     * @param event disabled state changed event
     */
    @Subscribe
    public void dataSourceStateChanged(final DisabledStateChangedEvent event) {
        String dataSourceName = Joiner.on(".").join(event.getOrchestrationSchema().getSchemaName(), event.getOrchestrationSchema().getDataSourceName());
        NodeState state = event.isDisabled() ? NodeState.DISABLED : NodeState.ONLINE;
        InstanceState instanceState = loadInstanceState();
        instanceState.getDataSources().entrySet().stream().filter(entry -> dataSourceName.equals(entry.getKey())).findFirst().get().getValue().setState(state);
        persistInstanceState(instanceState);
    }
    
    private static class ClusterStateInstanceHolder {
        
        private static final ClusterStateInstance INSTANCE = new ClusterStateInstance();
    }
}
