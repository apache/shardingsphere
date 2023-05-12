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

package org.apache.shardingsphere.test.e2e.transaction.cases.deadlock;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.test.e2e.transaction.cases.base.BaseTransactionTestCase;
import org.apache.shardingsphere.test.e2e.transaction.engine.base.TransactionBaseE2EIT;
import org.apache.shardingsphere.test.e2e.transaction.engine.base.TransactionContainerComposer;
import org.apache.shardingsphere.test.e2e.transaction.engine.base.TransactionTestCase;
import org.apache.shardingsphere.test.e2e.transaction.engine.constants.TransactionTestConstants;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Transaction deadlock test case.
 */
@Slf4j
@TransactionTestCase(dbTypes = TransactionTestConstants.MYSQL)
public final class TransactionDeadlockTestCase extends BaseTransactionTestCase {
    
    private final CyclicBarrier barrier = new CyclicBarrier(2);
    
    private final ExecutorService executor = Executors.newFixedThreadPool(2);
    
    public TransactionDeadlockTestCase(final TransactionBaseE2EIT baseTransactionITCase, final DataSource dataSource) {
        super(baseTransactionITCase, dataSource);
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
        final long startTime = System.currentTimeMillis();
        Collection<Future<Void>> futures = new LinkedList<>();
        futures.add(executor.submit(this::executeTransfer1));
        futures.add(executor.submit(this::executeTransfer2));
        try (Connection connection = getDataSource().getConnection()) {
            assertAccountRowCount(connection, 4);
        }
        for (Future<Void> each : futures) {
            try {
                each.get();
                // CHECKSTYLE:OFF
            } catch (final Exception ex) {
                // CHECKSTYLE:ON
                assertThat(ex.getMessage(), is("com.mysql.jdbc.exceptions.jdbc4.MySQLTransactionRollbackException: Lock wait timeout exceeded; try restarting transaction"));
            }
        }
        log.info("The deadlock test case execution time is: {}", System.currentTimeMillis() - startTime);
        executor.shutdown();
        try (Connection connection = getDataSource().getConnection()) {
            assertAccountBalances(connection, 1, 2, 3, 4);
        }
    }
    
    private Void executeTransfer1() throws SQLException {
        Connection connection = getDataSource().getConnection();
        try {
            connection.setAutoCommit(false);
            executeWithLog(connection, "UPDATE account SET balance = balance - 1 WHERE id = 1");
            await();
            executeWithLog(connection, "UPDATE account SET balance = balance + 1 WHERE id = 2");
            await();
            connection.commit();
        } catch (final SQLException ex) {
            connection.rollback();
            throw ex;
        } finally {
            connection.close();
        }
        return null;
    }
    
    private Void executeTransfer2() throws SQLException {
        Connection connection = getDataSource().getConnection();
        try {
            connection.setAutoCommit(false);
            executeWithLog(connection, "UPDATE account SET balance = balance - 1 WHERE id = 2");
            await();
            executeWithLog(connection, "UPDATE account SET balance = balance + 1 WHERE id = 1");
            await();
            connection.commit();
        } catch (final SQLException ex) {
            connection.rollback();
            throw ex;
        } finally {
            connection.close();
        }
        return null;
    }
    
    private void await() {
        try {
            barrier.await();
        } catch (final InterruptedException | BrokenBarrierException ignored) {
        }
    }
}
