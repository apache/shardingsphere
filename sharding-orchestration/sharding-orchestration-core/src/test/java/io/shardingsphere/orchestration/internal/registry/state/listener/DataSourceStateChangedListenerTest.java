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

package io.shardingsphere.orchestration.internal.registry.state.listener;

import io.shardingsphere.orchestration.internal.registry.state.schema.OrchestrationShardingSchema;
import io.shardingsphere.orchestration.internal.registry.state.service.DataSourceService;
import io.shardingsphere.orchestration.reg.api.RegistryCenter;
import io.shardingsphere.orchestration.reg.listener.DataChangedEvent;
import io.shardingsphere.orchestration.reg.listener.DataChangedEvent.ChangedType;
import io.shardingsphere.orchestration.util.FieldUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class DataSourceStateChangedListenerTest {
    
    private DataSourceStateChangedListener dataSourceStateChangedListener;
    
    @Mock
    private RegistryCenter regCenter;
    
    @Mock
    private DataSourceService dataSourceService;
    
    @Before
    public void setUp() {
        dataSourceStateChangedListener = new DataSourceStateChangedListener("test", regCenter);
        FieldUtil.setField(dataSourceStateChangedListener, "dataSourceService", dataSourceService);
    }
    
    @Test
    public void assertCreateShardingOrchestrationEvent() {
        OrchestrationShardingSchema expected = mock(OrchestrationShardingSchema.class);
        DataChangedEvent dataChangedEvent = new DataChangedEvent("test", "slave_0", ChangedType.UPDATED);
        when(dataSourceService.getDisabledSlaveShardingSchema(anyString())).thenReturn(expected);
        assertThat(dataSourceStateChangedListener.createShardingOrchestrationEvent(dataChangedEvent).getShardingSchema(), is(expected));
    }
}
