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

package org.apache.shardingsphere.governance.core.facade;

import lombok.SneakyThrows;
import org.apache.shardingsphere.governance.core.registry.RegistryCenter;
import org.apache.shardingsphere.governance.core.registry.listener.GovernanceListenerManager;
import org.apache.shardingsphere.governance.repository.api.config.GovernanceConfiguration;
import org.apache.shardingsphere.governance.repository.api.config.RegistryCenterConfiguration;
import org.apache.shardingsphere.governance.repository.spi.RegistryCenterRepository;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.datasource.DataSourceConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public final class GovernanceFacadeTest {
    
    private final GovernanceFacade governanceFacade = new GovernanceFacade();
    
    @Mock
    private RegistryCenterRepository registryCenterRepository;
    
    @Mock
    private RegistryCenter registryCenter;
    
    @Mock
    private GovernanceListenerManager listenerManager;
    
    @Before
    public void setUp() {
        GovernanceConfiguration governanceConfig = new GovernanceConfiguration("test_name", new RegistryCenterConfiguration("REG", "127.0.0.1", new Properties()), false);
        governanceFacade.init(governanceConfig, Arrays.asList("sharding_db", "replica_query_db"));
        setField(governanceFacade, "registryCenterRepository", registryCenterRepository);
        setField(governanceFacade, "registryCenter", registryCenter);
        setField(governanceFacade, "listenerManager", listenerManager);
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    public static void setField(final Object target, final String fieldName, final Object fieldValue) {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, fieldValue);
    }
    
    @Test
    public void assertOnlineInstanceWithParameters() {
        Map<String, DataSourceConfiguration> dataSourceConfigMap = Collections.singletonMap("test_ds", mock(DataSourceConfiguration.class));
        Map<String, Collection<RuleConfiguration>> ruleConfigurationMap = Collections.singletonMap("sharding_db", Collections.singletonList(mock(RuleConfiguration.class)));
        Collection<RuleConfiguration> globalRuleConfigs = Collections.singleton(mock(RuleConfiguration.class));
        Properties props = new Properties();
        governanceFacade.onlineInstance(Collections.singletonMap("sharding_db", dataSourceConfigMap), ruleConfigurationMap, globalRuleConfigs, props);
        verify(registryCenter).persistConfigurations("sharding_db", dataSourceConfigMap, ruleConfigurationMap.get("sharding_db"), false);
        verify(registryCenter).persistGlobalConfiguration(globalRuleConfigs, props, false);
        verify(registryCenter).persistInstanceOnline();
        verify(registryCenter).persistDataNodes();
        verify(listenerManager).initListeners();
    }
    
    @Test
    public void assertOnlineInstanceWithoutParameters() {
        governanceFacade.onlineInstance();
        verify(registryCenter).persistInstanceOnline();
        verify(registryCenter).persistDataNodes();
        verify(listenerManager).initListeners();
    }
    
    @Test
    public void assertClose() {
        governanceFacade.close();
        verify(registryCenterRepository).close();
    }
}
