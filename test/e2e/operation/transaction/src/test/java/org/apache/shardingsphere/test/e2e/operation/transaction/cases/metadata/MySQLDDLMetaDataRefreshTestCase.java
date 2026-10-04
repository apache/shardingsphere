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

/**
 * MySQL DDL meta data refresh in transaction integration test.
 *
 * <p>MySQL commits the active transaction implicitly when it executes DDL, so the altered table is already visible to other
 * connections while the logical transaction is still open. Its meta data is therefore refreshed immediately rather than deferred to
 * the end of the transaction, and a later rollback cannot undo it.</p>
 */
@TransactionTestCase(dbTypes = TransactionTestConstants.MYSQL, adapters = TransactionTestConstants.PROXY, transactionTypes = TransactionType.LOCAL)
public final class MySQLDDLMetaDataRefreshTestCase extends BaseTransactionTestCase {
    
    private static final String ADDED_COLUMN_NAME = "implicit_commit_column";
    
    private static final long REFRESH_TIMEOUT_SECONDS = 30L;
    
    public MySQLDDLMetaDataRefreshTestCase(final TransactionTestCaseParameter testCaseParam) {
        super(testCaseParam);
    }
    
    @Override
    public void executeTest(final TransactionContainerComposer containerComposer) throws SQLException {
        assertMetaDataRefreshedBeforeTransactionEnds();
    }
    
    private void assertMetaDataRefreshedBeforeTransactionEnds() throws SQLException {
        try (
                Connection connection = getDataSource().getConnection();
                Connection queryConnection = getDataSource().getConnection()) {
            connection.setAutoCommit(false);
            executeWithLog(connection, String.format("ALTER TABLE account ADD COLUMN %s INT;", ADDED_COLUMN_NAME));
            awaitColumnPresence(queryConnection, true, "Implicitly committed column is not refreshed into meta data while the transaction is still open.");
            connection.rollback();
            awaitColumnPresence(queryConnection, true, "Implicitly committed column is missing from meta data after transaction rollback.");
        }
        try (Connection connection = getDataSource().getConnection()) {
            executeWithLog(connection, String.format("ALTER TABLE account DROP COLUMN %s;", ADDED_COLUMN_NAME));
            awaitColumnPresence(connection, false, "Dropped column is still present in meta data.");
        }
    }
    
    private void awaitColumnPresence(final Connection connection, final boolean expected, final String message) {
        Awaitility.await(message).atMost(REFRESH_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .pollInterval(500L, TimeUnit.MILLISECONDS).until(() -> expected == containsAddedColumn(connection));
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
