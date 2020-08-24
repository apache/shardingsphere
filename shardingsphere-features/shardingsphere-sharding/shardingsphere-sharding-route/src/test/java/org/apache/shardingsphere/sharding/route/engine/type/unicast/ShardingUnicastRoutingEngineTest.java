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

package org.apache.shardingsphere.sharding.route.engine.type.unicast;

import org.apache.shardingsphere.infra.config.exception.ShardingSphereConfigurationException;
import org.apache.shardingsphere.infra.route.context.RouteResult;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

public final class ShardingUnicastRoutingEngineTest {
    
    private ShardingRule shardingRule;
    
    @Before
    public void setUp() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getTables().add(new ShardingTableRuleConfiguration("t_order", "ds${0..1}.t_order_${0..2}"));
        shardingRuleConfig.getBroadcastTables().add("t_config");
        shardingRule = new ShardingRule(shardingRuleConfig, Arrays.asList("ds0", "ds1", "ds2"));
    }
    
    @Test
    public void assertRoutingForShardingTable() {
        ShardingUnicastRoutingEngine unicastRoutingEngine = new ShardingUnicastRoutingEngine(Collections.singleton("t_order"));
        RouteResult routeResult = unicastRoutingEngine.route(shardingRule);
        assertThat(routeResult, instanceOf(RouteResult.class));
        assertThat(routeResult.getRouteUnits().size(), is(1));
        assertFalse(routeResult.getRouteUnits().iterator().next().getDataSourceMapper().getLogicName().equalsIgnoreCase("ds2"));
    }

    @Test
    public void assertRoutingForBroadcastTable() {
        ShardingUnicastRoutingEngine unicastRoutingEngine = new ShardingUnicastRoutingEngine(Collections.singleton("t_config"));
        RouteResult routeResult = unicastRoutingEngine.route(shardingRule);
        assertThat(routeResult, instanceOf(RouteResult.class));
        assertThat(routeResult.getRouteUnits().size(), is(1));
    }

    @Test
    public void assertRoutingForNoTable() {
        ShardingUnicastRoutingEngine unicastRoutingEngine = new ShardingUnicastRoutingEngine(Collections.emptyList());
        RouteResult routeResult = unicastRoutingEngine.route(shardingRule);
        assertThat(routeResult, instanceOf(RouteResult.class));
        assertThat(routeResult.getRouteUnits().size(), is(1));
    }
    
    @Test
    public void assertRoutingForShardingTableAndBroadcastTable() {
        Set<String> sets = new HashSet<>();
        sets.add("t_order");
        sets.add("t_config");
        ShardingUnicastRoutingEngine unicastRoutingEngine = new ShardingUnicastRoutingEngine(sets);
        RouteResult routeResult = unicastRoutingEngine.route(shardingRule);
        assertThat(routeResult, instanceOf(RouteResult.class));
        assertThat(routeResult.getRouteUnits().size(), is(1));
    }
    
    @Test(expected = ShardingSphereConfigurationException.class)
    public void assertRouteForWithNoIntersection() {
        Set<String> sets = new HashSet<>();
        sets.add("t_order");
        sets.add("t_config");
        sets.add("t_product");
        ShardingUnicastRoutingEngine unicastRoutingEngine = new ShardingUnicastRoutingEngine(sets);
        unicastRoutingEngine.route(shardingRule);
    }
    
    @Test
    public void assertRoutingForTableWithoutTableRule() {
        ShardingUnicastRoutingEngine unicastRoutingEngine = new ShardingUnicastRoutingEngine(Collections.singleton("t_other"));
        RouteResult routeResult = unicastRoutingEngine.route(shardingRule);
        assertThat(routeResult, instanceOf(RouteResult.class));
        assertThat(routeResult.getRouteUnits().size(), is(1));
    }
}
