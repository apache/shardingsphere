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

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.integration.transaction.engine.constants.TransactionTestConstants;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.Assert.assertEquals;

/**
 * Base transaction test case.
 */
@Slf4j
public abstract class BaseTransactionTestCase {
    
    @Getter
    private DataSource dataSource;
    
    public BaseTransactionTestCase(final DataSource dataSource) {
        this.dataSource = dataSource;
    }
    
    /**
     * Integration testing method with assertions.
     */
    public abstract void executeTest();
    
    @SneakyThrows(SQLException.class)
    protected static void executeWithLog(final Connection connection, final String sql) {
        log.info("Connection execute: {}.", sql);
        connection.createStatement().execute(sql);
    }
    
    @SneakyThrows(SQLException.class)
    protected static void executeUpdateWithLog(final Connection connection, final String sql) {
        log.info("Connection execute update: {}.", sql);
        connection.createStatement().executeUpdate(sql);
    }
    
    @SneakyThrows(SQLException.class)
    protected static ResultSet executeQueryWithLog(final Connection connection, final String sql) {
        log.info("Connection execute query: {}.", sql);
        return connection.createStatement().executeQuery(sql);
    }
    
    protected static void assertAccountRowCount(final Connection conn, final int rowNum) {
        assertTableRowCount(conn, TransactionTestConstants.ACCOUNT, rowNum);
    }
    
    @SneakyThrows(SQLException.class)
    protected static void assertTableRowCount(final Connection conn, final String tableName, final int rowNum) {
        Statement statement = conn.createStatement();
        ResultSet rs = statement.executeQuery("select * from " + tableName);
        int resultSetCount = 0;
        while (rs.next()) {
            resultSetCount++;
        }
        statement.close();
        assertEquals(String.format("Recode num assert error, expect: %s, actual: %s.", rowNum, resultSetCount), resultSetCount, rowNum);
    }
    
    @SneakyThrows(SQLException.class)
    protected void executeSqlListWithLog(final Connection conn, final String... sqlList) {
        for (String sql : sqlList) {
            log.info("Connection execute: {}.", sql);
            conn.createStatement().execute(sql);
        }
    }
}
