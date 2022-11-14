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

package org.apache.shardingsphere.integration.transaction.cases.truncate;

import org.apache.shardingsphere.integration.transaction.cases.base.BaseTransactionTestCase;
import org.apache.shardingsphere.integration.transaction.engine.base.BaseTransactionITCase;
import org.apache.shardingsphere.integration.transaction.engine.base.TransactionTestCase;
import org.apache.shardingsphere.integration.transaction.engine.constants.TransactionTestConstants;
import org.apache.shardingsphere.transaction.core.TransactionType;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * MySQL truncate local transaction integration test.
 */
@TransactionTestCase(dbTypes = {TransactionTestConstants.MYSQL}, transactionTypes = {TransactionType.LOCAL})
public final class MySQLLocalTruncateTestCase extends BaseTransactionTestCase {
    
    public MySQLLocalTruncateTestCase(final BaseTransactionITCase baseTransactionITCase, final DataSource dataSource) {
        super(baseTransactionITCase, dataSource);
    }
    
    @Override
    public void executeTest() throws SQLException {
        assertTruncateRollback();
        assertTruncateCommit();
    }
    
    private void assertTruncateRollback() throws SQLException {
        prepare();
        Connection connection = getDataSource().getConnection();
        connection.setAutoCommit(false);
        assertAccountRowCount(connection, 8);
        executeWithLog(connection, "truncate account;");
        assertAccountRowCount(connection, 0);
        connection.rollback();
        // Expected truncate operation cannot be rolled back in MySQL local transaction
        assertAccountRowCount(connection, 0);
        connection.close();
    }
    
    private void assertTruncateCommit() throws SQLException {
        prepare();
        Connection connection = getDataSource().getConnection();
        connection.setAutoCommit(false);
        assertAccountRowCount(connection, 8);
        executeWithLog(connection, "truncate account;");
        assertAccountRowCount(connection, 0);
        connection.commit();
        assertAccountRowCount(connection, 0);
        connection.close();
    }
    
    private void prepare() throws SQLException {
        Connection connection = getDataSource().getConnection();
        executeWithLog(connection, "delete from account;");
        executeWithLog(connection, "insert into account(id, balance, transaction_id) values(1, 1, 1),(2, 2, 2),(3, 3, 3),(4, 4, 4),(5, 5, 5),(6, 6, 6),(7, 7, 7),(8, 8, 8);");
        connection.close();
    }
}
