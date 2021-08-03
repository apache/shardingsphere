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
import org.apache.shardingsphere.infra.binder.LogicSQL;
import org.apache.shardingsphere.infra.binder.statement.CommonSQLStatementContext;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLDeleteStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLInsertStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLUpdateStatement;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Method;
import java.util.Collections;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public final class SQLRouteEngineAdviceTest extends MetricsAdviceBaseTest {
    
    private final SQLRouteEngineAdvice sqlRouteEngineAdvice = new SQLRouteEngineAdvice();
    
    @Mock
    private Method route;
    
    @Test
    public void assertDataSourceDelegate() {
        MockAdviceTargetObject targetObject = new MockAdviceTargetObject();
        LogicSQL logicSQL = new LogicSQL(new CommonSQLStatementContext(new MySQLInsertStatement()), "", Collections.emptyList());
        sqlRouteEngineAdvice.beforeMethod(targetObject, route, new Object[]{logicSQL}, new MethodInvocationResult());
        FixtureWrapper wrapper = (FixtureWrapper) MetricsPool.get(MetricIds.SQL_INSERT).get();
        assertNotNull(wrapper);
        assertThat(wrapper.getFixtureValue(), org.hamcrest.Matchers.is(1.0));
        logicSQL = new LogicSQL(new CommonSQLStatementContext(new MySQLSelectStatement()), "", Collections.emptyList());
        sqlRouteEngineAdvice.beforeMethod(targetObject, route, new Object[]{logicSQL}, new MethodInvocationResult());
        wrapper = (FixtureWrapper) MetricsPool.get(MetricIds.SQL_SELECT).get();
        assertNotNull(wrapper);
        assertThat(wrapper.getFixtureValue(), org.hamcrest.Matchers.is(1.0));
        logicSQL = new LogicSQL(new CommonSQLStatementContext(new MySQLUpdateStatement()), "", Collections.emptyList());
        sqlRouteEngineAdvice.beforeMethod(targetObject, route, new Object[]{logicSQL}, new MethodInvocationResult());
        wrapper = (FixtureWrapper) MetricsPool.get(MetricIds.SQL_UPDATE).get();
        assertNotNull(wrapper);
        assertThat(wrapper.getFixtureValue(), org.hamcrest.Matchers.is(1.0));
        logicSQL = new LogicSQL(new CommonSQLStatementContext(new MySQLDeleteStatement()), "", Collections.emptyList());
        sqlRouteEngineAdvice.beforeMethod(targetObject, route, new Object[]{logicSQL}, new MethodInvocationResult());
        wrapper = (FixtureWrapper) MetricsPool.get(MetricIds.SQL_DELETE).get();
        assertNotNull(wrapper);
        assertThat(wrapper.getFixtureValue(), org.hamcrest.Matchers.is(1.0));
        RouteContext routeContext = new RouteContext();
        RouteMapper dsMapper = new RouteMapper("logic_db", "ds_0");
        RouteMapper tbMapper = new RouteMapper("t_order", "t_order_0");
        RouteUnit routeUnit = new RouteUnit(dsMapper, Collections.singletonList(tbMapper));
        routeContext.getRouteUnits().add(routeUnit);
        MethodInvocationResult result = new MethodInvocationResult();
        result.rebase(routeContext);
        sqlRouteEngineAdvice.afterMethod(targetObject, route, new Object[]{}, result);
        wrapper = (FixtureWrapper) MetricsPool.get(MetricIds.ROUTE_DATASOURCE).get();
        assertNotNull(wrapper);
        assertThat(wrapper.getFixtureValue(), org.hamcrest.Matchers.is(1.0));
        wrapper = (FixtureWrapper) MetricsPool.get(MetricIds.ROUTE_TABLE).get();
        assertNotNull(wrapper);
        assertThat(wrapper.getFixtureValue(), org.hamcrest.Matchers.is(1.0));
    }
}
