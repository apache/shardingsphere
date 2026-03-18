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

package org.apache.shardingsphere.test.e2e.operation.transaction.cases.autocommit;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.test.e2e.operation.transaction.engine.base.TransactionContainerComposer;
import org.apache.shardingsphere.test.e2e.operation.transaction.engine.base.TransactionTestCase;
import org.apache.shardingsphere.test.e2e.operation.transaction.engine.constants.TransactionTestConstants;
import org.apache.shardingsphere.transaction.api.TransactionType;
import org.awaitility.Awaitility;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * MySQL auto commit transaction integration test.
 */
@TransactionTestCase(dbTypes = TransactionTestConstants.MYSQL)
@Slf4j
public final class MySQLAutoCommitTestCase extends AutoCommitTestCase {
    
    public MySQLAutoCommitTestCase(final TransactionTestCaseParameter testCaseParam) {
        super(testCaseParam);
    }
    
    @Override
    public void executeTest(final TransactionContainerComposer containerComposer) throws SQLException {
        if (TransactionType.LOCAL == getTransactionType()) {
            assertAutoCommit();
        }
        assertAutoCommitWithStatement();
        assertAutoCommitWithPreparedStatement();
        assertAutoCommitWithoutCommit();
        assertExceptionForceCommit();
    }
    
    private void assertExceptionForceCommit() throws SQLException {
        Connection connection = getDataSource().getConnection();
        try {
            executeWithLog(connection, "DELETE FROM account");
            connection.setAutoCommit(false);
            executeWithLog(connection, "INSERT INTO account VALUES (1, 1, 1), (2, 2, 2)");
            int causeExceptionResult = 1 / 0;
            log.info("Caused exception result: {}", causeExceptionResult);
            executeWithLog(connection, "INSERT INTO account VALUES (3, 3, 3), (4, 4, 4)");
        } catch (final ArithmeticException ignored) {
        } finally {
            connection.commit();
            connection.close();
        }
        try (Connection queryConnection = getDataSource().getConnection()) {
            assertAccountRowCount(queryConnection, 2);
        }
    }
    
    private void assertAutoCommit() throws SQLException {
        // TODO Currently XA transaction does not support two transactions in the same thread at the same time
        try (
                Connection connection1 = getDataSource().getConnection();
                Connection connection2 = getDataSource().getConnection()) {
            executeWithLog(connection1, "SET session transaction isolation level read committed;");
            executeWithLog(connection2, "SET session transaction isolation level read committed;");
            connection1.setAutoCommit(false);
            connection2.setAutoCommit(false);
            executeWithLog(connection1, "INSERT INTO account(id, balance, transaction_id) VALUES(1, 100, 1);");
            assertFalse(executeQueryWithLog(connection2, "SELECT * FROM account;").next());
            connection1.commit();
            Awaitility.await().atMost(1L, TimeUnit.SECONDS).pollDelay(200L, TimeUnit.MILLISECONDS).until(() -> executeQueryWithLog(connection2, "SELECT * FROM account;").next());
            assertTrue(executeQueryWithLog(connection2, "SELECT * FROM account;").next());
        }
    }
}
