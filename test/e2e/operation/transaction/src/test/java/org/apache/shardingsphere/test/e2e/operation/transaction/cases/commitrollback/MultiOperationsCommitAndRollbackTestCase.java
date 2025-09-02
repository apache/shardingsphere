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

package org.apache.shardingsphere.test.e2e.operation.transaction.cases.commitrollback;

import org.apache.shardingsphere.test.e2e.operation.transaction.cases.base.BaseTransactionTestCase;
import org.apache.shardingsphere.test.e2e.operation.transaction.engine.base.TransactionContainerComposer;
import org.apache.shardingsphere.test.e2e.operation.transaction.engine.base.TransactionTestCase;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Integration test of multiple operations in one transaction.
 */
@TransactionTestCase
public final class MultiOperationsCommitAndRollbackTestCase extends BaseTransactionTestCase {
    
    public MultiOperationsCommitAndRollbackTestCase(final TransactionTestCaseParameter testCaseParam) {
        super(testCaseParam);
    }
    
    @Override
    public void executeTest(final TransactionContainerComposer containerComposer) throws SQLException {
        assertRollback();
        assertCommit();
    }
    
    private void assertRollback() throws SQLException {
        try (Connection connection = getDataSource().getConnection()) {
            connection.setAutoCommit(false);
            assertAccountRowCount(connection, 0);
            executeWithLog(connection, "INSERT INTO account(id, balance, transaction_id) VALUES(1, 1, 1)");
            executeWithLog(connection, "INSERT INTO account(id, balance, transaction_id) VALUES(2, 2, 2)");
            executeUpdateWithLog(connection, "UPDATE account SET balance = 3, transaction_id = 3 WHERE id = 2");
            assertAccountBalances(connection, 1, 3);
            connection.rollback();
        }
        try (Connection connection = getDataSource().getConnection()) {
            assertAccountRowCount(connection, 0);
            assertAccountBalances(connection);
        }
    }
    
    private void assertCommit() throws SQLException {
        try (Connection connection = getDataSource().getConnection()) {
            connection.setAutoCommit(false);
            assertAccountRowCount(connection, 0);
            executeWithLog(connection, "INSERT INTO account(id, balance, transaction_id) VALUES(1, 1, 1)");
            executeWithLog(connection, "INSERT INTO account(id, balance, transaction_id) VALUES(2, 2, 2)");
            executeUpdateWithLog(connection, "UPDATE account SET balance = 3, transaction_id = 3 WHERE id = 2");
            assertAccountBalances(connection, 1, 3);
            connection.commit();
        }
        try (Connection connection = getDataSource().getConnection()) {
            assertAccountRowCount(connection, 2);
            assertAccountBalances(connection, 1, 3);
        }
    }
}
