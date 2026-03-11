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
import org.apache.shardingsphere.infra.binder.engine.segment.dml.order.OrderBySegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.OrderBySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.WindowItemSegment;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings({ExpressionSegmentBinder.class, OrderBySegmentBinder.class})
class WindowItemSegmentBinderTest {
    
    @Test
    void assertBind() {
        WindowItemSegment windowItemSegment = new WindowItemSegment(20, 64);
        IdentifierValue expectedWindowName = new IdentifierValue("w");
        windowItemSegment.setWindowName(expectedWindowName);
        ExpressionSegment partitionExpression = mock(ExpressionSegment.class);
        windowItemSegment.setPartitionListSegments(Collections.singleton(partitionExpression));
        OrderBySegment orderBySegment = mock(OrderBySegment.class);
        windowItemSegment.setOrderBySegment(orderBySegment);
        ExpressionSegment expectedFrameClause = mock(ExpressionSegment.class);
        windowItemSegment.setFrameClause(expectedFrameClause);
        ExpressionSegment expectedPartitionExpression = mock(ExpressionSegment.class);
        OrderBySegment expectedOrderBySegment = mock(OrderBySegment.class);
        SQLStatementBinderContext binderContext = mock(SQLStatementBinderContext.class);
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts = LinkedHashMultimap.create();
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> outerTableBinderContexts = LinkedHashMultimap.create();
        when(ExpressionSegmentBinder.bind(partitionExpression, SegmentType.PROJECTION, binderContext, tableBinderContexts, outerTableBinderContexts)).thenReturn(expectedPartitionExpression);
        when(OrderBySegmentBinder.bind(orderBySegment, binderContext, tableBinderContexts, tableBinderContexts, outerTableBinderContexts)).thenReturn(expectedOrderBySegment);
        WindowItemSegment actual = WindowItemSegmentBinder.bind(windowItemSegment, SegmentType.PROJECTION, binderContext, tableBinderContexts, outerTableBinderContexts);
        assertThat(actual.getWindowName(), is(expectedWindowName));
        assertThat(actual.getPartitionListSegments().iterator().next(), is(expectedPartitionExpression));
        assertThat(actual.getOrderBySegment(), is(expectedOrderBySegment));
        assertThat(actual.getFrameClause(), is(expectedFrameClause));
    }
    
    @Test
    void assertBindWithoutPartitionAndOrderBy() {
        WindowItemSegment windowItemSegment = new WindowItemSegment(20, 64);
        IdentifierValue expectedWindowName = new IdentifierValue("w");
        windowItemSegment.setWindowName(expectedWindowName);
        ExpressionSegment expectedFrameClause = mock(ExpressionSegment.class);
        windowItemSegment.setFrameClause(expectedFrameClause);
        SQLStatementBinderContext binderContext = mock(SQLStatementBinderContext.class);
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts = LinkedHashMultimap.create();
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> outerTableBinderContexts = LinkedHashMultimap.create();
        WindowItemSegment actual = WindowItemSegmentBinder.bind(windowItemSegment, SegmentType.PROJECTION, binderContext, tableBinderContexts, outerTableBinderContexts);
        assertThat(actual.getWindowName(), is(expectedWindowName));
        assertNull(actual.getPartitionListSegments());
        assertNull(actual.getOrderBySegment());
        assertThat(actual.getFrameClause(), is(expectedFrameClause));
    }
}
