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

package io.shardingjdbc.orchestration.internal.state.instance;

import io.shardingjdbc.orchestration.api.config.OrchestrationConfiguration;
import io.shardingjdbc.orchestration.internal.state.StateNode;
import io.shardingjdbc.orchestration.reg.base.CoordinatorRegistryCenter;

/**
 * Instance state service.
 * 
 * @author caohao
 */
public final class InstanceStateService {
    
    private final StateNode stateNode;
    
    private final CoordinatorRegistryCenter regCenter;
    
    public InstanceStateService(final OrchestrationConfiguration config) {
        stateNode = new StateNode(config.getName());
        regCenter = config.getRegistryCenter();
    }
    
    /**
     * Persist sharding instance online.
     */
    public void persistShardingInstanceOnline() {
        String instanceNodePath = stateNode.getInstancesNodeFullPath(new OrchestrationInstance().getInstanceId());
        regCenter.persistEphemeral(instanceNodePath, "");
        regCenter.addCacheData(instanceNodePath);
    }
    
    /**
     * Persist master-salve instance online.
     */
    public void persistMasterSlaveInstanceOnline() {
        String instanceNodePath = stateNode.getInstancesNodeFullPath(new OrchestrationInstance().getInstanceId());
        regCenter.persistEphemeral(instanceNodePath, "");
        regCenter.addCacheData(instanceNodePath);
    }
}
