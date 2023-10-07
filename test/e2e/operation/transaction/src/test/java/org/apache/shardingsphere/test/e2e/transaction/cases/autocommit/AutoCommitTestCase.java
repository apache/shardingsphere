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

package org.apache.shardingsphere.test.e2e.transaction.cases.autocommit;

import org.apache.shardingsphere.test.e2e.transaction.cases.base.BaseTransactionTestCase;

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
            connection.setAutoCommit(false);
            executeWithLog(connection, "DELETE FROM account");
            assertFalse(connection.getAutoCommit());
            executeWithLog(connection, "INSERT INTO account VALUES (1, 1, 1)");
            connection.commit();
            assertFalse(connection.getAutoCommit());
            executeUpdateWithLog(connection, "INSERT INTO account VALUES (2, 2, 2)");
            connection.commit();
            assertFalse(connection.getAutoCommit());
            executeWithLog(connection, "INSERT INTO account VALUES (3, 3, 3)");
            connection.rollback();
            assertFalse(connection.getAutoCommit());
            assertAccountRowCount(connection, 2);
            connection.setAutoCommit(true);
            assertTrue(connection.getAutoCommit());
            executeWithLog(connection, "INSERT INTO account VALUES (4, 4, 4)");
            assertAccountRowCount(connection, 3);
        }
    }
    
    protected void assertAutoCommitWithPrepareStatement() throws SQLException {
        try (Connection connection = getDataSource().getConnection()) {
            connection.setAutoCommit(false);
            executeWithLog(connection, "DELETE FROM account");
            assertFalse(connection.getAutoCommit());
            PreparedStatement prepareStatement = connection.prepareStatement("INSERT INTO account VALUES(?, ?, ?)");
            setPrepareStatementParameters(prepareStatement, 1);
            prepareStatement.execute();
            connection.commit();
            assertFalse(connection.getAutoCommit());
            setPrepareStatementParameters(prepareStatement, 2);
            prepareStatement.executeUpdate();
            connection.commit();
            assertFalse(connection.getAutoCommit());
            setPrepareStatementParameters(prepareStatement, 3);
            prepareStatement.execute();
            connection.rollback();
            assertFalse(connection.getAutoCommit());
            assertAccountRowCount(connection, 2);
            connection.setAutoCommit(true);
            assertTrue(connection.getAutoCommit());
            setPrepareStatementParameters(prepareStatement, 4);
            prepareStatement.execute();
            assertAccountRowCount(connection, 3);
        }
    }
    
    private void setPrepareStatementParameters(final PreparedStatement prepareStatement, final int value) throws SQLException {
        prepareStatement.setInt(1, value);
        prepareStatement.setInt(2, value);
        prepareStatement.setInt(3, value);
    }
}
