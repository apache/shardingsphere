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

import io.shardingsphere.api.config.MasterSlaveRuleConfiguration;
import io.shardingsphere.api.config.ShardingRuleConfiguration;
import io.shardingsphere.orchestration.internal.registry.config.event.MasterSlaveRuleChangedEvent;
import io.shardingsphere.orchestration.internal.registry.config.event.ShardingRuleChangedEvent;
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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class RuleChangedListenerTest {
    
    private RuleChangedListener ruleChangedListener;
    
    @Mock
    private RegistryCenter regCenter;
    
    @Mock
    private ConfigurationService configService;
    
    @Before
    @SneakyThrows
    public void setUp() {
        ruleChangedListener = new RuleChangedListener("test", regCenter, "sharding_schema");
        FieldUtil.setField(ruleChangedListener, "configService", configService);
    }
    
    @Test
    public void assertCreateShardingOrchestrationEventForSharding() {
        when(configService.isShardingRule("sharding_schema")).thenReturn(true);
        ShardingRuleConfiguration expected = mock(ShardingRuleConfiguration.class);
        when(configService.loadShardingRuleConfiguration("sharding_schema")).thenReturn(expected);
        ShardingRuleChangedEvent actual = (ShardingRuleChangedEvent) ruleChangedListener.createShardingOrchestrationEvent(mock(DataChangedEvent.class));
        assertThat(actual.getShardingSchemaName(), is("sharding_schema"));
        assertThat(actual.getShardingRuleConfiguration(), is(expected));
    }
    
    @Test
    public void assertCreateShardingOrchestrationEventForMasterSlave() {
        MasterSlaveRuleConfiguration expected = mock(MasterSlaveRuleConfiguration.class);
        when(configService.loadMasterSlaveRuleConfiguration("sharding_schema")).thenReturn(expected);
        MasterSlaveRuleChangedEvent actual = (MasterSlaveRuleChangedEvent) ruleChangedListener.createShardingOrchestrationEvent(mock(DataChangedEvent.class));
        assertThat(actual.getShardingSchemaName(), is("sharding_schema"));
        assertThat(actual.getMasterSlaveRuleConfiguration(), is(expected));
    }
}
