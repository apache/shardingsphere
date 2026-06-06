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

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.DialectDatabaseMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.transaction.DialectTransactionOption;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.driver.jdbc.core.resultset.ShardingSphereResultSetMetaData;
import org.apache.shardingsphere.infra.binder.context.segment.insert.keygen.GeneratedKeyContext;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.Projection;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.ProjectionsContext;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.ddl.CursorHeldSQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.ddl.CursorStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.connection.kernel.KernelProcessor;
import org.apache.shardingsphere.infra.exception.kernel.metadata.resource.storageunit.EmptyStorageUnitException;
import org.apache.shardingsphere.infra.exception.kernel.metadata.rule.EmptyRuleException;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionContext;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.context.SQLUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.result.ExecuteResult;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResultMetaData;
import org.apache.shardingsphere.infra.executor.sql.execute.result.update.UpdateResult;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.jdbc.JDBCDriverType;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.merge.MergeEngine;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.merge.result.impl.memory.MemoryMergedResult;
import org.apache.shardingsphere.infra.merge.result.impl.memory.MemoryQueryResultRow;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereIndex;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.database.schema.util.SystemSchemaUtils;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereStatistics;
import org.apache.shardingsphere.infra.metadata.statistics.builder.ShardingSphereStatisticsFactory;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.attribute.datanode.DataNodeRuleAttribute;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.infra.session.connection.cursor.CursorConnectionContext;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.refresher.federation.FederationMetaDataRefreshEngine;
import org.apache.shardingsphere.mode.metadata.refresher.pushdown.PushDownMetaDataRefreshEngine;
import org.apache.shardingsphere.parser.config.SQLParserRuleConfiguration;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.proxy.backend.connector.jdbc.fixture.QueryHeaderBuilderFixture;
import org.apache.shardingsphere.proxy.backend.connector.jdbc.datasource.JDBCBackendDataSource;
import org.apache.shardingsphere.proxy.backend.connector.jdbc.executor.ProxyJDBCExecutor;
import org.apache.shardingsphere.proxy.backend.connector.jdbc.statement.JDBCBackendStatement;
import org.apache.shardingsphere.proxy.backend.connector.jdbc.transaction.ProxyBackendTransactionManager;
import org.apache.shardingsphere.proxy.backend.context.BackendExecutorContext;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.exception.BackendConnectionException;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryHeaderBuilder;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryHeaderBuilderEngine;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.session.PreparedStatementCacheKey;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.engine.api.CacheOption;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.cursor.CursorNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.CloseStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.CursorStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sqlfederation.engine.SQLFederationEngine;
import org.apache.shardingsphere.sqlfederation.rule.SQLFederationRule;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.AfterEach;
import org.apache.shardingsphere.infra.metadata.user.Grantee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@StaticMockSettings({ProxyContext.class, SystemSchemaUtils.class})
class StandardDatabaseProxyConnectorTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ProxyDatabaseConnectionManager databaseConnectionManager;
    
    @Mock
    private Statement statement;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ResultSet resultSet;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private SQLFederationRule sqlFederationRule;
    
    @BeforeEach
    void setUp() {
        when(databaseConnectionManager.getConnectionSession().getCurrentDatabaseName()).thenReturn("foo_db");
        when(databaseConnectionManager.getConnectionSession().getUsedDatabaseName()).thenReturn("foo_db");
        ContextManager contextManager = mockContextManager();
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        BackendExecutorContext.getInstance().init();
    }
    
    @AfterEach
    void tearDown() {
        BackendExecutorContext.getInstance().shutdown();
    }
    
    private ContextManager mockContextManager() {
        RuleMetaData globalRuleMetaData = new RuleMetaData(Arrays.asList(new SQLParserRule(new SQLParserRuleConfiguration(mock(CacheOption.class), mock(CacheOption.class))), sqlFederationRule));
        ShardingSphereDatabase database = mockDatabase();
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(Collections.singleton(database), mock(ResourceMetaData.class), globalRuleMetaData, new ConfigurationProperties(new Properties()));
        MetaDataContexts metaDataContexts = new MetaDataContexts(metaData, ShardingSphereStatisticsFactory.create(metaData, new ShardingSphereStatistics()));
        ContextManager result = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(result.getMetaDataContexts()).thenReturn(metaDataContexts);
        when(result.getDatabase("foo_db")).thenReturn(database);
        return result;
    }
    
    private ShardingSphereDatabase mockDatabase() {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(result.getName()).thenReturn("foo_db");
        when(result.containsDataSource()).thenReturn(true);
        when(result.isComplete()).thenReturn(true);
        when(result.getProtocolType()).thenReturn(databaseType);
        when(result.getRuleMetaData().getRules()).thenReturn(Collections.singleton(mock(ShardingRule.class)));
        when(result.getRuleMetaData().getAttributes(DataNodeRuleAttribute.class)).thenReturn(Collections.emptyList());
        return result;
    }
    
    @Test
    void assertBinaryProtocolQueryHeader() throws SQLException, NoSuchFieldException, IllegalAccessException {
        SQLStatementContext sqlStatementContext = mock(SQLStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getTablesContext().getDatabaseNames()).thenReturn(Collections.emptyList());
        when(sqlStatementContext.getSqlStatement().getDatabaseType()).thenReturn(databaseType);
        DatabaseProxyConnector engine = createDatabaseProxyConnector(JDBCDriverType.PREPARED_STATEMENT, createQueryContext(sqlStatementContext, mockDatabase()));
        Field queryHeadersField = StandardDatabaseProxyConnector.class.getDeclaredField("queryHeaders");
        ShardingSphereDatabase database = createDatabaseMetaData();
        try (MockedStatic<DatabaseTypedSPILoader> spiLoader = mockStatic(DatabaseTypedSPILoader.class)) {
            spiLoader.when(() -> DatabaseTypedSPILoader.getService(QueryHeaderBuilder.class, databaseType)).thenReturn(new QueryHeaderBuilderFixture());
            Plugins.getMemberAccessor().set(queryHeadersField, engine, Collections.singletonList(new QueryHeaderBuilderEngine(databaseType).build(createResultSetMetaData(), database, 1)));
            Field mergedResultField = StandardDatabaseProxyConnector.class.getDeclaredField("mergedResult");
            Plugins.getMemberAccessor().set(mergedResultField, engine, new MemoryMergedResult<ShardingSphereRule>(null, null, null, Collections.emptyList()) {
                
                @Override
                protected List<MemoryQueryResultRow> init(final ShardingSphereRule rule, final ShardingSphereSchema schema,
                                                          final SQLStatementContext sqlStatementContext, final List<QueryResult> queryResults) {
                    return Collections.singletonList(mock(MemoryQueryResultRow.class));
                }
            });
            Exception ex = null;
            try {
                engine.getRowData();
            } catch (final SQLException | IndexOutOfBoundsException exception) {
                ex = exception;
            } finally {
                assertFalse(ex instanceof IndexOutOfBoundsException);
            }
        }
    }
    
    @Test
    void assertExecuteWithFederationMetaDataRefresh() throws SQLException {
        SQLStatementContext sqlStatementContext = createSQLStatementContext(new SQLStatement(databaseType));
        QueryContext queryContext = createQueryContext(sqlStatementContext, mockDatabase());
        ProxySQLExecutor proxySQLExecutor = mock(ProxySQLExecutor.class, RETURNS_DEEP_STUBS);
        when(proxySQLExecutor.getSqlFederationEngine().isSQLFederationEnabled()).thenReturn(true);
        DatabaseProxyConnector engine = createDatabaseProxyConnector(JDBCDriverType.STATEMENT, queryContext);
        setField(engine, "proxySQLExecutor", proxySQLExecutor);
        try (
                MockedConstruction<FederationMetaDataRefreshEngine> mockedConstruction = mockConstruction(FederationMetaDataRefreshEngine.class,
                        (mock, context) -> when(mock.isNeedRefresh()).thenReturn(true))) {
            assertThat(engine.execute(), isA(UpdateResponseHeader.class));
            FederationMetaDataRefreshEngine federationMetaDataRefreshEngine = mockedConstruction.constructed().iterator().next();
            verify(federationMetaDataRefreshEngine).refresh(any(), any(ShardingSphereDatabase.class));
        }
    }
    
    @Test
    void assertExecuteWithImplicitCommitTransaction() throws SQLException {
        when(databaseConnectionManager.getConnectionSession().isAutoCommit()).thenReturn(true);
        when(databaseConnectionManager.getConnectionSession().getConnectionContext().getTransactionContext().getTransactionType()).thenReturn(Optional.of("XA"));
        SQLStatementContext sqlStatementContext = createSQLStatementContext(InsertStatement.builder().databaseType(databaseType).build());
        DatabaseProxyConnector engine = createDatabaseProxyConnector(JDBCDriverType.STATEMENT, createQueryContext(sqlStatementContext, mockDatabase()));
        ProxySQLExecutor proxySQLExecutor = mock(ProxySQLExecutor.class, RETURNS_DEEP_STUBS);
        setField(engine, "proxySQLExecutor", proxySQLExecutor);
        ExecutionContext executionContext = mock(ExecutionContext.class, RETURNS_DEEP_STUBS);
        when(executionContext.getExecutionUnits()).thenReturn(Arrays.asList(mock(ExecutionUnit.class), mock(ExecutionUnit.class)));
        when(executionContext.getSqlStatementContext()).thenReturn(sqlStatementContext);
        when(proxySQLExecutor.execute(executionContext)).thenReturn(Collections.singletonList(new UpdateResult(1, 0L)));
        try (
                MockedConstruction<KernelProcessor> mockedKernelProcessor = mockConstruction(KernelProcessor.class,
                        (mock, context) -> when(mock.generateExecutionContext(any(QueryContext.class), any(RuleMetaData.class), any(ConfigurationProperties.class))).thenReturn(executionContext));
                MockedConstruction<DatabaseTypeRegistry> mockedDatabaseTypeRegistry = mockConstruction(DatabaseTypeRegistry.class,
                        (mock, context) -> when(mock.getDialectDatabaseMetaData()).thenReturn(mock(DialectDatabaseMetaData.class)));
                MockedConstruction<ProxyBackendTransactionManager> mockedTransactionManager = mockConstruction(ProxyBackendTransactionManager.class);
                MockedStatic<ShardingSphereServiceLoader> serviceLoader = mockStatic(ShardingSphereServiceLoader.class)) {
            serviceLoader.when(() -> ShardingSphereServiceLoader.getServiceInstances(AdvancedProxySQLExecutor.class)).thenReturn(Collections.emptyList());
            assertThat(engine.execute(), isA(UpdateResponseHeader.class));
            assertThat(mockedKernelProcessor.constructed().size(), is(1));
            assertThat(mockedDatabaseTypeRegistry.constructed().size(), is(1));
            ProxyBackendTransactionManager transactionManager = mockedTransactionManager.constructed().iterator().next();
            verify(transactionManager).begin();
            verify(transactionManager).commit();
        }
    }
    
    @Test
    void assertExecuteWithImplicitCommitTransactionAndException() throws SQLException {
        when(databaseConnectionManager.getConnectionSession().isAutoCommit()).thenReturn(true);
        when(databaseConnectionManager.getConnectionSession().getConnectionContext().getTransactionContext().getTransactionType()).thenReturn(Optional.of("XA"));
        SQLStatementContext sqlStatementContext = createSQLStatementContext(InsertStatement.builder().databaseType(databaseType).build());
        ProxySQLExecutor proxySQLExecutor = mock(ProxySQLExecutor.class, RETURNS_DEEP_STUBS);
        DatabaseProxyConnector engine = createDatabaseProxyConnector(JDBCDriverType.STATEMENT, createQueryContext(sqlStatementContext, mockDatabase()));
        setField(engine, "proxySQLExecutor", proxySQLExecutor);
        ExecutionContext executionContext = mock(ExecutionContext.class, RETURNS_DEEP_STUBS);
        when(executionContext.getExecutionUnits()).thenReturn(Arrays.asList(mock(ExecutionUnit.class), mock(ExecutionUnit.class)));
        when(executionContext.getSqlStatementContext()).thenReturn(sqlStatementContext);
        when(proxySQLExecutor.execute(executionContext)).thenThrow(RuntimeException.class);
        try (
                MockedConstruction<KernelProcessor> mockedKernelProcessor = mockConstruction(KernelProcessor.class,
                        (mock, context) -> when(mock.generateExecutionContext(any(QueryContext.class), any(RuleMetaData.class), any(ConfigurationProperties.class))).thenReturn(executionContext));
                MockedConstruction<DatabaseTypeRegistry> ignoredDatabaseTypeRegistry = mockConstruction(DatabaseTypeRegistry.class,
                        (mock, context) -> when(mock.getDialectDatabaseMetaData()).thenReturn(mock(DialectDatabaseMetaData.class)));
                MockedConstruction<ProxyBackendTransactionManager> mockedTransactionManager = mockConstruction(ProxyBackendTransactionManager.class);
                MockedStatic<ShardingSphereServiceLoader> serviceLoader = mockStatic(ShardingSphereServiceLoader.class)) {
            serviceLoader.when(() -> ShardingSphereServiceLoader.getServiceInstances(AdvancedProxySQLExecutor.class)).thenReturn(Collections.emptyList());
            assertThrows(SQLException.class, engine::execute);
            assertThat(mockedKernelProcessor.constructed().size(), is(1));
            ProxyBackendTransactionManager transactionManager = mockedTransactionManager.constructed().iterator().next();
            verify(transactionManager).begin();
            verify(transactionManager).rollback();
        }
    }
    
    @Test
    void assertExecuteWithUpdateResultAndAccumulate() throws SQLException {
        InsertStatementContext sqlStatementContext = mock(InsertStatementContext.class, RETURNS_DEEP_STUBS);
        InsertStatement insertStatement = InsertStatement.builder().databaseType(databaseType).build();
        when(sqlStatementContext.getSqlStatement()).thenReturn(insertStatement);
        when(sqlStatementContext.getTablesContext().getDatabaseNames()).thenReturn(Collections.emptyList());
        when(sqlStatementContext.getTablesContext().getSchemaNames()).thenReturn(Collections.emptyList());
        when(sqlStatementContext.getTablesContext().getTableNames()).thenReturn(Collections.singleton("t_order"));
        GeneratedKeyContext generatedKeyContext = new GeneratedKeyContext("order_id", true);
        generatedKeyContext.setSupportAutoIncrement(true);
        generatedKeyContext.getGeneratedValues().add(2L);
        when(sqlStatementContext.getGeneratedKeyContext()).thenReturn(Optional.of(generatedKeyContext));
        DataNodeRuleAttribute dataNodeRuleAttribute = mock(DataNodeRuleAttribute.class);
        when(dataNodeRuleAttribute.isNeedAccumulate(any(Collection.class))).thenReturn(true);
        ShardingSphereDatabase database = mockDatabase();
        when(database.getRuleMetaData().getAttributes(DataNodeRuleAttribute.class)).thenReturn(Collections.singleton(dataNodeRuleAttribute));
        DatabaseProxyConnector engine = createDatabaseProxyConnector(JDBCDriverType.STATEMENT, createQueryContext(sqlStatementContext, database));
        setField(engine, "proxySQLExecutor", mock(ProxySQLExecutor.class, RETURNS_DEEP_STUBS));
        ExecutionContext executionContext = mock(ExecutionContext.class, RETURNS_DEEP_STUBS);
        when(executionContext.getExecutionUnits()).thenReturn(Collections.singletonList(mock(ExecutionUnit.class)));
        when(executionContext.getSqlStatementContext()).thenReturn(sqlStatementContext);
        when(executionContext.getRouteContext().getRouteUnits()).thenReturn(Collections.emptyList());
        AdvancedProxySQLExecutor advancedProxySQLExecutor = mock(AdvancedProxySQLExecutor.class);
        when(advancedProxySQLExecutor.execute(any(ExecutionContext.class), any(ContextManager.class), any(ShardingSphereDatabase.class), any(DatabaseProxyConnector.class)))
                .thenReturn(Arrays.asList(new UpdateResult(1, 4L), new UpdateResult(2, 5L)));
        try (
                MockedConstruction<KernelProcessor> mockedKernelProcessor = mockConstruction(KernelProcessor.class,
                        (mock, context) -> when(mock.generateExecutionContext(any(QueryContext.class), any(RuleMetaData.class), any(ConfigurationProperties.class))).thenReturn(executionContext));
                MockedConstruction<DatabaseTypeRegistry> mockedDatabaseTypeRegistry = mockConstruction(DatabaseTypeRegistry.class,
                        (mock, context) -> when(mock.getDialectDatabaseMetaData()).thenReturn(mock(DialectDatabaseMetaData.class)));
                MockedConstruction<PushDownMetaDataRefreshEngine> mockedPushDownMetaDataRefreshEngine = mockConstruction(PushDownMetaDataRefreshEngine.class,
                        (mock, context) -> when(mock.isNeedRefresh()).thenReturn(true));
                MockedStatic<ShardingSphereServiceLoader> serviceLoader = mockStatic(ShardingSphereServiceLoader.class)) {
            serviceLoader.when(() -> ShardingSphereServiceLoader.getServiceInstances(AdvancedProxySQLExecutor.class))
                    .thenReturn(Collections.singleton(advancedProxySQLExecutor));
            UpdateResponseHeader actual = (UpdateResponseHeader) engine.execute();
            assertThat(actual.getUpdateCount(), is(3L));
            assertThat(actual.getLastInsertId(), is(2L));
            assertThat(mockedKernelProcessor.constructed().size(), is(1));
            assertThat(mockedDatabaseTypeRegistry.constructed().size(), is(1));
            PushDownMetaDataRefreshEngine pushDownMetaDataRefreshEngine = mockedPushDownMetaDataRefreshEngine.constructed().iterator().next();
            verify(pushDownMetaDataRefreshEngine).refresh(any(), eq(database), any(ConfigurationProperties.class), any(Collection.class));
        }
    }
    
    @Test
    void assertConstructWithSystemSchema() {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getName()).thenReturn("foo_db");
        when(SystemSchemaUtils.containsSystemSchema(any(DatabaseType.class), any(Collection.class), any(ShardingSphereDatabase.class))).thenReturn(true);
        assertNotNull(createDatabaseProxyConnector(JDBCDriverType.STATEMENT, createQueryContext(createSQLStatementContext(new SQLStatement(databaseType)), database)));
    }
    
    @Test
    void assertConstructWithEmptyStorageUnitException() {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getName()).thenReturn("foo_db");
        when(database.isComplete()).thenReturn(true);
        QueryContext queryContext = createQueryContext(createSQLStatementContext(new SQLStatement(databaseType)), database);
        assertNotNull(assertThrows(EmptyStorageUnitException.class, () -> createDatabaseProxyConnector(JDBCDriverType.STATEMENT, queryContext)));
    }
    
    @Test
    void assertConstructWithEmptyRuleException() {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getName()).thenReturn("foo_db");
        when(database.containsDataSource()).thenReturn(true);
        QueryContext queryContext = createQueryContext(createSQLStatementContext(new SQLStatement(databaseType)), database);
        assertNotNull(assertThrows(EmptyRuleException.class, () -> createDatabaseProxyConnector(JDBCDriverType.STATEMENT, queryContext)));
    }
    
    @Test
    void assertConstructWithCloseAllCursorStatement() {
        CloseStatement closeStatement = new CloseStatement(databaseType, null, true);
        SQLStatementContext sqlStatementContext = createSQLStatementContext(closeStatement);
        ConnectionContext connectionContext = mock(ConnectionContext.class, RETURNS_DEEP_STUBS);
        when(databaseConnectionManager.getConnectionSession().getConnectionContext()).thenReturn(connectionContext);
        createDatabaseProxyConnector(JDBCDriverType.STATEMENT, createQueryContext(sqlStatementContext, mockDatabase()));
        verify(connectionContext).clearCursorContext();
    }
    
    @Test
    void assertConstructWithNotExistedCursorHeldSQLStatementContext() {
        CursorNameSegment cursorNameSegment = new CursorNameSegment(0, 0, new IdentifierValue("foo_cursor"));
        CloseStatement closeStatement = new CloseStatement(databaseType, cursorNameSegment, false);
        CursorHeldSQLStatementContext sqlStatementContext = new CursorHeldSQLStatementContext(closeStatement);
        when(databaseConnectionManager.getConnectionSession().getConnectionContext().getCursorContext()).thenReturn(new CursorConnectionContext());
        assertNotNull(assertThrows(IllegalArgumentException.class, () -> createDatabaseProxyConnector(JDBCDriverType.STATEMENT, createQueryContext(sqlStatementContext, mockDatabase()))));
    }
    
    @Test
    void assertConstructWithCursorHeldSQLStatementContext() {
        CursorNameSegment cursorNameSegment = new CursorNameSegment(0, 0, new IdentifierValue("foo_cursor"));
        CloseStatement closeStatement = new CloseStatement(databaseType, cursorNameSegment, false);
        CursorHeldSQLStatementContext sqlStatementContext = new CursorHeldSQLStatementContext(closeStatement);
        CursorConnectionContext cursorConnectionContext = new CursorConnectionContext();
        CursorStatementContext cursorStatementContext = mock(CursorStatementContext.class, RETURNS_DEEP_STUBS);
        cursorConnectionContext.getCursorStatementContexts().put("foo_cursor", cursorStatementContext);
        when(databaseConnectionManager.getConnectionSession().getConnectionContext().getCursorContext()).thenReturn(cursorConnectionContext);
        createDatabaseProxyConnector(JDBCDriverType.STATEMENT, createQueryContext(sqlStatementContext, mockDatabase()));
        assertThat(sqlStatementContext.getCursorStatementContext(), is(cursorStatementContext));
        assertFalse(cursorConnectionContext.getCursorStatementContexts().containsKey("foo_cursor"));
    }
    
    @Test
    void assertConstructWithCursorStatementContext() {
        CursorNameSegment cursorNameSegment = new CursorNameSegment(0, 0, new IdentifierValue("foo_cursor"));
        CursorStatement cursorStatement = new CursorStatement(databaseType, cursorNameSegment, mock(SelectStatement.class));
        CursorStatementContext sqlStatementContext = mock(CursorStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getSqlStatement()).thenReturn(cursorStatement);
        when(sqlStatementContext.getTablesContext().getDatabaseNames()).thenReturn(Collections.emptyList());
        when(sqlStatementContext.getTablesContext().getSchemaNames()).thenReturn(Collections.emptyList());
        CursorConnectionContext cursorConnectionContext = new CursorConnectionContext();
        when(databaseConnectionManager.getConnectionSession().getConnectionContext().getCursorContext()).thenReturn(cursorConnectionContext);
        createDatabaseProxyConnector(JDBCDriverType.STATEMENT, createQueryContext(sqlStatementContext, mockDatabase()));
        assertThat(cursorConnectionContext.getCursorStatementContexts().get("foo_cursor"), is(sqlStatementContext));
    }
    
    @Test
    void assertConstructWithSelectStatementContextWithoutDerivedProjections() {
        SelectStatementContext sqlStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        SelectStatement selectStatement = SelectStatement.builder().databaseType(databaseType).build();
        when(sqlStatementContext.getSqlStatement()).thenReturn(selectStatement);
        when(sqlStatementContext.getTablesContext().getDatabaseNames()).thenReturn(Collections.emptyList());
        when(sqlStatementContext.getTablesContext().getSchemaNames()).thenReturn(Collections.emptyList());
        assertNotNull(createDatabaseProxyConnector(JDBCDriverType.STATEMENT, createQueryContext(sqlStatementContext, mockDatabase())));
    }
    
    @Test
    void assertExecuteWithFederation() throws SQLException {
        SQLStatementContext sqlStatementContext = createSQLStatementContext(SelectStatement.builder().databaseType(databaseType).build());
        QueryContext queryContext = createQueryContext(sqlStatementContext, mockDatabase());
        ProxySQLExecutor proxySQLExecutor = mock(ProxySQLExecutor.class, RETURNS_DEEP_STUBS);
        SQLFederationEngine sqlFederationEngine = mock(SQLFederationEngine.class);
        when(proxySQLExecutor.getSqlFederationEngine()).thenReturn(sqlFederationEngine);
        when(sqlFederationEngine.decide(any(QueryContext.class), any(RuleMetaData.class))).thenReturn(true);
        when(sqlFederationEngine.executeQuery(any(), any(), any())).thenReturn(resultSet);
        when(databaseConnectionManager.getConnectionSession().getStatementManager()).thenReturn(mock(JDBCBackendStatement.class));
        when(resultSet.getMetaData().getColumnCount()).thenReturn(1);
        when(resultSet.getMetaData().getColumnName(1)).thenReturn("order_id");
        when(resultSet.getMetaData().getColumnLabel(1)).thenReturn("order_id");
        DatabaseProxyConnector engine = createDatabaseProxyConnector(JDBCDriverType.STATEMENT, queryContext);
        setField(engine, "proxySQLExecutor", proxySQLExecutor);
        try (MockedStatic<DatabaseTypedSPILoader> spiLoader = mockStatic(DatabaseTypedSPILoader.class)) {
            spiLoader.when(() -> DatabaseTypedSPILoader.getService(eq(QueryHeaderBuilder.class), any(DatabaseType.class))).thenReturn(new QueryHeaderBuilderFixture());
            assertThat(engine.execute(), isA(QueryResponseHeader.class));
        }
    }
    
    @Test
    void assertExecuteWithFederationEnabledAndNoRefresh() throws SQLException {
        SQLStatementContext sqlStatementContext = createSQLStatementContext(new SQLStatement(databaseType));
        QueryContext queryContext = createQueryContext(sqlStatementContext, mockDatabase());
        ProxySQLExecutor proxySQLExecutor = mock(ProxySQLExecutor.class, RETURNS_DEEP_STUBS);
        SQLFederationEngine sqlFederationEngine = mock(SQLFederationEngine.class);
        when(proxySQLExecutor.getSqlFederationEngine()).thenReturn(sqlFederationEngine);
        when(sqlFederationEngine.decide(any(QueryContext.class), any(RuleMetaData.class))).thenReturn(false);
        when(sqlFederationEngine.isSQLFederationEnabled()).thenReturn(true);
        DatabaseProxyConnector engine = createDatabaseProxyConnector(JDBCDriverType.STATEMENT, queryContext);
        setField(engine, "proxySQLExecutor", proxySQLExecutor);
        ExecutionContext executionContext = mock(ExecutionContext.class);
        when(executionContext.getExecutionUnits()).thenReturn(Collections.emptyList());
        try (
                MockedConstruction<FederationMetaDataRefreshEngine> mockedRefreshEngine = mockConstruction(FederationMetaDataRefreshEngine.class,
                        (mock, context) -> when(mock.isNeedRefresh()).thenReturn(false));
                MockedConstruction<KernelProcessor> mockedKernelProcessor = mockConstruction(KernelProcessor.class,
                        (mock, context) -> when(mock.generateExecutionContext(any(QueryContext.class), any(RuleMetaData.class), any(ConfigurationProperties.class))).thenReturn(executionContext))) {
            assertThat(engine.execute(), isA(UpdateResponseHeader.class));
            assertThat(mockedRefreshEngine.constructed().size(), is(1));
            assertThat(mockedKernelProcessor.constructed().size(), is(1));
        }
    }
    
    @Test
    void assertExecuteWithPreparedStatementCacheReuseInHeldConnection() throws SQLException, BackendConnectionException {
        Connection connection = mock(Connection.class);
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        PreparedStatementExecutionTestContext testContext = createPreparedStatementExecutionTestContext("UPDATE tbl SET col = ?", connection);
        when(connection.prepareStatement("UPDATE tbl SET col = ?")).thenReturn(preparedStatement);
        ExecutionContext firstExecutionContext = createPreparedStatementExecutionContext(testContext, 1);
        ExecutionContext secondExecutionContext = createPreparedStatementExecutionContext(testContext, 2);
        try (
                MockedConstruction<DatabaseTypeRegistry> mockedDatabaseTypeRegistry = mockPreparedStatementDatabaseTypeRegistry();
                MockedConstruction<KernelProcessor> mockedKernelProcessor = mockKernelProcessor(firstExecutionContext, secondExecutionContext);
                MockedStatic<ShardingSphereServiceLoader> ignoredServiceLoader = mockNoAdvancedProxySQLExecutorsWithRealMethods()) {
            DatabaseProxyConnector engine = createPreparedStatementConnector(testContext);
            testContext.connectionSession.beginPreparedStatementCache(createPreparedStatementCacheKey(1));
            assertThat(engine.execute(), isA(UpdateResponseHeader.class));
            assertThat(testContext.connectionSession.getPreparedStatementCacheContext().size(), is(1));
            testContext.connectionSession.finishPreparedStatementCache();
            testContext.connectionSession.getDatabaseConnectionManager().closeExecutionResources();
            assertThat(testContext.connectionSession.getPreparedStatementCacheContext().size(), is(1));
            testContext.connectionSession.beginPreparedStatementCache(createPreparedStatementCacheKey(1));
            assertThat(engine.execute(), isA(UpdateResponseHeader.class));
            testContext.connectionSession.finishPreparedStatementCache();
            assertTrue(mockedDatabaseTypeRegistry.constructed().size() >= 3);
            assertThat(mockedKernelProcessor.constructed().size(), is(2));
        }
        verify(connection).prepareStatement("UPDATE tbl SET col = ?");
        verify(preparedStatement, times(2)).clearParameters();
        verify(preparedStatement).setObject(1, 1);
        verify(preparedStatement).setObject(1, 2);
        verify(connection, never()).close();
        assertThat(testContext.connectionSession.getPreparedStatementCacheContext().size(), is(1));
    }
    
    @Test
    void assertExecuteWithNewPreparedStatementAfterCacheInvalidation() throws SQLException {
        Connection connection = mock(Connection.class);
        PreparedStatement firstPreparedStatement = mock(PreparedStatement.class);
        PreparedStatement secondPreparedStatement = mock(PreparedStatement.class);
        PreparedStatementExecutionTestContext testContext = createPreparedStatementExecutionTestContext("UPDATE tbl SET col = ?", connection);
        when(connection.prepareStatement("UPDATE tbl SET col = ?")).thenReturn(firstPreparedStatement, secondPreparedStatement);
        ExecutionContext firstExecutionContext = createPreparedStatementExecutionContext(testContext, 1);
        ExecutionContext secondExecutionContext = createPreparedStatementExecutionContext(testContext, 2);
        try (
                MockedConstruction<DatabaseTypeRegistry> mockedDatabaseTypeRegistry = mockPreparedStatementDatabaseTypeRegistry();
                MockedConstruction<KernelProcessor> mockedKernelProcessor = mockKernelProcessor(firstExecutionContext, secondExecutionContext);
                MockedStatic<ShardingSphereServiceLoader> ignoredServiceLoader = mockNoAdvancedProxySQLExecutorsWithRealMethods()) {
            DatabaseProxyConnector engine = createPreparedStatementConnector(testContext);
            testContext.connectionSession.beginPreparedStatementCache(createPreparedStatementCacheKey(1));
            assertThat(engine.execute(), isA(UpdateResponseHeader.class));
            testContext.connectionSession.finishPreparedStatementCache();
            testContext.connectionSession.invalidatePreparedStatementCache(createPreparedStatementCacheKey(1));
            assertThat(testContext.connectionSession.getPreparedStatementCacheContext().size(), is(0));
            testContext.connectionSession.beginPreparedStatementCache(createPreparedStatementCacheKey(1));
            assertThat(engine.execute(), isA(UpdateResponseHeader.class));
            testContext.connectionSession.finishPreparedStatementCache();
            assertTrue(mockedDatabaseTypeRegistry.constructed().size() >= 3);
            assertThat(mockedKernelProcessor.constructed().size(), is(2));
        }
        verify(connection, times(2)).prepareStatement("UPDATE tbl SET col = ?");
        verify(firstPreparedStatement).clearParameters();
        verify(secondPreparedStatement).clearParameters();
        verify(firstPreparedStatement).setObject(1, 1);
        verify(secondPreparedStatement).setObject(1, 2);
        verify(firstPreparedStatement).close();
        assertThat(testContext.connectionSession.getPreparedStatementCacheContext().size(), is(1));
    }
    
    @Test
    void assertExecuteWithNewPreparedStatementAfterClosingConnections() throws SQLException {
        Connection firstConnection = mock(Connection.class);
        Connection secondConnection = mock(Connection.class);
        PreparedStatement firstPreparedStatement = mock(PreparedStatement.class);
        PreparedStatement secondPreparedStatement = mock(PreparedStatement.class);
        PreparedStatementExecutionTestContext testContext = createPreparedStatementExecutionTestContext("UPDATE tbl SET col = ?", firstConnection, secondConnection);
        when(firstConnection.prepareStatement("UPDATE tbl SET col = ?")).thenReturn(firstPreparedStatement);
        when(secondConnection.prepareStatement("UPDATE tbl SET col = ?")).thenReturn(secondPreparedStatement);
        ExecutionContext firstExecutionContext = createPreparedStatementExecutionContext(testContext, 1);
        ExecutionContext secondExecutionContext = createPreparedStatementExecutionContext(testContext, 2);
        try (
                MockedConstruction<DatabaseTypeRegistry> mockedDatabaseTypeRegistry = mockPreparedStatementDatabaseTypeRegistry();
                MockedConstruction<KernelProcessor> mockedKernelProcessor = mockKernelProcessor(firstExecutionContext, secondExecutionContext);
                MockedStatic<ShardingSphereServiceLoader> ignoredServiceLoader = mockNoAdvancedProxySQLExecutorsWithRealMethods()) {
            DatabaseProxyConnector engine = createPreparedStatementConnector(testContext);
            testContext.connectionSession.beginPreparedStatementCache(createPreparedStatementCacheKey(1));
            assertThat(engine.execute(), isA(UpdateResponseHeader.class));
            testContext.connectionSession.finishPreparedStatementCache();
            assertThat(testContext.connectionSession.getDatabaseConnectionManager().closeConnections(false), is(Collections.emptyList()));
            assertThat(testContext.connectionSession.getPreparedStatementCacheContext().size(), is(0));
            testContext.connectionSession.beginPreparedStatementCache(createPreparedStatementCacheKey(1));
            assertThat(engine.execute(), isA(UpdateResponseHeader.class));
            testContext.connectionSession.finishPreparedStatementCache();
            assertTrue(mockedDatabaseTypeRegistry.constructed().size() >= 3);
            assertThat(mockedKernelProcessor.constructed().size(), is(2));
        }
        verify(firstConnection).prepareStatement("UPDATE tbl SET col = ?");
        verify(secondConnection).prepareStatement("UPDATE tbl SET col = ?");
        verify(firstPreparedStatement).clearParameters();
        verify(secondPreparedStatement).clearParameters();
        verify(firstPreparedStatement).setObject(1, 1);
        verify(secondPreparedStatement).setObject(1, 2);
        verify(firstPreparedStatement).close();
        verify(firstConnection).close();
        assertThat(testContext.connectionSession.getPreparedStatementCacheContext().size(), is(1));
    }
    
    @Test
    void assertCloseExecutionResourcesAfterCachedStatementInvalidation() throws SQLException, BackendConnectionException {
        Connection connection = mock(Connection.class);
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        AtomicBoolean closed = new AtomicBoolean(false);
        doAnswer(invocation -> {
            if (closed.get()) {
                throw new SQLException("cancel after close");
            }
            return null;
        }).when(preparedStatement).cancel();
        doAnswer(invocation -> {
            closed.set(true);
            return null;
        }).when(preparedStatement).close();
        when(preparedStatement.isClosed()).thenAnswer(invocation -> closed.get());
        PreparedStatementExecutionTestContext testContext = createPreparedStatementExecutionTestContext("UPDATE tbl SET col = ?", connection);
        when(connection.prepareStatement("UPDATE tbl SET col = ?")).thenReturn(preparedStatement);
        ExecutionContext executionContext = createPreparedStatementExecutionContext(testContext, 1);
        try (
                MockedConstruction<DatabaseTypeRegistry> mockedDatabaseTypeRegistry = mockPreparedStatementDatabaseTypeRegistry();
                MockedConstruction<KernelProcessor> mockedKernelProcessor = mockKernelProcessor(executionContext);
                MockedStatic<ShardingSphereServiceLoader> ignoredServiceLoader = mockNoAdvancedProxySQLExecutorsWithRealMethods()) {
            DatabaseProxyConnector engine = createPreparedStatementConnector(testContext);
            testContext.connectionSession.getDatabaseConnectionManager().add(engine);
            testContext.connectionSession.getDatabaseConnectionManager().markResourceInUse(engine);
            testContext.connectionSession.beginPreparedStatementCache(createPreparedStatementCacheKey(1));
            assertThat(engine.execute(), isA(UpdateResponseHeader.class));
            testContext.connectionSession.finishPreparedStatementCache();
            testContext.connectionSession.getDatabaseConnectionManager().removeResource(engine);
            engine.close();
            testContext.connectionSession.invalidatePreparedStatementCache(createPreparedStatementCacheKey(1));
            testContext.connectionSession.getDatabaseConnectionManager().closeExecutionResources();
            assertTrue(mockedDatabaseTypeRegistry.constructed().size() >= 2);
            assertThat(mockedKernelProcessor.constructed().size(), is(1));
        }
        verify(connection).prepareStatement("UPDATE tbl SET col = ?");
        verify(preparedStatement, never()).cancel();
        verify(preparedStatement).close();
        assertTrue(closed.get());
    }
    
    @Test
    void assertExecuteWithQueryResult() throws SQLException {
        SQLStatementContext sqlStatementContext = createSQLStatementContext(new SQLStatement(databaseType));
        QueryContext queryContext = createQueryContext(sqlStatementContext, mockDatabase());
        ProxySQLExecutor proxySQLExecutor = mock(ProxySQLExecutor.class, RETURNS_DEEP_STUBS);
        DatabaseProxyConnector engine = createDatabaseProxyConnector(JDBCDriverType.STATEMENT, queryContext);
        setField(engine, "proxySQLExecutor", proxySQLExecutor);
        ExecutionContext executionContext = mock(ExecutionContext.class, RETURNS_DEEP_STUBS);
        when(executionContext.getExecutionUnits()).thenReturn(Collections.singletonList(mock(ExecutionUnit.class)));
        when(executionContext.getSqlStatementContext()).thenReturn(sqlStatementContext);
        QueryResult queryResult = mock(QueryResult.class);
        QueryResultMetaData queryResultMetaData = mock(QueryResultMetaData.class);
        ResultSetMetaData resultSetMetaData = mock(ResultSetMetaData.class);
        when(queryResultMetaData.getColumnCount()).thenReturn(1);
        when(queryResultMetaData.getResultSetMetaData()).thenReturn(resultSetMetaData);
        when(resultSetMetaData.getColumnName(1)).thenReturn("order_id");
        when(resultSetMetaData.getColumnLabel(1)).thenReturn("order_id");
        when(queryResult.getMetaData()).thenReturn(queryResultMetaData);
        when(proxySQLExecutor.execute(executionContext)).thenReturn(Collections.singletonList(queryResult));
        MergedResult mergedResult = mock(MergedResult.class);
        when(mergedResult.next()).thenReturn(true);
        when(mergedResult.getValue(1, Object.class)).thenReturn(1);
        DialectDatabaseMetaData dialectDatabaseMetaData = mock(DialectDatabaseMetaData.class);
        DialectTransactionOption dialectTransactionOption = mock(DialectTransactionOption.class);
        when(dialectDatabaseMetaData.getTransactionOption()).thenReturn(dialectTransactionOption);
        try (
                MockedConstruction<KernelProcessor> mockedKernelProcessor = mockConstruction(KernelProcessor.class,
                        (mock, context) -> when(mock.generateExecutionContext(any(QueryContext.class), any(RuleMetaData.class), any(ConfigurationProperties.class))).thenReturn(executionContext));
                MockedConstruction<DatabaseTypeRegistry> mockedDatabaseTypeRegistry = mockConstruction(DatabaseTypeRegistry.class,
                        (mock, context) -> when(mock.getDialectDatabaseMetaData()).thenReturn(dialectDatabaseMetaData));
                MockedConstruction<MergeEngine> mockedMergeEngine =
                        mockConstruction(MergeEngine.class, (mock, context) -> when(mock.merge(anyList(), any(QueryContext.class))).thenReturn(mergedResult));
                MockedStatic<DatabaseTypedSPILoader> spiLoader = mockStatic(DatabaseTypedSPILoader.class);
                MockedStatic<ShardingSphereServiceLoader> serviceLoader = mockStatic(ShardingSphereServiceLoader.class)) {
            spiLoader.when(() -> DatabaseTypedSPILoader.getService(eq(QueryHeaderBuilder.class), any(DatabaseType.class))).thenReturn(new QueryHeaderBuilderFixture());
            serviceLoader.when(() -> ShardingSphereServiceLoader.getServiceInstances(AdvancedProxySQLExecutor.class)).thenReturn(Collections.emptyList());
            assertThat(engine.execute(), isA(QueryResponseHeader.class));
            assertThat(mockedKernelProcessor.constructed().size(), is(1));
            assertThat(mockedDatabaseTypeRegistry.constructed().size(), is(1));
            assertThat(mockedMergeEngine.constructed().size(), is(1));
            assertTrue(engine.next());
            assertNotNull(engine.getRowData());
        }
    }
    
    @Test
    void assertExecuteWithDerivedQueryResult() throws SQLException {
        SelectStatementContext sqlStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        SelectStatement selectStatement = SelectStatement.builder().databaseType(databaseType).build();
        when(sqlStatementContext.getSqlStatement()).thenReturn(selectStatement);
        when(sqlStatementContext.getTablesContext().getDatabaseNames()).thenReturn(Collections.emptyList());
        when(sqlStatementContext.getTablesContext().getSchemaNames()).thenReturn(Collections.emptyList());
        when(sqlStatementContext.containsDerivedProjections()).thenReturn(true);
        Projection projection = mock(Projection.class);
        when(projection.getColumnName()).thenReturn("order_id");
        when(projection.getColumnLabel()).thenReturn("order_id");
        ProjectionsContext projectionsContext = new ProjectionsContext(0, 0, false, Collections.singleton(projection));
        when(sqlStatementContext.getProjectionsContext()).thenReturn(projectionsContext);
        QueryContext queryContext = createQueryContext(sqlStatementContext, mockDatabase());
        ProxySQLExecutor proxySQLExecutor = mock(ProxySQLExecutor.class, RETURNS_DEEP_STUBS);
        SQLFederationEngine sqlFederationEngine = mock(SQLFederationEngine.class);
        when(proxySQLExecutor.getSqlFederationEngine()).thenReturn(sqlFederationEngine);
        when(sqlFederationEngine.decide(any(QueryContext.class), any(RuleMetaData.class))).thenReturn(false);
        when(sqlFederationEngine.isSQLFederationEnabled()).thenReturn(false);
        DatabaseProxyConnector engine = createDatabaseProxyConnector(JDBCDriverType.STATEMENT, queryContext);
        setField(engine, "proxySQLExecutor", proxySQLExecutor);
        ExecutionContext executionContext = mock(ExecutionContext.class, RETURNS_DEEP_STUBS);
        when(executionContext.getExecutionUnits()).thenReturn(Collections.singletonList(mock(ExecutionUnit.class)));
        when(executionContext.getSqlStatementContext()).thenReturn(sqlStatementContext);
        QueryResult queryResult = mock(QueryResult.class);
        when(queryResult.getMetaData()).thenReturn(mock(QueryResultMetaData.class));
        when(proxySQLExecutor.execute(executionContext)).thenReturn(Collections.singletonList(queryResult));
        MergedResult mergedResult = mock(MergedResult.class);
        try (
                MockedConstruction<KernelProcessor> mockedKernelProcessor = mockConstruction(KernelProcessor.class,
                        (mock, context) -> when(mock.generateExecutionContext(any(QueryContext.class), any(RuleMetaData.class), any(ConfigurationProperties.class))).thenReturn(executionContext));
                MockedConstruction<DatabaseTypeRegistry> mockedDatabaseTypeRegistry = mockConstruction(DatabaseTypeRegistry.class,
                        (mock, context) -> when(mock.getDialectDatabaseMetaData()).thenReturn(mock(DialectDatabaseMetaData.class)));
                MockedConstruction<MergeEngine> mockedMergeEngine =
                        mockConstruction(MergeEngine.class, (mock, context) -> when(mock.merge(anyList(), any(QueryContext.class))).thenReturn(mergedResult));
                MockedStatic<DatabaseTypedSPILoader> spiLoader = mockStatic(DatabaseTypedSPILoader.class);
                MockedStatic<ShardingSphereServiceLoader> serviceLoader = mockStatic(ShardingSphereServiceLoader.class)) {
            spiLoader.when(() -> DatabaseTypedSPILoader.getService(eq(QueryHeaderBuilder.class), any(DatabaseType.class))).thenReturn(new QueryHeaderBuilderFixture());
            serviceLoader.when(() -> ShardingSphereServiceLoader.getServiceInstances(AdvancedProxySQLExecutor.class)).thenReturn(Collections.emptyList());
            assertThat(engine.execute(), isA(QueryResponseHeader.class));
            assertThat(mockedKernelProcessor.constructed().size(), is(1));
            assertThat(mockedDatabaseTypeRegistry.constructed().size(), is(1));
            assertThat(mockedMergeEngine.constructed().size(), is(1));
        }
    }
    
    @Test
    void assertExecuteWithImplicitCommit() throws SQLException {
        CloseStatement closeStatement = new CloseStatement(databaseType, null, false);
        SQLStatementContext sqlStatementContext = createSQLStatementContext(closeStatement);
        QueryContext queryContext = createQueryContext(sqlStatementContext, mockDatabase());
        ProxySQLExecutor proxySQLExecutor = mock(ProxySQLExecutor.class, RETURNS_DEEP_STUBS);
        DatabaseProxyConnector engine = createDatabaseProxyConnector(JDBCDriverType.STATEMENT, queryContext);
        setField(engine, "proxySQLExecutor", proxySQLExecutor);
        ExecutionContext executionContext = mock(ExecutionContext.class, RETURNS_DEEP_STUBS);
        when(executionContext.getExecutionUnits()).thenReturn(Collections.singletonList(mock(ExecutionUnit.class)));
        when(executionContext.getSqlStatementContext()).thenReturn(sqlStatementContext);
        when(proxySQLExecutor.execute(executionContext)).thenReturn(Collections.singletonList(new UpdateResult(1, 0L)));
        DialectDatabaseMetaData dialectDatabaseMetaData = mock(DialectDatabaseMetaData.class);
        DialectTransactionOption dialectTransactionOption = mock(DialectTransactionOption.class);
        when(dialectDatabaseMetaData.getTransactionOption()).thenReturn(dialectTransactionOption);
        when(dialectTransactionOption.isDDLNeedImplicitCommit()).thenReturn(true);
        try (
                MockedConstruction<KernelProcessor> mockedKernelProcessor = mockConstruction(KernelProcessor.class,
                        (mock, context) -> when(mock.generateExecutionContext(any(QueryContext.class), any(RuleMetaData.class), any(ConfigurationProperties.class))).thenReturn(executionContext));
                MockedConstruction<DatabaseTypeRegistry> mockedDatabaseTypeRegistry = mockConstruction(DatabaseTypeRegistry.class,
                        (mock, context) -> when(mock.getDialectDatabaseMetaData()).thenReturn(dialectDatabaseMetaData));
                MockedConstruction<ProxyBackendTransactionManager> mockedTransactionManager = mockConstruction(ProxyBackendTransactionManager.class);
                MockedStatic<ShardingSphereServiceLoader> serviceLoader = mockStatic(ShardingSphereServiceLoader.class)) {
            serviceLoader.when(() -> ShardingSphereServiceLoader.getServiceInstances(AdvancedProxySQLExecutor.class)).thenReturn(Collections.emptyList());
            assertThat(engine.execute(), isA(UpdateResponseHeader.class));
            assertThat(mockedKernelProcessor.constructed().size(), is(1));
            assertThat(mockedDatabaseTypeRegistry.constructed().size(), is(1));
            verify(mockedTransactionManager.constructed().iterator().next()).commit();
        }
    }
    
    @Test
    void assertExecuteWithUpdateResultAndNoAccumulate() throws SQLException {
        SQLStatementContext sqlStatementContext = createSQLStatementContext(InsertStatement.builder().databaseType(databaseType).build());
        when(sqlStatementContext.getTablesContext().getTableNames()).thenReturn(Collections.singleton("t_order"));
        ShardingSphereDatabase database = mockDatabase();
        when(database.getRuleMetaData().getAttributes(DataNodeRuleAttribute.class)).thenReturn(Collections.singleton(mock(DataNodeRuleAttribute.class)));
        DatabaseProxyConnector engine = createDatabaseProxyConnector(JDBCDriverType.STATEMENT, createQueryContext(sqlStatementContext, database));
        ProxySQLExecutor proxySQLExecutor = mock(ProxySQLExecutor.class, RETURNS_DEEP_STUBS);
        setField(engine, "proxySQLExecutor", proxySQLExecutor);
        ExecutionContext executionContext = mock(ExecutionContext.class, RETURNS_DEEP_STUBS);
        when(executionContext.getExecutionUnits()).thenReturn(Collections.singletonList(mock(ExecutionUnit.class)));
        when(executionContext.getSqlStatementContext()).thenReturn(sqlStatementContext);
        when(proxySQLExecutor.execute(executionContext)).thenReturn(Arrays.asList(new UpdateResult(1, 4L), new UpdateResult(2, 5L)));
        try (
                MockedConstruction<KernelProcessor> mockedKernelProcessor = mockConstruction(KernelProcessor.class,
                        (mock, context) -> when(mock.generateExecutionContext(any(QueryContext.class), any(RuleMetaData.class), any(ConfigurationProperties.class))).thenReturn(executionContext));
                MockedConstruction<DatabaseTypeRegistry> mockedDatabaseTypeRegistry = mockConstruction(DatabaseTypeRegistry.class,
                        (mock, context) -> when(mock.getDialectDatabaseMetaData()).thenReturn(mock(DialectDatabaseMetaData.class)));
                MockedStatic<ShardingSphereServiceLoader> serviceLoader = mockStatic(ShardingSphereServiceLoader.class)) {
            serviceLoader.when(() -> ShardingSphereServiceLoader.getServiceInstances(AdvancedProxySQLExecutor.class)).thenReturn(Collections.emptyList());
            UpdateResponseHeader actual = (UpdateResponseHeader) engine.execute();
            assertThat(actual.getUpdateCount(), is(1L));
            assertThat(mockedKernelProcessor.constructed().size(), is(1));
            assertThat(mockedDatabaseTypeRegistry.constructed().size(), is(1));
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("implicitCommitSkippedCases")
    void assertExecuteWithoutImplicitCommit(final String scenario, final SQLStatement sqlStatement, final String transactionType, final boolean inTransaction,
                                            final int executionUnitCount) throws SQLException {
        assertThat(executeWithImplicitCommitCondition(sqlStatement, transactionType, inTransaction, executionUnitCount), isA(UpdateResponseHeader.class));
    }
    
    private static Collection<Arguments> implicitCommitSkippedCases() {
        DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
        return Arrays.asList(
                Arguments.of("singleExecutionUnit", InsertStatement.builder().databaseType(databaseType).build(), "XA", false, 1),
                Arguments.of("localTransaction", InsertStatement.builder().databaseType(databaseType).build(), "LOCAL", false, 2),
                Arguments.of("alreadyInTransaction", InsertStatement.builder().databaseType(databaseType).build(), "XA", true, 2),
                Arguments.of("selectStatement", SelectStatement.builder().databaseType(databaseType).build(), "XA", false, 2),
                Arguments.of("notDMLStatement", new SQLStatement(databaseType), "XA", false, 2));
    }
    
    @Test
    void assertExecuteWithoutImplicitCommitForDDLWhenOptionDisabled() throws SQLException {
        CloseStatement closeStatement = new CloseStatement(databaseType, null, false);
        SQLStatementContext sqlStatementContext = createSQLStatementContext(closeStatement);
        ProxySQLExecutor proxySQLExecutor = mock(ProxySQLExecutor.class, RETURNS_DEEP_STUBS);
        DatabaseProxyConnector engine = createDatabaseProxyConnector(JDBCDriverType.STATEMENT, createQueryContext(sqlStatementContext, mockDatabase()));
        setField(engine, "proxySQLExecutor", proxySQLExecutor);
        ExecutionContext executionContext = mock(ExecutionContext.class);
        when(executionContext.getExecutionUnits()).thenReturn(Collections.singletonList(mock(ExecutionUnit.class)));
        when(executionContext.getSqlStatementContext()).thenReturn(sqlStatementContext);
        when(proxySQLExecutor.execute(executionContext)).thenReturn(Collections.singletonList(new UpdateResult(1, 0L)));
        try (
                MockedConstruction<KernelProcessor> mockedKernelProcessor = mockConstruction(KernelProcessor.class,
                        (mock, context) -> when(mock.generateExecutionContext(any(QueryContext.class), any(RuleMetaData.class), any(ConfigurationProperties.class))).thenReturn(executionContext));
                MockedConstruction<DatabaseTypeRegistry> mockedDatabaseTypeRegistry = mockConstruction(DatabaseTypeRegistry.class,
                        (mock, context) -> when(mock.getDialectDatabaseMetaData()).thenReturn(mock(DialectDatabaseMetaData.class, RETURNS_DEEP_STUBS)));
                MockedConstruction<ProxyBackendTransactionManager> mockedTransactionManager = mockConstruction(ProxyBackendTransactionManager.class);
                MockedStatic<ShardingSphereServiceLoader> serviceLoader = mockStatic(ShardingSphereServiceLoader.class)) {
            serviceLoader.when(() -> ShardingSphereServiceLoader.getServiceInstances(AdvancedProxySQLExecutor.class)).thenReturn(Collections.emptyList());
            assertThat(engine.execute(), isA(UpdateResponseHeader.class));
            assertThat(mockedKernelProcessor.constructed().size(), is(1));
            assertThat(mockedDatabaseTypeRegistry.constructed().size(), is(1));
            assertTrue(mockedTransactionManager.constructed().isEmpty());
        }
    }
    
    private DatabaseProxyConnector createDatabaseProxyConnector(final JDBCDriverType driverType, final QueryContext queryContext) {
        DatabaseProxyConnector result = new StandardDatabaseProxyConnector(driverType, queryContext, databaseConnectionManager);
        databaseConnectionManager.add(result);
        return result;
    }
    
    private QueryContext createQueryContext(final SQLStatementContext sqlStatementContext, final ShardingSphereDatabase database) {
        ConnectionContext connectionContext = mock(ConnectionContext.class);
        when(connectionContext.getCurrentDatabaseName()).thenReturn(Optional.of("foo_db"));
        ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class);
        when(metaData.containsDatabase("foo_db")).thenReturn(true);
        when(metaData.getDatabase("foo_db")).thenReturn(database);
        return new QueryContext(sqlStatementContext, "schemaName", Collections.emptyList(), new HintValueContext(), connectionContext, metaData);
    }
    
    private ShardingSphereDatabase createDatabaseMetaData() {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        ShardingSphereColumn column = new ShardingSphereColumn("order_id", Types.INTEGER, true, false, false, true, false, false);
        when(result.getSchema("foo_db").getTable("t_logic_order")).thenReturn(new ShardingSphereTable(
                "t_logic_order", Collections.singleton(column), Collections.singleton(new ShardingSphereIndex("order_id", Collections.emptyList(), false)), Collections.emptyList()));
        when(result.getRuleMetaData().getRules()).thenReturn(Collections.singleton(mock(ShardingRule.class)));
        return result;
    }
    
    private ShardingSphereResultSetMetaData createResultSetMetaData() throws SQLException {
        ResultSetMetaData resultSetMetaData = mock(ResultSetMetaData.class);
        when(resultSetMetaData.getColumnLabel(1)).thenReturn("order_id");
        when(resultSetMetaData.getColumnName(1)).thenReturn("order_id");
        return new ShardingSphereResultSetMetaData(resultSetMetaData, mock(ShardingSphereDatabase.class), null);
    }
    
    private ResponseHeader executeWithImplicitCommitCondition(final SQLStatement sqlStatement, final String transactionType, final boolean inTransaction,
                                                              final int executionUnitCount) throws SQLException {
        when(databaseConnectionManager.getConnectionSession().isAutoCommit()).thenReturn(true);
        when(databaseConnectionManager.getConnectionSession().getConnectionContext().getTransactionContext().getTransactionType()).thenReturn(Optional.of(transactionType));
        when(databaseConnectionManager.getConnectionSession().getTransactionStatus().isInTransaction()).thenReturn(inTransaction);
        SQLStatementContext sqlStatementContext = createSQLStatementContext(sqlStatement);
        QueryContext queryContext = createQueryContext(sqlStatementContext, mockDatabase());
        DatabaseProxyConnector engine = createDatabaseProxyConnector(JDBCDriverType.STATEMENT, queryContext);
        ProxySQLExecutor proxySQLExecutor = mock(ProxySQLExecutor.class, RETURNS_DEEP_STUBS);
        setField(engine, "proxySQLExecutor", proxySQLExecutor);
        ExecutionContext executionContext = mock(ExecutionContext.class);
        when(executionContext.getSqlStatementContext()).thenReturn(sqlStatementContext);
        when(executionContext.getExecutionUnits()).thenReturn(IntStream.range(0, executionUnitCount).mapToObj(index -> mock(ExecutionUnit.class)).collect(Collectors.toList()));
        when(proxySQLExecutor.execute(executionContext)).thenReturn(Collections.singletonList(new UpdateResult(1, 0L)));
        try (
                MockedConstruction<KernelProcessor> mockedKernelProcessor = mockConstruction(KernelProcessor.class,
                        (mock, context) -> when(mock.generateExecutionContext(any(QueryContext.class), any(RuleMetaData.class), any(ConfigurationProperties.class))).thenReturn(executionContext));
                MockedConstruction<DatabaseTypeRegistry> mockedDatabaseTypeRegistry = mockConstruction(DatabaseTypeRegistry.class,
                        (mock, context) -> when(mock.getDialectDatabaseMetaData()).thenReturn(mock(DialectDatabaseMetaData.class)));
                MockedConstruction<ProxyBackendTransactionManager> mockedTransactionManager = mockConstruction(ProxyBackendTransactionManager.class);
                MockedStatic<ShardingSphereServiceLoader> serviceLoader = mockStatic(ShardingSphereServiceLoader.class)) {
            serviceLoader.when(() -> ShardingSphereServiceLoader.getServiceInstances(AdvancedProxySQLExecutor.class)).thenReturn(Collections.emptyList());
            ResponseHeader result = engine.execute();
            assertTrue(mockedTransactionManager.constructed().isEmpty());
            return result;
        }
    }
    
    @Test
    void assertAddStatementCorrectly() throws SQLException {
        DatabaseProxyConnector engine = createDatabaseProxyConnector(JDBCDriverType.STATEMENT, createQueryContext(createSQLStatementContext(new SQLStatement(databaseType)), mockDatabase()));
        engine.add(statement);
        engine.close();
        verify(statement).cancel();
        verify(statement).close();
    }
    
    @Test
    void assertAddResultSetCorrectly() throws SQLException {
        DatabaseProxyConnector engine = createDatabaseProxyConnector(JDBCDriverType.STATEMENT, createQueryContext(createSQLStatementContext(new SQLStatement(databaseType)), mockDatabase()));
        engine.add(resultSet);
        engine.close();
        verify(resultSet).close();
    }
    
    @Test
    void assertCloseCorrectly() throws SQLException {
        DatabaseProxyConnector engine = createDatabaseProxyConnector(JDBCDriverType.STATEMENT, createQueryContext(createSQLStatementContext(new SQLStatement(databaseType)), mockDatabase()));
        engine.add(resultSet);
        engine.add(statement);
        engine.close();
        engine.close();
        verify(resultSet).close();
        verify(statement).cancel();
        verify(statement).close();
    }
    
    @Test
    void assertCloseSkipCachedPreparedStatement() throws SQLException {
        DatabaseProxyConnector engine = createDatabaseProxyConnector(JDBCDriverType.STATEMENT, createQueryContext(createSQLStatementContext(new SQLStatement(databaseType)), mockDatabase()));
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        engine.add(preparedStatement);
        when(databaseConnectionManager.getConnectionSession().getPreparedStatementCacheContext().contains(preparedStatement)).thenReturn(true, false);
        engine.close();
        engine.close();
        verify(preparedStatement, never()).cancel();
        verify(preparedStatement, never()).close();
    }
    
    @Test
    void assertCloseResultSetsWithExceptionThrown() throws SQLException {
        DatabaseProxyConnector engine = createDatabaseProxyConnector(JDBCDriverType.STATEMENT, createQueryContext(createSQLStatementContext(new SQLStatement(databaseType)), mockDatabase()));
        SQLException sqlExceptionByResultSet = new SQLException("ResultSet");
        doThrow(sqlExceptionByResultSet).when(resultSet).close();
        engine.add(resultSet);
        SQLException sqlExceptionByStatement = new SQLException("Statement");
        doThrow(sqlExceptionByStatement).when(statement).close();
        engine.add(statement);
        SQLException actual = null;
        try {
            engine.close();
        } catch (final SQLException ex) {
            actual = ex;
        }
        verify(resultSet).close();
        verify(statement).close();
        assertNotNull(actual);
        assertThat(actual.getNextException(), is(sqlExceptionByResultSet));
        assertThat(actual.getNextException().getNextException(), is(sqlExceptionByStatement));
    }
    
    @Test
    void assertNext() throws SQLException {
        assertFalse(createDatabaseProxyConnector(JDBCDriverType.STATEMENT, createQueryContext(createSQLStatementContext(new SQLStatement(databaseType)), mockDatabase())).next());
    }
    
    @Test
    void assertNextWithMergedResult() throws SQLException {
        DatabaseProxyConnector engine = createDatabaseProxyConnector(JDBCDriverType.STATEMENT, createQueryContext(createSQLStatementContext(new SQLStatement(databaseType)), mockDatabase()));
        MergedResult mergedResult = mock(MergedResult.class);
        when(mergedResult.next()).thenReturn(true);
        setField(engine, "mergedResult", mergedResult);
        assertTrue(engine.next());
        verify(mergedResult).next();
    }
    
    @Test
    void assertNextWithMergedResultNoMoreData() throws SQLException {
        DatabaseProxyConnector engine = createDatabaseProxyConnector(JDBCDriverType.STATEMENT, createQueryContext(createSQLStatementContext(new SQLStatement(databaseType)), mockDatabase()));
        MergedResult mergedResult = mock(MergedResult.class);
        when(mergedResult.next()).thenReturn(false);
        setField(engine, "mergedResult", mergedResult);
        assertFalse(engine.next());
        verify(mergedResult).next();
    }
    
    @Test
    void assertCloseWithSQLExceptionThrownBySQLFederationEngine() throws SQLException {
        DatabaseProxyConnector engine = createDatabaseProxyConnector(JDBCDriverType.STATEMENT, createQueryContext(createSQLStatementContext(new SQLStatement(databaseType)), mockDatabase()));
        ProxySQLExecutor proxySQLExecutor = mock(ProxySQLExecutor.class);
        SQLFederationEngine sqlFederationEngine = mock(SQLFederationEngine.class);
        when(proxySQLExecutor.getSqlFederationEngine()).thenReturn(sqlFederationEngine);
        SQLException expected = new SQLException("SQLFederationEngine");
        doThrow(expected).when(sqlFederationEngine).close();
        setField(engine, "proxySQLExecutor", proxySQLExecutor);
        SQLException actual = null;
        try {
            engine.close();
        } catch (final SQLException ex) {
            actual = ex;
        }
        assertNotNull(actual);
        assertThat(actual.getNextException(), is(expected));
        verify(sqlFederationEngine).close();
    }
    
    @Test
    void assertCloseWithNullSQLFederationEngine() throws SQLException {
        DatabaseProxyConnector engine = createDatabaseProxyConnector(JDBCDriverType.STATEMENT, createQueryContext(createSQLStatementContext(new SQLStatement(databaseType)), mockDatabase()));
        ProxySQLExecutor proxySQLExecutor = mock(ProxySQLExecutor.class);
        when(proxySQLExecutor.getSqlFederationEngine()).thenReturn(null);
        setField(engine, "proxySQLExecutor", proxySQLExecutor);
        engine.close();
        verify(proxySQLExecutor).getSqlFederationEngine();
    }
    
    private SQLStatementContext createSQLStatementContext(final SQLStatement sqlStatement) {
        SQLStatementContext result = mock(SQLStatementContext.class, RETURNS_DEEP_STUBS);
        when(result.getSqlStatement()).thenReturn(sqlStatement);
        when(result.getTablesContext().getDatabaseNames()).thenReturn(Collections.emptyList());
        when(result.getTablesContext().getSchemaNames()).thenReturn(Collections.emptyList());
        when(result.getTablesContext().getTableNames()).thenReturn(Collections.emptyList());
        return result;
    }
    
    private ConnectionSession createPreparedStatementConnectionSession(final ContextManager contextManager) {
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        ConnectionSession result = new ConnectionSession(databaseType, null);
        result.setGrantee(new Grantee("foo_user"));
        result.setCurrentDatabaseName("foo_db");
        result.getConnectionContext().getTransactionContext().beginTransaction("XA", null);
        result.getTransactionStatus().setInTransaction(true);
        return result;
    }
    
    private PreparedStatementExecutionTestContext createPreparedStatementExecutionTestContext(final String sql, final Connection... connections) throws SQLException {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class, RETURNS_DEEP_STUBS);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        StorageUnit storageUnit = mock(StorageUnit.class);
        JDBCBackendDataSource backendDataSource = mock(JDBCBackendDataSource.class);
        when(metaData.containsDatabase("foo_db")).thenReturn(true);
        when(metaData.getDatabase("foo_db")).thenReturn(database);
        when(metaData.getAllDatabases()).thenReturn(Collections.singleton(database));
        when(metaData.getProps().<Integer>getValue(ConfigurationPropertyKey.KERNEL_EXECUTOR_SIZE)).thenReturn(1);
        when(metaData.getProps().<Integer>getValue(ConfigurationPropertyKey.MAX_CONNECTIONS_SIZE_PER_QUERY)).thenReturn(1);
        when(metaData.getGlobalRuleMetaData()).thenReturn(new RuleMetaData(Collections.singletonList(sqlFederationRule)));
        when(database.getName()).thenReturn("foo_db");
        when(database.containsDataSource()).thenReturn(true);
        when(database.isComplete()).thenReturn(true);
        when(database.getProtocolType()).thenReturn(databaseType);
        when(database.getRuleMetaData().getRules()).thenReturn(Collections.emptyList());
        when(database.getRuleMetaData().getAttributes(DataNodeRuleAttribute.class)).thenReturn(Collections.emptyList());
        when(database.getResourceMetaData().getStorageUnits()).thenReturn(Collections.singletonMap("ds", storageUnit));
        when(storageUnit.getStorageType()).thenReturn(databaseType);
        when(contextManager.getMetaDataContexts().getMetaData()).thenReturn(metaData);
        when(contextManager.getDatabase("foo_db")).thenReturn(database);
        when(ProxyContext.getInstance().getBackendDataSource()).thenReturn(backendDataSource);
        if (1 == connections.length) {
            when(backendDataSource.getConnections(eq("foo_db"), eq("ds"), eq(1), any())).thenReturn(Collections.singletonList(connections[0]));
        } else {
            when(backendDataSource.getConnections(eq("foo_db"), eq("ds"), eq(1), any())).thenReturn(Collections.singletonList(connections[0]), Collections.singletonList(connections[1]));
        }
        ConnectionSession connectionSession = createPreparedStatementConnectionSession(contextManager);
        SQLStatementContext sqlStatementContext = createSQLStatementContext(new SQLStatement(databaseType));
        return new PreparedStatementExecutionTestContext(connectionSession, metaData, sqlStatementContext,
                new QueryContext(sqlStatementContext, sql, Collections.singletonList(1), new HintValueContext(), connectionSession.getConnectionContext(), metaData), sql);
    }
    
    private ExecutionContext createPreparedStatementExecutionContext(final PreparedStatementExecutionTestContext testContext, final int parameter) {
        return new ExecutionContext(new QueryContext(testContext.sqlStatementContext, testContext.sql, Collections.singletonList(parameter),
                new HintValueContext(), testContext.connectionSession.getConnectionContext(), testContext.metaData),
                Collections.singletonList(new ExecutionUnit("ds", new SQLUnit(testContext.sql, Collections.singletonList(parameter)))), mock());
    }
    
    private MockedConstruction<DatabaseTypeRegistry> mockPreparedStatementDatabaseTypeRegistry() {
        return mockConstruction(DatabaseTypeRegistry.class, (mock, context) -> {
            when(mock.getDefaultSchemaName("foo_db")).thenReturn("foo_db");
            when(mock.getDialectDatabaseMetaData()).thenReturn(mock(DialectDatabaseMetaData.class, RETURNS_DEEP_STUBS));
        });
    }
    
    private MockedConstruction<KernelProcessor> mockKernelProcessor(final ExecutionContext executionContext) {
        return mockConstruction(KernelProcessor.class,
                (mock, context) -> when(mock.generateExecutionContext(any(QueryContext.class), any(RuleMetaData.class), any(ConfigurationProperties.class))).thenReturn(executionContext));
    }
    
    private MockedConstruction<KernelProcessor> mockKernelProcessor(final ExecutionContext firstExecutionContext, final ExecutionContext secondExecutionContext) {
        return mockConstruction(KernelProcessor.class,
                (mock, context) -> when(mock.generateExecutionContext(any(QueryContext.class), any(RuleMetaData.class), any(ConfigurationProperties.class)))
                        .thenReturn(1 == context.getCount() ? firstExecutionContext : secondExecutionContext));
    }
    
    private MockedStatic<ShardingSphereServiceLoader> mockNoAdvancedProxySQLExecutorsWithRealMethods() {
        MockedStatic<ShardingSphereServiceLoader> result = mockStatic(ShardingSphereServiceLoader.class, CALLS_REAL_METHODS);
        result.when(() -> ShardingSphereServiceLoader.getServiceInstances(AdvancedProxySQLExecutor.class)).thenReturn(Collections.emptyList());
        return result;
    }
    
    private PreparedStatementCacheKey createPreparedStatementCacheKey(final int statementId) {
        return new PreparedStatementCacheKey("prepared-statement:" + statementId);
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private DatabaseProxyConnector createPreparedStatementConnector(final PreparedStatementExecutionTestContext testContext) throws SQLException {
        DatabaseProxyConnector result = new StandardDatabaseProxyConnector(
                JDBCDriverType.PREPARED_STATEMENT, testContext.queryContext, testContext.connectionSession.getDatabaseConnectionManager());
        ProxySQLExecutor proxySQLExecutor = (ProxySQLExecutor) Plugins.getMemberAccessor().get(StandardDatabaseProxyConnector.class.getDeclaredField("proxySQLExecutor"), result);
        ProxyJDBCExecutor proxyJDBCExecutor = mock(ProxyJDBCExecutor.class);
        List<ExecuteResult> executeResults = Collections.singletonList(new UpdateResult(1, 0L));
        when(proxyJDBCExecutor.execute(any(), any(), eq(false), anyBoolean())).thenReturn(executeResults);
        Plugins.getMemberAccessor().set(ProxySQLExecutor.class.getDeclaredField("regularExecutor"), proxySQLExecutor, proxyJDBCExecutor);
        return result;
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private void setField(final DatabaseProxyConnector target, final String fieldName, final Object value) {
        Plugins.getMemberAccessor().set(StandardDatabaseProxyConnector.class.getDeclaredField(fieldName), target, value);
    }
    
    @RequiredArgsConstructor
    private static final class PreparedStatementExecutionTestContext {
        
        private final ConnectionSession connectionSession;
        
        private final ShardingSphereMetaData metaData;
        
        private final SQLStatementContext sqlStatementContext;
        
        private final QueryContext queryContext;
        
        private final String sql;
    }
}
