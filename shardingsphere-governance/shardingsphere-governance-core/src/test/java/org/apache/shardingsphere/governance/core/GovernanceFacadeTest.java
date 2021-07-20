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

package org.apache.shardingsphere.governance.core;

import lombok.SneakyThrows;
import org.apache.shardingsphere.governance.core.registry.GovernanceWatcherFactory;
import org.apache.shardingsphere.governance.core.registry.RegistryCenter;
import org.apache.shardingsphere.governance.repository.spi.RegistryCenterRepository;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Arrays;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public final class GovernanceFacadeTest {
    
    private final GovernanceFacade governanceFacade = new GovernanceFacade();
    
    @Test
    public void assertInit() {
        governanceFacade.init(mock(RegistryCenterRepository.class));
        assertNotNull(governanceFacade.getRegistryCenter());
        assertThat(getField(governanceFacade, "listenerFactory"), instanceOf(GovernanceWatcherFactory.class));
    }
    
    @Test
    public void assertOnlineInstance() {
        RegistryCenter registryCenter = mock(RegistryCenter.class);
        GovernanceWatcherFactory listenerFactory = mock(GovernanceWatcherFactory.class);
        setField(governanceFacade, "registryCenter", registryCenter);
        setField(governanceFacade, "listenerFactory", listenerFactory);
        governanceFacade.onlineInstance(Arrays.asList("schema_0", "schema_1"));
        verify(registryCenter).registerInstanceOnline();
        verify(listenerFactory).watchListeners(Arrays.asList("schema_0", "schema_1"));
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private static Object getField(final Object target, final String fieldName) {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(target);
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private static void setField(final Object target, final String fieldName, final Object fieldValue) {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, fieldValue);
    }
}
