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

import org.apache.shardingsphere.proxy.backend.hbase.context.HBaseContext;
import org.apache.shardingsphere.proxy.backend.hbase.props.HBaseProperties;
import org.apache.shardingsphere.proxy.backend.hbase.props.HBasePropertyKey;
import org.apache.shardingsphere.proxy.backend.hbase.result.HBaseSupportedSQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class HeterogeneousSelectStatementCheckerTest {
    
    @Rule
    public ExpectedException expectedEx = ExpectedException.none();
    
    @Before
    public void setUp() {
        HBaseProperties props = mock(HBaseProperties.class);
        when(props.getValue(HBasePropertyKey.MAX_SCAN_LIMIT_SIZE)).thenReturn(5000L);
        HBaseContext.getInstance().setProps(props);
    }
    
    @Test
    public void assertSelectStatement() {
        SQLStatement sqlStatement = HBaseSupportedSQLStatement.parseSQLStatement(HBaseSupportedSQLStatement.getSelectStatement());
        HeterogeneousSQLStatementChecker<?> actual = HBaseCheckerFactory.newInstance(sqlStatement);
        actual.execute();
    }
    
    @Test
    public void assertSelectStatementWithLargeRowCount() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("row count must less than 5000");
        SQLStatement sqlStatement = HBaseSupportedSQLStatement.parseSQLStatement("select /*+ hbase */ * from t_order where id = 1 limit 5001");
        HeterogeneousSQLStatementChecker<?> actual = HBaseCheckerFactory.newInstance(sqlStatement);
        actual.execute();
    }
    
    @Test
    public void assertSelectStatementWithLimitSegment() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Do not supported offset segment");
        SQLStatement sqlStatement = HBaseSupportedSQLStatement.parseSQLStatement("select /*+ hbase */ * from t_order where id = 1 limit 5 offset 3");
        HeterogeneousSQLStatementChecker<?> actual = HBaseCheckerFactory.newInstance(sqlStatement);
        actual.execute();
    }
    
    @Test
    public void assertSelectStatementWithLockSegment() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Do not supported lock segment");
        SQLStatement sqlStatement = HBaseSupportedSQLStatement.parseSQLStatement("select /*+ hbase */ * from t_order where id = 1 lock in share mode");
        HeterogeneousSQLStatementChecker<?> actual = HBaseCheckerFactory.newInstance(sqlStatement);
        actual.execute();
    }
    
    @Test
    public void assertSelectStatementWithFunction() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Only supported ShorthandProjection and ColumnProjection");
        String sql = "SELECT /*+ HBase */ sum(score) FROM person";
        SQLStatement sqlStatement = HBaseSupportedSQLStatement.parseSQLStatement(sql);
        HeterogeneousSQLStatementChecker<?> actual = HBaseCheckerFactory.newInstance(sqlStatement);
        actual.execute();
    }
    
    @Test
    public void assertSelectStatementWithJoinStatement() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Only supported SimpleTableSegment");
        String sql = "SELECT /*+ HBase */ * FROM t_order o JOIN t_order_item i ON o.user_id = i.user_id AND o.order_id = i.order_id";
        SQLStatement sqlStatement = HBaseSupportedSQLStatement.parseSQLStatement(sql);
        HeterogeneousSQLStatementChecker<?> actual = HBaseCheckerFactory.newInstance(sqlStatement);
        actual.execute();
    }
    
    @Test
    public void assertSelectStatementWithMultipleInExpression() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("left segment must is ColumnSegment");
        String sql = "SELECT /*+ HBase */ * FROM t_order WHERE rowKey IN (?, ?) AND id IN (?, ?)";
        SQLStatement sqlStatement = HBaseSupportedSQLStatement.parseSQLStatement(sql);
        HeterogeneousSQLStatementChecker<?> actual = HBaseCheckerFactory.newInstance(sqlStatement);
        actual.execute();
    }
    
    @Test
    public void assertSelectStatementWithInExpression() {
        String sql = "SELECT /*+ HBase */ * from t_order where rowKey in (1, 2, 3)";
        SQLStatement sqlStatement = HBaseSupportedSQLStatement.parseSQLStatement(sql);
        HeterogeneousSQLStatementChecker<?> actual = HBaseCheckerFactory.newInstance(sqlStatement);
        actual.execute();
    }
    
    @Test
    public void assertSelectStatementWithErrorKey() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("age is not a allowed key");
        String sql = "SELECT /*+ HBase */ * from t_order where age in (1, 2, 3)";
        SQLStatement sqlStatement = HBaseSupportedSQLStatement.parseSQLStatement(sql);
        HeterogeneousSQLStatementChecker<?> actual = HBaseCheckerFactory.newInstance(sqlStatement);
        actual.execute();
    }
    
    @Test
    public void assertExecuteSelectWithNotIn() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Do not supported `not in`");
        String sql = "SELECT /*+ HBase */ * from t_order where rowKey not in (1, 2, 3)";
        SQLStatement sqlStatement = HBaseSupportedSQLStatement.parseSQLStatement(sql);
        HeterogeneousSQLStatementChecker<?> actual = HBaseCheckerFactory.newInstance(sqlStatement);
        actual.execute();
    }
    
    @Test
    public void assertExecuteSelectWithParameterMarker() {
        String sql = "SELECT /*+ HBase */ * from t_order where rowKey in (?, ?, ?)";
        SQLStatement sqlStatement = HBaseSupportedSQLStatement.parseSQLStatement(sql);
        HeterogeneousSQLStatementChecker<?> actual = HBaseCheckerFactory.newInstance(sqlStatement);
        actual.execute();
    }
    
    @Test
    public void assertSelectStatementUseCrc32() {
        String sql = "SELECT /*+ HBase */ crc32(concat_ws('#',rowKey)) from t_order where rowKey in (1, 2, 3)";
        SQLStatement sqlStatement = HBaseSupportedSQLStatement.parseSQLStatement(sql);
        HeterogeneousSQLStatementChecker<?> actual = HBaseCheckerFactory.newInstance(sqlStatement);
        actual.execute();
    }
    
    @Test
    public void assertExecuteSelectWithErrorInExpression() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Only supported ListExpression");
        String sql = "SELECT /*+ HBase */ * from t_order where rowKey in (select rowKey from t_order_item)";
        SQLStatement sqlStatement = HBaseSupportedSQLStatement.parseSQLStatement(sql);
        HeterogeneousSQLStatementChecker<?> actual = HBaseCheckerFactory.newInstance(sqlStatement);
        actual.execute();
    }
    
    @Test
    public void assertExecuteSelectWithBetween() {
        String sql = "SELECT /*+ HBase */ * from t_order where rowKey between 1 and 2";
        SQLStatement sqlStatement = HBaseSupportedSQLStatement.parseSQLStatement(sql);
        HeterogeneousSQLStatementChecker<?> actual = HBaseCheckerFactory.newInstance(sqlStatement);
        actual.execute();
    }
    
    @Test
    public void assertExecuteSelectWithNotBetween() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Do not supported `not between...and...`");
        String sql = "SELECT /*+ HBase */ * from t_order where rowKey not between 1 and 2";
        SQLStatement sqlStatement = HBaseSupportedSQLStatement.parseSQLStatement(sql);
        HeterogeneousSQLStatementChecker<?> actual = HBaseCheckerFactory.newInstance(sqlStatement);
        actual.execute();
    }
    
    @Test
    public void assertExecuteSelectWithBetweenErrorKey() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("age is not a allowed key");
        String sql = "SELECT /*+ HBase */ * from t_order where age between 1 and 2";
        SQLStatement sqlStatement = HBaseSupportedSQLStatement.parseSQLStatement(sql);
        HeterogeneousSQLStatementChecker<?> actual = HBaseCheckerFactory.newInstance(sqlStatement);
        actual.execute();
    }
    
    @Test
    public void assertExecuteSelectWithErrorBetweenExpr() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("between expr must is literal or parameter marker");
        String sql = "SELECT /*+ HBase */ * from t_order where rowKey between 1 and now()";
        SQLStatement sqlStatement = HBaseSupportedSQLStatement.parseSQLStatement(sql);
        HeterogeneousSQLStatementChecker<?> actual = HBaseCheckerFactory.newInstance(sqlStatement);
        actual.execute();
    }
    
    @Test
    public void assertSelectWithGroupBy() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Do not supported group by segment");
        String sql = "SELECT /*+ HBase */ * from t_order group by order_id";
        SQLStatement sqlStatement = HBaseSupportedSQLStatement.parseSQLStatement(sql);
        HeterogeneousSQLStatementChecker<?> actual = HBaseCheckerFactory.newInstance(sqlStatement);
        actual.execute();
    }
    
    @Test
    public void assertSelectWithNotAllowOperator() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Only Supported `=` operator");
        String sql = "select /*+ hbase */ * from t_order where rowKey != 1";
        SQLStatement sqlStatement = HBaseSupportedSQLStatement.parseSQLStatement(sql);
        HeterogeneousSQLStatementChecker<?> actual = HBaseCheckerFactory.newInstance(sqlStatement);
        actual.execute();
    }
    
    @Test
    public void assertSelectWithNotAllowColumn() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("age is not a allowed key");
        String sql = "select /*+ hbase */ * from t_order where age = 1";
        SQLStatement sqlStatement = HBaseSupportedSQLStatement.parseSQLStatement(sql);
        HeterogeneousSQLStatementChecker<?> actual = HBaseCheckerFactory.newInstance(sqlStatement);
        actual.execute();
    }
    
    @Test
    public void assertSelectWithMultipleExpression() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Do not supported Multiple expressions");
        String sql = "select /*+ hbase */ * from t_order where rowKey = 1 and age = 2";
        SQLStatement sqlStatement = HBaseSupportedSQLStatement.parseSQLStatement(sql);
        HeterogeneousSQLStatementChecker<?> actual = HBaseCheckerFactory.newInstance(sqlStatement);
        actual.execute();
    }
    
    @Test
    public void assertSelectWithNotColumnExpression() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("left segment must is ColumnSegment");
        String sql = "select /*+ hbase */ * from t_order where 1 = 1";
        SQLStatement sqlStatement = HBaseSupportedSQLStatement.parseSQLStatement(sql);
        HeterogeneousSQLStatementChecker<?> actual = HBaseCheckerFactory.newInstance(sqlStatement);
        actual.execute();
    }
    
    @Test
    public void assertSelectWithParameterMarker() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Only supported ShorthandProjection and ColumnProjection");
        String sql = "select /*+ hbase */ rowKey, name, ? from t_order where rowKey = 'kid'";
        SQLStatement sqlStatement = HBaseSupportedSQLStatement.parseSQLStatement(sql);
        HeterogeneousSQLStatementChecker<?> actual = HBaseCheckerFactory.newInstance(sqlStatement);
        actual.execute();
    }
}
