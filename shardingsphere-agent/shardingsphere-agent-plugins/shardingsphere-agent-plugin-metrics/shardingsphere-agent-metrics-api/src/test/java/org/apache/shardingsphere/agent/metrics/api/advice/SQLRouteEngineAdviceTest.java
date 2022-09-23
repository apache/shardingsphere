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

package org.apache.shardingsphere.agent.metrics.api.advice;

import org.apache.shardingsphere.agent.api.result.MethodInvocationResult;
import org.apache.shardingsphere.agent.metrics.api.MetricsPool;
import org.apache.shardingsphere.agent.metrics.api.constant.MetricIds;
import org.apache.shardingsphere.agent.metrics.api.fixture.FixtureWrapper;
import org.apache.shardingsphere.infra.binder.QueryContext;
import org.apache.shardingsphere.infra.binder.statement.CommonSQLStatementContext;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLDeleteStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLInsertStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLUpdateStatement;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public final class SQLRouteEngineAdviceTest extends MetricsAdviceBaseTest {
    
    private final SQLRouteEngineAdvice sqlRouteEngineAdvice = new SQLRouteEngineAdvice();
    
    @Test
    public void assertInsertRoute() {
        QueryContext queryContext = new QueryContext(new CommonSQLStatementContext<>(new MySQLInsertStatement()), "", Collections.emptyList());
        assertRoute(MetricIds.ROUTE_SQL_INSERT, queryContext);
    }
    
    @Test
    public void assertSelectRoute() {
        QueryContext queryContext = new QueryContext(new CommonSQLStatementContext<>(new MySQLSelectStatement()), "", Collections.emptyList());
        assertRoute(MetricIds.ROUTE_SQL_SELECT, queryContext);
    }
    
    @Test
    public void assertDeleteRoute() {
        QueryContext queryContext = new QueryContext(new CommonSQLStatementContext<>(new MySQLDeleteStatement()), "", Collections.emptyList());
        assertRoute(MetricIds.ROUTE_SQL_DELETE, queryContext);
    }
    
    @Test
    public void assertUpdateRoute() {
        QueryContext queryContext = new QueryContext(new CommonSQLStatementContext<>(new MySQLUpdateStatement()), "", Collections.emptyList());
        assertRoute(MetricIds.ROUTE_SQL_UPDATE, queryContext);
    }
    
    public void assertRoute(final String metricIds, final QueryContext queryContext) {
        MockAdviceTargetObject targetObject = new MockAdviceTargetObject();
        sqlRouteEngineAdvice.beforeMethod(targetObject, mock(Method.class), new Object[]{queryContext}, new MethodInvocationResult());
        FixtureWrapper wrapper = (FixtureWrapper) MetricsPool.get(metricIds).get();
        assertTrue(MetricsPool.get(metricIds).isPresent());
        assertThat(((FixtureWrapper) MetricsPool.get(metricIds).get()).getFixtureValue(), is(1.0));
    }
    
    @Test
    public void assertRouteDataSourceAndTable() {
        MockAdviceTargetObject targetObject = new MockAdviceTargetObject();
        RouteContext routeContext = new RouteContext();
        RouteMapper dsMapper = new RouteMapper("logic_db", "ds_0");
        RouteMapper tbMapper = new RouteMapper("t_order", "t_order_0");
        RouteUnit routeUnit = new RouteUnit(dsMapper, Collections.singletonList(tbMapper));
        routeContext.getRouteUnits().add(routeUnit);
        MethodInvocationResult result = new MethodInvocationResult();
        result.rebase(routeContext);
        sqlRouteEngineAdvice.afterMethod(targetObject, mock(Method.class), new Object[]{}, result);
        FixtureWrapper wrapper = (FixtureWrapper) MetricsPool.get(MetricIds.ROUTE_DATASOURCE).get();
        assertTrue(MetricsPool.get(MetricIds.ROUTE_DATASOURCE).isPresent());
        assertThat(wrapper.getFixtureValue(), is(1.0));
        wrapper = (FixtureWrapper) MetricsPool.get(MetricIds.ROUTE_TABLE).get();
        assertTrue(MetricsPool.get(MetricIds.ROUTE_TABLE).isPresent());
        assertThat(wrapper.getFixtureValue(), is(1.0));
    }
}
