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

import org.apache.calcite.avatica.util.TimeUnit;
import org.apache.calcite.sql.SqlBasicCall;
import org.apache.calcite.sql.SqlIntervalQualifier;
import org.apache.calcite.sql.SqlLiteral;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.IntervalExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.IntervalUnit;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.operator.common.SQLExtensionOperatorTable;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.expression.ExpressionConverter;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Optional;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ExpressionConverter.class)
class IntervalExpressionConverterTest {
    
    @Test
    void assertConvertThrowsForUnsupportedIntervalUnit() {
        LiteralExpressionSegment value = new LiteralExpressionSegment(0, 0, 1);
        IntervalExpression intervalExpression = new IntervalExpression(0, 0, value, IntervalUnit.YEAR_MONTH, "INTERVAL 1");
        assertThrows(UnsupportedOperationException.class, () -> IntervalExpressionConverter.convert(intervalExpression));
    }
    
    @ParameterizedTest(name = "convert interval unit {0}")
    @MethodSource("provideIntervalUnits")
    void assertConvertIntervalUnits(final IntervalUnit intervalUnit, final TimeUnit expectedTimeUnit) {
        LiteralExpressionSegment value = new LiteralExpressionSegment(0, 0, 1);
        SqlNode literalNode = SqlLiteral.createExactNumeric("1", SqlParserPos.ZERO);
        when(ExpressionConverter.convert(value)).thenReturn(Optional.of(literalNode));
        IntervalExpression intervalExpression = new IntervalExpression(0, 0, value, intervalUnit, "INTERVAL 1");
        SqlBasicCall actual = IntervalExpressionConverter.convert(intervalExpression);
        assertThat(actual.getOperator(), is(SQLExtensionOperatorTable.INTERVAL_OPERATOR));
        assertThat(actual.getOperandList().get(0), is(literalNode));
        SqlIntervalQualifier qualifier = (SqlIntervalQualifier) actual.getOperandList().get(1);
        assertThat(qualifier.getStartUnit(), is(expectedTimeUnit));
    }
    
    private static Stream<Arguments> provideIntervalUnits() {
        return Stream.of(
                Arguments.of(IntervalUnit.MICROSECOND, TimeUnit.MICROSECOND),
                Arguments.of(IntervalUnit.SECOND, TimeUnit.SECOND),
                Arguments.of(IntervalUnit.MINUTE, TimeUnit.MINUTE),
                Arguments.of(IntervalUnit.HOUR, TimeUnit.HOUR),
                Arguments.of(IntervalUnit.DAY, TimeUnit.DAY),
                Arguments.of(IntervalUnit.WEEK, TimeUnit.WEEK),
                Arguments.of(IntervalUnit.MONTH, TimeUnit.MONTH),
                Arguments.of(IntervalUnit.QUARTER, TimeUnit.QUARTER),
                Arguments.of(IntervalUnit.YEAR, TimeUnit.YEAR));
    }
}
