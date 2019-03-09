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

package org.apache.shardingsphere.core.routing.type.standard;

import org.apache.shardingsphere.api.config.sharding.ShardingRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.TableRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.strategy.InlineShardingStrategyConfiguration;
import org.apache.shardingsphere.core.optimizer.result.condition.ShardingCondition;
import org.apache.shardingsphere.core.optimizer.result.condition.ShardingConditions;
import org.apache.shardingsphere.core.parsing.parser.context.condition.Column;
import org.apache.shardingsphere.core.parsing.parser.sql.SQLStatement;
import org.apache.shardingsphere.core.routing.type.RoutingResult;
import org.apache.shardingsphere.core.routing.type.TableUnit;
import org.apache.shardingsphere.core.routing.value.ListRouteValue;
import org.apache.shardingsphere.core.routing.value.RouteValue;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public final class StandardRoutingEngineTest {
    
    private StandardRoutingEngine standardRoutingEngine;
    
    @Before
    public void setEngineContext() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getTableRuleConfigs().add(new TableRuleConfiguration("t_order", "ds_${0..1}.t_order_${0..1}"));
        shardingRuleConfig.setDefaultDatabaseShardingStrategyConfig(new InlineShardingStrategyConfiguration("user_id", "ds_${user_id % 2}"));
        shardingRuleConfig.setDefaultTableShardingStrategyConfig(new InlineShardingStrategyConfiguration("order_id", "t_order_${order_id % 2}"));
        List<ShardingCondition> shardingConditions = new ArrayList<>();
        RouteValue shardingValue1 = new ListRouteValue<>(new Column("user_id", "t_order"), Collections.singleton(1L));
        RouteValue shardingValue2 = new ListRouteValue<>(new Column("order_id", "t_order"), Collections.singleton(1L));
        ShardingCondition shardingCondition = new ShardingCondition();
        shardingCondition.getShardingValues().add(shardingValue1);
        shardingCondition.getShardingValues().add(shardingValue2);
        shardingConditions.add(shardingCondition);
        ShardingRule shardingRule = new ShardingRule(shardingRuleConfig, Arrays.asList("ds_0", "ds_1"));
        standardRoutingEngine = new StandardRoutingEngine(mock(SQLStatement.class), shardingRule, "t_order", new ShardingConditions(shardingConditions));
    }
    
    @Test
    public void assertRoute() {
        RoutingResult routingResult = standardRoutingEngine.route();
        List<TableUnit> tableUnitList = new ArrayList<>(routingResult.getTableUnits().getTableUnits());
        assertThat(routingResult, instanceOf(RoutingResult.class));
        assertThat(routingResult.getTableUnits().getTableUnits().size(), is(1));
        assertThat(tableUnitList.get(0).getDataSourceName(), is("ds_1"));
        assertThat(tableUnitList.get(0).getRoutingTables().size(), is(1));
        assertThat(tableUnitList.get(0).getRoutingTables().get(0).getActualTableName(), is("t_order_1"));
        assertThat(tableUnitList.get(0).getRoutingTables().get(0).getLogicTableName(), is("t_order"));
    }
}
