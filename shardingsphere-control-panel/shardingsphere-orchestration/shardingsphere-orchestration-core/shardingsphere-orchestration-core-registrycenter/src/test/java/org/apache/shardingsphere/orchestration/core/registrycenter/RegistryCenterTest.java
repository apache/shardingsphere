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

package org.apache.shardingsphere.orchestration.core.registrycenter;

import org.apache.shardingsphere.orchestration.center.RegistryCenterRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public final class RegistryCenterTest {
    
    @Mock
    private RegistryCenterRepository registryCenterRepository;
    
    private RegistryCenter registryCenter;
    
    @Before
    public void setUp() throws ReflectiveOperationException {
        registryCenter = new RegistryCenter("test", registryCenterRepository);
        Field field = registryCenter.getClass().getDeclaredField("repository");
        field.setAccessible(true);
        field.set(registryCenter, registryCenterRepository);
    }
    
    @Test
    public void assertPersistInstanceOnline() {
        registryCenter.persistInstanceOnline();
        verify(registryCenterRepository).persistEphemeral(anyString(), anyString());
    }
    
    @Test
    public void assertPersistDataSourcesNode() {
        registryCenter.persistDataSourcesNode();
        verify(registryCenterRepository).persist("/test/registry/datasources", "");
    }
    
    @Test
    public void assertPersistInstanceData() {
        registryCenter.persistInstanceData("test");
        verify(registryCenterRepository).persist(anyString(), anyString());
    }
    
    @Test
    public void assertLoadInstanceData() {
        registryCenter.loadInstanceData();
        verify(registryCenterRepository).get(anyString());
    }
}
