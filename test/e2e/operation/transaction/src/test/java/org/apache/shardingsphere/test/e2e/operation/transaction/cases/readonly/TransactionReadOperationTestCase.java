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
import java.sql.Savepoint;

@TransactionTestCase
public final class TransactionReadOperationTestCase extends BaseTransactionTestCase {
    
    public TransactionReadOperationTestCase(final TransactionTestCaseParameter testCaseParam) {
        super(testCaseParam);
    }
    
    @Override
    protected void executeTest(final TransactionContainerComposer containerComposer) throws SQLException {
        assertStandardReadInTransactionTestCase();
        try (
                Connection connection = getDataSource().getConnection();
                Connection queryConnection = getDataSource().getConnection()) {
            connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            queryConnection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            assertEmptyBeginAndCommit(connection);
            assertReadQueryTransaction(connection, queryConnection);
            assertReadWriteTransaction(connection, queryConnection);
        }
    }
    
    private void assertStandardReadInTransactionTestCase() throws SQLException {
        Connection queryConnection = getDataSource().getConnection();
        try (Connection connection = getDataSource().getConnection()) {
            connection.setAutoCommit(false);
            executeQueryWithLog(connection, "SELECT * FROM account");
            executeQueryWithLog(connection, "SELECT * FROM account");
            connection.rollback();
            connection.setAutoCommit(true);
        }
        try (Connection connection = getDataSource().getConnection()) {
            connection.setAutoCommit(false);
            executeQueryWithLog(connection, "SELECT * FROM account");
            executeQueryWithLog(connection, "SELECT * FROM account");
            connection.commit();
            connection.setAutoCommit(true);
        }
        assertAccountRowCount(queryConnection, 0);
        try (Connection connection = getDataSource().getConnection()) {
            connection.setAutoCommit(false);
            executeQueryWithLog(connection, "SELECT * FROM account");
            executeWithLog(connection, "INSERT INTO account VALUES (1, 1, 1)");
            executeQueryWithLog(connection, "SELECT * FROM account");
            connection.rollback();
            connection.setAutoCommit(true);
        }
        assertAccountRowCount(queryConnection, 0);
        try (Connection connection = getDataSource().getConnection()) {
            connection.setAutoCommit(false);
            executeQueryWithLog(connection, "SELECT * FROM account");
            executeWithLog(connection, "INSERT INTO account VALUES (1, 1, 1)");
            executeQueryWithLog(connection, "SELECT * FROM account");
            connection.commit();
            connection.setAutoCommit(true);
        }
        assertAccountBalances(queryConnection, 1);
        executeWithLog(queryConnection, "DELETE FROM account");
        queryConnection.close();
    }
    
    private void assertEmptyBeginAndCommit(final Connection connection) throws SQLException {
        connection.setAutoCommit(false);
        connection.commit();
        connection.setAutoCommit(false);
        connection.rollback();
    }
    
    private void assertReadQueryTransaction(final Connection connection, final Connection queryConnection) throws SQLException {
        connection.setAutoCommit(false);
        assertAccountRowCount(queryConnection, 0);
        executeQueryWithLog(connection, "SELECT * FROM account");
        connection.commit();
        connection.setAutoCommit(false);
        executeQueryWithLog(connection, "SELECT * FROM account");
        connection.rollback();
        assertAccountRowCount(queryConnection, 0);
        connection.setAutoCommit(false);
        executeQueryWithLog(connection, "SELECT * FROM account");
        executeQueryWithLog(connection, "SELECT * FROM account");
        connection.commit();
        assertAccountRowCount(queryConnection, 0);
        connection.setAutoCommit(false);
        executeQueryWithLog(connection, "SELECT * FROM account FOR UPDATE");
        connection.commit();
    }
    
    private void assertReadWriteTransaction(final Connection connection, final Connection queryConnection) throws SQLException {
        connection.setAutoCommit(false);
        executeQueryWithLog(connection, "SELECT * FROM account");
        executeWithLog(connection, "INSERT INTO account VALUES (1, 1, 1)");
        assertAccountRowCount(queryConnection, 0);
        executeQueryWithLog(connection, "SELECT * FROM account");
        connection.commit();
        assertAccountBalances(queryConnection, 1);
        connection.setAutoCommit(false);
        executeQueryWithLog(connection, "SELECT * FROM account");
        executeWithLog(connection, "INSERT INTO account VALUES (2, 2, 2)");
        assertAccountBalances(queryConnection, 1);
        connection.rollback();
        assertAccountBalances(queryConnection, 1);
        connection.setAutoCommit(false);
        executeQueryWithLog(connection, "SELECT * FROM account");
        connection.setAutoCommit(true);
        connection.setAutoCommit(false);
        executeQueryWithLog(connection, "SELECT * FROM account");
        Savepoint savepoint = connection.setSavepoint("savepoint1");
        executeWithLog(connection, "INSERT INTO account VALUES (3, 3, 3)");
        executeQueryWithLog(connection, "SELECT * FROM account");
        connection.rollback(savepoint);
        connection.commit();
        assertAccountBalances(queryConnection, 1);
        connection.setAutoCommit(false);
        executeQueryWithLog(connection, "SELECT now()");
        connection.commit();
    }
}
