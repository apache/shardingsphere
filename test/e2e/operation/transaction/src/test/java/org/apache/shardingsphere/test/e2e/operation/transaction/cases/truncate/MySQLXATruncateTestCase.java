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

package org.apache.shardingsphere.test.e2e.operation.transaction.cases.truncate;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.database.exception.core.exception.transaction.TableModifyInTransactionException;
import org.apache.shardingsphere.test.e2e.operation.transaction.cases.base.BaseTransactionTestCase;
import org.apache.shardingsphere.test.e2e.operation.transaction.engine.base.TransactionContainerComposer;
import org.apache.shardingsphere.test.e2e.operation.transaction.engine.base.TransactionTestCase;
import org.apache.shardingsphere.test.e2e.operation.transaction.engine.constants.TransactionTestConstants;
import org.apache.shardingsphere.transaction.api.TransactionType;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * MySQL truncate XA transaction integration test.
 */
@TransactionTestCase(dbTypes = TransactionTestConstants.MYSQL, transactionTypes = TransactionType.XA)
@Slf4j
public final class MySQLXATruncateTestCase extends BaseTransactionTestCase {
    
    public MySQLXATruncateTestCase(final TransactionTestCaseParameter testCaseParam) {
        super(testCaseParam);
    }
    
    @Override
    protected void beforeTest() throws SQLException {
        super.beforeTest();
        prepare();
    }
    
    private void prepare() throws SQLException {
        try (Connection connection = getDataSource().getConnection()) {
            executeWithLog(connection, "DELETE FROM account;");
            executeWithLog(connection, "INSERT INTO account(id, balance, transaction_id) VALUES(1, 1, 1),(2, 2, 2),(3, 3, 3),(4, 4, 4),(5, 5, 5),(6, 6, 6),(7, 7, 7),(8, 8, 8);");
        }
    }
    
    @Override
    public void executeTest(final TransactionContainerComposer containerComposer) throws SQLException {
        assertTruncateInMySQLXATransaction();
    }
    
    private void assertTruncateInMySQLXATransaction() throws SQLException {
        try (Connection connection = getDataSource().getConnection()) {
            connection.setAutoCommit(false);
            assertAccountRowCount(connection, 8);
            try {
                connection.createStatement().execute("TRUNCATE account");
                fail("Expect exception, but no exception report.");
            } catch (final TableModifyInTransactionException ex) {
                log.info("Exception for expected in Proxy: {}", ex.getMessage());
            } catch (final SQLException ex) {
                log.info("Exception for expected in JDBC: {}", ex.getMessage());
            } finally {
                connection.rollback();
            }
        }
    }
}
