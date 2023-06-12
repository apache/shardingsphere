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
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BetweenExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommonHeterogeneousSQLStatementCheckerTest {
    
    @Test
    void assertIsSinglePoint() {
        SelectStatement sqlStatement = (SelectStatement) HBaseSupportedSQLStatement.parseSQLStatement("select /*+ hbase */ * from t_order where rowKey = '1'");
        assertTrue(sqlStatement.getWhere().isPresent());
        new CommonHeterogeneousSQLStatementChecker(sqlStatement).checkIsSinglePointQuery(sqlStatement.getWhere().get());
    }
    
    @Test
    void assertIsSinglePointWithErrorKey() {
        SelectStatement sqlStatement = (SelectStatement) HBaseSupportedSQLStatement.parseSQLStatement("select /*+ hbase */ * from t_order where a = '1'");
        assertTrue(sqlStatement.getWhere().isPresent());
        Exception ex = assertThrows(IllegalArgumentException.class, () -> new CommonHeterogeneousSQLStatementChecker(sqlStatement).checkIsSinglePointQuery(sqlStatement.getWhere().get()));
        assertThat(ex.getMessage(), is("a is not a allowed key."));
    }
    
    @Test
    void assertIsSinglePointWithErrorOperation() {
        SelectStatement sqlStatement = (SelectStatement) HBaseSupportedSQLStatement.parseSQLStatement("select /*+ hbase */ * from t_order where rowKey between '1' and '2' ");
        assertTrue(sqlStatement.getWhere().isPresent());
        Exception ex = assertThrows(IllegalArgumentException.class, () -> new CommonHeterogeneousSQLStatementChecker(sqlStatement).checkIsSinglePointQuery(sqlStatement.getWhere().get()));
        assertThat(ex.getMessage(), is("Only support binary operation expression."));
    }
    
    @Test
    void assertInExpression() {
        SelectStatement sqlStatement = (SelectStatement) HBaseSupportedSQLStatement.parseSQLStatement("select /*+ hbase */ * from t_order where rowKey in ('1', '2') ");
        Optional<WhereSegment> whereSegment = sqlStatement.getWhere();
        assertTrue(whereSegment.isPresent());
        new CommonHeterogeneousSQLStatementChecker(sqlStatement).checkInExpressionIsExpected(whereSegment.get().getExpr());
    }
    
    @Test
    void assertInExpressionWithNotIn() {
        SelectStatement sqlStatement = (SelectStatement) HBaseSupportedSQLStatement.parseSQLStatement("select /*+ hbase */ * from t_order where rowKey not in ('1', '2') ");
        Optional<WhereSegment> whereSegment = sqlStatement.getWhere();
        assertTrue(whereSegment.isPresent());
        Exception ex = assertThrows(IllegalArgumentException.class, () -> new CommonHeterogeneousSQLStatementChecker(sqlStatement).checkInExpressionIsExpected(whereSegment.get().getExpr()));
        assertThat(ex.getMessage(), is("Do not supported `not in`."));
    }
    
    @Test
    void assertInExpressionWithErrorKey() {
        SelectStatement sqlStatement = (SelectStatement) HBaseSupportedSQLStatement.parseSQLStatement("select /*+ hbase */ * from t_order where a in ('1', '2') ");
        Optional<WhereSegment> whereSegment = sqlStatement.getWhere();
        assertTrue(whereSegment.isPresent());
        Exception ex = assertThrows(IllegalArgumentException.class, () -> new CommonHeterogeneousSQLStatementChecker(sqlStatement).checkInExpressionIsExpected(whereSegment.get().getExpr()));
        assertThat(ex.getMessage(), is("a is not a allowed key."));
    }
    
    @Test
    void assertIsAllowExpressionSegment() {
        SelectStatement sqlStatement = (SelectStatement) HBaseSupportedSQLStatement.parseSQLStatement("select /*+ hbase */ * from t_order where rowKey BETWEEN 'v1' AND 'v2' ");
        Optional<WhereSegment> whereSegment = sqlStatement.getWhere();
        assertTrue(whereSegment.isPresent());
        BetweenExpression betweenExpression = (BetweenExpression) whereSegment.get().getExpr();
        CommonHeterogeneousSQLStatementChecker checker = new CommonHeterogeneousSQLStatementChecker(sqlStatement);
        assertTrue(checker.isAllowExpressionSegment(betweenExpression.getBetweenExpr()));
        assertTrue(checker.isAllowExpressionSegment(betweenExpression.getAndExpr()));
    }
    
    @Test
    void assertIsAllowExpressionSegmentError() {
        SelectStatement sqlStatement = (SelectStatement) HBaseSupportedSQLStatement.parseSQLStatement("select /*+ hbase */ * from t_order where rowKey = '1'");
        Optional<WhereSegment> whereSegment = sqlStatement.getWhere();
        assertTrue(whereSegment.isPresent());
        assertFalse(new CommonHeterogeneousSQLStatementChecker(sqlStatement).isAllowExpressionSegment(whereSegment.get().getExpr()));
    }
}
