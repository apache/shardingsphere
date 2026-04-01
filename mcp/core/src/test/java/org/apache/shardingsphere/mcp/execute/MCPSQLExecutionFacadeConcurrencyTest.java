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

import org.apache.shardingsphere.mcp.capability.MCPCapabilityBuilder;
import org.apache.shardingsphere.mcp.metadata.MetadataRefreshCoordinator;
import org.apache.shardingsphere.mcp.protocol.ExecuteQueryColumnDefinition;
import org.apache.shardingsphere.mcp.protocol.ExecuteQueryResponse;
import org.apache.shardingsphere.mcp.protocol.MCPErrorCode;
import org.apache.shardingsphere.mcp.resource.DatabaseMetadataSnapshot;
import org.apache.shardingsphere.mcp.resource.DatabaseMetadataSnapshots;
import org.apache.shardingsphere.mcp.session.MCPSessionManager;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class MCPSQLExecutionFacadeConcurrencyTest {
    
    @Test
    void assertExecuteSerializesSameSessionTransactionCommand() throws InterruptedException, ExecutionException {
        MCPSessionManager sessionManager = new MCPSessionManager(mock(MCPJdbcTransactionResourceManager.class));
        sessionManager.createSession("session-1");
        MCPJdbcStatementExecutor statementExecutor = mock(MCPJdbcStatementExecutor.class);
        verifyNoInteractions(statementExecutor);
        CountDownLatch firstInvocationStarted = new CountDownLatch(1);
        CountDownLatch releaseFirstInvocation = new CountDownLatch(1);
        AtomicInteger currentExecutions = new AtomicInteger();
        AtomicInteger maxExecutions = new AtomicInteger();
        AtomicInteger invocationCount = new AtomicInteger();
        MCPJdbcTransactionResourceManager transactionResourceManager = mock(MCPJdbcTransactionResourceManager.class);
        doAnswer(createBlockingBeginAnswer(firstInvocationStarted, releaseFirstInvocation, currentExecutions, maxExecutions, invocationCount))
                .when(transactionResourceManager).beginTransaction(any(), any());
        MCPSQLExecutionFacade facade = createFacade(sessionManager, statementExecutor, transactionResourceManager);
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
            assertFalse(secondCompleted.await(200, TimeUnit.MILLISECONDS));
            releaseFirstInvocation.countDown();
            ExecuteQueryResponse firstActual = firstFuture.get();
            ExecuteQueryResponse secondActual = secondFuture.get();
            assertTrue(firstActual.isSuccessful());
            assertFalse(secondActual.isSuccessful());
            assertThat(secondActual.getError().orElseThrow().getErrorCode(), is(MCPErrorCode.TRANSACTION_STATE_ERROR));
            assertThat(maxExecutions.get(), is(1));
        } finally {
            executorService.shutdownNow();
        }
    }
    
    @Test
    void assertExecuteSerializesSameSessionQuery() throws InterruptedException, ExecutionException {
        MCPSessionManager sessionManager = new MCPSessionManager(mock(MCPJdbcTransactionResourceManager.class));
        sessionManager.createSession("session-1");
        CountDownLatch firstInvocationStarted = new CountDownLatch(1);
        CountDownLatch releaseFirstInvocation = new CountDownLatch(1);
        AtomicInteger currentExecutions = new AtomicInteger();
        AtomicInteger maxExecutions = new AtomicInteger();
        AtomicInteger invocationCount = new AtomicInteger();
        MCPJdbcStatementExecutor statementExecutor = mock(MCPJdbcStatementExecutor.class);
        when(statementExecutor.execute(any(ExecutionRequest.class), any(ClassificationResult.class)))
                .thenAnswer(createBlockingQueryAnswer(firstInvocationStarted, releaseFirstInvocation, currentExecutions, maxExecutions, invocationCount));
        MCPSQLExecutionFacade facade = createFacade(sessionManager, statementExecutor, mock(MCPJdbcTransactionResourceManager.class));
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
            assertTrue(firstFuture.get().isSuccessful());
            assertTrue(secondFuture.get().isSuccessful());
            assertThat(maxExecutions.get(), is(1));
        } finally {
            executorService.shutdownNow();
        }
    }
    
    @Test
    void assertExecutePreservesDifferentSessionConcurrency() throws InterruptedException, ExecutionException {
        MCPSessionManager sessionManager = new MCPSessionManager(mock(MCPJdbcTransactionResourceManager.class));
        sessionManager.createSession("session-1");
        sessionManager.createSession("session-2");
        CountDownLatch concurrentEntries = new CountDownLatch(2);
        CountDownLatch releaseQueries = new CountDownLatch(1);
        AtomicInteger currentExecutions = new AtomicInteger();
        AtomicInteger maxExecutions = new AtomicInteger();
        MCPJdbcStatementExecutor statementExecutor = mock(MCPJdbcStatementExecutor.class);
        when(statementExecutor.execute(any(ExecutionRequest.class), any(ClassificationResult.class)))
                .thenAnswer(createConcurrentQueryAnswer(concurrentEntries, releaseQueries, currentExecutions, maxExecutions));
        MCPSQLExecutionFacade facade = createFacade(sessionManager, statementExecutor, mock(MCPJdbcTransactionResourceManager.class));
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        try {
            Future<ExecuteQueryResponse> firstFuture = executorService.submit(() -> facade.execute(createExecutionRequest("session-1", "SELECT * FROM orders")));
            Future<ExecuteQueryResponse> secondFuture = executorService.submit(() -> facade.execute(createExecutionRequest("session-2", "SELECT * FROM orders")));
            assertFalse(firstFuture.isDone());
            assertTrue(concurrentEntries.await(1, TimeUnit.SECONDS));
            assertFalse(secondFuture.isDone());
            releaseQueries.countDown();
            assertTrue(firstFuture.get().isSuccessful());
            assertTrue(secondFuture.get().isSuccessful());
            assertThat(maxExecutions.get(), is(2));
        } finally {
            executorService.shutdownNow();
        }
    }
    
    private MCPSQLExecutionFacade createFacade(final MCPSessionManager sessionManager, final MCPJdbcStatementExecutor statementExecutor,
                                               final MCPJdbcTransactionResourceManager transactionResourceManager) {
        MCPCapabilityBuilder capabilityBuilder = new MCPCapabilityBuilder(
                new DatabaseMetadataSnapshots(Map.of("logic_db", new DatabaseMetadataSnapshot("MySQL", "", Collections.emptyList()))));
        return new MCPSQLExecutionFacade(capabilityBuilder, sessionManager, new MCPJdbcTransactionStatementExecutor(sessionManager, transactionResourceManager),
                statementExecutor, mock(MetadataRefreshCoordinator.class));
    }
    
    private ExecutionRequest createExecutionRequest(final String sessionId, final String sql) {
        return new ExecutionRequest(sessionId, "logic_db", "public", sql, 10, 1000);
    }
    
    private ExecuteQueryResponse createResultSetResponse() {
        return ExecuteQueryResponse.resultSet(List.of(new ExecuteQueryColumnDefinition("order_id", "INTEGER", "INTEGER", false)), List.of(List.of(1)), false);
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
    
    private org.mockito.stubbing.Answer<ExecuteQueryResponse> createBlockingQueryAnswer(final CountDownLatch firstInvocationStarted, final CountDownLatch releaseFirstInvocation,
                                                                                        final AtomicInteger currentExecutions, final AtomicInteger maxExecutions,
                                                                                        final AtomicInteger invocationCount) {
        return invocation -> handleBlockingQuery(firstInvocationStarted, releaseFirstInvocation, currentExecutions, maxExecutions, invocationCount);
    }
    
    private ExecuteQueryResponse handleBlockingQuery(final CountDownLatch firstInvocationStarted, final CountDownLatch releaseFirstInvocation,
                                                     final AtomicInteger currentExecutions, final AtomicInteger maxExecutions, final AtomicInteger invocationCount) {
        int current = currentExecutions.incrementAndGet();
        maxExecutions.accumulateAndGet(current, Math::max);
        try {
            if (1 == invocationCount.incrementAndGet()) {
                firstInvocationStarted.countDown();
                awaitLatch(releaseFirstInvocation);
            }
            return createResultSetResponse();
        } finally {
            currentExecutions.decrementAndGet();
        }
    }
    
    private org.mockito.stubbing.Answer<ExecuteQueryResponse> createConcurrentQueryAnswer(final CountDownLatch concurrentEntries, final CountDownLatch releaseQueries,
                                                                                          final AtomicInteger currentExecutions, final AtomicInteger maxExecutions) {
        return invocation -> handleConcurrentQuery(concurrentEntries, releaseQueries, currentExecutions, maxExecutions);
    }
    
    private ExecuteQueryResponse handleConcurrentQuery(final CountDownLatch concurrentEntries, final CountDownLatch releaseQueries,
                                                       final AtomicInteger currentExecutions, final AtomicInteger maxExecutions) {
        int current = currentExecutions.incrementAndGet();
        maxExecutions.accumulateAndGet(current, Math::max);
        concurrentEntries.countDown();
        try {
            awaitLatch(releaseQueries);
            return createResultSetResponse();
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
