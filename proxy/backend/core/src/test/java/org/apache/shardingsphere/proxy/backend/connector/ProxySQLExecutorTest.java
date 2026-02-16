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

package org.apache.shardingsphere.proxy.backend.connector;

import com.google.common.collect.LinkedHashMultimap;
import lombok.SneakyThrows;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.DialectDatabaseMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.transaction.DialectTransactionOption;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.exception.core.exception.transaction.TableModifyInTransactionException;
import org.apache.shardingsphere.infra.binder.context.segment.table.TablesContext;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroupContext;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroupReportContext;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionContext;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.raw.RawExecutor;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.raw.RawSQLExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.raw.callback.RawSQLExecutorCallback;
import org.apache.shardingsphere.infra.executor.sql.execute.result.ExecuteResult;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.DriverExecutionPrepareEngine;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.jdbc.JDBCDriverType;
import org.apache.shardingsphere.infra.executor.sql.prepare.raw.RawExecutionPrepareEngine;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.attribute.RuleAttributes;
import org.apache.shardingsphere.infra.rule.attribute.raw.RawExecutionRuleAttribute;
import org.apache.shardingsphere.infra.session.connection.transaction.TransactionConnectionContext;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.connector.jdbc.executor.ProxyJDBCExecutor;
import org.apache.shardingsphere.proxy.backend.connector.jdbc.statement.JDBCBackendStatement;
import org.apache.shardingsphere.proxy.backend.connector.sane.DialectSaneQueryResultEngine;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.sql.parser.statement.core.enums.TransactionIsolationLevel;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.CloseStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.CursorStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.FetchStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.MoveStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.TruncateStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.CreateTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sqlfederation.rule.SQLFederationRule;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.apache.shardingsphere.transaction.api.TransactionType;
import org.apache.shardingsphere.transaction.rule.TransactionRule;
import org.apache.shardingsphere.transaction.spi.TransactionHook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.internal.configuration.plugins.Plugins;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.anyCollection;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ProxyContext.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ProxySQLExecutorTest {
    
    private final DatabaseType fixtureDatabaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    private final DatabaseType mysqlDatabaseType = TypedSPILoader.getService(DatabaseType.class, "MySQL");
    
    private final DatabaseType postgresqlDatabaseType = TypedSPILoader.getService(DatabaseType.class, "PostgreSQL");
    
    @Mock
    private TransactionConnectionContext transactionConnectionContext;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ConnectionSession connectionSession;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ProxyDatabaseConnectionManager databaseConnectionManager;
    
    @Mock
    private DatabaseProxyConnector databaseProxyConnector;
    
    @Mock
    private ProxyJDBCExecutor regularExecutor;
    
    @Mock
    private RawExecutor rawExecutor;
    
    @Mock
    private TransactionHook transactionHook;
    
    @Mock
    private TransactionRule transactionRule;
    
    @Mock
    private ShardingSphereRule shardingSphereRule;
    
    @Mock
    private DialectSaneQueryResultEngine saneQueryResultEngine;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereMetaData metaData;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereDatabase database;
    
    @BeforeEach
    void setUp() {
        when(connectionSession.getConnectionContext().getTransactionContext()).thenReturn(transactionConnectionContext);
        when(databaseConnectionManager.getConnectionSession()).thenReturn(connectionSession);
        when(databaseConnectionManager.getCachedConnections()).thenReturn(LinkedHashMultimap.create());
        when(connectionSession.getDatabaseConnectionManager()).thenReturn(databaseConnectionManager);
        when(connectionSession.getIsolationLevel()).thenReturn(Optional.empty());
        when(connectionSession.getStatementManager()).thenReturn(mock(JDBCBackendStatement.class));
        when(database.getName()).thenReturn("foo_db");
        when(database.getProtocolType()).thenReturn(fixtureDatabaseType);
        when(database.getRuleMetaData().getRules()).thenReturn(Collections.emptyList());
        when(metaData.getDatabase("foo_db")).thenReturn(database);
        when(metaData.getAllDatabases()).thenReturn(Collections.singleton(database));
        when(metaData.getProps().<Integer>getValue(ConfigurationPropertyKey.KERNEL_EXECUTOR_SIZE)).thenReturn(0);
        when(metaData.getProps().<Integer>getValue(ConfigurationPropertyKey.MAX_CONNECTIONS_SIZE_PER_QUERY)).thenReturn(1);
        when(metaData.getProps().<Boolean>getValue(ConfigurationPropertyKey.PERSIST_SCHEMAS_TO_REPOSITORY_ENABLED)).thenReturn(true);
        when(transactionRule.getDefaultType()).thenReturn(TransactionType.XA);
        when(metaData.getGlobalRuleMetaData()).thenReturn(new RuleMetaData(Arrays.asList(mock(SQLFederationRule.class), transactionRule)));
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(contextManager.getMetaDataContexts().getMetaData()).thenReturn(metaData);
        when(contextManager.getDatabase("foo_db")).thenReturn(database);
        when(contextManager.getDatabaseType()).thenReturn(fixtureDatabaseType);
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
    }
    
    @Test
    void assertConstructorUseUsedDatabaseWhenCurrentDatabaseNameEmpty() {
        when(connectionSession.getCurrentDatabaseName()).thenReturn("");
        assertNotNull(createProxySQLExecutor("foo_schema", true).getSqlFederationEngine());
    }
    
    @Test
    void assertConstructorUseDefaultSchemaWhenSchemaMissing() {
        when(connectionSession.getCurrentDatabaseName()).thenReturn("foo_db");
        assertNotNull(createProxySQLExecutor("foo_schema", false).getSqlFederationEngine());
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("checkExecutePrerequisitesScenarios")
    void assertCheckExecutePrerequisites(final String name, final SQLStatement sqlStatement,
                                         final TransactionType transactionType, final boolean inTransaction, final boolean hasTable, final boolean expectedThrowException) {
        when(transactionRule.getDefaultType()).thenReturn(transactionType);
        when(connectionSession.getTransactionStatus().isInTransaction()).thenReturn(inTransaction);
        ProxySQLExecutor proxySQLExecutor = createProxySQLExecutor("foo_schema", true);
        SQLStatementContext sqlStatementContext = createCheckStatementContext(sqlStatement, hasTable);
        if (expectedThrowException) {
            assertThrows(TableModifyInTransactionException.class, () -> proxySQLExecutor.checkExecutePrerequisites(sqlStatementContext));
        } else {
            assertDoesNotThrow(() -> proxySQLExecutor.checkExecutePrerequisites(sqlStatementContext));
        }
    }
    
    private Stream<Arguments> checkExecutePrerequisitesScenarios() {
        return Stream.of(
                Arguments.of("ddl-create-mysql-xa-throws", createCreateTableStatement(mysqlDatabaseType), TransactionType.XA, true, true, true),
                Arguments.of("ddl-truncate-mysql-xa-throws", createTruncateStatement(mysqlDatabaseType), TransactionType.XA, true, true, true),
                Arguments.of("ddl-create-postgresql-local-throws", createCreateTableStatement(postgresqlDatabaseType), TransactionType.LOCAL, true, true, true),
                Arguments.of("ddl-create-postgresql-xa-throws", createCreateTableStatement(postgresqlDatabaseType), TransactionType.XA, true, true, true),
                Arguments.of("ddl-create-postgresql-local-empty-table-throws", createCreateTableStatement(postgresqlDatabaseType), TransactionType.LOCAL, true, false, true),
                Arguments.of("ddl-create-mysql-local-pass", createCreateTableStatement(mysqlDatabaseType), TransactionType.LOCAL, true, true, false),
                Arguments.of("ddl-truncate-mysql-local-pass", createTruncateStatement(mysqlDatabaseType), TransactionType.LOCAL, true, true, false),
                Arguments.of("ddl-create-base-transaction-pass", createCreateTableStatement(mysqlDatabaseType), TransactionType.BASE, true, true, false),
                Arguments.of("ddl-create-mysql-not-in-transaction-pass", createCreateTableStatement(mysqlDatabaseType), TransactionType.XA, false, true, false),
                Arguments.of("ddl-create-postgresql-not-in-transaction-pass", createCreateTableStatement(postgresqlDatabaseType), TransactionType.LOCAL, false, true, false),
                Arguments.of("ddl-truncate-postgresql-local-pass", createTruncateStatement(postgresqlDatabaseType), TransactionType.LOCAL, true, true, false),
                Arguments.of("ddl-cursor-postgresql-local-pass", new CursorStatement(postgresqlDatabaseType, null, null), TransactionType.LOCAL, true, true, false),
                Arguments.of("ddl-close-postgresql-local-pass", new CloseStatement(postgresqlDatabaseType, null, false), TransactionType.LOCAL, true, true, false),
                Arguments.of("ddl-move-postgresql-local-pass", new MoveStatement(postgresqlDatabaseType, null, null), TransactionType.LOCAL, true, true, false),
                Arguments.of("ddl-fetch-postgresql-local-pass", new FetchStatement(postgresqlDatabaseType, null, null), TransactionType.LOCAL, true, true, false),
                Arguments.of("ddl-truncate-postgresql-xa-pass", createTruncateStatement(postgresqlDatabaseType), TransactionType.XA, true, true, false),
                Arguments.of("dml-insert-mysql-xa-pass", createInsertStatement(mysqlDatabaseType), TransactionType.XA, true, true, false));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("executeScenarios")
    void assertExecute(final String name, final boolean hasRawExecutionRule, final SQLStatement sqlStatement, final boolean inTransaction,
                       final boolean expectedHookInvoked, final boolean isReturnGeneratedKeys) throws SQLException {
        when(connectionSession.getUsedDatabaseName()).thenReturn("foo_db");
        when(transactionConnectionContext.isInTransaction()).thenReturn(inTransaction);
        when(database.getRuleMetaData().getRules()).thenReturn(createRules(hasRawExecutionRule));
        ProxySQLExecutor proxySQLExecutor = createProxySQLExecutor("foo_schema", true);
        setExecutorField(proxySQLExecutor, "rawExecutor", rawExecutor);
        setExecutorField(proxySQLExecutor, "regularExecutor", regularExecutor);
        setExecutorField(proxySQLExecutor, "transactionHooks", Collections.singletonMap(shardingSphereRule, transactionHook));
        ExecutionContext executionContext = createExecutionContext(sqlStatement);
        ExecuteResult expectedExecuteResult = mock(ExecuteResult.class);
        List<ExecuteResult> expected = Collections.singletonList(expectedExecuteResult);
        if (hasRawExecutionRule) {
            ExecutionGroupContext<RawSQLExecutionUnit> rawExecutionGroupContext = mock(ExecutionGroupContext.class);
            try (
                    MockedConstruction<RawExecutionPrepareEngine> ignored = mockConstruction(RawExecutionPrepareEngine.class,
                            (mock, context) -> when(mock.prepare(anyString(), eq(executionContext), anyCollection(), any(ExecutionGroupReportContext.class)))
                                    .thenReturn(rawExecutionGroupContext))) {
                try (MockedConstruction<RawSQLExecutorCallback> ignoredCallback = mockConstruction(RawSQLExecutorCallback.class)) {
                    when(rawExecutor.execute(eq(rawExecutionGroupContext), any(), any(RawSQLExecutorCallback.class))).thenReturn(expected);
                    assertThat(proxySQLExecutor.execute(executionContext), is(expected));
                }
            }
            return;
        }
        ExecutionGroupContext<JDBCExecutionUnit> jdbcExecutionGroupContext = mock(ExecutionGroupContext.class);
        try (
                MockedConstruction<DriverExecutionPrepareEngine> ignored = mockConstruction(DriverExecutionPrepareEngine.class,
                        (mock, context) -> when(mock.prepare(anyString(), eq(executionContext), anyCollection(), any(ExecutionGroupReportContext.class))).thenReturn(jdbcExecutionGroupContext))) {
            when(regularExecutor.execute(any(), eq(jdbcExecutionGroupContext), eq(isReturnGeneratedKeys), anyBoolean())).thenReturn(expected);
            assertThat(proxySQLExecutor.execute(executionContext), is(expected));
        }
        verify(regularExecutor).execute(any(), eq(jdbcExecutionGroupContext), eq(isReturnGeneratedKeys), anyBoolean());
        if (expectedHookInvoked) {
            verify(transactionHook).beforeExecuteSQL(eq(shardingSphereRule), eq(fixtureDatabaseType), anyCollection(), eq(transactionConnectionContext), eq(TransactionIsolationLevel.READ_COMMITTED));
        } else {
            verify(transactionHook, never()).beforeExecuteSQL(any(), any(), anyCollection(), any(), any());
        }
        
    }
    
    private Stream<Arguments> executeScenarios() {
        return Stream.of(
                Arguments.of("execute-with-raw-rule", true, createCreateTableStatement(mysqlDatabaseType), true, false, false),
                Arguments.of("execute-with-driver-and-generated-keys", false, createInsertStatement(mysqlDatabaseType), true, true, true),
                Arguments.of("execute-with-driver-and-no-transaction", false, createInsertStatement(postgresqlDatabaseType), false, false, false));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("executeFallbackScenarios")
    void assertExecuteFallback(final String name, final boolean hasRawExecutionRule, final SQLStatement sqlStatement, final boolean hasSaneResult) throws SQLException {
        when(connectionSession.getUsedDatabaseName()).thenReturn("foo_db");
        when(database.getRuleMetaData().getRules()).thenReturn(createRules(hasRawExecutionRule));
        ProxySQLExecutor proxySQLExecutor = createProxySQLExecutor("foo_schema", true);
        setExecutorField(proxySQLExecutor, "rawExecutor", rawExecutor);
        setExecutorField(proxySQLExecutor, "regularExecutor", regularExecutor);
        setExecutorField(proxySQLExecutor, "transactionHooks", Collections.singletonMap(shardingSphereRule, transactionHook));
        ExecutionContext executionContext = createExecutionContext(sqlStatement);
        SQLException expectedException = new SQLException("mock prepare failure");
        try (MockedStatic<DatabaseTypedSPILoader> mockedDatabaseTypedSPILoader = mockStatic(DatabaseTypedSPILoader.class, CALLS_REAL_METHODS)) {
            mockedDatabaseTypedSPILoader.when(() -> DatabaseTypedSPILoader.findService(DialectSaneQueryResultEngine.class, fixtureDatabaseType)).thenReturn(Optional.of(saneQueryResultEngine));
            if (hasSaneResult) {
                ExecuteResult saneExecuteResult = mock(ExecuteResult.class);
                when(saneQueryResultEngine.getSaneQueryResult(sqlStatement, expectedException)).thenReturn(Optional.of(saneExecuteResult));
                if (hasRawExecutionRule) {
                    try (
                            MockedConstruction<RawExecutionPrepareEngine> ignored = mockConstruction(RawExecutionPrepareEngine.class,
                                    (mock, context) -> when(mock.prepare(anyString(), eq(executionContext), anyCollection(), any(ExecutionGroupReportContext.class)))
                                            .thenThrow(expectedException))) {
                        assertThat(proxySQLExecutor.execute(executionContext), is(Collections.singletonList(saneExecuteResult)));
                    }
                    return;
                }
                try (
                        MockedConstruction<DriverExecutionPrepareEngine> ignored = mockConstruction(DriverExecutionPrepareEngine.class,
                                (mock, context) -> when(mock.prepare(anyString(), eq(executionContext), anyCollection(), any(ExecutionGroupReportContext.class)))
                                        .thenThrow(expectedException))) {
                    assertThat(proxySQLExecutor.execute(executionContext), is(Collections.singletonList(saneExecuteResult)));
                }
                return;
            }
            when(saneQueryResultEngine.getSaneQueryResult(sqlStatement, expectedException)).thenReturn(Optional.empty());
            if (hasRawExecutionRule) {
                try (
                        MockedConstruction<RawExecutionPrepareEngine> ignored = mockConstruction(RawExecutionPrepareEngine.class,
                                (mock, context) -> when(mock.prepare(anyString(), eq(executionContext), anyCollection(), any(ExecutionGroupReportContext.class)))
                                        .thenThrow(expectedException))) {
                    SQLException actual = assertThrows(SQLException.class, () -> proxySQLExecutor.execute(executionContext));
                    assertThat(actual, is(expectedException));
                }
                return;
            }
            try (
                    MockedConstruction<DriverExecutionPrepareEngine> ignored = mockConstruction(DriverExecutionPrepareEngine.class,
                            (mock, context) -> when(mock.prepare(anyString(), eq(executionContext), anyCollection(), any(ExecutionGroupReportContext.class)))
                                    .thenThrow(expectedException))) {
                assertThat(assertThrows(SQLException.class, () -> proxySQLExecutor.execute(executionContext)), is(expectedException));
            }
        }
    }
    
    private Stream<Arguments> executeFallbackScenarios() {
        return Stream.of(
                Arguments.of("raw-prepare-failed-with-sane-result", true, createCreateTableStatement(mysqlDatabaseType), true),
                Arguments.of("driver-prepare-failed-with-sane-result", false, createInsertStatement(postgresqlDatabaseType), true),
                Arguments.of("driver-prepare-failed-throws-original", false, createInsertStatement(postgresqlDatabaseType), false));
    }
    
    @Test
    void assertCheckExecutePrerequisitesWithMetaDataRefreshInXATransaction() {
        DatabaseType databaseType = mock(DatabaseType.class);
        DialectDatabaseMetaData dialectDatabaseMetaData = mock(DialectDatabaseMetaData.class);
        when(dialectDatabaseMetaData.getTransactionOption()).thenReturn(new DialectTransactionOption(false, false, false, true, true,
                Connection.TRANSACTION_READ_COMMITTED, false, false, Collections.emptyList()));
        when(transactionRule.getDefaultType()).thenReturn(TransactionType.XA);
        when(connectionSession.getTransactionStatus().isInTransaction()).thenReturn(true);
        try (MockedStatic<DatabaseTypedSPILoader> mockedDatabaseTypedSPILoader = mockStatic(DatabaseTypedSPILoader.class, CALLS_REAL_METHODS)) {
            mockedDatabaseTypedSPILoader.when(() -> DatabaseTypedSPILoader.getService(DialectDatabaseMetaData.class, databaseType)).thenReturn(dialectDatabaseMetaData);
            ProxySQLExecutor proxySQLExecutor = createProxySQLExecutor("foo_schema", true);
            assertDoesNotThrow(() -> proxySQLExecutor.checkExecutePrerequisites(createCheckStatementContext(createCreateTableStatement(databaseType), true)));
        }
    }
    
    private ProxySQLExecutor createProxySQLExecutor(final String schemaName, final boolean hasSchemaName) {
        return new ProxySQLExecutor(JDBCDriverType.STATEMENT, databaseConnectionManager, databaseProxyConnector, createConstructorStatementContext(schemaName, hasSchemaName));
    }
    
    private SQLStatementContext createConstructorStatementContext(final String schemaName, final boolean hasSchemaName) {
        SQLStatementContext result = mock(SQLStatementContext.class, RETURNS_DEEP_STUBS);
        when(result.getSqlStatement().getDatabaseType()).thenReturn(fixtureDatabaseType);
        when(result.getTablesContext().getSchemaName()).thenReturn(hasSchemaName ? Optional.of(schemaName) : Optional.empty());
        return result;
    }
    
    private SQLStatementContext createCheckStatementContext(final SQLStatement sqlStatement, final boolean hasTable) {
        SQLStatementContext result = mock(SQLStatementContext.class);
        when(result.getSqlStatement()).thenReturn(sqlStatement);
        when(result.getTablesContext()).thenReturn(new TablesContext(hasTable ? new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order"))) : null));
        return result;
    }
    
    private ExecutionContext createExecutionContext(final SQLStatement sqlStatement) {
        SQLStatementContext sqlStatementContext = mock(SQLStatementContext.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(sqlStatement);
        ExecutionContext result = mock(ExecutionContext.class);
        when(result.getSqlStatementContext()).thenReturn(sqlStatementContext);
        return result;
    }
    
    private Collection<ShardingSphereRule> createRules(final boolean hasRawExecutionRule) {
        RuleAttributes attributes = hasRawExecutionRule ? new RuleAttributes(mock(RawExecutionRuleAttribute.class)) : new RuleAttributes();
        ShardingSphereRule result = new ShardingSphereRule() {
            
            @Override
            public RuleConfiguration getConfiguration() {
                return null;
            }
            
            @Override
            public RuleAttributes getAttributes() {
                return attributes;
            }
            
            @Override
            public int getOrder() {
                return 0;
            }
        };
        return Collections.singletonList(result);
    }
    
    private CreateTableStatement createCreateTableStatement(final DatabaseType databaseType) {
        CreateTableStatement result = new CreateTableStatement(databaseType);
        result.setTable(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order"))));
        return result;
    }
    
    private TruncateStatement createTruncateStatement(final DatabaseType databaseType) {
        return new TruncateStatement(databaseType, Collections.singleton(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order")))));
    }
    
    private InsertStatement createInsertStatement(final DatabaseType databaseType) {
        InsertStatement result = new InsertStatement(databaseType);
        result.setTable(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order"))));
        return result;
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private void setExecutorField(final ProxySQLExecutor target, final String fieldName, final Object value) {
        Plugins.getMemberAccessor().set(ProxySQLExecutor.class.getDeclaredField(fieldName), target, value);
    }
}
