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

package org.apache.shardingsphere.orchestration.core.facade;

import org.apache.shardingsphere.infra.auth.Authentication;
import org.apache.shardingsphere.infra.auth.ProxyUser;
import org.apache.shardingsphere.orchestration.center.RegistryCenterRepository;
import org.apache.shardingsphere.orchestration.center.config.CenterConfiguration;
import org.apache.shardingsphere.orchestration.center.config.OrchestrationConfiguration;
import org.apache.shardingsphere.orchestration.core.configcenter.ConfigCenter;
import org.apache.shardingsphere.orchestration.core.facade.listener.ShardingOrchestrationListenerManager;
import org.apache.shardingsphere.orchestration.core.facade.util.FieldUtil;
import org.apache.shardingsphere.orchestration.core.metadatacenter.MetaDataCenter;
import org.apache.shardingsphere.orchestration.core.registrycenter.RegistryCenter;
import org.apache.shardingsphere.infra.config.DataSourceConfiguration;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collection;
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
    private ConfigCenter configCenter;
    
    @Mock
    private RegistryCenter registryCenter;
    
    @Mock
    private MetaDataCenter metaDataCenter;
    
    @Mock
    private ShardingOrchestrationListenerManager listenerManager;
    
    @Before
    public void setUp() {
        Map<String, CenterConfiguration> instanceConfigurationMap = new HashMap<>();
        CenterConfiguration configuration1 = new CenterConfiguration("SecondTestRegistryCenter");
        configuration1.setOrchestrationType("registry_center");
        configuration1.setNamespace("namespace_1");
        instanceConfigurationMap.put("test_name_1", configuration1);
        CenterConfiguration configuration2 = new CenterConfiguration("FirstTestConfigCenter");
        configuration2.setOrchestrationType("config_center");
        configuration2.setNamespace("namespace_2");
        instanceConfigurationMap.put("test_name_2", configuration2);
        CenterConfiguration configuration3 = new CenterConfiguration("FirstTestConfigCenter");
        configuration3.setOrchestrationType("metadata_center");
        configuration3.setNamespace("namespace_3");
        instanceConfigurationMap.put("test_name_3", configuration3);
        OrchestrationConfiguration orchestrationConfiguration = new OrchestrationConfiguration(instanceConfigurationMap);
        shardingOrchestrationFacade = new ShardingOrchestrationFacade(orchestrationConfiguration, Arrays.asList("sharding_db", "masterslave_db"));
        FieldUtil.setField(shardingOrchestrationFacade, "registryCenterRepository", registryCenterRepository);
        FieldUtil.setField(shardingOrchestrationFacade, "configCenter", configCenter);
        FieldUtil.setField(shardingOrchestrationFacade, "registryCenter", registryCenter);
        FieldUtil.setField(shardingOrchestrationFacade, "metaDataCenter", metaDataCenter);
        FieldUtil.setField(shardingOrchestrationFacade, "listenerManager", listenerManager);
    }
    
    @Test
    public void assertInitWithParameters() {
        Map<String, DataSourceConfiguration> dataSourceConfigurationMap = Collections.singletonMap("test_ds", mock(DataSourceConfiguration.class));
        Map<String, Collection<RuleConfiguration>> ruleConfigurationMap = Collections.singletonMap("sharding_db", Collections.singletonList(mock(RuleConfiguration.class)));
        ProxyUser proxyUser = new ProxyUser("root", Collections.singleton("db1"));
        Authentication authentication = new Authentication();
        authentication.getUsers().put("root", proxyUser);
        Properties props = new Properties();
        shardingOrchestrationFacade.init(Collections.singletonMap("sharding_db", dataSourceConfigurationMap), ruleConfigurationMap, authentication, props);
        verify(configCenter).persistConfigurations("sharding_db", dataSourceConfigurationMap, ruleConfigurationMap.get("sharding_db"), authentication, props, false);
        verify(registryCenter).persistInstanceOnline();
        verify(registryCenter).persistDataSourcesNode();
        verify(listenerManager).initListeners();
    }
    
    @Test
    public void assertInitWithoutParameters() {
        shardingOrchestrationFacade.init();
        verify(registryCenter).persistInstanceOnline();
        verify(registryCenter).persistDataSourcesNode();
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
