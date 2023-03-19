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

public final class HeterogeneousUpdateStatementCheckerTest {
    
    @Rule
    public ExpectedException expectedEx = ExpectedException.none();
    
    @Test
    public void assertExecuteUpdateStatement() {
        SQLStatement sqlStatement = HBaseSupportedSQLStatement.parseSQLStatement(HBaseSupportedSQLStatement.getUpdateStatement());
        HeterogeneousSQLStatementChecker<?> actual = HBaseCheckerFactory.newInstance(sqlStatement);
        actual.execute();
    }
    
    @Test
    public void assertUpdateWithFunction() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Assigment must is literal or parameter marker");
        String sql = "update /*+ hbase */ t_test_order set age = 10, name = 'bob', time = now() where rowKey = 1";
        SQLStatement sqlStatement = HBaseSupportedSQLStatement.parseSQLStatement(sql);
        HeterogeneousSQLStatementChecker<?> actual = HBaseCheckerFactory.newInstance(sqlStatement);
        actual.execute();
    }
    
    @Test
    public void assertOperatorIsNotEqual() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Only Supported `=` operator");
        String sql = "update /*+ hbase */ t_test_order set age = 10 where rowKey > 1";
        SQLStatement sqlStatement = HBaseSupportedSQLStatement.parseSQLStatement(sql);
        HeterogeneousSQLStatementChecker<?> actual = HBaseCheckerFactory.newInstance(sqlStatement);
        actual.execute();
    }
    
    @Test
    public void assertColumnIsNotRowKey() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("is not a allowed key");
        String sql = "update /*+ hbase */ t_test_order set age = 10 where age = 1";
        SQLStatement sqlStatement = HBaseSupportedSQLStatement.parseSQLStatement(sql);
        HeterogeneousSQLStatementChecker<?> actual = HBaseCheckerFactory.newInstance(sqlStatement);
        actual.execute();
    }
    
    @Test
    public void assertLeftIsNotColumn() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("left segment must is ColumnSegment");
        String sql = "update /*+ hbase */ t_test_order set age = 10 where 1 = 1";
        SQLStatement sqlStatement = HBaseSupportedSQLStatement.parseSQLStatement(sql);
        HeterogeneousSQLStatementChecker<?> actual = HBaseCheckerFactory.newInstance(sqlStatement);
        actual.execute();
    }
    
    @Test
    public void assertMultiExpression() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Do not supported Multiple expressions");
        String sql = "update /*+ hbase */ t_test_order set age = 10 WHERE order_id = ? AND user_id = ? AND status=?";
        SQLStatement sqlStatement = HBaseSupportedSQLStatement.parseSQLStatement(sql);
        HeterogeneousSQLStatementChecker<?> actual = HBaseCheckerFactory.newInstance(sqlStatement);
        actual.execute();
    }
    
    @Test
    public void assertNotWhereSegment() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Must Have Where Segment");
        String sql = "update /*+ hbase */ t_test_order set age = 10 ";
        SQLStatement sqlStatement = HBaseSupportedSQLStatement.parseSQLStatement(sql);
        HeterogeneousSQLStatementChecker<?> actual = HBaseCheckerFactory.newInstance(sqlStatement);
        actual.execute();
    }
    
    @Test
    public void assertUpdateRowKey() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Do not allow update rowKey");
        String sql = "update /*+ hbase */ t_test_order set rowKey = 10 where rowKey = 'kid'";
        SQLStatement sqlStatement = HBaseSupportedSQLStatement.parseSQLStatement(sql);
        HeterogeneousSQLStatementChecker<?> actual = HBaseCheckerFactory.newInstance(sqlStatement);
        actual.execute();
    }
}
