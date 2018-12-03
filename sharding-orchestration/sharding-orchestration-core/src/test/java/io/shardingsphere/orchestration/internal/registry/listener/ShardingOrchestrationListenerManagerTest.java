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

package io.shardingsphere.orchestration.internal.registry.listener;

import io.shardingsphere.orchestration.internal.registry.config.listener.ConfigurationChangedListenerManager;
import io.shardingsphere.orchestration.internal.registry.state.listener.StateChangedListenerManager;
import io.shardingsphere.orchestration.reg.api.RegistryCenter;
import io.shardingsphere.orchestration.util.FieldUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public final class ShardingOrchestrationListenerManagerTest {
    
    @Mock
    private RegistryCenter regCenter;
    
    @Mock
    private ConfigurationChangedListenerManager configurationChangedListenerManager;
    
    @Mock
    private StateChangedListenerManager stateChangedListenerManager;
    
    @Test
    public void assertInitListeners() {
        ShardingOrchestrationListenerManager actual = new ShardingOrchestrationListenerManager("test", regCenter, Collections.<String>emptyList());
        FieldUtil.setField(actual, "configurationChangedListenerManager", configurationChangedListenerManager);
        FieldUtil.setField(actual, "stateChangedListenerManager", stateChangedListenerManager);
        actual.initListeners();
        verify(configurationChangedListenerManager).initListeners();
        verify(stateChangedListenerManager).initListeners();
    }
}
