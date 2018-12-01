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
    private RuleChangedListener ruleChangedListener0;
    
    @Mock
    private RuleChangedListener ruleChangedListener1;
    
    @Mock
    private DataSourceChangedListener dataSourceChangedListener0;
    
    @Mock
    private DataSourceChangedListener dataSourceChangedListener1;
    
    @Mock
    private PropertiesChangedListener propertiesChangedListener;
    
    @Mock
    private AuthenticationChangedListener authenticationChangedListener;
    
    @Mock
    private ConfigMapChangedListener configMapChangedListener;
    
    @Test
    public void assertInitListeners() throws ReflectiveOperationException {
        ConfigurationChangedListenerManager actual = new ConfigurationChangedListenerManager("test", regCenter, Arrays.asList("sharding_db", "masterslave_db"));
        setField(actual, "ruleChangedListeners", Arrays.asList(ruleChangedListener0, ruleChangedListener1));
        setField(actual, "dataSourceChangedListeners", Arrays.asList(dataSourceChangedListener0, dataSourceChangedListener1));
        setField(actual, "propertiesChangedListener", propertiesChangedListener);
        setField(actual, "authenticationChangedListener", authenticationChangedListener);
        setField(actual, "configMapChangedListener", configMapChangedListener);
        actual.initListeners();
        verify(ruleChangedListener0).watch();
        verify(ruleChangedListener1).watch();
        verify(dataSourceChangedListener0).watch();
        verify(dataSourceChangedListener1).watch();
        verify(propertiesChangedListener).watch();
        verify(authenticationChangedListener).watch();
        verify(configMapChangedListener).watch();
    }
    
    private void setField(final Object target, final String fieldName, final Object fieldValue) throws ReflectiveOperationException {
        Field field = ConfigurationChangedListenerManager.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, fieldValue);
    }
}
