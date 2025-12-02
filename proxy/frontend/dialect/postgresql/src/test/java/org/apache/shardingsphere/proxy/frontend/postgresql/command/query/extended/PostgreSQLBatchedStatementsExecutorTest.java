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
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.PostgreSQLColumnType;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.bind.PostgreSQLTypeUnspecifiedSQLParameter;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.ConnectionMode;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.jdbc.StatementOption;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.connector.ProxyDatabaseConnectionManager;
import org.apache.shardingsphere.proxy.backend.connector.jdbc.statement.JDBCBackendStatement;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sqltranslator.rule.SQLTranslatorRule;
import org.apache.shardingsphere.sqltranslator.rule.builder.DefaultSQLTranslatorRuleConfigurationBuilder;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
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
import java.sql.Types;
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
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "PostgreSQL");
    
    @Mock
    private ProxyDatabaseConnectionManager databaseConnectionManager;
    
    @Mock
    private JDBCBackendStatement backendStatement;
    
    @Test
    void assertExecuteBatch() throws SQLException {
        Connection connection = mock(Connection.class, RETURNS_DEEP_STUBS);
        when(connection.getMetaData().getURL()).thenReturn("jdbc:postgresql://127.0.0.1/db");
        when(databaseConnectionManager.getConnections(any(), nullable(String.class), anyInt(), anyInt(), any(ConnectionMode.class))).thenReturn(Collections.singletonList(connection));
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        when(preparedStatement.getConnection()).thenReturn(connection);
        when(preparedStatement.executeBatch()).thenReturn(new int[]{1, 1, 1});
        when(backendStatement.createStorageResource(any(ExecutionUnit.class), eq(connection), anyInt(), any(ConnectionMode.class), any(StatementOption.class), nullable(DatabaseType.class)))
                .thenReturn(preparedStatement);
        ContextManager contextManager = mockContextManager();
        ConnectionSession connectionSession = mockConnectionSession();
        PostgreSQLServerPreparedStatement postgresqlPreparedStatement = new PostgreSQLServerPreparedStatement("INSERT INTO t (id, col) VALUES (?, ?)", mockInsertStatementContext(),
                new HintValueContext(), Arrays.asList(PostgreSQLColumnType.INT4, PostgreSQLColumnType.VARCHAR), Arrays.asList("int", "varchar"), Arrays.asList(0, 1));
        List<List<Object>> parameterSets = Arrays.asList(Arrays.asList(1, new PostgreSQLTypeUnspecifiedSQLParameter("foo")),
                Arrays.asList(2, new PostgreSQLTypeUnspecifiedSQLParameter("bar")), Arrays.asList(3, new PostgreSQLTypeUnspecifiedSQLParameter("baz")));
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        PostgreSQLBatchedStatementsExecutor actual = new PostgreSQLBatchedStatementsExecutor(connectionSession, postgresqlPreparedStatement, parameterSets);
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
        InsertStatement insertStatement = new InsertStatement(databaseType);
        insertStatement.setTable(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t"))));
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
        StorageUnit storageUnit = mock(StorageUnit.class, RETURNS_DEEP_STUBS);
        when(storageUnit.getStorageType()).thenReturn(databaseType);
        when(database.getResourceMetaData().getStorageUnits()).thenReturn(Collections.singletonMap("ds_0", storageUnit));
        when(database.getResourceMetaData().getAllInstanceDataSourceNames()).thenReturn(Collections.singletonList("ds_0"));
        when(database.getRuleMetaData()).thenReturn(new RuleMetaData(Collections.emptyList()));
        when(database.containsSchema("public")).thenReturn(true);
        when(database.getSchema("public").containsTable("t")).thenReturn(true);
        when(database.getSchema("public").getTable("t").getAllColumns()).thenReturn(Arrays.asList(new ShardingSphereColumn("id", Types.VARCHAR, false, false, "varchar", false, true, false, false),
                new ShardingSphereColumn("col", Types.VARCHAR, false, false, "varchar", false, true, false, false)));
        when(result.getMetaDataContexts().getMetaData().containsDatabase("db")).thenReturn(true);
        when(result.getMetaDataContexts().getMetaData().getDatabase("db")).thenReturn(database);
        RuleMetaData globalRuleMetaData = new RuleMetaData(Collections.singleton(new SQLTranslatorRule(new DefaultSQLTranslatorRuleConfigurationBuilder().build())));
        when(result.getMetaDataContexts().getMetaData().getGlobalRuleMetaData()).thenReturn(globalRuleMetaData);
        return result;
    }
    
    private ConnectionSession mockConnectionSession() {
        ConnectionSession result = mock(ConnectionSession.class);
        when(result.getCurrentDatabaseName()).thenReturn("db");
        when(result.getUsedDatabaseName()).thenReturn("db");
        when(result.getDatabaseConnectionManager()).thenReturn(databaseConnectionManager);
        when(result.getStatementManager()).thenReturn(backendStatement);
        ConnectionContext connectionContext = new ConnectionContext(Collections::emptySet);
        connectionContext.setCurrentDatabaseName("db");
        when(result.getConnectionContext()).thenReturn(connectionContext);
        return result;
    }
    
    @SuppressWarnings("unchecked")
    @SneakyThrows(ReflectiveOperationException.class)
    private void prepareExecutionUnitParameters(final PostgreSQLBatchedStatementsExecutor target, final List<List<Object>> parameterSets) {
        ((Map<ExecutionUnit, List<List<Object>>>) Plugins.getMemberAccessor().get(PostgreSQLBatchedStatementsExecutor.class.getDeclaredField("executionUnitParams"), target))
                .replaceAll((key, value) -> parameterSets);
    }
}
