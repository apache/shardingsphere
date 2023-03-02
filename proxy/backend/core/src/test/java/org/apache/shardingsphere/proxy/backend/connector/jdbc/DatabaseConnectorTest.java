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

package org.apache.shardingsphere.proxy.backend.connector.jdbc;

import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.binder.QueryContext;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.DefaultDatabase;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.H2DatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResultMetaData;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.DriverExecutionPrepareEngine;
import org.apache.shardingsphere.infra.merge.result.impl.memory.MemoryMergedResult;
import org.apache.shardingsphere.infra.merge.result.impl.memory.MemoryQueryResultRow;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereIndex;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.database.schema.util.SystemSchemaUtil;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.proxy.backend.connector.BackendConnection;
import org.apache.shardingsphere.proxy.backend.connector.DatabaseConnector;
import org.apache.shardingsphere.proxy.backend.connector.DatabaseConnectorFactory;
import org.apache.shardingsphere.proxy.backend.connector.jdbc.executor.callback.ProxyJDBCExecutorCallback;
import org.apache.shardingsphere.proxy.backend.connector.jdbc.fixture.QueryHeaderBuilderFixture;
import org.apache.shardingsphere.proxy.backend.connector.jdbc.statement.JDBCBackendStatement;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.data.QueryResponseRow;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryHeaderBuilder;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryHeaderBuilderEngine;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sqlfederation.api.config.SQLFederationRuleConfiguration;
import org.apache.shardingsphere.sqlfederation.rule.SQLFederationRule;
import org.apache.shardingsphere.sqlfederation.spi.SQLFederationExecutor;
import org.apache.shardingsphere.sqlfederation.spi.SQLFederationExecutorContext;
import org.apache.shardingsphere.test.mock.AutoMockExtension;
import org.apache.shardingsphere.test.mock.StaticMockSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.internal.configuration.plugins.Plugins;
import org.mockito.plugins.MemberAccessor;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings({ProxyContext.class, SystemSchemaUtil.class})
public final class DatabaseConnectorTest {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private BackendConnection backendConnection;
    
    @Mock
    private Statement statement;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ResultSet resultSet;
    
    @BeforeEach
    public void setUp() {
        when(backendConnection.getConnectionSession().getDatabaseName()).thenReturn("foo_db");
        ContextManager contextManager = mockContextManager();
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        ShardingSphereDatabase database = contextManager.getMetaDataContexts().getMetaData().getDatabase("foo_db");
        when(ProxyContext.getInstance().getDatabase("foo_db")).thenReturn(database);
    }
    
    private ContextManager mockContextManager() {
        ShardingSphereRuleMetaData globalRuleMetaData = new ShardingSphereRuleMetaData(Collections.singleton(new SQLFederationRule(new SQLFederationRuleConfiguration("ORIGINAL"))));
        MetaDataContexts metaDataContexts = new MetaDataContexts(mock(MetaDataPersistService.class),
                new ShardingSphereMetaData(mockDatabases(), globalRuleMetaData, new ConfigurationProperties(new Properties())));
        ContextManager result = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(result.getMetaDataContexts()).thenReturn(metaDataContexts);
        return result;
    }
    
    private Map<String, ShardingSphereDatabase> mockDatabases() {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.containsDataSource()).thenReturn(true);
        when(database.isComplete()).thenReturn(true);
        when(database.getResourceMetaData().getStorageTypes()).thenReturn(Collections.singletonMap("ds_0", new H2DatabaseType()));
        when(database.getProtocolType()).thenReturn(new H2DatabaseType());
        when(database.getRuleMetaData().getRules()).thenReturn(Collections.emptyList());
        return Collections.singletonMap("foo_db", database);
    }
    
    @Test
    public void assertExecuteFederationAndClose() throws SQLException {
        SQLStatementContext<?> sqlStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getDatabaseType()).thenReturn(new MySQLDatabaseType());
        DatabaseConnector engine =
                DatabaseConnectorFactory.getInstance().newInstance(new QueryContext(sqlStatementContext, "schemaName", Collections.emptyList()), backendConnection, true);
        when(backendConnection.getConnectionSession().getStatementManager()).thenReturn(new JDBCBackendStatement());
        SQLFederationExecutor federationExecutor = mock(SQLFederationExecutor.class);
        when(SystemSchemaUtil.containsSystemSchema(any(DatabaseType.class), any(), any(ShardingSphereDatabase.class))).thenReturn(true);
        try (MockedStatic<TypedSPILoader> typedSPILoader = mockStatic(TypedSPILoader.class)) {
            when(federationExecutor.executeQuery(any(DriverExecutionPrepareEngine.class), any(ProxyJDBCExecutorCallback.class), any(SQLFederationExecutorContext.class))).thenReturn(resultSet);
            when(resultSet.getMetaData().getColumnCount()).thenReturn(1);
            when(resultSet.next()).thenReturn(true, false);
            when(resultSet.getObject(1)).thenReturn(Integer.MAX_VALUE);
            typedSPILoader.when(() -> TypedSPILoader.getService(SQLFederationExecutor.class, "NONE")).thenReturn(federationExecutor);
            typedSPILoader.when(() -> TypedSPILoader.getService(QueryHeaderBuilder.class, "H2")).thenReturn(new QueryHeaderBuilderFixture());
            engine.execute();
        }
        assertTrue(engine.next());
        QueryResponseRow actualRow = engine.getRowData();
        assertThat(actualRow.getCells().get(0).getJdbcType(), is(Types.INTEGER));
        assertThat(actualRow.getCells().get(0).getData(), is(Integer.MAX_VALUE));
        assertFalse(engine.next());
        engine.close();
        verify(federationExecutor).close();
    }
    
    @Test
    public void assertBinaryProtocolQueryHeader() throws SQLException, NoSuchFieldException, IllegalAccessException {
        SQLStatementContext<?> sqlStatementContext = mock(SQLStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getTablesContext().getSchemaNames()).thenReturn(Collections.emptyList());
        DatabaseConnector engine = DatabaseConnectorFactory.getInstance().newInstance(new QueryContext(sqlStatementContext, "schemaName", Collections.emptyList()), backendConnection, true);
        assertNotNull(engine);
        assertThat(engine, instanceOf(DatabaseConnector.class));
        Field queryHeadersField = DatabaseConnector.class.getDeclaredField("queryHeaders");
        ShardingSphereDatabase database = createDatabaseMetaData();
        MemberAccessor accessor = Plugins.getMemberAccessor();
        try (MockedStatic<TypedSPILoader> typedSPILoader = mockStatic(TypedSPILoader.class)) {
            typedSPILoader.when(() -> TypedSPILoader.getService(QueryHeaderBuilder.class, "MySQL")).thenReturn(new QueryHeaderBuilderFixture());
            accessor.set(queryHeadersField, engine, Collections.singletonList(new QueryHeaderBuilderEngine(new MySQLDatabaseType()).build(createQueryResultMetaData(), database, 1)));
            Field mergedResultField = DatabaseConnector.class.getDeclaredField("mergedResult");
            accessor.set(mergedResultField, engine, new MemoryMergedResult<ShardingSphereRule>(null, null, null, Collections.emptyList()) {
                
                @Override
                protected List<MemoryQueryResultRow> init(final ShardingSphereRule rule, final ShardingSphereSchema schema,
                                                          final SQLStatementContext<?> sqlStatementContext, final List<QueryResult> queryResults) {
                    return Collections.singletonList(mock(MemoryQueryResultRow.class));
                }
            });
            Exception ex = null;
            try {
                engine.getRowData();
            } catch (final SQLException | IndexOutOfBoundsException e) {
                ex = e;
            } finally {
                assertFalse(ex instanceof IndexOutOfBoundsException);
            }
        }
    }
    
    private ShardingSphereDatabase createDatabaseMetaData() {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        ShardingSphereColumn column = new ShardingSphereColumn("order_id", Types.INTEGER, true, false, false, true, false);
        when(result.getSchema(DefaultDatabase.LOGIC_NAME).getTable("t_logic_order")).thenReturn(
                new ShardingSphereTable("t_logic_order", Collections.singleton(column), Collections.singleton(new ShardingSphereIndex("order_id")), Collections.emptyList()));
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
    public void assertAddStatementCorrectly() {
        SQLStatementContext<?> sqlStatementContext = mock(SQLStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getTablesContext().getSchemaNames()).thenReturn(Collections.emptyList());
        DatabaseConnector engine = DatabaseConnectorFactory.getInstance().newInstance(new QueryContext(sqlStatementContext, "schemaName", Collections.emptyList()), backendConnection, false);
        engine.add(statement);
        Collection<?> actual = getField(engine, "cachedStatements");
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next(), is(statement));
    }
    
    @Test
    public void assertAddResultSetCorrectly() {
        SQLStatementContext<?> sqlStatementContext = mock(SQLStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getTablesContext().getSchemaNames()).thenReturn(Collections.emptyList());
        DatabaseConnector engine = DatabaseConnectorFactory.getInstance().newInstance(new QueryContext(sqlStatementContext, "schemaName", Collections.emptyList()), backendConnection, false);
        engine.add(resultSet);
        Collection<?> actual = getField(engine, "cachedResultSets");
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next(), is(resultSet));
    }
    
    @Test
    public void assertCloseCorrectly() throws SQLException {
        SQLStatementContext<?> sqlStatementContext = mock(SQLStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getTablesContext().getSchemaNames()).thenReturn(Collections.emptyList());
        DatabaseConnector engine = DatabaseConnectorFactory.getInstance().newInstance(new QueryContext(sqlStatementContext, "schemaName", Collections.emptyList()), backendConnection, false);
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
    public void assertCloseResultSetsWithExceptionThrown() throws SQLException {
        SQLStatementContext<?> sqlStatementContext = mock(SQLStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getTablesContext().getSchemaNames()).thenReturn(Collections.emptyList());
        DatabaseConnector engine = DatabaseConnectorFactory.getInstance().newInstance(new QueryContext(sqlStatementContext, "schemaName", Collections.emptyList()), backendConnection, false);
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
    private <T> T getField(final DatabaseConnector target, final String fieldName) {
        return (T) Plugins.getMemberAccessor().get(DatabaseConnector.class.getDeclaredField(fieldName), target);
    }
}
