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

import io.shardingsphere.orchestration.internal.config.ConfigMapListenerManager;
import io.shardingsphere.orchestration.internal.config.ConfigurationListenerManager;
import io.shardingsphere.orchestration.internal.state.datasource.DataSourceListenerManager;
import io.shardingsphere.orchestration.internal.state.instance.InstanceListenerManager;
import io.shardingsphere.orchestration.reg.api.RegistryCenter;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Registry center's listener factory.
 *
 * @author caohao
 * @author panjuan
 */
public final class ListenerFactory {
    
    private final Collection<ConfigurationListenerManager> configurationListenerManagers = new LinkedList<>();
    
    private final InstanceListenerManager instanceListenerManager;
    
    private final ConfigMapListenerManager configMapListenerManager;
    
    private final DataSourceListenerManager dataSourceListenerManager;
    
    public ListenerFactory(final String name, final RegistryCenter regCenter, final Collection<String> shardingSchemaNames) {
        for (String each : shardingSchemaNames) {
            configurationListenerManagers.add(new ConfigurationListenerManager(name, regCenter, each));
        }
        instanceListenerManager = new InstanceListenerManager(name, regCenter);
        configMapListenerManager = new ConfigMapListenerManager(name, regCenter);
        dataSourceListenerManager = new DataSourceListenerManager(name, regCenter);
    }
    
    /**
     * Initialize listeners.
     *
     */
    public void initListeners() {
        for (ConfigurationListenerManager each : configurationListenerManagers) {
            each.watch();
        }
        instanceListenerManager.watch();
        dataSourceListenerManager.watch();
        configMapListenerManager.watch();
    }
}
