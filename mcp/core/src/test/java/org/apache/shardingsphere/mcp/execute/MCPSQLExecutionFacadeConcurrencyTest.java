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

package org.apache.shardingsphere.mcp.execute;

import org.apache.shardingsphere.mcp.context.MCPRuntimeContext;
import org.apache.shardingsphere.mcp.metadata.jdbc.RuntimeDatabaseConfiguration;
import org.apache.shardingsphere.mcp.metadata.model.MCPDatabaseMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPDatabaseMetadataCatalog;
import org.apache.shardingsphere.mcp.protocol.exception.MCPTransactionStateException;
import org.apache.shardingsphere.mcp.protocol.response.ExecuteQueryResponse;
import org.apache.shardingsphere.mcp.session.MCPSessionExecutionCoordinator;
import org.apache.shardingsphere.mcp.session.MCPSessionManager;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MCPSQLExecutionFacadeConcurrencyTest {
    
    @Test
    void assertExecuteSerializesSameSessionTransactionCommand() throws InterruptedException, ExecutionException {
        CountDownLatch firstInvocationStarted = new CountDownLatch(1);
        CountDownLatch releaseFirstInvocation = new CountDownLatch(1);
        AtomicInteger currentExecutions = new AtomicInteger();
        AtomicInteger maxExecutions = new AtomicInteger();
        AtomicInteger invocationCount = new AtomicInteger();
        MCPJdbcTransactionResourceManager transactionResourceManager = mock(MCPJdbcTransactionResourceManager.class);
        when(transactionResourceManager.getRuntimeDatabases()).thenReturn(Collections.emptyMap());
        doAnswer(createBlockingBeginAnswer(firstInvocationStarted, releaseFirstInvocation, currentExecutions, maxExecutions, invocationCount))
                .when(transactionResourceManager).beginTransaction(anyString(), anyString());
        MCPSessionManager sessionManager = new MCPSessionManager(transactionResourceManager);
        sessionManager.createSession("session-1");
        MCPSQLExecutionFacade facade = createFacade(sessionManager);
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        try {
            Future<ExecuteQueryResponse> firstFuture = executorService.submit(() -> facade.execute(createExecutionRequest("session-1", "BEGIN")));
            assertTrue(firstInvocationStarted.await(1, TimeUnit.SECONDS));
            assertFalse(firstFuture.isDone());
            CountDownLatch secondCompleted = new CountDownLatch(1);
            Future<ExecuteQueryResponse> secondFuture = executorService.submit(() -> {
                try {
                    return facade.execute(createExecutionRequest("session-1", "BEGIN"));
                } finally {
                    secondCompleted.countDown();
                }
            });
            assertFalse(secondFuture.isDone());
            assertFalse(secondCompleted.await(200, TimeUnit.MILLISECONDS));
            releaseFirstInvocation.countDown();
            assertThat(firstFuture.get().getMessage(), is("Transaction started."));
            ExecutionException actual = assertThrows(ExecutionException.class, secondFuture::get);
            assertThat(actual.getCause(), isA(MCPTransactionStateException.class));
            assertThat(actual.getCause().getMessage(), is("Transaction already active."));
            assertThat(maxExecutions.get(), is(1));
        } finally {
            executorService.shutdownNow();
        }
    }
    
    @Test
    void assertExecuteSerializesSameSessionQuery() throws InterruptedException, ExecutionException, SQLException {
        CountDownLatch firstInvocationStarted = new CountDownLatch(1);
        CountDownLatch releaseFirstInvocation = new CountDownLatch(1);
        AtomicInteger currentExecutions = new AtomicInteger();
        AtomicInteger maxExecutions = new AtomicInteger();
        AtomicInteger invocationCount = new AtomicInteger();
        RuntimeDatabaseConfiguration runtimeDatabaseConfig = mock(RuntimeDatabaseConfiguration.class);
        Connection firstConnection = createBlockingQueryConnection(firstInvocationStarted, releaseFirstInvocation, currentExecutions, maxExecutions, invocationCount);
        Connection secondConnection = createBlockingQueryConnection(firstInvocationStarted, releaseFirstInvocation, currentExecutions, maxExecutions, invocationCount);
        when(runtimeDatabaseConfig.openConnection("logic_db")).thenReturn(firstConnection, secondConnection);
        MCPJdbcTransactionResourceManager transactionResourceManager = mock(MCPJdbcTransactionResourceManager.class);
        when(transactionResourceManager.getRuntimeDatabases()).thenReturn(Map.of("logic_db", runtimeDatabaseConfig));
        when(transactionResourceManager.findTransactionConnection(anyString(), eq("logic_db"))).thenReturn(Optional.empty());
        MCPSessionManager sessionManager = new MCPSessionManager(transactionResourceManager);
        sessionManager.createSession("session-1");
        MCPSQLExecutionFacade facade = createFacade(sessionManager);
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        try {
            Future<ExecuteQueryResponse> firstFuture = executorService.submit(() -> facade.execute(createExecutionRequest("session-1", "SELECT * FROM orders")));
            assertTrue(firstInvocationStarted.await(1, TimeUnit.SECONDS));
            assertFalse(firstFuture.isDone());
            CountDownLatch secondCompleted = new CountDownLatch(1);
            Future<ExecuteQueryResponse> secondFuture = executorService.submit(() -> {
                try {
                    return facade.execute(createExecutionRequest("session-1", "SELECT * FROM orders"));
                } finally {
                    secondCompleted.countDown();
                }
            });
            assertFalse(secondFuture.isDone());
            assertFalse(secondCompleted.await(200, TimeUnit.MILLISECONDS));
            releaseFirstInvocation.countDown();
            assertThat(firstFuture.get().getRows().size(), is(1));
            assertThat(secondFuture.get().getRows().size(), is(1));
            assertThat(maxExecutions.get(), is(1));
        } finally {
            executorService.shutdownNow();
        }
    }
    
    @Test
    void assertExecutePreservesDifferentSessionConcurrency() throws InterruptedException, ExecutionException, SQLException {
        CountDownLatch concurrentEntries = new CountDownLatch(2);
        CountDownLatch releaseQueries = new CountDownLatch(1);
        AtomicInteger currentExecutions = new AtomicInteger();
        AtomicInteger maxExecutions = new AtomicInteger();
        RuntimeDatabaseConfiguration runtimeDatabaseConfig = mock(RuntimeDatabaseConfiguration.class);
        Connection firstConnection = createConcurrentQueryConnection(concurrentEntries, releaseQueries, currentExecutions, maxExecutions);
        Connection secondConnection = createConcurrentQueryConnection(concurrentEntries, releaseQueries, currentExecutions, maxExecutions);
        when(runtimeDatabaseConfig.openConnection("logic_db")).thenReturn(firstConnection, secondConnection);
        MCPJdbcTransactionResourceManager transactionResourceManager = mock(MCPJdbcTransactionResourceManager.class);
        when(transactionResourceManager.getRuntimeDatabases()).thenReturn(Map.of("logic_db", runtimeDatabaseConfig));
        when(transactionResourceManager.findTransactionConnection(anyString(), eq("logic_db"))).thenReturn(Optional.empty());
        MCPSessionManager sessionManager = new MCPSessionManager(transactionResourceManager);
        sessionManager.createSession("session-1");
        sessionManager.createSession("session-2");
        MCPSQLExecutionFacade facade = createFacade(sessionManager);
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        try {
            Future<ExecuteQueryResponse> firstFuture = executorService.submit(() -> facade.execute(createExecutionRequest("session-1", "SELECT * FROM orders")));
            Future<ExecuteQueryResponse> secondFuture = executorService.submit(() -> facade.execute(createExecutionRequest("session-2", "SELECT * FROM orders")));
            assertFalse(firstFuture.isDone());
            assertTrue(concurrentEntries.await(1, TimeUnit.SECONDS));
            assertFalse(secondFuture.isDone());
            releaseQueries.countDown();
            assertThat(firstFuture.get().getRows().size(), is(1));
            assertThat(secondFuture.get().getRows().size(), is(1));
            assertThat(maxExecutions.get(), is(2));
        } finally {
            executorService.shutdownNow();
        }
    }
    
    private MCPSQLExecutionFacade createFacade(final MCPSessionManager sessionManager) {
        return new MCPSQLExecutionFacade(new MCPRuntimeContext(sessionManager,
                new MCPSessionExecutionCoordinator(sessionManager), createMetadataCatalog()));
    }
    
    private MCPDatabaseMetadataCatalog createMetadataCatalog() {
        return new MCPDatabaseMetadataCatalog(Map.of("logic_db", new MCPDatabaseMetadata("logic_db", "H2", "", Collections.emptyList())));
    }
    
    private ExecutionRequest createExecutionRequest(final String sessionId, final String sql) {
        return new ExecutionRequest(sessionId, "logic_db", "public", sql, 10, 1000);
    }
    
    private Connection createBlockingQueryConnection(final CountDownLatch firstInvocationStarted, final CountDownLatch releaseFirstInvocation,
                                                     final AtomicInteger currentExecutions, final AtomicInteger maxExecutions,
                                                     final AtomicInteger invocationCount) throws SQLException {
        Connection result = mock(Connection.class);
        Statement statement = mock(Statement.class);
        ResultSet resultSet = createResultSet();
        when(result.createStatement()).thenReturn(statement);
        when(statement.execute(anyString())).thenAnswer(
                createBlockingQueryAnswer(firstInvocationStarted, releaseFirstInvocation, currentExecutions, maxExecutions, invocationCount));
        when(statement.getResultSet()).thenReturn(resultSet);
        return result;
    }
    
    private Connection createConcurrentQueryConnection(final CountDownLatch concurrentEntries, final CountDownLatch releaseQueries,
                                                       final AtomicInteger currentExecutions, final AtomicInteger maxExecutions) throws SQLException {
        Connection result = mock(Connection.class);
        Statement statement = mock(Statement.class);
        ResultSet resultSet = createResultSet();
        when(result.createStatement()).thenReturn(statement);
        when(statement.execute(anyString())).thenAnswer(createConcurrentQueryAnswer(concurrentEntries, releaseQueries, currentExecutions, maxExecutions));
        when(statement.getResultSet()).thenReturn(resultSet);
        return result;
    }
    
    private ResultSet createResultSet() throws SQLException {
        ResultSet result = mock(ResultSet.class);
        ResultSetMetaData resultSetMetaData = mock(ResultSetMetaData.class);
        AtomicInteger rowIndex = new AtomicInteger(-1);
        when(result.getMetaData()).thenReturn(resultSetMetaData);
        when(result.next()).thenAnswer(invocation -> rowIndex.incrementAndGet() < 1);
        when(result.getObject(anyInt())).thenReturn(1);
        when(resultSetMetaData.getColumnCount()).thenReturn(1);
        when(resultSetMetaData.getColumnLabel(1)).thenReturn("order_id");
        when(resultSetMetaData.getColumnTypeName(1)).thenReturn("INTEGER");
        when(resultSetMetaData.isNullable(1)).thenReturn(ResultSetMetaData.columnNoNulls);
        return result;
    }
    
    private org.mockito.stubbing.Answer<Object> createBlockingBeginAnswer(final CountDownLatch firstInvocationStarted, final CountDownLatch releaseFirstInvocation,
                                                                          final AtomicInteger currentExecutions, final AtomicInteger maxExecutions,
                                                                          final AtomicInteger invocationCount) {
        return invocation -> handleBlockingBegin(firstInvocationStarted, releaseFirstInvocation, currentExecutions, maxExecutions, invocationCount);
    }
    
    private Object handleBlockingBegin(final CountDownLatch firstInvocationStarted, final CountDownLatch releaseFirstInvocation, final AtomicInteger currentExecutions,
                                       final AtomicInteger maxExecutions, final AtomicInteger invocationCount) {
        int current = currentExecutions.incrementAndGet();
        maxExecutions.accumulateAndGet(current, Math::max);
        try {
            if (1 == invocationCount.incrementAndGet()) {
                firstInvocationStarted.countDown();
                awaitLatch(releaseFirstInvocation);
                return null;
            }
            throw new IllegalStateException("Transaction already active.");
        } finally {
            currentExecutions.decrementAndGet();
        }
    }
    
    private org.mockito.stubbing.Answer<Boolean> createBlockingQueryAnswer(final CountDownLatch firstInvocationStarted, final CountDownLatch releaseFirstInvocation,
                                                                           final AtomicInteger currentExecutions, final AtomicInteger maxExecutions,
                                                                           final AtomicInteger invocationCount) {
        return invocation -> handleBlockingQuery(firstInvocationStarted, releaseFirstInvocation, currentExecutions, maxExecutions, invocationCount);
    }
    
    private Boolean handleBlockingQuery(final CountDownLatch firstInvocationStarted, final CountDownLatch releaseFirstInvocation,
                                        final AtomicInteger currentExecutions, final AtomicInteger maxExecutions, final AtomicInteger invocationCount) {
        int current = currentExecutions.incrementAndGet();
        maxExecutions.accumulateAndGet(current, Math::max);
        try {
            if (1 == invocationCount.incrementAndGet()) {
                firstInvocationStarted.countDown();
                awaitLatch(releaseFirstInvocation);
            }
            return true;
        } finally {
            currentExecutions.decrementAndGet();
        }
    }
    
    private org.mockito.stubbing.Answer<Boolean> createConcurrentQueryAnswer(final CountDownLatch concurrentEntries, final CountDownLatch releaseQueries,
                                                                             final AtomicInteger currentExecutions, final AtomicInteger maxExecutions) {
        return invocation -> handleConcurrentQuery(concurrentEntries, releaseQueries, currentExecutions, maxExecutions);
    }
    
    private Boolean handleConcurrentQuery(final CountDownLatch concurrentEntries, final CountDownLatch releaseQueries,
                                          final AtomicInteger currentExecutions, final AtomicInteger maxExecutions) {
        int current = currentExecutions.incrementAndGet();
        maxExecutions.accumulateAndGet(current, Math::max);
        concurrentEntries.countDown();
        try {
            awaitLatch(releaseQueries);
            return true;
        } finally {
            currentExecutions.decrementAndGet();
        }
    }
    
    private void awaitLatch(final CountDownLatch latch) {
        try {
            latch.await(1, TimeUnit.SECONDS);
        } catch (final InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(ex);
        }
    }
}
