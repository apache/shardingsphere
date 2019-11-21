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

package io.shardingsphere.orchestration.internal.rule;

import io.shardingsphere.api.algorithm.masterslave.RandomMasterSlaveLoadBalanceAlgorithm;
import io.shardingsphere.api.config.rule.MasterSlaveRuleConfiguration;
import io.shardingsphere.api.config.rule.ShardingRuleConfiguration;
import io.shardingsphere.api.config.rule.TableRuleConfiguration;
import io.shardingsphere.api.config.strategy.InlineShardingStrategyConfiguration;
import io.shardingsphere.api.config.strategy.NoneShardingStrategyConfiguration;
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
        TableRuleConfiguration tableRuleConfig = new TableRuleConfiguration();
        tableRuleConfig.setLogicTable("t_order");
        tableRuleConfig.setActualDataNodes("ds_ms.t_order_${0..1}");
        result.getTableRuleConfigs().add(tableRuleConfig);
        result.getMasterSlaveRuleConfigs().add(new MasterSlaveRuleConfiguration("ds_ms", "master_db", Arrays.asList("slave_db_0", "slave_db_1"), new RandomMasterSlaveLoadBalanceAlgorithm()));
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
