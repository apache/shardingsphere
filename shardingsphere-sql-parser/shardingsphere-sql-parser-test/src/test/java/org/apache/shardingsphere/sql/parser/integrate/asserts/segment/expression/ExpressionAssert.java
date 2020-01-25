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

package org.apache.shardingsphere.sql.parser.integrate.asserts.segment.expression;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.integrate.asserts.SQLStatementAssertMessage;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.impl.expr.complex.ExpectedCommonExpression;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.impl.expr.complex.ExpectedSubquery;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.impl.expr.simple.ExpectedLiteralExpression;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.impl.expr.simple.ExpectedParameterMarkerExpression;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.expr.complex.ComplexExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.test.sql.SQLCaseType;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 *  Expression assert.
 *
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ExpressionAssert {
    
    /**
     * Assert parameter marker expression.
     * 
     * @param assertMessage assert message
     * @param actual actual parameter marker expression segment
     * @param expected expected parameter marker expression
     * @param sqlCaseType SQL case type
     */
    public static void assertParameterMarkerExpression(final SQLStatementAssertMessage assertMessage, 
                                                        final ParameterMarkerExpressionSegment actual, final ExpectedParameterMarkerExpression expected, final SQLCaseType sqlCaseType) {
        assertThat(assertMessage.getText("Parameter marker index assertion error: "), actual.getParameterMarkerIndex(), is(expected.getValue()));
        // TODO assert start index and stop index
//        SQLSegmentAssert.assertIs(assertMessage, actual, expected, sqlCaseType);
    }
    
    /**
     * Assert literal expression.
     *
     * @param assertMessage assert message
     * @param actual actual literal expression segment
     * @param expected expected literal expression
     * @param sqlCaseType SQL case type
     */
    public static void assertLiteralExpression(final SQLStatementAssertMessage assertMessage, 
                                                final LiteralExpressionSegment actual, final ExpectedLiteralExpression expected, final SQLCaseType sqlCaseType) {
        assertThat(assertMessage.getText("Literal assertion error: "), actual.getLiterals().toString(), is(expected.getValue()));
        // TODO assert start index and stop index
//        SQLSegmentAssert.assertIs(assertMessage, actual, expected, sqlCaseType);
    }
    
    /**
     * Assert common expression.
     *
     * @param assertMessage assert message
     * @param actual actual common expression segment
     * @param expected expected common expression
     * @param sqlCaseType SQL case type
     */
    public static void assertCommonExpression(final SQLStatementAssertMessage assertMessage, 
                                               final ComplexExpressionSegment actual, final ExpectedCommonExpression expected, final SQLCaseType sqlCaseType) {
        assertThat(assertMessage.getText("Common expression text assertion error: "), actual.getText(), is(expected.getText()));
        // TODO assert start index and stop index
//        SQLSegmentAssert.assertIs(assertMessage, actual, expected, sqlCaseType);
    }
    
    /**
     * Assert subquery expression.
     *
     * @param assertMessage assert message
     * @param actual actual subquery segment
     * @param expected expected subquery expression
     * @param sqlCaseType SQL case type
     */
    public static void assertSubquery(final SQLStatementAssertMessage assertMessage, final ComplexExpressionSegment actual, final ExpectedSubquery expected, final SQLCaseType sqlCaseType) {
        // TODO assert start index and stop index
        assertThat(assertMessage.getText("Subquery text assertion error: "), actual.getText(), is(expected.getText()));
//        SQLSegmentAssert.assertIs(assertMessage, actual, expected, sqlCaseType);
    }
}
