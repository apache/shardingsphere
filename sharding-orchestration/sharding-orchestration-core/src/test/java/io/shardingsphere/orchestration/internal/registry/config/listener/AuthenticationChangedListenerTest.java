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

import io.shardingsphere.core.rule.Authentication;
import io.shardingsphere.orchestration.internal.registry.config.service.ConfigurationService;
import io.shardingsphere.orchestration.reg.api.RegistryCenter;
import io.shardingsphere.orchestration.reg.listener.DataChangedEvent;
import io.shardingsphere.orchestration.util.FieldUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class AuthenticationChangedListenerTest {
    
    private AuthenticationChangedListener authenticationChangedListener;
    
    @Mock
    private RegistryCenter regCenter;
    
    @Mock
    private ConfigurationService configService;
    
    @Before
    public void setUp() {
        authenticationChangedListener = new AuthenticationChangedListener("test", regCenter);
        FieldUtil.setField(authenticationChangedListener, "configService", configService);
    }
    
    @Test
    public void assertCreateShardingOrchestrationEvent() {
        Authentication expected = new Authentication();
        when(configService.loadAuthentication()).thenReturn(expected);
        assertThat(authenticationChangedListener.createShardingOrchestrationEvent(mock(DataChangedEvent.class)).getAuthentication(), is(expected));
    }
}
