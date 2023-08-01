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

package org.apache.shardingsphere.broadcast.route.engine.type.unicast;

import org.apache.shardingsphere.broadcast.rule.BroadcastRule;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.ddl.CreateViewStatementContext;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BroadcastUnicastRoutingEngineTest {
    
    private BroadcastRule broadcastRule;
    
    @BeforeEach
    void setUp() {
        broadcastRule = mock(BroadcastRule.class);
        when(broadcastRule.getAvailableDataSourceNames()).thenReturn(Arrays.asList("ds_0", "ds_1"));
    }
    
    @Test
    void assertRoute() {
        SQLStatementContext sqlStatementContext = mock(SQLStatementContext.class);
        Collection<String> logicTables = Collections.singleton("t_address");
        ConnectionContext connectionContext = mock(ConnectionContext.class);
        BroadcastUnicastRoutingEngine engine = new BroadcastUnicastRoutingEngine(sqlStatementContext, logicTables, connectionContext);
        RouteContext routeContext = engine.route(new RouteContext(), broadcastRule);
        assertThat(routeContext.getRouteUnits().size(), is(1));
        assertTableRouteMapper(routeContext);
    }
    
    @Test
    void assertRouteWithCreateViewStatementContext() {
        CreateViewStatementContext sqlStatementContext = mock(CreateViewStatementContext.class);
        Collection<String> logicTables = Collections.singleton("t_address");
        ConnectionContext connectionContext = mock(ConnectionContext.class);
        BroadcastUnicastRoutingEngine engine = new BroadcastUnicastRoutingEngine(sqlStatementContext, logicTables, connectionContext);
        RouteContext routeContext = engine.route(new RouteContext(), broadcastRule);
        assertThat(routeContext.getRouteUnits().size(), is(1));
        RouteMapper dataSourceRouteMapper = routeContext.getRouteUnits().iterator().next().getDataSourceMapper();
        assertThat(dataSourceRouteMapper.getLogicName(), is("ds_0"));
        assertTableRouteMapper(routeContext);
    }
    
    @Test
    void assertRouteWithCursorStatement() {
        CreateViewStatementContext sqlStatementContext = mock(CreateViewStatementContext.class);
        Collection<String> logicTables = Collections.singleton("t_address");
        ConnectionContext connectionContext = mock(ConnectionContext.class);
        BroadcastUnicastRoutingEngine engine = new BroadcastUnicastRoutingEngine(sqlStatementContext, logicTables, connectionContext);
        RouteContext routeContext = engine.route(new RouteContext(), broadcastRule);
        assertThat(routeContext.getRouteUnits().size(), is(1));
        RouteMapper dataSourceRouteMapper = routeContext.getRouteUnits().iterator().next().getDataSourceMapper();
        assertThat(dataSourceRouteMapper.getLogicName(), is("ds_0"));
        assertTableRouteMapper(routeContext);
    }
    
    private void assertTableRouteMapper(final RouteContext routeContext) {
        Collection<RouteMapper> tableRouteMappers = routeContext.getRouteUnits().iterator().next().getTableMappers();
        assertThat(tableRouteMappers.size(), is(1));
        RouteMapper tableRouteMapper = tableRouteMappers.iterator().next();
        assertThat(tableRouteMapper.getLogicName(), is("t_address"));
        assertThat(tableRouteMapper.getActualName(), is("t_address"));
    }
}
