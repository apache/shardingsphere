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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;
import java.util.Arrays;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public final class ConfigurationOrchestrationListenerManagerTest {
    
    @Mock
    private RegistryCenter regCenter;
    
    @Mock
    private RuleOrchestrationListener ruleOrchestrationListener0;
    
    @Mock
    private RuleOrchestrationListener ruleOrchestrationListener1;
    
    @Mock
    private DataSourceOrchestrationListener dataSourceOrchestrationListener0;
    
    @Mock
    private DataSourceOrchestrationListener dataSourceOrchestrationListener1;
    
    @Mock
    private PropertiesOrchestrationListener propertiesListenerManager;
    
    @Mock
    private AuthenticationOrchestrationListener authenticationListenerManager;
    
    @Mock
    private ConfigMapOrchestrationListener configMapListenerManager;
    
    @Test
    public void assertInitListeners() throws ReflectiveOperationException {
        ConfigurationOrchestrationListenerManager actual = new ConfigurationOrchestrationListenerManager("test", regCenter, Arrays.asList("sharding_db", "masterslave_db"));
        setField(actual, "ruleListenerManagers", Arrays.asList(ruleOrchestrationListener0, ruleOrchestrationListener1));
        setField(actual, "dataSourceListenerManagers", Arrays.asList(dataSourceOrchestrationListener0, dataSourceOrchestrationListener1));
        setField(actual, "propertiesListenerManager", propertiesListenerManager);
        setField(actual, "authenticationListenerManager", authenticationListenerManager);
        setField(actual, "configMapListenerManager", configMapListenerManager);
        actual.initListeners();
        verify(ruleOrchestrationListener0).watch();
        verify(ruleOrchestrationListener1).watch();
        verify(dataSourceOrchestrationListener0).watch();
        verify(dataSourceOrchestrationListener1).watch();
        verify(propertiesListenerManager).watch();
        verify(authenticationListenerManager).watch();
        verify(configMapListenerManager).watch();
    }
    
    private void setField(final Object target, final String fieldName, final Object fieldValue) throws ReflectiveOperationException {
        Field field = ConfigurationOrchestrationListenerManager.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, fieldValue);
    }
}
