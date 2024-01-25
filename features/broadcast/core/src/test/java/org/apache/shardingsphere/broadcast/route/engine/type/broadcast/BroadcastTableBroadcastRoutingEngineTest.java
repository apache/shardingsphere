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

package org.apache.shardingsphere.broadcast.route.engine.type.broadcast;

import org.apache.shardingsphere.broadcast.rule.BroadcastRule;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BroadcastTableBroadcastRoutingEngineTest {
    
    @Test
    void assertRouteWithBroadcastRuleTable() {
        Collection<String> broadcastRuleTableNames = Collections.singleton("t_address");
        BroadcastTableBroadcastRoutingEngine engine = new BroadcastTableBroadcastRoutingEngine(broadcastRuleTableNames);
        BroadcastRule broadcastRule = mock(BroadcastRule.class);
        when(broadcastRule.getDataSourceNames()).thenReturn(Arrays.asList("ds_0", "ds_1"));
        when(broadcastRule.getBroadcastRuleTableNames(any())).thenReturn(Collections.singleton("t_address"));
        RouteContext routeContext = engine.route(new RouteContext(), broadcastRule);
        assertThat(routeContext.getRouteUnits().size(), is(2));
        Iterator<RouteUnit> iterator = routeContext.getRouteUnits().iterator();
        assertRouteMapper(iterator.next(), "ds_0", "t_address");
        assertRouteMapper(iterator.next(), "ds_1", "t_address");
    }
    
    @Test
    void assertRouteWithoutBroadcastRuleTable() {
        Collection<String> broadcastRuleTableNames = Collections.singleton("t_address");
        BroadcastTableBroadcastRoutingEngine engine = new BroadcastTableBroadcastRoutingEngine(broadcastRuleTableNames);
        BroadcastRule broadcastRule = mock(BroadcastRule.class);
        when(broadcastRule.getDataSourceNames()).thenReturn(Arrays.asList("ds_0", "ds_1"));
        when(broadcastRule.getBroadcastRuleTableNames(any())).thenReturn(Collections.emptyList());
        RouteContext routeContext = engine.route(new RouteContext(), broadcastRule);
        assertThat(routeContext.getRouteUnits().size(), is(2));
        Iterator<RouteUnit> iterator = routeContext.getRouteUnits().iterator();
        assertRouteMapper(iterator.next(), "ds_0", "");
        assertRouteMapper(iterator.next(), "ds_1", "");
    }
    
    private void assertRouteMapper(final RouteUnit routeUnit, final String expectedDataSourceName, final String expectedTableName) {
        assertThat(routeUnit.getDataSourceMapper().getLogicName(), is(expectedDataSourceName));
        assertThat(routeUnit.getDataSourceMapper().getActualName(), is(expectedDataSourceName));
        assertThat(routeUnit.getTableMappers().size(), is(1));
        RouteMapper tableRouteMapper = routeUnit.getTableMappers().iterator().next();
        assertThat(tableRouteMapper.getLogicName(), is(expectedTableName));
        assertThat(tableRouteMapper.getActualName(), is(expectedTableName));
    }
}
