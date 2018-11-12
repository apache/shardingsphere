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

package io.shardingsphere.orchestration.internal.state.service;

import io.shardingsphere.orchestration.reg.api.RegistryCenter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class InstanceStateServiceTest {
    
    @Mock
    private RegistryCenter regCenter;
    
    private InstanceStateService instanceStateService;
    
    @Before
    public void setUp() {
        instanceStateService = new InstanceStateService("test", regCenter);
    }
    
    @Test
    public void assertPersistInstanceOnline() {
        instanceStateService.persistInstanceOnline();
        verify(regCenter).persistEphemeral(anyString(), anyString());
    }
}
