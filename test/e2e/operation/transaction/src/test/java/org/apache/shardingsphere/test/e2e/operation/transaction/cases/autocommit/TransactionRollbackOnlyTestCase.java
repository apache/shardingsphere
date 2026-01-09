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

import org.apache.shardingsphere.driver.jdbc.core.connection.ShardingSphereConnection;
import org.apache.shardingsphere.test.e2e.operation.transaction.cases.base.BaseTransactionTestCase;
import org.apache.shardingsphere.test.e2e.operation.transaction.engine.base.TransactionContainerComposer;
import org.apache.shardingsphere.test.e2e.operation.transaction.engine.base.TransactionTestCase;
import org.apache.shardingsphere.test.e2e.operation.transaction.engine.constants.TransactionTestConstants;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Transaction rollback only test case.
 */
@TransactionTestCase(dbTypes = {TransactionTestConstants.POSTGRESQL, TransactionTestConstants.OPENGAUSS})
public class TransactionRollbackOnlyTestCase extends BaseTransactionTestCase {
    
    public TransactionRollbackOnlyTestCase(final TransactionTestCaseParameter testCaseParam) {
        super(testCaseParam);
    }
    
    @Override
    protected void executeTest(final TransactionContainerComposer containerComposer) throws SQLException {
        assertRollbackOnlyWithStatement();
        assertRollbackOnlyWithPreparedStatement();
    }
    
    private void assertRollbackOnlyWithStatement() throws SQLException {
        prepare();
        try (Connection connection = getDataSource().getConnection()) {
            connection.setAutoCommit(false);
            assertExceptionOccur(connection, false);
            try {
                executeUpdateWithLog(connection, "UPDATE account SET balance = 100 WHERE id = 1");
                String duplicatedKeySQL = "INSERT INTO account (id, balance, transaction_id) values (1, 11, 11)";
                executeUpdateWithLog(connection, duplicatedKeySQL);
                throw new SQLException("Expect report SQLException, but not report");
            } catch (final SQLException ignored) {
                assertExceptionOccur(connection, true);
            }
            connection.commit();
            assertAccountBalances(connection, 1);
        }
    }
    
    private void assertRollbackOnlyWithPreparedStatement() throws SQLException {
        prepare();
        try (Connection connection = getDataSource().getConnection()) {
            connection.setAutoCommit(false);
            assertExceptionOccur(connection, false);
            try {
                executeUpdateWithLog(connection, "UPDATE account SET balance = 100 WHERE id = 1");
                PreparedStatement duplicatedKeyInsertStatement = connection.prepareStatement("INSERT INTO account (id, balance, transaction_id) values (?, 11, 11)");
                duplicatedKeyInsertStatement.setInt(1, 1);
                duplicatedKeyInsertStatement.execute();
                throw new SQLException("Expect report SQLException, but not report");
            } catch (final SQLException ignored) {
                assertExceptionOccur(connection, true);
            }
            connection.commit();
            assertAccountBalances(connection, 1);
        }
    }
    
    private void prepare() throws SQLException {
        try (Connection connection = getDataSource().getConnection()) {
            executeWithLog(connection, "DELETE FROM account");
            executeWithLog(connection, "INSERT INTO account (id, balance, transaction_id) values (1, 1, 1)");
        }
    }
    
    private void assertExceptionOccur(final Connection connection, final boolean expected) {
        if (connection instanceof ShardingSphereConnection) {
            assertThat(((ShardingSphereConnection) connection).getDatabaseConnectionManager().getConnectionContext().getTransactionContext().isExceptionOccur(), is(expected));
        }
    }
}
