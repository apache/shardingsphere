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

package org.apache.shardingsphere.sql.parser.integrate.asserts.segment.set;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.integrate.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.sql.parser.integrate.asserts.segment.SQLSegmentAssert;
import org.apache.shardingsphere.sql.parser.integrate.asserts.segment.column.ColumnAssert;
import org.apache.shardingsphere.sql.parser.integrate.asserts.segment.expression.ExpressionAssert;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.impl.set.ExpectedSetAssignment;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.impl.set.ExpectedUpdateAssignment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.assignment.AssignmentSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.assignment.SetAssignmentSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.item.ExpressionProjectionSegment;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

/**
 * Set assignment assert.
 *
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SetAssignmentAssert {
    
    /**
     * Assert actual set assignment segment is correct with expected set assignment.
     * 
     * @param assertContext assert context
     * @param actual actual tables
     * @param expected expected tables
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final SetAssignmentSegment actual, final ExpectedSetAssignment expected) {
        assertNotNull(assertContext.getText("Assignments should existed."), expected);
        assertThat(assertContext.getText("Assignments size assertion error: "), actual.getAssignments().size(), is(expected.getAssignments().size()));
        int count = 0;
        for (AssignmentSegment each : actual.getAssignments()) {
            assertAssignment(assertContext, each, expected.getAssignments().get(count));
            count++;
        }
        SQLSegmentAssert.assertIs(assertContext, actual, expected);
    }
    
    private static void assertAssignment(final SQLCaseAssertContext assertContext, final AssignmentSegment actual, final ExpectedUpdateAssignment expected) {
        ColumnAssert.assertIs(assertContext, actual.getColumn(), expected.getColumn());
        if (actual.getValue() instanceof ParameterMarkerExpressionSegment) {
            ExpressionAssert.assertParameterMarkerExpression(assertContext, (ParameterMarkerExpressionSegment) actual.getValue(), expected.getParameterMarkerExpression());
        } else if (actual.getValue() instanceof LiteralExpressionSegment) {
            ExpressionAssert.assertLiteralExpression(assertContext, (LiteralExpressionSegment) actual.getValue(), expected.getLiteralExpression());
        // FIXME should be CommonExpressionProjection, not ExpressionProjectionSegment
        } else if (actual.getValue() instanceof ExpressionProjectionSegment) {
            ExpressionAssert.assertCommonExpression(assertContext, (ExpressionProjectionSegment) actual.getValue(), expected.getCommonExpression());
        }
        SQLSegmentAssert.assertIs(assertContext, actual, expected);
    }
}
