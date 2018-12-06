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

package io.shardingsphere.orchestration.internal.registry.state.service;

import io.shardingsphere.orchestration.reg.api.RegistryCenter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public final class StateServiceTest {
    
    @Mock
    private RegistryCenter regCenter;
    
    private StateService stateService;
    
    @Before
    public void setUp() throws ReflectiveOperationException {
        stateService = new StateService("test", regCenter);
        Field field = stateService.getClass().getDeclaredField("regCenter");
        field.setAccessible(true);
        field.set(stateService, regCenter);
    }
    
    @Test
    public void assertPersistInstanceOnline() {
        stateService.persistInstanceOnline();
        verify(regCenter).persistEphemeral(anyString(), anyString());
    }
    
    @Test
    public void assertPersistDataSourcesNode() {
        stateService.persistDataSourcesNode();
        verify(regCenter).persist("/test/state/datasources", "");
    }
}
