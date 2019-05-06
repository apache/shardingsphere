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

package io.shardingsphere.core.routing.type.complex;

import io.shardingsphere.api.algorithm.sharding.ListShardingValue;
import io.shardingsphere.api.algorithm.sharding.ShardingValue;
import io.shardingsphere.api.config.rule.ShardingRuleConfiguration;
import io.shardingsphere.api.config.rule.TableRuleConfiguration;
import io.shardingsphere.api.config.strategy.InlineShardingStrategyConfiguration;
import io.shardingsphere.core.optimizer.condition.ShardingCondition;
import io.shardingsphere.core.optimizer.condition.ShardingConditions;
import io.shardingsphere.core.routing.type.RoutingResult;
import io.shardingsphere.core.routing.type.TableUnit;
import io.shardingsphere.core.rule.ShardingRule;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class ComplexRoutingEngineTest {
    
    private ShardingRule shardingRule;
    
    @Before
    public void setEngineContext() {
        TableRuleConfiguration tableRuleConfig1 = new TableRuleConfiguration();
        tableRuleConfig1.setLogicTable("t_order");
        tableRuleConfig1.setActualDataNodes("ds${0..1}.t_order_${0..2}");
        tableRuleConfig1.setTableShardingStrategyConfig(new InlineShardingStrategyConfiguration("order_id", "t_order_${order_id % 2}"));
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getTableRuleConfigs().add(tableRuleConfig1);
        TableRuleConfiguration tableRuleConfig2 = new TableRuleConfiguration();
        tableRuleConfig2.setLogicTable("t_order_item");
        tableRuleConfig2.setActualDataNodes("ds${0..1}.t_order_item_${0..2}");
        tableRuleConfig2.setTableShardingStrategyConfig(new InlineShardingStrategyConfiguration("order_id", "t_order_item_${order_id % 2}"));
        shardingRuleConfig.getTableRuleConfigs().add(tableRuleConfig2);
        shardingRuleConfig.getBindingTableGroups().add("t_order, t_order_item");
        shardingRuleConfig.setDefaultDatabaseShardingStrategyConfig(new InlineShardingStrategyConfiguration("user_id", "ds${user_id % 2}"));
        shardingRuleConfig.getBroadcastTables().add("t_config");
        shardingRule = new ShardingRule(shardingRuleConfig, Arrays.asList("ds0", "ds1"));
    }
    
    @Test
    public void assertRoutingForBindingTables() {
        List<ShardingCondition> shardingConditions = new ArrayList<>();
        ShardingValue shardingValue1 = new ListShardingValue<>("t_order", "user_id", Collections.singleton(1L));
        ShardingValue shardingValue2 = new ListShardingValue<>("t_order", "order_id", Collections.singleton(1L));
        ShardingCondition shardingCondition = new ShardingCondition();
        shardingCondition.getShardingValues().add(shardingValue1);
        shardingCondition.getShardingValues().add(shardingValue2);
        shardingConditions.add(shardingCondition);
        ComplexRoutingEngine complexRoutingEngine = new ComplexRoutingEngine(shardingRule, Arrays.asList("t_order", "t_order_item"), new ShardingConditions(shardingConditions));
        RoutingResult routingResult = complexRoutingEngine.route();
        List<TableUnit> tableUnitList = new ArrayList<>(routingResult.getTableUnits().getTableUnits());
        assertThat(routingResult, instanceOf(RoutingResult.class));
        assertThat(routingResult.getTableUnits().getTableUnits().size(), is(1));
        assertThat(tableUnitList.get(0).getDataSourceName(), is("ds1"));
        assertThat(tableUnitList.get(0).getRoutingTables().size(), is(1));
        assertThat(tableUnitList.get(0).getRoutingTables().get(0).getActualTableName(), is("t_order_1"));
        assertThat(tableUnitList.get(0).getRoutingTables().get(0).getLogicTableName(), is("t_order"));
    }
    
    @Test
    public void assertRoutingForShardingTableJoinBroadcastTable() {
        List<ShardingCondition> shardingConditions = new ArrayList<>();
        ShardingValue shardingValue1 = new ListShardingValue<>("t_order", "user_id", Collections.singleton(1L));
        ShardingValue shardingValue2 = new ListShardingValue<>("t_order", "order_id", Collections.singleton(1L));
        ShardingCondition shardingCondition = new ShardingCondition();
        shardingCondition.getShardingValues().add(shardingValue1);
        shardingCondition.getShardingValues().add(shardingValue2);
        shardingConditions.add(shardingCondition);
        ComplexRoutingEngine complexRoutingEngine = new ComplexRoutingEngine(shardingRule, Arrays.asList("t_order", "t_config"), new ShardingConditions(shardingConditions));
        RoutingResult routingResult = complexRoutingEngine.route();
        List<TableUnit> tableUnitList = new ArrayList<>(routingResult.getTableUnits().getTableUnits());
        assertThat(routingResult, instanceOf(RoutingResult.class));
        assertThat(routingResult.getTableUnits().getTableUnits().size(), is(1));
        assertThat(tableUnitList.get(0).getDataSourceName(), is("ds1"));
        assertThat(tableUnitList.get(0).getRoutingTables().size(), is(1));
        assertThat(tableUnitList.get(0).getRoutingTables().get(0).getActualTableName(), is("t_order_1"));
        assertThat(tableUnitList.get(0).getRoutingTables().get(0).getLogicTableName(), is("t_order"));
    }
}
