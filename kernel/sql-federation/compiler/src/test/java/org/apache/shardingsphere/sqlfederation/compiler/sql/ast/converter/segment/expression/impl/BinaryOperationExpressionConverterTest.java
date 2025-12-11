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
import org.apache.calcite.sql.SqlNodeList;
import org.apache.calcite.sql.SqlNumericLiteral;
import org.apache.calcite.sql.SqlOperator;
import org.apache.calcite.sql.fun.SqlStdOperatorTable;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.QuantifySubqueryExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.expression.ExpressionConverter;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ExpressionConverter.class)
class BinaryOperationExpressionConverterTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Test
    void assertConvertThrowsForUnsupportedOperator() {
        BinaryOperationExpression expression = new BinaryOperationExpression(0, 0, new LiteralExpressionSegment(0, 0, 1), new LiteralExpressionSegment(0, 0, 1), "UNSUPPORTED", "");
        assertThrows(IllegalStateException.class, () -> BinaryOperationExpressionConverter.convert(expression));
    }
    
    @Test
    void assertConvertThrowsWhenIsOperatorUnsupportedLiteral() {
        LiteralExpressionSegment left = new LiteralExpressionSegment(0, 0, 1);
        LiteralExpressionSegment right = new LiteralExpressionSegment(0, 0, "unknown");
        BinaryOperationExpression expression = new BinaryOperationExpression(0, 0, left, right, "IS", "");
        assertThrows(IllegalStateException.class, () -> BinaryOperationExpressionConverter.convert(expression));
    }
    
    @Test
    void assertConvertIsNullOperator() {
        LiteralExpressionSegment left = new LiteralExpressionSegment(0, 0, 1);
        LiteralExpressionSegment right = new LiteralExpressionSegment(0, 0, "NULL");
        SqlNode leftNode = mock(SqlNode.class);
        when(ExpressionConverter.convert(left)).thenReturn(Optional.of(leftNode));
        SqlBasicCall actual = (SqlBasicCall) BinaryOperationExpressionConverter.convert(new BinaryOperationExpression(0, 0, left, right, "IS", "")).orElse(null);
        assertNotNull(actual);
        assertThat(actual.getOperator(), is(SqlStdOperatorTable.IS_NULL));
        assertThat(actual.getOperandList(), is(Collections.singletonList(leftNode)));
    }
    
    @Test
    void assertConvertIsNotNullOperator() {
        LiteralExpressionSegment left = new LiteralExpressionSegment(0, 0, 1);
        LiteralExpressionSegment right = new LiteralExpressionSegment(0, 0, "NOT NULL");
        SqlNode leftNode = mock(SqlNode.class);
        when(ExpressionConverter.convert(left)).thenReturn(Optional.of(leftNode));
        SqlBasicCall actual = (SqlBasicCall) BinaryOperationExpressionConverter.convert(new BinaryOperationExpression(0, 0, left, right, "IS", "")).orElse(null);
        assertNotNull(actual);
        assertThat(actual.getOperator(), is(SqlStdOperatorTable.IS_NOT_NULL));
        assertThat(actual.getOperandList(), is(Collections.singletonList(leftNode)));
    }
    
    @Test
    void assertConvertIsFalseConvertsNumericZeroToBooleanFalse() {
        LiteralExpressionSegment left = new LiteralExpressionSegment(0, 0, 0);
        LiteralExpressionSegment right = new LiteralExpressionSegment(0, 0, "FALSE");
        SqlNumericLiteral leftNode = SqlLiteral.createExactNumeric("0", SqlParserPos.ZERO);
        when(ExpressionConverter.convert(left)).thenReturn(Optional.of(leftNode));
        SqlBasicCall actual = (SqlBasicCall) BinaryOperationExpressionConverter.convert(new BinaryOperationExpression(0, 0, left, right, "IS", "")).orElse(null);
        assertNotNull(actual);
        assertThat(actual.getOperator(), is(SqlStdOperatorTable.IS_FALSE));
        SqlLiteral operand = (SqlLiteral) actual.getOperandList().get(0);
        assertThat(operand.getValueAs(Boolean.class), is(false));
    }
    
    @Test
    void assertConvertIsNotFalseConvertsNumericToBooleanTrue() {
        LiteralExpressionSegment left = new LiteralExpressionSegment(0, 0, 2);
        LiteralExpressionSegment right = new LiteralExpressionSegment(0, 0, "NOT FALSE");
        SqlNumericLiteral leftNode = SqlLiteral.createExactNumeric("2", SqlParserPos.ZERO);
        when(ExpressionConverter.convert(left)).thenReturn(Optional.of(leftNode));
        SqlBasicCall actual = (SqlBasicCall) BinaryOperationExpressionConverter.convert(new BinaryOperationExpression(0, 0, left, right, "IS", "")).orElse(null);
        assertNotNull(actual);
        assertThat(actual.getOperator(), is(SqlStdOperatorTable.IS_NOT_FALSE));
        SqlLiteral operand = (SqlLiteral) actual.getOperandList().get(0);
        assertThat(operand.getValueAs(Boolean.class), is(true));
    }
    
    @Test
    void assertConvertIsTrueKeepsNonNumericLeft() {
        LiteralExpressionSegment left = new LiteralExpressionSegment(0, 0, "left");
        LiteralExpressionSegment right = new LiteralExpressionSegment(0, 0, "TRUE");
        SqlNode leftNode = mock(SqlNode.class);
        when(ExpressionConverter.convert(left)).thenReturn(Optional.of(leftNode));
        SqlBasicCall actual = (SqlBasicCall) BinaryOperationExpressionConverter.convert(new BinaryOperationExpression(0, 0, left, right, "IS", "")).orElse(null);
        assertNotNull(actual);
        assertThat(actual.getOperator(), is(SqlStdOperatorTable.IS_TRUE));
        assertThat(actual.getOperandList().get(0), is(leftNode));
    }
    
    @Test
    void assertConvertIsNotTrueConvertsNumericZeroToBooleanFalse() {
        LiteralExpressionSegment left = new LiteralExpressionSegment(0, 0, 0);
        LiteralExpressionSegment right = new LiteralExpressionSegment(0, 0, "NOT TRUE");
        SqlNumericLiteral leftNode = SqlLiteral.createExactNumeric("0", SqlParserPos.ZERO);
        when(ExpressionConverter.convert(left)).thenReturn(Optional.of(leftNode));
        SqlBasicCall actual = (SqlBasicCall) BinaryOperationExpressionConverter.convert(new BinaryOperationExpression(0, 0, left, right, "IS", "")).orElse(null);
        assertNotNull(actual);
        assertThat(actual.getOperator(), is(SqlStdOperatorTable.IS_NOT_TRUE));
        SqlLiteral operand = (SqlLiteral) actual.getOperandList().get(0);
        assertThat(operand.getValueAs(Boolean.class), is(false));
    }
    
    @Test
    void assertConvertFlattensSqlNodeListOperands() {
        LiteralExpressionSegment left = new LiteralExpressionSegment(0, 0, 1);
        LiteralExpressionSegment right = new LiteralExpressionSegment(0, 0, 2);
        BinaryOperationExpression expression = new BinaryOperationExpression(0, 0, left, right, "+", "");
        SqlNode leftNode = mock(SqlNode.class);
        SqlNode firstRightNode = mock(SqlNode.class);
        SqlNode secondRightNode = mock(SqlNode.class);
        when(ExpressionConverter.convert(left)).thenReturn(Optional.of(leftNode));
        when(ExpressionConverter.convert(right)).thenReturn(Optional.of(new SqlNodeList(Arrays.asList(firstRightNode, secondRightNode), SqlParserPos.ZERO)));
        SqlBasicCall actual = (SqlBasicCall) BinaryOperationExpressionConverter.convert(expression).orElse(null);
        assertNotNull(actual);
        assertThat(actual.getOperator(), is(SqlStdOperatorTable.PLUS));
        assertThat(actual.getOperandList(), is(Arrays.asList(leftNode, firstRightNode, secondRightNode)));
    }
    
    @Test
    void assertConvertQuantifySubqueryFallsBackToOriginalOperator() {
        LiteralExpressionSegment left = new LiteralExpressionSegment(0, 0, 1);
        QuantifySubqueryExpression right = new QuantifySubqueryExpression(0, 0, new SubquerySegment(0, 0, new SelectStatement(databaseType), "sub"), "ALL");
        SqlNode leftNode = mock(SqlNode.class);
        SqlNode rightNode = mock(SqlNode.class);
        when(ExpressionConverter.convert(left)).thenReturn(Optional.of(leftNode));
        when(ExpressionConverter.convert(right)).thenReturn(Optional.of(rightNode));
        SqlBasicCall actual = (SqlBasicCall) BinaryOperationExpressionConverter.convert(new BinaryOperationExpression(0, 0, left, right, "AND", "")).orElse(null);
        assertNotNull(actual);
        assertThat(actual.getOperator(), is(SqlStdOperatorTable.AND));
        assertThat(actual.getOperandList(), is(Arrays.asList(leftNode, rightNode)));
    }
    
    @ParameterizedTest(name = "{0} {1}")
    @MethodSource("provideQuantifyOperators")
    void assertConvertQuantifySubquery(final String operator, final String quantifyOperator, final SqlOperator expectedOperator) {
        LiteralExpressionSegment left = new LiteralExpressionSegment(0, 0, 1);
        QuantifySubqueryExpression right = new QuantifySubqueryExpression(0, 0, new SubquerySegment(0, 0, new SelectStatement(databaseType), "sub"), quantifyOperator);
        SqlNode leftNode = mock(SqlNode.class);
        SqlNode rightNode = mock(SqlNode.class);
        when(ExpressionConverter.convert(left)).thenReturn(Optional.of(leftNode));
        when(ExpressionConverter.convert(right)).thenReturn(Optional.of(rightNode));
        SqlBasicCall actual = (SqlBasicCall) BinaryOperationExpressionConverter.convert(new BinaryOperationExpression(0, 0, left, right, operator, "")).orElse(null);
        assertNotNull(actual);
        assertThat(actual.getOperator(), is(expectedOperator));
        assertThat(actual.getOperandList(), is(Arrays.asList(leftNode, rightNode)));
    }
    
    private static Stream<Arguments> provideQuantifyOperators() {
        return Stream.of(
                Arguments.of("=", "ALL", SqlStdOperatorTable.ALL_EQ),
                Arguments.of("=", "SOME", SqlStdOperatorTable.SOME_EQ),
                Arguments.of(">", "ALL", SqlStdOperatorTable.ALL_GT),
                Arguments.of(">", "SOME", SqlStdOperatorTable.SOME_GT),
                Arguments.of(">=", "ALL", SqlStdOperatorTable.ALL_GE),
                Arguments.of(">=", "SOME", SqlStdOperatorTable.SOME_GE),
                Arguments.of("<", "ALL", SqlStdOperatorTable.ALL_LT),
                Arguments.of("<", "SOME", SqlStdOperatorTable.SOME_LT),
                Arguments.of("<=", "ALL", SqlStdOperatorTable.ALL_LE),
                Arguments.of("<=", "SOME", SqlStdOperatorTable.SOME_LE),
                Arguments.of("!=", "ALL", SqlStdOperatorTable.ALL_NE),
                Arguments.of("<>", "SOME", SqlStdOperatorTable.SOME_NE)
        );
    }
}
