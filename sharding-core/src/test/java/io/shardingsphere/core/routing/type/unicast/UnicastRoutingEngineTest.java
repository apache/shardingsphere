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

package io.shardingsphere.core.routing.type.unicast;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import io.shardingsphere.api.config.rule.ShardingRuleConfiguration;
import io.shardingsphere.api.config.rule.TableRuleConfiguration;
import io.shardingsphere.core.exception.ShardingConfigurationException;
import io.shardingsphere.core.routing.type.RoutingResult;
import io.shardingsphere.core.routing.type.TableUnit;
import io.shardingsphere.core.rule.ShardingRule;

public final class UnicastRoutingEngineTest {
    
    private ShardingRule shardingRule;
    
    @Before
    public void setEngineContext() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        TableRuleConfiguration tableRuleConfig = new TableRuleConfiguration();
        tableRuleConfig.setLogicTable("t_order");
        tableRuleConfig.setActualDataNodes("ds${0..1}.t_order_${0..2}");
        shardingRuleConfig.getTableRuleConfigs().add(tableRuleConfig);
        shardingRuleConfig.getBroadcastTables().add("t_config");
        shardingRuleConfig.setDefaultDataSourceName("defaultDatasource");
        shardingRule = new ShardingRule(shardingRuleConfig, Arrays.asList("ds0", "ds1"));
    }
    
    @Test
    public void assertRoutingForShardingTable() {
        UnicastRoutingEngine unicastRoutingEngine = new UnicastRoutingEngine(shardingRule, Collections.singleton("t_order"));
        RoutingResult routingResult = unicastRoutingEngine.route();
        List<TableUnit> tableUnitList = new ArrayList<>(routingResult.getTableUnits().getTableUnits());
        assertThat(routingResult, instanceOf(RoutingResult.class));
        assertThat(routingResult.getTableUnits().getTableUnits().size(), is(1));
    }

    @Test
    public void assertRoutingForBroadcastTable() {
        UnicastRoutingEngine unicastRoutingEngine = new UnicastRoutingEngine(shardingRule, Collections.singleton("t_config"));
        RoutingResult routingResult = unicastRoutingEngine.route();
        List<TableUnit> tableUnitList = new ArrayList<>(routingResult.getTableUnits().getTableUnits());
        assertThat(routingResult, instanceOf(RoutingResult.class));
        assertThat(routingResult.getTableUnits().getTableUnits().size(), is(1));
    }

    @Test
    public void assertRoutingForNoTable() {
        UnicastRoutingEngine unicastRoutingEngine = new UnicastRoutingEngine(shardingRule, Collections.<String>emptyList());
        RoutingResult routingResult = unicastRoutingEngine.route();
        List<TableUnit> tableUnitList = new ArrayList<>(routingResult.getTableUnits().getTableUnits());
        assertThat(routingResult, instanceOf(RoutingResult.class));
        assertThat(routingResult.getTableUnits().getTableUnits().size(), is(1));
    }
    
    @Test
    public void assertRoutingForShardingTableAndBroadcastTable() {
        Set<String> sets = new HashSet<>();
        sets.add("t_order");
        sets.add("t_config");
        UnicastRoutingEngine unicastRoutingEngine = new UnicastRoutingEngine(shardingRule, sets);
        RoutingResult routingResult = unicastRoutingEngine.route();
        assertThat(routingResult, instanceOf(RoutingResult.class));
        assertThat(routingResult.getTableUnits().getTableUnits().size(), is(1));
    }
    
    @Test(expected = ShardingConfigurationException.class)
    public void assertRouteForWithNoIntersection() {
        Set<String> sets = new HashSet<>();
        sets.add("t_order");
        sets.add("t_config");
        sets.add("t_product");
        UnicastRoutingEngine unicastRoutingEngine = new UnicastRoutingEngine(shardingRule, sets);
        unicastRoutingEngine.route();
    }
}
