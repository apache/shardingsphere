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
import io.shardingsphere.core.constant.transaction.TransactionOperationType;
import io.shardingsphere.core.constant.transaction.TransactionType;
import io.shardingsphere.shardingproxy.backend.jdbc.datasource.JDBCBackendDataSource;
import io.shardingsphere.shardingproxy.runtime.schema.LogicSchema;
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
import static org.junit.Assert.assertSame;
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
public class BackendConnectionTest {
    
    @Mock
    private LogicSchema logicSchema;
    
    @Mock
    private JDBCBackendDataSource backendDataSource;
    
    private BackendConnection backendConnection = new BackendConnection(TransactionType.LOCAL);
    
    @Before
    @SuppressWarnings("unchecked")
    @SneakyThrows
    public void setup() {
        when(logicSchema.getBackendDataSource()).thenReturn(backendDataSource);
        backendConnection.setLogicSchema(logicSchema);
    }
    
    @Test
    public void assertGetConnectionCacheIsEmpty() throws SQLException {
        when(backendDataSource.getConnections((ConnectionMode) any(), anyString(), eq(2))).thenReturn(MockConnectionUtil.mockNewConnections(2));
        List<Connection> actualConnections = backendConnection.getConnections(ConnectionMode.MEMORY_STRICTLY, "ds1", 2);
        assertThat(actualConnections.size(), is(2));
        assertThat(backendConnection.getConnectionSize(), is(2));
        assertThat(backendConnection.getStatus(), is(ConnectionStatus.RUNNING));
    }
    
    @Test
    public void assertGetConnectionSizeLessThanCache() throws SQLException {
        MockConnectionUtil.setCachedConnections(backendConnection, "ds1", 10);
        List<Connection> actualConnections = backendConnection.getConnections(ConnectionMode.MEMORY_STRICTLY, "ds1", 2);
        assertThat(actualConnections.size(), is(2));
        assertThat(backendConnection.getConnectionSize(), is(10));
        assertThat(backendConnection.getStatus(), is(ConnectionStatus.RUNNING));
    }
    
    @Test
    public void assertGetConnectionSizeGreaterThanCache() throws SQLException {
        MockConnectionUtil.setCachedConnections(backendConnection, "ds1", 10);
        when(backendDataSource.getConnections((ConnectionMode) any(), anyString(), eq(2))).thenReturn(MockConnectionUtil.mockNewConnections(2));
        List<Connection> actualConnections = backendConnection.getConnections(ConnectionMode.MEMORY_STRICTLY, "ds1", 12);
        assertThat(actualConnections.size(), is(12));
        assertThat(backendConnection.getConnectionSize(), is(12));
        assertThat(backendConnection.getStatus(), is(ConnectionStatus.RUNNING));
    }
    
    @Test
    public void assertGetConnectionWithMethodInvocation() throws SQLException {
        when(backendDataSource.getConnections((ConnectionMode) any(), anyString(), eq(2))).thenReturn(MockConnectionUtil.mockNewConnections(2));
        setMethodInvocation();
        List<Connection> actualConnections = backendConnection.getConnections(ConnectionMode.MEMORY_STRICTLY, "ds1", 2);
        verify(backendConnection.getMethodInvocations().iterator().next(), times(2)).invoke(any());
        assertThat(actualConnections.size(), is(2));
        assertThat(backendConnection.getStatus(), is(ConnectionStatus.RUNNING));
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
        List<Connection> actualConnections = backendConnection.getConnections(ConnectionMode.MEMORY_STRICTLY, "ds1", 12);
        assertThat(actualConnections.size(), is(12));
        assertThat(backendConnection.getConnectionSize(), is(12));
        assertThat(backendConnection.getStatus(), is(ConnectionStatus.RUNNING));
    }
    
    @Test
    public void assertAutoCloseConnectionWithException() {
        BackendConnection actual = null;
        try (BackendConnection backendConnection = new BackendConnection(TransactionType.LOCAL)) {
            backendConnection.setLogicSchema(logicSchema);
            backendConnection.setTransactionType(TransactionType.XA);
            MockConnectionUtil.setCachedConnections(backendConnection, "ds1", 10);
            when(backendDataSource.getConnections((ConnectionMode) any(), anyString(), eq(2))).thenReturn(MockConnectionUtil.mockNewConnections(2));
            MockConnectionUtil.mockThrowException(backendConnection.getCachedConnections().values());
            backendConnection.getConnections(ConnectionMode.MEMORY_STRICTLY, "ds1", 12);
            backendConnection.setStatus(ConnectionStatus.TERMINATED);
            mockResultSetAndStatement(backendConnection);
            actual = backendConnection;
        } catch (SQLException ex) {
            assertThat(ex.getNextException().getNextException(), instanceOf(SQLException.class));
        }
        assert actual != null;
        assertThat(actual.getConnectionSize(), is(0));
        assertTrue(actual.getCachedConnections().isEmpty());
        assertTrue(actual.getCachedResultSets().isEmpty());
        assertTrue(actual.getCachedStatements().isEmpty());
    }
    
    private void mockResultSetAndStatement(final BackendConnection backendConnection) throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        Statement statement = mock(Statement.class);
        doThrow(SQLException.class).when(resultSet).close();
        doThrow(SQLException.class).when(statement).close();
        backendConnection.add(resultSet);
        backendConnection.add(statement);
    }
    
    @Test
    public void assertFailedSwitchTransactionTypeWhileBegin() throws SQLException {
        BackendTransactionManager transactionManager = new BackendTransactionManager(backendConnection);
        transactionManager.doInTransaction(TransactionOperationType.BEGIN);
        backendConnection.setTransactionType(TransactionType.XA);
        assertSame(TransactionType.LOCAL, backendConnection.getTransactionType());
    }
    
    @Test
    public void assertFailedSwitchLogicSchemaWhileBegin() throws SQLException {
        BackendTransactionManager transactionManager = new BackendTransactionManager(backendConnection);
        transactionManager.doInTransaction(TransactionOperationType.BEGIN);
        backendConnection.setLogicSchema(mock(LogicSchema.class));
        assertSame(logicSchema, backendConnection.getLogicSchema());
    }
    
    @Test
    public void assertCancelStatement() throws SQLException {
        mockStatementCancel(backendConnection);
        backendConnection.cancel();
        assertTrue(backendConnection.getCachedStatements().isEmpty());
    }
    
    private void mockStatementCancel(final BackendConnection backendConnection) throws SQLException {
        Statement statement = mock(Statement.class);
        doThrow(SQLException.class).when(statement).cancel();
        backendConnection.add(statement);
    }
}
