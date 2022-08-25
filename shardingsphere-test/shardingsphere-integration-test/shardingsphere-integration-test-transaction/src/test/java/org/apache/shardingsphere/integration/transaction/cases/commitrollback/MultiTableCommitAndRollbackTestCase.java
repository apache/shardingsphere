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

package org.apache.shardingsphere.integration.transaction.cases.commitrollback;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.integration.transaction.cases.base.BaseTransactionTestCase;
import org.apache.shardingsphere.integration.transaction.engine.base.BaseTransactionITCase;
import org.apache.shardingsphere.integration.transaction.engine.base.TransactionTestCase;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Multi-table transaction commit and rollback integration test.
 */
@Slf4j
@TransactionTestCase
public final class MultiTableCommitAndRollbackTestCase extends BaseTransactionTestCase {
    
    private static final String T_ORDER = "t_order";
    
    private static final String T_ORDER_ITEM = "t_order_item";
    
    public MultiTableCommitAndRollbackTestCase(final BaseTransactionITCase baseTransactionITCase, final DataSource dataSource) {
        super(baseTransactionITCase, dataSource);
    }
    
    @Override
    @SneakyThrows
    public void executeTest() {
        assertRollback();
        assertCommit();
    }
    
    private void assertRollback() throws SQLException {
        Connection conn = getDataSource().getConnection();
        conn.setAutoCommit(false);
        assertTableRowCount(conn, T_ORDER, 0);
        assertTableRowCount(conn, T_ORDER_ITEM, 0);
        executeSqlListWithLog(conn,
                "INSERT INTO t_order (order_id, user_id, status) VALUES (1, 1, '1');",
                "INSERT INTO t_order (order_id, user_id, status) VALUES (2, 2, '2');",
                "INSERT INTO t_order_item (item_id, order_id, user_id, status) VALUES (1, 1, 1, '1');",
                "INSERT INTO t_order_item (item_id, order_id, user_id, status) VALUES (2, 2, 2, '2');");
        assertTableRowCount(conn, T_ORDER, 2);
        assertTableRowCount(conn, T_ORDER_ITEM, 2);
        conn.rollback();
        assertTableRowCount(conn, T_ORDER, 0);
        assertTableRowCount(conn, T_ORDER_ITEM, 0);
    }
    
    private void assertCommit() throws SQLException {
        Connection conn = getDataSource().getConnection();
        conn.setAutoCommit(false);
        assertTableRowCount(conn, T_ORDER, 0);
        assertTableRowCount(conn, T_ORDER_ITEM, 0);
        executeSqlListWithLog(conn,
                "INSERT INTO t_order (order_id, user_id, status) VALUES (1, 1, '1');",
                "INSERT INTO t_order (order_id, user_id, status) VALUES (2, 2, '2');",
                "INSERT INTO t_order_item (item_id, order_id, user_id, status) VALUES (1, 1, 1, '1');",
                "INSERT INTO t_order_item (item_id, order_id, user_id, status) VALUES (2, 2, 2, '2');");
        assertTableRowCount(conn, T_ORDER, 2);
        assertTableRowCount(conn, T_ORDER_ITEM, 2);
        conn.commit();
        assertTableRowCount(conn, T_ORDER, 2);
        assertTableRowCount(conn, T_ORDER_ITEM, 2);
    }
    
}
