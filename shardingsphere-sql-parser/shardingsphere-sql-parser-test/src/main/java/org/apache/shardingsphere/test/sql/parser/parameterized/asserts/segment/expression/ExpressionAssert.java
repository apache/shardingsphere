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

package org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment.expression;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment.SQLSegmentAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.dml.impl.SelectStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.segment.impl.expr.complex.ExpectedCommonExpression;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.segment.impl.expr.simple.ExpectedLiteralExpression;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.segment.impl.expr.simple.ExpectedParameterMarkerExpression;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.segment.impl.expr.simple.ExpectedSubquery;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.sql.SQLCaseType;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.complex.ComplexExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.subquery.SubqueryExpressionSegment;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

/**
 *  Expression assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ExpressionAssert {
    
    /**
     * Assert parameter marker expression.
     *
     * @param assertContext assert context
     * @param actual actual parameter marker expression segment
     * @param expected expected parameter marker expression
     */
    public static void assertParameterMarkerExpression(final SQLCaseAssertContext assertContext,
                                                        final ParameterMarkerExpressionSegment actual, final ExpectedParameterMarkerExpression expected) {
        assertNotNull(assertContext.getText("Expected parameter marker expression should exist."), expected);
        assertThat(assertContext.getText("Parameter marker index assertion error: "), actual.getParameterMarkerIndex(), is(expected.getValue()));
        SQLSegmentAssert.assertIs(assertContext, actual, expected);
    }
    
    /**
     * Assert literal expression.
     *
     * @param assertContext assert context
     * @param actual actual literal expression segment
     * @param expected expected literal expression
     */
    public static void assertLiteralExpression(final SQLCaseAssertContext assertContext,
                                                final LiteralExpressionSegment actual, final ExpectedLiteralExpression expected) {
        assertNotNull(assertContext.getText("Expected literal expression should exist."));
        assertThat(assertContext.getText("Literal assertion error: "), actual.getLiterals().toString(), is(expected.getValue()));
        SQLSegmentAssert.assertIs(assertContext, actual, expected);
    }
    
    /**
     * Assert common expression.
     *
     * @param assertContext assert context
     * @param actual actual common expression segment
     * @param expected expected common expression
     */
    public static void assertCommonExpression(final SQLCaseAssertContext assertContext,
                                               final ComplexExpressionSegment actual, final ExpectedCommonExpression expected) {
        assertNotNull(assertContext.getText("Expected common expression should exist."));
        String expectedText = SQLCaseType.Literal == assertContext.getSqlCaseType() && null != expected.getLiteralText() ? expected.getLiteralText() : expected.getText();
        assertThat(assertContext.getText("Common expression text assertion error: "), actual.getText(), is(expectedText));
        SQLSegmentAssert.assertIs(assertContext, actual, expected);
    }
    
    /**
     * Assert subquery expression.
     *
     * @param assertContext assert context
     * @param actual actual subquery segment
     * @param expected expected subquery expression
     */
    public static void assertSubqueryExpression(final SQLCaseAssertContext assertContext, final SubqueryExpressionSegment actual, final ExpectedSubquery expected) {
        SelectStatementAssert.assertIs(assertContext, actual.getSubquery().getSelect(), expected.getSelectTestCases());
        SQLSegmentAssert.assertIs(assertContext, actual, expected);
    }
}
