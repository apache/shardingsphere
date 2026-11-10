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

package org.apache.shardingsphere.infra.binder.engine.segment.dml.from.context.type;

import org.apache.shardingsphere.sql.parser.statement.core.enums.TableSourceType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ShorthandProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.AliasSegment;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;

class SimpleTableSegmentBinderContextTest {
    
    @Test
    void assertRetainDuplicateLabelsInProjectionOrder() {
        ExpressionProjectionSegment first = createProjection("1");
        ExpressionProjectionSegment second = createProjection("'constant'");
        SimpleTableSegmentBinderContext context = new SimpleTableSegmentBinderContext(Arrays.asList(first, second), TableSourceType.TEMPORARY_TABLE);
        assertThat(context.getProjectionSegments(), contains(first, second));
        assertThat(context.findProjectionSegmentByColumnLabel("?column?").get(), is(second));
    }
    
    @Test
    void assertFlattenShorthandWithoutLosingDuplicateLabels() {
        ExpressionProjectionSegment first = createProjection("1");
        ExpressionProjectionSegment second = createProjection("2");
        ShorthandProjectionSegment shorthand = new ShorthandProjectionSegment(0, 0);
        shorthand.getActualProjectionSegments().addAll(Arrays.asList(first, second));
        SimpleTableSegmentBinderContext context = new SimpleTableSegmentBinderContext(Collections.singleton(shorthand), TableSourceType.TEMPORARY_TABLE);
        assertThat(context.getProjectionSegments(), contains(first, second));
    }
    
    @Test
    void assertKeepMapConstructorCompatible() {
        ExpressionProjectionSegment projection = createProjection("1");
        Map<String, ProjectionSegment> projections = Collections.singletonMap("?column?", projection);
        SimpleTableSegmentBinderContext context = new SimpleTableSegmentBinderContext(projections, TableSourceType.TEMPORARY_TABLE);
        assertThat(context.getProjectionSegments(), contains(projection));
        assertThat(context.findProjectionSegmentByColumnLabel("?column?").get(), is(projection));
    }
    
    private ExpressionProjectionSegment createProjection(final String text) {
        ExpressionProjectionSegment result = new ExpressionProjectionSegment(0, 0, text);
        result.setAlias(new AliasSegment(0, 0, new IdentifierValue("?column?")));
        return result;
    }
}
