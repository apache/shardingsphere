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

package org.apache.shardingsphere.orchestration.internal.rule;

import org.apache.shardingsphere.api.config.masterslave.LoadBalanceStrategyConfiguration;
import org.apache.shardingsphere.api.config.masterslave.MasterSlaveRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.ShardingRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.TableRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.strategy.InlineShardingStrategyConfiguration;
import org.apache.shardingsphere.api.config.sharding.strategy.NoneShardingStrategyConfiguration;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class OrchestrationShardingRuleTest {
    
    private OrchestrationShardingRule orchestrationShardingRule;
    
    @Before
    public void setUp() {
        orchestrationShardingRule = new OrchestrationShardingRule(createShardingRuleConfiguration(), Arrays.asList("master_db", "slave_db_0", "slave_db_1"));
    }
    
    private ShardingRuleConfiguration createShardingRuleConfiguration() {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        result.getTableRuleConfigs().add(new TableRuleConfiguration("t_order", "ds_ms.t_order_${0..1}"));
        result.getMasterSlaveRuleConfigs().add(new MasterSlaveRuleConfiguration("ds_ms", "master_db", Arrays.asList("slave_db_0", "slave_db_1"), new LoadBalanceStrategyConfiguration("RANDOM")));
        result.setDefaultDatabaseShardingStrategyConfig(new NoneShardingStrategyConfiguration());
        result.setDefaultTableShardingStrategyConfig(new InlineShardingStrategyConfiguration("order_id", "t_order_${order_id % 2}"));
        return result;
    }
    
    @Test
    public void assertGetMasterSlaveRules() {
        assertThat(orchestrationShardingRule.getMasterSlaveRules().size(), is(1));
        assertThat(orchestrationShardingRule.getMasterSlaveRules().iterator().next(), instanceOf(OrchestrationMasterSlaveRule.class));
    }
}
