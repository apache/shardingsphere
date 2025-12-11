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
import org.apache.calcite.sql.SqlLiteral;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.fun.SqlCase;
import org.apache.calcite.sql.fun.SqlStdOperatorTable;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.CaseWhenExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.expression.ExpressionConverter;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ExpressionConverter.class)
class CaseWhenExpressionConverterTest {
    
    @Test
    void assertConvertCaseExpressionWithElseExpr() {
        ExpressionSegment caseExpr = new LiteralExpressionSegment(0, 0, "case_expr");
        ExpressionSegment whenExpr = new LiteralExpressionSegment(0, 0, "when_expr");
        ExpressionSegment thenExpr = new LiteralExpressionSegment(0, 0, "then_expr");
        ExpressionSegment elseExpr = new LiteralExpressionSegment(0, 0, "else_expr");
        CaseWhenExpression expression = new CaseWhenExpression(0, 0, caseExpr, Collections.singleton(whenExpr), Collections.singleton(thenExpr), elseExpr);
        SqlNode caseNode = mock(SqlNode.class);
        SqlNode whenNode = mock(SqlNode.class);
        SqlNode thenNode = mock(SqlNode.class);
        SqlNode elseNode = mock(SqlNode.class);
        when(ExpressionConverter.convert(caseExpr)).thenReturn(Optional.of(caseNode));
        when(ExpressionConverter.convert(whenExpr)).thenReturn(Optional.of(whenNode));
        when(ExpressionConverter.convert(thenExpr)).thenReturn(Optional.of(thenNode));
        when(ExpressionConverter.convert(elseExpr)).thenReturn(Optional.of(elseNode));
        SqlCase actual = (SqlCase) CaseWhenExpressionConverter.convert(expression).orElse(null);
        assertNotNull(actual);
        SqlBasicCall whenCall = (SqlBasicCall) actual.getWhenOperands().get(0);
        assertThat(whenCall.getOperator(), is(SqlStdOperatorTable.EQUALS));
        assertThat(whenCall.getOperandList().get(0), is(caseNode));
        assertThat(whenCall.getOperandList().get(1), is(whenNode));
        assertThat(actual.getThenOperands().get(0), is(thenNode));
        assertThat(actual.getElseOperand(), is(elseNode));
    }
    
    @Test
    void assertConvertSearchedCaseWithDefaultElse() {
        ExpressionSegment whenExpr = new LiteralExpressionSegment(0, 0, "when_expr");
        ExpressionSegment thenExpr = new LiteralExpressionSegment(0, 0, "then_expr");
        CaseWhenExpression expression = new CaseWhenExpression(0, 0, null, Collections.singleton(whenExpr), Collections.singleton(thenExpr), null);
        SqlNode whenNode = mock(SqlNode.class);
        SqlNode thenNode = mock(SqlNode.class);
        when(ExpressionConverter.convert(whenExpr)).thenReturn(Optional.of(whenNode));
        when(ExpressionConverter.convert(thenExpr)).thenReturn(Optional.of(thenNode));
        when(ExpressionConverter.convert(null)).thenReturn(Optional.empty());
        SqlCase actual = (SqlCase) CaseWhenExpressionConverter.convert(expression).orElse(null);
        assertNotNull(actual);
        assertThat(actual.getValueOperand(), is((SqlNode) null));
        assertThat(actual.getWhenOperands().get(0), is(whenNode));
        assertThat(actual.getThenOperands().get(0), is(thenNode));
        assertNotNull(actual.getElseOperand());
        assertThat(((SqlLiteral) actual.getElseOperand()).getValueAs(String.class), is("NULL"));
    }
    
    @Test
    void assertConvertSkipsWhenExpressionIfConversionEmpty() {
        ExpressionSegment caseExpr = new LiteralExpressionSegment(0, 0, "case_expr");
        ExpressionSegment whenExpr = new LiteralExpressionSegment(0, 0, "when_expr");
        ExpressionSegment thenExpr = new LiteralExpressionSegment(0, 0, "then_expr");
        CaseWhenExpression expression = new CaseWhenExpression(0, 0, caseExpr, Collections.singleton(whenExpr), Collections.singleton(thenExpr), null);
        SqlNode caseNode = mock(SqlNode.class);
        SqlNode thenNode = mock(SqlNode.class);
        when(ExpressionConverter.convert(caseExpr)).thenReturn(Optional.of(caseNode));
        when(ExpressionConverter.convert(whenExpr)).thenReturn(Optional.empty());
        when(ExpressionConverter.convert(thenExpr)).thenReturn(Optional.of(thenNode));
        when(ExpressionConverter.convert(null)).thenReturn(Optional.empty());
        SqlCase actual = (SqlCase) CaseWhenExpressionConverter.convert(expression).orElse(null);
        assertNotNull(actual);
        assertTrue(actual.getWhenOperands().isEmpty());
        assertThat(actual.getThenOperands().get(0), is(thenNode));
    }
}
