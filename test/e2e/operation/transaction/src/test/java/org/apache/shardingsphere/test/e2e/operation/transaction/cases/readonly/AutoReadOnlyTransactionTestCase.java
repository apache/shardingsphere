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

package org.apache.shardingsphere.test.e2e.operation.transaction.cases.readonly;

import org.apache.shardingsphere.test.e2e.operation.transaction.cases.base.BaseTransactionTestCase;
import org.apache.shardingsphere.test.e2e.operation.transaction.engine.base.TransactionContainerComposer;
import org.apache.shardingsphere.test.e2e.operation.transaction.engine.base.TransactionTestCase;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Auto read only transaction test case.
 */
@TransactionTestCase
public final class AutoReadOnlyTransactionTestCase extends BaseTransactionTestCase {
    
    public AutoReadOnlyTransactionTestCase(final TransactionTestCaseParameter testCaseParam) {
        super(testCaseParam);
    }
    
    @Override
    public void executeTest(final TransactionContainerComposer containerComposer) throws SQLException {
        try (Connection connection = getDataSource().getConnection()) {
            connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            Connection queryConnection = getDataSource().getConnection();
            queryConnection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            assertAccountRowCount(connection, 0);
            connection.setAutoCommit(true);
            executeWithLog(connection, "INSERT INTO account VALUES (1, 1, 1)");
            assertAccountBalances(queryConnection, 1);
            connection.setAutoCommit(false);
            executeWithLog(connection, "SELECT * FROM account");
            connection.setAutoCommit(false);
            executeWithLog(connection, "INSERT INTO account VALUES (2, 2, 2)");
            connection.setAutoCommit(true);
            assertAccountBalances(queryConnection, 1, 2);
            executeWithLog(connection, "INSERT INTO account VALUES (3, 3, 3)");
            assertAccountBalances(queryConnection, 1, 2, 3);
            connection.setAutoCommit(false);
            executeWithLog(connection, "SELECT * FROM account");
            executeWithLog(connection, "SELECT * FROM account");
            connection.commit();
            assertAccountBalances(queryConnection, 1, 2, 3);
            connection.setAutoCommit(false);
            executeWithLog(connection, "SELECT * FROM account");
            executeWithLog(connection, "INSERT INTO account VALUES (4, 4, 4)");
            executeWithLog(connection, "SELECT * FROM account");
            connection.commit();
            assertAccountBalances(queryConnection, 1, 2, 3, 4);
            queryConnection.close();
        }
    }
}
