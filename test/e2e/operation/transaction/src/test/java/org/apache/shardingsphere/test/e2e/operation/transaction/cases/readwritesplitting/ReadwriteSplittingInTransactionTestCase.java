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

package org.apache.shardingsphere.test.e2e.operation.transaction.cases.readwritesplitting;

import org.apache.shardingsphere.test.e2e.operation.transaction.cases.base.BaseTransactionTestCase;
import org.apache.shardingsphere.test.e2e.operation.transaction.engine.base.TransactionContainerComposer;
import org.apache.shardingsphere.test.e2e.operation.transaction.engine.base.TransactionTestCase;
import org.apache.shardingsphere.test.e2e.operation.transaction.engine.constants.TransactionTestConstants;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TransactionTestCase(dbTypes = TransactionTestConstants.MYSQL, scenario = "readwrite-splitting", adapters = TransactionTestConstants.PROXY)
public final class ReadwriteSplittingInTransactionTestCase extends BaseTransactionTestCase {
    
    public ReadwriteSplittingInTransactionTestCase(final TransactionTestCaseParameter testCaseParam) {
        super(testCaseParam);
    }
    
    @Override
    protected void beforeTest() throws SQLException {
        super.beforeTest();
        unregisterAbnormalStoreUnitAfterForceStartup();
    }
    
    private void unregisterAbnormalStoreUnitAfterForceStartup() throws SQLException {
        try (Connection connection = getDataSource().getConnection()) {
            ResultSet resultSet = connection.createStatement().executeQuery("SHOW STORAGE UNITS");
            boolean hasAbnormalStorageUnit = false;
            while (resultSet.next()) {
                if ("read_ds_error".equals(resultSet.getString("name"))) {
                    hasAbnormalStorageUnit = true;
                    break;
                }
            }
            if (hasAbnormalStorageUnit) {
                executeWithLog(connection, "UNREGISTER STORAGE UNIT read_ds_error");
            }
        }
    }
    
    @Override
    protected void executeTest(final TransactionContainerComposer containerComposer) throws SQLException {
        assertRollback();
        assertCommit();
    }
    
    private void assertRollback() throws SQLException {
        try (Connection connection = getDataSource().getConnection()) {
            assertRouteToReadDataSource(preview(connection, "SELECT * FROM account"));
            connection.setAutoCommit(false);
            assertAccountRowCount(connection, 0);
            assertRouteToWriteDataSource(preview(connection, "INSERT INTO account(id, balance, transaction_id) VALUES(1, 1, 1)"));
            executeWithLog(connection, "INSERT INTO account(id, balance, transaction_id) VALUES(1, 1, 1)");
            assertRouteToWriteDataSource(preview(connection, "SELECT * FROM account"));
            assertRouteToWriteDataSource(preview(connection, "SELECT COUNT(*) FROM account FOR UPDATE"));
            assertWriteDataSourceTableRowCount(connection, 1);
            assertAccountRowCount(connection, 1);
            connection.rollback();
            assertAccountRowCount(connection, 0);
        }
    }
    
    private void assertCommit() throws SQLException {
        try (Connection connection = getDataSource().getConnection()) {
            assertRouteToReadDataSource(preview(connection, "SELECT * FROM account"));
            connection.setAutoCommit(false);
            assertAccountRowCount(connection, 0);
            assertRouteToWriteDataSource(preview(connection, "INSERT INTO account(id, balance, transaction_id) VALUES(1, 1, 1)"));
            executeWithLog(connection, "INSERT INTO account(id, balance, transaction_id) VALUES(1, 1, 1)");
            assertRouteToWriteDataSource(preview(connection, "SELECT * FROM account"));
            assertRouteToWriteDataSource(preview(connection, "SELECT COUNT(*) FROM account FOR UPDATE"));
            assertWriteDataSourceTableRowCount(connection, 1);
            assertAccountRowCount(connection, 1);
            connection.commit();
            assertAccountRowCount(connection, 1);
        }
    }
    
    private String preview(final Connection connection, final String sql) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute(String.format("PREVIEW %s;", sql));
            ResultSet resultSet = statement.getResultSet();
            if (resultSet.next()) {
                return resultSet.getString("data_source_name");
            }
            return "";
        }
    }
    
    private void assertWriteDataSourceTableRowCount(final Connection connection, final int rowNum) throws SQLException {
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) FROM account FOR UPDATE");
        int resultSetCount = 0;
        while (resultSet.next()) {
            resultSetCount++;
        }
        statement.close();
        assertThat(String.format("Recode num assert error, expect: %s, actual: %s.", rowNum, resultSetCount), resultSetCount, is(rowNum));
    }
    
    private void assertRouteToReadDataSource(final String routedDataSource) {
        assertTrue(Arrays.asList("read_ds_0", "read_ds_1").contains(routedDataSource));
    }
    
    private void assertRouteToWriteDataSource(final String routedDataSource) {
        assertThat(routedDataSource, is("write_ds"));
    }
}
