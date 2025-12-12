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

/**
 * Multi-table transaction commit and rollback integration test.
 */
@TransactionTestCase
public final class MultiTableCommitAndRollbackTestCase extends BaseTransactionTestCase {
    
    private static final String T_ORDER = "t_order";
    
    private static final String T_ORDER_ITEM = "t_order_item";
    
    public MultiTableCommitAndRollbackTestCase(final TransactionTestCaseParameter testCaseParam) {
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
            assertTableRowCount(connection, T_ORDER, 0);
            assertTableRowCount(connection, T_ORDER_ITEM, 0);
            executeSqlListWithLog(connection,
                    "INSERT INTO t_order (order_id, user_id, status) VALUES (1, 1, '1');",
                    "INSERT INTO t_order (order_id, user_id, status) VALUES (2, 2, '2');",
                    "INSERT INTO t_order_item (item_id, order_id, user_id, status) VALUES (1, 1, 1, '1');",
                    "INSERT INTO t_order_item (item_id, order_id, user_id, status) VALUES (2, 2, 2, '2');");
            assertTableRowCount(connection, T_ORDER, 2);
            assertTableRowCount(connection, T_ORDER_ITEM, 2);
            connection.rollback();
            assertTableRowCount(connection, T_ORDER, 0);
            assertTableRowCount(connection, T_ORDER_ITEM, 0);
        }
    }
    
    private void assertCommit() throws SQLException {
        try (Connection connection = getDataSource().getConnection()) {
            connection.setAutoCommit(false);
            assertTableRowCount(connection, T_ORDER, 0);
            assertTableRowCount(connection, T_ORDER_ITEM, 0);
            executeSqlListWithLog(connection,
                    "INSERT INTO t_order (order_id, user_id, status) VALUES (1, 1, '1');",
                    "INSERT INTO t_order (order_id, user_id, status) VALUES (2, 2, '2');",
                    "INSERT INTO t_order_item (item_id, order_id, user_id, status) VALUES (1, 1, 1, '1');",
                    "INSERT INTO t_order_item (item_id, order_id, user_id, status) VALUES (2, 2, 2, '2');");
            assertTableRowCount(connection, T_ORDER, 2);
            assertTableRowCount(connection, T_ORDER_ITEM, 2);
            connection.commit();
            assertTableRowCount(connection, T_ORDER, 2);
            assertTableRowCount(connection, T_ORDER_ITEM, 2);
        }
    }
}
