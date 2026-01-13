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

package org.apache.shardingsphere.infra.binder.context.segment.select.projection;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.AggregationDistinctProjection;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.AggregationProjection;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.ColumnProjection;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.DerivedProjection;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.ExpressionProjection;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.ShorthandProjection;
import org.apache.shardingsphere.sql.parser.statement.core.enums.AggregationType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.AggregationProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class ProjectionsContextTest {
    
    @Test
    void assertIsUnqualifiedShorthandProjectionWithEmptyItems() {
        ProjectionsContext projectionsContext = new ProjectionsContext(0, 0, true, Collections.emptySet());
        assertFalse(projectionsContext.isUnqualifiedShorthandProjection());
    }
    
    @Test
    void assertIsUnqualifiedShorthandProjectionWithWrongProjection() {
        ProjectionsContext projectionsContext = new ProjectionsContext(0, 0, true, Collections.singleton(getColumnProjection()));
        assertFalse(projectionsContext.isUnqualifiedShorthandProjection());
    }
    
    @Test
    void assertIsUnqualifiedShorthandProjectionWithWrongShortProjection() {
        ProjectionsContext projectionsContext = new ProjectionsContext(0, 0, true, Collections.singleton(getShorthandProjection()));
        assertFalse(projectionsContext.isUnqualifiedShorthandProjection());
    }
    
    @Test
    void assertIsUnqualifiedShorthandProjection() {
        Projection projection = new ShorthandProjection(null, Collections.emptyList());
        ProjectionsContext projectionsContext = new ProjectionsContext(0, 0, true, Collections.singleton(projection));
        assertTrue(projectionsContext.isUnqualifiedShorthandProjection());
    }
    
    @Test
    void assertFindAliasWithOutAlias() {
        ProjectionsContext projectionsContext = new ProjectionsContext(0, 0, true, Collections.emptyList());
        assertFalse(projectionsContext.findAlias("").isPresent());
    }
    
    @Test
    void assertFindAlias() {
        Projection projection = getColumnProjectionWithAlias();
        ProjectionsContext projectionsContext = new ProjectionsContext(0, 0, true, Collections.singleton(projection));
        assertTrue(projectionsContext.findAlias(projection.getExpression()).isPresent());
    }
    
    @Test
    void assertFindProjectionIndex() {
        Projection projection = getColumnProjection();
        ProjectionsContext projectionsContext = new ProjectionsContext(0, 0, true, Collections.singleton(projection));
        Optional<Integer> actual = projectionsContext.findProjectionIndex(projection.getExpression());
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is(1));
    }
    
    @Test
    void assertFindProjectionIndexFailure() {
        Projection projection = getColumnProjection();
        ProjectionsContext projectionsContext = new ProjectionsContext(0, 0, true, Collections.singleton(projection));
        Optional<Integer> actual = projectionsContext.findProjectionIndex("");
        assertFalse(actual.isPresent());
    }
    
    @Test
    void assertGetAggregationProjections() {
        Projection projection = getAggregationProjection();
        List<AggregationProjection> items = new ProjectionsContext(0, 0, true, Arrays.asList(projection, getColumnProjection())).getAggregationProjections();
        assertTrue(items.contains(projection));
        assertThat(items.size(), is(1));
    }
    
    @Test
    void assertGetAggregationDistinctProjections() {
        Projection projection = getAggregationDistinctProjection();
        Collection<AggregationDistinctProjection> items = new ProjectionsContext(0, 0, true, Arrays.asList(projection, getColumnProjection())).getAggregationDistinctProjections();
        assertTrue(items.contains(projection));
        assertThat(items.size(), is(1));
    }
    
    private ShorthandProjection getShorthandProjection() {
        return new ShorthandProjection(new IdentifierValue("table"), Collections.emptyList());
    }
    
    private ColumnProjection getColumnProjection() {
        return new ColumnProjection("table", "name", null, mock(DatabaseType.class));
    }
    
    private ColumnProjection getColumnProjectionWithAlias() {
        return new ColumnProjection("table", "name", "n", mock(DatabaseType.class));
    }
    
    private AggregationProjection getAggregationProjection() {
        return new AggregationProjection(AggregationType.COUNT, new AggregationProjectionSegment(0, 0, AggregationType.COUNT, "(column)"), new IdentifierValue("c"), mock(DatabaseType.class));
    }
    
    private AggregationDistinctProjection getAggregationDistinctProjection() {
        return new AggregationDistinctProjection(
                0, 0, AggregationType.COUNT, new AggregationProjectionSegment(0, 0, AggregationType.COUNT, "(DISTINCT column)"), new IdentifierValue("c"), "column", mock(DatabaseType.class));
    }
    
    @Test
    void assertGetExpandProjections() {
        ColumnProjection columnProjection1 = new ColumnProjection(null, "col1", null, mock(DatabaseType.class));
        ColumnProjection columnProjection2 = new ColumnProjection(null, "col2", null, mock(DatabaseType.class));
        ColumnProjection columnProjection3 = new ColumnProjection(null, "col3", null, mock(DatabaseType.class));
        DerivedProjection derivedProjection = new DerivedProjection("col3", new IdentifierValue("a3"), null);
        ShorthandProjection shorthandProjection = new ShorthandProjection(null, Arrays.asList(columnProjection2, columnProjection3));
        ProjectionsContext actual = new ProjectionsContext(0, 0, false, Arrays.asList(columnProjection1, shorthandProjection, derivedProjection));
        assertThat(actual.getExpandProjections().size(), is(3));
        assertThat(actual.getExpandProjections().get(0), is(columnProjection1));
        assertThat(actual.getExpandProjections().get(1), is(columnProjection2));
        assertThat(actual.getExpandProjections().get(2), is(columnProjection3));
    }
    
    @Test
    void assertIsContainsLastInsertIdProjection() {
        ProjectionsContext lastInsertIdProjection = new ProjectionsContext(0, 0, false,
                Collections.singletonList(new ExpressionProjection(new ExpressionProjectionSegment(0, 0, "LAST_INSERT_ID()"), new IdentifierValue("id"), mock(DatabaseType.class))));
        assertTrue(lastInsertIdProjection.isContainsLastInsertIdProjection());
        ProjectionsContext maxProjection = new ProjectionsContext(0, 0, false,
                Collections.singletonList(new ExpressionProjection(new ExpressionProjectionSegment(0, 0, "MAX(id)"), new IdentifierValue("max"), mock(DatabaseType.class))));
        assertFalse(maxProjection.isContainsLastInsertIdProjection());
    }
    
    @Test
    void assertNotFindColumnProjectionWithOutOfIndex() {
        ProjectionsContext projectionsContext = new ProjectionsContext(0, 0, true, Collections.emptySet());
        assertFalse(projectionsContext.findColumnProjection(1).isPresent());
    }
    
    @Test
    void assertNotFindColumnProjectionWithNotColumnProjection() {
        ProjectionsContext projectionsContext = new ProjectionsContext(0, 0, true, Collections.singleton(mock(Projection.class)));
        assertFalse(projectionsContext.findColumnProjection(1).isPresent());
    }
    
    @Test
    void assertFindColumnProjection() {
        ProjectionsContext projectionsContext = new ProjectionsContext(0, 0, true, Collections.singleton(mock(ColumnProjection.class)));
        assertTrue(projectionsContext.findColumnProjection(1).isPresent());
    }
    
    @Test
    void assertGetExpandAggregationProjectionsWithNestedAvg() {
        AggregationProjectionSegment avgSegment = new AggregationProjectionSegment(0, 0, AggregationType.AVG, "AVG(score)");
        ExpressionProjectionSegment expressionSegment = new ExpressionProjectionSegment(0, 0, "IFNULL(AVG(score), 0)");
        expressionSegment.getAggregationProjectionSegments().add(avgSegment);
        ExpressionProjection expressionProjection = new ExpressionProjection(expressionSegment, new IdentifierValue("avg_val"), mock(DatabaseType.class));
        ProjectionsContext projectionsContext = new ProjectionsContext(0, 0, false, Collections.singletonList(expressionProjection));
        List<AggregationProjection> actual = projectionsContext.getExpandAggregationProjections();
        assertThat(actual.size(), is(3));
        AggregationProjection avgProjection = actual.get(0);
        assertThat(avgProjection.getDerivedAggregationProjections().size(), is(2));
        assertThat(avgProjection.getIndex(), is(2));
        assertThat(actual.get(1).getIndex(), is(3));
        assertThat(actual.get(2).getIndex(), is(4));
    }
    
    @Test
    void assertGetExpandAggregationProjectionsWithMultipleNestedAggregations() {
        AggregationProjectionSegment sumASegment = new AggregationProjectionSegment(0, 0, AggregationType.SUM, "SUM(a)");
        AggregationProjectionSegment sumBSegment = new AggregationProjectionSegment(0, 0, AggregationType.SUM, "SUM(b)");
        ExpressionProjectionSegment expressionSegment = new ExpressionProjectionSegment(0, 0, "SUM(a) + SUM(b)");
        expressionSegment.getAggregationProjectionSegments().addAll(Arrays.asList(sumASegment, sumBSegment));
        ExpressionProjection expressionProjection = new ExpressionProjection(expressionSegment, new IdentifierValue("total"), mock(DatabaseType.class));
        ProjectionsContext projectionsContext = new ProjectionsContext(0, 0, false, Collections.singletonList(expressionProjection));
        List<AggregationProjection> actual = projectionsContext.getExpandAggregationProjections();
        assertThat(actual.size(), is(2));
        assertThat(actual.get(0).getIndex(), is(2));
        assertThat(actual.get(1).getIndex(), is(3));
    }
}
