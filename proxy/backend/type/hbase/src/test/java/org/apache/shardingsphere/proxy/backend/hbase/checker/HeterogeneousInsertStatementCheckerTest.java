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

package org.apache.shardingsphere.proxy.backend.hbase.checker;

import org.apache.shardingsphere.proxy.backend.hbase.result.HBaseSupportedSQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class HeterogeneousInsertStatementCheckerTest {
    
    @Test
    void assertExecuteInsertStatement() {
        SQLStatement sqlStatement = HBaseSupportedSQLStatement.parseSQLStatement(HBaseSupportedSQLStatement.getInsertStatement());
        assertDoesNotThrow(() -> HBaseCheckerFactory.newInstance(sqlStatement).execute());
    }
    
    @Test
    void assertInsertWithoutRowKey() {
        String sql = "INSERT /*+ HBase */ INTO t_order (order_id, user_id, status) VALUES (?, ?, ?)";
        SQLStatement sqlStatement = HBaseSupportedSQLStatement.parseSQLStatement(sql);
        Exception ex = assertThrows(IllegalArgumentException.class, () -> HBaseCheckerFactory.newInstance(sqlStatement).execute());
        assertThat(ex.getMessage(), is("First column must be rowKey"));
    }
    
    @Test
    void assertInsertWithoutColumns() {
        String sql = "INSERT /*+ HBase */ INTO t_order VALUES (?, ?, ?)";
        SQLStatement sqlStatement = HBaseSupportedSQLStatement.parseSQLStatement(sql);
        Exception ex = assertThrows(IllegalArgumentException.class, () -> HBaseCheckerFactory.newInstance(sqlStatement).execute());
        assertThat(ex.getMessage(), is("The inserted column must be explicitly specified"));
    }
    
    @Test
    void assertInsertWithMultipleRowKey() {
        String sql = "INSERT /*+ HBase */ INTO t_order (rowKey, id, status) VALUES (?, ?, ?)";
        SQLStatement sqlStatement = HBaseSupportedSQLStatement.parseSQLStatement(sql);
        Exception ex = assertThrows(IllegalArgumentException.class, () -> HBaseCheckerFactory.newInstance(sqlStatement).execute());
        assertThat(ex.getMessage(), is("Cannot contain multiple rowKey"));
    }
    
    @Test
    void assertInsertWithOnDuplicateKey() {
        String sql = "INSERT /*+ HBase */ INTO t_order (rowKey, user_id, status) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE status = ?";
        SQLStatement sqlStatement = HBaseSupportedSQLStatement.parseSQLStatement(sql);
        Exception ex = assertThrows(IllegalArgumentException.class, () -> HBaseCheckerFactory.newInstance(sqlStatement).execute());
        assertThat(ex.getMessage(), is("Do not supported ON DUPLICATE KEY UPDATE"));
    }
    
    @Test
    void assertInsertWithFunction() {
        String sql = "INSERT /*+ HBase */ INTO t_order_item (rowKey, order_id, user_id, status, creation_date) VALUES (?, ?, ?, 'insert', now())";
        SQLStatement sqlStatement = HBaseSupportedSQLStatement.parseSQLStatement(sql);
        Exception ex = assertThrows(IllegalArgumentException.class, () -> HBaseCheckerFactory.newInstance(sqlStatement).execute());
        assertThat(ex.getMessage(), is("Value must is literal or parameter marker"));
    }
    
    @Test
    void assertInsertWithLiteralAndParameterMarker() {
        String sql = "INSERT /*+ HBase */ INTO t_order_item(rowKey, order_id, user_id, status, creation_date) VALUES (?, ?, ?, 'insert', '2017-08-08')";
        SQLStatement sqlStatement = HBaseSupportedSQLStatement.parseSQLStatement(sql);
        assertDoesNotThrow(() -> HBaseCheckerFactory.newInstance(sqlStatement).execute());
    }
    
    @Test
    void assertInsertWithSubQuery() {
        String sql = "INSERT /*+ HBase */ INTO t_order_item(rowKey, order_id, user_id) select rowKey, order_id, user_id from t_order";
        SQLStatement sqlStatement = HBaseSupportedSQLStatement.parseSQLStatement(sql);
        Exception ex = assertThrows(IllegalArgumentException.class, () -> HBaseCheckerFactory.newInstance(sqlStatement).execute());
        assertThat(ex.getMessage(), is("Do not supported `insert into...select...`"));
    }
}
