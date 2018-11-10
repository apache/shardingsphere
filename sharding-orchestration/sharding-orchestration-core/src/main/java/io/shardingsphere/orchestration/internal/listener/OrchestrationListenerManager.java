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

import io.shardingsphere.orchestration.internal.config.listener.AuthenticationOrchestrationListener;
import io.shardingsphere.orchestration.internal.config.listener.ConfigMapOrchestrationListener;
import io.shardingsphere.orchestration.internal.config.listener.RuleOrchestrationListener;
import io.shardingsphere.orchestration.internal.config.listener.DataSourceOrchestrationListener;
import io.shardingsphere.orchestration.internal.config.listener.PropertiesOrchestrationListener;
import io.shardingsphere.orchestration.internal.state.listener.DataSourceStateOrchestrationListener;
import io.shardingsphere.orchestration.internal.state.listener.InstanceStateOrchestrationListener;
import io.shardingsphere.orchestration.reg.api.RegistryCenter;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Registry center's listener manager.
 *
 * @author caohao
 * @author panjuan
 */
public final class OrchestrationListenerManager {
    
    private final Collection<RuleOrchestrationListener> ruleListenerManagers = new LinkedList<>();
    
    private final Collection<DataSourceOrchestrationListener> dataSourceListenerManagers = new LinkedList<>();
    
    private final PropertiesOrchestrationListener propertiesListenerManager;
    
    private final AuthenticationOrchestrationListener authenticationListenerManager;
    
    private final ConfigMapOrchestrationListener configMapListenerManager;
    
    private final InstanceStateOrchestrationListener instanceStateListenerManager;
    
    private final DataSourceStateOrchestrationListener dataSourceStateListenerManager;
    
    public OrchestrationListenerManager(final String name, final RegistryCenter regCenter, final Collection<String> shardingSchemaNames) {
        for (String each : shardingSchemaNames) {
            dataSourceListenerManagers.add(new DataSourceOrchestrationListener(name, regCenter, each));
            ruleListenerManagers.add(new RuleOrchestrationListener(name, regCenter, each));
        }
        propertiesListenerManager = new PropertiesOrchestrationListener(name, regCenter);
        authenticationListenerManager = new AuthenticationOrchestrationListener(name, regCenter);
        instanceStateListenerManager = new InstanceStateOrchestrationListener(name, regCenter);
        configMapListenerManager = new ConfigMapOrchestrationListener(name, regCenter);
        dataSourceStateListenerManager = new DataSourceStateOrchestrationListener(name, regCenter);
    }
    
    /**
     * Initialize listeners.
     *
     */
    public void initListeners() {
        initRuleListenerManagers();
        initDataSourceListenerManagers();
        propertiesListenerManager.watch();
        authenticationListenerManager.watch();
        instanceStateListenerManager.watch();
        dataSourceStateListenerManager.watch();
        configMapListenerManager.watch();
    }
    
    private void initDataSourceListenerManagers() {
        for (DataSourceOrchestrationListener each : dataSourceListenerManagers) {
            each.watch();
        }
    }
    
    private void initRuleListenerManagers() {
        for (RuleOrchestrationListener each : ruleListenerManagers) {
            each.watch();
        }
    }
}
