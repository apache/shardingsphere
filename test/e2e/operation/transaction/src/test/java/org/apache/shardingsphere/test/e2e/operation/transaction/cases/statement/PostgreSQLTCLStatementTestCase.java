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

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * PostgreSQL TCL statement transaction test case.
 */
@TransactionTestCase(adapters = TransactionTestConstants.PROXY, dbTypes = TransactionTestConstants.POSTGRESQL)
public final class PostgreSQLTCLStatementTestCase extends BaseTCLStatementTransactionTestCase {
    
    public PostgreSQLTCLStatementTestCase(final TransactionTestCaseParameter testCaseParam) {
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
            assertBeginTransaction(connection, queryConnection);
        }
    }
    
    private void assertBegin(final Connection connection, final Connection queryConnection) throws SQLException {
        executeWithLog(connection, "BEGIN");
        executeWithLog(connection, "INSERT INTO account (id, balance, transaction_id) VALUES (3, 3, 3), (4, 4, 4)");
        assertAccountBalances(queryConnection, 1, 2);
        assertThrows(SQLException.class, () -> executeWithLog(connection, "BEGIN"));
        executeWithLog(connection, "ROLLBACK");
        assertAccountBalances(queryConnection, 1, 2);
    }
    
    private void assertBeginTransaction(final Connection connection, final Connection queryConnection) throws SQLException {
        executeWithLog(connection, "DELETE FROM account");
        assertAccountRowCount(queryConnection, 0);
        executeWithLog(connection, "BEGIN TRANSACTION");
        executeWithLog(connection, "INSERT INTO account (id, balance, transaction_id) VALUES (1, 1, 1), (2, 2, 2)");
        assertAccountBalances(queryConnection);
        executeWithLog(connection, "END TRANSACTION");
        assertAccountBalances(queryConnection, 1, 2);
    }
}
