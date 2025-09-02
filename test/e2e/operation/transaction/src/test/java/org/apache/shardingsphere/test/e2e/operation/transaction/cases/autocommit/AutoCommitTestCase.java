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

import org.apache.shardingsphere.test.e2e.operation.transaction.cases.base.BaseTransactionTestCase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Auto commit transaction integration test.
 */
public abstract class AutoCommitTestCase extends BaseTransactionTestCase {
    
    protected AutoCommitTestCase(final TransactionTestCaseParameter testCaseParam) {
        super(testCaseParam);
    }
    
    protected void assertAutoCommitWithStatement() throws SQLException {
        try (Connection connection = getDataSource().getConnection()) {
            Connection queryConnection = getDataSource().getConnection();
            queryConnection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            connection.setAutoCommit(false);
            executeWithLog(connection, "DELETE FROM account");
            assertFalse(connection.getAutoCommit());
            executeWithLog(connection, "INSERT INTO account VALUES (1, 1, 1)");
            connection.commit();
            assertAccountBalances(queryConnection, 1);
            assertFalse(connection.getAutoCommit());
            executeUpdateWithLog(connection, "INSERT INTO account VALUES (2, 2, 2)");
            assertAccountBalances(queryConnection, 1);
            connection.commit();
            assertAccountBalances(queryConnection, 1, 2);
            assertFalse(connection.getAutoCommit());
            executeWithLog(connection, "INSERT INTO account VALUES (3, 3, 3)");
            assertAccountBalances(queryConnection, 1, 2);
            connection.rollback();
            assertFalse(connection.getAutoCommit());
            assertAccountBalances(queryConnection, 1, 2);
            executeWithLog(connection, "INSERT INTO account VALUES (4, 4, 4)");
            assertAccountBalances(queryConnection, 1, 2);
            connection.setAutoCommit(true);
            assertTrue(connection.getAutoCommit());
            assertAccountBalances(queryConnection, 1, 2, 4);
            executeWithLog(connection, "INSERT INTO account VALUES (5, 5, 5)");
            assertAccountBalances(queryConnection, 1, 2, 4, 5);
            executeWithLog(connection, "INSERT INTO account VALUES (6, 6, 6)");
            assertAccountBalances(queryConnection, 1, 2, 4, 5, 6);
            connection.setAutoCommit(false);
            executeWithLog(connection, "INSERT INTO account VALUES (7, 7, 7)");
            assertAccountBalances(queryConnection, 1, 2, 4, 5, 6);
            executeWithLog(connection, "INSERT INTO account VALUES (8, 8, 8)");
            assertAccountBalances(queryConnection, 1, 2, 4, 5, 6);
            connection.setAutoCommit(false);
            assertAccountBalances(queryConnection, 1, 2, 4, 5, 6);
            executeWithLog(connection, "INSERT INTO account VALUES (9, 9, 9)");
            executeWithLog(connection, "INSERT INTO account VALUES (10, 10, 10)");
            assertAccountBalances(queryConnection, 1, 2, 4, 5, 6);
            connection.setAutoCommit(true);
            assertAccountBalances(queryConnection, 1, 2, 4, 5, 6, 7, 8, 9, 10);
            queryConnection.close();
        }
    }
    
    protected void assertAutoCommitWithPreparedStatement() throws SQLException {
        try (Connection connection = getDataSource().getConnection()) {
            Connection queryConnection = getDataSource().getConnection();
            queryConnection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            connection.setAutoCommit(false);
            executeWithLog(connection, "DELETE FROM account");
            assertFalse(connection.getAutoCommit());
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO account VALUES(?, ?, ?)");
            executePreparedStatement(preparedStatement, 1);
            connection.commit();
            assertFalse(connection.getAutoCommit());
            assertAccountBalances(queryConnection, 1);
            executeUpdatePreparedStatement(preparedStatement, 2);
            assertAccountBalances(queryConnection, 1);
            connection.commit();
            assertAccountBalances(queryConnection, 1, 2);
            assertFalse(connection.getAutoCommit());
            executePreparedStatement(preparedStatement, 3);
            assertAccountBalances(queryConnection, 1, 2);
            connection.rollback();
            assertFalse(connection.getAutoCommit());
            assertAccountBalances(queryConnection, 1, 2);
            executeUpdatePreparedStatement(preparedStatement, 4);
            assertAccountBalances(queryConnection, 1, 2);
            connection.setAutoCommit(true);
            assertTrue(connection.getAutoCommit());
            assertAccountBalances(queryConnection, 1, 2, 4);
            executePreparedStatement(preparedStatement, 5);
            assertAccountBalances(queryConnection, 1, 2, 4, 5);
            executePreparedStatement(preparedStatement, 6);
            connection.setAutoCommit(false);
            executePreparedStatement(preparedStatement, 7);
            assertAccountBalances(queryConnection, 1, 2, 4, 5, 6);
            executePreparedStatement(preparedStatement, 8);
            assertAccountBalances(queryConnection, 1, 2, 4, 5, 6);
            connection.setAutoCommit(false);
            assertAccountBalances(queryConnection, 1, 2, 4, 5, 6);
            executePreparedStatement(preparedStatement, 9);
            executePreparedStatement(preparedStatement, 10);
            assertAccountBalances(queryConnection, 1, 2, 4, 5, 6);
            connection.setAutoCommit(true);
            assertAccountBalances(queryConnection, 1, 2, 4, 5, 6, 7, 8, 9, 10);
            queryConnection.close();
        }
    }
    
    protected void assertAutoCommitWithoutCommit() throws SQLException {
        try (Connection connection = getDataSource().getConnection()) {
            executeWithLog(connection, "DELETE FROM account");
            connection.setAutoCommit(false);
            executeWithLog(connection, "INSERT INTO account VALUES (1, 1, 1), (2, 2, 2)");
        }
        try (Connection connection = getDataSource().getConnection()) {
            assertAccountRowCount(connection, 0);
        }
    }
    
    private void executeUpdatePreparedStatement(final PreparedStatement preparedStatement, final int value) throws SQLException {
        setPreparedStatementParameters(preparedStatement, value);
        preparedStatement.executeUpdate();
    }
    
    private void executePreparedStatement(final PreparedStatement prepareStatement, final int value) throws SQLException {
        setPreparedStatementParameters(prepareStatement, value);
        prepareStatement.execute();
    }
    
    private void setPreparedStatementParameters(final PreparedStatement prepareStatement, final int value) throws SQLException {
        prepareStatement.setInt(1, value);
        prepareStatement.setInt(2, value);
        prepareStatement.setInt(3, value);
    }
}
