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

package org.apache.shardingsphere.proxy.frontend.postgresql.command.query.extended;

import lombok.SneakyThrows;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.extended.PostgreSQLColumnType;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.extended.bind.PostgreSQLTypeUnspecifiedSQLParameter;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.ConnectionMode;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.jdbc.StatementOption;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.JDBCBackendConnection;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.statement.JDBCBackendStatement;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.postgresql.ProxyContextRestorer;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dml.PostgreSQLInsertStatement;
import org.apache.shardingsphere.sqltranslator.rule.SQLTranslatorRule;
import org.apache.shardingsphere.sqltranslator.rule.builder.DefaultSQLTranslatorRuleConfigurationBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class PostgreSQLBatchedStatementsExecutorTest extends ProxyContextRestorer {
    
    @Mock
    private ConnectionSession connectionSession;
    
    @Mock
    private JDBCBackendConnection backendConnection;
    
    @Mock
    private JDBCBackendStatement backendStatement;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ContextManager contextManager;
    
    @Before
    public void setup() {
        when(connectionSession.getDatabaseName()).thenReturn("db");
        when(connectionSession.getBackendConnection()).thenReturn(backendConnection);
        when(connectionSession.getStatementManager()).thenReturn(backendStatement);
        ProxyContext.init(contextManager);
        when(contextManager.getMetaDataContexts().getMetaData().getProps().getValue(ConfigurationPropertyKey.KERNEL_EXECUTOR_SIZE)).thenReturn(1);
        when(contextManager.getMetaDataContexts().getMetaData().getProps().getValue(ConfigurationPropertyKey.MAX_CONNECTIONS_SIZE_PER_QUERY)).thenReturn(1);
        when(contextManager.getMetaDataContexts().getMetaData().getProps().getValue(ConfigurationPropertyKey.SQL_SHOW)).thenReturn(false);
        ShardingSphereRuleMetaData globalRuleMetaData = mock(ShardingSphereRuleMetaData.class);
        when(ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getGlobalRuleMetaData()).thenReturn(globalRuleMetaData);
        when(globalRuleMetaData.getSingleRule(SQLTranslatorRule.class)).thenReturn(new SQLTranslatorRule(new DefaultSQLTranslatorRuleConfigurationBuilder().build()));
    }
    
    @Test
    public void assertExecuteBatch() throws SQLException {
        PostgreSQLInsertStatement insertStatement = mock(PostgreSQLInsertStatement.class, RETURNS_DEEP_STUBS);
        when(insertStatement.getTable().getTableName().getIdentifier().getValue()).thenReturn("t");
        PostgreSQLServerPreparedStatement postgreSQLPreparedStatement = new PostgreSQLServerPreparedStatement("insert into t (id, col) values (?, ?)", insertStatement, null,
                Arrays.asList(PostgreSQLColumnType.POSTGRESQL_TYPE_INT4, PostgreSQLColumnType.POSTGRESQL_TYPE_VARCHAR));
        List<List<Object>> parameterSets = Arrays.asList(Arrays.asList(1, new PostgreSQLTypeUnspecifiedSQLParameter("foo")),
                Arrays.asList(2, new PostgreSQLTypeUnspecifiedSQLParameter("bar")), Arrays.asList(3, new PostgreSQLTypeUnspecifiedSQLParameter("baz")));
        Connection connection = mock(Connection.class, RETURNS_DEEP_STUBS);
        when(connection.getMetaData().getURL()).thenReturn("jdbc:postgresql://127.0.0.1/db");
        when(backendConnection.getConnections(nullable(String.class), anyInt(), any(ConnectionMode.class))).thenReturn(Collections.singletonList(connection));
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        when(preparedStatement.getConnection()).thenReturn(connection);
        when(preparedStatement.executeBatch()).thenReturn(new int[]{1, 1, 1});
        when(backendStatement.createStorageResource(any(ExecutionUnit.class), eq(connection), any(ConnectionMode.class), any(StatementOption.class))).thenReturn(preparedStatement);
        PostgreSQLBatchedStatementsExecutor actual = new PostgreSQLBatchedStatementsExecutor(connectionSession, postgreSQLPreparedStatement, parameterSets);
        prepareExecutionUnitParameters(actual, parameterSets);
        int actualUpdated = actual.executeBatch();
        assertThat(actualUpdated, is(3));
        InOrder inOrder = inOrder(preparedStatement);
        for (List<Object> each : parameterSets) {
            inOrder.verify(preparedStatement).setObject(1, each.get(0));
            inOrder.verify(preparedStatement).setObject(2, each.get(1).toString());
            inOrder.verify(preparedStatement).addBatch();
        }
    }
    
    @SuppressWarnings("unchecked")
    @SneakyThrows(ReflectiveOperationException.class)
    private void prepareExecutionUnitParameters(final PostgreSQLBatchedStatementsExecutor target, final List<List<Object>> parameterSets) {
        Field executionUnitParametersField = PostgreSQLBatchedStatementsExecutor.class.getDeclaredField("executionUnitParameters");
        executionUnitParametersField.setAccessible(true);
        Map<ExecutionUnit, List<List<Object>>> map = (Map<ExecutionUnit, List<List<Object>>>) executionUnitParametersField.get(target);
        map.replaceAll((k, v) -> parameterSets);
    }
}
