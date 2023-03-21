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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class HeterogeneousSelectStatementCheckerTest {
    
    @BeforeEach
    public void setUp() {
        HBaseProperties props = mock(HBaseProperties.class);
        when(props.getValue(HBasePropertyKey.MAX_SCAN_LIMIT_SIZE)).thenReturn(5000L);
        HBaseContext.getInstance().setProps(props);
    }
    
    @Test
    public void assertSelectStatement() {
        SQLStatement sqlStatement = HBaseSupportedSQLStatement.parseSQLStatement(HBaseSupportedSQLStatement.getSelectStatement());
        HBaseCheckerFactory.newInstance(sqlStatement).execute();
    }
    
    @Test
    public void assertSelectStatementWithLargeRowCount() {
        SQLStatement sqlStatement = HBaseSupportedSQLStatement.parseSQLStatement("select /*+ hbase */ * from t_order where id = 1 limit 5001");
        Exception ex = assertThrows(IllegalArgumentException.class, () -> HBaseCheckerFactory.newInstance(sqlStatement).execute());
        assertThat(ex.getMessage(), is("row count must less than 5000"));
    }
    
    @Test
    public void assertSelectStatementWithLimitSegment() {
        SQLStatement sqlStatement = HBaseSupportedSQLStatement.parseSQLStatement("select /*+ hbase */ * from t_order where id = 1 limit 5 offset 3");
        Exception ex = assertThrows(IllegalArgumentException.class, () -> HBaseCheckerFactory.newInstance(sqlStatement).execute());
        assertThat(ex.getMessage(), is("Do not supported offset segment"));
    }
    
    @Test
    public void assertSelectStatementWithLockSegment() {
        SQLStatement sqlStatement = HBaseSupportedSQLStatement.parseSQLStatement("select /*+ hbase */ * from t_order where id = 1 lock in share mode");
        Exception ex = assertThrows(IllegalArgumentException.class, () -> HBaseCheckerFactory.newInstance(sqlStatement).execute());
        assertThat(ex.getMessage(), is("Do not supported lock segment"));
    }
    
    @Test
    public void assertSelectStatementWithFunction() {
        String sql = "SELECT /*+ HBase */ sum(score) FROM person";
        SQLStatement sqlStatement = HBaseSupportedSQLStatement.parseSQLStatement(sql);
        Exception ex = assertThrows(IllegalArgumentException.class, () -> HBaseCheckerFactory.newInstance(sqlStatement).execute());
        assertThat(ex.getMessage(), is("Only supported ShorthandProjection and ColumnProjection and crc32ExpressionProjection"));
    }
    
    @Test
    public void assertSelectStatementWithJoinStatement() {
        String sql = "SELECT /*+ HBase */ * FROM t_order o JOIN t_order_item i ON o.user_id = i.user_id AND o.order_id = i.order_id";
        SQLStatement sqlStatement = HBaseSupportedSQLStatement.parseSQLStatement(sql);
        Exception ex = assertThrows(IllegalArgumentException.class, () -> HBaseCheckerFactory.newInstance(sqlStatement).execute());
        assertThat(ex.getMessage(), is("Only supported SimpleTableSegment"));
    }
    
    @Test
    public void assertSelectStatementWithMultipleInExpression() {
        String sql = "SELECT /*+ HBase */ * FROM t_order WHERE rowKey IN (?, ?) AND id IN (?, ?)";
        SQLStatement sqlStatement = HBaseSupportedSQLStatement.parseSQLStatement(sql);
        Exception ex = assertThrows(IllegalArgumentException.class, () -> HBaseCheckerFactory.newInstance(sqlStatement).execute());
        assertThat(ex.getMessage(), is("left segment must is ColumnSegment"));
    }
    
    @Test
    public void assertSelectStatementWithInExpression() {
        String sql = "SELECT /*+ HBase */ * from t_order where rowKey in (1, 2, 3)";
        SQLStatement sqlStatement = HBaseSupportedSQLStatement.parseSQLStatement(sql);
        HBaseCheckerFactory.newInstance(sqlStatement).execute();
    }
    
    @Test
    public void assertSelectStatementWithErrorKey() {
        String sql = "SELECT /*+ HBase */ * from t_order where age in (1, 2, 3)";
        SQLStatement sqlStatement = HBaseSupportedSQLStatement.parseSQLStatement(sql);
        Exception ex = assertThrows(IllegalArgumentException.class, () -> HBaseCheckerFactory.newInstance(sqlStatement).execute());
        assertThat(ex.getMessage(), is("age is not a allowed key"));
    }
    
    @Test
    public void assertExecuteSelectWithNotIn() {
        String sql = "SELECT /*+ HBase */ * from t_order where rowKey not in (1, 2, 3)";
        SQLStatement sqlStatement = HBaseSupportedSQLStatement.parseSQLStatement(sql);
        Exception ex = assertThrows(IllegalArgumentException.class, () -> HBaseCheckerFactory.newInstance(sqlStatement).execute());
        assertThat(ex.getMessage(), is("Do not supported `not in`"));
    }
    
    @Test
    public void assertExecuteSelectWithParameterMarker() {
        String sql = "SELECT /*+ HBase */ * from t_order where rowKey in (?, ?, ?)";
        SQLStatement sqlStatement = HBaseSupportedSQLStatement.parseSQLStatement(sql);
        HBaseCheckerFactory.newInstance(sqlStatement).execute();
    }
    
    @Test
    public void assertSelectStatementUseCrc32() {
        String sql = "SELECT /*+ HBase */ crc32(concat_ws('#',rowKey)) from t_order where rowKey in (1, 2, 3)";
        SQLStatement sqlStatement = HBaseSupportedSQLStatement.parseSQLStatement(sql);
        HBaseCheckerFactory.newInstance(sqlStatement).execute();
    }
    
    @Test
    public void assertExecuteSelectWithErrorInExpression() {
        String sql = "SELECT /*+ HBase */ * from t_order where rowKey in (select rowKey from t_order_item)";
        SQLStatement sqlStatement = HBaseSupportedSQLStatement.parseSQLStatement(sql);
        Exception ex = assertThrows(IllegalArgumentException.class, () -> HBaseCheckerFactory.newInstance(sqlStatement).execute());
        assertThat(ex.getMessage(), is("Only supported ListExpression"));
    }
    
    @Test
    public void assertExecuteSelectWithBetween() {
        String sql = "SELECT /*+ HBase */ * from t_order where rowKey between 1 and 2";
        SQLStatement sqlStatement = HBaseSupportedSQLStatement.parseSQLStatement(sql);
        HBaseCheckerFactory.newInstance(sqlStatement).execute();
    }
    
    @Test
    public void assertExecuteSelectWithNotBetween() {
        String sql = "SELECT /*+ HBase */ * from t_order where rowKey not between 1 and 2";
        SQLStatement sqlStatement = HBaseSupportedSQLStatement.parseSQLStatement(sql);
        Exception ex = assertThrows(IllegalArgumentException.class, () -> HBaseCheckerFactory.newInstance(sqlStatement).execute());
        assertThat(ex.getMessage(), is("Do not supported `not between...and...`"));
    }
    
    @Test
    public void assertExecuteSelectWithBetweenErrorKey() {
        String sql = "SELECT /*+ HBase */ * from t_order where age between 1 and 2";
        SQLStatement sqlStatement = HBaseSupportedSQLStatement.parseSQLStatement(sql);
        Exception ex = assertThrows(IllegalArgumentException.class, () -> HBaseCheckerFactory.newInstance(sqlStatement).execute());
        assertThat(ex.getMessage(), is("age is not a allowed key"));
    }
    
    @Test
    public void assertExecuteSelectWithErrorBetweenExpr() {
        String sql = "SELECT /*+ HBase */ * from t_order where rowKey between 1 and now()";
        SQLStatement sqlStatement = HBaseSupportedSQLStatement.parseSQLStatement(sql);
        Exception ex = assertThrows(IllegalArgumentException.class, () -> HBaseCheckerFactory.newInstance(sqlStatement).execute());
        assertThat(ex.getMessage(), is("between expr must is literal or parameter marker"));
    }
    
    @Test
    public void assertSelectWithGroupBy() {
        String sql = "SELECT /*+ HBase */ * from t_order group by order_id";
        SQLStatement sqlStatement = HBaseSupportedSQLStatement.parseSQLStatement(sql);
        Exception ex = assertThrows(IllegalArgumentException.class, () -> HBaseCheckerFactory.newInstance(sqlStatement).execute());
        assertThat(ex.getMessage(), is("Do not supported group by segment"));
    }
    
    @Test
    public void assertSelectWithNotAllowOperator() {
        String sql = "select /*+ hbase */ * from t_order where rowKey != 1";
        SQLStatement sqlStatement = HBaseSupportedSQLStatement.parseSQLStatement(sql);
        Exception ex = assertThrows(IllegalArgumentException.class, () -> HBaseCheckerFactory.newInstance(sqlStatement).execute());
        assertThat(ex.getMessage(), is("Only Supported `=` operator"));
    }
    
    @Test
    public void assertSelectWithNotAllowColumn() {
        String sql = "select /*+ hbase */ * from t_order where age = 1";
        SQLStatement sqlStatement = HBaseSupportedSQLStatement.parseSQLStatement(sql);
        Exception ex = assertThrows(IllegalArgumentException.class, () -> HBaseCheckerFactory.newInstance(sqlStatement).execute());
        assertThat(ex.getMessage(), is("age is not a allowed key"));
    }
    
    @Test
    public void assertSelectWithMultipleExpression() {
        String sql = "select /*+ hbase */ * from t_order where rowKey = 1 and age = 2";
        SQLStatement sqlStatement = HBaseSupportedSQLStatement.parseSQLStatement(sql);
        Exception ex = assertThrows(IllegalArgumentException.class, () -> HBaseCheckerFactory.newInstance(sqlStatement).execute());
        assertThat(ex.getMessage(), is("Do not supported Multiple expressions"));
    }
    
    @Test
    public void assertSelectWithNotColumnExpression() {
        String sql = "select /*+ hbase */ * from t_order where 1 = 1";
        SQLStatement sqlStatement = HBaseSupportedSQLStatement.parseSQLStatement(sql);
        Exception ex = assertThrows(IllegalArgumentException.class, () -> HBaseCheckerFactory.newInstance(sqlStatement).execute());
        assertThat(ex.getMessage(), is("left segment must is ColumnSegment"));
    }
    
    @Test
    public void assertSelectWithParameterMarker() {
        String sql = "select /*+ hbase */ rowKey, name, ? from t_order where rowKey = 'kid'";
        SQLStatement sqlStatement = HBaseSupportedSQLStatement.parseSQLStatement(sql);
        Exception ex = assertThrows(IllegalArgumentException.class, () -> HBaseCheckerFactory.newInstance(sqlStatement).execute());
        assertThat(ex.getMessage(), is("Only supported ShorthandProjection and ColumnProjection and crc32ExpressionProjection"));
    }
}
