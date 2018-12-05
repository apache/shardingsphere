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

import io.shardingsphere.orchestration.internal.registry.state.instance.OrchestrationInstance;
import io.shardingsphere.orchestration.internal.registry.state.node.StateNode;
import io.shardingsphere.orchestration.reg.api.RegistryCenter;

/**
 * Instance state service.
 * 
 * @author caohao
 * @author panjuan
 */
public final class StateService {
    
    private final StateNode stateNode;
    
    private final RegistryCenter regCenter;
    
    private final OrchestrationInstance instance;
    
    public StateService(final String name, final RegistryCenter regCenter) {
        stateNode = new StateNode(name);
        this.regCenter = regCenter;
        instance = OrchestrationInstance.getInstance();
    }
    
    /**
     * Persist instance online.
     */
    public void persistInstanceOnline() {
        regCenter.persistEphemeral(stateNode.getInstancesNodeFullPath(instance.getInstanceId()), "");
    }
    
    /**
     * Initialize data sources node.
     */
    public void persistDataSourcesNode() {
        regCenter.persist(stateNode.getDataSourcesNodeFullRootPath(), "");
    }
}
