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

package org.apache.shardingsphere.sharding.route.engine.type.federated;

import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.sharding.route.engine.fixture.AbstractRoutingEngineTest;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public final class ShardingFederatedRoutingEngineTest extends AbstractRoutingEngineTest {
    
    @Test
    public void assertRouteByNonConditions() {
        ShardingFederatedRoutingEngine federatedRoutingEngine = createShardingFederatedRoutingEngine(Collections.singletonList("t_order"));
        RouteContext actual = federatedRoutingEngine.route(createBasedShardingRule());
        List<RouteUnit> routeUnits = new ArrayList<>(actual.getRouteUnits());
        assertThat(actual.getRouteUnits().size(), is(2));
        RouteUnit routeUnit1 = routeUnits.get(0);
        assertThat(routeUnit1.getDataSourceMapper().getActualName(), is("ds_0"));
        assertThat(routeUnit1.getTableMappers().size(), is(2));
        Collection<RouteMapper> tableMappers1 = routeUnit1.getTableMappers();
        Iterator<RouteMapper> iterator1 = tableMappers1.iterator();
        RouteMapper tableMapper1 = iterator1.next();
        assertThat(tableMapper1.getActualName(), is("t_order_0"));
        assertThat(tableMapper1.getLogicName(), is("t_order"));
        RouteMapper tableMapper2 = iterator1.next();
        assertThat(tableMapper2.getActualName(), is("t_order_1"));
        assertThat(tableMapper2.getLogicName(), is("t_order"));
        RouteUnit routeUnit2 = routeUnits.get(1);
        assertThat(routeUnit2.getDataSourceMapper().getActualName(), is("ds_1"));
        assertThat(routeUnit2.getTableMappers().size(), is(2));
        Collection<RouteMapper> tableMappers2 = routeUnit2.getTableMappers();
        Iterator<RouteMapper> iterator2 = tableMappers2.iterator();
        RouteMapper tableMapper3 = iterator2.next();
        assertThat(tableMapper3.getActualName(), is("t_order_0"));
        assertThat(tableMapper3.getLogicName(), is("t_order"));
        RouteMapper tableMapper4 = iterator2.next();
        assertThat(tableMapper4.getActualName(), is("t_order_1"));
        assertThat(tableMapper4.getLogicName(), is("t_order"));
    }
    
    @Test
    public void assertRouteByShardingConditions() {
        ShardingFederatedRoutingEngine federatedRoutingEngine = createShardingFederatedRoutingEngine(Collections.singletonList("t_order"));
        RouteContext actual = federatedRoutingEngine.route(createBasedShardingRule());
        assertThat(actual.getRouteUnits().size(), is(2));
        List<RouteUnit> routeUnits = new ArrayList<>(actual.getRouteUnits());
        assertThat(routeUnits.get(0).getDataSourceMapper().getActualName(), is("ds_0"));
        assertThat(routeUnits.get(0).getDataSourceMapper().getLogicName(), is("ds_0"));
        assertThat(routeUnits.get(0).getTableMappers().size(), is(2));
        List<RouteMapper> firstRouteMappers = new ArrayList<>(routeUnits.get(0).getTableMappers());
        assertThat(firstRouteMappers.get(0).getActualName(), is("t_order_0"));
        assertThat(firstRouteMappers.get(0).getLogicName(), is("t_order"));
        assertThat(firstRouteMappers.get(1).getActualName(), is("t_order_1"));
        assertThat(firstRouteMappers.get(1).getLogicName(), is("t_order"));
        assertThat(routeUnits.get(1).getDataSourceMapper().getActualName(), is("ds_1"));
        assertThat(routeUnits.get(1).getDataSourceMapper().getLogicName(), is("ds_1"));
        assertThat(routeUnits.get(1).getTableMappers().size(), is(2));
        List<RouteMapper> secondRouteMappers = new ArrayList<>(routeUnits.get(1).getTableMappers());
        assertThat(secondRouteMappers.get(0).getActualName(), is("t_order_0"));
        assertThat(secondRouteMappers.get(0).getLogicName(), is("t_order"));
        assertThat(secondRouteMappers.get(1).getActualName(), is("t_order_1"));
        assertThat(secondRouteMappers.get(1).getLogicName(), is("t_order"));
    }
    
    @Test
    public void assertRoutingForBindingTables() {
        ShardingFederatedRoutingEngine federatedRoutingEngine = createShardingFederatedRoutingEngine(Arrays.asList("t_order", "t_order_item"));
        RouteContext actual = federatedRoutingEngine.route(createBindingShardingRule());
        assertThat(actual.getRouteUnits().size(), is(2));
        List<RouteUnit> routeUnits = new ArrayList<>(actual.getRouteUnits());
        assertThat(routeUnits.get(0).getDataSourceMapper().getActualName(), is("ds_0"));
        assertThat(routeUnits.get(0).getDataSourceMapper().getLogicName(), is("ds_0"));
        assertThat(routeUnits.get(0).getTableMappers().size(), is(4));
        List<RouteMapper> firstRouteMappers = new ArrayList<>(routeUnits.get(0).getTableMappers());
        assertThat(firstRouteMappers.get(0).getActualName(), is("t_order_0"));
        assertThat(firstRouteMappers.get(0).getLogicName(), is("t_order"));
        assertThat(firstRouteMappers.get(1).getActualName(), is("t_order_1"));
        assertThat(firstRouteMappers.get(1).getLogicName(), is("t_order"));
        assertThat(firstRouteMappers.get(2).getActualName(), is("t_order_item_0"));
        assertThat(firstRouteMappers.get(2).getLogicName(), is("t_order_item"));
        assertThat(firstRouteMappers.get(3).getActualName(), is("t_order_item_1"));
        assertThat(firstRouteMappers.get(3).getLogicName(), is("t_order_item"));
        assertThat(routeUnits.get(1).getDataSourceMapper().getActualName(), is("ds_1"));
        assertThat(routeUnits.get(1).getDataSourceMapper().getLogicName(), is("ds_1"));
        assertThat(routeUnits.get(1).getTableMappers().size(), is(4));
        List<RouteMapper> secondRouteMappers = new ArrayList<>(routeUnits.get(1).getTableMappers());
        assertThat(secondRouteMappers.get(0).getActualName(), is("t_order_0"));
        assertThat(secondRouteMappers.get(0).getLogicName(), is("t_order"));
        assertThat(secondRouteMappers.get(1).getActualName(), is("t_order_1"));
        assertThat(secondRouteMappers.get(1).getLogicName(), is("t_order"));
        assertThat(secondRouteMappers.get(2).getActualName(), is("t_order_item_0"));
        assertThat(secondRouteMappers.get(2).getLogicName(), is("t_order_item"));
        assertThat(secondRouteMappers.get(3).getActualName(), is("t_order_item_1"));
        assertThat(secondRouteMappers.get(3).getLogicName(), is("t_order_item"));
    }
    
    @Test
    public void assertRoutingForNonLogicTable() {
        ShardingFederatedRoutingEngine complexRoutingEngine = createShardingFederatedRoutingEngine(Collections.emptyList());
        RouteContext actual = complexRoutingEngine.route(mock(ShardingRule.class));
        assertThat(actual.getOriginalDataNodes().size(), is(0));
        assertThat(actual.getRouteUnits().size(), is(0));
        assertThat(actual.getRouteStageContexts().size(), is(0));
        assertThat(actual.isFederated(), is(true));
    }
    
    @Test
    public void assertRoutingForShardingTableJoinBroadcastTable() {
        ShardingFederatedRoutingEngine federatedRoutingEngine = createShardingFederatedRoutingEngine(Collections.singletonList("t_config"));
        RouteContext actual = federatedRoutingEngine.route(createBroadcastShardingRule());
        List<RouteUnit> routeUnits = new ArrayList<>(actual.getRouteUnits());
        assertThat(actual.getRouteUnits().size(), is(2));
        assertThat(routeUnits.get(0).getDataSourceMapper().getActualName(), is("ds_0"));
        assertThat(routeUnits.get(0).getDataSourceMapper().getLogicName(), is("ds_0"));
        assertThat(routeUnits.get(0).getTableMappers().size(), is(1));
        List<RouteMapper> firstRouteMappers = new ArrayList<>(routeUnits.get(0).getTableMappers());
        assertThat(firstRouteMappers.get(0).getActualName(), is("t_config"));
        assertThat(firstRouteMappers.get(0).getLogicName(), is("t_config"));
        assertThat(routeUnits.get(1).getDataSourceMapper().getActualName(), is("ds_1"));
        assertThat(routeUnits.get(1).getDataSourceMapper().getLogicName(), is("ds_1"));
        assertThat(routeUnits.get(1).getTableMappers().size(), is(1));
        List<RouteMapper> secondRouteMappers = new ArrayList<>(routeUnits.get(0).getTableMappers());
        assertThat(secondRouteMappers.get(0).getActualName(), is("t_config"));
        assertThat(secondRouteMappers.get(0).getLogicName(), is("t_config"));
    }
    
    private ShardingFederatedRoutingEngine createShardingFederatedRoutingEngine(final Collection<String> logicTables) {
        return new ShardingFederatedRoutingEngine(logicTables);
    }
}
