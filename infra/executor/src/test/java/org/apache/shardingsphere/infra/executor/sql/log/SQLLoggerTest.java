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

package org.apache.shardingsphere.infra.executor.sql.log;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.apache.shardingsphere.infra.binder.QueryContext;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionContext;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.context.SQLUnit;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public final class SQLLoggerTest {
    
    private static final String SQL = "SELECT * FROM t_user";
    
    private static List<LoggingEvent> appenderList;
    
    private final QueryContext queryContext = new QueryContext(mock(SQLStatementContext.class), SQL, Collections.emptyList());
    
    private Collection<ExecutionUnit> executionUnits;
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    @BeforeClass
    public static void setupLogger() {
        ch.qos.logback.classic.Logger log = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger("ShardingSphere-SQL");
        ListAppender<LoggingEvent> appender = (ListAppender) log.getAppender("SQLLoggerTestAppender");
        appenderList = appender.list;
    }
    
    @Before
    public void setUp() {
        executionUnits = prepareExecutionUnits(Arrays.asList("db1", "db2", "db3"));
        appenderList.clear();
    }
    
    private Collection<ExecutionUnit> prepareExecutionUnits(final Collection<String> dataSourceNames) {
        return dataSourceNames.stream().map(each -> new ExecutionUnit(each, new SQLUnit(SQL, new ArrayList<>()))).collect(Collectors.toList());
    }
    
    @Test
    public void assertLogNormalSQLWithoutParameter() {
        SQLLogger.logSQL(queryContext, false, new ExecutionContext(queryContext, executionUnits, mock(RouteContext.class)));
        assertThat(appenderList.size(), is(5));
        assertTrue(appenderList.stream().allMatch(loggingEvent -> Level.INFO == loggingEvent.getLevel()));
        assertThat(appenderList.get(0).getFormattedMessage(), is("Logic SQL: SELECT * FROM t_user"));
        assertThat(appenderList.get(1).getFormattedMessage(), is("SQLStatement: null"));
        assertThat(appenderList.get(2).getFormattedMessage(), is("Actual SQL: db1 ::: SELECT * FROM t_user"));
        assertThat(appenderList.get(3).getFormattedMessage(), is("Actual SQL: db2 ::: SELECT * FROM t_user"));
        assertThat(appenderList.get(4).getFormattedMessage(), is("Actual SQL: db3 ::: SELECT * FROM t_user"));
    }
    
    @Test
    public void assertLogNormalSQLWithParameters() {
        executionUnits.forEach(each -> each.getSqlUnit().getParameters().add("parameter"));
        SQLLogger.logSQL(queryContext, false, new ExecutionContext(queryContext, executionUnits, mock(RouteContext.class)));
        assertThat(appenderList.size(), is(5));
        assertTrue(appenderList.stream().allMatch(loggingEvent -> Level.INFO == loggingEvent.getLevel()));
        assertThat(appenderList.get(0).getFormattedMessage(), is("Logic SQL: SELECT * FROM t_user"));
        assertThat(appenderList.get(1).getFormattedMessage(), is("SQLStatement: null"));
        assertThat(appenderList.get(2).getFormattedMessage(), is("Actual SQL: db1 ::: SELECT * FROM t_user ::: [parameter]"));
        assertThat(appenderList.get(3).getFormattedMessage(), is("Actual SQL: db2 ::: SELECT * FROM t_user ::: [parameter]"));
        assertThat(appenderList.get(4).getFormattedMessage(), is("Actual SQL: db3 ::: SELECT * FROM t_user ::: [parameter]"));
    }
    
    @Test
    public void assertLogSimpleSQL() {
        SQLLogger.logSQL(queryContext, true, new ExecutionContext(queryContext, executionUnits, mock(RouteContext.class)));
        assertThat(appenderList.size(), is(3));
        assertTrue(appenderList.stream().allMatch(loggingEvent -> Level.INFO == loggingEvent.getLevel()));
        assertThat(appenderList.get(0).getFormattedMessage(), is("Logic SQL: SELECT * FROM t_user"));
        assertThat(appenderList.get(1).getFormattedMessage(), is("SQLStatement: null"));
        assertThat(appenderList.get(2).getFormattedMessage(), is("Actual SQL(simple): [db3, db2, db1] ::: 3"));
    }
}
