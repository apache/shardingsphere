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

package org.apache.shardingsphere.sql.parser.integrate.asserts.segment.projection;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.integrate.asserts.segment.SQLSegmentAssert;
import org.apache.shardingsphere.sql.parser.integrate.asserts.SQLCaseAssertMessage;
import org.apache.shardingsphere.sql.parser.integrate.asserts.segment.owner.OwnerAssert;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.impl.projection.ExpectedProjection;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.impl.projection.ExpectedProjections;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.impl.projection.impl.aggregation.ExpectedAggregationDistinctProjection;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.impl.projection.impl.aggregation.ExpectedAggregationProjection;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.impl.projection.impl.column.ExpectedColumnProjection;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.impl.projection.impl.expression.ExpectedExpressionProjection;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.impl.projection.impl.shorthand.ExpectedShorthandProjection;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.impl.projection.impl.top.ExpectedTopProjection;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.item.AggregationDistinctProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.item.AggregationProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.item.ShorthandProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.pagination.rownum.NumberLiteralRowNumberValueSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.pagination.rownum.ParameterMarkerRowNumberValueSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.pagination.top.TopProjectionSegment;
import org.apache.shardingsphere.test.sql.SQLCaseType;

import java.util.List;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 *  Projection assert.
 *
 * @author zhaoyanan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ProjectionAssert {
    
    /**
     * Assert actual projections segment is correct with expected projections.
     * 
     * @param assertMessage assert message
     * @param actual actual projection
     * @param expected expected projections
     * @param sqlCaseType SQL case type
     */
    public static void assertIs(final SQLCaseAssertMessage assertMessage, final ProjectionsSegment actual, final ExpectedProjections expected, final SQLCaseType sqlCaseType) {
        assertProjections(assertMessage, actual, expected, sqlCaseType);
        List<ExpectedProjection> expectedProjections = expected.getExpectedProjections();
        int count = 0;
        for (ProjectionSegment each : actual.getProjections()) {
            assertProjection(assertMessage, each, expectedProjections.get(count), sqlCaseType);
            count++;
        }
    }
    
    private static void assertProjections(final SQLCaseAssertMessage assertMessage, final ProjectionsSegment actual, final ExpectedProjections expected, final SQLCaseType sqlCaseType) {
        assertThat(assertMessage.getText("Projections size assertion error: "), actual.getProjections().size(), is(expected.getSize()));
        assertThat(assertMessage.getText("Projections distinct row assertion error: "), actual.isDistinctRow(), is(expected.isDistinctRow()));
        SQLSegmentAssert.assertIs(assertMessage, actual, expected, sqlCaseType);
    }
    
    private static void assertProjection(final SQLCaseAssertMessage assertMessage, final ProjectionSegment actual, final ExpectedProjection expected, final SQLCaseType sqlCaseType) {
        if (actual instanceof ShorthandProjectionSegment) {
            assertThat(assertMessage.getText("Projection type assertion error: "), expected, instanceOf(ExpectedShorthandProjection.class));
            assertShorthandProjection(assertMessage, (ShorthandProjectionSegment) actual, (ExpectedShorthandProjection) expected, sqlCaseType);
        } else if (actual instanceof ColumnProjectionSegment) {
            assertThat(assertMessage.getText("Projection type assertion error: "), expected, instanceOf(ExpectedColumnProjection.class));
            assertColumnProjection(assertMessage, (ColumnProjectionSegment) actual, (ExpectedColumnProjection) expected, sqlCaseType);
        } else if (actual instanceof AggregationProjectionSegment) {
            assertThat(assertMessage.getText("Projection type assertion error: "), expected, instanceOf(ExpectedAggregationProjection.class));
            assertAggregationProjection(assertMessage, (AggregationProjectionSegment) actual, (ExpectedAggregationProjection) expected);
        } else if (actual instanceof ExpressionProjectionSegment) {
            assertThat(assertMessage.getText("Projection type assertion error: "), expected, instanceOf(ExpectedExpressionProjection.class));
            assertExpressionProjection(assertMessage, (ExpressionProjectionSegment) actual, (ExpectedExpressionProjection) expected);
        } else if (actual instanceof TopProjectionSegment) {
            assertThat(assertMessage.getText("Projection type assertion error: "), expected, instanceOf(ExpectedTopProjection.class));
            assertTopProjection(assertMessage, (TopProjectionSegment) actual, (ExpectedTopProjection) expected, sqlCaseType);
        }
        SQLSegmentAssert.assertIs(assertMessage, actual, expected, sqlCaseType);
    }
    
    private static void assertShorthandProjection(final SQLCaseAssertMessage assertMessage, 
                                                  final ShorthandProjectionSegment actual, final ExpectedShorthandProjection expected, final SQLCaseType sqlCaseType) {
        if (null != expected.getOwner()) {
            assertTrue(assertMessage.getText("Actual owner should exist."), actual.getOwner().isPresent());
            OwnerAssert.assertTable(assertMessage, actual.getOwner().get(), expected.getOwner(), sqlCaseType);
        } else {
            assertFalse(assertMessage.getText("Actual owner should not exist."), actual.getOwner().isPresent());
        }
    }
    
    private static void assertColumnProjection(final SQLCaseAssertMessage assertMessage, 
                                               final ColumnProjectionSegment actual, final ExpectedColumnProjection expected, final SQLCaseType sqlCaseType) {
        assertThat(assertMessage.getText("Column projection name assertion error: "), actual.getName(), is(expected.getName()));
        assertThat(assertMessage.getText("Column projection start delimiter assertion error: "), actual.getQuoteCharacter().getStartDelimiter(), is(expected.getStartDelimiter()));
        assertThat(assertMessage.getText("Column projection end delimiter assertion error: "), actual.getQuoteCharacter().getEndDelimiter(), is(expected.getEndDelimiter()));
        assertThat(assertMessage.getText("Column projection alias assertion error: "), actual.getAlias().orNull(), is(expected.getAlias()));
        if (null != expected.getOwner()) {
            assertTrue(assertMessage.getText("Actual owner should exist."), actual.getOwner().isPresent());
            OwnerAssert.assertTable(assertMessage, actual.getOwner().get(), expected.getOwner(), sqlCaseType);
        } else {
            assertFalse(assertMessage.getText("Actual owner should not exist."), actual.getOwner().isPresent());
        }
    }
    
    private static void assertAggregationProjection(final SQLCaseAssertMessage assertMessage, final AggregationProjectionSegment actual, final ExpectedAggregationProjection expected) {
        assertThat(assertMessage.getText("Aggregation projection type assertion error: "), actual.getType().name(), is(expected.getType()));
        assertThat(assertMessage.getText("Aggregation projection inner expression start index assertion error: "), actual.getInnerExpressionStartIndex(), is(expected.getInnerExpressionStartIndex()));
        assertThat(assertMessage.getText("Aggregation projection alias assertion error: "), actual.getAlias().orNull(), is(expected.getAlias()));
        if (actual instanceof AggregationDistinctProjectionSegment) {
            assertThat(assertMessage.getText("Projection type assertion error: "), expected, instanceOf(ExpectedAggregationDistinctProjection.class));
            assertThat(assertMessage.getText("Aggregation projection alias assertion error: "), 
                    ((AggregationDistinctProjectionSegment) actual).getDistinctExpression(), is(((ExpectedAggregationDistinctProjection) expected).getDistinctExpression()));
        }
    }
    
    private static void assertExpressionProjection(final SQLCaseAssertMessage assertMessage, final ExpressionProjectionSegment actual, final ExpectedExpressionProjection expected) {
        assertThat(assertMessage.getText("Expression projection alias assertion error: "), actual.getAlias().orNull(), is(expected.getAlias()));
    }
    
    private static void assertTopProjection(final SQLCaseAssertMessage assertMessage, final TopProjectionSegment actual, final ExpectedTopProjection expected, final SQLCaseType sqlCaseType) {
        if (actual.getTop() instanceof NumberLiteralRowNumberValueSegment) {
            assertThat(assertMessage.getText("Expression projection top value assertion error: "), 
                    ((NumberLiteralRowNumberValueSegment) actual.getTop()).getValue(), is(expected.getTopValue().getValue()));
        } else {
            assertThat(assertMessage.getText("Expression projection top parameter index assertion error: "), 
                    ((ParameterMarkerRowNumberValueSegment) actual.getTop()).getParameterIndex(), is(expected.getTopValue().getParameterIndex()));
        }
        assertThat(assertMessage.getText("Expression projection alias assertion error: "), actual.getAlias(), is(expected.getAlias()));
        SQLSegmentAssert.assertIs(assertMessage, actual, expected, sqlCaseType);
    }
}
