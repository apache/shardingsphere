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

package org.apache.shardingsphere.core.preprocessor.segment.select.projection.engine;

import com.google.common.base.Optional;
import org.apache.shardingsphere.core.preprocessor.segment.select.projection.Projection;
import org.apache.shardingsphere.core.preprocessor.segment.select.projection.impl.ShorthandProjection;
import org.apache.shardingsphere.core.preprocessor.segment.select.projection.impl.ColumnProjection;
import org.apache.shardingsphere.core.preprocessor.segment.select.projection.impl.ExpressionProjection;
import org.apache.shardingsphere.core.preprocessor.segment.select.projection.impl.AggregationDistinctProjection;
import org.apache.shardingsphere.core.preprocessor.segment.select.projection.impl.AggregationProjection;
import org.apache.shardingsphere.core.parse.core.constant.AggregationType;
import org.apache.shardingsphere.core.parse.sql.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.item.ShorthandSelectItemSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.item.ColumnSelectItemSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.item.ExpressionSelectItemSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.item.AggregationDistinctSelectItemSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.item.AggregationSelectItemSegment;
import org.apache.shardingsphere.core.parse.sql.segment.generic.TableSegment;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ProjectionEngineTest {
    
    @Test
    public void assertProjectionCreatedWhenSelectItemSegmentNotMatched() {
        Optional<Projection> projection = new ProjectionEngine().createProjection(null, null);
        assertFalse(projection.isPresent());
    }
    
    @Test
    public void assertProjectionCreatedWhenSelectItemSegmentInstanceOfShorthandSelectItemSegment() {
        ShorthandSelectItemSegment shorthandSelectItemSegment = mock(ShorthandSelectItemSegment.class);
        when(shorthandSelectItemSegment.getOwner()).thenReturn(Optional.of(mock(TableSegment.class)));
        Optional<Projection> projection = new ProjectionEngine().createProjection(null, shorthandSelectItemSegment);
        assertTrue(projection.isPresent());
        assertThat(projection.get(), instanceOf(ShorthandProjection.class));
    }
    
    @Test
    public void assertProjectionCreatedWhenSelectItemSegmentInstanceOfColumnSelectItemSegment() {
        ColumnSelectItemSegment columnSelectItemSegment = new ColumnSelectItemSegment("text", new ColumnSegment(0, 10, "name"));
        columnSelectItemSegment.setAlias("alias");
        Optional<Projection> projection = new ProjectionEngine().createProjection(null, columnSelectItemSegment);
        assertTrue(projection.isPresent());
        assertThat(projection.get(), instanceOf(ColumnProjection.class));
    }

    @Test
    public void assertProjectionCreatedWhenSelectItemSegmentInstanceOfExpressionSelectItemSegment() {
        ExpressionSelectItemSegment expressionSelectItemSegment = new ExpressionSelectItemSegment(0, 10, "text");
        Optional<Projection> projection = new ProjectionEngine().createProjection(null, expressionSelectItemSegment);
        assertTrue(projection.isPresent());
        assertThat(projection.get(), instanceOf(ExpressionProjection.class));
    }
    
    @Test
    public void assertProjectionCreatedWhenSelectItemSegmentInstanceOfAggregationDistinctSelectItemSegment() {
        AggregationDistinctSelectItemSegment aggregationDistinctSelectItemSegment = new AggregationDistinctSelectItemSegment(0, 10, "text", AggregationType.COUNT, 0, "distinctExpression");
        Optional<Projection> projection = new ProjectionEngine().createProjection("select count(1) from table_1", aggregationDistinctSelectItemSegment);
        assertTrue(projection.isPresent());
        assertThat(projection.get(), instanceOf(AggregationDistinctProjection.class));
    }
    
    @Test
    public void assertProjectionCreatedWhenSelectItemSegmentInstanceOfAggregationSelectItemSegment() {
        AggregationSelectItemSegment aggregationSelectItemSegment = new AggregationSelectItemSegment(0, 10, "text", AggregationType.COUNT, 0);
        Optional<Projection> projection = new ProjectionEngine().createProjection("select count(1) from table_1", aggregationSelectItemSegment);
        assertTrue(projection.isPresent());
        assertThat(projection.get(), instanceOf(AggregationProjection.class));
    }
    
    @Test
    public void assertProjectionCreatedWhenSelectItemSegmentInstanceOfAggregationDistinctSelectItemSegmentAndAggregationTypeIsAvg() {
        AggregationDistinctSelectItemSegment aggregationDistinctSelectItemSegment = new AggregationDistinctSelectItemSegment(0, 10, "text", AggregationType.AVG, 0, "distinctExpression");
        Optional<Projection> projection = new ProjectionEngine().createProjection("select count(1) from table_1", aggregationDistinctSelectItemSegment);
        assertTrue(projection.isPresent());
        assertThat(projection.get(), instanceOf(AggregationDistinctProjection.class));
    }
    
    @Test
    public void assertProjectionCreatedWhenSelectItemSegmentInstanceOfAggregationSelectItemSegmentAndAggregationTypeIsAvg() {
        AggregationSelectItemSegment aggregationSelectItemSegment = new AggregationSelectItemSegment(0, 10, "text", AggregationType.AVG, 0);
        Optional<Projection> projection = new ProjectionEngine().createProjection("select count(1) from table_1", aggregationSelectItemSegment);
        assertTrue(projection.isPresent());
        assertThat(projection.get(), instanceOf(AggregationProjection.class));
    }
    
}
