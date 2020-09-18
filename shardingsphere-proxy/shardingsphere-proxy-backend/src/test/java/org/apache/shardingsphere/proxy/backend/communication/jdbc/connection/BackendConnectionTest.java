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

package org.apache.shardingsphere.proxy.backend.communication.jdbc.connection;

import com.google.common.collect.Multimap;
import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.auth.Authentication;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.context.SchemaContext;
import org.apache.shardingsphere.infra.context.impl.StandardSchemaContexts;
import org.apache.shardingsphere.infra.context.runtime.RuntimeContext;
import org.apache.shardingsphere.infra.context.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.infra.executor.sql.ConnectionMode;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.datasource.JDBCBackendDataSource;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.transaction.BackendTransactionManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.transaction.ShardingTransactionManagerEngine;
import org.apache.shardingsphere.transaction.context.TransactionContexts;
import org.apache.shardingsphere.transaction.core.TransactionType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class BackendConnectionTest {
    
    private static final String SCHEMA_PATTERN = "schema_%s";
    
    @Mock
    private JDBCBackendDataSource backendDataSource;
    
    private final BackendConnection backendConnection = new BackendConnection(TransactionType.LOCAL);
    
    @Before
    public void setUp() throws ReflectiveOperationException {
        setSchemaContexts();
        setTransactionContexts();
        setBackendDataSource();
        backendConnection.setCurrentSchema(String.format(SCHEMA_PATTERN, 0));
    }
    
    @After
    public void clean() throws ReflectiveOperationException {
        Field field = ProxyContext.getInstance().getClass().getDeclaredField("backendDataSource");
        field.setAccessible(true);
        Class<?> clazz = field.getType();
        Object datasource = clazz.getDeclaredConstructors()[0].newInstance();
        field.set(ProxyContext.getInstance(), datasource);
    }
    
    private void setSchemaContexts() throws ReflectiveOperationException {
        Field field = ProxyContext.getInstance().getClass().getDeclaredField("schemaContexts");
        field.setAccessible(true);
        field.set(ProxyContext.getInstance(), new StandardSchemaContexts(createSchemaContextMap(), new Authentication(), new ConfigurationProperties(new Properties()), new MySQLDatabaseType()));
    }
    
    private Map<String, SchemaContext> createSchemaContextMap() {
        Map<String, SchemaContext> result = new HashMap<>(10);
        for (int i = 0; i < 10; i++) {
            String name = String.format(SCHEMA_PATTERN, i);
            RuntimeContext runtimeContext = mock(RuntimeContext.class);
            SchemaContext schemaContext = new SchemaContext(name, mock(ShardingSphereSchema.class), runtimeContext);
            result.put(name, schemaContext);
        }
        return result;
    }
    
    private void setTransactionContexts() throws ReflectiveOperationException {
        Field field = ProxyContext.getInstance().getClass().getDeclaredField("transactionContexts");
        field.setAccessible(true);
        field.set(ProxyContext.getInstance(), createTransactionContexts());
    }
    
    private TransactionContexts createTransactionContexts() {
        TransactionContexts result = mock(TransactionContexts.class, RETURNS_DEEP_STUBS);
        for (int i = 0; i < 10; i++) {
            String name = String.format(SCHEMA_PATTERN, i);
            when(result.getEngines().get(name)).thenReturn(new ShardingTransactionManagerEngine());
        }
        return result;
    }
    
    private void setBackendDataSource() throws ReflectiveOperationException {
        Field field = ProxyContext.getInstance().getClass().getDeclaredField("backendDataSource");
        field.setAccessible(true);
        field.set(ProxyContext.getInstance(), backendDataSource);
    }
    
    @Test
    public void assertGetConnectionCacheIsEmpty() throws SQLException {
        backendConnection.getTransactionStatus().setInTransaction(true);
        when(backendDataSource.getConnections(anyString(), anyString(), eq(2), any())).thenReturn(MockConnectionUtil.mockNewConnections(2));
        List<Connection> actualConnections = backendConnection.getConnections("ds1", 2, ConnectionMode.MEMORY_STRICTLY);
        assertThat(actualConnections.size(), is(2));
        assertThat(backendConnection.getConnectionSize(), is(2));
        assertTrue(backendConnection.getTransactionStatus().isInTransaction());
    }
    
    @Test
    public void assertGetConnectionSizeLessThanCache() throws SQLException {
        backendConnection.getTransactionStatus().setInTransaction(true);
        MockConnectionUtil.setCachedConnections(backendConnection, "ds1", 10);
        List<Connection> actualConnections = backendConnection.getConnections("ds1", 2, ConnectionMode.MEMORY_STRICTLY);
        assertThat(actualConnections.size(), is(2));
        assertThat(backendConnection.getConnectionSize(), is(10));
        assertTrue(backendConnection.getTransactionStatus().isInTransaction());
    }
    
    @Test
    public void assertGetConnectionSizeGreaterThanCache() throws SQLException {
        backendConnection.getTransactionStatus().setInTransaction(true);
        MockConnectionUtil.setCachedConnections(backendConnection, "ds1", 10);
        when(backendDataSource.getConnections(anyString(), anyString(), eq(2), any())).thenReturn(MockConnectionUtil.mockNewConnections(2));
        List<Connection> actualConnections = backendConnection.getConnections("ds1", 12, ConnectionMode.MEMORY_STRICTLY);
        assertThat(actualConnections.size(), is(12));
        assertThat(backendConnection.getConnectionSize(), is(12));
        assertTrue(backendConnection.getTransactionStatus().isInTransaction());
    }
    
    @Test
    public void assertGetConnectionWithMethodInvocation() throws SQLException {
        backendConnection.getTransactionStatus().setInTransaction(true);
        when(backendDataSource.getConnections(anyString(), anyString(), eq(2), any())).thenReturn(MockConnectionUtil.mockNewConnections(2));
        setMethodInvocation();
        List<Connection> actualConnections = backendConnection.getConnections("ds1", 2, ConnectionMode.MEMORY_STRICTLY);
        verify(backendConnection.getMethodInvocations().iterator().next(), times(2)).invoke(any());
        assertThat(actualConnections.size(), is(2));
        assertTrue(backendConnection.getTransactionStatus().isInTransaction());
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private void setMethodInvocation() {
        MethodInvocation invocation = mock(MethodInvocation.class);
        Collection<MethodInvocation> methodInvocations = new LinkedList<>();
        methodInvocations.add(invocation);
        Field field = backendConnection.getClass().getDeclaredField("methodInvocations");
        field.setAccessible(true);
        field.set(backendConnection, methodInvocations);
    }
    
    @Test
    public void assertMultiThreadsGetConnection() throws SQLException, InterruptedException {
        MockConnectionUtil.setCachedConnections(backendConnection, "ds1", 10);
        when(backendDataSource.getConnections(anyString(), anyString(), eq(2), any())).thenReturn(MockConnectionUtil.mockNewConnections(2));
        Thread thread1 = new Thread(this::assertOneThreadResult);
        Thread thread2 = new Thread(this::assertOneThreadResult);
        thread1.start();
        thread2.start();
        thread1.join();
        thread2.join();
    }
    
    @SneakyThrows
    private void assertOneThreadResult() {
        backendConnection.getTransactionStatus().setInTransaction(true);
        List<Connection> actualConnections = backendConnection.getConnections("ds1", 12, ConnectionMode.MEMORY_STRICTLY);
        assertThat(actualConnections.size(), is(12));
        assertThat(backendConnection.getConnectionSize(), is(12));
        assertTrue(backendConnection.getTransactionStatus().isInTransaction());
    }
    
    @Test(expected = ShardingSphereException.class)
    public void assertFailedSwitchTransactionTypeWhileBegin() {
        BackendTransactionManager transactionManager = new BackendTransactionManager(backendConnection);
        transactionManager.begin();
        backendConnection.getTransactionStatus().setTransactionType(TransactionType.XA);
    }
    
    @Test(expected = ShardingSphereException.class)
    public void assertFailedSwitchSchemaWhileBegin() {
        BackendTransactionManager transactionManager = new BackendTransactionManager(backendConnection);
        transactionManager.begin();
        backendConnection.setCurrentSchema("newSchema");
    }
    
    @Test
    public void assertIsNotSerialExecuteWhenNotInTransaction() {
        backendConnection.getTransactionStatus().setInTransaction(false);
        assertFalse(backendConnection.isSerialExecute());
    }
    
    @Test
    public void assertIsNotSerialExecuteWhenInTransactionAndBaseTransactionType() {
        backendConnection.getTransactionStatus().setInTransaction(false);
        backendConnection.getTransactionStatus().setTransactionType(TransactionType.BASE);
        assertFalse(backendConnection.isSerialExecute());
    }
    
    @Test
    public void assertIsSerialExecuteWhenInTransactionAndLocalTransactionType() {
        backendConnection.getTransactionStatus().setTransactionType(TransactionType.LOCAL);
        backendConnection.getTransactionStatus().setInTransaction(true);
        assertTrue(backendConnection.isSerialExecute());
    }
    
    @Test
    public void assertIsSerialExecuteWhenInTransactionAndXaTransactionType() {
        backendConnection.getTransactionStatus().setTransactionType(TransactionType.XA);
        backendConnection.getTransactionStatus().setInTransaction(true);
        assertTrue(backendConnection.isSerialExecute());
    }
    
    @SneakyThrows
    @Test
    public void assertSetFetchSizeAsExpected() {
        Statement statement = mock(Statement.class);
        Method setFetchSizeMethod = backendConnection.getClass().getDeclaredMethod("setFetchSize", Statement.class);
        setFetchSizeMethod.setAccessible(true);
        setFetchSizeMethod.invoke(backendConnection, statement);
        verify(statement, times(1)).setFetchSize(Integer.MIN_VALUE);
    }
    
    @SneakyThrows
    @Test
    public void assertAddStatementCorrectly() {
        Statement statement = mock(Statement.class);
        backendConnection.add(statement);
        Field field = backendConnection.getClass().getDeclaredField("cachedStatements");
        field.setAccessible(true);
        assertTrue(((Collection<Statement>) field.get(backendConnection)).contains(statement));
    }
    
    @SneakyThrows
    @Test
    public void assertAddResultSetCorrectly() {
        ResultSet resultSet = mock(ResultSet.class);
        backendConnection.add(resultSet);
        Field field = backendConnection.getClass().getDeclaredField("cachedResultSets");
        field.setAccessible(true);
        assertTrue(((Collection<ResultSet>) field.get(backendConnection)).contains(resultSet));
    }
    
    @SneakyThrows
    @Test
    public void assertCloseResultSetsCorrectly() {
        Field field = backendConnection.getClass().getDeclaredField("cachedResultSets");
        field.setAccessible(true);
        Collection<ResultSet> cachedResultSets = (Collection<ResultSet>) field.get(backendConnection);
        ResultSet resultSet = mock(ResultSet.class);
        cachedResultSets.add(resultSet);
        backendConnection.closeResultSets();
        verify(resultSet, times(1)).close();
        assertTrue(cachedResultSets.isEmpty());
    }
    
    @SneakyThrows
    @Test
    public void assertCloseResultSetsWithExceptionThrown() {
        Field field = backendConnection.getClass().getDeclaredField("cachedResultSets");
        field.setAccessible(true);
        Collection<ResultSet> cachedResultSets = (Collection<ResultSet>) field.get(backendConnection);
        ResultSet resultSet = mock(ResultSet.class);
        SQLException sqlException = new SQLException("");
        doThrow(sqlException).when(resultSet).close();
        cachedResultSets.add(resultSet);
        Collection<SQLException> result = backendConnection.closeResultSets();
        verify(resultSet, times(1)).close();
        assertTrue(cachedResultSets.isEmpty());
        assertTrue(result.contains(sqlException));
    }
    
    @SneakyThrows
    @Test
    public void assertCloseStatementsCorrectly() {
        Field field = backendConnection.getClass().getDeclaredField("cachedStatements");
        field.setAccessible(true);
        Collection<Statement> cachedStatement = (Collection<Statement>) field.get(backendConnection);
        Statement statement = mock(Statement.class);
        cachedStatement.add(statement);
        backendConnection.closeStatements();
        verify(statement, times(1)).close();
        assertTrue(cachedStatement.isEmpty());
    }
    
    @SneakyThrows
    @Test
    public void assertCloseStatementsWithExceptionThrown() {
        Field field = backendConnection.getClass().getDeclaredField("cachedStatements");
        field.setAccessible(true);
        Collection<Statement> cachedStatement = (Collection<Statement>) field.get(backendConnection);
        Statement statement = mock(Statement.class);
        cachedStatement.add(statement);
        SQLException sqlException = new SQLException("");
        doThrow(sqlException).when(statement).close();
        Collection<SQLException> result = backendConnection.closeStatements();
        verify(statement, times(1)).close();
        assertTrue(cachedStatement.isEmpty());
        assertTrue(result.contains(sqlException));
    }
    
    @SneakyThrows
    @Test
    public void assertCloseConnectionsCorrectlyWhenNotForceRollback() {
        Field field = backendConnection.getClass().getDeclaredField("cachedConnections");
        field.setAccessible(true);
        Multimap<String, Connection> cachedConnections = (Multimap<String, Connection>) field.get(backendConnection);
        Connection connection = prepareCachedConnections();
        cachedConnections.put("ignoredDataSourceName", connection);
        ConnectionStatus connectionStatus = mock(ConnectionStatus.class);
        prepareConnectionStatus(connectionStatus);
        backendConnection.closeConnections(false);
        verify(connection, times(1)).close();
        assertTrue(cachedConnections.isEmpty());
        verifyMethodInvocationsEmpty();
        verify(connectionStatus, times(1)).switchToReleased();
    }
    
    @SneakyThrows
    @Test
    public void assertCloseConnectionsCorrectlyWhenForceRollbackAndNotInTransaction() {
        ConnectionStatus connectionStatus = mock(ConnectionStatus.class);
        prepareConnectionStatus(connectionStatus);
        backendConnection.getTransactionStatus().setInTransaction(false);
        Connection connection = prepareCachedConnections();
        backendConnection.closeConnections(true);
        verify(connection, never()).rollback();
        verify(connectionStatus, times(1)).switchToReleased();
    }
    
    @SneakyThrows
    @Test
    public void assertCloseConnectionsCorrectlyWhenForceRollbackAndInTransaction() {
        ConnectionStatus connectionStatus = mock(ConnectionStatus.class);
        prepareConnectionStatus(connectionStatus);
        backendConnection.getTransactionStatus().setInTransaction(true);
        Connection connection = prepareCachedConnections();
        backendConnection.closeConnections(true);
        verify(connection, times(1)).rollback();
        verify(connectionStatus, times(1)).switchToReleased();
    }
    
    @SneakyThrows
    @Test
    public void assertCloseConnectionsCorrectlyWhenSQLExceptionThrown() {
        ConnectionStatus connectionStatus = mock(ConnectionStatus.class);
        prepareConnectionStatus(connectionStatus);
        Connection connection = prepareCachedConnections();
        SQLException sqlException = new SQLException("");
        doThrow(sqlException).when(connection).close();
        assertTrue(backendConnection.closeConnections(false).contains(sqlException));
    }
    
    @SneakyThrows
    @Test
    public void assertCreateStorageResourceCorrectlyWhenConnectionModeMemoryStrictly() {
        Connection connection = mock(Connection.class);
        Statement statement = mock(Statement.class);
        when(connection.createStatement()).thenReturn(statement);
        assertThat(backendConnection.createStorageResource(connection, ConnectionMode.MEMORY_STRICTLY, null), is(statement));
        verify(connection, times(1)).createStatement();
    }
    
    @SneakyThrows
    @Test
    public void assertGetConnectionsWithoutTransactions() {
        backendConnection.getTransactionStatus().setInTransaction(false);
        List<Connection> connectionList = MockConnectionUtil.mockNewConnections(1);
        when(backendDataSource.getConnections(anyString(), anyString(), eq(1), any())).thenReturn(connectionList);
        List<Connection> fetchedConnections = backendConnection.getConnections("ds1", 1, null);
        assertThat(fetchedConnections.size(), is(1));
        assertTrue(fetchedConnections.contains(connectionList.get(0)));
        assertConnectionsCached("ds1", connectionList);
    }
    
    @SneakyThrows
    private void assertConnectionsCached(final String dataSourceName, final Collection<Connection> collectionList) {
        Field field = backendConnection.getClass().getDeclaredField("cachedConnections");
        field.setAccessible(true);
        Multimap<String, Connection> cachedConnections = (Multimap<String, Connection>) field.get(backendConnection);
        assertTrue(cachedConnections.containsKey(dataSourceName));
        assertArrayEquals(cachedConnections.get(dataSourceName).toArray(), collectionList.toArray());
    }
    
    @SneakyThrows
    private Connection prepareCachedConnections() {
        Field field = backendConnection.getClass().getDeclaredField("cachedConnections");
        field.setAccessible(true);
        Multimap<String, Connection> cachedConnections = (Multimap<String, Connection>) field.get(backendConnection);
        Connection connection = mock(Connection.class);
        cachedConnections.put("ignoredDataSourceName", connection);
        return connection;
    }
    
    @SneakyThrows
    private void prepareConnectionStatus(final ConnectionStatus connectionStatus) {
        Field field = backendConnection.getClass().getDeclaredField("connectionStatus");
        field.setAccessible(true);
        field.set(backendConnection, connectionStatus);
    }
    
    @SneakyThrows
    private void verifyMethodInvocationsEmpty() {
        Field field = backendConnection.getClass().getDeclaredField("methodInvocations");
        field.setAccessible(true);
        Collection<MethodInvocation> methodInvocations = (Collection<MethodInvocation>) field.get(backendConnection);
        assertTrue(methodInvocations.isEmpty());
    }
}
