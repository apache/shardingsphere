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
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.FunctionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.OrderBySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.WindowItemSegment;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Collections;
import java.util.Iterator;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings({ExpressionSegmentBinder.class, OrderBySegmentBinder.class})
class FunctionExpressionSegmentBinderTest {
    
    @Test
    void assertBindSkipsParameterForXmlElement() {
        FunctionSegment functionSegment = new FunctionSegment(0, 5, "XMLELEMENT", "XMLELEMENT()");
        OwnerSegment ownerSegment = new OwnerSegment(0, 0, new IdentifierValue("schema"));
        functionSegment.setOwner(ownerSegment);
        WindowItemSegment windowItemSegment = new WindowItemSegment(0, 0);
        functionSegment.setWindow(windowItemSegment);
        ExpressionSegment skippedParameter = mock(ExpressionSegment.class);
        ExpressionSegment parameterToBind = mock(ExpressionSegment.class);
        functionSegment.getParameters().add(skippedParameter);
        functionSegment.getParameters().add(parameterToBind);
        ExpressionSegment boundParameter = mock(ExpressionSegment.class);
        SQLStatementBinderContext binderContext = mock(SQLStatementBinderContext.class);
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts = LinkedHashMultimap.create();
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> outerTableBinderContexts = LinkedHashMultimap.create();
        when(ExpressionSegmentBinder.bind(parameterToBind, SegmentType.PROJECTION, binderContext, tableBinderContexts, outerTableBinderContexts)).thenReturn(boundParameter);
        FunctionSegment actual = FunctionExpressionSegmentBinder.bind(functionSegment, SegmentType.PROJECTION, binderContext, tableBinderContexts, outerTableBinderContexts);
        Iterator<ExpressionSegment> parameters = actual.getParameters().iterator();
        assertThat(parameters.next(), is(skippedParameter));
        assertThat(parameters.next(), is(boundParameter));
        assertThat(actual.getOwner(), is(ownerSegment));
        assertTrue(actual.getWindow().isPresent());
        assertThat(actual.getWindow().get().getStartIndex(), is(windowItemSegment.getStartIndex()));
        assertThat(actual.getWindow().get().getStopIndex(), is(windowItemSegment.getStopIndex()));
    }
    
    @Test
    void assertBindWithoutSkip() {
        FunctionSegment functionSegment = new FunctionSegment(3, 8, "ABS", "ABS()");
        ExpressionSegment firstParameter = mock(ExpressionSegment.class);
        ExpressionSegment secondParameter = mock(ExpressionSegment.class);
        functionSegment.getParameters().add(firstParameter);
        functionSegment.getParameters().add(secondParameter);
        ExpressionSegment boundFirstParameter = mock(ExpressionSegment.class);
        ExpressionSegment boundSecondParameter = mock(ExpressionSegment.class);
        SQLStatementBinderContext binderContext = mock(SQLStatementBinderContext.class);
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts = LinkedHashMultimap.create();
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> outerTableBinderContexts = LinkedHashMultimap.create();
        when(ExpressionSegmentBinder.bind(firstParameter, SegmentType.PROJECTION, binderContext, tableBinderContexts, outerTableBinderContexts)).thenReturn(boundFirstParameter);
        when(ExpressionSegmentBinder.bind(secondParameter, SegmentType.PROJECTION, binderContext, tableBinderContexts, outerTableBinderContexts)).thenReturn(boundSecondParameter);
        FunctionSegment actual = FunctionExpressionSegmentBinder.bind(functionSegment, SegmentType.PROJECTION, binderContext, tableBinderContexts, outerTableBinderContexts);
        Iterator<ExpressionSegment> parameters = actual.getParameters().iterator();
        assertThat(parameters.next(), is(boundFirstParameter));
        assertThat(parameters.next(), is(boundSecondParameter));
    }
    
    @Test
    void assertBindWindowItem() {
        FunctionSegment functionSegment = new FunctionSegment(7, 64, "ROW_NUMBER", "ROW_NUMBER() OVER (PARTITION BY user_id ORDER BY order_id)");
        WindowItemSegment windowItemSegment = new WindowItemSegment(20, 64);
        ExpressionSegment partitionExpression = mock(ExpressionSegment.class);
        windowItemSegment.setPartitionListSegments(Collections.singleton(partitionExpression));
        OrderBySegment orderBySegment = mock(OrderBySegment.class);
        windowItemSegment.setOrderBySegment(orderBySegment);
        functionSegment.setWindow(windowItemSegment);
        ExpressionSegment boundPartitionExpression = mock(ExpressionSegment.class);
        OrderBySegment boundOrderBySegment = mock(OrderBySegment.class);
        SQLStatementBinderContext binderContext = mock(SQLStatementBinderContext.class);
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts = LinkedHashMultimap.create();
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> outerTableBinderContexts = LinkedHashMultimap.create();
        when(ExpressionSegmentBinder.bind(partitionExpression, SegmentType.PROJECTION, binderContext, tableBinderContexts, outerTableBinderContexts)).thenReturn(boundPartitionExpression);
        when(OrderBySegmentBinder.bind(orderBySegment, binderContext, tableBinderContexts, tableBinderContexts, outerTableBinderContexts)).thenReturn(boundOrderBySegment);
        FunctionSegment actual = FunctionExpressionSegmentBinder.bind(functionSegment, SegmentType.PROJECTION, binderContext, tableBinderContexts, outerTableBinderContexts);
        assertThat(actual.getWindow().get().getPartitionListSegments().iterator().next(), is(boundPartitionExpression));
        assertThat(actual.getWindow().get().getOrderBySegment(), is(boundOrderBySegment));
    }
}
