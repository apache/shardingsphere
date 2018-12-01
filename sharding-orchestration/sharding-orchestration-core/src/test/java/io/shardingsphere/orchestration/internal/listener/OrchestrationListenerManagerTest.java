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

import io.shardingsphere.orchestration.internal.config.listener.ConfigurationChangedListenerManager;
import io.shardingsphere.orchestration.internal.state.listener.StateChangedListenerManager;
import io.shardingsphere.orchestration.reg.api.RegistryCenter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;
import java.util.Collections;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public final class OrchestrationListenerManagerTest {
    
    @Mock
    private RegistryCenter regCenter;
    
    @Mock
    private ConfigurationChangedListenerManager configurationChangedListenerManager;
    
    @Mock
    private StateChangedListenerManager stateChangedListenerManager;
    
    @Test
    public void assertInitListeners() throws ReflectiveOperationException {
        ShardingOrchestrationListenerManager actual = new ShardingOrchestrationListenerManager("test", regCenter, Collections.<String>emptyList());
        setField(actual, "configurationChangedListenerManager", configurationChangedListenerManager);
        setField(actual, "stateChangedListenerManager", stateChangedListenerManager);
        actual.initListeners();
        verify(configurationChangedListenerManager).initListeners();
        verify(stateChangedListenerManager).initListeners();
    }
    
    private void setField(final Object target, final String fieldName, final Object fieldValue) throws ReflectiveOperationException {
        Field field = ShardingOrchestrationListenerManager.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, fieldValue);
    }
}
