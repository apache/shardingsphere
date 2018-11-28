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

package io.shardingsphere.orchestration.internal.config.listener;

import io.shardingsphere.orchestration.reg.api.RegistryCenter;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Configuration orchestration listener manager.
 *
 * @author zhangliang
 */
public final class ConfigurationOrchestrationListenerManager {
    
    private final Collection<RuleOrchestrationListener> ruleListenerManagers = new LinkedList<>();
    
    private final Collection<DataSourceOrchestrationListener> dataSourceListenerManagers = new LinkedList<>();
    
    private final PropertiesOrchestrationListener propertiesListenerManager;
    
    private final AuthenticationOrchestrationListener authenticationListenerManager;
    
    private final ConfigMapOrchestrationListener configMapListenerManager;
    
    public ConfigurationOrchestrationListenerManager(final String name, final RegistryCenter regCenter, final Collection<String> shardingSchemaNames) {
        for (String each : shardingSchemaNames) {
            ruleListenerManagers.add(new RuleOrchestrationListener(name, regCenter, each));
            dataSourceListenerManagers.add(new DataSourceOrchestrationListener(name, regCenter, each));
        }
        propertiesListenerManager = new PropertiesOrchestrationListener(name, regCenter);
        authenticationListenerManager = new AuthenticationOrchestrationListener(name, regCenter);
        configMapListenerManager = new ConfigMapOrchestrationListener(name, regCenter);
    }
    
    /**
     * Initialize all configuration orchestration listeners.
     */
    public void initListeners() {
        for (RuleOrchestrationListener each : ruleListenerManagers) {
            each.watch();
        }
        for (DataSourceOrchestrationListener each : dataSourceListenerManagers) {
            each.watch();
        }
        propertiesListenerManager.watch();
        authenticationListenerManager.watch();
        configMapListenerManager.watch();
    }
}
