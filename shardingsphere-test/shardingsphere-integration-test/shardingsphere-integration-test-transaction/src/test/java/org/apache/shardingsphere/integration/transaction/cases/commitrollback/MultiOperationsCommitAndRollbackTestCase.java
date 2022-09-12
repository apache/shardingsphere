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

import com.google.common.base.Preconditions;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.integration.transaction.cases.base.BaseTransactionTestCase;
import org.apache.shardingsphere.integration.transaction.engine.base.BaseTransactionITCase;
import org.apache.shardingsphere.integration.transaction.engine.base.TransactionTestCase;
import org.apache.shardingsphere.integration.transaction.engine.constants.TransactionTestConstants;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Integration test of multiple operations in one transaction.
 */
@Slf4j
@TransactionTestCase
public final class MultiOperationsCommitAndRollbackTestCase extends BaseTransactionTestCase {
    
    public MultiOperationsCommitAndRollbackTestCase(final BaseTransactionITCase baseTransactionITCase, final DataSource dataSource) {
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
        assertTableRowCount(conn, TransactionTestConstants.ACCOUNT, 0);
        executeWithLog(conn, "insert into account(id, balance, transaction_id) values(1, 1, 1);");
        executeWithLog(conn, "insert into account(id, balance, transaction_id) values(2, 2, 2);");
        executeUpdateWithLog(conn, "update account set balance=3, transaction_id=3 where id=2;");
        assertQueryAccount(conn, 1, 3);
        conn.rollback();
        assertTableRowCount(conn, TransactionTestConstants.ACCOUNT, 0);
        assertQueryAccount(conn, 1, 2);
    }
    
    private void assertCommit() throws SQLException {
        Connection conn = getDataSource().getConnection();
        conn.setAutoCommit(false);
        assertTableRowCount(conn, TransactionTestConstants.ACCOUNT, 0);
        executeWithLog(conn, "insert into account(id, balance, transaction_id) values(1, 1, 1);");
        executeWithLog(conn, "insert into account(id, balance, transaction_id) values(2, 2, 2);");
        executeUpdateWithLog(conn, "update account set balance=3, transaction_id=3 where id=2;");
        assertQueryAccount(conn, 1, 3);
        conn.commit();
        assertTableRowCount(conn, TransactionTestConstants.ACCOUNT, 2);
        assertQueryAccount(conn, 1, 3);
    }
    
    private void assertQueryAccount(final Connection conn, final int... expectedBalances) throws SQLException {
        Preconditions.checkArgument(2 == expectedBalances.length);
        Statement queryStatement = conn.createStatement();
        ResultSet rs = queryStatement.executeQuery("select * from account;");
        while (rs.next()) {
            int id = rs.getInt("id");
            int actualBalance = rs.getInt("balance");
            if (1 == id) {
                assertBalance(actualBalance, expectedBalances[0]);
            }
            if (2 == id) {
                assertBalance(actualBalance, expectedBalances[1]);
            }
        }
    }
    
    private void assertBalance(final int actual, final int expected) {
        assertThat(String.format("Balance is %s, should be %s.", actual, expected), actual, is(expected));
    }
}
