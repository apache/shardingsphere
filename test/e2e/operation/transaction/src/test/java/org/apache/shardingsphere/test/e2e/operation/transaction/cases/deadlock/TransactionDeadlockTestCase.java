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

package org.apache.shardingsphere.test.e2e.operation.transaction.cases.deadlock;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.test.e2e.operation.transaction.cases.base.BaseTransactionTestCase;
import org.apache.shardingsphere.test.e2e.operation.transaction.engine.base.TransactionContainerComposer;
import org.apache.shardingsphere.test.e2e.operation.transaction.engine.base.TransactionTestCase;
import org.apache.shardingsphere.test.e2e.operation.transaction.engine.constants.TransactionTestConstants;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Transaction deadlock test case.
 */
@TransactionTestCase(dbTypes = TransactionTestConstants.MYSQL)
@Slf4j
public final class TransactionDeadlockTestCase extends BaseTransactionTestCase {
    
    private final CyclicBarrier barrier = new CyclicBarrier(2);
    
    private final ExecutorService executor = Executors.newFixedThreadPool(2);
    
    public TransactionDeadlockTestCase(final TransactionTestCaseParameter testCaseParam) {
        super(testCaseParam);
    }
    
    @Override
    protected void beforeTest() throws SQLException {
        super.beforeTest();
        prepare();
    }
    
    private void prepare() throws SQLException {
        try (Connection connection = getDataSource().getConnection()) {
            executeWithLog(connection, "DELETE FROM account");
            executeWithLog(connection, "INSERT INTO account(id, balance, transaction_id) VALUES(1, 1, 1),(2, 2, 2),(3, 3, 3),(4, 4, 4)");
        }
        try (Connection connection = getDataSource().getConnection()) {
            assertAccountRowCount(connection, 4);
        }
    }
    
    @Override
    protected void executeTest(final TransactionContainerComposer containerComposer) throws SQLException {
        long startTime = System.currentTimeMillis();
        try {
            executeAndAssertTransfers();
        } finally {
            executor.shutdown();
        }
        log.info("The deadlock test case execution time is: {}", System.currentTimeMillis() - startTime);
    }
    
    private void executeAndAssertTransfers() throws SQLException {
        Future<Void> transfer1 = executor.submit(() -> executeTransfer(1, 2));
        Future<Void> transfer2 = executor.submit(() -> executeTransfer(2, 1));
        boolean transfer1Successful = isTransferSuccessful(transfer1);
        boolean transfer2Successful = isTransferSuccessful(transfer2);
        assertFalse(transfer1Successful && transfer2Successful, "At least one transfer should fail because of the deadlock.");
        try (Connection connection = getDataSource().getConnection()) {
            assertAccountRowCount(connection, 4);
            if (transfer1Successful) {
                assertAccountBalances(connection, 0, 3, 3, 4);
            } else if (transfer2Successful) {
                assertAccountBalances(connection, 2, 1, 3, 4);
            } else {
                assertAccountBalances(connection, 1, 2, 3, 4);
            }
        }
    }
    
    private boolean isTransferSuccessful(final Future<Void> future) {
        try {
            future.get();
            return true;
        } catch (final InterruptedException ex) {
            Thread.currentThread().interrupt();
            return fail("Interrupted while waiting for a transfer result.", ex);
        } catch (final ExecutionException ex) {
            assertTrue(ex.getCause() instanceof SQLException);
            SQLException actualException = (SQLException) ex.getCause();
            assertThat(actualException.getMessage(),
                    anyOf(containsString("Lock wait timeout exceeded; try restarting transaction"), containsString("Deadlock found when trying to get lock; try restarting transaction")));
            return false;
        }
    }
    
    private Void executeTransfer(final int sourceId, final int targetId) throws SQLException {
        try (Connection connection = getDataSource().getConnection()) {
            try {
                connection.setAutoCommit(false);
                assertAccountRowCount(connection, 4);
                executeWithLog(connection, String.format("UPDATE account SET balance = balance - 1 WHERE id = %d", sourceId));
                await();
                executeWithLog(connection, String.format("UPDATE account SET balance = balance + 1 WHERE id = %d", targetId));
                await();
                connection.commit();
            } catch (final SQLException ex) {
                rollback(connection, ex);
                throw ex;
            }
        }
        return null;
    }
    
    private void rollback(final Connection connection, final SQLException cause) {
        try {
            await();
        } catch (final SQLException ex) {
            cause.addSuppressed(ex);
        }
        try {
            connection.rollback();
        } catch (final SQLException ex) {
            cause.addSuppressed(ex);
        }
    }
    
    private void await() throws SQLException {
        try {
            barrier.await(10L, TimeUnit.SECONDS);
        } catch (final InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new SQLException("Interrupted while coordinating deadlock transfers.", ex);
        } catch (final BrokenBarrierException | TimeoutException ex) {
            throw new SQLException("Failed to coordinate deadlock transfers.", ex);
        }
    }
}
