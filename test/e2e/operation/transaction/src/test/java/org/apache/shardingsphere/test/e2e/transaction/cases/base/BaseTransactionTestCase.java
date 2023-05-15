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

package org.apache.shardingsphere.test.e2e.transaction.cases.base;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.test.e2e.transaction.engine.base.TransactionBaseE2EIT;
import org.apache.shardingsphere.test.e2e.transaction.engine.base.TransactionContainerComposer;
import org.apache.shardingsphere.test.e2e.transaction.engine.constants.TransactionTestConstants;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Base transaction test case.
 */
@RequiredArgsConstructor
@Getter(AccessLevel.PROTECTED)
@Slf4j
public abstract class BaseTransactionTestCase {
    
    private final TransactionBaseE2EIT baseTransactionITCase;
    
    private final DataSource dataSource;
    
    /**
     * Execute test cases.
     *
     * @param containerComposer container composer
     * @throws SQLException SQL exception
     */
    public void execute(final TransactionContainerComposer containerComposer) throws SQLException {
        beforeTest();
        executeTest(containerComposer);
        afterTest();
    }
    
    protected abstract void executeTest(TransactionContainerComposer containerComposer) throws SQLException;
    
    protected void beforeTest() throws SQLException {
        try (Connection connection = getDataSource().getConnection()) {
            connection.setAutoCommit(false);
            executeWithLog(connection, "delete from account;");
            executeWithLog(connection, "delete from t_order;");
            executeWithLog(connection, "delete from t_order_item;");
            connection.commit();
        }
    }
    
    protected void afterTest() throws SQLException {
    }
    
    protected static void executeWithLog(final Connection connection, final String sql) throws SQLException {
        log.info("Connection execute: {}.", sql);
        connection.createStatement().execute(sql);
    }
    
    protected static void executeUpdateWithLog(final Connection connection, final String sql) throws SQLException {
        log.info("Connection execute update: {}.", sql);
        connection.createStatement().executeUpdate(sql);
    }
    
    protected static ResultSet executeQueryWithLog(final Connection connection, final String sql) throws SQLException {
        log.info("Connection execute query: {}.", sql);
        return connection.createStatement().executeQuery(sql);
    }
    
    protected static void assertAccountRowCount(final Connection connection, final int rowNum) throws SQLException {
        assertTableRowCount(connection, TransactionTestConstants.ACCOUNT, rowNum);
    }
    
    protected static void assertTableRowCount(final Connection connection, final String tableName, final int rowNum) throws SQLException {
        try (Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery(String.format("SELECT COUNT(*) FROM %s", tableName))) {
            if (resultSet.next()) {
                int rowCount = resultSet.getInt(1);
                assertThat(String.format("Recode num assert error, expect: %s, actual: %s.", rowNum, rowCount), rowCount, is(rowNum));
            }
        }
    }
    
    protected void executeSqlListWithLog(final Connection connection, final String... sqlList) throws SQLException {
        for (String each : sqlList) {
            log.info("Connection execute: {}.", each);
            connection.createStatement().execute(each);
        }
    }
    
    protected int countWithLog(final Connection connection, final String sql) throws SQLException {
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(sql);
        int result = 0;
        while (resultSet.next()) {
            result++;
        }
        return result;
    }
    
    protected void assertAccountBalances(final Connection connection, final int... expectedBalances) throws SQLException {
        try (Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery("SELECT * FROM account")) {
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                int actualBalance = resultSet.getInt("balance");
                assertBalance(actualBalance, expectedBalances[id - 1]);
            }
        }
    }
    
    private void assertBalance(final int actual, final int expected) {
        assertThat(String.format("Balance is %s, should be %s.", actual, expected), actual, is(expected));
    }
}
