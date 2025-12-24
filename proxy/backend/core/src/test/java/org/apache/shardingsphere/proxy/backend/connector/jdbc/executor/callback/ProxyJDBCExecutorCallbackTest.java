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

package org.apache.shardingsphere.proxy.backend.connector.jdbc.executor.callback;

import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.ConnectionMode;
import org.apache.shardingsphere.infra.executor.sql.execute.result.ExecuteResult;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.driver.jdbc.type.memory.JDBCMemoryQueryResult;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.driver.jdbc.type.stream.JDBCStreamQueryResult;
import org.apache.shardingsphere.infra.executor.sql.execute.result.update.UpdateResult;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.connector.DatabaseProxyConnector;
import org.apache.shardingsphere.proxy.backend.connector.sane.DialectSaneQueryResultEngine;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.internal.configuration.plugins.Plugins;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.Properties;
import java.util.Queue;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

@ExtendWith(MockitoExtension.class)
class ProxyJDBCExecutorCallbackTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    private Object originalContextManager;
    
    @Mock
    private DatabaseProxyConnector databaseProxyConnector;
    
    @Mock
    private ResourceMetaData resourceMetaData;
    
    @Mock
    private SQLStatement sqlStatement;
    
    @Mock
    private SQLException sqlException;
    
    @AfterEach
    void resetContextManager() throws ReflectiveOperationException {
        Plugins.getMemberAccessor().set(ProxyContext.class.getDeclaredField("contextManager"), ProxyContext.getInstance(), originalContextManager);
    }
    
    @Test
    void assertExecuteSQLCoversQueryModesAndFetchMetaDataToggle() throws ReflectiveOperationException, SQLException {
        setContextManager(mock(ContextManager.class));
        Statement streamStatement = mock(Statement.class);
        ResultSet streamResultSet = mock(ResultSet.class);
        ResultSetMetaData streamMetaData = mock(ResultSetMetaData.class);
        when(streamStatement.getResultSet()).thenReturn(streamResultSet);
        when(streamResultSet.getMetaData()).thenReturn(streamMetaData);
        Statement memoryStatement = mock(Statement.class);
        ResultSet memoryResultSet = mock(ResultSet.class);
        ResultSetMetaData memoryMetaData = mock(ResultSetMetaData.class);
        when(memoryStatement.getResultSet()).thenReturn(memoryResultSet);
        when(memoryResultSet.getMetaData()).thenReturn(memoryMetaData);
        when(memoryMetaData.getColumnCount()).thenReturn(0);
        ProxyJDBCExecutorCallback callback = mockCallback(true, true, true, true);
        ExecuteResult streamResult = callback.executeSQL("foo_sql", streamStatement, ConnectionMode.MEMORY_STRICTLY, databaseType);
        ExecuteResult memoryResult = callback.executeSQL("bar_sql", memoryStatement, ConnectionMode.CONNECTION_STRICTLY, databaseType);
        assertThat(streamResult, isA(JDBCStreamQueryResult.class));
        assertThat(memoryResult, isA(JDBCMemoryQueryResult.class));
        verify(databaseProxyConnector).add(streamStatement);
        verify(databaseProxyConnector).add(streamResultSet);
        verify(databaseProxyConnector).add(memoryStatement);
        verify(databaseProxyConnector).add(memoryResultSet);
    }
    
    @Test
    void assertExecuteSQLUpdateCoversGeneratedKeyBranches() throws ReflectiveOperationException, SQLException {
        setContextManager(mock(ContextManager.class));
        Statement integerStatement = mock(Statement.class);
        ResultSet integerResultSet = mock(ResultSet.class);
        ResultSetMetaData integerMetaData = mock(ResultSetMetaData.class);
        when(integerStatement.getUpdateCount()).thenReturn(3);
        when(integerStatement.getGeneratedKeys()).thenReturn(integerResultSet);
        when(integerResultSet.next()).thenReturn(true);
        when(integerResultSet.getMetaData()).thenReturn(integerMetaData);
        when(integerMetaData.getColumnType(1)).thenReturn(Types.INTEGER);
        when(integerResultSet.getLong(1)).thenReturn(99L);
        Statement nonIntegerStatement = mock(Statement.class);
        ResultSet nonIntegerResultSet = mock(ResultSet.class);
        ResultSetMetaData nonIntegerMetaData = mock(ResultSetMetaData.class);
        when(nonIntegerStatement.getUpdateCount()).thenReturn(4);
        when(nonIntegerStatement.getGeneratedKeys()).thenReturn(nonIntegerResultSet);
        when(nonIntegerResultSet.next()).thenReturn(true);
        when(nonIntegerResultSet.getMetaData()).thenReturn(nonIntegerMetaData);
        when(nonIntegerMetaData.getColumnType(1)).thenReturn(Types.VARCHAR);
        Statement noRowStatement = mock(Statement.class);
        ResultSet emptyResultSet = mock(ResultSet.class);
        when(noRowStatement.getUpdateCount()).thenReturn(5);
        when(noRowStatement.getGeneratedKeys()).thenReturn(emptyResultSet);
        Statement unsupportedStatement = mock(Statement.class);
        when(unsupportedStatement.getUpdateCount()).thenReturn(6);
        when(unsupportedStatement.getGeneratedKeys()).thenThrow(new SQLFeatureNotSupportedException());
        ProxyJDBCExecutorCallback callback = mockCallback(true, false, false, false, false, false);
        UpdateResult integerUpdate = (UpdateResult) callback.executeSQL("int_sql", integerStatement, ConnectionMode.MEMORY_STRICTLY, databaseType);
        assertThat(integerUpdate.getUpdateCount(), is(3));
        assertThat(integerUpdate.getLastInsertId(), is(99L));
        UpdateResult nonIntegerUpdate = (UpdateResult) callback.executeSQL("varchar_sql", nonIntegerStatement, ConnectionMode.MEMORY_STRICTLY, databaseType);
        assertThat(nonIntegerUpdate.getUpdateCount(), is(4));
        assertThat(nonIntegerUpdate.getLastInsertId(), is(0L));
        UpdateResult emptyUpdate = (UpdateResult) callback.executeSQL("empty_sql", noRowStatement, ConnectionMode.MEMORY_STRICTLY, databaseType);
        assertThat(emptyUpdate.getUpdateCount(), is(5));
        assertThat(emptyUpdate.getLastInsertId(), is(0L));
        UpdateResult unsupportedUpdate = (UpdateResult) callback.executeSQL("unsupported_sql", unsupportedStatement, ConnectionMode.MEMORY_STRICTLY, databaseType);
        assertThat(unsupportedUpdate.getUpdateCount(), is(6));
        assertThat(unsupportedUpdate.getLastInsertId(), is(0L));
        verify(databaseProxyConnector, times(4)).add(any(Statement.class));
    }
    
    @Test
    void assertExecuteSQLUpdateWithoutGeneratedKeys() throws ReflectiveOperationException, SQLException {
        setContextManager(mock(ContextManager.class));
        Statement statement = mock(Statement.class);
        when(statement.getUpdateCount()).thenReturn(7);
        ProxyJDBCExecutorCallback callback = mockCallback(false, false, false);
        UpdateResult updateResult = (UpdateResult) callback.executeSQL("no_key_sql", statement, ConnectionMode.CONNECTION_STRICTLY, databaseType);
        assertThat(updateResult.getUpdateCount(), is(7));
        assertThat(updateResult.getLastInsertId(), is(0L));
        verify(databaseProxyConnector).add(statement);
    }
    
    @Test
    void assertGetSaneResultReturnsEngineResult() throws ReflectiveOperationException, SQLException {
        ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class);
        when(metaData.getProps()).thenReturn(
                new ConfigurationProperties(PropertiesBuilder.build(new Property(ConfigurationPropertyKey.PROXY_FRONTEND_DATABASE_PROTOCOL_TYPE.getKey(), databaseType.getType()))));
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(contextManager.getMetaDataContexts().getMetaData()).thenReturn(metaData);
        setContextManager(contextManager);
        ProxyJDBCExecutorCallback callback = mockCallback(false, false);
        try (MockedStatic<DatabaseTypedSPILoader> spiLoader = mockStatic(DatabaseTypedSPILoader.class)) {
            DialectSaneQueryResultEngine saneQueryResultEngine = mock(DialectSaneQueryResultEngine.class);
            ExecuteResult executeResult = mock(ExecuteResult.class);
            spiLoader.when(() -> DatabaseTypedSPILoader.findService(DialectSaneQueryResultEngine.class, databaseType)).thenReturn(Optional.of(saneQueryResultEngine));
            when(saneQueryResultEngine.getSaneQueryResult(sqlStatement, sqlException)).thenReturn(Optional.of(executeResult));
            Optional<ExecuteResult> actual = callback.getSaneResult(sqlStatement, sqlException);
            assertTrue(actual.isPresent());
            assertThat(actual.get(), is(executeResult));
        }
    }
    
    @Test
    void assertGetSaneResultReturnsEmptyWhenNoDatabaseConfigured() throws ReflectiveOperationException, SQLException {
        ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class);
        when(metaData.getProps()).thenReturn(new ConfigurationProperties(new Properties()));
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(contextManager.getMetaDataContexts().getMetaData()).thenReturn(metaData);
        setContextManager(contextManager);
        ProxyJDBCExecutorCallback callback = mockCallback(false, false);
        try (MockedStatic<DatabaseTypedSPILoader> databaseTypedSPILoader = mockStatic(DatabaseTypedSPILoader.class)) {
            databaseTypedSPILoader.when(() -> DatabaseTypedSPILoader.findService(DialectSaneQueryResultEngine.class, databaseType)).thenReturn(Optional.empty());
            assertFalse(callback.getSaneResult(sqlStatement, sqlException).isPresent());
        }
    }
    
    @Test
    void assertGetSaneResultUsesExistingDatabaseProtocolType() throws ReflectiveOperationException, SQLException {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class);
        ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class);
        when(metaData.getProps()).thenReturn(new ConfigurationProperties(new Properties()));
        when(metaData.getAllDatabases()).thenReturn(Collections.singletonList(database));
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(contextManager.getMetaDataContexts().getMetaData()).thenReturn(metaData);
        setContextManager(contextManager);
        ProxyJDBCExecutorCallback callback = mockCallback(false, false);
        try (MockedStatic<DatabaseTypedSPILoader> spiLoader = mockStatic(DatabaseTypedSPILoader.class)) {
            spiLoader.when(() -> DatabaseTypedSPILoader.findService(DialectSaneQueryResultEngine.class, databaseType)).thenReturn(Optional.empty());
            Optional<ExecuteResult> actual = callback.getSaneResult(sqlStatement, sqlException);
            assertFalse(actual.isPresent());
        }
    }
    
    private void setContextManager(final ContextManager contextManager) throws ReflectiveOperationException {
        Field contextManagerField = ProxyContext.class.getDeclaredField("contextManager");
        if (null == originalContextManager) {
            originalContextManager = Plugins.getMemberAccessor().get(contextManagerField, ProxyContext.getInstance());
        }
        Plugins.getMemberAccessor().set(contextManagerField, ProxyContext.getInstance(), contextManager);
    }
    
    private ProxyJDBCExecutorCallback mockCallback(final boolean isReturnGeneratedKeys, final boolean fetchMetaData, final Boolean... executeResults) throws SQLException {
        ProxyJDBCExecutorCallback result = mock(ProxyJDBCExecutorCallback.class, withSettings().useConstructor(
                databaseType, resourceMetaData, sqlStatement, databaseProxyConnector, isReturnGeneratedKeys, false, fetchMetaData).defaultAnswer(CALLS_REAL_METHODS));
        Queue<Boolean> executeResultQueue = new ArrayDeque<>(Arrays.asList(executeResults));
        lenient().when(result.execute(anyString(), any(Statement.class), anyBoolean())).thenAnswer(invocation -> !executeResultQueue.isEmpty() && executeResultQueue.remove());
        return result;
    }
}
