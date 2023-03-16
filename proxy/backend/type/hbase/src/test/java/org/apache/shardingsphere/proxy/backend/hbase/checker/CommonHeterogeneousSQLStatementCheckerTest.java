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
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.junit.Test;
import java.util.Optional;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public final class CommonHeterogeneousSQLStatementCheckerTest {
    
    @Test
    public void assertIsSinglePoint() {
        SelectStatement sqlStatement = (SelectStatement) HBaseSupportedSQLStatement.parseSQLStatement("select /*+ hbase */ * from t_order where rowKey = '1'");
        CommonHeterogeneousSQLStatementChecker<SQLStatement> commonHeterogeneousSQLStatementChecker = new CommonHeterogeneousSQLStatementChecker<>(sqlStatement);
        commonHeterogeneousSQLStatementChecker.checkIsSinglePointQuery(sqlStatement.getWhere());
    }
    
    @Test
    public void assertIsSinglePointWithErrorKey() {
        SelectStatement sqlStatement = (SelectStatement) HBaseSupportedSQLStatement.parseSQLStatement("select /*+ hbase */ * from t_order where a = '1'");
        CommonHeterogeneousSQLStatementChecker<SQLStatement> commonHeterogeneousSQLStatementChecker = new CommonHeterogeneousSQLStatementChecker<>(sqlStatement);
        try {
            commonHeterogeneousSQLStatementChecker.checkIsSinglePointQuery(sqlStatement.getWhere());
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals(e.getMessage(), "a is not a allowed key");
        }
    }
    
    @Test
    public void assertIsSinglePointWithErrorOperation() {
        SelectStatement sqlStatement = (SelectStatement) HBaseSupportedSQLStatement.parseSQLStatement("select /*+ hbase */ * from t_order where rowKey between '1' and '2' ");
        CommonHeterogeneousSQLStatementChecker<SQLStatement> commonHeterogeneousSQLStatementChecker = new CommonHeterogeneousSQLStatementChecker<>(sqlStatement);
        try {
            commonHeterogeneousSQLStatementChecker.checkIsSinglePointQuery(sqlStatement.getWhere());
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals(e.getMessage(), "Only Support BinaryOperationExpression");
        }
    }
    
    @Test
    public void assertInExpression() {
        SelectStatement sqlStatement = (SelectStatement) HBaseSupportedSQLStatement.parseSQLStatement("select /*+ hbase */ * from t_order where rowKey in ('1', '2') ");
        CommonHeterogeneousSQLStatementChecker<SQLStatement> commonHeterogeneousSQLStatementChecker = new CommonHeterogeneousSQLStatementChecker<>(sqlStatement);
        Optional<WhereSegment> whereSegment = sqlStatement.getWhere();
        if (whereSegment.isPresent()) {
            commonHeterogeneousSQLStatementChecker.checkInExpressionIsExpected(whereSegment.get().getExpr());
        } else {
            fail();
        }
    }
    
    @Test
    public void assertInExpressionWithNotIn() {
        SelectStatement sqlStatement = (SelectStatement) HBaseSupportedSQLStatement.parseSQLStatement("select /*+ hbase */ * from t_order where rowKey not in ('1', '2') ");
        CommonHeterogeneousSQLStatementChecker<SQLStatement> commonHeterogeneousSQLStatementChecker = new CommonHeterogeneousSQLStatementChecker<>(sqlStatement);
        Optional<WhereSegment> whereSegment = sqlStatement.getWhere();
        if (whereSegment.isPresent()) {
            try {
                commonHeterogeneousSQLStatementChecker.checkInExpressionIsExpected(whereSegment.get().getExpr());
                fail();
            } catch (IllegalArgumentException e) {
                assertEquals(e.getMessage(), "Do not supported `not in`");
            }
        } else {
            fail();
        }
    }
    
    @Test
    public void assertInExpressionWithErrorKey() {
        SelectStatement sqlStatement = (SelectStatement) HBaseSupportedSQLStatement.parseSQLStatement("select /*+ hbase */ * from t_order where a in ('1', '2') ");
        CommonHeterogeneousSQLStatementChecker<SQLStatement> commonHeterogeneousSQLStatementChecker = new CommonHeterogeneousSQLStatementChecker<>(sqlStatement);
        Optional<WhereSegment> whereSegment = sqlStatement.getWhere();
        if (whereSegment.isPresent()) {
            try {
                commonHeterogeneousSQLStatementChecker.checkInExpressionIsExpected(whereSegment.get().getExpr());
                fail();
            } catch (IllegalArgumentException e) {
                assertEquals(e.getMessage(), "a is not a allowed key");
            }
        } else {
            fail();
        }
    }
    
    @Test
    public void assertIsAllowExpressionSegment() {
        SelectStatement sqlStatement = (SelectStatement) HBaseSupportedSQLStatement.parseSQLStatement("select /*+ hbase */ * from t_order where rowKey BETWEEN 'v1' AND 'v2' ");
        CommonHeterogeneousSQLStatementChecker<SQLStatement> commonHeterogeneousSQLStatementChecker = new CommonHeterogeneousSQLStatementChecker<>(sqlStatement);
        Optional<WhereSegment> whereSegment = sqlStatement.getWhere();
        if (whereSegment.isPresent()) {
            BetweenExpression betweenExpression = (BetweenExpression) whereSegment.get().getExpr();
            assertTrue(commonHeterogeneousSQLStatementChecker.isAllowExpressionSegment(betweenExpression.getBetweenExpr()));
            assertTrue(commonHeterogeneousSQLStatementChecker.isAllowExpressionSegment(betweenExpression.getAndExpr()));
        } else {
            fail();
        }
    }
    
    @Test
    public void assertIsAllowExpressionSegmentError() {
        SelectStatement sqlStatement = (SelectStatement) HBaseSupportedSQLStatement.parseSQLStatement("select /*+ hbase */ * from t_order where rowKey = '1'");
        CommonHeterogeneousSQLStatementChecker<SQLStatement> commonHeterogeneousSQLStatementChecker = new CommonHeterogeneousSQLStatementChecker<>(sqlStatement);
        Optional<WhereSegment> whereSegment = sqlStatement.getWhere();
        if (whereSegment.isPresent()) {
            assertFalse(commonHeterogeneousSQLStatementChecker.isAllowExpressionSegment(whereSegment.get().getExpr()));
        } else {
            fail();
        }
    }
}
