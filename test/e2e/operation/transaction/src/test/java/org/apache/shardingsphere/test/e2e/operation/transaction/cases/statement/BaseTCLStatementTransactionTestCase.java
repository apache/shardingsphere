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

import org.apache.shardingsphere.test.e2e.operation.transaction.cases.base.BaseTransactionTestCase;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Base TCL statement transaction test case.
 */
public abstract class BaseTCLStatementTransactionTestCase extends BaseTransactionTestCase {
    
    protected BaseTCLStatementTransactionTestCase(final TransactionTestCaseParameter testCaseParam) {
        super(testCaseParam);
    }
    
    protected void assertRollback(final Connection connection, final Connection queryConnection) throws SQLException {
        executeWithLog(connection, "BEGIN");
        executeWithLog(connection, "INSERT INTO account (id, balance, transaction_id) VALUES (1, 1, 1), (2, 2, 2)");
        assertAccountBalances(queryConnection);
        executeWithLog(connection, "ROLLBACK");
        assertAccountBalances(queryConnection);
    }
    
    protected void assertCommit(final Connection connection, final Connection queryConnection) throws SQLException {
        executeWithLog(connection, "BEGIN");
        executeWithLog(connection, "INSERT INTO account (id, balance, transaction_id) VALUES (1, 1, 1), (2, 2, 2)");
        assertAccountBalances(queryConnection);
        executeWithLog(connection, "COMMIT");
        assertAccountBalances(queryConnection, 1, 2);
    }
}
