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

package org.apache.shardingsphere.test.e2e.operation.transaction.cases.statement;

import org.apache.shardingsphere.test.e2e.operation.transaction.engine.base.TransactionContainerComposer;
import org.apache.shardingsphere.test.e2e.operation.transaction.engine.base.TransactionTestCase;
import org.apache.shardingsphere.test.e2e.operation.transaction.engine.constants.TransactionTestConstants;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * MySQL TCL statement transaction test case.
 */
@TransactionTestCase(adapters = TransactionTestConstants.PROXY, dbTypes = TransactionTestConstants.MYSQL)
public final class MySQLTCLStatementTestCase extends BaseTCLStatementTransactionTestCase {
    
    public MySQLTCLStatementTestCase(final TransactionTestCaseParameter testCaseParam) {
        super(testCaseParam);
    }
    
    @Override
    protected void executeTest(final TransactionContainerComposer containerComposer) throws SQLException {
        try (
                Connection connection = getDataSource().getConnection();
                Connection queryConnection = getDataSource().getConnection()) {
            assertRollback(connection, queryConnection);
            assertCommit(connection, queryConnection);
            assertBegin(connection, queryConnection);
            assertStartTransaction(connection, queryConnection);
            assertSetAutoCommit(connection, queryConnection);
        }
    }
    
    private void assertBegin(final Connection connection, final Connection queryConnection) throws SQLException {
        executeWithLog(connection, "BEGIN");
        executeWithLog(connection, "INSERT INTO account (id, balance, transaction_id) VALUES (3, 3, 3), (4, 4, 4)");
        assertAccountBalances(queryConnection, 1, 2);
        executeWithLog(connection, "BEGIN");
        assertAccountBalances(queryConnection, 1, 2, 3, 4);
        executeWithLog(connection, "INSERT INTO account (id, balance, transaction_id) VALUES (5, 5, 5), (6, 6, 6)");
        executeWithLog(connection, "ROLLBACK");
        assertAccountBalances(queryConnection, 1, 2, 3, 4);
    }
    
    private void assertStartTransaction(final Connection connection, final Connection queryConnection) throws SQLException {
        executeWithLog(connection, "DELETE FROM account");
        assertAccountRowCount(queryConnection, 0);
        executeWithLog(connection, "START TRANSACTION");
        executeWithLog(connection, "INSERT INTO account (id, balance, transaction_id) VALUES (1, 1, 1), (2, 2, 2)");
        assertAccountBalances(queryConnection);
        executeWithLog(connection, "COMMIT");
        assertAccountBalances(queryConnection, 1, 2);
    }
    
    private void assertSetAutoCommit(final Connection connection, final Connection queryConnection) throws SQLException {
        executeWithLog(connection, "DELETE FROM account");
        assertAccountBalances(queryConnection);
        executeWithLog(connection, "SET AUTOCOMMIT=0");
        executeWithLog(connection, "INSERT INTO account (id, balance, transaction_id) VALUES (1, 1, 1), (2, 2, 2)");
        assertAccountBalances(queryConnection);
        executeWithLog(connection, "COMMIT");
        assertAccountBalances(queryConnection, 1, 2);
        executeWithLog(connection, "INSERT INTO account (id, balance, transaction_id) VALUES (3, 3, 3), (4, 4, 4)");
        assertAccountBalances(queryConnection, 1, 2);
        executeWithLog(connection, "BEGIN");
        assertAccountBalances(queryConnection, 1, 2, 3, 4);
        assertTrue(connection.getAutoCommit());
        executeWithLog(connection, "INSERT INTO account (id, balance, transaction_id) VALUES (5, 5, 5), (6, 6, 6)");
        assertAccountBalances(queryConnection, 1, 2, 3, 4);
        executeWithLog(connection, "SET AUTOCOMMIT=1");
        assertAccountBalances(queryConnection, 1, 2, 3, 4, 5, 6);
        assertTrue(connection.getAutoCommit());
    }
}
