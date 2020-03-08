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

package org.apache.shardingsphere.orchestration.internal.registry;

import org.apache.shardingsphere.underlying.common.config.RuleConfiguration;
import org.apache.shardingsphere.underlying.common.config.DataSourceConfiguration;
import org.apache.shardingsphere.core.rule.Authentication;
import org.apache.shardingsphere.core.rule.ProxyUser;
import org.apache.shardingsphere.orchestration.center.api.RegistryCenterRepository;
import org.apache.shardingsphere.orchestration.center.configuration.InstanceConfiguration;
import org.apache.shardingsphere.orchestration.center.configuration.OrchestrationConfiguration;
import org.apache.shardingsphere.orchestration.internal.registry.config.service.ConfigurationService;
import org.apache.shardingsphere.orchestration.internal.registry.listener.ShardingOrchestrationListenerManager;
import org.apache.shardingsphere.orchestration.internal.registry.state.service.StateService;
import org.apache.shardingsphere.orchestration.util.FieldUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public final class ShardingOrchestrationFacadeTest {
    
    private ShardingOrchestrationFacade shardingOrchestrationFacade;
    
    @Mock
    private RegistryCenterRepository registryCenterRepository;
    
    @Mock
    private ConfigurationService configService;
    
    @Mock
    private StateService stateService;
    
    @Mock
    private ShardingOrchestrationListenerManager listenerManager;
    
    @Before
    public void setUp() {
        Map<String, InstanceConfiguration> instanceConfigurationMap = new HashMap<>();
        InstanceConfiguration instanceConfiguration1 = new InstanceConfiguration("SecondTestRegistryCenter");
        instanceConfiguration1.setOrchestrationType("registry_center");
        instanceConfiguration1.setNamespace("namespace_1");
        instanceConfigurationMap.put("test_name_1", instanceConfiguration1);
        InstanceConfiguration instanceConfiguration2 = new InstanceConfiguration("FirstTestConfigCenter");
        instanceConfiguration2.setOrchestrationType("config_center");
        instanceConfiguration2.setNamespace("namespace_2");
        instanceConfigurationMap.put("test_name_2", instanceConfiguration2);
        OrchestrationConfiguration orchestrationConfiguration = new OrchestrationConfiguration();
        orchestrationConfiguration.setInstanceConfigurationMap(instanceConfigurationMap);
        shardingOrchestrationFacade = new ShardingOrchestrationFacade(orchestrationConfiguration, Arrays.asList("sharding_db", "masterslave_db"));
        FieldUtil.setField(shardingOrchestrationFacade, "registryCenterRepository", registryCenterRepository);
        FieldUtil.setField(shardingOrchestrationFacade, "configService", configService);
        FieldUtil.setField(shardingOrchestrationFacade, "stateService", stateService);
        FieldUtil.setField(shardingOrchestrationFacade, "listenerManager", listenerManager);
    }
    
    @Test
    public void assertInitWithParameters() {
        Map<String, DataSourceConfiguration> dataSourceConfigurationMap = Collections.singletonMap("test_ds", mock(DataSourceConfiguration.class));
        Map<String, RuleConfiguration> ruleConfigurationMap = Collections.singletonMap("sharding_db", mock(RuleConfiguration.class));
        ProxyUser proxyUser = new ProxyUser("root", Collections.singleton("db1"));
        Authentication authentication = new Authentication();
        authentication.getUsers().put("root", proxyUser);
        Properties props = new Properties();
        shardingOrchestrationFacade.init(Collections.singletonMap("sharding_db", dataSourceConfigurationMap), ruleConfigurationMap, authentication, props);
        verify(configService).persistConfiguration("sharding_db", dataSourceConfigurationMap, ruleConfigurationMap.get("sharding_db"), authentication, props, false);
        verify(stateService).persistInstanceOnline();
        verify(stateService).persistDataSourcesNode();
        verify(listenerManager).initListeners();
    }
    
    @Test
    public void assertInitWithoutParameters() {
        shardingOrchestrationFacade.init();
        verify(stateService).persistInstanceOnline();
        verify(stateService).persistDataSourcesNode();
        verify(listenerManager).initListeners();
    }
    
    @Test
    public void assertCloseSuccess() {
        shardingOrchestrationFacade.close();
        verify(registryCenterRepository).close();
    }
    
    @Test
    public void assertCloseFailure() {
        doThrow(new RuntimeException()).when(registryCenterRepository).close();
        shardingOrchestrationFacade.close();
        verify(registryCenterRepository).close();
    }
}
