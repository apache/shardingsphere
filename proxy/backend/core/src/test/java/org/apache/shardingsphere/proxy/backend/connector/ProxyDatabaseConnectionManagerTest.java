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

import com.google.common.collect.Multimap;
import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.ConnectionMode;
import org.apache.shardingsphere.proxy.backend.connector.jdbc.connection.ConnectionPostProcessor;
import org.apache.shardingsphere.proxy.backend.connector.jdbc.datasource.JDBCBackendDataSource;
import org.apache.shardingsphere.proxy.backend.connector.jdbc.statement.JDBCBackendStatement;
import org.apache.shardingsphere.proxy.backend.connector.jdbc.transaction.BackendTransactionManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.exception.BackendConnectionException;
import org.apache.shardingsphere.proxy.backend.handler.ProxyBackendHandler;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.session.RequiredSessionVariableRecorder;
import org.apache.shardingsphere.proxy.backend.session.transaction.TransactionStatus;
import org.apache.shardingsphere.test.mock.AutoMockExtension;
import org.apache.shardingsphere.test.mock.StaticMockSettings;
import org.apache.shardingsphere.transaction.api.TransactionType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.internal.configuration.plugins.Plugins;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ProxyContext.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ProxyDatabaseConnectionManagerTest {
    
    private static final String SCHEMA_PATTERN = "schema_%s";
    
    @Mock
    private JDBCBackendDataSource backendDataSource;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ConnectionSession connectionSession;
    
    private ProxyDatabaseConnectionManager databaseConnectionManager;
    
    @BeforeEach
    void setUp() {
        when(ProxyContext.getInstance().getBackendDataSource()).thenReturn(backendDataSource);
        when(connectionSession.getDatabaseName()).thenReturn(String.format(SCHEMA_PATTERN, 0));
        databaseConnectionManager = new ProxyDatabaseConnectionManager(connectionSession);
        when(connectionSession.getDatabaseConnectionManager()).thenReturn(databaseConnectionManager);
        when(connectionSession.getTransactionStatus()).thenReturn(new TransactionStatus(TransactionType.LOCAL));
        JDBCBackendStatement backendStatement = new JDBCBackendStatement();
        when(connectionSession.getStatementManager()).thenReturn(backendStatement);
        when(connectionSession.getRequiredSessionVariableRecorder()).thenReturn(new RequiredSessionVariableRecorder());
    }
    
    @AfterEach
    void clean() throws ReflectiveOperationException {
        Field field = ProxyContext.getInstance().getClass().getDeclaredField("backendDataSource");
        Object datasource = field.getType().getDeclaredConstructor().newInstance();
        Plugins.getMemberAccessor().set(field, ProxyContext.getInstance(), datasource);
    }
    
    @Test
    void assertGetConnectionCacheIsEmpty() throws SQLException {
        connectionSession.getTransactionStatus().setInTransaction(true);
        when(backendDataSource.getConnections(anyString(), anyString(), eq(2), any())).thenReturn(MockConnectionUtils.mockNewConnections(2));
        List<Connection> actualConnections = databaseConnectionManager.getConnections("ds1", 0, 2, ConnectionMode.MEMORY_STRICTLY);
        assertThat(actualConnections.size(), is(2));
        assertThat(databaseConnectionManager.getConnectionSize(), is(2));
        assertTrue(connectionSession.getTransactionStatus().isInTransaction());
    }
    
    @Test
    void assertGetConnectionSizeLessThanCache() throws SQLException {
        connectionSession.getTransactionStatus().setInTransaction(true);
        MockConnectionUtils.setCachedConnections(databaseConnectionManager, "ds1", 10);
        List<Connection> actualConnections = databaseConnectionManager.getConnections("ds1", 0, 2, ConnectionMode.MEMORY_STRICTLY);
        assertThat(actualConnections.size(), is(2));
        assertThat(databaseConnectionManager.getConnectionSize(), is(10));
        assertTrue(connectionSession.getTransactionStatus().isInTransaction());
    }
    
    @Test
    void assertGetConnectionSizeGreaterThanCache() throws SQLException {
        connectionSession.getTransactionStatus().setInTransaction(true);
        MockConnectionUtils.setCachedConnections(databaseConnectionManager, "ds1", 10);
        when(backendDataSource.getConnections(anyString(), anyString(), eq(2), any())).thenReturn(MockConnectionUtils.mockNewConnections(2));
        List<Connection> actualConnections = databaseConnectionManager.getConnections("ds1", 0, 12, ConnectionMode.MEMORY_STRICTLY);
        assertThat(actualConnections.size(), is(12));
        assertThat(databaseConnectionManager.getConnectionSize(), is(12));
        assertTrue(connectionSession.getTransactionStatus().isInTransaction());
    }
    
    @Test
    void assertGetConnectionWithConnectionPostProcessors() throws SQLException {
        connectionSession.getTransactionStatus().setInTransaction(true);
        when(backendDataSource.getConnections(anyString(), anyString(), eq(2), any())).thenReturn(MockConnectionUtils.mockNewConnections(2));
        setConnectionPostProcessors();
        List<Connection> actualConnections = databaseConnectionManager.getConnections("ds1", 0, 2, ConnectionMode.MEMORY_STRICTLY);
        verify(databaseConnectionManager.getConnectionPostProcessors().iterator().next(), times(2)).process(any());
        assertThat(actualConnections.size(), is(2));
        assertTrue(connectionSession.getTransactionStatus().isInTransaction());
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private void setConnectionPostProcessors() {
        ConnectionPostProcessor connectionPostProcessor = mock(ConnectionPostProcessor.class);
        Collection<ConnectionPostProcessor> connectionPostProcessors = new LinkedList<>();
        connectionPostProcessors.add(connectionPostProcessor);
        Plugins.getMemberAccessor().set(ProxyDatabaseConnectionManager.class.getDeclaredField("connectionPostProcessors"), databaseConnectionManager, connectionPostProcessors);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    void assertCloseConnectionsCorrectlyWhenNotForceRollback() throws ReflectiveOperationException, SQLException {
        Multimap<String, Connection> cachedConnections = (Multimap<String, Connection>) Plugins.getMemberAccessor()
                .get(ProxyDatabaseConnectionManager.class.getDeclaredField("cachedConnections"), databaseConnectionManager);
        Connection connection = prepareCachedConnections();
        cachedConnections.put("ignoredDataSourceName", connection);
        databaseConnectionManager.closeConnections(false);
        verify(connection).close();
        assertTrue(cachedConnections.isEmpty());
        verifyConnectionPostProcessorsEmpty();
    }
    
    @SuppressWarnings("unchecked")
    @SneakyThrows(ReflectiveOperationException.class)
    private void verifyConnectionPostProcessorsEmpty() {
        Collection<ConnectionPostProcessor> connectionPostProcessors = (Collection<ConnectionPostProcessor>) Plugins.getMemberAccessor()
                .get(ProxyDatabaseConnectionManager.class.getDeclaredField("connectionPostProcessors"), databaseConnectionManager);
        assertTrue(connectionPostProcessors.isEmpty());
    }
    
    @Test
    void assertCloseConnectionsCorrectlyWhenForceRollbackAndNotInTransaction() throws SQLException {
        connectionSession.getTransactionStatus().setInTransaction(false);
        Connection connection = prepareCachedConnections();
        databaseConnectionManager.closeConnections(true);
        verify(connection, never()).rollback();
    }
    
    @Test
    void assertCloseConnectionsCorrectlyWhenForceRollbackAndInTransaction() throws SQLException {
        connectionSession.getTransactionStatus().setInTransaction(true);
        Connection connection = prepareCachedConnections();
        databaseConnectionManager.closeConnections(true);
        verify(connection).rollback();
    }
    
    @Test
    void assertCloseConnectionsCorrectlyWhenSQLExceptionThrown() throws SQLException {
        Connection connection = prepareCachedConnections();
        SQLException sqlException = new SQLException("");
        doThrow(sqlException).when(connection).close();
        assertTrue(databaseConnectionManager.closeConnections(false).contains(sqlException));
    }
    
    @Test
    void assertCreateStorageResourceCorrectlyWhenConnectionModeMemoryStrictly() throws SQLException {
        Connection connection = mock(Connection.class);
        Statement statement = mock(Statement.class);
        when(connection.createStatement()).thenReturn(statement);
        JDBCBackendStatement backendStatement = (JDBCBackendStatement) connectionSession.getStatementManager();
        assertThat(backendStatement.createStorageResource(connection, ConnectionMode.MEMORY_STRICTLY, null, connectionSession.getProtocolType()), is(statement));
        verify(connection).createStatement();
    }
    
    @Test
    void assertGetConnectionsAndReplaySessionVariables() throws SQLException {
        connectionSession.getRequiredSessionVariableRecorder().setVariable("key", "value");
        ProxyContext proxyContext = mock(ProxyContext.class, RETURNS_DEEP_STUBS);
        when(ProxyContext.getInstance()).thenReturn(proxyContext);
        Connection connection = mock(Connection.class, RETURNS_DEEP_STUBS);
        when(connection.getMetaData().getDatabaseProductName()).thenReturn("PostgreSQL");
        when(proxyContext.getBackendDataSource().getConnections(anyString(), anyString(), anyInt(), any(ConnectionMode.class))).thenReturn(Collections.singletonList(connection));
        List<Connection> actualConnections = databaseConnectionManager.getConnections("", 0, 1, ConnectionMode.CONNECTION_STRICTLY);
        Connection actualConnection = actualConnections.get(0);
        verify(actualConnection.createStatement()).execute("SET key=value");
    }
    
    @Test
    void assertGetConnectionsAndFailedToReplaySessionVariables() throws SQLException {
        connectionSession.getRequiredSessionVariableRecorder().setVariable("key", "value");
        Connection connection = null;
        SQLException expectedException = new SQLException();
        try {
            connection = mock(Connection.class, RETURNS_DEEP_STUBS);
            when(connection.getMetaData().getDatabaseProductName()).thenReturn("PostgreSQL");
            when(connection.createStatement().execute("SET key=value")).thenThrow(expectedException);
            when(ProxyContext.getInstance().getBackendDataSource().getConnections(anyString(), anyString(), anyInt(), any(ConnectionMode.class))).thenReturn(Collections.singletonList(connection));
            databaseConnectionManager.getConnections("", 0, 1, ConnectionMode.CONNECTION_STRICTLY);
        } catch (final SQLException ex) {
            assertThat(ex, is(expectedException));
            verify(connection).close();
        }
    }
    
    @Test
    void assertGetConnectionsWithoutTransactions() throws SQLException {
        connectionSession.getTransactionStatus().setInTransaction(false);
        List<Connection> connections = MockConnectionUtils.mockNewConnections(1);
        when(backendDataSource.getConnections(anyString(), anyString(), eq(1), any())).thenReturn(connections);
        List<Connection> fetchedConnections = databaseConnectionManager.getConnections("ds1", 0, 1, null);
        assertThat(fetchedConnections.size(), is(1));
        assertTrue(fetchedConnections.contains(connections.get(0)));
        assertConnectionsCached(connectionSession.getDatabaseName() + ".ds1", connections);
    }
    
    @SuppressWarnings("unchecked")
    @SneakyThrows(ReflectiveOperationException.class)
    private void assertConnectionsCached(final String dataSourceName, final Collection<Connection> connections) {
        Multimap<String, Connection> cachedConnections = (Multimap<String, Connection>) Plugins.getMemberAccessor()
                .get(ProxyDatabaseConnectionManager.class.getDeclaredField("cachedConnections"), databaseConnectionManager);
        assertTrue(cachedConnections.containsKey(dataSourceName));
        assertArrayEquals(cachedConnections.get(dataSourceName).toArray(), connections.toArray());
    }
    
    @Test
    void assertGetConnectionWithConnectionOffset() throws SQLException {
        when(backendDataSource.getConnections(anyString(), anyString(), eq(1), any())).thenReturn(MockConnectionUtils.mockNewConnections(1));
        assertThat(databaseConnectionManager.getConnections("ds1", 0, 1, ConnectionMode.MEMORY_STRICTLY),
                is(databaseConnectionManager.getConnections("ds1", 0, 1, ConnectionMode.MEMORY_STRICTLY)));
        when(backendDataSource.getConnections(anyString(), anyString(), eq(1), any())).thenReturn(MockConnectionUtils.mockNewConnections(1));
        assertThat(databaseConnectionManager.getConnections("ds1", 1, 1, ConnectionMode.MEMORY_STRICTLY),
                is(databaseConnectionManager.getConnections("ds1", 1, 1, ConnectionMode.MEMORY_STRICTLY)));
        when(backendDataSource.getConnections(anyString(), anyString(), eq(1), any())).thenReturn(MockConnectionUtils.mockNewConnections(1));
        assertThat(databaseConnectionManager.getConnections("ds1", 0, 1, ConnectionMode.MEMORY_STRICTLY),
                not(databaseConnectionManager.getConnections("ds1", 1, 1, ConnectionMode.MEMORY_STRICTLY)));
    }
    
    @Test
    void assertHandleAutoCommit() {
        when(connectionSession.isAutoCommit()).thenReturn(false);
        connectionSession.getTransactionStatus().setInTransaction(false);
        try (MockedConstruction<BackendTransactionManager> mockedConstruction = mockConstruction(BackendTransactionManager.class)) {
            databaseConnectionManager.handleAutoCommit();
            verify(mockedConstruction.constructed().get(0)).begin();
        }
    }
    
    @Test
    void assertAddDatabaseConnector() {
        ProxyBackendHandler expectedEngine = mock(DatabaseConnector.class);
        databaseConnectionManager.add(expectedEngine);
        Collection<ProxyBackendHandler> actual = getBackendHandlers();
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next(), is(expectedEngine));
    }
    
    @Test
    void assertMarkDatabaseConnectorInUse() {
        ProxyBackendHandler expectedEngine = mock(DatabaseConnector.class);
        databaseConnectionManager.add(expectedEngine);
        databaseConnectionManager.markResourceInUse(expectedEngine);
        Collection<ProxyBackendHandler> actual = getInUseBackendHandlers();
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next(), is(expectedEngine));
    }
    
    @Test
    void assertUnmarkInUseDatabaseConnector() {
        ProxyBackendHandler engine = mock(DatabaseConnector.class);
        Collection<ProxyBackendHandler> actual = getInUseBackendHandlers();
        actual.add(engine);
        databaseConnectionManager.unmarkResourceInUse(engine);
        assertTrue(actual.isEmpty());
    }
    
    @Test
    void assertCloseHandlers() throws SQLException {
        ProxyBackendHandler engine = mock(DatabaseConnector.class);
        ProxyBackendHandler inUseEngine = mock(DatabaseConnector.class);
        SQLException expectedException = mock(SQLException.class);
        doThrow(expectedException).when(engine).close();
        Collection<ProxyBackendHandler> databaseConnectors = getBackendHandlers();
        Collection<ProxyBackendHandler> inUseDatabaseConnectors = getInUseBackendHandlers();
        databaseConnectors.add(engine);
        databaseConnectors.add(inUseEngine);
        inUseDatabaseConnectors.add(inUseEngine);
        Collection<SQLException> actual = databaseConnectionManager.closeHandlers(false);
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next(), is(expectedException));
        assertThat(inUseDatabaseConnectors.size(), is(1));
        assertThat(databaseConnectors.size(), is(1));
        verify(engine).close();
        databaseConnectionManager.closeHandlers(true);
        verify(inUseEngine).close();
        assertTrue(databaseConnectors.isEmpty());
        assertTrue(inUseDatabaseConnectors.isEmpty());
    }
    
    @Test
    void assertCloseExecutionResourcesNotInTransaction() throws BackendConnectionException, SQLException {
        ProxyBackendHandler notInUseHandler = mock(ProxyBackendHandler.class);
        ProxyBackendHandler inUseHandler = mock(ProxyBackendHandler.class);
        getBackendHandlers().addAll(Arrays.asList(notInUseHandler, inUseHandler));
        getInUseBackendHandlers().add(inUseHandler);
        Connection cachedConnection = prepareCachedConnections();
        databaseConnectionManager.closeExecutionResources();
        verify(cachedConnection).close();
        assertTrue(getBackendHandlers().isEmpty());
        assertTrue(getInUseBackendHandlers().isEmpty());
        verify(notInUseHandler).close();
        verify(inUseHandler).close();
    }
    
    @Test
    void assertCloseExecutionResourcesInTransaction() throws BackendConnectionException {
        connectionSession.getTransactionStatus().setInTransaction(true);
        ProxyBackendHandler notInUseHandler = mock(ProxyBackendHandler.class);
        ProxyBackendHandler inUseHandler = mock(ProxyBackendHandler.class);
        getBackendHandlers().addAll(Arrays.asList(notInUseHandler, inUseHandler));
        getInUseBackendHandlers().add(inUseHandler);
        Connection cachedConnection = prepareCachedConnections();
        databaseConnectionManager.closeExecutionResources();
        verifyNoInteractions(inUseHandler, cachedConnection);
        assertThat(getBackendHandlers(), is(Collections.singleton(inUseHandler)));
        assertThat(getInUseBackendHandlers(), is(Collections.singleton(inUseHandler)));
    }
    
    @SuppressWarnings("unchecked")
    @SneakyThrows(ReflectiveOperationException.class)
    private Collection<ProxyBackendHandler> getBackendHandlers() {
        return (Collection<ProxyBackendHandler>) Plugins.getMemberAccessor().get(ProxyDatabaseConnectionManager.class.getDeclaredField("backendHandlers"), databaseConnectionManager);
    }
    
    @SuppressWarnings("unchecked")
    @SneakyThrows(ReflectiveOperationException.class)
    private Collection<ProxyBackendHandler> getInUseBackendHandlers() {
        return (Collection<ProxyBackendHandler>) Plugins.getMemberAccessor().get(ProxyDatabaseConnectionManager.class.getDeclaredField("inUseBackendHandlers"), databaseConnectionManager);
    }
    
    @Test
    void assertCloseAllResourcesInTransaction() throws SQLException {
        connectionSession.getTransactionStatus().setInTransaction(true);
        Connection cachedConnection = prepareCachedConnections();
        databaseConnectionManager.closeAllResources();
        assertTrue(databaseConnectionManager.getClosed().get());
        verify(cachedConnection).rollback();
    }
    
    @SuppressWarnings("unchecked")
    @SneakyThrows(ReflectiveOperationException.class)
    private Connection prepareCachedConnections() {
        Multimap<String, Connection> cachedConnections = (Multimap<String, Connection>) Plugins.getMemberAccessor()
                .get(ProxyDatabaseConnectionManager.class.getDeclaredField("cachedConnections"), databaseConnectionManager);
        Connection connection = mock(Connection.class);
        cachedConnections.put("ignoredDataSourceName", connection);
        return connection;
    }
    
    @Test
    void assertCloseConnectionsAndResetVariables() throws SQLException {
        connectionSession.getRequiredSessionVariableRecorder().setVariable("key", "default");
        Connection connection = mock(Connection.class, RETURNS_DEEP_STUBS);
        when(connection.getMetaData().getDatabaseProductName()).thenReturn("PostgreSQL");
        databaseConnectionManager.getCachedConnections().put("", connection);
        databaseConnectionManager.closeConnections(false);
        verify(connection.createStatement()).execute("RESET ALL");
        assertTrue(connectionSession.getRequiredSessionVariableRecorder().isEmpty());
    }
    
    @Test
    void assertCloseConnectionsAndFailedToGetDatabaseType() throws SQLException {
        connectionSession.getRequiredSessionVariableRecorder().setVariable("key", "default");
        Connection connection = mock(Connection.class, RETURNS_DEEP_STUBS);
        SQLException expectedException = new SQLException();
        when(connection.getMetaData().getDatabaseProductName()).thenThrow(expectedException);
        databaseConnectionManager.getCachedConnections().put("", connection);
        Collection<SQLException> actualExceptions = databaseConnectionManager.closeConnections(false);
        assertThat(actualExceptions, is(Collections.singletonList(expectedException)));
    }
    
    @Test
    void assertCloseConnectionsAndFailedToResetVariables() throws SQLException {
        connectionSession.getRequiredSessionVariableRecorder().setVariable("key", "default");
        Connection connection = mock(Connection.class, RETURNS_DEEP_STUBS);
        when(connection.getMetaData().getDatabaseProductName()).thenReturn("PostgreSQL");
        SQLException expectedException = new SQLException();
        when(connection.createStatement()).thenThrow(expectedException);
        databaseConnectionManager.getCachedConnections().put("", connection);
        Collection<SQLException> actualExceptions = databaseConnectionManager.closeConnections(false);
        assertThat(actualExceptions, is(Collections.singletonList(expectedException)));
    }
    
    @Test
    void assertGetDataSourceNamesOfCachedConnections() {
        databaseConnectionManager.getCachedConnections().put(connectionSession.getDatabaseName() + ".ds_0", null);
        databaseConnectionManager.getCachedConnections().put(connectionSession.getDatabaseName() + ".ds_1", null);
        databaseConnectionManager.getCachedConnections().put(connectionSession.getDatabaseName() + ".ds_2", null);
        List<String> actual = new ArrayList<>(databaseConnectionManager.getUsedDataSourceNames());
        Collections.sort(actual);
        assertThat(actual, is(Arrays.asList("ds_0", "ds_1", "ds_2")));
    }
}
