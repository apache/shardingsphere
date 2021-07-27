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
import org.apache.shardingsphere.infra.config.persist.DistMetaDataPersistService;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.context.metadata.impl.StandardMetaDataContexts;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.infra.executor.kernel.ExecutorEngine;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.ConnectionMode;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.optimize.context.OptimizeContextFactory;
import org.apache.shardingsphere.proxy.backend.communication.DatabaseCommunicationEngine;
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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
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
        setMetaDataContexts();
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
    
    private void setMetaDataContexts() throws ReflectiveOperationException {
        Field field = ProxyContext.getInstance().getClass().getDeclaredField("metaDataContexts");
        field.setAccessible(true);
        field.set(ProxyContext.getInstance(), new StandardMetaDataContexts(mock(DistMetaDataPersistService.class), createMetaDataMap(), 
                mock(ShardingSphereRuleMetaData.class), mock(ExecutorEngine.class), new ConfigurationProperties(new Properties()), mock(OptimizeContextFactory.class)));
    }
    
    private Map<String, ShardingSphereMetaData> createMetaDataMap() {
        Map<String, ShardingSphereMetaData> result = new HashMap<>(10);
        for (int i = 0; i < 10; i++) {
            String name = String.format(SCHEMA_PATTERN, i);
            ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class, RETURNS_DEEP_STUBS);
            when(metaData.getResource().getDatabaseType()).thenReturn(new MySQLDatabaseType());
            result.put(name, metaData);
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
    public void assertGetConnectionWithConnectionPostProcessors() throws SQLException {
        backendConnection.getTransactionStatus().setInTransaction(true);
        when(backendDataSource.getConnections(anyString(), anyString(), eq(2), any())).thenReturn(MockConnectionUtil.mockNewConnections(2));
        setConnectionPostProcessors();
        List<Connection> actualConnections = backendConnection.getConnections("ds1", 2, ConnectionMode.MEMORY_STRICTLY);
        verify(backendConnection.getConnectionPostProcessors().iterator().next(), times(2)).process(any());
        assertThat(actualConnections.size(), is(2));
        assertTrue(backendConnection.getTransactionStatus().isInTransaction());
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private void setConnectionPostProcessors() {
        ConnectionPostProcessor invocation = mock(ConnectionPostProcessor.class);
        Collection<ConnectionPostProcessor> connectionPostProcessors = new LinkedList<>();
        connectionPostProcessors.add(invocation);
        Field field = backendConnection.getClass().getDeclaredField("connectionPostProcessors");
        field.setAccessible(true);
        field.set(backendConnection, connectionPostProcessors);
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
    
    @SneakyThrows(SQLException.class)
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
    
    @Test
    public void assertSetFetchSizeAsExpected() throws InvocationTargetException, IllegalAccessException, NoSuchMethodException, SQLException {
        Statement statement = mock(Statement.class);
        Method setFetchSizeMethod = backendConnection.getClass().getDeclaredMethod("setFetchSize", Statement.class);
        setFetchSizeMethod.setAccessible(true);
        setFetchSizeMethod.invoke(backendConnection, statement);
        verify(statement, times(1)).setFetchSize(Integer.MIN_VALUE);
    }
    
    @Test
    public void assertCloseConnectionsCorrectlyWhenNotForceRollback() throws NoSuchFieldException, IllegalAccessException, SQLException {
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
        verifyConnectionPostProcessorsEmpty();
        verify(connectionStatus, times(1)).switchToReleased();
    }
    
    @Test
    public void assertCloseConnectionsCorrectlyWhenForceRollbackAndNotInTransaction() throws SQLException {
        ConnectionStatus connectionStatus = mock(ConnectionStatus.class);
        prepareConnectionStatus(connectionStatus);
        backendConnection.getTransactionStatus().setInTransaction(false);
        Connection connection = prepareCachedConnections();
        backendConnection.closeConnections(true);
        verify(connection, never()).rollback();
        verify(connectionStatus, times(1)).switchToReleased();
    }
    
    @Test
    public void assertCloseConnectionsCorrectlyWhenForceRollbackAndInTransaction() throws SQLException {
        ConnectionStatus connectionStatus = mock(ConnectionStatus.class);
        prepareConnectionStatus(connectionStatus);
        backendConnection.getTransactionStatus().setInTransaction(true);
        Connection connection = prepareCachedConnections();
        backendConnection.closeConnections(true);
        verify(connection, times(1)).rollback();
        verify(connectionStatus, times(1)).switchToReleased();
    }
    
    @Test
    public void assertCloseConnectionsCorrectlyWhenSQLExceptionThrown() throws SQLException {
        ConnectionStatus connectionStatus = mock(ConnectionStatus.class);
        prepareConnectionStatus(connectionStatus);
        Connection connection = prepareCachedConnections();
        SQLException sqlException = new SQLException("");
        doThrow(sqlException).when(connection).close();
        assertTrue(backendConnection.closeConnections(false).contains(sqlException));
    }
    
    @Test
    public void assertCreateStorageResourceCorrectlyWhenConnectionModeMemoryStrictly() throws SQLException {
        Connection connection = mock(Connection.class);
        Statement statement = mock(Statement.class);
        when(connection.createStatement()).thenReturn(statement);
        assertThat(backendConnection.createStorageResource(connection, ConnectionMode.MEMORY_STRICTLY, null), is(statement));
        verify(connection, times(1)).createStatement();
    }
    
    @Test
    public void assertGetConnectionsWithoutTransactions() throws SQLException {
        backendConnection.getTransactionStatus().setInTransaction(false);
        List<Connection> connections = MockConnectionUtil.mockNewConnections(1);
        when(backendDataSource.getConnections(anyString(), anyString(), eq(1), any())).thenReturn(connections);
        List<Connection> fetchedConnections = backendConnection.getConnections("ds1", 1, null);
        assertThat(fetchedConnections.size(), is(1));
        assertTrue(fetchedConnections.contains(connections.get(0)));
        assertConnectionsCached("ds1", connections);
    }
    
    @SuppressWarnings("unchecked")
    @SneakyThrows(ReflectiveOperationException.class)
    private void assertConnectionsCached(final String dataSourceName, final Collection<Connection> connections) {
        Field field = backendConnection.getClass().getDeclaredField("cachedConnections");
        field.setAccessible(true);
        Multimap<String, Connection> cachedConnections = (Multimap<String, Connection>) field.get(backendConnection);
        assertTrue(cachedConnections.containsKey(dataSourceName));
        assertArrayEquals(cachedConnections.get(dataSourceName).toArray(), connections.toArray());
    }
    
    @SuppressWarnings("unchecked")
    @SneakyThrows(ReflectiveOperationException.class)
    private Connection prepareCachedConnections() {
        Field field = backendConnection.getClass().getDeclaredField("cachedConnections");
        field.setAccessible(true);
        Multimap<String, Connection> cachedConnections = (Multimap<String, Connection>) field.get(backendConnection);
        Connection connection = mock(Connection.class);
        cachedConnections.put("ignoredDataSourceName", connection);
        return connection;
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private void prepareConnectionStatus(final ConnectionStatus connectionStatus) {
        Field field = backendConnection.getClass().getDeclaredField("connectionStatus");
        field.setAccessible(true);
        field.set(backendConnection, connectionStatus);
    }
    
    @SuppressWarnings("unchecked")
    @SneakyThrows(ReflectiveOperationException.class)
    private void verifyConnectionPostProcessorsEmpty() {
        Field field = backendConnection.getClass().getDeclaredField("connectionPostProcessors");
        field.setAccessible(true);
        Collection<ConnectionPostProcessor> connectionPostProcessors = (Collection<ConnectionPostProcessor>) field.get(backendConnection);
        assertTrue(connectionPostProcessors.isEmpty());
    }
    
    @Test
    public void assertAddDatabaseCommunicationEngine() {
        DatabaseCommunicationEngine expectedEngine = mock(DatabaseCommunicationEngine.class);
        backendConnection.add(expectedEngine);
        Collection<DatabaseCommunicationEngine> actual = getDatabaseCommunicationEngines();
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next(), is(expectedEngine));
    }
    
    @Test
    public void assertMarkDatabaseCommunicationEngineInUse() {
        DatabaseCommunicationEngine expectedEngine = mock(DatabaseCommunicationEngine.class);
        backendConnection.add(expectedEngine);
        backendConnection.markResourceInUse(expectedEngine);
        Collection<DatabaseCommunicationEngine> actual = getInUseDatabaseCommunicationEngines();
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next(), is(expectedEngine));
    }
    
    @Test
    public void assertUnmarkInUseDatabaseCommunicationEngine() {
        DatabaseCommunicationEngine engine = mock(DatabaseCommunicationEngine.class);
        Collection<DatabaseCommunicationEngine> actual = getInUseDatabaseCommunicationEngines();
        actual.add(engine);
        backendConnection.unmarkResourceInUse(engine);
        assertTrue(actual.isEmpty());
    }
    
    @Test
    public void assertCloseDatabaseCommunicationEngines() throws SQLException {
        DatabaseCommunicationEngine engine = mock(DatabaseCommunicationEngine.class);
        DatabaseCommunicationEngine inUseEngine = mock(DatabaseCommunicationEngine.class);
        SQLException expectedException = mock(SQLException.class);
        doThrow(expectedException).when(engine).close();
        Collection<DatabaseCommunicationEngine> databaseCommunicationEngines = getDatabaseCommunicationEngines();
        Collection<DatabaseCommunicationEngine> inUseDatabaseCommunicationEngines = getInUseDatabaseCommunicationEngines();
        databaseCommunicationEngines.add(engine);
        databaseCommunicationEngines.add(inUseEngine);
        inUseDatabaseCommunicationEngines.add(inUseEngine);
        Collection<SQLException> actual = backendConnection.closeDatabaseCommunicationEngines(false);
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next(), is(expectedException));
        assertThat(inUseDatabaseCommunicationEngines.size(), is(1));
        assertThat(databaseCommunicationEngines.size(), is(1));
        verify(engine).close();
        backendConnection.closeDatabaseCommunicationEngines(true);
        verify(inUseEngine).close();
        assertTrue(databaseCommunicationEngines.isEmpty());
        assertTrue(inUseDatabaseCommunicationEngines.isEmpty());
    }
    
    @SneakyThrows
    private Collection<DatabaseCommunicationEngine> getDatabaseCommunicationEngines() {
        Field field = BackendConnection.class.getDeclaredField("databaseCommunicationEngines");
        field.setAccessible(true);
        return (Collection<DatabaseCommunicationEngine>) field.get(backendConnection);
    }
    
    @SneakyThrows
    private Collection<DatabaseCommunicationEngine> getInUseDatabaseCommunicationEngines() {
        Field field = BackendConnection.class.getDeclaredField("inUseDatabaseCommunicationEngines");
        field.setAccessible(true);
        return (Collection<DatabaseCommunicationEngine>) field.get(backendConnection);
    }
}
