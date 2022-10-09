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

package org.apache.shardingsphere.integration.transaction.cases.alterresource;

import org.apache.shardingsphere.integration.transaction.cases.base.BaseTransactionTestCase;
import org.apache.shardingsphere.integration.transaction.engine.base.BaseTransactionITCase;
import org.apache.shardingsphere.integration.transaction.engine.base.TransactionTestCase;
import org.apache.shardingsphere.integration.transaction.engine.constants.TransactionTestConstants;
import org.apache.shardingsphere.test.integration.env.container.atomic.constants.AdapterContainerConstants;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Integration test of add resource.
 */
@TransactionTestCase(adapters = AdapterContainerConstants.PROXY, group = "addResource")
public final class AddResourceTestCase extends BaseTransactionTestCase {
    
    public AddResourceTestCase(final BaseTransactionITCase baseTransactionITCase, final DataSource dataSource) {
        super(baseTransactionITCase, dataSource);
    }
    
    @Override
    public void executeTest() throws SQLException {
        assertAddResource();
    }
    
    private void assertAddResource() throws SQLException {
        Connection conn = getDataSource().getConnection();
        getBaseTransactionITCase().addResource(conn, "transaction_it_2");
        createThreeDataSourceAccountTableRule(conn);
        reCreateAccountTable(conn);
        assertRollback();
        assertCommit();
        conn.close();
    }
    
    private void createThreeDataSourceAccountTableRule(final Connection connection) throws SQLException {
        executeWithLog(connection, "DROP SHARDING TABLE RULE account;");
        executeWithLog(connection, getBaseTransactionITCase().getCommonSQLCommand().getCreateThreeDataSourceAccountTableRule());
        int ruleCount = countWithLog(connection, "SHOW SHARDING TABLE RULES FROM sharding_db;");
        assertThat(ruleCount, is(3));
    }
    
    private void reCreateAccountTable(final Connection conn) throws SQLException {
        getBaseTransactionITCase().dropAccountTable(conn);
        getBaseTransactionITCase().createAccountTable(conn);
    }
    
    private void assertRollback() throws SQLException {
        Connection conn = getDataSource().getConnection();
        conn.setAutoCommit(false);
        assertTableRowCount(conn, TransactionTestConstants.ACCOUNT, 0);
        executeWithLog(conn, "insert into account(id, BALANCE, TRANSACTION_ID) values(1, 1, 1),(2, 2, 2),(3, 3, 3),(4, 4, 4),(5, 5, 5),(6, 6, 6);");
        assertTableRowCount(conn, TransactionTestConstants.ACCOUNT, 6);
        conn.rollback();
        assertTableRowCount(conn, TransactionTestConstants.ACCOUNT, 0);
    }
    
    private void assertCommit() throws SQLException {
        Connection conn = getDataSource().getConnection();
        conn.setAutoCommit(false);
        assertTableRowCount(conn, TransactionTestConstants.ACCOUNT, 0);
        executeWithLog(conn, "insert into account(id, BALANCE, TRANSACTION_ID) values(1, 1, 1),(2, 2, 2),(3, 3, 3),(4, 4, 4),(5, 5, 5),(6, 6, 6);");
        assertTableRowCount(conn, TransactionTestConstants.ACCOUNT, 6);
        conn.commit();
        assertTableRowCount(conn, TransactionTestConstants.ACCOUNT, 6);
    }
}
