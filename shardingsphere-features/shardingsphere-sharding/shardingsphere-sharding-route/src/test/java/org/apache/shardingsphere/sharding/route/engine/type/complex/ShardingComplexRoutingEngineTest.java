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

import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.sharding.route.fixture.AbstractRoutingEngineTest;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public final class ShardingComplexRoutingEngineTest extends AbstractRoutingEngineTest {
    
    @Test
    public void assertRoutingForBindingTables() {
        ShardingComplexRoutingEngine complexRoutingEngine = new ShardingComplexRoutingEngine(Arrays.asList("t_order", "t_order_item"), 
                createShardingConditions("t_order"), new ConfigurationProperties(new Properties()));
        RouteContext routeContext = new RouteContext();
        complexRoutingEngine.route(routeContext, createBindingShardingRule());
        List<RouteUnit> routeUnits = new ArrayList<>(routeContext.getRouteUnits());
        assertThat(routeContext.getRouteUnits().size(), is(1));
        assertThat(routeUnits.get(0).getDataSourceMapper().getActualName(), is("ds_1"));
        assertThat(routeUnits.get(0).getTableMappers().size(), is(1));
        assertThat(routeUnits.get(0).getTableMappers().iterator().next().getActualName(), is("t_order_1"));
        assertThat(routeUnits.get(0).getTableMappers().iterator().next().getLogicName(), is("t_order"));
    }
    
    @Test
    public void assertRoutingForShardingTableJoinBroadcastTable() {
        ShardingComplexRoutingEngine complexRoutingEngine = new ShardingComplexRoutingEngine(Arrays.asList("t_order", "t_config"),
                createShardingConditions("t_order"), new ConfigurationProperties(new Properties()));
        RouteContext routeContext = new RouteContext();
        complexRoutingEngine.route(routeContext, createBroadcastShardingRule());
        List<RouteUnit> routeUnits = new ArrayList<>(routeContext.getRouteUnits());
        assertThat(routeContext.getRouteUnits().size(), is(1));
        assertThat(routeUnits.get(0).getDataSourceMapper().getActualName(), is("ds_1"));
        assertThat(routeUnits.get(0).getTableMappers().size(), is(1));
        assertThat(routeUnits.get(0).getTableMappers().iterator().next().getActualName(), is("t_order_1"));
        assertThat(routeUnits.get(0).getTableMappers().iterator().next().getLogicName(), is("t_order"));
    }
    
    @Test(expected = ShardingSphereException.class)
    public void assertRoutingForNonLogicTable() {
        ShardingComplexRoutingEngine complexRoutingEngine = new ShardingComplexRoutingEngine(Collections.emptyList(), 
                createShardingConditions("t_order"), new ConfigurationProperties(new Properties()));
        RouteContext routeContext = new RouteContext();
        complexRoutingEngine.route(routeContext, mock(ShardingRule.class));
    }
}
