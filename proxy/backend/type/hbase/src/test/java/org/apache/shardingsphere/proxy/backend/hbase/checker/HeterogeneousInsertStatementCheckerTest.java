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
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public final class HeterogeneousInsertStatementCheckerTest {
    
    @Rule
    public ExpectedException expectedEx = ExpectedException.none();
    
    @Test
    public void assertExecuteInsertStatement() {
        SQLStatement sqlStatement = HBaseSupportedSQLStatement.parseSQLStatement(HBaseSupportedSQLStatement.getInsertStatement());
        HeterogeneousSQLStatementChecker<?> actual = HBaseCheckerFactory.newInstance(sqlStatement);
        actual.execute();
    }
    
    @Test
    public void assertInsertWithoutRowKey() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("First column must be rowKey");
        String sql = "INSERT /*+ HBase */ INTO t_order (order_id, user_id, status) VALUES (?, ?, ?)";
        SQLStatement sqlStatement = HBaseSupportedSQLStatement.parseSQLStatement(sql);
        HeterogeneousSQLStatementChecker<?> actual = HBaseCheckerFactory.newInstance(sqlStatement);
        actual.execute();
    }
    
    @Test
    public void assertInsertWithoutColumns() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("The inserted column must be explicitly specified");
        String sql = "INSERT /*+ HBase */ INTO t_order VALUES (?, ?, ?)";
        SQLStatement sqlStatement = HBaseSupportedSQLStatement.parseSQLStatement(sql);
        HeterogeneousSQLStatementChecker<?> actual = HBaseCheckerFactory.newInstance(sqlStatement);
        actual.execute();
    }
    
    @Test
    public void assertInsertWithMultipleRowKey() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Cannot contain multiple rowKey");
        String sql = "INSERT /*+ HBase */ INTO t_order (rowKey, id, status) VALUES (?, ?, ?)";
        SQLStatement sqlStatement = HBaseSupportedSQLStatement.parseSQLStatement(sql);
        HeterogeneousSQLStatementChecker<?> actual = HBaseCheckerFactory.newInstance(sqlStatement);
        actual.execute();
    }
    
    @Test
    public void assertInsertWithOnDuplicateKey() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Do not supported ON DUPLICATE KEY UPDATE");
        String sql = "INSERT /*+ HBase */ INTO t_order (rowKey, user_id, status) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE status = ?";
        SQLStatement sqlStatement = HBaseSupportedSQLStatement.parseSQLStatement(sql);
        HeterogeneousSQLStatementChecker<?> actual = HBaseCheckerFactory.newInstance(sqlStatement);
        actual.execute();
    }
    
    @Test
    public void assertInsertWithFunction() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Value must is literal or parameter marker");
        String sql = "INSERT /*+ HBase */ INTO t_order_item (rowKey, order_id, user_id, status, creation_date) VALUES (?, ?, ?, 'insert', now())";
        SQLStatement sqlStatement = HBaseSupportedSQLStatement.parseSQLStatement(sql);
        HeterogeneousSQLStatementChecker<?> actual = HBaseCheckerFactory.newInstance(sqlStatement);
        actual.execute();
    }
    
    @Test
    public void assertInsertWithLiteralAndParameterMarker() {
        String sql = "INSERT /*+ HBase */ INTO t_order_item(rowKey, order_id, user_id, status, creation_date) VALUES (?, ?, ?, 'insert', '2017-08-08')";
        SQLStatement sqlStatement = HBaseSupportedSQLStatement.parseSQLStatement(sql);
        HeterogeneousSQLStatementChecker<?> actual = HBaseCheckerFactory.newInstance(sqlStatement);
        actual.execute();
    }
    
    @Test
    public void assertInsertWithSubQuery() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Do not supported `insert into...select...`");
        String sql = "INSERT /*+ HBase */ INTO t_order_item(rowKey, order_id, user_id) select rowKey, order_id, user_id from t_order";
        SQLStatement sqlStatement = HBaseSupportedSQLStatement.parseSQLStatement(sql);
        HeterogeneousSQLStatementChecker<?> actual = HBaseCheckerFactory.newInstance(sqlStatement);
        actual.execute();
    }
}
