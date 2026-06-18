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

package org.apache.shardingsphere.test.e2e.operation.transaction.cases.alterresource;

import org.apache.shardingsphere.test.e2e.operation.transaction.cases.base.BaseTransactionTestCase;
import org.apache.shardingsphere.test.e2e.operation.transaction.engine.base.TransactionContainerComposer;
import org.apache.shardingsphere.test.e2e.operation.transaction.engine.base.TransactionTestCase;
import org.apache.shardingsphere.test.e2e.operation.transaction.engine.constants.TransactionTestConstants;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Integration test of close resource.
 */
@TransactionTestCase(adapters = TransactionTestConstants.PROXY, scenario = "closeResource")
public final class CloseResourceTestCase extends BaseTransactionTestCase {
    
    public CloseResourceTestCase(final TransactionTestCaseParameter testCaseParam) {
        super(testCaseParam);
    }
    
    @Override
    public void executeTest(final TransactionContainerComposer containerComposer) throws SQLException {
        assertCloseResource(containerComposer);
    }
    
    private void assertCloseResource(final TransactionContainerComposer containerComposer) throws SQLException {
        try (Connection connection = getDataSource().getConnection()) {
            getBaseTransactionITCase().createOriginalAccountTableRule(connection, containerComposer);
            reCreateAccountTable(connection);
            assertRollback();
            assertCommit();
        }
    }
    
    private void reCreateAccountTable(final Connection connection) throws SQLException {
        getBaseTransactionITCase().dropAccountTable(connection);
        getBaseTransactionITCase().createAccountTable(connection);
    }
    
    private void assertRollback() throws SQLException {
        try (Connection connection = getDataSource().getConnection()) {
            connection.setAutoCommit(false);
            assertTableRowCount(connection, TransactionTestConstants.ACCOUNT, 0);
            executeWithLog(connection, "INSERT INTO account(id, balance, transaction_id) VALUES(1, 1, 1),(2, 2, 2),(3, 3, 3),(4, 4, 4),(5, 5, 5),(6, 6, 6);");
            assertTableRowCount(connection, TransactionTestConstants.ACCOUNT, 6);
            connection.rollback();
            assertTableRowCount(connection, TransactionTestConstants.ACCOUNT, 0);
        }
    }
    
    private void assertCommit() throws SQLException {
        try (Connection connection = getDataSource().getConnection()) {
            connection.setAutoCommit(false);
            assertTableRowCount(connection, TransactionTestConstants.ACCOUNT, 0);
            executeWithLog(connection, "INSERT INTO account(id, balance, transaction_id) VALUES(1, 1, 1),(2, 2, 2),(3, 3, 3),(4, 4, 4),(5, 5, 5),(6, 6, 6);");
            assertTableRowCount(connection, TransactionTestConstants.ACCOUNT, 6);
            connection.commit();
            assertTableRowCount(connection, TransactionTestConstants.ACCOUNT, 6);
        }
    }
}
