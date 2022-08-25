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
import java.sql.Statement;

/**
 * Single table transaction commit and rollback integration test.
 */
@TransactionTestCase
public final class SingleTableCommitAndRollbackTestCase extends BaseTransactionTestCase {
    
    public SingleTableCommitAndRollbackTestCase(final BaseTransactionITCase baseTransactionITCase, final DataSource dataSource) {
        super(baseTransactionITCase, dataSource);
    }
    
    @Override
    @SneakyThrows
    public void executeTest() {
        assertRollback();
        assertCommit();
    }
    
    @SneakyThrows(SQLException.class)
    private void assertRollback() {
        Connection conn = getDataSource().getConnection();
        conn.setAutoCommit(false);
        assertAccountRowCount(conn, 0);
        Statement std1 = conn.createStatement();
        std1.execute("insert into account(id, balance, transaction_id) values(1, 1, 1);");
        assertAccountRowCount(conn, 1);
        conn.rollback();
        assertAccountRowCount(conn, 0);
    }
    
    @SneakyThrows(SQLException.class)
    public void assertCommit() {
        Connection conn = getDataSource().getConnection();
        conn.setAutoCommit(false);
        assertAccountRowCount(conn, 0);
        Statement std1 = conn.createStatement();
        std1.execute("insert into account(id, balance, transaction_id) values(1, 1, 1);");
        assertAccountRowCount(conn, 1);
        conn.commit();
        assertAccountRowCount(conn, 1);
    }
}
