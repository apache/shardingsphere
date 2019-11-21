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

package io.shardingsphere.orchestration.internal.registry;

import io.shardingsphere.api.config.rule.RuleConfiguration;
import io.shardingsphere.core.config.DataSourceConfiguration;
import io.shardingsphere.core.rule.Authentication;
import io.shardingsphere.orchestration.config.OrchestrationConfiguration;
import io.shardingsphere.orchestration.internal.registry.config.service.ConfigurationService;
import io.shardingsphere.orchestration.internal.registry.listener.ShardingOrchestrationListenerManager;
import io.shardingsphere.orchestration.internal.registry.state.service.StateService;
import io.shardingsphere.orchestration.reg.api.RegistryCenter;
import io.shardingsphere.orchestration.reg.api.RegistryCenterConfiguration;
import io.shardingsphere.orchestration.util.FieldUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public final class ShardingOrchestrationFacadeTest {
    
    private ShardingOrchestrationFacade shardingOrchestrationFacade;
    
    @Mock
    private RegistryCenter regCenter;
    
    @Mock
    private ConfigurationService configService;
    
    @Mock
    private StateService stateService;
    
    @Mock
    private ShardingOrchestrationListenerManager listenerManager;
    
    @Before
    public void setUp() {
        shardingOrchestrationFacade = new ShardingOrchestrationFacade(new OrchestrationConfiguration("test", new RegistryCenterConfiguration(), true), Arrays.asList("sharding_db", "masterslave_db"));
        FieldUtil.setField(shardingOrchestrationFacade, "regCenter", regCenter);
        FieldUtil.setField(shardingOrchestrationFacade, "configService", configService);
        FieldUtil.setField(shardingOrchestrationFacade, "stateService", stateService);
        FieldUtil.setField(shardingOrchestrationFacade, "listenerManager", listenerManager);
    }
    
    @Test
    public void assertInitWithParameters() {
        Map<String, DataSourceConfiguration> dataSourceConfigurationMap = Collections.singletonMap("test_ds", mock(DataSourceConfiguration.class));
        Map<String, RuleConfiguration> ruleConfigurationMap = Collections.singletonMap("sharding_db", mock(RuleConfiguration.class));
        Authentication authentication = new Authentication();
        Properties props = new Properties();
        shardingOrchestrationFacade.init(
                Collections.singletonMap("sharding_db", dataSourceConfigurationMap), ruleConfigurationMap, authentication, Collections.<String, Object>emptyMap(), props);
        verify(configService).persistConfiguration(
                "sharding_db", dataSourceConfigurationMap, ruleConfigurationMap.get("sharding_db"), authentication, Collections.<String, Object>emptyMap(), props, true);
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
    public void assertCloseSuccess() throws Exception {
        shardingOrchestrationFacade.close();
        verify(regCenter).close();
    }
    
    @Test
    public void assertCloseFailure() throws Exception {
        doThrow(new RuntimeException()).when(regCenter).close();
        shardingOrchestrationFacade.close();
        verify(regCenter).close();
    }
}
