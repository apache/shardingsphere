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
import org.apache.shardingsphere.integration.transaction.cases.base.BaseTransactionTestCase;
import org.apache.shardingsphere.integration.transaction.engine.base.BaseTransactionITCase;
import org.apache.shardingsphere.integration.transaction.engine.base.TransactionTestCase;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Broadcast table transaction integration test.
 */
@TransactionTestCase
public final class BroadcastTableTransactionTestCase extends BaseTransactionTestCase {
    
    private static final String T_ADDRESS = "t_address";
    
    public BroadcastTableTransactionTestCase(final BaseTransactionITCase baseTransactionITCase, final DataSource dataSource) {
        super(baseTransactionITCase, dataSource);
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
    protected void executeTest() {
        rollback();
        commit();
    }
    
    private void init() throws SQLException {
        Connection conn = getDataSource().getConnection();
        executeWithLog(conn, "delete from t_address;");
        assertTableRowCount(conn, T_ADDRESS, 0);
    }
    
    private void commit() throws SQLException {
        Connection conn = getDataSource().getConnection();
        conn.setAutoCommit(false);
        executeWithLog(conn, "delete from t_address;");
        assertTableRowCount(conn, T_ADDRESS, 0);
        executeWithLog(conn, "INSERT INTO t_address (id, code, address) VALUES (1, '1', 'nanjing');");
        assertTableRowCount(conn, T_ADDRESS, 1);
        conn.commit();
    }
    
    private void rollback() throws SQLException {
        Connection conn = getDataSource().getConnection();
        conn.setAutoCommit(false);
        executeWithLog(conn, "delete from t_address;");
        assertTableRowCount(conn, T_ADDRESS, 0);
        executeWithLog(conn, "INSERT INTO t_address (id, code, address) VALUES (1, '1', 'nanjing');");
        assertTableRowCount(conn, T_ADDRESS, 1);
        conn.commit();
    }
}
