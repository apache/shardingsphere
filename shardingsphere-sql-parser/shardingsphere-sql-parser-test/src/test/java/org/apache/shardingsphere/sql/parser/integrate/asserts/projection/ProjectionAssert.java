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
import org.apache.shardingsphere.sql.parser.integrate.jaxb.projection.ExpectedTopProjection;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.item.AggregationDistinctProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.item.AggregationProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.item.ShorthandProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.pagination.top.TopSegment;
import org.apache.shardingsphere.test.sql.SQLCaseType;

import java.util.Collection;
import java.util.LinkedList;

import static org.hamcrest.CoreMatchers.is;
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
     * @param sqlCaseType SQL case type
     */
    public static void assertIs(final SQLStatementAssertMessage assertMessage, final ProjectionsSegment actual, final ExpectedProjections expected, final SQLCaseType sqlCaseType) {
        Collection<ProjectionSegment> projections = actual.getProjections();
        assertThat(assertMessage.getText("Projections size error: "), projections.size(), is(expected.getSize()));
        Collection<ExpectedProjection> expectedBaseProjections = new LinkedList<>();
        for (ProjectionSegment each : projections) {
            if (each instanceof ShorthandProjectionSegment) {
                expectedBaseProjections = expected.findExpectedProjections(ExpectedShorthandProjection.class);
            }
            if (each instanceof AggregationProjectionSegment) {
                expectedBaseProjections = expected.findExpectedProjections(ExpectedAggregationProjection.class);
            }
            if (each instanceof AggregationDistinctProjectionSegment) {
                expectedBaseProjections = expected.findExpectedProjections(ExpectedAggregationDistinctProjection.class);
            }
            if (each instanceof ColumnProjectionSegment) {
                expectedBaseProjections = expected.findExpectedProjections(ExpectedColumnProjection.class);
            }
            if (each instanceof ExpressionProjectionSegment) {
                expectedBaseProjections = expected.findExpectedProjections(ExpectedExpressionProjection.class);
            }
            if (each instanceof TopSegment && SQLCaseType.Literal.equals(sqlCaseType)) {
                expectedBaseProjections = expected.findExpectedProjections(ExpectedTopProjection.class);
            }
            if (!expectedBaseProjections.isEmpty()) {
                assertProjection(assertMessage, each, expectedBaseProjections);
            }
        }
    }
    
    private static void assertProjection(final SQLStatementAssertMessage assertMessage, final ProjectionSegment actual, final Collection<ExpectedProjection> expected) {
        String actualText = actual.getText();
        String expectedText = "";
        for (ExpectedProjection each: expected) {
            if (actualText.equals(each.getText())) {
                expectedText = each.getText();
                break;
            }
        }
        assertThat(assertMessage.getText("Projection text assert error: "), actualText, is(expectedText));
    }
}
