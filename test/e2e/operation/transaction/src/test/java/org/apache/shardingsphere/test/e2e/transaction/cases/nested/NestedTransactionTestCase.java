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

package org.apache.shardingsphere.test.e2e.transaction.cases.nested;

import org.apache.shardingsphere.driver.jdbc.core.connection.ShardingSphereConnection;
import org.apache.shardingsphere.test.e2e.transaction.cases.base.BaseTransactionTestCase;
import org.apache.shardingsphere.test.e2e.transaction.engine.base.TransactionContainerComposer;
import org.apache.shardingsphere.test.e2e.transaction.engine.base.TransactionTestCase;
import org.apache.shardingsphere.test.e2e.transaction.engine.constants.TransactionTestConstants;
import org.apache.shardingsphere.transaction.api.TransactionType;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Nested transaction test case.
 */
@TransactionTestCase(transactionTypes = TransactionType.LOCAL, adapters = TransactionTestConstants.JDBC)
public final class NestedTransactionTestCase extends BaseTransactionTestCase {
    
    public NestedTransactionTestCase(final TransactionTestCaseParameter testCaseParam) {
        super(testCaseParam);
    }
    
    @Override
    protected void executeTest(final TransactionContainerComposer containerComposer) throws SQLException {
        assertOuterCommitAndInnerRollback();
        assertOuterRollbackAndInnerRollback();
        assertOuterCommitAndInnerCommit();
        assertOuterRollbackAndInnerCommit();
    }
    
    private void assertOuterCommitAndInnerRollback() throws SQLException {
        try (
                ShardingSphereConnection connection = (ShardingSphereConnection) getDataSource().getConnection();
                Connection queryConnection = getDataSource().getConnection()) {
            assertFalse(connection.getDatabaseConnectionManager().getConnectionTransaction().isHoldTransaction(connection.getAutoCommit()));
            connection.setAutoCommit(false);
            executeWithLog(connection, "INSERT INTO account (id, balance, transaction_id) VALUES (1, 1, 1), (2, 2, 2)");
            assertTrue(connection.getDatabaseConnectionManager().getConnectionTransaction().isHoldTransaction(connection.getAutoCommit()));
            requiresNewTransactionRollback();
            executeWithLog(connection, "INSERT INTO account (id, balance, transaction_id) VALUES (7, 7, 7), (8, 8, 8)");
            assertTrue(connection.getDatabaseConnectionManager().getConnectionTransaction().isHoldTransaction(connection.getAutoCommit()));
            connection.commit();
            assertAccountBalances(queryConnection, 1, 2, 3, 4, 7, 8);
            connection.setAutoCommit(true);
            executeWithLog(connection, "DELETE FROM ACCOUNT");
        }
    }
    
    private void assertOuterRollbackAndInnerRollback() throws SQLException {
        try (
                ShardingSphereConnection connection = (ShardingSphereConnection) getDataSource().getConnection();
                Connection queryConnection = getDataSource().getConnection()) {
            assertFalse(connection.getDatabaseConnectionManager().getConnectionTransaction().isHoldTransaction(connection.getAutoCommit()));
            connection.setAutoCommit(false);
            executeWithLog(connection, "INSERT INTO account (id, balance, transaction_id) VALUES (1, 1, 1), (2, 2, 2)");
            assertTrue(connection.getDatabaseConnectionManager().getConnectionTransaction().isHoldTransaction(connection.getAutoCommit()));
            requiresNewTransactionRollback();
            executeWithLog(connection, "INSERT INTO account (id, balance, transaction_id) VALUES (7, 7, 7), (8, 8, 8)");
            assertTrue(connection.getDatabaseConnectionManager().getConnectionTransaction().isHoldTransaction(connection.getAutoCommit()));
            connection.rollback();
            assertAccountBalances(queryConnection, 3, 4);
            connection.setAutoCommit(true);
            executeWithLog(connection, "DELETE FROM ACCOUNT");
        }
    }
    
    private void assertOuterCommitAndInnerCommit() throws SQLException {
        try (
                ShardingSphereConnection connection = (ShardingSphereConnection) getDataSource().getConnection();
                Connection queryConnection = getDataSource().getConnection()) {
            assertFalse(connection.getDatabaseConnectionManager().getConnectionTransaction().isHoldTransaction(connection.getAutoCommit()));
            connection.setAutoCommit(false);
            executeWithLog(connection, "INSERT INTO account (id, balance, transaction_id) VALUES (1, 1, 1), (2, 2, 2)");
            assertTrue(connection.getDatabaseConnectionManager().getConnectionTransaction().isHoldTransaction(connection.getAutoCommit()));
            requiresNewTransactionCommit();
            executeWithLog(connection, "INSERT INTO account (id, balance, transaction_id) VALUES (7, 7, 7), (8, 8, 8)");
            assertTrue(connection.getDatabaseConnectionManager().getConnectionTransaction().isHoldTransaction(connection.getAutoCommit()));
            connection.commit();
            assertAccountBalances(queryConnection, 1, 2, 3, 4, 5, 6, 7, 8);
            connection.setAutoCommit(true);
            executeWithLog(connection, "DELETE FROM ACCOUNT");
        }
    }
    
    private void assertOuterRollbackAndInnerCommit() throws SQLException {
        try (
                ShardingSphereConnection connection = (ShardingSphereConnection) getDataSource().getConnection();
                Connection queryConnection = getDataSource().getConnection()) {
            assertFalse(connection.getDatabaseConnectionManager().getConnectionTransaction().isHoldTransaction(connection.getAutoCommit()));
            connection.setAutoCommit(false);
            executeWithLog(connection, "INSERT INTO account (id, balance, transaction_id) VALUES (1, 1, 1), (2, 2, 2)");
            assertTrue(connection.getDatabaseConnectionManager().getConnectionTransaction().isHoldTransaction(connection.getAutoCommit()));
            requiresNewTransactionCommit();
            executeWithLog(connection, "INSERT INTO account (id, balance, transaction_id) VALUES (7, 7, 7), (8, 8, 8)");
            assertTrue(connection.getDatabaseConnectionManager().getConnectionTransaction().isHoldTransaction(connection.getAutoCommit()));
            connection.rollback();
            assertAccountBalances(queryConnection, 3, 4, 5, 6);
            connection.setAutoCommit(true);
            executeWithLog(connection, "DELETE FROM ACCOUNT");
        }
    }
    
    private void requiresNewTransactionRollback() throws SQLException {
        try (ShardingSphereConnection connection = (ShardingSphereConnection) getDataSource().getConnection()) {
            assertFalse(connection.getDatabaseConnectionManager().getConnectionTransaction().isHoldTransaction(connection.getAutoCommit()));
            executeWithLog(connection, "INSERT INTO account (id, balance, transaction_id) VALUES (3, 3, 3), (4, 4, 4)");
            connection.setAutoCommit(false);
            executeWithLog(connection, "INSERT INTO account (id, balance, transaction_id) VALUES (5, 5, 5), (6, 6, 6)");
            assertTrue(connection.getDatabaseConnectionManager().getConnectionTransaction().isHoldTransaction(connection.getAutoCommit()));
            connection.rollback();
        }
    }
    
    private void requiresNewTransactionCommit() throws SQLException {
        try (ShardingSphereConnection connection = (ShardingSphereConnection) getDataSource().getConnection()) {
            assertFalse(connection.getDatabaseConnectionManager().getConnectionTransaction().isHoldTransaction(connection.getAutoCommit()));
            executeWithLog(connection, "INSERT INTO account (id, balance, transaction_id) VALUES (3, 3, 3), (4, 4, 4)");
            connection.setAutoCommit(false);
            executeWithLog(connection, "INSERT INTO account (id, balance, transaction_id) VALUES (5, 5, 5), (6, 6, 6)");
            assertTrue(connection.getDatabaseConnectionManager().getConnectionTransaction().isHoldTransaction(connection.getAutoCommit()));
            connection.commit();
        }
    }
}
