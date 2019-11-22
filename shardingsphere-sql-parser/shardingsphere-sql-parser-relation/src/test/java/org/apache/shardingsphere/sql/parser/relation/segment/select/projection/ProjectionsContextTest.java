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

package org.apache.shardingsphere.sql.parser.relation.segment.select.projection;

import com.google.common.base.Optional;
import org.apache.shardingsphere.sql.parser.core.constant.AggregationType;
import org.apache.shardingsphere.sql.parser.relation.metadata.RelationMetaData;
import org.apache.shardingsphere.sql.parser.relation.metadata.RelationMetas;
import org.apache.shardingsphere.sql.parser.relation.segment.select.projection.impl.AggregationDistinctProjection;
import org.apache.shardingsphere.sql.parser.relation.segment.select.projection.impl.AggregationProjection;
import org.apache.shardingsphere.sql.parser.relation.segment.select.projection.impl.ColumnProjection;
import org.apache.shardingsphere.sql.parser.relation.segment.select.projection.impl.DerivedProjection;
import org.apache.shardingsphere.sql.parser.relation.segment.select.projection.impl.ExpressionProjection;
import org.apache.shardingsphere.sql.parser.relation.segment.select.projection.impl.ShorthandProjection;
import org.apache.shardingsphere.sql.parser.sql.segment.generic.TableSegment;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class ProjectionsContextTest {
    
    @Test
    public void assertUnqualifiedShorthandProjectionWithEmptyItems() {
        ProjectionsContext projectionsContext = new ProjectionsContext(0, 0, true, Collections.<Projection>emptySet());
        assertFalse(projectionsContext.isUnqualifiedShorthandProjection());
    }
    
    @Test
    public void assertUnqualifiedShorthandProjectionWithWrongSelectItem() {
        ProjectionsContext projectionsContext = new ProjectionsContext(0, 0, true, Collections.singleton((Projection) getColumnProjection()));
        assertFalse(projectionsContext.isUnqualifiedShorthandProjection());
    }
    
    @Test
    public void assertUnqualifiedShorthandProjectionWithWrongShortSelectItem() {
        ProjectionsContext projectionsContext = new ProjectionsContext(0, 0, true, Collections.singleton((Projection) getShorthandProjection()));
        assertFalse(projectionsContext.isUnqualifiedShorthandProjection());
    }
    
    @Test
    public void assertUnqualifiedShorthandProjection() {
        Projection projection = new ShorthandProjection(null);
        ProjectionsContext projectionsContext = new ProjectionsContext(0, 0, true, Collections.singleton(projection));
        assertTrue(projectionsContext.isUnqualifiedShorthandProjection());
    }
    
    @Test
    public void assertFindAliasWithOutAlias() {
        ProjectionsContext projectionsContext = new ProjectionsContext(0, 0, true, Collections.<Projection>emptyList());
        assertFalse(projectionsContext.findAlias("").isPresent());
    }
    
    @Test
    public void assertFindAlias() {
        Projection projection = getColumnProjectionWithAlias();
        ProjectionsContext projectionsContext = new ProjectionsContext(0, 0, true, Collections.singleton(projection));
        assertTrue(projectionsContext.findAlias(projection.getExpression()).isPresent());
    }
    
    @Test
    public void assertFindProjectionIndex() {
        Projection projection = getColumnProjection();
        ProjectionsContext projectionsContext = new ProjectionsContext(0, 0, true, Collections.singleton(projection));
        Optional<Integer> actual = projectionsContext.findProjectionIndex(projection.getExpression());
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is(1));
    }
    
    @Test
    public void assertFindProjectionIndexFailure() {
        Projection projection = getColumnProjection();
        ProjectionsContext projectionsContext = new ProjectionsContext(0, 0, true, Collections.singleton(projection));
        Optional<Integer> actual = projectionsContext.findProjectionIndex("");
        assertFalse(actual.isPresent());
    }
    
    @Test
    public void assertGetAggregationProjections() {
        Projection projection = getAggregationProjection();
        List<AggregationProjection> items = new ProjectionsContext(0, 0, true, Arrays.asList(projection, getColumnProjection())).getAggregationProjections();
        assertTrue(items.contains(projection));
        assertEquals(items.size(), 1);
    }
    
    @Test
    public void assertGetAggregationDistinctProjections() {
        Projection projection = getAggregationDistinctProjection();
        List<AggregationDistinctProjection> items = new ProjectionsContext(0, 0, true, Arrays.asList(projection, getColumnProjection())).getAggregationDistinctProjections();
        assertTrue(items.contains(projection));
        assertEquals(items.size(), 1);
    }
    
    @Test
    public void assertGetColumnLabelWithShorthandProjection() {
        Projection projection = getShorthandProjection();
        List<String> columnLabels = new ProjectionsContext(
                0, 0, true, Collections.singletonList(projection)).getColumnLabels(createRelationMetas(), Collections.singletonList(new TableSegment(0, 0, "table")));
        assertEquals(columnLabels, Arrays.asList("id", "name"));
    }
    
    @Test
    public void assertGetColumnLabelWithShorthandProjectionWithoutOwner() {
        Projection projection = getShorthandProjectionWithoutOwner();
        List<String> columnLabels = new ProjectionsContext(
                0, 0, true, Collections.singletonList(projection)).getColumnLabels(createRelationMetas(), Collections.singletonList(new TableSegment(0, 0, "table")));
        assertEquals(columnLabels, Arrays.asList("id", "name"));
    }
    
    @Test
    public void assertGetColumnLabelsWithCommonProjection() {
        Projection projection = getColumnProjection();
        List<String> columnLabels = new ProjectionsContext(0, 0, true, Collections.singletonList(projection)).getColumnLabels(createRelationMetas(), Collections.<TableSegment>emptyList());
        assertTrue(columnLabels.contains(projection.getColumnLabel()));
    }
    
    @Test
    public void assertGetColumnLabelsWithCommonProjectionAlias() {
        Projection projection = getColumnProjectionWithAlias();
        List<String> columnLabels = new ProjectionsContext(0, 0, true, Collections.singletonList(projection)).getColumnLabels(createRelationMetas(), Collections.<TableSegment>emptyList());
        assertTrue(columnLabels.contains(projection.getAlias().or("")));
    }
    
    @Test
    public void assertGetColumnLabelsWithExpressionProjection() {
        Projection projection = getExpressionProjection();
        List<String> columnLabels = new ProjectionsContext(0, 0, true, Collections.singletonList(projection)).getColumnLabels(createRelationMetas(), Collections.<TableSegment>emptyList());
        assertTrue(columnLabels.contains(projection.getColumnLabel()));
    }
    
    @Test
    public void assertGetColumnLabelsWithExpressionProjectionAlias() {
        Projection projection = getExpressionSelectProjectionWithAlias();
        List<String> columnLabels = new ProjectionsContext(0, 0, true, Collections.singletonList(projection)).getColumnLabels(createRelationMetas(), Collections.<TableSegment>emptyList());
        assertTrue(columnLabels.contains(projection.getAlias().or("")));
    }
    
    @Test
    public void assertGetColumnLabelsWithDerivedProjection() {
        Projection projection = getDerivedProjection();
        List<String> columnLabels = new ProjectionsContext(0, 0, true, Collections.singletonList(projection)).getColumnLabels(createRelationMetas(), Collections.<TableSegment>emptyList());
        assertTrue(columnLabels.contains(projection.getColumnLabel()));
    }
    
    @Test
    public void assertGetColumnLabelsWithDerivedProjectionAlias() {
        Projection projection = getDerivedProjectionWithAlias();
        List<String> columnLabels = new ProjectionsContext(0, 0, true, Collections.singletonList(projection)).getColumnLabels(createRelationMetas(), Collections.<TableSegment>emptyList());
        assertTrue(columnLabels.contains(projection.getAlias().or("")));
    }
    
    @Test
    public void assertGetColumnLabelsWithAggregationProjection() {
        Projection projection = getAggregationProjection();
        List<String> columnLabels = new ProjectionsContext(0, 0, true, Collections.singletonList(projection)).getColumnLabels(createRelationMetas(), Collections.<TableSegment>emptyList());
        assertTrue(columnLabels.contains(projection.getColumnLabel()));
    }
    
    @Test
    public void assertGetColumnLabelsWithAggregationDistinctProjection() {
        Projection projection = getAggregationDistinctProjection();
        List<String> columnLabels = new ProjectionsContext(0, 0, true, Collections.singletonList(projection)).getColumnLabels(createRelationMetas(), Collections.<TableSegment>emptyList());
        assertTrue(columnLabels.contains(projection.getColumnLabel()));
    }
    
    private RelationMetas createRelationMetas() {
        Map<String, RelationMetaData> relations = new HashMap<>(1, 1);
        relations.put("table", new RelationMetaData(Arrays.asList("id", "name")));
        return new RelationMetas(relations);
    }
    
    private ShorthandProjection getShorthandProjection() {
        return new ShorthandProjection("table");
    }
    
    private ShorthandProjection getShorthandProjectionWithoutOwner() {
        return new ShorthandProjection(null);
    }
    
    private ColumnProjection getColumnProjection() {
        return new ColumnProjection("table", "name", null);
    }
    
    private ColumnProjection getColumnProjectionWithAlias() {
        return new ColumnProjection("table", "name", "n");
    }
    
    private ExpressionProjection getExpressionProjection() {
        return new ExpressionProjection("table.name", null);
    }
    
    private ExpressionProjection getExpressionSelectProjectionWithAlias() {
        return new ExpressionProjection("table.name", "n");
    }
    
    private DerivedProjection getDerivedProjection() {
        return new DerivedProjection("table.name", null);
    }
    
    private DerivedProjection getDerivedProjectionWithAlias() {
        return new DerivedProjection("table.name", "n");
    }
    
    private AggregationProjection getAggregationProjection() {
        return new AggregationProjection(AggregationType.COUNT, "(column)", "c");
    }
    
    private AggregationDistinctProjection getAggregationDistinctProjection() {
        return new AggregationDistinctProjection(0, 0, AggregationType.COUNT, "(DISTINCT column)", "c", "column");
    }
}
