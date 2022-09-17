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
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.ConnectionMode;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.JDBCDatabaseCommunicationEngine;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.datasource.JDBCBackendDataSource;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.statement.JDBCBackendStatement;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.exception.BackendConnectionException;
import org.apache.shardingsphere.proxy.backend.handler.ProxyBackendHandler;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.session.RequiredSessionVariableRecorder;
import org.apache.shardingsphere.proxy.backend.session.transaction.TransactionStatus;
import org.apache.shardingsphere.proxy.backend.util.ProxyContextRestorer;
import org.apache.shardingsphere.transaction.core.TransactionType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Collections;
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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class JDBCBackendConnectionTest extends ProxyContextRestorer {
    
    private static final String SCHEMA_PATTERN = "schema_%s";
    
    @Mock
    private JDBCBackendDataSource backendDataSource;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ConnectionSession connectionSession;
    
    private JDBCBackendConnection backendConnection;
    
    @Before
    public void setUp() throws ReflectiveOperationException {
        setContextManager();
        setBackendDataSource();
        when(connectionSession.getDatabaseName()).thenReturn(String.format(SCHEMA_PATTERN, 0));
        backendConnection = spy(new JDBCBackendConnection(connectionSession));
        when(connectionSession.getBackendConnection()).thenReturn(backendConnection);
        when(connectionSession.getTransactionStatus()).thenReturn(new TransactionStatus(TransactionType.LOCAL));
        JDBCBackendStatement backendStatement = new JDBCBackendStatement();
        backendStatement.setDatabaseType(connectionSession.getDatabaseType());
        when(connectionSession.getStatementManager()).thenReturn(backendStatement);
        when(connectionSession.getRequiredSessionVariableRecorder()).thenReturn(new RequiredSessionVariableRecorder());
    }
    
    private void setContextManager() {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        MetaDataContexts metaDataContexts = new MetaDataContexts(mock(MetaDataPersistService.class),
                new ShardingSphereMetaData(createDatabases(), mockGlobalRuleMetaData(), new ConfigurationProperties(new Properties())));
        when(contextManager.getMetaDataContexts()).thenReturn(metaDataContexts);
        ProxyContext.init(contextManager);
    }
    
    private Map<String, ShardingSphereDatabase> createDatabases() {
        Map<String, ShardingSphereDatabase> result = new HashMap<>(10, 1);
        for (int i = 0; i < 10; i++) {
            String name = String.format(SCHEMA_PATTERN, i);
            ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
            when(database.getResource().getDatabaseType()).thenReturn(new MySQLDatabaseType());
            result.put(name, database);
        }
        return result;
    }
    
    private ShardingSphereRuleMetaData mockGlobalRuleMetaData() {
        return mock(ShardingSphereRuleMetaData.class);
    }
    
    private void setBackendDataSource() throws ReflectiveOperationException {
        Field field = ProxyContext.getInstance().getClass().getDeclaredField("backendDataSource");
        field.setAccessible(true);
        field.set(ProxyContext.getInstance(), backendDataSource);
    }
    
    @After
    public void clean() throws ReflectiveOperationException {
        Field field = ProxyContext.getInstance().getClass().getDeclaredField("backendDataSource");
        field.setAccessible(true);
        Class<?> clazz = field.getType();
        Object datasource = clazz.getDeclaredConstructor().newInstance();
        field.set(ProxyContext.getInstance(), datasource);
    }
    
    @Test
    public void assertGetConnectionCacheIsEmpty() throws SQLException {
        connectionSession.getTransactionStatus().setInTransaction(true);
        when(backendDataSource.getConnections(anyString(), anyString(), eq(2), any())).thenReturn(MockConnectionUtil.mockNewConnections(2));
        List<Connection> actualConnections = backendConnection.getConnections("ds1", 2, ConnectionMode.MEMORY_STRICTLY);
        assertThat(actualConnections.size(), is(2));
        assertThat(backendConnection.getConnectionSize(), is(2));
        assertTrue(connectionSession.getTransactionStatus().isInTransaction());
    }
    
    @Test
    public void assertGetConnectionSizeLessThanCache() throws SQLException {
        connectionSession.getTransactionStatus().setInTransaction(true);
        MockConnectionUtil.setCachedConnections(backendConnection, "ds1", 10);
        List<Connection> actualConnections = backendConnection.getConnections("ds1", 2, ConnectionMode.MEMORY_STRICTLY);
        assertThat(actualConnections.size(), is(2));
        assertThat(backendConnection.getConnectionSize(), is(10));
        assertTrue(connectionSession.getTransactionStatus().isInTransaction());
    }
    
    @Test
    public void assertGetConnectionSizeGreaterThanCache() throws SQLException {
        connectionSession.getTransactionStatus().setInTransaction(true);
        MockConnectionUtil.setCachedConnections(backendConnection, "ds1", 10);
        when(backendDataSource.getConnections(anyString(), anyString(), eq(2), any())).thenReturn(MockConnectionUtil.mockNewConnections(2));
        List<Connection> actualConnections = backendConnection.getConnections("ds1", 12, ConnectionMode.MEMORY_STRICTLY);
        assertThat(actualConnections.size(), is(12));
        assertThat(backendConnection.getConnectionSize(), is(12));
        assertTrue(connectionSession.getTransactionStatus().isInTransaction());
    }
    
    @Test
    public void assertGetConnectionWithConnectionPostProcessors() throws SQLException {
        connectionSession.getTransactionStatus().setInTransaction(true);
        when(backendDataSource.getConnections(anyString(), anyString(), eq(2), any())).thenReturn(MockConnectionUtil.mockNewConnections(2));
        setConnectionPostProcessors();
        List<Connection> actualConnections = backendConnection.getConnections("ds1", 2, ConnectionMode.MEMORY_STRICTLY);
        verify(backendConnection.getConnectionPostProcessors().iterator().next(), times(2)).process(any());
        assertThat(actualConnections.size(), is(2));
        assertTrue(connectionSession.getTransactionStatus().isInTransaction());
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private void setConnectionPostProcessors() {
        ConnectionPostProcessor<?> connectionPostProcessor = mock(ConnectionPostProcessor.class);
        Collection<ConnectionPostProcessor<?>> connectionPostProcessors = new LinkedList<>();
        connectionPostProcessors.add(connectionPostProcessor);
        Field field = JDBCBackendConnection.class.getDeclaredField("connectionPostProcessors");
        field.setAccessible(true);
        field.set(backendConnection, connectionPostProcessors);
    }
    
    @Test
    public void assertIsNotSerialExecuteWhenNotInTransaction() {
        connectionSession.getTransactionStatus().setInTransaction(false);
        assertFalse(backendConnection.isSerialExecute());
    }
    
    @Test
    public void assertIsNotSerialExecuteWhenInTransactionAndBaseTransactionType() {
        connectionSession.getTransactionStatus().setInTransaction(false);
        connectionSession.getTransactionStatus().setTransactionType(TransactionType.BASE);
        assertFalse(backendConnection.isSerialExecute());
    }
    
    @Test
    public void assertIsSerialExecuteWhenInTransactionAndLocalTransactionType() {
        connectionSession.getTransactionStatus().setTransactionType(TransactionType.LOCAL);
        connectionSession.getTransactionStatus().setInTransaction(true);
        assertTrue(backendConnection.isSerialExecute());
    }
    
    @Test
    public void assertIsSerialExecuteWhenInTransactionAndXaTransactionType() {
        connectionSession.getTransactionStatus().setTransactionType(TransactionType.XA);
        connectionSession.getTransactionStatus().setInTransaction(true);
        assertTrue(backendConnection.isSerialExecute());
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void assertCloseConnectionsCorrectlyWhenNotForceRollback() throws NoSuchFieldException, IllegalAccessException, SQLException {
        Field field = JDBCBackendConnection.class.getDeclaredField("cachedConnections");
        field.setAccessible(true);
        Multimap<String, Connection> cachedConnections = (Multimap<String, Connection>) field.get(backendConnection);
        Connection connection = prepareCachedConnections();
        cachedConnections.put("ignoredDataSourceName", connection);
        backendConnection.closeConnections(false);
        verify(connection, times(1)).close();
        assertTrue(cachedConnections.isEmpty());
        verifyConnectionPostProcessorsEmpty();
    }
    
    @Test
    public void assertCloseConnectionsCorrectlyWhenForceRollbackAndNotInTransaction() throws SQLException {
        connectionSession.getTransactionStatus().setInTransaction(false);
        Connection connection = prepareCachedConnections();
        backendConnection.closeConnections(true);
        verify(connection, never()).rollback();
    }
    
    @Test
    public void assertCloseConnectionsCorrectlyWhenForceRollbackAndInTransaction() throws SQLException {
        connectionSession.getTransactionStatus().setInTransaction(true);
        Connection connection = prepareCachedConnections();
        backendConnection.closeConnections(true);
        verify(connection, times(1)).rollback();
    }
    
    @Test
    public void assertCloseConnectionsCorrectlyWhenSQLExceptionThrown() throws SQLException {
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
        JDBCBackendStatement backendStatement = (JDBCBackendStatement) connectionSession.getStatementManager();
        assertThat(backendStatement.createStorageResource(connection, ConnectionMode.MEMORY_STRICTLY, null), is(statement));
        verify(connection, times(1)).createStatement();
    }
    
    @Test
    public void assertGetConnectionsAndReplaySessionVariables() throws SQLException {
        connectionSession.getRequiredSessionVariableRecorder().setVariable("key", "value");
        List<Connection> actualConnections;
        try (MockedStatic<ProxyContext> mockedStatic = mockStatic(ProxyContext.class)) {
            ProxyContext proxyContext = mock(ProxyContext.class, RETURNS_DEEP_STUBS);
            mockedStatic.when(ProxyContext::getInstance).thenReturn(proxyContext);
            Connection connection = mock(Connection.class, RETURNS_DEEP_STUBS);
            when(connection.getMetaData().getDatabaseProductName()).thenReturn("PostgreSQL");
            when(proxyContext.getBackendDataSource().getConnections(anyString(), anyString(), anyInt(), any(ConnectionMode.class)))
                    .thenReturn(Collections.singletonList(connection));
            actualConnections = backendConnection.getConnections("", 1, ConnectionMode.CONNECTION_STRICTLY);
        }
        Connection actualConnection = actualConnections.get(0);
        verify(actualConnection.createStatement()).execute("SET key=value");
    }
    
    @Test
    public void assertGetConnectionsAndFailedToReplaySessionVariables() throws SQLException {
        connectionSession.getRequiredSessionVariableRecorder().setVariable("key", "value");
        Connection connection = null;
        SQLException expectedException = new SQLException();
        try (MockedStatic<ProxyContext> mockedStatic = mockStatic(ProxyContext.class)) {
            ProxyContext proxyContext = mock(ProxyContext.class, RETURNS_DEEP_STUBS);
            mockedStatic.when(ProxyContext::getInstance).thenReturn(proxyContext);
            connection = mock(Connection.class, RETURNS_DEEP_STUBS);
            when(connection.getMetaData().getDatabaseProductName()).thenReturn("PostgreSQL");
            when(connection.createStatement().execute("SET key=value")).thenThrow(expectedException);
            when(proxyContext.getBackendDataSource().getConnections(anyString(), anyString(), anyInt(), any(ConnectionMode.class)))
                    .thenReturn(Collections.singletonList(connection));
            backendConnection.getConnections("", 1, ConnectionMode.CONNECTION_STRICTLY);
        } catch (SQLException ex) {
            assertThat(ex, is(expectedException));
            verify(connection).close();
        }
    }
    
    @Test
    public void assertGetConnectionsWithoutTransactions() throws SQLException {
        connectionSession.getTransactionStatus().setInTransaction(false);
        List<Connection> connections = MockConnectionUtil.mockNewConnections(1);
        when(backendDataSource.getConnections(anyString(), anyString(), eq(1), any())).thenReturn(connections);
        List<Connection> fetchedConnections = backendConnection.getConnections("ds1", 1, null);
        assertThat(fetchedConnections.size(), is(1));
        assertTrue(fetchedConnections.contains(connections.get(0)));
        assertConnectionsCached(connectionSession.getDatabaseName() + ".ds1", connections);
    }
    
    @SuppressWarnings("unchecked")
    @SneakyThrows(ReflectiveOperationException.class)
    private void assertConnectionsCached(final String dataSourceName, final Collection<Connection> connections) {
        Field field = JDBCBackendConnection.class.getDeclaredField("cachedConnections");
        field.setAccessible(true);
        Multimap<String, Connection> cachedConnections = (Multimap<String, Connection>) field.get(backendConnection);
        assertTrue(cachedConnections.containsKey(dataSourceName));
        assertArrayEquals(cachedConnections.get(dataSourceName).toArray(), connections.toArray());
    }
    
    @SuppressWarnings("unchecked")
    @SneakyThrows(ReflectiveOperationException.class)
    private Connection prepareCachedConnections() {
        Field field = JDBCBackendConnection.class.getDeclaredField("cachedConnections");
        field.setAccessible(true);
        Multimap<String, Connection> cachedConnections = (Multimap<String, Connection>) field.get(backendConnection);
        Connection connection = mock(Connection.class);
        cachedConnections.put("ignoredDataSourceName", connection);
        return connection;
    }
    
    @SuppressWarnings("unchecked")
    @SneakyThrows(ReflectiveOperationException.class)
    private void verifyConnectionPostProcessorsEmpty() {
        Field field = JDBCBackendConnection.class.getDeclaredField("connectionPostProcessors");
        field.setAccessible(true);
        Collection<ConnectionPostProcessor<?>> connectionPostProcessors = (Collection<ConnectionPostProcessor<?>>) field.get(backendConnection);
        assertTrue(connectionPostProcessors.isEmpty());
    }
    
    @Test
    public void assertAddDatabaseCommunicationEngine() {
        ProxyBackendHandler expectedEngine = mock(JDBCDatabaseCommunicationEngine.class);
        backendConnection.add(expectedEngine);
        Collection<ProxyBackendHandler> actual = getDatabaseCommunicationEngines();
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next(), is(expectedEngine));
    }
    
    @Test
    public void assertMarkDatabaseCommunicationEngineInUse() {
        ProxyBackendHandler expectedEngine = mock(JDBCDatabaseCommunicationEngine.class);
        backendConnection.add(expectedEngine);
        backendConnection.markResourceInUse(expectedEngine);
        Collection<ProxyBackendHandler> actual = getInUseDatabaseCommunicationEngines();
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next(), is(expectedEngine));
    }
    
    @Test
    public void assertUnmarkInUseDatabaseCommunicationEngine() {
        ProxyBackendHandler engine = mock(JDBCDatabaseCommunicationEngine.class);
        Collection<ProxyBackendHandler> actual = getInUseDatabaseCommunicationEngines();
        actual.add(engine);
        backendConnection.unmarkResourceInUse(engine);
        assertTrue(actual.isEmpty());
    }
    
    @Test
    public void assertCloseHandlers() throws SQLException {
        ProxyBackendHandler engine = mock(JDBCDatabaseCommunicationEngine.class);
        ProxyBackendHandler inUseEngine = mock(JDBCDatabaseCommunicationEngine.class);
        SQLException expectedException = mock(SQLException.class);
        doThrow(expectedException).when(engine).close();
        Collection<ProxyBackendHandler> databaseCommunicationEngines = getDatabaseCommunicationEngines();
        Collection<ProxyBackendHandler> inUseDatabaseCommunicationEngines = getInUseDatabaseCommunicationEngines();
        databaseCommunicationEngines.add(engine);
        databaseCommunicationEngines.add(inUseEngine);
        inUseDatabaseCommunicationEngines.add(inUseEngine);
        Collection<SQLException> actual = backendConnection.closeHandlers(false);
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next(), is(expectedException));
        assertThat(inUseDatabaseCommunicationEngines.size(), is(1));
        assertThat(databaseCommunicationEngines.size(), is(1));
        verify(engine).close();
        backendConnection.closeHandlers(true);
        verify(inUseEngine).close();
        assertTrue(databaseCommunicationEngines.isEmpty());
        assertTrue(inUseDatabaseCommunicationEngines.isEmpty());
    }
    
    @SuppressWarnings("unchecked")
    @SneakyThrows(ReflectiveOperationException.class)
    private Collection<ProxyBackendHandler> getDatabaseCommunicationEngines() {
        Field field = JDBCBackendConnection.class.getDeclaredField("backendHandlers");
        field.setAccessible(true);
        return (Collection<ProxyBackendHandler>) field.get(backendConnection);
    }
    
    @SuppressWarnings("unchecked")
    @SneakyThrows(ReflectiveOperationException.class)
    private Collection<ProxyBackendHandler> getInUseDatabaseCommunicationEngines() {
        Field field = JDBCBackendConnection.class.getDeclaredField("inUseBackendHandlers");
        field.setAccessible(true);
        return (Collection<ProxyBackendHandler>) field.get(backendConnection);
    }
    
    @Test
    public void assertPrepareForTaskExecution() {
        backendConnection.prepareForTaskExecution();
    }
    
    @Test
    public void assertCloseExecutionResources() throws BackendConnectionException {
        backendConnection.closeExecutionResources();
        verify(backendConnection).closeHandlers(false);
        verify(backendConnection).closeHandlers(true);
        verify(backendConnection).closeConnections(false);
    }
    
    @Test
    public void assertCloseAllResources() {
        backendConnection.closeAllResources();
        verify(backendConnection).closeHandlers(true);
        verify(backendConnection).closeConnections(true);
    }
    
    @Test
    public void assertCloseConnectionsAndResetVariables() throws SQLException {
        connectionSession.getRequiredSessionVariableRecorder().setVariable("key", "default");
        Connection connection = mock(Connection.class, RETURNS_DEEP_STUBS);
        when(connection.getMetaData().getDatabaseProductName()).thenReturn("PostgreSQL");
        backendConnection.getCachedConnections().put("", connection);
        backendConnection.closeConnections(false);
        verify(connection.createStatement()).execute("RESET ALL");
        assertTrue(connectionSession.getRequiredSessionVariableRecorder().isEmpty());
    }
    
    @Test
    public void assertCloseConnectionsAndFailedToGetDatabaseType() throws SQLException {
        connectionSession.getRequiredSessionVariableRecorder().setVariable("key", "default");
        Connection connection = mock(Connection.class, RETURNS_DEEP_STUBS);
        SQLException expectedException = new SQLException();
        when(connection.getMetaData().getDatabaseProductName()).thenThrow(expectedException);
        backendConnection.getCachedConnections().put("", connection);
        Collection<SQLException> actualExceptions = backendConnection.closeConnections(false);
        assertThat(actualExceptions, is(Collections.singletonList(expectedException)));
    }
    
    @Test
    public void assertCloseConnectionsAndFailedToResetVariables() throws SQLException {
        connectionSession.getRequiredSessionVariableRecorder().setVariable("key", "default");
        Connection connection = mock(Connection.class, RETURNS_DEEP_STUBS);
        when(connection.getMetaData().getDatabaseProductName()).thenReturn("PostgreSQL");
        SQLException expectedException = new SQLException();
        when(connection.createStatement()).thenThrow(expectedException);
        backendConnection.getCachedConnections().put("", connection);
        Collection<SQLException> actualExceptions = backendConnection.closeConnections(false);
        assertThat(actualExceptions, is(Collections.singletonList(expectedException)));
    }
}
