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

package org.apache.shardingsphere.test.e2e.operation.transaction.cases.savepoint;

import org.apache.shardingsphere.test.e2e.operation.transaction.cases.base.BaseTransactionTestCase;
import org.apache.shardingsphere.test.e2e.operation.transaction.engine.base.TransactionContainerComposer;
import org.apache.shardingsphere.test.e2e.operation.transaction.engine.base.TransactionTestCase;
import org.apache.shardingsphere.test.e2e.operation.transaction.engine.constants.TransactionTestConstants;
import org.apache.shardingsphere.transaction.api.TransactionType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Savepoint;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * PostgreSQL and openGauss transaction continuation after savepoint rollback integration test.
 */
@TransactionTestCase(dbTypes = {TransactionTestConstants.POSTGRESQL, TransactionTestConstants.OPENGAUSS}, adapters = TransactionTestConstants.JDBC,
        transactionTypes = TransactionType.LOCAL)
public final class PostgreSQLAndOpenGaussTransactionContinueAfterSavepointTestCase extends BaseTransactionTestCase {
    
    public PostgreSQLAndOpenGaussTransactionContinueAfterSavepointTestCase(final TransactionTestCaseParameter testCaseParam) {
        super(testCaseParam);
    }
    
    @Override
    protected void executeTest(final TransactionContainerComposer containerComposer) throws SQLException {
        assertContinueWithStatement();
        assertContinueWithPreparedStatement();
    }
    
    private void assertContinueWithStatement() throws SQLException {
        prepareAccount();
        try (Connection connection = getDataSource().getConnection()) {
            connection.setAutoCommit(false);
            executeStatementFailureAndContinue(connection, connection.setSavepoint("point_before_failure"));
            connection.commit();
        }
        assertCommittedBalances();
    }
    
    private void executeStatementFailureAndContinue(final Connection connection, final Savepoint savepoint) throws SQLException {
        executeUpdateWithLog(connection, "UPDATE account SET balance = 100 WHERE id = 1");
        SQLException duplicatedKeyException = assertThrows(SQLException.class,
                () -> executeUpdateWithLog(connection, "INSERT INTO account (id, balance, transaction_id) VALUES (1, 11, 11)"));
        assertThat(duplicatedKeyException.getSQLState(), is("23505"));
        assertTransactionAborted(connection, "INSERT INTO account (id, balance, transaction_id) VALUES (2, 2, 2)");
        connection.rollback(savepoint);
        executeUpdateWithLog(connection, "INSERT INTO account (id, balance, transaction_id) VALUES (2, 2, 2)");
    }
    
    private void assertContinueWithPreparedStatement() throws SQLException {
        prepareAccount();
        try (
                Connection connection = getDataSource().getConnection();
                PreparedStatement insertStatement = connection.prepareStatement("INSERT INTO account (id, balance, transaction_id) VALUES (?, ?, ?)")) {
            connection.setAutoCommit(false);
            executePreparedStatementFailureAndContinue(connection, insertStatement, connection.setSavepoint("point_before_failure"));
            connection.commit();
        }
        assertCommittedBalances();
    }
    
    private void executePreparedStatementFailureAndContinue(final Connection connection, final PreparedStatement insertStatement, final Savepoint savepoint) throws SQLException {
        setAccount(insertStatement, 1, 11);
        SQLException duplicatedKeyException = assertThrows(SQLException.class, insertStatement::executeUpdate);
        assertThat(duplicatedKeyException.getSQLState(), is("23505"));
        setAccount(insertStatement, 2, 2);
        SQLException abortedTransactionException = assertThrows(SQLException.class, insertStatement::executeUpdate);
        assertThat(abortedTransactionException.getClass(), is(SQLFeatureNotSupportedException.class));
        connection.rollback(savepoint);
        setAccount(insertStatement, 2, 2);
        insertStatement.executeUpdate();
    }
    
    private void prepareAccount() throws SQLException {
        try (Connection connection = getDataSource().getConnection()) {
            executeWithLog(connection, "DELETE FROM account");
            executeWithLog(connection, "INSERT INTO account (id, balance, transaction_id) VALUES (1, 1, 1)");
        }
    }
    
    private void assertTransactionAborted(final Connection connection, final String sql) {
        SQLException actualException = assertThrows(SQLException.class, () -> executeUpdateWithLog(connection, sql));
        assertThat(actualException.getClass(), is(SQLFeatureNotSupportedException.class));
    }
    
    private void setAccount(final PreparedStatement insertStatement, final int id, final int balance) throws SQLException {
        insertStatement.setInt(1, id);
        insertStatement.setInt(2, balance);
        insertStatement.setInt(3, id);
    }
    
    private void assertCommittedBalances() throws SQLException {
        try (Connection connection = getDataSource().getConnection()) {
            assertAccountBalances(connection, 1, 2);
        }
    }
}
