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
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.CommonSQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.ddl.AlterViewStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.ddl.CreateViewStatementContext;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.SQLStatementAttributes;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.type.CursorSQLStatementAttribute;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.view.DropViewStatement;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BroadcastUnicastRouteEngineTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Mock
    private BroadcastRule rule;
    
    @Mock
    private ConnectionContext connectionContext;
    
    @BeforeEach
    void setUp() {
        when(rule.getDataSourceNames()).thenReturn(Arrays.asList("ds_0", "ds_1"));
    }
    
    @Test
    void assertRouteToFirstDataSourceWithCursorStatement() {
        SQLStatement sqlStatement = mock(SQLStatement.class);
        when(sqlStatement.getAttributes()).thenReturn(new SQLStatementAttributes(mock(CursorSQLStatementAttribute.class)));
        SQLStatementContext sqlStatementContext = mock(SQLStatementContext.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(sqlStatement);
        assertRoute(sqlStatementContext, is("ds_0"));
    }
    
    @Test
    void assertRouteToFirstDataSourceWithCreateViewStatementContext() {
        assertRoute(mock(CreateViewStatementContext.class, RETURNS_DEEP_STUBS), is("ds_0"));
    }
    
    @Test
    void assertRouteToFirstDataSourceWithAlterViewStatementContext() {
        assertRoute(mock(AlterViewStatementContext.class, RETURNS_DEEP_STUBS), is("ds_0"));
    }
    
    @Test
    void assertRouteToFirstDataSourceWithDropViewStatementContext() {
        SQLStatementContext sqlStatementContext = new CommonSQLStatementContext(new DropViewStatement(databaseType));
        assertRoute(sqlStatementContext, is("ds_0"));
    }
    
    @Test
    void assertRouteToRandomDataSourceWithUnusedDataSources() {
        assertRoute(mock(SQLStatementContext.class, RETURNS_DEEP_STUBS), is("ds_0"), is("ds_1"));
    }
    
    @Test
    void assertRouteToRandomDataSourceWithUsedDataSources() {
        when(connectionContext.getUsedDataSourceNames()).thenReturn(Collections.singletonList("ds_2"));
        assertRoute(mock(SQLStatementContext.class, RETURNS_DEEP_STUBS), is("ds_2"));
    }
    
    @SafeVarargs
    private final void assertRoute(final SQLStatementContext sqlStatementContext, final Matcher<String>... matchers) {
        BroadcastUnicastRouteEngine engine = new BroadcastUnicastRouteEngine(sqlStatementContext, Collections.singleton("foo_tbl"), connectionContext);
        RouteContext actual = engine.route(rule);
        assertThat(actual.getRouteUnits().size(), is(1));
        RouteMapper actualDataSourceRouteMapper = actual.getRouteUnits().iterator().next().getDataSourceMapper();
        assertThat(actualDataSourceRouteMapper.getLogicName(), anyOf(matchers));
        Collection<RouteMapper> actualTableRouteMappers = actual.getRouteUnits().iterator().next().getTableMappers();
        assertTableRouteMapper(actualTableRouteMappers);
    }
    
    private void assertTableRouteMapper(final Collection<RouteMapper> actual) {
        assertThat(actual.size(), is(1));
        RouteMapper tableRouteMapper = actual.iterator().next();
        assertThat(tableRouteMapper.getLogicName(), is("foo_tbl"));
        assertThat(tableRouteMapper.getActualName(), is("foo_tbl"));
    }
    
    @Test
    void assertRouteWithEmptyTables() {
        BroadcastUnicastRouteEngine engine = new BroadcastUnicastRouteEngine(mock(SQLStatementContext.class, RETURNS_DEEP_STUBS), Collections.emptyList(), connectionContext);
        RouteContext actual = engine.route(rule);
        assertThat(actual.getRouteUnits().size(), is(1));
        Collection<RouteMapper> actualTableRouteMappers = actual.getRouteUnits().iterator().next().getTableMappers();
        assertTrue(actualTableRouteMappers.isEmpty());
    }
}
