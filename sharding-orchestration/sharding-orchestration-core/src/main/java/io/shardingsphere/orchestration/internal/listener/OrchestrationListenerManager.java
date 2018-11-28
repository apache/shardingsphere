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

package io.shardingsphere.orchestration.internal.listener;

import io.shardingsphere.orchestration.internal.config.listener.ConfigurationOrchestrationListenerManager;
import io.shardingsphere.orchestration.internal.state.listener.StateOrchestrationListenerManager;
import io.shardingsphere.orchestration.reg.api.RegistryCenter;

import java.util.Collection;

/**
 * Orchestration listener manager.
 *
 * @author caohao
 * @author panjuan
 */
public final class OrchestrationListenerManager {
    
    private final ConfigurationOrchestrationListenerManager configOrchestrationListenerManager;
    
    private final StateOrchestrationListenerManager stateOrchestrationListenerManager;
    
    public OrchestrationListenerManager(final String name, final RegistryCenter regCenter, final Collection<String> shardingSchemaNames) {
        configOrchestrationListenerManager = new ConfigurationOrchestrationListenerManager(name, regCenter, shardingSchemaNames);
        stateOrchestrationListenerManager = new StateOrchestrationListenerManager(name, regCenter);
    }
    
    /**
     * Initialize all orchestration listeners.
     */
    public void initListeners() {
        configOrchestrationListenerManager.initListeners();
        stateOrchestrationListenerManager.initListeners();
    }
}
