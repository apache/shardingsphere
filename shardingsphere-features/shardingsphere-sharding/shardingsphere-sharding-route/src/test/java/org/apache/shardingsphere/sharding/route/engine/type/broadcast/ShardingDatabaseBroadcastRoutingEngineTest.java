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

package org.apache.shardingsphere.sharding.route.engine.type.broadcast;

import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.infra.route.context.RouteResult;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class ShardingDatabaseBroadcastRoutingEngineTest {
    
    private final ShardingDatabaseBroadcastRoutingEngine shardingDatabaseBroadcastRoutingEngine = new ShardingDatabaseBroadcastRoutingEngine();
    
    @Test
    public void assertRoute() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getTables().add(new ShardingTableRuleConfiguration("t_order", "ds${0..1}.t_order_${0..2}"));
        RouteResult routeResult = shardingDatabaseBroadcastRoutingEngine.route(new ShardingRule(shardingRuleConfig, Arrays.asList("ds0", "ds1")));
        List<RouteUnit> routeUnits = new ArrayList<>(routeResult.getRouteUnits());
        assertThat(routeResult, instanceOf(RouteResult.class));
        assertThat(routeResult.getRouteUnits().size(), is(2));
        assertThat(routeUnits.get(0).getDataSourceMapper().getActualName(), is("ds0"));
        assertThat(routeUnits.get(1).getDataSourceMapper().getActualName(), is("ds1"));
    }
}
