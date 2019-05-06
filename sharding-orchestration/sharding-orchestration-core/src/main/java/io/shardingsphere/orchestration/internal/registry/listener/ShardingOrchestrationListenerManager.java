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

package io.shardingsphere.orchestration.internal.registry.listener;

import io.shardingsphere.orchestration.internal.registry.config.listener.ConfigurationChangedListenerManager;
import io.shardingsphere.orchestration.internal.registry.state.listener.StateChangedListenerManager;
import io.shardingsphere.orchestration.reg.api.RegistryCenter;

import java.util.Collection;

/**
 * Sharding orchestration listener manager.
 *
 * @author caohao
 * @author panjuan
 */
public final class ShardingOrchestrationListenerManager {
    
    private final ConfigurationChangedListenerManager configurationChangedListenerManager;
    
    private final StateChangedListenerManager stateChangedListenerManager;
    
    public ShardingOrchestrationListenerManager(final String name, final RegistryCenter regCenter, final Collection<String> shardingSchemaNames) {
        configurationChangedListenerManager = new ConfigurationChangedListenerManager(name, regCenter, shardingSchemaNames);
        stateChangedListenerManager = new StateChangedListenerManager(name, regCenter);
    }
    
    /**
     * Initialize all orchestration listeners.
     */
    public void initListeners() {
        configurationChangedListenerManager.initListeners();
        stateChangedListenerManager.initListeners();
    }
}
