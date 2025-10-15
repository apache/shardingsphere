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

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;

/**
 * Base savepoint transaction integration test.
 */
public abstract class BaseSavePointTestCase extends BaseTransactionTestCase {
    
    protected BaseSavePointTestCase(final TransactionTestCaseParameter testCaseParam) {
        super(testCaseParam);
    }
    
    void assertRollbackToSavepoint() throws SQLException {
        try (Connection connection = getDataSource().getConnection()) {
            connection.setAutoCommit(false);
            assertAccountRowCount(connection, 0);
            executeWithLog(connection, "INSERT INTO account(id, balance, transaction_id) VALUES(1, 1, 1);");
            Savepoint savepoint = connection.setSavepoint("point1");
            executeSQLInSavepoint(connection);
            connection.rollback(savepoint);
            assertAccountRowCount(connection, 1);
            Savepoint savepointWithoutName = connection.setSavepoint();
            executeSQLInSavepoint(connection);
            connection.rollback(savepointWithoutName);
            assertAccountRowCount(connection, 1);
            connection.commit();
            assertAccountRowCount(connection, 1);
        }
    }
    
    private void executeSQLInSavepoint(final Connection connection) throws SQLException {
        assertAccountRowCount(connection, 1);
        executeWithLog(connection, "INSERT INTO account(id, balance, transaction_id) VALUES(2, 2, 2);");
        assertAccountRowCount(connection, 2);
    }
    
    void assertReleaseSavepoint() throws SQLException {
        try (Connection connection = getDataSource().getConnection()) {
            connection.setAutoCommit(false);
            assertAccountRowCount(connection, 1);
            executeWithLog(connection, "INSERT INTO account(id, balance, transaction_id) VALUES(2, 2, 2);");
            final Savepoint savepoint = connection.setSavepoint("point2");
            assertAccountRowCount(connection, 2);
            executeWithLog(connection, "INSERT INTO account(id, balance, transaction_id) VALUES(3, 3, 3);");
            assertAccountRowCount(connection, 3);
            connection.releaseSavepoint(savepoint);
            assertAccountRowCount(connection, 3);
            connection.commit();
            assertAccountRowCount(connection, 3);
        }
        try (Connection connection = getDataSource().getConnection()) {
            connection.setAutoCommit(false);
            assertAccountRowCount(connection, 3);
            executeWithLog(connection, "INSERT INTO account(id, balance, transaction_id) VALUES(4, 4, 4);");
            executeWithLog(connection, "SAVEPOINT point1");
            assertAccountRowCount(connection, 4);
            executeWithLog(connection, "INSERT INTO account(id, balance, transaction_id) VALUES(5, 5, 5);");
            assertAccountRowCount(connection, 5);
            executeWithLog(connection, "RELEASE SAVEPOINT point1");
            assertAccountRowCount(connection, 5);
            connection.commit();
        }
    }
}
