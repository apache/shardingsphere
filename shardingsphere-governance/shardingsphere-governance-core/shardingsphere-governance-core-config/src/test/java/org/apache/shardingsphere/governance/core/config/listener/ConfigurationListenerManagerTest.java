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

package org.apache.shardingsphere.governance.core.config.listener;

import lombok.SneakyThrows;
import org.apache.shardingsphere.governance.repository.api.ConfigurationRepository;
import org.apache.shardingsphere.governance.repository.api.listener.DataChangedEvent.ChangedType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;
import java.util.Arrays;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public final class ConfigurationListenerManagerTest {
    
    @Mock
    private ConfigurationRepository configurationRepository;
    
    @Mock
    private SchemaChangedListener schemaChangedListener;
    
    @Mock
    private PropertiesChangedListener propertiesChangedListener;
    
    @Mock
    private AuthenticationChangedListener authenticationChangedListener;
    
    @Test
    public void assertInitListeners() {
        ConfigurationListenerManager actual = new ConfigurationListenerManager(configurationRepository, Arrays.asList("sharding_db", "primary_replica_replication_db"));
        setField(actual, "schemaChangedListener", schemaChangedListener);
        setField(actual, "propertiesChangedListener", propertiesChangedListener);
        setField(actual, "authenticationChangedListener", authenticationChangedListener);
        actual.initListeners();
        verify(propertiesChangedListener).watch(ChangedType.UPDATED);
        verify(authenticationChangedListener).watch(ChangedType.UPDATED);
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private static void setField(final Object target, final String fieldName, final Object fieldValue) {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, fieldValue);
    }
}
