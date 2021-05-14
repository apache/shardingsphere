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
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public final class GovernanceFacadeTest {
    
    private final GovernanceFacade governanceFacade = new GovernanceFacade();
    
    @Test
    public void assertInit() {
        GovernanceConfiguration config = new GovernanceConfiguration("test_name", new RegistryCenterConfiguration("TEST", "127.0.0.1", new Properties()), false);
        governanceFacade.init(config, Arrays.asList("schema_0", "schema_1"));
        assertNotNull(governanceFacade.getRegistryCenter());
        // TODO use reflection to assert attributes of GovernanceFacade
    }
    
    @Test
    public void assertOnlineInstance() {
        RegistryCenter registryCenter = mock(RegistryCenter.class);
        GovernanceListenerManager listenerManager = mock(GovernanceListenerManager.class);
        setField(governanceFacade, "registryCenter", registryCenter);
        setField(governanceFacade, "listenerManager", listenerManager);
        Map<String, DataSourceConfiguration> dataSourceConfigMap = Collections.singletonMap("test_ds", mock(DataSourceConfiguration.class));
        Map<String, Collection<RuleConfiguration>> ruleConfigurationMap = Collections.singletonMap("sharding_db", Collections.singletonList(mock(RuleConfiguration.class)));
        Collection<RuleConfiguration> globalRuleConfigs = Collections.singleton(mock(RuleConfiguration.class));
        Properties props = new Properties();
        governanceFacade.onlineInstance(Collections.singletonMap("sharding_db", dataSourceConfigMap), ruleConfigurationMap, globalRuleConfigs, props);
        verify(registryCenter).persistGlobalConfiguration(globalRuleConfigs, props, false);
        verify(registryCenter).persistConfigurations("sharding_db", dataSourceConfigMap, ruleConfigurationMap.get("sharding_db"), false);
        verify(registryCenter).persistInstanceOnline();
        verify(registryCenter).persistDataNodes();
        verify(registryCenter).persistPrimaryNodes();
        verify(listenerManager).initListeners();
    }
    
    @Test
    public void assertClose() {
        RegistryCenterRepository registryCenterRepository = mock(RegistryCenterRepository.class);
        setField(governanceFacade, "registryCenterRepository", registryCenterRepository);
        governanceFacade.close();
        verify(registryCenterRepository).close();
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    public static void setField(final Object target, final String fieldName, final Object fieldValue) {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, fieldValue);
    }
}
