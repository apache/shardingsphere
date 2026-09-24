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
import org.apache.shardingsphere.test.e2e.operation.transaction.engine.constants.TransactionTestConstants;
import org.apache.shardingsphere.transaction.api.TransactionType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Batch transaction commit and rollback integration test.
 */
@TransactionTestCase(adapters = TransactionTestConstants.JDBC, transactionTypes = {TransactionType.LOCAL, TransactionType.XA})
public final class BatchCommitAndRollbackTestCase extends BaseTransactionTestCase {
    
    private static final String INSERT_SQL = "INSERT INTO account(id, balance, transaction_id) VALUES(?, ?, ?)";
    
    public BatchCommitAndRollbackTestCase(final TransactionTestCaseParameter testCaseParam) {
        super(testCaseParam);
    }
    
    @Override
    public void executeTest(final TransactionContainerComposer containerComposer) throws SQLException {
        assertRollback();
        assertCommit();
    }
    
    private void assertRollback() throws SQLException {
        try (
                Connection connection = getDataSource().getConnection();
                PreparedStatement statement = connection.prepareStatement(INSERT_SQL)) {
            connection.setAutoCommit(false);
            addBatch(statement);
            assertThat(statement.executeBatch(), is(new int[]{1, 1}));
            assertAccountBalances(connection, 1, 2);
            connection.rollback();
            assertAccountBalances(connection);
            connection.setAutoCommit(true);
        }
        try (Connection connection = getDataSource().getConnection()) {
            assertAccountBalances(connection);
        }
    }
    
    private void assertCommit() throws SQLException {
        try (
                Connection connection = getDataSource().getConnection();
                PreparedStatement statement = connection.prepareStatement(INSERT_SQL)) {
            connection.setAutoCommit(false);
            addBatch(statement);
            assertThat(statement.executeBatch(), is(new int[]{1, 1}));
            assertAccountBalances(connection, 1, 2);
            connection.commit();
            assertAccountBalances(connection, 1, 2);
            connection.setAutoCommit(true);
        }
        try (Connection connection = getDataSource().getConnection()) {
            assertAccountBalances(connection, 1, 2);
        }
    }
    
    private void addBatch(final PreparedStatement statement) throws SQLException {
        for (int each = 1; each <= 2; each++) {
            statement.setInt(1, each);
            statement.setInt(2, each);
            statement.setInt(3, each);
            statement.addBatch();
        }
    }
}
