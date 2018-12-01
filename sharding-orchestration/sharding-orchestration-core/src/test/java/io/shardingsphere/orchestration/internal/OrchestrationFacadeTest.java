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

package io.shardingsphere.orchestration.internal;

import io.shardingsphere.api.config.RuleConfiguration;
import io.shardingsphere.core.config.DataSourceConfiguration;
import io.shardingsphere.core.rule.Authentication;
import io.shardingsphere.orchestration.config.OrchestrationConfiguration;
import io.shardingsphere.orchestration.internal.config.service.ConfigurationService;
import io.shardingsphere.orchestration.internal.listener.ShardingOrchestrationListenerManager;
import io.shardingsphere.orchestration.internal.state.service.DataSourceService;
import io.shardingsphere.orchestration.internal.state.service.InstanceStateService;
import io.shardingsphere.orchestration.reg.api.RegistryCenter;
import io.shardingsphere.orchestration.reg.api.RegistryCenterConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public final class OrchestrationFacadeTest {
    
    private OrchestrationFacade orchestrationFacade;
    
    @Mock
    private RegistryCenter regCenter;
    
    @Mock
    private ConfigurationService configService;
    
    @Mock
    private InstanceStateService instanceStateService;
    
    @Mock
    private DataSourceService dataSourceService;
    
    @Mock
    private ShardingOrchestrationListenerManager listenerManager;
    
    @Before
    public void setUp() throws ReflectiveOperationException {
        orchestrationFacade = new OrchestrationFacade(new OrchestrationConfiguration("test", new RegistryCenterConfiguration(), true), Arrays.asList("sharding_db", "masterslave_db"));
        setField(orchestrationFacade, "regCenter", regCenter);
        setField(orchestrationFacade, "configService", configService);
        setField(orchestrationFacade, "instanceStateService", instanceStateService);
        setField(orchestrationFacade, "dataSourceService", dataSourceService);
        setField(orchestrationFacade, "listenerManager", listenerManager);
    }
    
    private void setField(final Object target, final String fieldName, final Object fieldValue) throws ReflectiveOperationException {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, fieldValue);
    }
    
    @Test
    public void assertInitWithParameters() {
        Map<String, DataSourceConfiguration> dataSourceConfigurationMap = Collections.singletonMap("test_ds", mock(DataSourceConfiguration.class));
        Map<String, RuleConfiguration> ruleConfigurationMap = Collections.singletonMap("sharding_db", mock(RuleConfiguration.class));
        Authentication authentication = new Authentication();
        Properties props = new Properties();
        orchestrationFacade.init(
                Collections.singletonMap("sharding_db", dataSourceConfigurationMap), ruleConfigurationMap, authentication, Collections.<String, Object>emptyMap(), props);
        verify(configService).persistConfiguration(
                "sharding_db", dataSourceConfigurationMap, ruleConfigurationMap.get("sharding_db"), authentication, Collections.<String, Object>emptyMap(), props, true);
        verify(instanceStateService).persistInstanceOnline();
        verify(dataSourceService).initDataSourcesNode();
        verify(listenerManager).initListeners();
    }
    
    @Test
    public void assertInitWithoutParameters() {
        orchestrationFacade.init();
        verify(instanceStateService).persistInstanceOnline();
        verify(dataSourceService).initDataSourcesNode();
        verify(listenerManager).initListeners();
    }
    
    @Test
    public void assertCloseSuccess() throws Exception {
        orchestrationFacade.close();
        verify(regCenter).close();
    }
    
    @Test
    public void assertCloseFailure() throws Exception {
        doThrow(new RuntimeException()).when(regCenter).close();
        orchestrationFacade.close();
        verify(regCenter).close();
    }
}
