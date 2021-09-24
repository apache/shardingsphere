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

import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.sharding.route.engine.condition.ShardingConditions;
import org.apache.shardingsphere.sharding.route.engine.fixture.AbstractRoutingEngineTest;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Sharding federated routing engine test.
 */
public final class ShardingFederatedRoutingEngineTest extends AbstractRoutingEngineTest {
    
    @Test
    public void assertRouteByNonConditions() {
        ShardingFederatedRoutingEngine federatedRoutingEngine = createShardingFederatedRoutingEngine(Arrays.asList("t_order"),
                new ShardingConditions(Collections.emptyList(), mock(SQLStatementContext.class), mock(ShardingRule.class)));
        RouteContext routeContext = federatedRoutingEngine.route(createBasedShardingRule());
        List<RouteUnit> routeUnits = new ArrayList<>(routeContext.getRouteUnits());
        assertThat(routeContext.getRouteUnits().size(), is(2));
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
        ShardingFederatedRoutingEngine federatedRoutingEngine = createShardingFederatedRoutingEngine(Arrays.asList("t_order"), createShardingConditions("t_order"));
        RouteContext routeContext = federatedRoutingEngine.route(createBasedShardingRule());
        List<RouteUnit> routeUnits = new ArrayList<>(routeContext.getRouteUnits());
        assertThat(routeContext.getRouteUnits().size(), is(1));
        assertThat(routeUnits.get(0).getDataSourceMapper().getActualName(), is("ds_1"));
        assertThat(routeUnits.get(0).getTableMappers().size(), is(1));
        assertThat(routeUnits.get(0).getTableMappers().iterator().next().getActualName(), is("t_order_1"));
        assertThat(routeUnits.get(0).getTableMappers().iterator().next().getLogicName(), is("t_order"));
    }
    
    @Test
    public void assertRoutingForBindingTables() {
        ShardingFederatedRoutingEngine federatedRoutingEngine = createShardingFederatedRoutingEngine(Arrays.asList("t_order", "t_order_item"), createShardingConditions("t_order"));
        RouteContext routeContext = federatedRoutingEngine.route(createBindingShardingRule());
        List<RouteUnit> routeUnits = new ArrayList<>(routeContext.getRouteUnits());
        assertThat(routeContext.getRouteUnits().size(), is(1));
        RouteUnit routeUnit = routeUnits.get(0);
        assertThat(routeUnit.getDataSourceMapper().getActualName(), is("ds_1"));
        assertThat(routeUnit.getDataSourceMapper().getLogicName(), is("ds_1"));
        assertThat(routeUnit.getTableMappers().size(), is(2));
        Collection<RouteMapper> tableMappers = routeUnit.getTableMappers();
        Iterator<RouteMapper> iterator = tableMappers.iterator();
        RouteMapper tableMapper1 = iterator.next();
        assertThat(tableMapper1.getActualName(), is("t_order_1"));
        assertThat(tableMapper1.getLogicName(), is("t_order"));
        RouteMapper tableMapper2 = iterator.next();
        assertThat(tableMapper2.getActualName(), is("t_order_item_1"));
        assertThat(tableMapper2.getLogicName(), is("t_order_item"));
    }
    
    @Test
    public void assertRoutingForNonLogicTable() {
        ShardingFederatedRoutingEngine complexRoutingEngine = createShardingFederatedRoutingEngine(Collections.emptyList(), createShardingConditions("t_order"));
        RouteContext routeContext = complexRoutingEngine.route(mock(ShardingRule.class));
        assertThat(routeContext.getOriginalDataNodes().size(), is(0));
        assertThat(routeContext.getRouteUnits().size(), is(0));
        assertThat(routeContext.getRouteStageContexts().size(), is(0));
        assertThat(routeContext.isFederated(), is(true));
    }
    
    @Test
    public void assertRoutingForShardingTableJoinBroadcastTable() {
        ShardingFederatedRoutingEngine federatedRoutingEngine = createShardingFederatedRoutingEngine(Arrays.asList("t_config"), mock(ShardingConditions.class));
        RouteContext routeContext = federatedRoutingEngine.route(createBroadcastShardingRule());
        List<RouteUnit> routeUnits = new ArrayList<>(routeContext.getRouteUnits());
        assertThat(routeContext.getRouteUnits().size(), is(1));
        assertThat(routeUnits.get(0).getTableMappers().size(), is(1));
        assertThat(routeUnits.get(0).getTableMappers().iterator().next().getActualName(), is("t_config"));
        assertThat(routeUnits.get(0).getTableMappers().iterator().next().getLogicName(), is("t_config"));
    }
    
    private ShardingFederatedRoutingEngine createShardingFederatedRoutingEngine(final Collection<String> logicTables, final ShardingConditions shardingConditions) {
        return new ShardingFederatedRoutingEngine(logicTables, shardingConditions, new ConfigurationProperties(new Properties()));
    }
}
