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

package org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.orderby.item;

import org.apache.calcite.sql.SqlBasicCall;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.fun.SqlStdOperatorTable;
import org.apache.shardingsphere.database.connector.core.metadata.database.enums.NullsOrderType;
import org.apache.shardingsphere.sql.parser.statement.core.enums.OrderDirection;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.item.ExpressionOrderByItemSegment;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.expression.ExpressionConverter;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ExpressionConverter.class)
class ExpressionOrderByItemConverterTest {
    
    @Test
    void assertConvertReturnsEmptyForNullSegment() {
        assertFalse(ExpressionOrderByItemConverter.convert(null).isPresent());
    }
    
    @Test
    void assertConvertReturnsEmptyWhenExpressionConverterReturnsEmpty() {
        ExpressionSegment expressionSegment = mock(ExpressionSegment.class);
        when(ExpressionConverter.convert(expressionSegment)).thenReturn(Optional.empty());
        assertFalse(ExpressionOrderByItemConverter.convert(new ExpressionOrderByItemSegment(0, 0, "expr", OrderDirection.ASC, null, expressionSegment)).isPresent());
    }
    
    @Test
    void assertConvertReturnsExpressionNodeWithoutNulls() {
        ExpressionSegment expressionSegment = mock(ExpressionSegment.class);
        SqlNode expectedNode = mock(SqlNode.class);
        when(ExpressionConverter.convert(expressionSegment)).thenReturn(Optional.of(expectedNode));
        Optional<SqlNode> actual = ExpressionOrderByItemConverter.convert(new ExpressionOrderByItemSegment(0, 0, "expr", OrderDirection.ASC, null, expressionSegment));
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is(expectedNode));
    }
    
    @Test
    void assertConvertWrapsNullsFirst() {
        ExpressionSegment expressionSegment = mock(ExpressionSegment.class);
        SqlNode expectedNode = mock(SqlNode.class);
        when(ExpressionConverter.convert(expressionSegment)).thenReturn(Optional.of(expectedNode));
        Optional<SqlNode> actual = ExpressionOrderByItemConverter.convert(new ExpressionOrderByItemSegment(0, 0, "expr", OrderDirection.ASC, NullsOrderType.FIRST, expressionSegment));
        assertTrue(actual.isPresent());
        SqlBasicCall nullsFirstCall = (SqlBasicCall) actual.get();
        assertThat(nullsFirstCall, isA(SqlBasicCall.class));
        assertThat(nullsFirstCall.getOperator(), is(SqlStdOperatorTable.NULLS_FIRST));
        assertThat(nullsFirstCall.getOperandList().get(0), is(expectedNode));
    }
    
    @Test
    void assertConvertWrapsNullsLast() {
        ExpressionSegment expressionSegment = mock(ExpressionSegment.class);
        SqlNode expectedNode = mock(SqlNode.class);
        when(ExpressionConverter.convert(expressionSegment)).thenReturn(Optional.of(expectedNode));
        Optional<SqlNode> actual = ExpressionOrderByItemConverter.convert(new ExpressionOrderByItemSegment(0, 0, "expr", OrderDirection.ASC, NullsOrderType.LAST, expressionSegment));
        assertTrue(actual.isPresent());
        SqlBasicCall nullsLastCall = (SqlBasicCall) actual.get();
        assertThat(nullsLastCall, isA(SqlBasicCall.class));
        assertThat(nullsLastCall.getOperator(), is(SqlStdOperatorTable.NULLS_LAST));
        assertThat(nullsLastCall.getOperandList().get(0), is(expectedNode));
    }
}
