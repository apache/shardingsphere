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

package org.apache.shardingsphere.driver.executor.engine;

import org.apache.shardingsphere.driver.executor.callback.add.StatementAddCallback;
import org.apache.shardingsphere.driver.executor.callback.replay.StatementReplayCallback;
import org.apache.shardingsphere.driver.executor.engine.pushdown.jdbc.DriverJDBCPushDownExecuteQueryExecutor;
import org.apache.shardingsphere.driver.executor.engine.pushdown.raw.DriverRawPushDownExecuteQueryExecutor;
import org.apache.shardingsphere.driver.jdbc.core.connection.ShardingSphereConnection;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutor;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.raw.RawExecutor;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.DriverExecutionPrepareEngine;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rule.attribute.raw.RawExecutionRuleAttribute;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.sqlfederation.engine.SQLFederationEngine;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class DriverExecuteQueryExecutorTest {
    
    @Test
    void assertExecuteQueryWithFederation() throws SQLException {
        ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class, RETURNS_DEEP_STUBS);
        SQLFederationEngine sqlFederationEngine = mock(SQLFederationEngine.class);
        QueryContext queryContext = mock(QueryContext.class, RETURNS_DEEP_STUBS);
        when(sqlFederationEngine.decide(queryContext, metaData.getGlobalRuleMetaData())).thenReturn(true);
        ResultSet expected = mock(ResultSet.class);
        when(sqlFederationEngine.executeQuery(any(), any(), any())).thenReturn(expected);
        JDBCExecutor jdbcExecutor = mock(JDBCExecutor.class);
        RawExecutor rawExecutor = mock(RawExecutor.class);
        DriverExecuteQueryExecutor executor = new DriverExecuteQueryExecutor(mock(ShardingSphereConnection.class, RETURNS_DEEP_STUBS), metaData, jdbcExecutor, rawExecutor, sqlFederationEngine);
        ResultSet actual = executor.executeQuery(mock(ShardingSphereDatabase.class), queryContext, mock(DriverExecutionPrepareEngine.class), mock(Statement.class), Collections.emptyMap(),
                mock(StatementAddCallback.class), mock(StatementReplayCallback.class));
        assertThat(actual, sameInstance(expected));
        verifyNoInteractions(jdbcExecutor, rawExecutor);
    }
    
    @Test
    void assertExecuteQueryWithJDBCPushDown() throws SQLException {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getRuleMetaData().getAttributes(RawExecutionRuleAttribute.class)).thenReturn(Collections.emptyList());
        QueryContext queryContext = mock(QueryContext.class, RETURNS_DEEP_STUBS);
        Statement statement = mock(Statement.class);
        DriverExecutionPrepareEngine prepareEngine = mock(DriverExecutionPrepareEngine.class);
        StatementAddCallback addCallback = mock(StatementAddCallback.class);
        StatementReplayCallback replayCallback = mock(StatementReplayCallback.class);
        ResultSet expected = mock(ResultSet.class);
        try (
                MockedConstruction<DriverJDBCPushDownExecuteQueryExecutor> mockedExecutors = mockConstruction(DriverJDBCPushDownExecuteQueryExecutor.class,
                        (mock, context) -> when(mock.executeQuery(database, queryContext, prepareEngine, statement, Collections.emptyMap(), addCallback, replayCallback)).thenReturn(expected))) {
            DriverExecuteQueryExecutor executor = new DriverExecuteQueryExecutor(mock(ShardingSphereConnection.class, RETURNS_DEEP_STUBS), mock(ShardingSphereMetaData.class, RETURNS_DEEP_STUBS),
                    mock(JDBCExecutor.class), mock(RawExecutor.class), mock(SQLFederationEngine.class));
            ResultSet actual = executor.executeQuery(database, queryContext, prepareEngine, statement, Collections.emptyMap(), addCallback, replayCallback);
            assertThat(actual, sameInstance(expected));
            verify(mockedExecutors.constructed().iterator().next()).executeQuery(database, queryContext, prepareEngine, statement, Collections.emptyMap(), addCallback, replayCallback);
        }
    }
    
    @Test
    void assertExecuteQueryWithRawPushDown() throws SQLException {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getRuleMetaData().getAttributes(RawExecutionRuleAttribute.class)).thenReturn(Collections.singleton(mock(RawExecutionRuleAttribute.class)));
        QueryContext queryContext = mock(QueryContext.class, RETURNS_DEEP_STUBS);
        Statement statement = mock(Statement.class);
        ResultSet expected = mock(ResultSet.class);
        try (
                MockedConstruction<DriverRawPushDownExecuteQueryExecutor> mockedExecutors = mockConstruction(DriverRawPushDownExecuteQueryExecutor.class,
                        (mock, context) -> when(mock.executeQuery(database, queryContext, statement, Collections.emptyMap())).thenReturn(expected))) {
            DriverExecuteQueryExecutor executor = new DriverExecuteQueryExecutor(mock(ShardingSphereConnection.class, RETURNS_DEEP_STUBS), mock(ShardingSphereMetaData.class, RETURNS_DEEP_STUBS),
                    mock(JDBCExecutor.class), mock(RawExecutor.class), mock(SQLFederationEngine.class));
            ResultSet actual = executor.executeQuery(database, queryContext, mock(DriverExecutionPrepareEngine.class), statement, Collections.emptyMap(), mock(StatementAddCallback.class),
                    mock(StatementReplayCallback.class));
            assertThat(actual, sameInstance(expected));
            verify(mockedExecutors.constructed().iterator().next()).executeQuery(database, queryContext, statement, Collections.emptyMap());
        }
    }
}
