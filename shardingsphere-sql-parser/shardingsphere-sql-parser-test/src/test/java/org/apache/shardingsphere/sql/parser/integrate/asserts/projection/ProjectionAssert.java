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

package org.apache.shardingsphere.sql.parser.integrate.asserts.projection;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.integrate.asserts.SQLStatementAssertMessage;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.projection.ExpectedAggregationDistinctProjection;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.projection.ExpectedAggregationProjection;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.projection.ExpectedColumnProjection;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.projection.ExpectedExpressionProjection;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.projection.ExpectedProjection;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.projection.ExpectedProjections;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.projection.ExpectedShorthandProjection;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.projection.ExpectedTableSegment;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.projection.ExpectedTopProjection;
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
import org.apache.shardingsphere.sql.parser.sql.segment.generic.TableSegment;

import java.util.List;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

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
     */
    public static void assertIs(final SQLStatementAssertMessage assertMessage, final ProjectionsSegment actual, final ExpectedProjections expected) {
        assertProjections(assertMessage, actual, expected);
        List<ExpectedProjection> expectedProjections = expected.getExpectedProjections();
        int count = 0;
        for (ProjectionSegment each : actual.getProjections()) {
            assertProjection(assertMessage, each, expectedProjections.get(count));
            count++;
        }
    }
    
    private static void assertProjections(final SQLStatementAssertMessage assertMessage, final ProjectionsSegment actual, final ExpectedProjections expected) {
        assertThat(assertMessage.getText("Projections size assertion error: "), actual.getProjections().size(), is(expected.getSize()));
        assertThat(assertMessage.getText("Projections distinct row assertion error: "), actual.isDistinctRow(), is(expected.isDistinctRow()));
        assertThat(assertMessage.getText("Projections start index assertion error: "), actual.getStartIndex(), is(expected.getStartIndex()));
        assertThat(assertMessage.getText("Projections stop index assertion error: "), actual.getStopIndex(), is(expected.getStopIndex()));
    }
    
    private static void assertProjection(final SQLStatementAssertMessage assertMessage, final ProjectionSegment actual, final ExpectedProjection expected) {
        if (actual instanceof ShorthandProjectionSegment) {
            assertThat(assertMessage.getText("Projection type assertion error: "), expected, instanceOf(ExpectedShorthandProjection.class));
            assertShorthandProjection(assertMessage, (ShorthandProjectionSegment) actual, (ExpectedShorthandProjection) expected);
        } else if (actual instanceof ColumnProjectionSegment) {
            assertThat(assertMessage.getText("Projection type assertion error: "), expected, instanceOf(ExpectedColumnProjection.class));
            assertColumnProjection(assertMessage, (ColumnProjectionSegment) actual, (ExpectedColumnProjection) expected);
        } else if (actual instanceof AggregationProjectionSegment) {
            assertThat(assertMessage.getText("Projection type assertion error: "), expected, instanceOf(ExpectedAggregationProjection.class));
            assertAggregationProjection(assertMessage, (AggregationProjectionSegment) actual, (ExpectedAggregationProjection) expected);
        } else if (actual instanceof ExpressionProjectionSegment) {
            assertThat(assertMessage.getText("Projection type assertion error: "), expected, instanceOf(ExpectedExpressionProjection.class));
            assertExpressionProjection(assertMessage, (ExpressionProjectionSegment) actual, (ExpectedExpressionProjection) expected);
        } else if (actual instanceof TopProjectionSegment) {
            assertThat(assertMessage.getText("Projection type assertion error: "), expected, instanceOf(ExpectedTopProjection.class));
            assertTopProjection(assertMessage, (TopProjectionSegment) actual, (ExpectedTopProjection) expected);
        }
        assertThat(assertMessage.getText("Projection start index assertion error: "), actual.getStartIndex(), is(expected.getStartIndex()));
        assertThat(assertMessage.getText("Projection stop index assertion error: "), actual.getStopIndex(), is(expected.getStopIndex()));
    }
    
    private static void assertShorthandProjection(final SQLStatementAssertMessage assertMessage, final ShorthandProjectionSegment actual, final ExpectedShorthandProjection expected) {
        if (actual.getOwner().isPresent()) {
            assertOwner(assertMessage, actual.getOwner().get(), expected.getOwner());
        } else {
            assertNull(expected.getOwner());
        }
    }
    
    private static void assertColumnProjection(final SQLStatementAssertMessage assertMessage, final ColumnProjectionSegment actual, final ExpectedColumnProjection expected) {
        assertThat(assertMessage.getText("Column projection name assertion error: "), actual.getName(), is(expected.getName()));
        assertThat(assertMessage.getText("Column projection start delimiter assertion error: "), actual.getQuoteCharacter().getStartDelimiter(), is(expected.getStartDelimiter()));
        assertThat(assertMessage.getText("Column projection end delimiter assertion error: "), actual.getQuoteCharacter().getEndDelimiter(), is(expected.getEndDelimiter()));
        assertThat(assertMessage.getText("Column projection alias assertion error: "), actual.getAlias().orNull(), is(expected.getAlias()));
        if (actual.getOwner().isPresent()) {
            assertOwner(assertMessage, actual.getOwner().get(), expected.getOwner());
        } else {
            assertNull(expected.getOwner());
        }
    }
    
    private static void assertOwner(final SQLStatementAssertMessage assertMessage, final TableSegment actual, final ExpectedTableSegment expected) {
        assertThat(assertMessage.getText("Projection owner name assertion error: "), actual.getTableName(), is(expected.getName()));
        assertThat(assertMessage.getText("Projection owner name start delimiter assertion error: "), actual.getTableQuoteCharacter().getStartDelimiter(), is(expected.getStartDelimiter()));
        assertThat(assertMessage.getText("Projection owner name end delimiter assertion error: "), actual.getTableQuoteCharacter().getEndDelimiter(), is(expected.getEndDelimiter()));
        assertThat(assertMessage.getText("Projection owner name start index assertion error: "), actual.getStartIndex(), is(expected.getStartIndex()));
        assertThat(assertMessage.getText("Projection owner name stop index assertion error: "), actual.getStopIndex(), is(expected.getStopIndex()));
    }
    
    private static void assertAggregationProjection(final SQLStatementAssertMessage assertMessage, final AggregationProjectionSegment actual, final ExpectedAggregationProjection expected) {
        assertThat(assertMessage.getText("Aggregation projection type assertion error: "), actual.getType().name(), is(expected.getType()));
        assertThat(assertMessage.getText("Aggregation projection inner expression start index assertion error: "), actual.getInnerExpressionStartIndex(), is(expected.getInnerExpressionStartIndex()));
        assertThat(assertMessage.getText("Aggregation projection alias assertion error: "), actual.getAlias().orNull(), is(expected.getAlias()));
        if (actual instanceof AggregationDistinctProjectionSegment) {
            assertThat(assertMessage.getText("Projection type assertion error: "), expected, instanceOf(ExpectedAggregationDistinctProjection.class));
            assertThat(assertMessage.getText("Aggregation projection alias assertion error: "), 
                    ((AggregationDistinctProjectionSegment) actual).getDistinctExpression(), is(((ExpectedAggregationDistinctProjection) expected).getDistinctExpression()));
        }
    }
    
    private static void assertExpressionProjection(final SQLStatementAssertMessage assertMessage, final ExpressionProjectionSegment actual, final ExpectedExpressionProjection expected) {
        assertThat(assertMessage.getText("Expression projection alias assertion error: "), actual.getAlias().orNull(), is(expected.getAlias()));
    }
    
    private static void assertTopProjection(final SQLStatementAssertMessage assertMessage, final TopProjectionSegment actual, final ExpectedTopProjection expected) {
        if (actual.getTop() instanceof NumberLiteralRowNumberValueSegment) {
            assertThat(assertMessage.getText("Expression projection top value assertion error: "), ((NumberLiteralRowNumberValueSegment) actual.getTop()).getValue(), is(expected.getTopValue()));
        } else {
            assertThat(assertMessage.getText("Expression projection top parameter index assertion error: "), 
                    ((ParameterMarkerRowNumberValueSegment) actual.getTop()).getParameterIndex(), is(expected.getTopParameterIndex()));
        }
        assertThat(assertMessage.getText("Expression projection top value start index assertion error: "), actual.getTop().getStartIndex(), is(expected.getTopValueStartIndex()));
        assertThat(assertMessage.getText("Expression projection top value stop index assertion error: "), actual.getTop().getStopIndex(), is(expected.getTopValueStopIndex()));
        assertThat(assertMessage.getText("Expression projection alias assertion error: "), actual.getAlias(), is(expected.getAlias()));
    }
}
