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
import org.apache.calcite.sql.fun.SqlTrimFunction.Flag;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.FunctionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.expression.ExpressionConverter;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ExpressionConverter.class)
class TrimFunctionConverterTest {
    
    @Test
    void assertConvertSingleParameterAddsDefaultTrimOptions() {
        FunctionSegment segment = new FunctionSegment(0, 0, "TRIM", "TRIM");
        LiteralExpressionSegment parameter = new LiteralExpressionSegment(0, 0, "param");
        segment.getParameters().add(parameter);
        SqlNode paramNode = mock(SqlNode.class);
        when(ExpressionConverter.convert(parameter)).thenReturn(Optional.of(paramNode));
        SqlBasicCall actual = TrimFunctionConverter.convert(segment);
        SqlLiteral trimFlag = (SqlLiteral) actual.getOperandList().get(0);
        assertThat(trimFlag.getValueAs(Flag.class), is(Flag.BOTH));
        SqlLiteral padding = (SqlLiteral) actual.getOperandList().get(1);
        assertThat(padding.getValueAs(String.class), is(" "));
        assertThat(actual.getOperandList().get(2), is(paramNode));
    }
    
    @Test
    void assertConvertTwoParametersAddsDefaultFlag() {
        FunctionSegment segment = new FunctionSegment(0, 0, "TRIM", "TRIM");
        LiteralExpressionSegment first = new LiteralExpressionSegment(0, 0, "first");
        LiteralExpressionSegment second = new LiteralExpressionSegment(0, 0, "second");
        segment.getParameters().add(first);
        segment.getParameters().add(second);
        SqlNode firstNode = mock(SqlNode.class);
        SqlNode secondNode = mock(SqlNode.class);
        when(ExpressionConverter.convert(first)).thenReturn(Optional.of(firstNode));
        when(ExpressionConverter.convert(second)).thenReturn(Optional.of(secondNode));
        SqlBasicCall actual = TrimFunctionConverter.convert(segment);
        SqlLiteral trimFlag = (SqlLiteral) actual.getOperandList().get(0);
        assertThat(trimFlag.getValueAs(Flag.class), is(Flag.BOTH));
        assertThat(actual.getOperandList().get(1), is(firstNode));
        assertThat(actual.getOperandList().get(2), is(secondNode));
    }
}
