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
import org.apache.calcite.sql.SqlNodeList;
import org.apache.calcite.sql.fun.SqlStdOperatorTable;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.InExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.expression.ExpressionConverter;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ExpressionConverter.class)
class InExpressionConverterTest {
    
    @Test
    void assertConvertReturnsEmptyWhenExpressionIsNull() {
        assertFalse(InExpressionConverter.convert(null).isPresent());
    }
    
    @Test
    void assertConvertWrapsSqlBasicCallRightAsListForNotIn() {
        LiteralExpressionSegment left = new LiteralExpressionSegment(0, 0, 1);
        LiteralExpressionSegment right = new LiteralExpressionSegment(0, 0, 2);
        SqlNode leftNode = mock(SqlNode.class);
        SqlNode rightOperand = mock(SqlNode.class);
        SqlBasicCall rightBasicCall = new SqlBasicCall(SqlStdOperatorTable.PLUS, Collections.singletonList(rightOperand), SqlParserPos.ZERO);
        when(ExpressionConverter.convert(left)).thenReturn(Optional.of(leftNode));
        when(ExpressionConverter.convert(right)).thenReturn(Optional.of(rightBasicCall));
        SqlBasicCall actual = (SqlBasicCall) InExpressionConverter.convert(new InExpression(0, 0, left, right, true)).orElse(null);
        assertNotNull(actual);
        assertThat(actual.getOperator(), is(SqlStdOperatorTable.NOT_IN));
        SqlNode secondOperand = actual.getOperandList().get(1);
        assertThat(secondOperand, instanceOf(SqlNodeList.class));
        assertThat(((SqlNodeList) secondOperand).size(), is(1));
        assertThat(((SqlNodeList) secondOperand).get(0), is(rightOperand));
    }
    
    @Test
    void assertConvertAddsNonBasicCallRightForIn() {
        LiteralExpressionSegment left = new LiteralExpressionSegment(0, 0, 1);
        LiteralExpressionSegment right = new LiteralExpressionSegment(0, 0, 2);
        SqlNode leftNode = mock(SqlNode.class);
        SqlNode rightNode = mock(SqlNode.class);
        when(ExpressionConverter.convert(left)).thenReturn(Optional.of(leftNode));
        when(ExpressionConverter.convert(right)).thenReturn(Optional.of(rightNode));
        SqlBasicCall actual = (SqlBasicCall) InExpressionConverter.convert(new InExpression(0, 0, left, right, false)).orElse(null);
        assertNotNull(actual);
        assertThat(actual.getOperator(), is(SqlStdOperatorTable.IN));
        assertThat(actual.getOperandList(), is(Arrays.asList(leftNode, rightNode)));
    }
}
