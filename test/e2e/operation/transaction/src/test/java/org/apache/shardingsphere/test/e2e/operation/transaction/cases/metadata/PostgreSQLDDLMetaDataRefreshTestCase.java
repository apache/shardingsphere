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

package org.apache.shardingsphere.test.e2e.operation.transaction.cases.metadata;

import org.apache.shardingsphere.test.e2e.operation.transaction.cases.base.BaseTransactionTestCase;
import org.apache.shardingsphere.test.e2e.operation.transaction.engine.base.TransactionContainerComposer;
import org.apache.shardingsphere.test.e2e.operation.transaction.engine.base.TransactionTestCase;
import org.apache.shardingsphere.test.e2e.operation.transaction.engine.constants.TransactionTestConstants;
import org.apache.shardingsphere.transaction.api.TransactionType;
import org.awaitility.Awaitility;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * PostgreSQL DDL meta data refresh in transaction integration test.
 *
 * <p>Meta data altered by a refresh is persisted to the governance center and applied to the in-memory meta data asynchronously,
 * so the assertions below wait for that propagation instead of reading the meta data immediately after the transaction ends.</p>
 */
@TransactionTestCase(dbTypes = TransactionTestConstants.POSTGRESQL, adapters = TransactionTestConstants.PROXY, transactionTypes = TransactionType.LOCAL)
public final class PostgreSQLDDLMetaDataRefreshTestCase extends BaseTransactionTestCase {
    
    private static final String ADDED_COLUMN_NAME = "deferred_refresh_column";
    
    private static final long REFRESH_TIMEOUT_SECONDS = 30L;
    
    private static final long REFRESH_SETTLE_SECONDS = 5L;
    
    public PostgreSQLDDLMetaDataRefreshTestCase(final TransactionTestCaseParameter testCaseParam) {
        super(testCaseParam);
    }
    
    @Override
    public void executeTest(final TransactionContainerComposer containerComposer) throws SQLException {
        assertMetaDataRefreshedAfterCommit();
        assertMetaDataNotRefreshedAfterRollback();
    }
    
    private void assertMetaDataRefreshedAfterCommit() throws SQLException {
        try (Connection connection = getDataSource().getConnection()) {
            connection.setAutoCommit(false);
            executeWithLog(connection, String.format("ALTER TABLE account ADD COLUMN %s INT;", ADDED_COLUMN_NAME));
            connection.commit();
        }
        try (Connection connection = getDataSource().getConnection()) {
            Awaitility.await("Added column is not refreshed into meta data after transaction commit.")
                    .atMost(REFRESH_TIMEOUT_SECONDS, TimeUnit.SECONDS).pollInterval(500L, TimeUnit.MILLISECONDS).until(() -> containsAddedColumn(connection));
            executeWithLog(connection, String.format("ALTER TABLE account DROP COLUMN %s;", ADDED_COLUMN_NAME));
        }
    }
    
    private void assertMetaDataNotRefreshedAfterRollback() throws SQLException {
        try (Connection connection = getDataSource().getConnection()) {
            connection.setAutoCommit(false);
            executeWithLog(connection, String.format("ALTER TABLE account ADD COLUMN %s INT;", ADDED_COLUMN_NAME));
            connection.rollback();
        }
        Awaitility.await().pollDelay(REFRESH_SETTLE_SECONDS, TimeUnit.SECONDS).until(() -> true);
        try (Connection connection = getDataSource().getConnection()) {
            assertFalse(containsAddedColumn(connection), "Rolled back column is refreshed into meta data after transaction rollback.");
        }
    }
    
    private boolean containsAddedColumn(final Connection connection) throws SQLException {
        try (ResultSet resultSet = executeQueryWithLog(connection, "SELECT * FROM account;")) {
            ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
            for (int i = 1; i <= resultSetMetaData.getColumnCount(); i++) {
                if (ADDED_COLUMN_NAME.equalsIgnoreCase(resultSetMetaData.getColumnName(i))) {
                    return true;
                }
            }
            return false;
        }
    }
}
