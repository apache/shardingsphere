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
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.DefaultDatabase;
import org.apache.shardingsphere.infra.database.type.dialect.H2DatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResultMetaData;
import org.apache.shardingsphere.infra.federation.optimizer.context.OptimizerContext;
import org.apache.shardingsphere.infra.merge.result.impl.memory.MemoryMergedResult;
import org.apache.shardingsphere.infra.merge.result.impl.memory.MemoryQueryResultRow;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereIndex;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.proxy.backend.communication.DatabaseCommunicationEngine;
import org.apache.shardingsphere.proxy.backend.communication.DatabaseCommunicationEngineFactory;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.JDBCBackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryHeaderBuilderEngine;
import org.apache.shardingsphere.proxy.backend.util.ProxyContextRestorer;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
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
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class JDBCDatabaseCommunicationEngineTest extends ProxyContextRestorer {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private JDBCBackendConnection backendConnection;
    
    @Mock
    private Statement statement;
    
    @Mock
    private ResultSet resultSet;
    
    @Before
    public void setUp() {
        when(backendConnection.getConnectionSession().getDatabaseName()).thenReturn("db");
        MetaDataContexts metaDataContexts = new MetaDataContexts(mock(MetaDataPersistService.class),
                new ShardingSphereMetaData(mockDatabases(), mock(ShardingSphereRuleMetaData.class), new ConfigurationProperties(new Properties())),
                mock(OptimizerContext.class, RETURNS_DEEP_STUBS));
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(contextManager.getMetaDataContexts()).thenReturn(metaDataContexts);
        ProxyContext.init(contextManager);
    }
    
    private Map<String, ShardingSphereDatabase> mockDatabases() {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(result.getResource().getDatabaseType()).thenReturn(new H2DatabaseType());
        when(result.getRuleMetaData().getRules()).thenReturn(Collections.emptyList());
        return Collections.singletonMap("db", result);
    }
    
    @Test
    public void assertBinaryProtocolQueryHeader() throws SQLException, NoSuchFieldException, IllegalAccessException {
        SQLStatementContext<?> sqlStatementContext = mock(SQLStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getTablesContext().getSchemaNames()).thenReturn(Collections.emptyList());
        JDBCDatabaseCommunicationEngine engine =
                DatabaseCommunicationEngineFactory.getInstance().newBinaryProtocolInstance(sqlStatementContext, "schemaName", Collections.emptyList(), backendConnection);
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
            engine.getQueryResponseRow();
        } catch (final SQLException | IndexOutOfBoundsException e) {
            ex = e;
        } finally {
            assertFalse(ex instanceof IndexOutOfBoundsException);
        }
    }
    
    private ShardingSphereDatabase createDatabaseMetaData() {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        ShardingSphereColumn column = new ShardingSphereColumn("order_id", Types.INTEGER, true, false, false);
        when(result.getSchemas().get(DefaultDatabase.LOGIC_NAME).get("t_logic_order")).thenReturn(
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
                DatabaseCommunicationEngineFactory.getInstance().newBinaryProtocolInstance(sqlStatementContext, "schemaName", Collections.emptyList(), backendConnection);
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
                DatabaseCommunicationEngineFactory.getInstance().newBinaryProtocolInstance(sqlStatementContext, "schemaName", Collections.emptyList(), backendConnection);
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
                DatabaseCommunicationEngineFactory.getInstance().newBinaryProtocolInstance(sqlStatementContext, "schemaName", Collections.emptyList(), backendConnection);
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
                DatabaseCommunicationEngineFactory.getInstance().newBinaryProtocolInstance(sqlStatementContext, "schemaName", Collections.emptyList(), backendConnection);
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
