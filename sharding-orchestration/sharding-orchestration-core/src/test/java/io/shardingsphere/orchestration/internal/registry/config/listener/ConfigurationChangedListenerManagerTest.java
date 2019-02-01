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

package io.shardingsphere.orchestration.internal.registry.config.listener;

import io.shardingsphere.orchestration.reg.api.RegistryCenter;
import io.shardingsphere.orchestration.reg.listener.DataChangedEvent.ChangedType;
import io.shardingsphere.orchestration.util.FieldUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public final class ConfigurationChangedListenerManagerTest {
    
    @Mock
    private RegistryCenter regCenter;
    
    @Mock
    private SchemaChangedListener schemaChangedListener;
    
    @Mock
    private PropertiesChangedListener propertiesChangedListener;
    
    @Mock
    private AuthenticationChangedListener authenticationChangedListener;
    
    @Mock
    private ConfigMapChangedListener configMapChangedListener;
    
    @Test
    public void assertInitListeners() {
        ConfigurationChangedListenerManager actual = new ConfigurationChangedListenerManager("test", regCenter, Arrays.asList("sharding_db", "masterslave_db"));
        FieldUtil.setField(actual, "schemaChangedListener", schemaChangedListener);
        FieldUtil.setField(actual, "propertiesChangedListener", propertiesChangedListener);
        FieldUtil.setField(actual, "authenticationChangedListener", authenticationChangedListener);
        FieldUtil.setField(actual, "configMapChangedListener", configMapChangedListener);
        actual.initListeners();
        verify(propertiesChangedListener).watch(ChangedType.UPDATED);
        verify(authenticationChangedListener).watch(ChangedType.UPDATED);
        verify(configMapChangedListener).watch(ChangedType.UPDATED);
    }
}
