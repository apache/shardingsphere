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
import org.apache.shardingsphere.sql.parser.relation.segment.select.projection.impl.AggregationDistinctProjection;
import org.apache.shardingsphere.sql.parser.relation.segment.select.projection.impl.AggregationProjection;
import org.apache.shardingsphere.sql.parser.relation.segment.select.projection.impl.ColumnProjection;
import org.apache.shardingsphere.sql.parser.relation.segment.select.projection.impl.ShorthandProjection;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class ProjectionsContextTest {
    
    @Test
    public void assertUnqualifiedShorthandProjectionWithEmptyItems() {
        ProjectionsContext projectionsContext = new ProjectionsContext(0, 0, true, Collections.<Projection>emptySet(), Collections.<String>emptyList());
        assertFalse(projectionsContext.isUnqualifiedShorthandProjection());
    }
    
    @Test
    public void assertUnqualifiedShorthandProjectionWithWrongProjection() {
        ProjectionsContext projectionsContext = new ProjectionsContext(0, 0, true, Collections.singleton((Projection) getColumnProjection()), Collections.<String>emptyList());
        assertFalse(projectionsContext.isUnqualifiedShorthandProjection());
    }
    
    @Test
    public void assertUnqualifiedShorthandProjectionWithWrongShortProjection() {
        ProjectionsContext projectionsContext = new ProjectionsContext(0, 0, true, Collections.singleton((Projection) getShorthandProjection()), Collections.<String>emptyList());
        assertFalse(projectionsContext.isUnqualifiedShorthandProjection());
    }
    
    @Test
    public void assertUnqualifiedShorthandProjection() {
        Projection projection = new ShorthandProjection(null);
        ProjectionsContext projectionsContext = new ProjectionsContext(0, 0, true, Collections.singleton(projection), Collections.<String>emptyList());
        assertTrue(projectionsContext.isUnqualifiedShorthandProjection());
    }
    
    @Test
    public void assertFindAliasWithOutAlias() {
        ProjectionsContext projectionsContext = new ProjectionsContext(0, 0, true, Collections.<Projection>emptyList(), Collections.<String>emptyList());
        assertFalse(projectionsContext.findAlias("").isPresent());
    }
    
    @Test
    public void assertFindAlias() {
        Projection projection = getColumnProjectionWithAlias();
        ProjectionsContext projectionsContext = new ProjectionsContext(0, 0, true, Collections.singleton(projection), Collections.<String>emptyList());
        assertTrue(projectionsContext.findAlias(projection.getExpression()).isPresent());
    }
    
    @Test
    public void assertFindProjectionIndex() {
        Projection projection = getColumnProjection();
        ProjectionsContext projectionsContext = new ProjectionsContext(0, 0, true, Collections.singleton(projection), Collections.<String>emptyList());
        Optional<Integer> actual = projectionsContext.findProjectionIndex(projection.getExpression());
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is(1));
    }
    
    @Test
    public void assertFindProjectionIndexFailure() {
        Projection projection = getColumnProjection();
        ProjectionsContext projectionsContext = new ProjectionsContext(0, 0, true, Collections.singleton(projection), Collections.<String>emptyList());
        Optional<Integer> actual = projectionsContext.findProjectionIndex("");
        assertFalse(actual.isPresent());
    }
    
    @Test
    public void assertGetAggregationProjections() {
        Projection projection = getAggregationProjection();
        List<AggregationProjection> items = new ProjectionsContext(0, 0, true, Arrays.asList(projection, getColumnProjection()), Collections.<String>emptyList()).getAggregationProjections();
        assertTrue(items.contains(projection));
        assertThat(items.size(), is(1));
    }
    
    @Test
    public void assertGetAggregationDistinctProjections() {
        Projection projection = getAggregationDistinctProjection();
        List<AggregationDistinctProjection> items = new ProjectionsContext(
                0, 0, true, Arrays.asList(projection, getColumnProjection()), Collections.<String>emptyList()).getAggregationDistinctProjections();
        assertTrue(items.contains(projection));
        assertThat(items.size(), is(1));
    }
    
    private ShorthandProjection getShorthandProjection() {
        return new ShorthandProjection("table");
    }
    
    private ColumnProjection getColumnProjection() {
        return new ColumnProjection("table", "name", null);
    }
    
    private ColumnProjection getColumnProjectionWithAlias() {
        return new ColumnProjection("table", "name", "n");
    }
    
    private AggregationProjection getAggregationProjection() {
        return new AggregationProjection(AggregationType.COUNT, "(column)", "c");
    }
    
    private AggregationDistinctProjection getAggregationDistinctProjection() {
        return new AggregationDistinctProjection(0, 0, AggregationType.COUNT, "(DISTINCT column)", "c", "column");
    }
}
