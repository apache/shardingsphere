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

import lombok.SneakyThrows;
import org.apache.shardingsphere.test.e2e.operation.transaction.cases.base.BaseTransactionTestCase;
import org.apache.shardingsphere.test.e2e.operation.transaction.engine.base.TransactionContainerComposer;
import org.apache.shardingsphere.test.e2e.operation.transaction.engine.base.TransactionTestCase;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Broadcast table transaction integration test.
 */
@TransactionTestCase
public final class BroadcastTableTransactionTestCase extends BaseTransactionTestCase {
    
    private static final String T_ADDRESS = "t_address";
    
    public BroadcastTableTransactionTestCase(final TransactionTestCaseParameter testCaseParam) {
        super(testCaseParam);
    }
    
    @Override
    protected void beforeTest() throws SQLException {
        super.beforeTest();
        init();
    }
    
    @Override
    protected void afterTest() throws SQLException {
        super.afterTest();
        init();
    }
    
    @Override
    @SneakyThrows(SQLException.class)
    protected void executeTest(final TransactionContainerComposer containerComposer) {
        rollback();
        commit();
    }
    
    private void init() throws SQLException {
        try (Connection connection = getDataSource().getConnection()) {
            executeWithLog(connection, "DELETE FROM t_address;");
            assertTableRowCount(connection, T_ADDRESS, 0);
        }
    }
    
    private void commit() throws SQLException {
        try (Connection connection = getDataSource().getConnection()) {
            connection.setAutoCommit(false);
            executeWithLog(connection, "DELETE FROM t_address;");
            assertTableRowCount(connection, T_ADDRESS, 0);
            executeWithLog(connection, "INSERT INTO t_address (id, code, address) VALUES (1, '1', 'nanjing');");
            assertTableRowCount(connection, T_ADDRESS, 1);
            connection.commit();
        }
    }
    
    private void rollback() throws SQLException {
        try (Connection connection = getDataSource().getConnection()) {
            connection.setAutoCommit(false);
            executeWithLog(connection, "DELETE FROM t_address;");
            assertTableRowCount(connection, T_ADDRESS, 0);
            executeWithLog(connection, "INSERT INTO t_address (id, code, address) VALUES (1, '1', 'nanjing');");
            assertTableRowCount(connection, T_ADDRESS, 1);
            connection.rollback();
        }
    }
}
