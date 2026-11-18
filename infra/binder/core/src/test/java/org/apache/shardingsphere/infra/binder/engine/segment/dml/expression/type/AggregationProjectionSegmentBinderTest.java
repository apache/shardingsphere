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

package org.apache.shardingsphere.infra.binder.engine.segment.dml.expression.type;

import com.cedarsoftware.util.CaseInsensitiveMap.CaseInsensitiveString;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import org.apache.shardingsphere.infra.binder.engine.segment.SegmentType;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.expression.ExpressionSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.context.TableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.sql.parser.statement.core.enums.AggregationType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.AggregationProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.WindowItemSegment;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings({ExpressionSegmentBinder.class, WindowItemSegmentBinder.class})
class AggregationProjectionSegmentBinderTest {
    
    @Test
    void assertBindWithFilter() {
        AggregationProjectionSegment segment = new AggregationProjectionSegment(0, 40, AggregationType.SUM, "SUM(col) FILTER (WHERE condition)");
        ExpressionSegment filter = mock(ExpressionSegment.class);
        ExpressionSegment expectedFilter = mock(ExpressionSegment.class);
        segment.setFilter(filter);
        SQLStatementBinderContext binderContext = mock(SQLStatementBinderContext.class);
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts = LinkedHashMultimap.create();
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> outerTableBinderContexts = LinkedHashMultimap.create();
        when(ExpressionSegmentBinder.bind(filter, SegmentType.PROJECTION, binderContext, tableBinderContexts, outerTableBinderContexts)).thenReturn(expectedFilter);
        AggregationProjectionSegment actual = AggregationProjectionSegmentBinder.bind(segment, SegmentType.PROJECTION, binderContext, tableBinderContexts, outerTableBinderContexts);
        assertThat(actual.getFilter().get(), is(expectedFilter));
        assertThat(actual.getStartIndex(), is(segment.getStartIndex()));
        assertThat(actual.getStopIndex(), is(segment.getStopIndex()));
    }
    
    @Test
    void assertBind() {
        AggregationProjectionSegment segment = new AggregationProjectionSegment(0, 72, AggregationType.COUNT, "COUNT(product_id) OVER (PARTITION BY order_id)", ",");
        ExpressionSegment parameter = mock(ExpressionSegment.class);
        segment.getParameters().add(parameter);
        WindowItemSegment windowItemSegment = new WindowItemSegment(33, 72);
        segment.setWindow(windowItemSegment);
        ExpressionSegment expectedParameter = mock(ExpressionSegment.class);
        WindowItemSegment expectedWindowItemSegment = mock(WindowItemSegment.class);
        SQLStatementBinderContext binderContext = mock(SQLStatementBinderContext.class);
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts = LinkedHashMultimap.create();
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> outerTableBinderContexts = LinkedHashMultimap.create();
        when(ExpressionSegmentBinder.bind(parameter, SegmentType.PROJECTION, binderContext, tableBinderContexts, outerTableBinderContexts)).thenReturn(expectedParameter);
        when(WindowItemSegmentBinder.bind(windowItemSegment, SegmentType.PROJECTION, binderContext, tableBinderContexts, outerTableBinderContexts)).thenReturn(expectedWindowItemSegment);
        AggregationProjectionSegment actual = AggregationProjectionSegmentBinder.bind(segment, SegmentType.PROJECTION, binderContext, tableBinderContexts, outerTableBinderContexts);
        assertThat(actual.getParameters().iterator().next(), is(expectedParameter));
        assertThat(actual.getSeparator().orElse(null), is(","));
        assertTrue(actual.getWindow().isPresent());
        assertThat(actual.getWindow().get(), is(expectedWindowItemSegment));
    }
}
