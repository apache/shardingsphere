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
import org.apache.shardingsphere.infra.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.PostgreSQLDatabaseType;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.ConnectionMode;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.jdbc.StatementOption;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.logging.rule.LoggingRule;
import org.apache.shardingsphere.logging.rule.builder.DefaultLoggingRuleConfigurationBuilder;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.connector.ProxyDatabaseConnectionManager;
import org.apache.shardingsphere.proxy.backend.connector.jdbc.statement.JDBCBackendStatement;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dml.PostgreSQLInsertStatement;
import org.apache.shardingsphere.sqltranslator.rule.SQLTranslatorRule;
import org.apache.shardingsphere.sqltranslator.rule.builder.DefaultSQLTranslatorRuleConfigurationBuilder;
import org.apache.shardingsphere.test.mock.AutoMockExtension;
import org.apache.shardingsphere.test.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.internal.configuration.plugins.Plugins;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

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

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ProxyContext.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PostgreSQLBatchedStatementsExecutorTest {
    
    @Mock
    private ProxyDatabaseConnectionManager databaseConnectionManager;
    
    @Mock
    private JDBCBackendStatement backendStatement;
    
    @Test
    void assertExecuteBatch() throws SQLException {
        Connection connection = mock(Connection.class, RETURNS_DEEP_STUBS);
        when(connection.getMetaData().getURL()).thenReturn("jdbc:postgresql://127.0.0.1/db");
        when(databaseConnectionManager.getConnections(nullable(String.class), anyInt(), anyInt(), any(ConnectionMode.class))).thenReturn(Collections.singletonList(connection));
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        when(preparedStatement.getConnection()).thenReturn(connection);
        when(preparedStatement.executeBatch()).thenReturn(new int[]{1, 1, 1});
        when(backendStatement.createStorageResource(any(ExecutionUnit.class), eq(connection), any(ConnectionMode.class), any(StatementOption.class), nullable(DatabaseType.class)))
                .thenReturn(preparedStatement);
        ContextManager contextManager = mockContextManager();
        ConnectionSession connectionSession = mockConnectionSession();
        PostgreSQLServerPreparedStatement postgreSQLPreparedStatement = new PostgreSQLServerPreparedStatement("insert into t (id, col) values (?, ?)", mockInsertStatementContext(),
                new HintValueContext(), Arrays.asList(PostgreSQLColumnType.INT4, PostgreSQLColumnType.VARCHAR), Arrays.asList(0, 1));
        List<List<Object>> parameterSets = Arrays.asList(Arrays.asList(1, new PostgreSQLTypeUnspecifiedSQLParameter("foo")),
                Arrays.asList(2, new PostgreSQLTypeUnspecifiedSQLParameter("bar")), Arrays.asList(3, new PostgreSQLTypeUnspecifiedSQLParameter("baz")));
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
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
    
    private InsertStatementContext mockInsertStatementContext() {
        PostgreSQLInsertStatement insertStatement = mock(PostgreSQLInsertStatement.class, RETURNS_DEEP_STUBS);
        when(insertStatement.getTable().getTableName().getIdentifier().getValue()).thenReturn("t");
        InsertStatementContext result = mock(InsertStatementContext.class);
        when(result.getSqlStatement()).thenReturn(insertStatement);
        return result;
    }
    
    private ContextManager mockContextManager() {
        ContextManager result = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(result.getMetaDataContexts().getMetaData().getProps().getValue(ConfigurationPropertyKey.KERNEL_EXECUTOR_SIZE)).thenReturn(1);
        when(result.getMetaDataContexts().getMetaData().getProps().getValue(ConfigurationPropertyKey.MAX_CONNECTIONS_SIZE_PER_QUERY)).thenReturn(1);
        when(result.getMetaDataContexts().getMetaData().getProps().getValue(ConfigurationPropertyKey.SQL_SHOW)).thenReturn(false);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getResourceMetaData().getStorageTypes()).thenReturn(Collections.singletonMap("ds_0", new PostgreSQLDatabaseType()));
        when(database.getResourceMetaData().getAllInstanceDataSourceNames()).thenReturn(Collections.singletonList("ds_0"));
        when(database.getRuleMetaData()).thenReturn(new ShardingSphereRuleMetaData(Collections.emptyList()));
        when(result.getMetaDataContexts().getMetaData().getDatabase("db")).thenReturn(database);
        ShardingSphereRuleMetaData globalRuleMetaData = new ShardingSphereRuleMetaData(Arrays.asList(new SQLTranslatorRule(new DefaultSQLTranslatorRuleConfigurationBuilder().build()),
                new LoggingRule(new DefaultLoggingRuleConfigurationBuilder().build())));
        when(result.getMetaDataContexts().getMetaData().getGlobalRuleMetaData()).thenReturn(globalRuleMetaData);
        return result;
    }
    
    private ConnectionSession mockConnectionSession() {
        ConnectionSession result = mock(ConnectionSession.class);
        when(result.getDatabaseName()).thenReturn("db");
        when(result.getDatabaseConnectionManager()).thenReturn(databaseConnectionManager);
        when(result.getStatementManager()).thenReturn(backendStatement);
        when(result.getConnectionContext()).thenReturn(new ConnectionContext());
        return result;
    }
    
    @SuppressWarnings("unchecked")
    @SneakyThrows(ReflectiveOperationException.class)
    private void prepareExecutionUnitParameters(final PostgreSQLBatchedStatementsExecutor target, final List<List<Object>> parameterSets) {
        ((Map<ExecutionUnit, List<List<Object>>>) Plugins.getMemberAccessor().get(PostgreSQLBatchedStatementsExecutor.class.getDeclaredField("executionUnitParams"), target))
                .replaceAll((k, v) -> parameterSets);
    }
}
