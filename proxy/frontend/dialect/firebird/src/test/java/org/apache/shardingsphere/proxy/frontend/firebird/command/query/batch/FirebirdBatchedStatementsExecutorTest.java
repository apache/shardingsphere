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

package org.apache.shardingsphere.proxy.frontend.firebird.command.query.batch;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.connection.kernel.KernelProcessor;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionContext;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.context.SQLUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.ConnectionMode;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.jdbc.StatementOption;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.connector.ProxyDatabaseConnectionManager;
import org.apache.shardingsphere.proxy.backend.connector.jdbc.statement.JDBCBackendStatement;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.FirebirdServerPreparedStatement;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.InsertValuesSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.InsertColumnsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
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
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ProxyContext.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class FirebirdBatchedStatementsExecutorTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "Firebird");
    
    @Mock
    private ProxyDatabaseConnectionManager databaseConnectionManager;
    
    @Mock
    private JDBCBackendStatement backendStatement;
    
    @Test
    void assertExecuteBatch() throws SQLException {
        Connection connection = mock(Connection.class, RETURNS_DEEP_STUBS);
        when(connection.getMetaData().getURL()).thenReturn("jdbc:firebirdsql://127.0.0.1/db");
        when(databaseConnectionManager.getConnections(any(), nullable(String.class), anyInt(), anyInt(), any(ConnectionMode.class))).thenReturn(Collections.singletonList(connection));
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        when(preparedStatement.getConnection()).thenReturn(connection);
        when(preparedStatement.executeBatch()).thenReturn(new int[]{1, 2});
        when(backendStatement.createStorageResource(any(ExecutionUnit.class), eq(connection), anyInt(), any(ConnectionMode.class), any(StatementOption.class), nullable(DatabaseType.class)))
                .thenReturn(preparedStatement);
        ContextManager contextManager = mockContextManager();
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        FirebirdServerPreparedStatement firebirdPreparedStatement = new FirebirdServerPreparedStatement("INSERT INTO t (id, col) VALUES (?, ?)",
                mockInsertStatementContext(), new HintValueContext());
        List<List<Object>> parameterSets = Arrays.asList(Arrays.asList(1, "foo_1"), Arrays.asList(2, "foo_2"));
        int[] actual = new FirebirdBatchedStatementsExecutor(mockConnectionSession(), firebirdPreparedStatement, parameterSets).executeBatch();
        assertArrayEquals(new int[]{1, 2}, actual);
        InOrder inOrder = inOrder(preparedStatement);
        for (List<Object> each : parameterSets) {
            inOrder.verify(preparedStatement).setObject(1, each.get(0));
            inOrder.verify(preparedStatement).setObject(2, each.get(1));
            inOrder.verify(preparedStatement).addBatch();
        }
        inOrder.verify(preparedStatement).executeBatch();
        inOrder.verify(preparedStatement).close();
    }
    
    @Test
    void assertExecuteBatchWithMultiRouteMessageCounts() throws SQLException {
        Connection connection = mock(Connection.class, RETURNS_DEEP_STUBS);
        when(connection.getMetaData().getURL()).thenReturn("jdbc:firebirdsql://127.0.0.1/db");
        when(databaseConnectionManager.getConnections(any(), nullable(String.class), anyInt(), anyInt(), any(ConnectionMode.class))).thenReturn(Collections.singletonList(connection));
        PreparedStatement firstPreparedStatement = mock(PreparedStatement.class);
        PreparedStatement secondPreparedStatement = mock(PreparedStatement.class);
        when(firstPreparedStatement.getConnection()).thenReturn(connection);
        when(secondPreparedStatement.getConnection()).thenReturn(connection);
        when(firstPreparedStatement.executeBatch()).thenReturn(new int[]{1});
        when(secondPreparedStatement.executeBatch()).thenReturn(new int[]{1});
        when(backendStatement.createStorageResource(any(ExecutionUnit.class), eq(connection), anyInt(), any(ConnectionMode.class), any(StatementOption.class), nullable(DatabaseType.class)))
                .thenReturn(firstPreparedStatement, secondPreparedStatement);
        ContextManager contextManager = mockContextManager("ds_0", "ds_1");
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        FirebirdServerPreparedStatement firebirdPreparedStatement = new FirebirdServerPreparedStatement("INSERT INTO t (id, col) VALUES (?, ?)",
                mockInsertStatementContext(), new HintValueContext());
        List<Object> params = Arrays.asList(1, "foo_1");
        ExecutionUnit firstExecutionUnit = new ExecutionUnit("ds_0", new SQLUnit(firebirdPreparedStatement.getSql(), params));
        ExecutionUnit secondExecutionUnit = new ExecutionUnit("ds_1", new SQLUnit(firebirdPreparedStatement.getSql(), params));
        try (
                MockedConstruction<KernelProcessor> ignored = mockConstruction(KernelProcessor.class,
                        (mock, context) -> when(mock.generateExecutionContext(any(), any(), any()))
                                .thenReturn(new ExecutionContext(null, Arrays.asList(firstExecutionUnit, secondExecutionUnit), mock(RouteContext.class))))) {
            assertArrayEquals(new int[]{2}, new FirebirdBatchedStatementsExecutor(mockConnectionSession(), firebirdPreparedStatement, Collections.singletonList(params)).executeBatch());
        }
    }
    
    private InsertStatementContext mockInsertStatementContext() {
        InsertStatement insertStatement = InsertStatement.builder()
                .databaseType(databaseType)
                .table(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t"))))
                .insertColumns(new InsertColumnsSegment(0, 0, Arrays.asList(
                        new ColumnSegment(0, 0, new IdentifierValue("id")), new ColumnSegment(0, 0, new IdentifierValue("col")))))
                .values(Collections.singleton(new InsertValuesSegment(0, 0, Arrays.asList(
                        new ParameterMarkerExpressionSegment(0, 0, 0), new ParameterMarkerExpressionSegment(0, 0, 1)))))
                .build();
        InsertStatementContext result = mock(InsertStatementContext.class);
        when(result.getSqlStatement()).thenReturn(insertStatement);
        return result;
    }
    
    private ContextManager mockContextManager() {
        return mockContextManager("ds_0");
    }
    
    private ContextManager mockContextManager(final String... dataSourceNames) {
        ContextManager result = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(result.getMetaDataContexts().getMetaData().getProps().getValue(ConfigurationPropertyKey.KERNEL_EXECUTOR_SIZE)).thenReturn(1);
        when(result.getMetaDataContexts().getMetaData().getProps().getValue(ConfigurationPropertyKey.MAX_CONNECTIONS_SIZE_PER_QUERY)).thenReturn(1);
        when(result.getMetaDataContexts().getMetaData().getProps().getValue(ConfigurationPropertyKey.SQL_SHOW)).thenReturn(false);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        Map<String, StorageUnit> storageUnits = new LinkedHashMap<>(dataSourceNames.length, 1F);
        for (String each : dataSourceNames) {
            StorageUnit storageUnit = mock(StorageUnit.class, RETURNS_DEEP_STUBS);
            when(storageUnit.getStorageType()).thenReturn(databaseType);
            storageUnits.put(each, storageUnit);
        }
        when(database.getProtocolType()).thenReturn(databaseType);
        when(database.getDefaultSchemaName()).thenReturn("DB");
        when(database.getResourceMetaData().getStorageUnits()).thenReturn(storageUnits);
        when(database.getResourceMetaData().getAllInstanceDataSourceNames()).thenReturn(Arrays.asList(dataSourceNames));
        when(database.getRuleMetaData()).thenReturn(new RuleMetaData(Collections.emptyList()));
        when(database.containsSchema("public")).thenReturn(true);
        when(database.containsSchema(new IdentifierValue("public"))).thenReturn(true);
        when(database.containsSchema("DB")).thenReturn(true);
        when(database.containsSchema(new IdentifierValue("DB"))).thenReturn(true);
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class, RETURNS_DEEP_STUBS);
        when(database.getSchema("public")).thenReturn(schema);
        when(database.getSchema(new IdentifierValue("public"))).thenReturn(schema);
        when(database.getSchema("DB")).thenReturn(schema);
        when(database.getSchema(new IdentifierValue("DB"))).thenReturn(schema);
        when(schema.containsTable("t")).thenReturn(true);
        when(schema.containsTable(new IdentifierValue("t"))).thenReturn(true);
        when(schema.getTable("t").getAllColumns()).thenReturn(Arrays.asList(new ShardingSphereColumn("id", Types.INTEGER, false, false, false, true, false, false),
                new ShardingSphereColumn("col", Types.VARCHAR, false, false, false, true, false, false)));
        when(schema.getTable(new IdentifierValue("t")).getAllColumns()).thenReturn(Arrays.asList(new ShardingSphereColumn("id", Types.INTEGER, false, false, false, true, false, false),
                new ShardingSphereColumn("col", Types.VARCHAR, false, false, false, true, false, false)));
        when(result.getMetaDataContexts().getMetaData().containsDatabase("db")).thenReturn(true);
        when(result.getMetaDataContexts().getMetaData().containsDatabase(new IdentifierValue("db"))).thenReturn(true);
        when(result.getMetaDataContexts().getMetaData().getDatabase("db")).thenReturn(database);
        when(result.getMetaDataContexts().getMetaData().getDatabase(new IdentifierValue("db"))).thenReturn(database);
        RuleMetaData globalRuleMetaData = new RuleMetaData(Collections.singleton(new SQLTranslatorRule(new DefaultSQLTranslatorRuleConfigurationBuilder().build())));
        when(result.getMetaDataContexts().getMetaData().getGlobalRuleMetaData()).thenReturn(globalRuleMetaData);
        when(result.getMetaDataContexts().getMetaData().getProps()).thenReturn(new ConfigurationProperties(new Properties()));
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
}
