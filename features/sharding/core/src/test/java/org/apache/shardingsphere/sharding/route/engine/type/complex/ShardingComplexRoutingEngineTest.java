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

package org.apache.shardingsphere.sharding.route.engine.type.complex;

import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.sharding.exception.metadata.ShardingTableRuleNotFoundException;
import org.apache.shardingsphere.sharding.route.engine.fixture.ShardingRoutingEngineFixtureBuilder;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class ShardingComplexRoutingEngineTest {
    
    @Test
    void assertRoutingForBindingTables() {
        ShardingComplexRoutingEngine complexRoutingEngine = new ShardingComplexRoutingEngine(ShardingRoutingEngineFixtureBuilder.createShardingConditions("t_order"),
                mock(SQLStatementContext.class), new HintValueContext(), new ConfigurationProperties(new Properties()), Arrays.asList("t_order", "t_order_item"));
        RouteContext routeContext = complexRoutingEngine.route(ShardingRoutingEngineFixtureBuilder.createBindingShardingRule());
        List<RouteUnit> routeUnits = new ArrayList<>(routeContext.getRouteUnits());
        assertThat(routeContext.getRouteUnits().size(), is(1));
        assertThat(routeUnits.get(0).getDataSourceMapper().getActualName(), is("ds_1"));
        assertThat(routeUnits.get(0).getTableMappers().size(), is(1));
        assertThat(routeUnits.get(0).getTableMappers().iterator().next().getActualName(), is("t_order_1"));
        assertThat(routeUnits.get(0).getTableMappers().iterator().next().getLogicName(), is("t_order"));
    }
    
    @Test
    void assertRoutingForShardingTableJoinBroadcastTable() {
        ShardingComplexRoutingEngine complexRoutingEngine = new ShardingComplexRoutingEngine(ShardingRoutingEngineFixtureBuilder.createShardingConditions("t_order"),
                mock(SQLStatementContext.class), new HintValueContext(), new ConfigurationProperties(new Properties()), Arrays.asList("t_order", "t_config"));
        RouteContext routeContext = complexRoutingEngine.route(ShardingRoutingEngineFixtureBuilder.createBroadcastShardingRule());
        List<RouteUnit> routeUnits = new ArrayList<>(routeContext.getRouteUnits());
        assertThat(routeContext.getRouteUnits().size(), is(1));
        assertThat(routeUnits.get(0).getDataSourceMapper().getActualName(), is("ds_1"));
        assertThat(routeUnits.get(0).getTableMappers().size(), is(1));
        assertThat(routeUnits.get(0).getTableMappers().iterator().next().getActualName(), is("t_order_1"));
        assertThat(routeUnits.get(0).getTableMappers().iterator().next().getLogicName(), is("t_order"));
    }
    
    @Test
    void assertRoutingForNonLogicTable() {
        ShardingComplexRoutingEngine complexRoutingEngine = new ShardingComplexRoutingEngine(ShardingRoutingEngineFixtureBuilder.createShardingConditions("t_order"),
                mock(SQLStatementContext.class), new HintValueContext(), new ConfigurationProperties(new Properties()), Collections.emptyList());
        assertThrows(ShardingTableRuleNotFoundException.class, () -> complexRoutingEngine.route(mock(ShardingRule.class)));
    }
}
