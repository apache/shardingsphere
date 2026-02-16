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

import lombok.SneakyThrows;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.DialectDatabaseMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.transaction.DialectTransactionOption;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.binder.context.segment.insert.keygen.GeneratedKeyContext;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.Projection;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.ProjectionsContext;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.ddl.CursorHeldSQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.ddl.CursorStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.connection.kernel.KernelProcessor;
import org.apache.shardingsphere.infra.exception.kernel.metadata.resource.storageunit.EmptyStorageUnitException;
import org.apache.shardingsphere.infra.exception.kernel.metadata.rule.EmptyRuleException;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionContext;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;
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
import org.apache.shardingsphere.proxy.backend.connector.jdbc.statement.JDBCBackendStatement;
import org.apache.shardingsphere.proxy.backend.connector.jdbc.transaction.ProxyBackendTransactionManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryHeaderBuilder;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryHeaderBuilderEngine;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.internal.configuration.plugins.Plugins;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
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
            Plugins.getMemberAccessor().set(queryHeadersField, engine, Collections.singletonList(new QueryHeaderBuilderEngine(databaseType).build(createQueryResultMetaData(), database, 1)));
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
        SQLStatementContext sqlStatementContext = createSQLStatementContext(new InsertStatement(databaseType));
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
        SQLStatementContext sqlStatementContext = createSQLStatementContext(new InsertStatement(databaseType));
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
        InsertStatement insertStatement = new InsertStatement(databaseType);
        insertStatement.buildAttributes();
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
        closeStatement.buildAttributes();
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
        closeStatement.buildAttributes();
        CursorHeldSQLStatementContext sqlStatementContext = new CursorHeldSQLStatementContext(closeStatement);
        when(databaseConnectionManager.getConnectionSession().getConnectionContext().getCursorContext()).thenReturn(new CursorConnectionContext());
        assertNotNull(assertThrows(IllegalArgumentException.class, () -> createDatabaseProxyConnector(JDBCDriverType.STATEMENT, createQueryContext(sqlStatementContext, mockDatabase()))));
    }
    
    @Test
    void assertConstructWithCursorHeldSQLStatementContext() {
        CursorNameSegment cursorNameSegment = new CursorNameSegment(0, 0, new IdentifierValue("foo_cursor"));
        CloseStatement closeStatement = new CloseStatement(databaseType, cursorNameSegment, false);
        closeStatement.buildAttributes();
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
        cursorStatement.buildAttributes();
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
        SelectStatement selectStatement = new SelectStatement(databaseType);
        selectStatement.buildAttributes();
        when(sqlStatementContext.getSqlStatement()).thenReturn(selectStatement);
        when(sqlStatementContext.getTablesContext().getDatabaseNames()).thenReturn(Collections.emptyList());
        when(sqlStatementContext.getTablesContext().getSchemaNames()).thenReturn(Collections.emptyList());
        assertNotNull(createDatabaseProxyConnector(JDBCDriverType.STATEMENT, createQueryContext(sqlStatementContext, mockDatabase())));
    }
    
    @Test
    void assertExecuteWithFederation() throws SQLException {
        SQLStatementContext sqlStatementContext = createSQLStatementContext(new SelectStatement(databaseType));
        QueryContext queryContext = createQueryContext(sqlStatementContext, mockDatabase());
        ProxySQLExecutor proxySQLExecutor = mock(ProxySQLExecutor.class, RETURNS_DEEP_STUBS);
        SQLFederationEngine sqlFederationEngine = mock(SQLFederationEngine.class);
        when(proxySQLExecutor.getSqlFederationEngine()).thenReturn(sqlFederationEngine);
        when(sqlFederationEngine.decide(any(QueryContext.class), any(RuleMetaData.class))).thenReturn(true);
        when(databaseConnectionManager.getConnectionSession().getStatementManager()).thenReturn(mock(JDBCBackendStatement.class));
        when(resultSet.getMetaData().getColumnCount()).thenReturn(1);
        when(resultSet.getMetaData().getColumnName(1)).thenReturn("order_id");
        when(resultSet.getMetaData().getColumnLabel(1)).thenReturn("order_id");
        DatabaseProxyConnector engine = createDatabaseProxyConnector(JDBCDriverType.STATEMENT, queryContext);
        setField(engine, "proxySQLExecutor", proxySQLExecutor);
        try (MockedStatic<DatabaseTypedSPILoader> spiLoader = mockStatic(DatabaseTypedSPILoader.class)) {
            when(sqlFederationEngine.executeQuery(any(), any(), any())).thenAnswer(invocation -> {
                setField(engine, "database", null);
                return resultSet;
            });
            spiLoader.when(() -> DatabaseTypedSPILoader.getService(eq(QueryHeaderBuilder.class), any(DatabaseType.class))).thenReturn(new QueryHeaderBuilderFixture());
            spiLoader.when(() -> DatabaseTypedSPILoader.getService(QueryHeaderBuilder.class, null)).thenReturn(new QueryHeaderBuilderFixture());
            assertThat(engine.execute(), isA(QueryResponseHeader.class));
        }
    }
    
    @Test
    void assertExecuteWithFederationAndNotNullDatabase() throws SQLException {
        SQLStatementContext sqlStatementContext = createSQLStatementContext(new SelectStatement(databaseType));
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
        when(queryResultMetaData.getColumnCount()).thenReturn(1);
        when(queryResultMetaData.getColumnName(1)).thenReturn("order_id");
        when(queryResultMetaData.getColumnLabel(1)).thenReturn("order_id");
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
        SelectStatement selectStatement = new SelectStatement(databaseType);
        selectStatement.buildAttributes();
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
        closeStatement.buildAttributes();
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
        SQLStatementContext sqlStatementContext = createSQLStatementContext(new InsertStatement(databaseType));
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
    
    @Test
    void assertExecuteWithoutImplicitCommitWhenSingleExecutionUnit() throws SQLException {
        InsertStatement insertStatement = new InsertStatement(databaseType);
        assertThat(executeWithImplicitCommitCondition(insertStatement, "XA", false, 1), isA(UpdateResponseHeader.class));
    }
    
    @Test
    void assertExecuteWithoutImplicitCommitWhenLocalTransaction() throws SQLException {
        InsertStatement insertStatement = new InsertStatement(databaseType);
        assertThat(executeWithImplicitCommitCondition(insertStatement, "LOCAL", false, 2), isA(UpdateResponseHeader.class));
    }
    
    @Test
    void assertExecuteWithoutImplicitCommitWhenAlreadyInTransaction() throws SQLException {
        InsertStatement insertStatement = new InsertStatement(databaseType);
        assertThat(executeWithImplicitCommitCondition(insertStatement, "XA", true, 2), isA(UpdateResponseHeader.class));
    }
    
    @Test
    void assertExecuteWithoutImplicitCommitWhenSelectStatement() throws SQLException {
        SelectStatement selectStatement = new SelectStatement(databaseType);
        assertThat(executeWithImplicitCommitCondition(selectStatement, "XA", false, 2), isA(UpdateResponseHeader.class));
    }
    
    @Test
    void assertExecuteWithoutImplicitCommitWhenSQLStatementIsNotDML() throws SQLException {
        SQLStatement sqlStatement = new SQLStatement(databaseType);
        assertThat(executeWithImplicitCommitCondition(sqlStatement, "XA", false, 2), isA(UpdateResponseHeader.class));
    }
    
    @Test
    void assertExecuteWithoutImplicitCommitForDDLWhenOptionDisabled() throws SQLException {
        CloseStatement closeStatement = new CloseStatement(databaseType, null, false);
        closeStatement.buildAttributes();
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
    
    private QueryResultMetaData createQueryResultMetaData() throws SQLException {
        QueryResultMetaData result = mock(QueryResultMetaData.class);
        when(result.getColumnLabel(1)).thenReturn("order_id");
        when(result.getColumnName(1)).thenReturn("order_id");
        return result;
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
                MockedStatic<ShardingSphereServiceLoader> serviceLoader = mockStatic(ShardingSphereServiceLoader.class)) {
            serviceLoader.when(() -> ShardingSphereServiceLoader.getServiceInstances(AdvancedProxySQLExecutor.class)).thenReturn(Collections.emptyList());
            ResponseHeader result = engine.execute();
            assertThat(mockedKernelProcessor.constructed().size(), is(1));
            assertThat(mockedDatabaseTypeRegistry.constructed().size(), is(1));
            return result;
        }
    }
    
    @Test
    void assertAddStatementCorrectly() {
        SQLStatementContext sqlStatementContext = mock(SQLStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getTablesContext().getDatabaseNames()).thenReturn(Collections.emptyList());
        when(sqlStatementContext.getSqlStatement().getDatabaseType()).thenReturn(databaseType);
        DatabaseProxyConnector engine = createDatabaseProxyConnector(JDBCDriverType.STATEMENT, createQueryContext(sqlStatementContext, mockDatabase()));
        engine.add(statement);
        Collection<?> actual = getField(engine, "cachedStatements");
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next(), is(statement));
    }
    
    @Test
    void assertAddResultSetCorrectly() {
        SQLStatementContext sqlStatementContext = mock(SQLStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getTablesContext().getDatabaseNames()).thenReturn(Collections.emptyList());
        when(sqlStatementContext.getSqlStatement().getDatabaseType()).thenReturn(databaseType);
        DatabaseProxyConnector engine = createDatabaseProxyConnector(JDBCDriverType.STATEMENT, createQueryContext(sqlStatementContext, mockDatabase()));
        engine.add(resultSet);
        Collection<?> actual = getField(engine, "cachedResultSets");
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next(), is(resultSet));
    }
    
    @Test
    void assertCloseCorrectly() throws SQLException {
        SQLStatementContext sqlStatementContext = mock(SQLStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getTablesContext().getDatabaseNames()).thenReturn(Collections.emptyList());
        when(sqlStatementContext.getSqlStatement().getDatabaseType()).thenReturn(databaseType);
        DatabaseProxyConnector engine = createDatabaseProxyConnector(JDBCDriverType.STATEMENT, createQueryContext(sqlStatementContext, mockDatabase()));
        Collection<ResultSet> cachedResultSets = getField(engine, "cachedResultSets");
        cachedResultSets.add(resultSet);
        Collection<Statement> cachedStatements = getField(engine, "cachedStatements");
        cachedStatements.add(statement);
        engine.close();
        verify(resultSet).close();
        verify(statement).cancel();
        verify(statement).close();
        assertTrue(cachedResultSets.isEmpty());
        assertTrue(cachedStatements.isEmpty());
    }
    
    @Test
    void assertCloseResultSetsWithExceptionThrown() throws SQLException {
        SQLStatementContext sqlStatementContext = mock(SQLStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getTablesContext().getDatabaseNames()).thenReturn(Collections.emptyList());
        when(sqlStatementContext.getSqlStatement().getDatabaseType()).thenReturn(databaseType);
        DatabaseProxyConnector engine = createDatabaseProxyConnector(JDBCDriverType.STATEMENT, createQueryContext(sqlStatementContext, mockDatabase()));
        Collection<ResultSet> cachedResultSets = getField(engine, "cachedResultSets");
        SQLException sqlExceptionByResultSet = new SQLException("ResultSet");
        doThrow(sqlExceptionByResultSet).when(resultSet).close();
        cachedResultSets.add(resultSet);
        Collection<Statement> cachedStatements = getField(engine, "cachedStatements");
        SQLException sqlExceptionByStatement = new SQLException("Statement");
        doThrow(sqlExceptionByStatement).when(statement).close();
        cachedStatements.add(statement);
        SQLException actual = null;
        try {
            engine.close();
        } catch (final SQLException ex) {
            actual = ex;
        }
        verify(resultSet).close();
        verify(statement).close();
        assertTrue(cachedResultSets.isEmpty());
        assertTrue(cachedStatements.isEmpty());
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
        Collection<?> cachedStatements = getField(engine, "cachedStatements");
        Collection<?> cachedResultSets = getField(engine, "cachedResultSets");
        assertTrue(cachedStatements.isEmpty());
        assertTrue(cachedResultSets.isEmpty());
    }
    
    private SQLStatementContext createSQLStatementContext(final SQLStatement sqlStatement) {
        sqlStatement.buildAttributes();
        SQLStatementContext result = mock(SQLStatementContext.class, RETURNS_DEEP_STUBS);
        when(result.getSqlStatement()).thenReturn(sqlStatement);
        when(result.getTablesContext().getDatabaseNames()).thenReturn(Collections.emptyList());
        when(result.getTablesContext().getSchemaNames()).thenReturn(Collections.emptyList());
        when(result.getTablesContext().getTableNames()).thenReturn(Collections.emptyList());
        return result;
    }
    
    @SuppressWarnings("unchecked")
    @SneakyThrows(ReflectiveOperationException.class)
    private <T> T getField(final DatabaseProxyConnector target, final String fieldName) {
        return (T) Plugins.getMemberAccessor().get(StandardDatabaseProxyConnector.class.getDeclaredField(fieldName), target);
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private void setField(final DatabaseProxyConnector target, final String fieldName, final Object value) {
        Plugins.getMemberAccessor().set(StandardDatabaseProxyConnector.class.getDeclaredField(fieldName), target, value);
    }
}
