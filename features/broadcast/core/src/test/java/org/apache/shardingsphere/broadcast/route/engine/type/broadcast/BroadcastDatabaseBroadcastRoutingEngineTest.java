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
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Iterator;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BroadcastDatabaseBroadcastRoutingEngineTest {
    
    @Test
    void assertRoute() {
        BroadcastRule broadcastRule = mock(BroadcastRule.class);
        when(broadcastRule.getDataSourceNames()).thenReturn(Arrays.asList("ds_0", "ds_1"));
        BroadcastDatabaseBroadcastRoutingEngine engine = new BroadcastDatabaseBroadcastRoutingEngine();
        RouteContext routeContext = engine.route(new RouteContext(), broadcastRule);
        assertThat(routeContext.getRouteUnits().size(), is(2));
        Iterator<RouteUnit> iterator = routeContext.getRouteUnits().iterator();
        assertDataSourceRouteMapper(iterator.next(), "ds_0");
        assertDataSourceRouteMapper(iterator.next(), "ds_1");
    }
    
    private void assertDataSourceRouteMapper(final RouteUnit routeUnit, final String expected) {
        assertThat(routeUnit.getDataSourceMapper().getLogicName(), is(expected));
        assertThat(routeUnit.getDataSourceMapper().getActualName(), is(expected));
    }
}
