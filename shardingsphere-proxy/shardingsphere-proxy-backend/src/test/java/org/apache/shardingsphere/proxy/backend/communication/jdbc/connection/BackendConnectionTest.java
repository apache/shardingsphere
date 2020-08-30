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

import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.auth.Authentication;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.infra.executor.sql.ConnectionMode;
import org.apache.shardingsphere.kernel.context.SchemaContext;
import org.apache.shardingsphere.kernel.context.impl.StandardSchemaContexts;
import org.apache.shardingsphere.kernel.context.runtime.RuntimeContext;
import org.apache.shardingsphere.kernel.context.schema.ShardingSphereSchema;
import org.apache.shardingsphere.proxy.backend.schema.ProxySchemaContexts;
import org.apache.shardingsphere.proxy.backend.schema.ProxySchemaContexts.JDBCBackendDataSource;
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

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class BackendConnectionTest {
    
    @Mock
    private JDBCBackendDataSource backendDataSource;
    
    private final BackendConnection backendConnection = new BackendConnection(TransactionType.LOCAL);
    
    @Before
    public void setUp() {
        setSchemaContexts();
        setTransactionContexts();
        setBackendDataSource();
        backendConnection.setCurrentSchema("schema_0");
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private void setSchemaContexts() {
        Field field = ProxySchemaContexts.getInstance().getClass().getDeclaredField("schemaContexts");
        field.setAccessible(true);
        field.set(ProxySchemaContexts.getInstance(),
                new StandardSchemaContexts(createSchemaContextMap(), new Authentication(), new ConfigurationProperties(new Properties()), new MySQLDatabaseType()));
    }
    
    private Map<String, SchemaContext> createSchemaContextMap() {
        Map<String, SchemaContext> result = new HashMap<>(10);
        for (int i = 0; i < 10; i++) {
            String name = "schema_" + i;
            RuntimeContext runtimeContext = mock(RuntimeContext.class);
            SchemaContext schemaContext = new SchemaContext(name, mock(ShardingSphereSchema.class), runtimeContext);
            result.put(name, schemaContext);
        }
        return result;
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private void setTransactionContexts() {
        Field field = ProxySchemaContexts.getInstance().getClass().getDeclaredField("transactionContexts");
        field.setAccessible(true);
        field.set(ProxySchemaContexts.getInstance(), createTransactionContexts());
    }
    
    private TransactionContexts createTransactionContexts() {
        TransactionContexts result = mock(TransactionContexts.class, RETURNS_DEEP_STUBS);
        for (int i = 0; i < 10; i++) {
            String name = "schema_" + i;
            when(result.getEngines().get(name)).thenReturn(new ShardingTransactionManagerEngine());
        }
        return result;
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private void setBackendDataSource() {
        Field field = ProxySchemaContexts.getInstance().getClass().getDeclaredField("backendDataSource");
        field.setAccessible(true);
        field.set(ProxySchemaContexts.getInstance(), backendDataSource);
    }
    
    @Test
    public void assertGetConnectionCacheIsEmpty() throws SQLException {
        backendConnection.getStateHandler().setStatus(ConnectionStatus.TRANSACTION);
        when(backendDataSource.getConnections(anyString(), anyString(), eq(2), any())).thenReturn(MockConnectionUtil.mockNewConnections(2));
        List<Connection> actualConnections = backendConnection.getConnections("ds1", 2, ConnectionMode.MEMORY_STRICTLY);
        assertThat(actualConnections.size(), is(2));
        assertThat(backendConnection.getConnectionSize(), is(2));
        assertThat(backendConnection.getStateHandler().getStatus(), is(ConnectionStatus.TRANSACTION));
    }
    
    @Test
    public void assertGetConnectionSizeLessThanCache() throws SQLException {
        backendConnection.getStateHandler().setStatus(ConnectionStatus.TRANSACTION);
        MockConnectionUtil.setCachedConnections(backendConnection, "ds1", 10);
        List<Connection> actualConnections = backendConnection.getConnections("ds1", 2, ConnectionMode.MEMORY_STRICTLY);
        assertThat(actualConnections.size(), is(2));
        assertThat(backendConnection.getConnectionSize(), is(10));
        assertThat(backendConnection.getStateHandler().getStatus(), is(ConnectionStatus.TRANSACTION));
    }
    
    @Test
    public void assertGetConnectionSizeGreaterThanCache() throws SQLException {
        backendConnection.getStateHandler().setStatus(ConnectionStatus.TRANSACTION);
        MockConnectionUtil.setCachedConnections(backendConnection, "ds1", 10);
        when(backendDataSource.getConnections(anyString(), anyString(), eq(2), any())).thenReturn(MockConnectionUtil.mockNewConnections(2));
        List<Connection> actualConnections = backendConnection.getConnections("ds1", 12, ConnectionMode.MEMORY_STRICTLY);
        assertThat(actualConnections.size(), is(12));
        assertThat(backendConnection.getConnectionSize(), is(12));
        assertThat(backendConnection.getStateHandler().getStatus(), is(ConnectionStatus.TRANSACTION));
    }
    
    @Test
    public void assertGetConnectionWithMethodInvocation() throws SQLException {
        backendConnection.getStateHandler().setStatus(ConnectionStatus.TRANSACTION);
        when(backendDataSource.getConnections(anyString(), anyString(), eq(2), any())).thenReturn(MockConnectionUtil.mockNewConnections(2));
        setMethodInvocation();
        List<Connection> actualConnections = backendConnection.getConnections("ds1", 2, ConnectionMode.MEMORY_STRICTLY);
        verify(backendConnection.getMethodInvocations().iterator().next(), times(2)).invoke(any());
        assertThat(actualConnections.size(), is(2));
        assertThat(backendConnection.getStateHandler().getStatus(), is(ConnectionStatus.TRANSACTION));
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
    public void assertMultiThreadGetConnection() throws SQLException, InterruptedException {
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
        backendConnection.getStateHandler().setStatus(ConnectionStatus.TRANSACTION);
        List<Connection> actualConnections = backendConnection.getConnections("ds1", 12, ConnectionMode.MEMORY_STRICTLY);
        assertThat(actualConnections.size(), is(12));
        assertThat(backendConnection.getConnectionSize(), is(12));
        assertThat(backendConnection.getStateHandler().getStatus(), is(ConnectionStatus.TRANSACTION));
    }
    
    @Test
    public void assertAutoCloseConnectionWithoutTransaction() throws SQLException {
        BackendConnection actual;
        try (BackendConnection backendConnection = new BackendConnection(TransactionType.LOCAL)) {
            backendConnection.setCurrentSchema("schema_0");
            when(backendDataSource.getConnections(anyString(), anyString(), eq(12), any())).thenReturn(MockConnectionUtil.mockNewConnections(12));
            backendConnection.getConnections("ds1", 12, ConnectionMode.MEMORY_STRICTLY);
            assertThat(backendConnection.getStateHandler().getStatus(), is(ConnectionStatus.INIT));
            backendConnection.getStateHandler().setRunningStatusIfNecessary();
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
            when(backendDataSource.getConnections(anyString(), anyString(), eq(2), any())).thenReturn(MockConnectionUtil.mockNewConnections(2));
            backendConnection.getStateHandler().setStatus(ConnectionStatus.TRANSACTION);
            backendConnection.getConnections("ds1", 12, ConnectionMode.MEMORY_STRICTLY);
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
            backendConnection.getStateHandler().setStatus(ConnectionStatus.TRANSACTION);
            MockConnectionUtil.setCachedConnections(backendConnection, "ds1", 10);
            backendConnection.getConnections("ds1", 12, ConnectionMode.MEMORY_STRICTLY);
            backendConnection.getStateHandler().setStatus(ConnectionStatus.TERMINATED);
            mockResultSetAndStatement(backendConnection);
            mockResultSetAndStatementException(backendConnection);
            actual = backendConnection;
        } catch (final SQLException ex) {
            assertThat(ex.getNextException().getNextException(), instanceOf(SQLException.class));
        }
        assertNotNull(actual);
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
    
    @Test(expected = ShardingSphereException.class)
    public void assertFailedSwitchTransactionTypeWhileBegin() {
        BackendTransactionManager transactionManager = new BackendTransactionManager(backendConnection);
        transactionManager.begin();
        backendConnection.setTransactionType(TransactionType.XA);
    }
    
    @Test(expected = ShardingSphereException.class)
    public void assertFailedSwitchSchemaWhileBegin() {
        BackendTransactionManager transactionManager = new BackendTransactionManager(backendConnection);
        transactionManager.begin();
        backendConnection.setCurrentSchema("newSchema");
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    @After
    public void clean() {
        Field field = ProxySchemaContexts.getInstance().getClass().getDeclaredField("backendDataSource");
        field.setAccessible(true);
        Class<?> clazz = field.getType();
        Object datasource = clazz.getDeclaredConstructors()[0].newInstance(ProxySchemaContexts.getInstance());
        field.set(ProxySchemaContexts.getInstance(), datasource);
    }
}
