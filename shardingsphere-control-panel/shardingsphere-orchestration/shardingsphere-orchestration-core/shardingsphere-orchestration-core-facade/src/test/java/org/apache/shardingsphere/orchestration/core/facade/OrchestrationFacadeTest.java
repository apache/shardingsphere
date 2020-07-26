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
import org.apache.shardingsphere.infra.config.DataSourceConfiguration;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.metrics.configuration.config.MetricsConfiguration;
import org.apache.shardingsphere.orchestration.core.config.ConfigCenter;
import org.apache.shardingsphere.orchestration.core.facade.listener.OrchestrationListenerManager;
import org.apache.shardingsphere.orchestration.core.facade.util.FieldUtil;
import org.apache.shardingsphere.orchestration.core.metadata.MetaDataCenter;
import org.apache.shardingsphere.orchestration.core.registry.RegistryCenter;
import org.apache.shardingsphere.orchestration.repository.api.RegistryRepository;
import org.apache.shardingsphere.orchestration.repository.api.config.OrchestrationConfiguration;
import org.apache.shardingsphere.orchestration.repository.api.config.OrchestrationRepositoryConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public final class OrchestrationFacadeTest {
    
    private final OrchestrationFacade orchestrationFacade = OrchestrationFacade.getInstance();
    
    @Mock
    private RegistryRepository registryRepository;
    
    @Mock
    private ConfigCenter configCenter;
    
    @Mock
    private RegistryCenter registryCenter;
    
    @Mock
    private MetaDataCenter metaDataCenter;
    
    @Mock
    private OrchestrationListenerManager listenerManager;
    
    @Before
    public void setUp() {
        OrchestrationRepositoryConfiguration configuration1 = new OrchestrationRepositoryConfiguration("REG_TEST", new Properties());
        configuration1.setNamespace("namespace_1");
        OrchestrationRepositoryConfiguration configuration2 = new OrchestrationRepositoryConfiguration("CONFIG_TEST", new Properties());
        configuration2.setNamespace("namespace_2");
        OrchestrationConfiguration orchestrationConfiguration = new OrchestrationConfiguration("test_name", configuration1, configuration2);
        orchestrationFacade.init(orchestrationConfiguration, Arrays.asList("sharding_db", "masterslave_db"));
        FieldUtil.setField(orchestrationFacade, "registryRepository", registryRepository);
        FieldUtil.setField(orchestrationFacade, "configCenter", configCenter);
        FieldUtil.setField(orchestrationFacade, "registryCenter", registryCenter);
        FieldUtil.setField(orchestrationFacade, "metaDataCenter", metaDataCenter);
        FieldUtil.setField(orchestrationFacade, "listenerManager", listenerManager);
    }
    
    @Test
    public void assertInitWithParameters() {
        Map<String, DataSourceConfiguration> dataSourceConfigurationMap = Collections.singletonMap("test_ds", mock(DataSourceConfiguration.class));
        Map<String, Collection<RuleConfiguration>> ruleConfigurationMap = Collections.singletonMap("sharding_db", Collections.singletonList(mock(RuleConfiguration.class)));
        ProxyUser proxyUser = new ProxyUser("root", Collections.singleton("db1"));
        Authentication authentication = new Authentication();
        authentication.getUsers().put("root", proxyUser);
        Properties props = new Properties();
        orchestrationFacade.initConfigurations(Collections.singletonMap("sharding_db", dataSourceConfigurationMap), ruleConfigurationMap, authentication, props);
        verify(configCenter).persistConfigurations("sharding_db", dataSourceConfigurationMap, ruleConfigurationMap.get("sharding_db"), false);
        verify(configCenter).persistGlobalConfiguration(authentication, props, false);
        verify(registryCenter).persistInstanceOnline();
        verify(registryCenter).persistDataSourcesNode();
        verify(listenerManager).initListeners();
    }
    
    @Test
    public void assertInitMetricsConfiguration() {
        MetricsConfiguration metricsConfiguration = new MetricsConfiguration("fixture", null, 0, false, true, 8, null);
        orchestrationFacade.initMetricsConfiguration(metricsConfiguration);
        verify(configCenter).persistMetricsConfiguration(metricsConfiguration, false);
    }
    
    @Test
    public void assertInitWithoutParameters() {
        orchestrationFacade.initConfigurations();
        verify(registryCenter).persistInstanceOnline();
        verify(registryCenter).persistDataSourcesNode();
        verify(listenerManager).initListeners();
    }
    
    @Test
    public void assertCloseSuccess() {
        orchestrationFacade.close();
        verify(registryRepository).close();
    }
    
    @Test
    public void assertCloseFailure() {
        doThrow(new RuntimeException()).when(registryRepository).close();
        orchestrationFacade.close();
        verify(registryRepository).close();
    }
}
