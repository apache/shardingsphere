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
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResultMetaData;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.jdbc.JDBCDriverType;
import org.apache.shardingsphere.infra.hint.HintValueContext;
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
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.parser.config.SQLParserRuleConfiguration;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.proxy.backend.connector.jdbc.fixture.QueryHeaderBuilderFixture;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryHeaderBuilder;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryHeaderBuilderEngine;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.engine.api.CacheOption;
import org.apache.shardingsphere.sqlfederation.rule.SQLFederationRule;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@StaticMockSettings({ProxyContext.class, SystemSchemaUtils.class})
class StandardDatabaseProxyConnectorTest {
    
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
        when(result.getProtocolType()).thenReturn(TypedSPILoader.getService(DatabaseType.class, "H2"));
        return result;
    }
    
    @Test
    void assertBinaryProtocolQueryHeader() throws SQLException, NoSuchFieldException, IllegalAccessException {
        SQLStatementContext sqlStatementContext = mock(SQLStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getTablesContext().getDatabaseNames()).thenReturn(Collections.emptyList());
        when(sqlStatementContext.getSqlStatement().getDatabaseType()).thenReturn(TypedSPILoader.getService(DatabaseType.class, "FIXTURE"));
        DatabaseProxyConnector engine = createDatabaseProxyConnector(JDBCDriverType.PREPARED_STATEMENT, createQueryContext(sqlStatementContext));
        Field queryHeadersField = StandardDatabaseProxyConnector.class.getDeclaredField("queryHeaders");
        ShardingSphereDatabase database = createDatabaseMetaData();
        try (MockedStatic<DatabaseTypedSPILoader> spiLoader = mockStatic(DatabaseTypedSPILoader.class)) {
            spiLoader.when(() -> DatabaseTypedSPILoader.getService(QueryHeaderBuilder.class, TypedSPILoader.getService(DatabaseType.class, "MySQL"))).thenReturn(new QueryHeaderBuilderFixture());
            Plugins.getMemberAccessor().set(queryHeadersField, engine,
                    Collections.singletonList(new QueryHeaderBuilderEngine(TypedSPILoader.getService(DatabaseType.class, "MySQL")).build(createQueryResultMetaData(), database, 1)));
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
    
    private DatabaseProxyConnector createDatabaseProxyConnector(final JDBCDriverType driverType, final QueryContext queryContext) {
        DatabaseProxyConnector result = new StandardDatabaseProxyConnector(driverType, queryContext, databaseConnectionManager);
        databaseConnectionManager.add(result);
        return result;
    }
    
    private QueryContext createQueryContext(final SQLStatementContext sqlStatementContext) {
        ConnectionContext connectionContext = mock(ConnectionContext.class);
        when(connectionContext.getCurrentDatabaseName()).thenReturn(Optional.of("foo_db"));
        ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class);
        when(metaData.containsDatabase("foo_db")).thenReturn(true);
        ShardingSphereDatabase database = mockDatabase();
        when(metaData.getDatabase("foo_db")).thenReturn(database);
        return new QueryContext(sqlStatementContext, "schemaName", Collections.emptyList(), new HintValueContext(), connectionContext, metaData);
    }
    
    private ShardingSphereDatabase createDatabaseMetaData() {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        ShardingSphereColumn column = new ShardingSphereColumn("order_id", Types.INTEGER, true, false, "int", false, true, false, false);
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
    
    @Test
    void assertAddStatementCorrectly() {
        SQLStatementContext sqlStatementContext = mock(SQLStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getTablesContext().getDatabaseNames()).thenReturn(Collections.emptyList());
        when(sqlStatementContext.getSqlStatement().getDatabaseType()).thenReturn(TypedSPILoader.getService(DatabaseType.class, "FIXTURE"));
        DatabaseProxyConnector engine = createDatabaseProxyConnector(JDBCDriverType.STATEMENT, createQueryContext(sqlStatementContext));
        engine.add(statement);
        Collection<?> actual = getField(engine, "cachedStatements");
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next(), is(statement));
    }
    
    @Test
    void assertAddResultSetCorrectly() {
        SQLStatementContext sqlStatementContext = mock(SQLStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getTablesContext().getDatabaseNames()).thenReturn(Collections.emptyList());
        when(sqlStatementContext.getSqlStatement().getDatabaseType()).thenReturn(TypedSPILoader.getService(DatabaseType.class, "FIXTURE"));
        DatabaseProxyConnector engine = createDatabaseProxyConnector(JDBCDriverType.STATEMENT, createQueryContext(sqlStatementContext));
        engine.add(resultSet);
        Collection<?> actual = getField(engine, "cachedResultSets");
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next(), is(resultSet));
    }
    
    @Test
    void assertCloseCorrectly() throws SQLException {
        SQLStatementContext sqlStatementContext = mock(SQLStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getTablesContext().getDatabaseNames()).thenReturn(Collections.emptyList());
        when(sqlStatementContext.getSqlStatement().getDatabaseType()).thenReturn(TypedSPILoader.getService(DatabaseType.class, "FIXTURE"));
        DatabaseProxyConnector engine = createDatabaseProxyConnector(JDBCDriverType.STATEMENT, createQueryContext(sqlStatementContext));
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
        when(sqlStatementContext.getSqlStatement().getDatabaseType()).thenReturn(TypedSPILoader.getService(DatabaseType.class, "FIXTURE"));
        DatabaseProxyConnector engine = createDatabaseProxyConnector(JDBCDriverType.STATEMENT, createQueryContext(sqlStatementContext));
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
    
    @SuppressWarnings("unchecked")
    @SneakyThrows(ReflectiveOperationException.class)
    private <T> T getField(final DatabaseProxyConnector target, final String fieldName) {
        return (T) Plugins.getMemberAccessor().get(StandardDatabaseProxyConnector.class.getDeclaredField(fieldName), target);
    }
}
