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

package org.apache.shardingsphere.proxy.backend.handler.distsql.rul;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.exception.core.exception.syntax.sql.DialectSQLParsingException;
import org.apache.shardingsphere.distsql.handler.engine.DistSQLConnectionContext;
import org.apache.shardingsphere.distsql.handler.engine.query.DistSQLQueryExecutor;
import org.apache.shardingsphere.distsql.statement.type.rul.sql.PreviewStatement;
import org.apache.shardingsphere.infra.binder.context.segment.table.TablesContext;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.ddl.CursorHeldSQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.ddl.CursorStatementContext;
import org.apache.shardingsphere.infra.binder.engine.SQLBindEngine;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.connection.kernel.KernelProcessor;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.exception.kernel.metadata.rule.EmptyRuleException;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionContext;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.context.SQLUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.ConnectionMode;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutor;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutorCallback;
import org.apache.shardingsphere.infra.executor.sql.execute.result.ExecuteResult;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.DatabaseConnectionManager;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.DriverExecutionPrepareEngine;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.ExecutorStatementManager;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.resource.node.StorageNode;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.parser.rule.builder.DefaultSQLParserRuleConfigurationBuilder;
import org.apache.shardingsphere.proxy.backend.connector.ProxyDatabaseConnectionManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.cursor.CursorNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.SQLStatementAttributes;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.type.CursorSQLStatementAttribute;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sqlfederation.context.SQLFederationContext;
import org.apache.shardingsphere.sqlfederation.engine.SQLFederationEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.invocation.InvocationOnMock;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

class PreviewExecutorTest {
    
    private final PreviewExecutor executor = (PreviewExecutor) TypedSPILoader.getService(DistSQLQueryExecutor.class, PreviewStatement.class);
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "MySQL");
    
    private ContextManager contextManager;
    
    @BeforeEach
    void setUp() {
        contextManager = mockContextManager();
        ProxyContext.init(contextManager);
    }
    
    private ContextManager mockContextManager() {
        ContextManager result = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        Collection<ShardingSphereRule> rules = Collections.singleton(new SQLParserRule(new DefaultSQLParserRuleConfigurationBuilder().build()));
        when(result.getMetaDataContexts().getMetaData()).thenReturn(
                new ShardingSphereMetaData(Collections.emptyList(), mock(), new RuleMetaData(rules), new ConfigurationProperties(new Properties())));
        return result;
    }
    
    @Test
    void assertGetColumnNames() {
        assertThat(executor.getColumnNames(new PreviewStatement("SELECT 1")), is(Arrays.asList("data_source_name", "actual_sql")));
    }
    
    @Test
    void assertGetRowsWithInvalidSQL() {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class);
        when(database.getProtocolType()).thenReturn(databaseType);
        executor.setDatabase(database);
        assertThrows(DialectSQLParsingException.class, () -> executor.getRows(new PreviewStatement("invalid sql"), contextManager));
    }
    
    @Test
    void assertGetRowsWithIncompleteDatabase() {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class);
        when(database.getProtocolType()).thenReturn(databaseType);
        when(database.getName()).thenReturn("foo_db");
        executor.setDatabase(database);
        HintValueContext hintValueContext = new HintValueContext();
        executor.setConnectionContext(mockConnectionContext(hintValueContext, new ConnectionContext(Collections::emptyList), mock(DatabaseConnectionManager.class)));
        assertThrows(EmptyRuleException.class, () -> executor.getRows(new PreviewStatement("SELECT 1"), contextManager));
        assertTrue(hintValueContext.isSkipMetadataValidate());
    }
    
    @Test
    void assertGetRowsWithCursorAttributeAndNotCursorHeld() {
        HintValueContext hintValueContext = new HintValueContext();
        executor.setDatabase(mockCompleteDatabase());
        executor.setConnectionContext(mockConnectionContext(hintValueContext, new ConnectionContext(Collections::emptyList), mock(DatabaseConnectionManager.class)));
        SQLStatement sqlStatement = mockSQLStatement(new CursorSQLStatementAttribute(null));
        SQLStatementContext sqlStatementContext = mock(SQLStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getSqlStatement()).thenReturn(sqlStatement);
        when(sqlStatementContext.getTablesContext().getSchemaName()).thenReturn(Optional.empty());
        ExecutionContext executionContext = new ExecutionContext(mock(QueryContext.class), Collections.singletonList(createExecutionUnit("foo_ds", "SELECT 1")), mock(RouteContext.class));
        try (
                MockedConstruction<SQLBindEngine> ignoredBindEngine =
                        mockConstruction(SQLBindEngine.class, (mock, context) -> when(mock.bind(any(SQLStatement.class))).thenReturn(sqlStatementContext));
                MockedConstruction<JDBCExecutor> ignoredJDBCExecutor = mockConstruction(JDBCExecutor.class);
                MockedConstruction<SQLFederationEngine> ignoredFederationEngine =
                        mockConstruction(SQLFederationEngine.class, (mock, context) -> when(mock.decide(any(QueryContext.class), any(RuleMetaData.class))).thenReturn(false));
                MockedConstruction<KernelProcessor> ignoredKernelProcessor = mockConstruction(KernelProcessor.class,
                        (mock, context) -> when(mock.generateExecutionContext(any(QueryContext.class), any(RuleMetaData.class), any(ConfigurationProperties.class))).thenReturn(executionContext))) {
            assertThat(executor.getRows(new PreviewStatement("SELECT 1"), contextManager).iterator().next().getCell(1), is("foo_ds"));
            assertTrue(hintValueContext.isSkipMetadataValidate());
        }
    }
    
    @Test
    void assertGetRowsWithCursorHeldAndNoCursorName() {
        HintValueContext hintValueContext = new HintValueContext();
        executor.setDatabase(mockCompleteDatabase());
        executor.setConnectionContext(mockConnectionContext(hintValueContext, new ConnectionContext(Collections::emptyList), mock(DatabaseConnectionManager.class)));
        SQLStatement sqlStatement = mockSQLStatement(new CursorSQLStatementAttribute(null));
        CursorHeldSQLStatementContext cursorHeldSQLStatementContext = new CursorHeldSQLStatementContext(sqlStatement);
        ExecutionContext executionContext = new ExecutionContext(mock(QueryContext.class), Collections.singletonList(createExecutionUnit("foo_ds", "SELECT 1")), mock(RouteContext.class));
        try (
                MockedConstruction<SQLBindEngine> ignoredBindEngine =
                        mockConstruction(SQLBindEngine.class, (mock, context) -> when(mock.bind(any(SQLStatement.class))).thenReturn(cursorHeldSQLStatementContext));
                MockedConstruction<JDBCExecutor> ignoredJDBCExecutor = mockConstruction(JDBCExecutor.class);
                MockedConstruction<SQLFederationEngine> ignoredFederationEngine =
                        mockConstruction(SQLFederationEngine.class, (mock, context) -> when(mock.decide(any(QueryContext.class), any(RuleMetaData.class))).thenReturn(false));
                MockedConstruction<KernelProcessor> ignoredKernelProcessor = mockConstruction(KernelProcessor.class,
                        (mock, context) -> when(mock.generateExecutionContext(any(QueryContext.class), any(RuleMetaData.class), any(ConfigurationProperties.class))).thenReturn(executionContext))) {
            assertThat(executor.getRows(new PreviewStatement("SELECT 1"), contextManager).iterator().next().getCell(2), is("SELECT 1"));
            assertNull(cursorHeldSQLStatementContext.getCursorStatementContext());
        }
    }
    
    @Test
    void assertGetRowsWithCursorHeldAndCursorNameWithFederation() {
        ConnectionContext connectionContext = new ConnectionContext(Collections::emptyList);
        CursorStatementContext cursorStatementContext = mock(CursorStatementContext.class);
        TablesContext tablesContext = mock(TablesContext.class);
        when(tablesContext.getSchemaName()).thenReturn(Optional.of("foo_schema"));
        when(cursorStatementContext.getTablesContext()).thenReturn(tablesContext);
        connectionContext.getCursorContext().getCursorStatementContexts().put("foo_cursor", cursorStatementContext);
        ProxyDatabaseConnectionManager databaseConnectionManager = mock(ProxyDatabaseConnectionManager.class, RETURNS_DEEP_STUBS);
        when(databaseConnectionManager.getConnectionSession().getProcessId()).thenReturn("process_id");
        HintValueContext hintValueContext = new HintValueContext();
        executor.setDatabase(mockCompleteDatabase());
        executor.setConnectionContext(mockConnectionContext(hintValueContext, connectionContext, databaseConnectionManager));
        SQLStatement sqlStatement = mockSQLStatement(new CursorSQLStatementAttribute(new CursorNameSegment(0, 0, new IdentifierValue("FOO_CURSOR"))));
        CursorHeldSQLStatementContext cursorHeldSQLStatementContext = new CursorHeldSQLStatementContext(sqlStatement);
        AtomicReference<String> actualSchemaName = new AtomicReference<>();
        try (
                MockedConstruction<SQLBindEngine> ignoredBindEngine =
                        mockConstruction(SQLBindEngine.class, (mock, context) -> when(mock.bind(any(SQLStatement.class))).thenReturn(cursorHeldSQLStatementContext));
                MockedConstruction<JDBCExecutor> ignoredJDBCExecutor = mockConstruction(JDBCExecutor.class);
                MockedConstruction<DriverExecutionPrepareEngine> ignoredPrepareEngine = mockConstruction(DriverExecutionPrepareEngine.class);
                MockedConstruction<SQLFederationEngine> ignored4 = mockConstruction(SQLFederationEngine.class, (mock, context) -> configureSQLFederationEngine(mock, context, actualSchemaName))) {
            assertThat(executor.getRows(new PreviewStatement("SELECT 1"), contextManager).iterator().next().getCell(1), is("bar_ds"));
            assertThat(actualSchemaName.get(), is("foo_schema"));
            assertThat(cursorHeldSQLStatementContext.getCursorStatementContext(), is(cursorStatementContext));
        }
    }
    
    private ShardingSphereDatabase mockCompleteDatabase() {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class);
        when(result.getProtocolType()).thenReturn(databaseType);
        when(result.getName()).thenReturn("foo_db");
        when(result.isComplete()).thenReturn(true);
        when(result.getResourceMetaData()).thenReturn(createResourceMetaData());
        when(result.getRuleMetaData()).thenReturn(new RuleMetaData(Collections.emptyList()));
        return result;
    }
    
    private ResourceMetaData createResourceMetaData() {
        Map<String, StorageUnit> storageUnits = new LinkedHashMap<>(2, 1F);
        storageUnits.put("foo_ds", createStorageUnit("foo_ds", "jdbc:mysql://localhost:3306/foo_db"));
        storageUnits.put("bar_ds", createStorageUnit("bar_ds", "jdbc:postgresql://localhost:5432/bar_db"));
        return new ResourceMetaData(Collections.emptyMap(), storageUnits);
    }
    
    private StorageUnit createStorageUnit(final String name, final String url) {
        Map<String, Object> props = new LinkedHashMap<>(2, 1F);
        props.put("url", url);
        props.put("username", "root");
        return new StorageUnit(new StorageNode(name), new DataSourcePoolProperties("com.zaxxer.hikari.HikariDataSource", props), mock(DataSource.class));
    }
    
    @SuppressWarnings("rawtypes")
    private DistSQLConnectionContext mockConnectionContext(final HintValueContext hintValueContext, final ConnectionContext connectionContext,
                                                           final DatabaseConnectionManager databaseConnectionManager) {
        QueryContext queryContext = mock(QueryContext.class);
        when(queryContext.getHintValueContext()).thenReturn(hintValueContext);
        when(queryContext.getConnectionContext()).thenReturn(connectionContext);
        return new DistSQLConnectionContext(queryContext, 1, databaseType, databaseConnectionManager, mock(ExecutorStatementManager.class));
    }
    
    private SQLStatement mockSQLStatement(final CursorSQLStatementAttribute cursorSQLStatementAttribute) {
        SQLStatement result = mock(SQLStatement.class);
        when(result.getDatabaseType()).thenReturn(databaseType);
        when(result.getAttributes()).thenReturn(new SQLStatementAttributes(cursorSQLStatementAttribute));
        return result;
    }
    
    private ExecutionUnit createExecutionUnit(final String dataSourceName, final String sql) {
        return new ExecutionUnit(dataSourceName, new SQLUnit(sql, Collections.emptyList()));
    }
    
    private JDBCExecutionUnit createJDBCExecutionUnit(final String dataSourceName, final String sql, final boolean isThrowSQLException) throws SQLException {
        Statement statement = mock(Statement.class);
        if (isThrowSQLException) {
            when(statement.executeQuery(sql)).thenThrow(new SQLException("mock exception"));
        } else {
            when(statement.executeQuery(sql)).thenReturn(mock(ResultSet.class));
        }
        return new JDBCExecutionUnit(createExecutionUnit(dataSourceName, sql), ConnectionMode.MEMORY_STRICTLY, statement);
    }
    
    private void configureSQLFederationEngine(final SQLFederationEngine federationEngine, final MockedConstruction.Context context, final AtomicReference<String> actualSchemaName) {
        actualSchemaName.set(context.arguments().get(1).toString());
        when(federationEngine.decide(any(QueryContext.class), any(RuleMetaData.class))).thenReturn(true);
        doAnswer(this::executePreviewCallback).when(federationEngine).executeQuery(any(), any(), any(SQLFederationContext.class));
    }
    
    private Object executePreviewCallback(final InvocationOnMock invocation) throws SQLException {
        SQLFederationContext sqlFederationContext = invocation.getArgument(2);
        JDBCExecutorCallback<? extends ExecuteResult> callback = invocation.getArgument(1);
        callback.execute(Collections.singletonList(createJDBCExecutionUnit("foo_ds", "SELECT 2", false)), true, "process_id");
        try {
            callback.execute(Collections.singletonList(createJDBCExecutionUnit("bar_ds", "SELECT 3", true)), true, "process_id");
        } catch (final SQLException ignored) {
        }
        sqlFederationContext.getPreviewExecutionUnits().add(createExecutionUnit("bar_ds", "SELECT 2"));
        return null;
    }
}
