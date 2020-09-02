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

package org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment.projection;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment.SQLSegmentAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment.table.TableAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.segment.impl.projection.ExpectedProjection;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.segment.impl.projection.ExpectedProjections;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.segment.impl.projection.impl.aggregation.ExpectedAggregationDistinctProjection;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.segment.impl.projection.impl.aggregation.ExpectedAggregationProjection;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.segment.impl.projection.impl.column.ExpectedColumnProjection;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.segment.impl.projection.impl.expression.ExpectedExpressionProjection;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.segment.impl.projection.impl.shorthand.ExpectedShorthandProjection;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.segment.impl.projection.impl.top.ExpectedTopProjection;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.AggregationDistinctProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.AggregationProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ShorthandProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.pagination.rownum.NumberLiteralRowNumberValueSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.pagination.rownum.ParameterMarkerRowNumberValueSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.pagination.top.TopProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;

import java.util.List;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

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
    
    private static void assertProjection(final SQLCaseAssertContext assertContext, final ProjectionSegment actual, final ExpectedProjection expected) {
        if (actual instanceof ShorthandProjectionSegment) {
            assertThat(assertContext.getText("Projection type assertion error: "), expected, instanceOf(ExpectedShorthandProjection.class));
            assertShorthandProjection(assertContext, (ShorthandProjectionSegment) actual, (ExpectedShorthandProjection) expected);
        } else if (actual instanceof ColumnProjectionSegment) {
            assertThat(assertContext.getText("Projection type assertion error: "), expected, instanceOf(ExpectedColumnProjection.class));
            assertColumnProjection(assertContext, (ColumnProjectionSegment) actual, (ExpectedColumnProjection) expected);
        } else if (actual instanceof AggregationProjectionSegment) {
            assertThat(assertContext.getText("Projection type assertion error: "), expected, instanceOf(ExpectedAggregationProjection.class));
            assertAggregationProjection(assertContext, (AggregationProjectionSegment) actual, (ExpectedAggregationProjection) expected);
        } else if (actual instanceof ExpressionProjectionSegment) {
            assertThat(assertContext.getText("Projection type assertion error: "), expected, instanceOf(ExpectedExpressionProjection.class));
            assertExpressionProjection(assertContext, (ExpressionProjectionSegment) actual, (ExpectedExpressionProjection) expected);
        } else if (actual instanceof TopProjectionSegment) {
            assertThat(assertContext.getText("Projection type assertion error: "), expected, instanceOf(ExpectedTopProjection.class));
            assertTopProjection(assertContext, (TopProjectionSegment) actual, (ExpectedTopProjection) expected);
        }
        SQLSegmentAssert.assertIs(assertContext, actual, expected);
    }
    
    private static void assertShorthandProjection(final SQLCaseAssertContext assertContext, final ShorthandProjectionSegment actual, final ExpectedShorthandProjection expected) {
        if (null != expected.getOwner()) {
            assertTrue(assertContext.getText("Actual owner should exist."), actual.getOwner().isPresent());
            OwnerSegment owner = actual.getOwner().get();
            TableAssert.assertOwner(assertContext, new SimpleTableSegment(owner.getStartIndex(), owner.getStopIndex(), owner.getIdentifier()), expected.getOwner());
        } else {
            assertFalse(assertContext.getText("Actual owner should not exist."), actual.getOwner().isPresent());
        }
    }
    
    private static void assertColumnProjection(final SQLCaseAssertContext assertContext, final ColumnProjectionSegment actual, final ExpectedColumnProjection expected) {
        assertThat(assertContext.getText("Column projection name assertion error: "), actual.getColumn().getIdentifier().getValue(), is(expected.getName()));
        assertThat(assertContext.getText("Column projection start delimiter assertion error: "), 
                actual.getColumn().getIdentifier().getQuoteCharacter().getStartDelimiter(), is(expected.getStartDelimiter()));
        assertThat(assertContext.getText("Column projection end delimiter assertion error: "), 
                actual.getColumn().getIdentifier().getQuoteCharacter().getEndDelimiter(), is(expected.getEndDelimiter()));
        assertThat(assertContext.getText("Column projection alias assertion error: "), actual.getAlias().orElse(null), is(expected.getAlias()));
        if (null != expected.getOwner()) {
            assertTrue(assertContext.getText("Actual owner should exist."), actual.getColumn().getOwner().isPresent());
            // TODO OwnerAssert is needed.
            OwnerSegment owner = actual.getColumn().getOwner().get();
            TableAssert.assertOwner(assertContext, new SimpleTableSegment(owner.getStartIndex(), owner.getStopIndex(), owner.getIdentifier()), expected.getOwner());
        } else {
            assertFalse(assertContext.getText("Actual owner should not exist."), actual.getColumn().getOwner().isPresent());
        }
    }
    
    private static void assertAggregationProjection(final SQLCaseAssertContext assertContext, final AggregationProjectionSegment actual, final ExpectedAggregationProjection expected) {
        assertThat(assertContext.getText("Aggregation projection type assertion error: "), actual.getType().name(), is(expected.getType()));
        assertThat(assertContext.getText("Aggregation projection inner expression assertion error: "), actual.getInnerExpression(), is(expected.getInnerExpression()));
        assertThat(assertContext.getText("Aggregation projection alias assertion error: "), actual.getAlias().orElse(null), is(expected.getAlias()));
        if (actual instanceof AggregationDistinctProjectionSegment) {
            assertThat(assertContext.getText("Projection type assertion error: "), expected, instanceOf(ExpectedAggregationDistinctProjection.class));
            assertThat(assertContext.getText("Aggregation projection alias assertion error: "), 
                    ((AggregationDistinctProjectionSegment) actual).getDistinctExpression(), is(((ExpectedAggregationDistinctProjection) expected).getDistinctExpression()));
        }
    }
    
    private static void assertExpressionProjection(final SQLCaseAssertContext assertContext, final ExpressionProjectionSegment actual, final ExpectedExpressionProjection expected) {
        assertThat(assertContext.getText("Expression projection alias assertion error: "), actual.getAlias().orElse(null), is(expected.getAlias()));
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
