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
import org.apache.shardingsphere.infra.exception.kernel.metadata.rule.EmptyRuleException;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionContext;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.context.SQLUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutor;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.DatabaseConnectionManager;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.DriverExecutionPrepareEngine;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.ExecutorStatementManager;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.route.context.RouteContext;
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

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.CoreMatchers.is;
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
    
    @Test
    void assertGetColumnNames() {
        assertThat(executor.getColumnNames(new PreviewStatement("SELECT 1")), is(Arrays.asList("data_source_name", "actual_sql")));
    }
    
    @Test
    void assertGetRowsWithInvalidSQL() {
        executor.setDatabase(mockDatabaseForParsing());
        assertThrows(DialectSQLParsingException.class, () -> executor.getRows(new PreviewStatement("invalid sql"), contextManager));
    }
    
    @Test
    void assertGetRowsWithIncompleteDatabase() {
        HintValueContext hintValueContext = new HintValueContext();
        executor.setDatabase(mockIncompleteDatabase());
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
        SQLStatementContext sqlStatementContext = mockSQLStatementContext(sqlStatement, Optional.empty());
        ExecutionContext executionContext = new ExecutionContext(mock(QueryContext.class), Collections.singletonList(createExecutionUnit("foo_ds", "SELECT 1")), mock(RouteContext.class));
        try (
                MockedConstruction<SQLBindEngine> ignored1 =
                        mockConstruction(SQLBindEngine.class, (mock, context) -> when(mock.bind(any(SQLStatement.class))).thenReturn(sqlStatementContext));
                MockedConstruction<JDBCExecutor> ignored2 = mockConstruction(JDBCExecutor.class);
                MockedConstruction<SQLFederationEngine> ignored3 =
                        mockConstruction(SQLFederationEngine.class, (mock, context) -> when(mock.decide(any(QueryContext.class), any(RuleMetaData.class))).thenReturn(false));
                MockedConstruction<KernelProcessor> ignored4 =
                        mockConstruction(KernelProcessor.class,
                                (mock, context) -> when(mock.generateExecutionContext(any(QueryContext.class), any(RuleMetaData.class), any(ConfigurationProperties.class)))
                                        .thenReturn(executionContext))) {
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
                MockedConstruction<SQLBindEngine> ignored1 =
                        mockConstruction(SQLBindEngine.class, (mock, context) -> when(mock.bind(any(SQLStatement.class))).thenReturn(cursorHeldSQLStatementContext));
                MockedConstruction<JDBCExecutor> ignored2 = mockConstruction(JDBCExecutor.class);
                MockedConstruction<SQLFederationEngine> ignored3 =
                        mockConstruction(SQLFederationEngine.class, (mock, context) -> when(mock.decide(any(QueryContext.class), any(RuleMetaData.class))).thenReturn(false));
                MockedConstruction<KernelProcessor> ignored4 =
                        mockConstruction(KernelProcessor.class,
                                (mock, context) -> when(mock.generateExecutionContext(any(QueryContext.class), any(RuleMetaData.class), any(ConfigurationProperties.class)))
                                        .thenReturn(executionContext))) {
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
                MockedConstruction<SQLBindEngine> ignored1 =
                        mockConstruction(SQLBindEngine.class, (mock, context) -> when(mock.bind(any(SQLStatement.class))).thenReturn(cursorHeldSQLStatementContext));
                MockedConstruction<JDBCExecutor> ignored2 = mockConstruction(JDBCExecutor.class);
                MockedConstruction<DriverExecutionPrepareEngine> ignored3 = mockConstruction(DriverExecutionPrepareEngine.class);
                MockedConstruction<SQLFederationEngine> ignored4 = mockConstruction(SQLFederationEngine.class, (mock, context) -> {
                    actualSchemaName.set(context.arguments().get(1).toString());
                    when(mock.decide(any(QueryContext.class), any(RuleMetaData.class))).thenReturn(true);
                    doAnswer(invocation -> {
                        SQLFederationContext sqlFederationContext = invocation.getArgument(2);
                        sqlFederationContext.getPreviewExecutionUnits().add(createExecutionUnit("bar_ds", "SELECT 2"));
                        return null;
                    }).when(mock).executeQuery(any(), any(), any(SQLFederationContext.class));
                })) {
            assertThat(executor.getRows(new PreviewStatement("SELECT 1"), contextManager).iterator().next().getCell(1), is("bar_ds"));
            assertThat(actualSchemaName.get(), is("foo_schema"));
            assertThat(cursorHeldSQLStatementContext.getCursorStatementContext(), is(cursorStatementContext));
        }
    }
    
    private ShardingSphereDatabase mockDatabaseForParsing() {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class);
        when(result.getProtocolType()).thenReturn(databaseType);
        return result;
    }
    
    private ShardingSphereDatabase mockIncompleteDatabase() {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class);
        when(result.getProtocolType()).thenReturn(databaseType);
        when(result.getName()).thenReturn("foo_db");
        return result;
    }
    
    private ShardingSphereDatabase mockCompleteDatabase() {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class);
        when(result.getProtocolType()).thenReturn(databaseType);
        when(result.getName()).thenReturn("foo_db");
        when(result.isComplete()).thenReturn(true);
        when(result.getResourceMetaData()).thenReturn(new ResourceMetaData(Collections.emptyMap()));
        when(result.getRuleMetaData()).thenReturn(new RuleMetaData(Collections.emptyList()));
        return result;
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
    
    private SQLStatementContext mockSQLStatementContext(final SQLStatement sqlStatement, final Optional<String> schemaName) {
        SQLStatementContext result = mock(SQLStatementContext.class);
        TablesContext tablesContext = mock(TablesContext.class);
        when(tablesContext.getSchemaName()).thenReturn(schemaName);
        when(result.getSqlStatement()).thenReturn(sqlStatement);
        when(result.getTablesContext()).thenReturn(tablesContext);
        return result;
    }
    
    private ExecutionUnit createExecutionUnit(final String dataSourceName, final String sql) {
        return new ExecutionUnit(dataSourceName, new SQLUnit(sql, Collections.emptyList()));
    }
    
    private ContextManager mockContextManager() {
        ContextManager result = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(result.getMetaDataContexts().getMetaData()).thenReturn(createMetaData());
        return result;
    }
    
    private ShardingSphereMetaData createMetaData() {
        return new ShardingSphereMetaData(Collections.emptyList(), new ResourceMetaData(Collections.emptyMap()),
                new RuleMetaData(Collections.singleton(new SQLParserRule(new DefaultSQLParserRuleConfigurationBuilder().build()))), new ConfigurationProperties(new Properties()));
    }
}
