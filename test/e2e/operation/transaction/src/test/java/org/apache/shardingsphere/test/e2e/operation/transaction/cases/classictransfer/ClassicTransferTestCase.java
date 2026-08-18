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

package org.apache.shardingsphere.test.e2e.operation.transaction.cases.classictransfer;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.test.e2e.operation.transaction.cases.base.BaseTransactionTestCase;
import org.apache.shardingsphere.test.e2e.operation.transaction.engine.base.TransactionContainerComposer;
import org.apache.shardingsphere.test.e2e.operation.transaction.engine.base.TransactionTestCase;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Classic transfer transaction integration test.
 */
@TransactionTestCase
public final class ClassicTransferTestCase extends BaseTransactionTestCase {
    
    public ClassicTransferTestCase(final TransactionTestCaseParameter testCaseParam) {
        super(testCaseParam);
    }
    
    @Override
    public void executeTest(final TransactionContainerComposer containerComposer) throws SQLException {
        try (Connection connection = getDataSource().getConnection()) {
            executeUpdateWithLog(connection, "INSERT INTO account(id, transaction_id, balance) VALUES (1, 1, 0), (2, 2, 100);");
        }
        innerRun();
    }
    
    private void innerRun() throws SQLException {
        Collection<Thread> tasks = new LinkedList<>();
        AtomicReference<Throwable> failure = new AtomicReference<>();
        for (int i = 0; i < 20; i++) {
            Thread updateThread = new Thread(new UpdateAccountTask(getDataSource(), failure));
            updateThread.start();
            tasks.add(updateThread);
        }
        for (Thread task : tasks) {
            try {
                task.join();
            } catch (final InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new SQLException("Interrupted while waiting for transfer tasks.", ex);
            }
        }
        Throwable actualFailure = failure.get();
        if (actualFailure instanceof SQLException) {
            throw (SQLException) actualFailure;
        }
        if (null != actualFailure) {
            throw (AssertionError) actualFailure;
        }
        assertAccountBalances();
    }
    
    private void assertAccountBalances() throws SQLException {
        try (
                Connection connection = getDataSource().getConnection();
                Statement statement = connection.createStatement()) {
            connection.setAutoCommit(false);
            try (ResultSet resultSet = statement.executeQuery("SELECT transaction_id, balance FROM account WHERE transaction_id IN (1, 2) ORDER BY transaction_id")) {
                assertTrue(resultSet.next());
                assertThat(resultSet.getInt("transaction_id"), is(1));
                assertThat(resultSet.getInt("balance"), is(20));
                assertTrue(resultSet.next());
                assertThat(resultSet.getInt("transaction_id"), is(2));
                assertThat(resultSet.getInt("balance"), is(80));
                assertFalse(resultSet.next());
            }
            connection.commit();
        }
    }
    
    @RequiredArgsConstructor
    private static final class UpdateAccountTask implements Runnable {
        
        private final DataSource dataSource;
        
        private final AtomicReference<Throwable> failure;
        
        public void run() {
            try (Connection connection = dataSource.getConnection()) {
                connection.setAutoCommit(false);
                try (Statement statement = connection.createStatement()) {
                    assertThat(statement.executeUpdate("UPDATE account SET balance = balance - 1 WHERE id = 2 AND transaction_id = 2;"), is(1));
                    assertThat(statement.executeUpdate("UPDATE account SET balance = balance + 1 WHERE id = 1 AND transaction_id = 1;"), is(1));
                    connection.commit();
                } catch (final SQLException | AssertionError ex) {
                    rollback(connection, ex);
                    throw ex;
                }
            } catch (final SQLException | AssertionError ex) {
                failure.compareAndSet(null, ex);
            }
        }
        
        private void rollback(final Connection connection, final Throwable cause) {
            try {
                connection.rollback();
            } catch (final SQLException ex) {
                cause.addSuppressed(ex);
            }
        }
    }
}
