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

package org.apache.shardingsphere.integration.transaction.cases.base;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.integration.transaction.engine.base.BaseTransactionITCase;
import org.apache.shardingsphere.integration.transaction.engine.constants.TransactionTestConstants;

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
    
    private final BaseTransactionITCase baseTransactionITCase;
    
    private final DataSource dataSource;
    
    /**
     * Execute test cases.
     * 
     * @throws SQLException SQL exception
     */
    public void execute() throws SQLException {
        beforeTest();
        executeTest();
        afterTest();
    }
    
    protected abstract void executeTest() throws SQLException;
    
    protected void beforeTest() throws SQLException {
        Connection conn = getDataSource().getConnection();
        executeWithLog(conn, "delete from account;");
        executeWithLog(conn, "delete from t_order;");
        executeWithLog(conn, "delete from t_order_item;");
        conn.close();
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
    
    protected static void assertAccountRowCount(final Connection conn, final int rowNum) throws SQLException {
        assertTableRowCount(conn, TransactionTestConstants.ACCOUNT, rowNum);
    }
    
    protected static void assertTableRowCount(final Connection conn, final String tableName, final int rowNum) throws SQLException {
        Statement statement = conn.createStatement();
        ResultSet resultSet = statement.executeQuery("select * from " + tableName);
        int resultSetCount = 0;
        while (resultSet.next()) {
            resultSetCount++;
        }
        statement.close();
        assertThat(String.format("Recode num assert error, expect: %s, actual: %s.", rowNum, resultSetCount), resultSetCount, is(rowNum));
    }
    
    protected void executeSqlListWithLog(final Connection conn, final String... sqlList) throws SQLException {
        for (String each : sqlList) {
            log.info("Connection execute: {}.", each);
            conn.createStatement().execute(each);
        }
    }
}
