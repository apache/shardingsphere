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
 * <p>A meta data refresh is persisted to the governance center and applied to the in-memory meta data asynchronously, so every
 * assertion below waits for that propagation. Each phase uses its own column name so that one phase never depends on the
 * propagation of another phase's DDL.</p>
 */
@TransactionTestCase(dbTypes = TransactionTestConstants.POSTGRESQL, adapters = TransactionTestConstants.PROXY, transactionTypes = {TransactionType.LOCAL, TransactionType.XA})
public final class PostgreSQLDDLMetaDataRefreshTestCase extends BaseTransactionTestCase {
    
    private static final String COMMITTED_COLUMN_NAME = "deferred_refresh_committed_column";
    
    private static final String ROLLED_BACK_COLUMN_NAME = "deferred_refresh_rolled_back_column";
    
    private static final String CREATED_TABLE_NAME = "t_deferred_created";
    
    private static final long REFRESH_TIMEOUT_SECONDS = 30L;
    
    private static final long REFRESH_SETTLE_SECONDS = 5L;
    
    public PostgreSQLDDLMetaDataRefreshTestCase(final TransactionTestCaseParameter testCaseParam) {
        super(testCaseParam);
    }
    
    @Override
    public void executeTest(final TransactionContainerComposer containerComposer) throws SQLException {
        assertMetaDataRefreshedAfterCommit();
        assertMetaDataNotRefreshedAfterRollback();
        assertCreatedTableReconciledAfterCommit();
    }
    
    private void assertCreatedTableReconciledAfterCommit() throws SQLException {
        try (Connection connection = getDataSource().getConnection()) {
            connection.setAutoCommit(false);
            executeWithLog(connection, String.format("CREATE TABLE %s (id INT NOT NULL, PRIMARY KEY (id));", CREATED_TABLE_NAME));
            connection.commit();
        }
        try (Connection connection = getDataSource().getConnection()) {
            awaitQueryable(connection, CREATED_TABLE_NAME, true, "Table created inside the transaction is not present in meta data after commit.");
            executeWithLog(connection, String.format("DROP TABLE %s;", CREATED_TABLE_NAME));
            awaitQueryable(connection, CREATED_TABLE_NAME, false, "Dropped table is still present in meta data.");
        }
    }
    
    private void awaitQueryable(final Connection connection, final String tableName, final boolean expected, final String message) {
        Awaitility.await(message).atMost(REFRESH_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .pollInterval(500L, TimeUnit.MILLISECONDS).until(() -> expected == isQueryable(connection, tableName));
    }
    
    private boolean isQueryable(final Connection connection, final String tableName) {
        try (ResultSet ignored = executeQueryWithLog(connection, String.format("SELECT * FROM %s;", tableName))) {
            return true;
        } catch (final SQLException ignored) {
            return false;
        }
    }
    
    private void assertMetaDataRefreshedAfterCommit() throws SQLException {
        addColumnInTransaction(COMMITTED_COLUMN_NAME, true);
        try (Connection connection = getDataSource().getConnection()) {
            awaitColumnPresence(connection, COMMITTED_COLUMN_NAME, true, "Added column is not refreshed into meta data after transaction commit.");
            executeWithLog(connection, String.format("ALTER TABLE account DROP COLUMN %s;", COMMITTED_COLUMN_NAME));
            awaitColumnPresence(connection, COMMITTED_COLUMN_NAME, false, "Dropped column is still present in meta data.");
        }
    }
    
    private void assertMetaDataNotRefreshedAfterRollback() throws SQLException {
        addColumnInTransaction(ROLLED_BACK_COLUMN_NAME, false);
        Awaitility.await().pollDelay(REFRESH_SETTLE_SECONDS, TimeUnit.SECONDS).until(() -> true);
        try (Connection connection = getDataSource().getConnection()) {
            assertFalse(containsColumn(connection, ROLLED_BACK_COLUMN_NAME), "Rolled back column is refreshed into meta data after transaction rollback.");
        }
    }
    
    private void addColumnInTransaction(final String columnName, final boolean commit) throws SQLException {
        try (Connection connection = getDataSource().getConnection()) {
            connection.setAutoCommit(false);
            executeWithLog(connection, String.format("ALTER TABLE account ADD COLUMN %s INT;", columnName));
            if (commit) {
                connection.commit();
            } else {
                connection.rollback();
            }
        }
    }
    
    private void awaitColumnPresence(final Connection connection, final String columnName, final boolean expected, final String message) {
        Awaitility.await(message).atMost(REFRESH_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .pollInterval(500L, TimeUnit.MILLISECONDS).until(() -> expected == containsColumn(connection, columnName));
    }
    
    private boolean containsColumn(final Connection connection, final String columnName) throws SQLException {
        try (ResultSet resultSet = executeQueryWithLog(connection, "SELECT * FROM account;")) {
            ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
            for (int i = 1; i <= resultSetMetaData.getColumnCount(); i++) {
                if (columnName.equalsIgnoreCase(resultSetMetaData.getColumnName(i))) {
                    return true;
                }
            }
            return false;
        }
    }
}
