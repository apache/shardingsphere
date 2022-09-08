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

package org.apache.shardingsphere.proxy.backend.communication.jdbc;

import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.binder.QueryContext;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.context.kernel.KernelProcessor;
import org.apache.shardingsphere.infra.database.DefaultDatabase;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.H2DatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutor;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResultMetaData;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.DriverExecutionPrepareEngine;
import org.apache.shardingsphere.infra.merge.result.impl.memory.MemoryMergedResult;
import org.apache.shardingsphere.infra.merge.result.impl.memory.MemoryQueryResultRow;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereIndex;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.database.schema.util.SystemSchemaUtil;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.util.eventbus.EventBusContext;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.proxy.backend.communication.DatabaseCommunicationEngine;
import org.apache.shardingsphere.proxy.backend.communication.DatabaseCommunicationEngineFactory;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.JDBCBackendConnection;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.executor.callback.ProxyJDBCExecutorCallback;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.statement.JDBCBackendStatement;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.data.QueryResponseRow;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryHeaderBuilderEngine;
import org.apache.shardingsphere.proxy.backend.util.ProxyContextRestorer;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sqlfederation.factory.SQLFederationExecutorFactory;
import org.apache.shardingsphere.sqlfederation.spi.SQLFederationExecutorContext;
import org.apache.shardingsphere.sqlfederation.spi.SQLFederationExecutor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.internal.configuration.plugins.Plugins;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.plugins.MemberAccessor;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class JDBCDatabaseCommunicationEngineTest extends ProxyContextRestorer {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private JDBCBackendConnection backendConnection;
    
    @Mock
    private Statement statement;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ResultSet resultSet;
    
    @Before
    public void setUp() {
        when(backendConnection.getConnectionSession().getDatabaseName()).thenReturn("db");
        MetaDataContexts metaDataContexts = new MetaDataContexts(mock(MetaDataPersistService.class),
                new ShardingSphereMetaData(mockDatabases(), mock(ShardingSphereRuleMetaData.class), new ConfigurationProperties(new Properties())));
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(contextManager.getMetaDataContexts()).thenReturn(metaDataContexts);
        ProxyContext.init(contextManager);
    }
    
    private Map<String, ShardingSphereDatabase> mockDatabases() {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.containsDataSource()).thenReturn(true);
        when(database.isComplete()).thenReturn(true);
        when(database.getResource().getDatabaseType()).thenReturn(new H2DatabaseType());
        when(database.getProtocolType()).thenReturn(new H2DatabaseType());
        when(database.getRuleMetaData().getRules()).thenReturn(Collections.emptyList());
        Map<String, ShardingSphereDatabase> result = new LinkedHashMap<>(1, 1);
        result.put("db", database);
        return result;
    }
    
    @Test
    public void assertExecuteFederationAndClose() throws SQLException, NoSuchFieldException, IllegalAccessException {
        SQLStatementContext<?> sqlStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getDatabaseType()).thenReturn(new MySQLDatabaseType());
        JDBCDatabaseCommunicationEngine engine =
                DatabaseCommunicationEngineFactory.getInstance().newDatabaseCommunicationEngine(new QueryContext(sqlStatementContext, "schemaName", Collections.emptyList()), backendConnection, true);
        Field kernelProcessorField = DatabaseCommunicationEngine.class.getDeclaredField("kernelProcessor");
        kernelProcessorField.setAccessible(true);
        KernelProcessor kernelProcessor = mock(KernelProcessor.class);
        kernelProcessorField.set(engine, kernelProcessor);
        when(backendConnection.getConnectionSession().getStatementManager()).thenReturn(new JDBCBackendStatement());
        SQLFederationExecutor federationExecutor = mock(SQLFederationExecutor.class);
        try (
                MockedStatic<SQLFederationExecutorFactory> federationExecutorFactory = mockStatic(SQLFederationExecutorFactory.class);
                MockedStatic<SystemSchemaUtil> systemSchemaUtil = mockStatic(SystemSchemaUtil.class)) {
            when(federationExecutor.executeQuery(any(DriverExecutionPrepareEngine.class), any(ProxyJDBCExecutorCallback.class), any(SQLFederationExecutorContext.class))).thenReturn(resultSet);
            when(resultSet.getMetaData().getColumnCount()).thenReturn(1);
            when(resultSet.getMetaData().getColumnType(1)).thenReturn(Types.INTEGER);
            when(resultSet.next()).thenReturn(true, false);
            when(resultSet.getObject(1)).thenReturn(Integer.MAX_VALUE);
            federationExecutorFactory.when(() -> SQLFederationExecutorFactory.newInstance(anyString(), nullable(String.class),
                    any(ShardingSphereMetaData.class), any(JDBCExecutor.class), any(EventBusContext.class))).thenReturn(federationExecutor);
            systemSchemaUtil.when(() -> SystemSchemaUtil.containsSystemSchema(any(DatabaseType.class), any(), any(ShardingSphereDatabase.class))).thenReturn(true);
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
        JDBCDatabaseCommunicationEngine engine =
                DatabaseCommunicationEngineFactory.getInstance().newDatabaseCommunicationEngine(new QueryContext(sqlStatementContext, "schemaName", Collections.emptyList()), backendConnection, true);
        assertNotNull(engine);
        assertThat(engine, instanceOf(DatabaseCommunicationEngine.class));
        Field queryHeadersField = DatabaseCommunicationEngine.class.getDeclaredField("queryHeaders");
        ShardingSphereDatabase database = createDatabaseMetaData();
        MemberAccessor accessor = Plugins.getMemberAccessor();
        accessor.set(queryHeadersField, engine, Collections.singletonList(
                new QueryHeaderBuilderEngine(new MySQLDatabaseType()).build(createQueryResultMetaData(), database, 1)));
        Field mergedResultField = DatabaseCommunicationEngine.class.getDeclaredField("mergedResult");
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
    
    private ShardingSphereDatabase createDatabaseMetaData() {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        ShardingSphereColumn column = new ShardingSphereColumn("order_id", Types.INTEGER, true, false, false, true);
        when(result.getSchema(DefaultDatabase.LOGIC_NAME).getTable("t_logic_order")).thenReturn(
                new ShardingSphereTable("t_logic_order", Collections.singletonList(column), Collections.singletonList(new ShardingSphereIndex("order_id")), Collections.emptyList()));
        when(result.getRuleMetaData().getRules()).thenReturn(Collections.singletonList(mock(ShardingRule.class)));
        when(result.getName()).thenReturn("sharding_schema");
        return result;
    }
    
    private QueryResultMetaData createQueryResultMetaData() throws SQLException {
        QueryResultMetaData result = mock(QueryResultMetaData.class);
        when(result.getTableName(1)).thenReturn("t_order");
        when(result.getColumnLabel(1)).thenReturn("order_id");
        when(result.getColumnName(1)).thenReturn("order_id");
        when(result.getColumnType(1)).thenReturn(Types.INTEGER);
        when(result.isSigned(1)).thenReturn(true);
        when(result.isAutoIncrement(1)).thenReturn(true);
        when(result.getColumnLength(1)).thenReturn(1);
        when(result.getDecimals(1)).thenReturn(1);
        when(result.isNotNull(1)).thenReturn(true);
        return result;
    }
    
    @Test
    public void assertAddStatementCorrectly() {
        SQLStatementContext<?> sqlStatementContext = mock(SQLStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getTablesContext().getSchemaNames()).thenReturn(Collections.emptyList());
        JDBCDatabaseCommunicationEngine engine =
                DatabaseCommunicationEngineFactory.getInstance().newDatabaseCommunicationEngine(new QueryContext(sqlStatementContext, "schemaName", Collections.emptyList()), backendConnection, false);
        engine.add(statement);
        Collection<?> actual = getField(engine, "cachedStatements");
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next(), is(statement));
    }
    
    @Test
    public void assertAddResultSetCorrectly() {
        SQLStatementContext<?> sqlStatementContext = mock(SQLStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getTablesContext().getSchemaNames()).thenReturn(Collections.emptyList());
        JDBCDatabaseCommunicationEngine engine =
                DatabaseCommunicationEngineFactory.getInstance().newDatabaseCommunicationEngine(new QueryContext(sqlStatementContext, "schemaName", Collections.emptyList()), backendConnection, false);
        engine.add(resultSet);
        Collection<?> actual = getField(engine, "cachedResultSets");
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next(), is(resultSet));
    }
    
    @Test
    public void assertCloseCorrectly() throws SQLException {
        SQLStatementContext<?> sqlStatementContext = mock(SQLStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getTablesContext().getSchemaNames()).thenReturn(Collections.emptyList());
        JDBCDatabaseCommunicationEngine engine =
                DatabaseCommunicationEngineFactory.getInstance().newDatabaseCommunicationEngine(new QueryContext(sqlStatementContext, "schemaName", Collections.emptyList()), backendConnection, false);
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
        JDBCDatabaseCommunicationEngine engine =
                DatabaseCommunicationEngineFactory.getInstance().newDatabaseCommunicationEngine(new QueryContext(sqlStatementContext, "schemaName", Collections.emptyList()), backendConnection, false);
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
    @SneakyThrows
    private <T> T getField(final JDBCDatabaseCommunicationEngine target, final String fieldName) {
        Field field = JDBCDatabaseCommunicationEngine.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        return (T) field.get(target);
    }
}
