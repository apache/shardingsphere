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
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionContext;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.context.SQLUnit;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SQLLoggerTest {
    
    private static final String SQL = "SELECT * FROM t_user";
    
    private static List<LoggingEvent> appenderList;
    
    private QueryContext queryContext;
    
    private Collection<ExecutionUnit> executionUnits;
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    @BeforeAll
    static void setupLogger() {
        ch.qos.logback.classic.Logger log = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger("org.apache.shardingsphere.sql");
        ListAppender<LoggingEvent> appender = (ListAppender) log.getAppender("SQLLoggerTestAppender");
        appenderList = appender.list;
    }
    
    @BeforeEach
    void setUp() {
        ConnectionContext connectionContext = mock(ConnectionContext.class);
        when(connectionContext.getCurrentDatabaseName()).thenReturn(Optional.of("foo_db"));
        ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class);
        when(metaData.containsDatabase("foo_db")).thenReturn(true);
        when(metaData.getDatabase("foo_db")).thenReturn(mock(ShardingSphereDatabase.class));
        SQLStatementContext sqlStatementContext = mock(SQLStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getTablesContext().getDatabaseNames()).thenReturn(Collections.emptyList());
        queryContext = new QueryContext(sqlStatementContext, SQL, Collections.emptyList(), new HintValueContext(), connectionContext, metaData);
        executionUnits = prepareExecutionUnits(Arrays.asList("db1", "db2", "db3"));
        appenderList.clear();
    }
    
    @SuppressWarnings("CollectionWithoutInitialCapacity")
    private Collection<ExecutionUnit> prepareExecutionUnits(final Collection<String> dataSourceNames) {
        return dataSourceNames.stream().map(each -> new ExecutionUnit(each, new SQLUnit(SQL, new ArrayList<>()))).collect(Collectors.toList());
    }
    
    @Test
    void assertLogNormalSQLWithoutParameter() {
        SQLLogger.logSQL(queryContext, false, new ExecutionContext(queryContext, executionUnits, mock(RouteContext.class)));
        assertThat(appenderList.size(), is(4));
        assertTrue(appenderList.stream().allMatch(loggingEvent -> Level.INFO == loggingEvent.getLevel()));
        assertThat(appenderList.get(0).getFormattedMessage(), is("Logic SQL: SELECT * FROM t_user"));
        assertThat(appenderList.get(1).getFormattedMessage(), is("Actual SQL: db1 ::: SELECT * FROM t_user"));
        assertThat(appenderList.get(2).getFormattedMessage(), is("Actual SQL: db2 ::: SELECT * FROM t_user"));
        assertThat(appenderList.get(3).getFormattedMessage(), is("Actual SQL: db3 ::: SELECT * FROM t_user"));
    }
    
    @Test
    void assertLogNormalSQLWithParameters() {
        executionUnits.forEach(each -> each.getSqlUnit().getParameters().add("parameter"));
        SQLLogger.logSQL(queryContext, false, new ExecutionContext(queryContext, executionUnits, mock(RouteContext.class)));
        assertThat(appenderList.size(), is(4));
        assertTrue(appenderList.stream().allMatch(loggingEvent -> Level.INFO == loggingEvent.getLevel()));
        assertThat(appenderList.get(0).getFormattedMessage(), is("Logic SQL: SELECT * FROM t_user"));
        assertThat(appenderList.get(1).getFormattedMessage(), is("Actual SQL: db1 ::: SELECT * FROM t_user ::: [parameter]"));
        assertThat(appenderList.get(2).getFormattedMessage(), is("Actual SQL: db2 ::: SELECT * FROM t_user ::: [parameter]"));
        assertThat(appenderList.get(3).getFormattedMessage(), is("Actual SQL: db3 ::: SELECT * FROM t_user ::: [parameter]"));
    }
    
    @Test
    void assertLogSimpleSQL() {
        SQLLogger.logSQL(queryContext, true, new ExecutionContext(queryContext, executionUnits, mock(RouteContext.class)));
        assertThat(appenderList.size(), is(2));
        assertTrue(appenderList.stream().allMatch(loggingEvent -> Level.INFO == loggingEvent.getLevel()));
        assertThat(appenderList.get(0).getFormattedMessage(), is("Logic SQL: SELECT * FROM t_user"));
        assertThat(appenderList.get(1).getFormattedMessage(), is("Actual SQL(simple): [db3, db2, db1] ::: 3"));
    }
}
