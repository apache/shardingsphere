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

public final class HeterogeneousDeleteStatementCheckerTest {
    
    @Rule
    public ExpectedException expectedEx = ExpectedException.none();
    
    @Test
    public void assertExecuteDeleteStatement() {
        SQLStatement sqlStatement = HBaseSupportedSQLStatement.parseSQLStatement(HBaseSupportedSQLStatement.getDeleteStatement());
        HeterogeneousSQLStatementChecker<?> actual = HBaseCheckerFactory.newInstance(sqlStatement);
        actual.execute();
    }
    
    @Test
    public void assertOperatorIsNotEqual() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Only Supported `=` operator");
        String sql = "delete /*+ hbase */ from t_test_order where rowKey > 1";
        SQLStatement sqlStatement = HBaseSupportedSQLStatement.parseSQLStatement(sql);
        HeterogeneousSQLStatementChecker<?> actual = HBaseCheckerFactory.newInstance(sqlStatement);
        actual.execute();
    }
    
    @Test
    public void assertColumnIsNotRowKey() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("is not a allowed key");
        String sql = "delete /*+ hbase */ from t_test_order where age = 1";
        SQLStatement sqlStatement = HBaseSupportedSQLStatement.parseSQLStatement(sql);
        HeterogeneousSQLStatementChecker<?> actual = HBaseCheckerFactory.newInstance(sqlStatement);
        actual.execute();
    }
    
    @Test
    public void assertLeftIsNotColumn() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("left segment must is ColumnSegment");
        String sql = "delete /*+ hbase */ from t_test_order where 1 = 1";
        SQLStatement sqlStatement = HBaseSupportedSQLStatement.parseSQLStatement(sql);
        HeterogeneousSQLStatementChecker<?> actual = HBaseCheckerFactory.newInstance(sqlStatement);
        actual.execute();
    }
    
    @Test
    public void assertMultiExpression() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Do not supported Multiple expressions");
        String sql = "DELETE /*+ hbase */ FROM t_order WHERE order_id = ? AND user_id = ? AND status=?";
        SQLStatement sqlStatement = HBaseSupportedSQLStatement.parseSQLStatement(sql);
        HeterogeneousSQLStatementChecker<?> actual = HBaseCheckerFactory.newInstance(sqlStatement);
        actual.execute();
    }
    
    @Test
    public void assertWithBetweenExpression() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Only Support BinaryOperationExpression");
        String sql = "DELETE /*+ hbase */ FROM t_order WHERE rowKey between 1 and 5";
        SQLStatement sqlStatement = HBaseSupportedSQLStatement.parseSQLStatement(sql);
        HeterogeneousSQLStatementChecker<?> actual = HBaseCheckerFactory.newInstance(sqlStatement);
        actual.execute();
    }
    
    @Test
    public void assertNotWhereSegment() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Must Have Where Segment");
        String sql = "DELETE /*+ hbase */ FROM t_order";
        SQLStatement sqlStatement = HBaseSupportedSQLStatement.parseSQLStatement(sql);
        HeterogeneousSQLStatementChecker<?> actual = HBaseCheckerFactory.newInstance(sqlStatement);
        actual.execute();
    }
}
