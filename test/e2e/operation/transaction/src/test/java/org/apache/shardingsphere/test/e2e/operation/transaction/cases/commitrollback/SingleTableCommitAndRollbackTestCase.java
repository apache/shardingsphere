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
import java.sql.Statement;

/**
 * Single table transaction commit and rollback integration test.
 */
@TransactionTestCase
public final class SingleTableCommitAndRollbackTestCase extends BaseTransactionTestCase {
    
    public SingleTableCommitAndRollbackTestCase(final TransactionTestCaseParameter testCaseParam) {
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
            Statement statement = connection.createStatement();
            statement.execute("INSERT INTO account(id, balance, transaction_id) VALUES(1, 1, 1);");
            assertAccountRowCount(connection, 1);
            connection.rollback();
            assertAccountRowCount(connection, 0);
        }
    }
    
    private void assertCommit() throws SQLException {
        try (Connection connection = getDataSource().getConnection()) {
            connection.setAutoCommit(false);
            assertAccountRowCount(connection, 0);
            Statement statement = connection.createStatement();
            statement.execute("INSERT INTO account(id, balance, transaction_id) VALUES(1, 1, 1);");
            assertAccountRowCount(connection, 1);
            connection.commit();
            assertAccountRowCount(connection, 1);
        }
    }
}
