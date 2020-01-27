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

package org.apache.shardingsphere.sql.parser.integrate.asserts.segment.insert;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.integrate.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.segment.impl.insert.ExpectedAssignment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.expr.complex.ComplexExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.test.sql.SQLCaseType;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AssignmentAssert {
    
    /**
     * Assert actual expression segment is correct with expected assignment.
     *
     * @param assertContext assert context
     * @param actual actual assignment
     * @param expected expected assignment
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final ExpressionSegment actual, final ExpectedAssignment expected) {
        if (SQLCaseType.Placeholder == assertContext.getSqlCaseType()) {
            if (null == expected.getTypeForPlaceholder()) {
                return;
            }
            assertThat(assertContext.getText("SQL expression type for placeholder error: "), actual.getClass().getSimpleName(), is(expected.getTypeForPlaceholder()));
            assertThat(assertContext.getText("SQL expression text for placeholder error: "), getText(actual), is(expected.getTextForPlaceholder()));
        } else {
            assertThat(assertContext.getText("SQL expression type for literal error: "), actual.getClass().getSimpleName(), is(expected.getTypeForLiteral()));
            assertThat(assertContext.getText("SQL expression text for literal error: "), getText(actual), is(expected.getTextForLiteral()));
        }
    }
    
    private static String getText(final ExpressionSegment expressionSegment) {
        if (expressionSegment instanceof ParameterMarkerExpressionSegment) {
            return "" + ((ParameterMarkerExpressionSegment) expressionSegment).getParameterMarkerIndex();
        }
        if (expressionSegment instanceof LiteralExpressionSegment) {
            return ((LiteralExpressionSegment) expressionSegment).getLiterals().toString();
        }
        if (expressionSegment instanceof ComplexExpressionSegment) {
            return ((ComplexExpressionSegment) expressionSegment).getText();
        }
        return "";
    }
}
