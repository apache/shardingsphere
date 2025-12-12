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
import org.apache.calcite.sql.SqlIdentifier;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlNodeList;
import org.apache.calcite.sql.SqlUnresolvedFunction;
import org.apache.calcite.sql.SqlWindow;
import org.apache.calcite.sql.fun.SqlStdOperatorTable;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.FunctionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.WindowItemSegment;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.expression.ExpressionConverter;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.generic.OwnerConverter;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.window.WindowConverter;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings({ExpressionConverter.class, TrimFunctionConverter.class, WindowFunctionConverter.class, WindowConverter.class, OwnerConverter.class})
class FunctionConverterTest {
    
    @Test
    void assertConvertReturnsCurrentUserIdentifier() {
        FunctionSegment segment = new FunctionSegment(0, 0, "CURRENT_USER", "CURRENT_USER");
        SqlIdentifier actual = (SqlIdentifier) FunctionConverter.convert(segment).orElse(null);
        assertNotNull(actual);
        assertThat(actual.getSimple(), is("CURRENT_USER"));
    }
    
    @Test
    void assertConvertDelegatesToTrimFunctionConverter() {
        FunctionSegment segment = new FunctionSegment(0, 0, "TRIM", "TRIM");
        SqlBasicCall expected = mock(SqlBasicCall.class);
        when(TrimFunctionConverter.convert(segment)).thenReturn(expected);
        SqlNode actual = FunctionConverter.convert(segment).orElse(null);
        assertThat(actual, is(expected));
    }
    
    @Test
    void assertConvertDelegatesToWindowFunctionConverter() {
        FunctionSegment segment = new FunctionSegment(0, 0, "OVER", "OVER");
        SqlNode expected = mock(SqlNode.class);
        when(WindowFunctionConverter.convert(segment)).thenReturn(Optional.of(expected));
        SqlNode actual = FunctionConverter.convert(segment).orElse(null);
        assertThat(actual, is(expected));
    }
    
    @Test
    void assertConvertResolvedFunctionWithWindow() {
        FunctionSegment segment = new FunctionSegment(0, 0, "COUNT", "COUNT");
        ExpressionSegment param = new LiteralExpressionSegment(0, 0, 1);
        segment.getParameters().add(param);
        WindowItemSegment windowItemSegment = new WindowItemSegment(0, 0);
        segment.setWindow(windowItemSegment);
        SqlNode paramNode = mock(SqlNode.class);
        SqlWindow windowNode = mock(SqlWindow.class);
        when(ExpressionConverter.convert(param)).thenReturn(Optional.of(paramNode));
        when(WindowConverter.convertWindowItem(windowItemSegment)).thenReturn(windowNode);
        SqlBasicCall actual = (SqlBasicCall) FunctionConverter.convert(segment).orElse(null);
        assertNotNull(actual);
        assertThat(actual.getOperator(), is(SqlStdOperatorTable.OVER));
        SqlBasicCall functionCall = (SqlBasicCall) actual.getOperandList().get(0);
        assertThat(functionCall.getOperator().getName(), is("COUNT"));
        assertThat(functionCall.getOperandList(), is(Collections.singletonList(paramNode)));
        assertThat(actual.getOperandList().get(1), is(windowNode));
    }
    
    @Test
    void assertConvertResolvedFunctionWithoutWindowFlattensParameters() {
        FunctionSegment segment = new FunctionSegment(0, 0, "SUM", "SUM");
        ExpressionSegment firstParam = new LiteralExpressionSegment(0, 0, "list");
        ExpressionSegment secondParam = new LiteralExpressionSegment(0, 0, "single");
        segment.getParameters().add(firstParam);
        segment.getParameters().add(secondParam);
        SqlNode nodeInList = mock(SqlNode.class);
        SqlNode listSecondNode = mock(SqlNode.class);
        SqlNode secondNode = mock(SqlNode.class);
        when(ExpressionConverter.convert(firstParam)).thenReturn(Optional.of(new SqlNodeList(Arrays.asList(nodeInList, listSecondNode), SqlParserPos.ZERO)));
        when(ExpressionConverter.convert(secondParam)).thenReturn(Optional.of(secondNode));
        SqlBasicCall actual = (SqlBasicCall) FunctionConverter.convert(segment).orElse(null);
        assertNotNull(actual);
        assertThat(actual.getOperator().getName(), is("SUM"));
        assertThat(actual.getOperandList(), is(Arrays.asList(nodeInList, listSecondNode, secondNode)));
    }
    
    @Test
    void assertConvertUnresolvedFunctionWithOwner() {
        FunctionSegment segment = new FunctionSegment(0, 0, "custom_func", "custom_func");
        OwnerSegment owner = new OwnerSegment(0, 0, new IdentifierValue("schema"));
        segment.setOwner(owner);
        ExpressionSegment param = new LiteralExpressionSegment(0, 0, "p");
        segment.getParameters().add(param);
        SqlNode paramNode = mock(SqlNode.class);
        when(OwnerConverter.convert(owner)).thenReturn(new ArrayList<>());
        when(ExpressionConverter.convert(param)).thenReturn(Optional.of(paramNode));
        SqlBasicCall actual = (SqlBasicCall) FunctionConverter.convert(segment).orElse(null);
        assertNotNull(actual);
        assertThat(actual.getOperator(), instanceOf(SqlUnresolvedFunction.class));
        SqlIdentifier functionName = actual.getOperator().getNameAsId();
        assertThat(functionName.names, is(Collections.singletonList("custom_func")));
        assertThat(functionName.getSimple(), is("custom_func"));
        assertThat(actual.getOperandList(), is(Collections.singletonList(paramNode)));
    }
}
