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

package org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.expression.impl;

import org.apache.calcite.sql.SqlBasicCall;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.fun.SqlStdOperatorTable;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.BetweenExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.expression.ExpressionConverter;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Arrays;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ExpressionConverter.class)
class BetweenExpressionConverterTest {
    
    @Test
    void assertConvertReturnsEmptyForNullExpression() {
        assertFalse(BetweenExpressionConverter.convert(null).isPresent());
    }
    
    @Test
    void assertConvertBetweenExpression() {
        ExpressionSegment left = new LiteralExpressionSegment(0, 0, "left");
        ExpressionSegment betweenExpr = new LiteralExpressionSegment(0, 0, "between");
        ExpressionSegment andExpr = new LiteralExpressionSegment(0, 0, "and");
        SqlNode leftNode = mock(SqlNode.class);
        SqlNode betweenNode = mock(SqlNode.class);
        SqlNode andNode = mock(SqlNode.class);
        when(ExpressionConverter.convert(left)).thenReturn(Optional.of(leftNode));
        when(ExpressionConverter.convert(betweenExpr)).thenReturn(Optional.of(betweenNode));
        when(ExpressionConverter.convert(andExpr)).thenReturn(Optional.of(andNode));
        Optional<SqlNode> actual = BetweenExpressionConverter.convert(new BetweenExpression(0, 0, left, betweenExpr, andExpr, false));
        assertTrue(actual.isPresent());
        SqlBasicCall call = (SqlBasicCall) actual.orElse(null);
        assertThat(call.getOperator(), is(SqlStdOperatorTable.BETWEEN));
        assertThat(call.getOperandList(), is(Arrays.asList(leftNode, betweenNode, andNode)));
    }
    
    @Test
    void assertConvertNotBetweenExpression() {
        ExpressionSegment left = new LiteralExpressionSegment(0, 0, "left");
        ExpressionSegment betweenExpr = new LiteralExpressionSegment(0, 0, "between");
        ExpressionSegment andExpr = new LiteralExpressionSegment(0, 0, "and");
        SqlNode leftNode = mock(SqlNode.class);
        SqlNode betweenNode = mock(SqlNode.class);
        SqlNode andNode = mock(SqlNode.class);
        when(ExpressionConverter.convert(left)).thenReturn(Optional.of(leftNode));
        when(ExpressionConverter.convert(betweenExpr)).thenReturn(Optional.of(betweenNode));
        when(ExpressionConverter.convert(andExpr)).thenReturn(Optional.of(andNode));
        Optional<SqlNode> actual = BetweenExpressionConverter.convert(new BetweenExpression(0, 0, left, betweenExpr, andExpr, true));
        assertTrue(actual.isPresent());
        SqlBasicCall call = (SqlBasicCall) actual.orElse(null);
        assertThat(call.getOperator(), is(SqlStdOperatorTable.NOT_BETWEEN));
        assertThat(call.getOperandList(), is(Arrays.asList(leftNode, betweenNode, andNode)));
    }
}
