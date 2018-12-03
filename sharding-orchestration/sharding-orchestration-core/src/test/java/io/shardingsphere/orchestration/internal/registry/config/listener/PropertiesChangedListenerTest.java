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

import io.shardingsphere.core.constant.properties.ShardingPropertiesConstant;
import io.shardingsphere.orchestration.internal.registry.config.service.ConfigurationService;
import io.shardingsphere.orchestration.reg.api.RegistryCenter;
import io.shardingsphere.orchestration.reg.listener.DataChangedEvent;
import io.shardingsphere.orchestration.util.FieldUtil;
import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class PropertiesChangedListenerTest {
    
    private PropertiesChangedListener propertiesChangedListener;
    
    @Mock
    private RegistryCenter regCenter;
    
    @Mock
    private ConfigurationService configService;
    
    @Before
    @SneakyThrows
    public void setUp() {
        propertiesChangedListener = new PropertiesChangedListener("test", regCenter);
        FieldUtil.setField(propertiesChangedListener, "configService", configService);
    }
    
    @Test
    public void assertCreateShardingOrchestrationEvent() {
        Properties expected = new Properties();
        expected.setProperty(ShardingPropertiesConstant.SQL_SHOW.getKey(), Boolean.TRUE.toString());
        when(configService.loadProperties()).thenReturn(expected);
        assertThat(propertiesChangedListener.createShardingOrchestrationEvent(mock(DataChangedEvent.class)).getProps(), is(expected));
    }
}
