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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.projection;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.AggregationDistinctProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.AggregationProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ShorthandProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.SubqueryProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.pagination.rownum.NumberLiteralRowNumberValueSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.pagination.rownum.ParameterMarkerRowNumberValueSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.pagination.top.TopProjectionSegment;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.SQLSegmentAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.bound.ColumnBoundAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.expression.ExpressionAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.generic.ParenthesesAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.identifier.IdentifierValueAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.owner.OwnerAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dml.standard.type.SelectStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.projection.ExpectedProjection;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.projection.ExpectedProjections;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.projection.impl.aggregation.ExpectedAggregationDistinctProjection;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.projection.impl.aggregation.ExpectedAggregationProjection;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.projection.impl.column.ExpectedColumnProjection;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.projection.impl.expression.ExpectedExpressionProjection;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.projection.impl.shorthand.ExpectedShorthandProjection;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.projection.impl.subquery.ExpectedSubqueryProjection;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.projection.impl.top.ExpectedTopProjection;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.sql.type.SQLCaseType;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Projection assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ProjectionAssert {
    
    /**
     * Assert actual projections segment is correct with expected projections.
     *
     * @param assertContext assert context
     * @param actual actual projection
     * @param expected expected projections
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final ProjectionsSegment actual, final ExpectedProjections expected) {
        assertProjections(assertContext, actual, expected);
        List<ExpectedProjection> expectedProjections = expected.getExpectedProjections();
        int count = 0;
        for (ProjectionSegment each : actual.getProjections()) {
            assertProjection(assertContext, each, expectedProjections.get(count));
            count++;
        }
    }
    
    private static void assertProjections(final SQLCaseAssertContext assertContext, final ProjectionsSegment actual, final ExpectedProjections expected) {
        assertThat(assertContext.getText("Projections size assertion error: "), actual.getProjections().size(), is(expected.getSize()));
        assertThat(assertContext.getText("Projections distinct row assertion error: "), actual.isDistinctRow(), is(expected.isDistinctRow()));
        SQLSegmentAssert.assertIs(assertContext, actual, expected);
    }
    
    /**
     * Assert actual projection segment is correct with expected projection.
     * @param assertContext assert context
     * @param actual actual projection
     * @param expected expected projection
     */
    public static void assertProjection(final SQLCaseAssertContext assertContext, final ProjectionSegment actual, final ExpectedProjection expected) {
        if (actual instanceof ShorthandProjectionSegment) {
            assertThat(assertContext.getText("Projection type assertion error: "), expected, isA(ExpectedShorthandProjection.class));
            assertShorthandProjection(assertContext, (ShorthandProjectionSegment) actual, (ExpectedShorthandProjection) expected);
        } else if (actual instanceof ColumnProjectionSegment) {
            assertThat(assertContext.getText("Projection type assertion error: "), expected, isA(ExpectedColumnProjection.class));
            assertColumnProjection(assertContext, (ColumnProjectionSegment) actual, (ExpectedColumnProjection) expected);
        } else if (actual instanceof AggregationProjectionSegment) {
            assertThat(assertContext.getText("Projection type assertion error: "), expected, isA(ExpectedAggregationProjection.class));
            assertAggregationProjection(assertContext, (AggregationProjectionSegment) actual, (ExpectedAggregationProjection) expected);
        } else if (actual instanceof ExpressionProjectionSegment) {
            assertThat(assertContext.getText("Projection type assertion error: "), expected, isA(ExpectedExpressionProjection.class));
            assertExpressionProjection(assertContext, (ExpressionProjectionSegment) actual, (ExpectedExpressionProjection) expected);
        } else if (actual instanceof TopProjectionSegment) {
            assertThat(assertContext.getText("Projection type assertion error: "), expected, isA(ExpectedTopProjection.class));
            assertTopProjection(assertContext, (TopProjectionSegment) actual, (ExpectedTopProjection) expected);
        } else if (actual instanceof SubqueryProjectionSegment) {
            assertThat(assertContext.getText("Projection type assertion error: "), expected, isA(ExpectedSubqueryProjection.class));
            assertSubqueryProjection(assertContext, (SubqueryProjectionSegment) actual, (ExpectedSubqueryProjection) expected);
        }
        SQLSegmentAssert.assertIs(assertContext, actual, expected);
    }
    
    private static void assertSubqueryProjection(final SQLCaseAssertContext assertContext, final SubqueryProjectionSegment actual, final ExpectedSubqueryProjection expected) {
        assertThat(assertContext.getText("Subquery projection alias assertion error: "), actual.getAliasName().orElse(null), is(expected.getAlias()));
        String expectedText = SQLCaseType.LITERAL == assertContext.getCaseType() && null != expected.getLiteralText() ? expected.getLiteralText() : expected.getText();
        assertThat(assertContext.getText("Subquery projection text assertion error: "), actual.getText(), is(expectedText));
        SelectStatementAssert.assertIs(assertContext, actual.getSubquery().getSelect(), expected.getSubquery().getSelectTestCases());
    }
    
    private static void assertShorthandProjection(final SQLCaseAssertContext assertContext, final ShorthandProjectionSegment actual, final ExpectedShorthandProjection expected) {
        if (null == expected.getOwner()) {
            assertFalse(actual.getOwner().isPresent(), assertContext.getText("Actual owner should not exist."));
        } else {
            assertTrue(actual.getOwner().isPresent(), assertContext.getText("Actual owner should exist."));
            OwnerAssert.assertIs(assertContext, actual.getOwner().get(), expected.getOwner());
        }
        assertActualProjections(assertContext, actual, expected);
    }
    
    private static void assertActualProjections(final SQLCaseAssertContext assertContext, final ShorthandProjectionSegment actual, final ExpectedShorthandProjection expected) {
        if (0 == expected.getActualProjections().getSize()) {
            assertTrue(actual.getActualProjectionSegments().isEmpty(), assertContext.getText("Actual projections should not exist."));
        } else {
            assertFalse(actual.getActualProjectionSegments().isEmpty(), assertContext.getText("Actual projections should exist."));
            assertThat(assertContext.getText("Actual projections size assertion error:"), actual.getActualProjectionSegments().size(), is(expected.getActualProjections().getSize()));
            List<ProjectionSegment> actualProjectionSegments = new ArrayList<>(actual.getActualProjectionSegments());
            int index = 0;
            for (ExpectedProjection each : expected.getActualProjections().getExpectedProjections()) {
                assertProjection(assertContext, actualProjectionSegments.get(index++), each);
            }
        }
    }
    
    private static void assertColumnProjection(final SQLCaseAssertContext assertContext, final ColumnProjectionSegment actual, final ExpectedColumnProjection expected) {
        assertThat(assertContext.getText("Column projection alias assertion error: "), actual.getAliasName().orElse(null), is(expected.getAlias()));
        assertColumnSegment(assertContext, actual, expected);
        assertLeftParentheses(assertContext, actual, expected);
        assertRightParentheses(assertContext, actual, expected);
        if (null == expected.getOwner()) {
            assertFalse(actual.getColumn().getOwner().isPresent(), assertContext.getText("Actual owner should not exist."));
        } else {
            assertTrue(actual.getColumn().getOwner().isPresent(), assertContext.getText("Actual owner should exist."));
            OwnerAssert.assertIs(assertContext, actual.getColumn().getOwner().get(), expected.getOwner());
        }
    }
    
    private static void assertColumnSegment(final SQLCaseAssertContext assertContext, final ColumnProjectionSegment actual, final ExpectedColumnProjection expected) {
        if (null != actual.getColumn().getNestedObjectAttributes()) {
            assertThat(assertContext.getText("Nested Object attributes assertion error: "), actual.getColumn().getExpression(), is(expected.getName()));
        } else {
            IdentifierValueAssert.assertIs(assertContext, actual.getColumn().getIdentifier(), expected, "Column projection");
            ColumnBoundAssert.assertIs(assertContext, actual.getColumn().getColumnBoundInfo(), expected.getColumnBound());
        }
    }
    
    private static void assertLeftParentheses(final SQLCaseAssertContext assertContext, final ColumnProjectionSegment actual, final ExpectedColumnProjection expected) {
        if (null == expected.getLeftParentheses()) {
            assertFalse(actual.getColumn().getLeftParentheses().isPresent(), assertContext.getText("Actual left parentheses should not exist."));
        } else {
            assertTrue(actual.getColumn().getLeftParentheses().isPresent(), assertContext.getText("Actual left parentheses should exist."));
            ParenthesesAssert.assertIs(assertContext, actual.getColumn().getLeftParentheses().get(), expected.getLeftParentheses());
        }
    }
    
    private static void assertRightParentheses(final SQLCaseAssertContext assertContext, final ColumnProjectionSegment actual, final ExpectedColumnProjection expected) {
        if (null == expected.getRightParentheses()) {
            assertFalse(actual.getColumn().getRightParentheses().isPresent(), assertContext.getText("Actual right parentheses should not exist."));
        } else {
            assertTrue(actual.getColumn().getRightParentheses().isPresent(), assertContext.getText("Actual right parentheses should exist."));
            ParenthesesAssert.assertIs(assertContext, actual.getColumn().getRightParentheses().get(), expected.getRightParentheses());
        }
    }
    
    private static void assertAggregationProjection(final SQLCaseAssertContext assertContext, final AggregationProjectionSegment actual, final ExpectedAggregationProjection expected) {
        assertThat(assertContext.getText("Aggregation projection type assertion error: "), actual.getType().name(), is(expected.getType()));
        assertThat(assertContext.getText("Aggregation projection inner expression assertion error: "), actual.getExpression(), is(expected.getExpression()));
        assertThat(assertContext.getText("Aggregation projection alias assertion error: "), actual.getAliasName().orElse(null), is(expected.getAlias()));
        assertThat(assertContext.getText("Aggregation projection separator assertion error: "), actual.getSeparator().orElse(null), is(expected.getSeparator()));
        if (actual instanceof AggregationDistinctProjectionSegment) {
            assertThat(assertContext.getText("Projection type assertion error: "), expected, isA(ExpectedAggregationDistinctProjection.class));
            assertThat(assertContext.getText("Aggregation projection distinct inner expression assertion error: "),
                    ((AggregationDistinctProjectionSegment) actual).getDistinctInnerExpression(), is(((ExpectedAggregationDistinctProjection) expected).getDistinctInnerExpression()));
        }
    }
    
    private static void assertExpressionProjection(final SQLCaseAssertContext assertContext, final ExpressionProjectionSegment actual, final ExpectedExpressionProjection expected) {
        assertThat(assertContext.getText("Expression projection alias assertion error: "),
                actual.getAliasName().orElse(null), is(expected.getAlias()));
        String expectedText = SQLCaseType.LITERAL == assertContext.getCaseType() && null != expected.getLiteralText()
                ? expected.getLiteralText()
                : expected.getText();
        assertThat(assertContext.getText("Expression projection text assertion error: "), actual.getText(), is(expectedText));
        if (null != expected.getExpr()) {
            ExpressionAssert.assertExpression(assertContext, actual.getExpr(), expected.getExpr());
        }
    }
    
    private static void assertTopProjection(final SQLCaseAssertContext assertContext, final TopProjectionSegment actual, final ExpectedTopProjection expected) {
        if (actual.getTop() instanceof NumberLiteralRowNumberValueSegment) {
            assertThat(assertContext.getText("Expression projection top value assertion error: "),
                    ((NumberLiteralRowNumberValueSegment) actual.getTop()).getValue(), is(expected.getTopValue().getValue()));
        } else {
            assertThat(assertContext.getText("Expression projection top parameter index assertion error: "),
                    ((ParameterMarkerRowNumberValueSegment) actual.getTop()).getParameterIndex(), is(expected.getTopValue().getParameterIndex()));
        }
        assertThat(assertContext.getText("Expression projection alias assertion error: "), actual.getAlias(), is(expected.getAlias()));
        SQLSegmentAssert.assertIs(assertContext, actual, expected);
    }
}
