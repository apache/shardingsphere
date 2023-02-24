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

package org.apache.shardingsphere.agent.plugin.metrics.core.advice;

import org.apache.shardingsphere.agent.plugin.metrics.core.collector.MetricsCollectorRegistry;
import org.apache.shardingsphere.agent.plugin.metrics.core.config.MetricCollectorType;
import org.apache.shardingsphere.agent.plugin.metrics.core.config.MetricConfiguration;
import org.apache.shardingsphere.agent.plugin.metrics.core.fixture.collector.MetricsCollectorFixture;
import org.apache.shardingsphere.agent.plugin.metrics.core.fixture.TargetAdviceObjectFixture;
import org.apache.shardingsphere.infra.binder.QueryContext;
import org.apache.shardingsphere.infra.binder.statement.CommonSQLStatementContext;
import org.apache.shardingsphere.infra.context.ConnectionContext;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLDeleteStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLInsertStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLUpdateStatement;
import org.junit.After;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

public final class SQLRouteCountAdviceTest {
    
    private final MetricConfiguration config = new MetricConfiguration("routed_sql_total", MetricCollectorType.COUNTER, null, Collections.singletonList("type"), Collections.emptyMap());
    
    private final SQLRouteCountAdvice advice = new SQLRouteCountAdvice();
    
    @After
    public void reset() {
        ((MetricsCollectorFixture) MetricsCollectorRegistry.get(config, "FIXTURE")).reset();
    }
    
    @Test
    public void assertInsertRoute() {
        QueryContext queryContext = new QueryContext(new CommonSQLStatementContext<>(new MySQLInsertStatement()), "", Collections.emptyList());
        assertRoute(queryContext, "INSERT=1");
    }
    
    @Test
    public void assertUpdateRoute() {
        QueryContext queryContext = new QueryContext(new CommonSQLStatementContext<>(new MySQLUpdateStatement()), "", Collections.emptyList());
        assertRoute(queryContext, "UPDATE=1");
    }
    
    @Test
    public void assertDeleteRoute() {
        QueryContext queryContext = new QueryContext(new CommonSQLStatementContext<>(new MySQLDeleteStatement()), "", Collections.emptyList());
        assertRoute(queryContext, "DELETE=1");
    }
    
    @Test
    public void assertSelectRoute() {
        QueryContext queryContext = new QueryContext(new CommonSQLStatementContext<>(new MySQLSelectStatement()), "", Collections.emptyList());
        assertRoute(queryContext, "SELECT=1");
    }
    
    public void assertRoute(final QueryContext queryContext, final String expected) {
        advice.beforeMethod(new TargetAdviceObjectFixture(), mock(Method.class), new Object[]{new ConnectionContext(), queryContext}, "FIXTURE");
        assertThat(MetricsCollectorRegistry.get(config, "FIXTURE").toString(), is(expected));
    }
}
