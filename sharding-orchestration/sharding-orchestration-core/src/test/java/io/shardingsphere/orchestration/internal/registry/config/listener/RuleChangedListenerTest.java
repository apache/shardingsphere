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

import io.shardingsphere.orchestration.internal.registry.config.event.MasterSlaveRuleChangedEvent;
import io.shardingsphere.orchestration.internal.registry.config.event.ShardingRuleChangedEvent;
import io.shardingsphere.orchestration.reg.api.RegistryCenter;
import io.shardingsphere.orchestration.reg.listener.DataChangedEvent;
import io.shardingsphere.orchestration.reg.listener.DataChangedEvent.ChangedType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public final class RuleChangedListenerTest {
    
    private static final String SHARDING_RULE_YAML = "tables:\n" + "  t_order:\n" + "    logicTable: t_order\n" + "    actualDataNodes: ds_${0..1}.t_order_${0..1}\n"
            + "    tableStrategy:\n" + "      inline:\n" + "        algorithmExpression: t_order_${order_id % 2}\n" + "        shardingColumn: order_id";
    
    private static final String MASTER_SLAVE_RULE_YAML = "masterDataSourceName: master_ds\n" + "name: ms_ds\n" + "slaveDataSourceNames:\n" + "- slave_ds_0\n" + "- slave_ds_1\n";
    
    private RuleChangedListener ruleChangedListener;
    
    @Mock
    private RegistryCenter regCenter;
    
    @Before
    public void setUp() {
        ruleChangedListener = new RuleChangedListener("test", regCenter, "sharding_schema");
    }
    
    @Test
    public void assertCreateShardingOrchestrationEventForSharding() {
        ShardingRuleChangedEvent actual = (ShardingRuleChangedEvent) ruleChangedListener.createShardingOrchestrationEvent(new DataChangedEvent("test", SHARDING_RULE_YAML, ChangedType.UPDATED));
        assertThat(actual.getShardingSchemaName(), is("sharding_schema"));
        assertThat(actual.getShardingRuleConfiguration().getTableRuleConfigs().size(), is(1));
    }
    
    @Test
    public void assertCreateShardingOrchestrationEventForMasterSlave() {
        MasterSlaveRuleChangedEvent actual = (MasterSlaveRuleChangedEvent) ruleChangedListener.createShardingOrchestrationEvent(
                new DataChangedEvent("test", MASTER_SLAVE_RULE_YAML, ChangedType.UPDATED));
        assertThat(actual.getShardingSchemaName(), is("sharding_schema"));
        assertThat(actual.getMasterSlaveRuleConfiguration().getMasterDataSourceName(), is("master_ds"));
    }
}
