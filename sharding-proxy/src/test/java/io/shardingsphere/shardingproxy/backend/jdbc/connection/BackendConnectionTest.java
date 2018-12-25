/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.shardingproxy.backend.jdbc.connection;

import io.shardingsphere.core.constant.ConnectionMode;
import io.shardingsphere.core.exception.ShardingException;
import io.shardingsphere.shardingproxy.backend.MockGlobalRegistryUtil;
import io.shardingsphere.shardingproxy.backend.jdbc.datasource.JDBCBackendDataSource;
import io.shardingsphere.transaction.api.TransactionType;
import io.shardingsphere.transaction.core.TransactionOperationType;
import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class BackendConnectionTest {
    
    @Mock
    private JDBCBackendDataSource backendDataSource;
    
    private BackendConnection backendConnection = new BackendConnection(TransactionType.LOCAL);
    
    @Before
    @SneakyThrows
    public void setUp() {
        MockGlobalRegistryUtil.setLogicSchemas("schema", 2);
        backendConnection.setCurrentSchema("schema_0");
        when(backendConnection.getLogicSchema().getBackendDataSource()).thenReturn(backendDataSource);
    }
    
    @Test
    public void assertGetConnectionCacheIsEmpty() throws SQLException {
        backendConnection.getStateHandler().getAndSetStatus(ConnectionStatus.TRANSACTION);
        when(backendDataSource.getConnections((ConnectionMode) any(), anyString(), eq(2))).thenReturn(MockConnectionUtil.mockNewConnections(2));
        List<Connection> actualConnections = backendConnection.getConnections(ConnectionMode.MEMORY_STRICTLY, "ds1", 2);
        assertThat(actualConnections.size(), is(2));
        assertThat(backendConnection.getConnectionSize(), is(2));
        assertThat(backendConnection.getStateHandler().getStatus(), is(ConnectionStatus.TRANSACTION));
    }
    
    @Test
    public void assertGetConnectionSizeLessThanCache() throws SQLException {
        backendConnection.getStateHandler().getAndSetStatus(ConnectionStatus.TRANSACTION);
        MockConnectionUtil.setCachedConnections(backendConnection, "ds1", 10);
        List<Connection> actualConnections = backendConnection.getConnections(ConnectionMode.MEMORY_STRICTLY, "ds1", 2);
        assertThat(actualConnections.size(), is(2));
        assertThat(backendConnection.getConnectionSize(), is(10));
        assertThat(backendConnection.getStateHandler().getStatus(), is(ConnectionStatus.TRANSACTION));
    }
    
    @Test
    public void assertGetConnectionSizeGreaterThanCache() throws SQLException {
        backendConnection.getStateHandler().getAndSetStatus(ConnectionStatus.TRANSACTION);
        MockConnectionUtil.setCachedConnections(backendConnection, "ds1", 10);
        when(backendDataSource.getConnections((ConnectionMode) any(), anyString(), eq(2))).thenReturn(MockConnectionUtil.mockNewConnections(2));
        List<Connection> actualConnections = backendConnection.getConnections(ConnectionMode.MEMORY_STRICTLY, "ds1", 12);
        assertThat(actualConnections.size(), is(12));
        assertThat(backendConnection.getConnectionSize(), is(12));
        assertThat(backendConnection.getStateHandler().getStatus(), is(ConnectionStatus.TRANSACTION));
    }
    
    @Test
    public void assertGetConnectionWithMethodInvocation() throws SQLException {
        backendConnection.getStateHandler().getAndSetStatus(ConnectionStatus.TRANSACTION);
        when(backendDataSource.getConnections((ConnectionMode) any(), anyString(), eq(2))).thenReturn(MockConnectionUtil.mockNewConnections(2));
        setMethodInvocation();
        List<Connection> actualConnections = backendConnection.getConnections(ConnectionMode.MEMORY_STRICTLY, "ds1", 2);
        verify(backendConnection.getMethodInvocations().iterator().next(), times(2)).invoke(any());
        assertThat(actualConnections.size(), is(2));
        assertThat(backendConnection.getStateHandler().getStatus(), is(ConnectionStatus.TRANSACTION));
    }
    
    @SneakyThrows
    private void setMethodInvocation() {
        MethodInvocation invocation = mock(MethodInvocation.class);
        Collection<MethodInvocation> methodInvocations = new ArrayList<>();
        methodInvocations.add(invocation);
        Field field = backendConnection.getClass().getDeclaredField("methodInvocations");
        field.setAccessible(true);
        field.set(backendConnection, methodInvocations);
    }
    
    @Test
    @SneakyThrows
    public void assertMultiThreadGetConnection() {
        MockConnectionUtil.setCachedConnections(backendConnection, "ds1", 10);
        when(backendDataSource.getConnections((ConnectionMode) any(), anyString(), eq(2))).thenReturn(MockConnectionUtil.mockNewConnections(2));
        Thread thread1 = new Thread(new Runnable() {
            @Override
            public void run() {
                assertOneThreadResult();
            }
        });
        Thread thread2 = new Thread(new Runnable() {
            @Override
            public void run() {
                assertOneThreadResult();
            }
        });
        thread1.start();
        thread2.start();
        thread1.join();
        thread2.join();
    }
    
    @SneakyThrows
    private void assertOneThreadResult() {
        backendConnection.getStateHandler().getAndSetStatus(ConnectionStatus.TRANSACTION);
        List<Connection> actualConnections = backendConnection.getConnections(ConnectionMode.MEMORY_STRICTLY, "ds1", 12);
        assertThat(actualConnections.size(), is(12));
        assertThat(backendConnection.getConnectionSize(), is(12));
        assertThat(backendConnection.getStateHandler().getStatus(), is(ConnectionStatus.TRANSACTION));
    }
    
    @Test
    public void assertAutoCloseConnectionWithoutTransaction() throws SQLException {
        BackendConnection actual;
        try (BackendConnection backendConnection = new BackendConnection(TransactionType.LOCAL)) {
            backendConnection.setCurrentSchema("schema_0");
            when(backendDataSource.getConnections((ConnectionMode) any(), anyString(), eq(12))).thenReturn(MockConnectionUtil.mockNewConnections(12));
            backendConnection.getConnections(ConnectionMode.MEMORY_STRICTLY, "ds1", 12);
            assertThat(backendConnection.getStateHandler().getStatus(), is(ConnectionStatus.RUNNING));
            mockResultSetAndStatement(backendConnection);
            actual = backendConnection;
        }
        assertThat(actual.getConnectionSize(), is(0));
        assertTrue(actual.getCachedConnections().isEmpty());
        assertTrue(actual.getCachedResultSets().isEmpty());
        assertTrue(actual.getCachedStatements().isEmpty());
        assertThat(actual.getStateHandler().getStatus(), is(ConnectionStatus.RELEASE));
    }
    
    @Test
    public void assertAutoCloseConnectionWithTransaction() throws SQLException {
        BackendConnection actual;
        try (BackendConnection backendConnection = new BackendConnection(TransactionType.LOCAL)) {
            backendConnection.setCurrentSchema("schema_0");
            MockConnectionUtil.setCachedConnections(backendConnection, "ds1", 10);
            when(backendDataSource.getConnections((ConnectionMode) any(), anyString(), eq(2))).thenReturn(MockConnectionUtil.mockNewConnections(2));
            backendConnection.getStateHandler().getAndSetStatus(ConnectionStatus.TRANSACTION);
            backendConnection.getConnections(ConnectionMode.MEMORY_STRICTLY, "ds1", 12);
            mockResultSetAndStatement(backendConnection);
            actual = backendConnection;
        }
        assertThat(actual.getConnectionSize(), is(12));
        assertThat(actual.getCachedConnections().get("ds1").size(), is(12));
        assertTrue(actual.getCachedResultSets().isEmpty());
        assertTrue(actual.getCachedStatements().isEmpty());
    }
    
    @Test
    public void assertAutoCloseConnectionWithException() {
        BackendConnection actual = null;
        try (BackendConnection backendConnection = new BackendConnection(TransactionType.LOCAL)) {
            backendConnection.setCurrentSchema("schema_0");
            backendConnection.setTransactionType(TransactionType.XA);
            backendConnection.getStateHandler().getAndSetStatus(ConnectionStatus.TRANSACTION);
            MockConnectionUtil.setCachedConnections(backendConnection, "ds1", 10);
            when(backendDataSource.getConnections((ConnectionMode) any(), anyString(), eq(2), eq(TransactionType.XA))).thenReturn(MockConnectionUtil.mockNewConnections(2));
            backendConnection.getConnections(ConnectionMode.MEMORY_STRICTLY, "ds1", 12);
            backendConnection.getStateHandler().getAndSetStatus(ConnectionStatus.TERMINATED);
            mockResultSetAndStatement(backendConnection);
            mockResultSetAndStatementException(backendConnection);
            actual = backendConnection;
        } catch (final SQLException ex) {
            assertThat(ex.getNextException().getNextException(), instanceOf(SQLException.class));
        }
        assert actual != null;
        assertThat(actual.getConnectionSize(), is(0));
        assertTrue(actual.getCachedConnections().isEmpty());
        assertTrue(actual.getCachedResultSets().isEmpty());
        assertTrue(actual.getCachedStatements().isEmpty());
    }
    
    private void mockResultSetAndStatement(final BackendConnection backendConnection) {
        ResultSet resultSet = mock(ResultSet.class);
        Statement statement = mock(Statement.class);
        backendConnection.add(resultSet);
        backendConnection.add(statement);
    }
    
    private void mockResultSetAndStatementException(final BackendConnection backendConnection) throws SQLException {
        for (Statement each : backendConnection.getCachedStatements()) {
            doThrow(SQLException.class).when(each).close();
        }
        for (ResultSet each : backendConnection.getCachedResultSets()) {
            doThrow(SQLException.class).when(each).close();
        }
    }
    
    @Test(expected = ShardingException.class)
    public void assertFailedSwitchTransactionTypeWhileBegin() throws SQLException {
        BackendTransactionManager transactionManager = new BackendTransactionManager(backendConnection);
        transactionManager.doInTransaction(TransactionOperationType.BEGIN);
        backendConnection.setTransactionType(TransactionType.XA);
    }
    
    @Test(expected = ShardingException.class)
    public void assertFailedSwitchLogicSchemaWhileBegin() throws SQLException {
        BackendTransactionManager transactionManager = new BackendTransactionManager(backendConnection);
        transactionManager.doInTransaction(TransactionOperationType.BEGIN);
        backendConnection.setCurrentSchema("newSchema");
    }
}
