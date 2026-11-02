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

package org.apache.shardingsphere.infra.binder.engine.segment.dml.order;

import com.cedarsoftware.util.CaseInsensitiveMap.CaseInsensitiveString;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import org.apache.shardingsphere.infra.binder.engine.segment.SegmentType;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.context.TableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.order.item.OrderByItemSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.infra.exception.kernel.metadata.ColumnNotFoundException;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.OrderBySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.item.OrderByItemSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

class OrderBySegmentBinderTest {
    
    private final SQLStatementBinderContext binderContext = new SQLStatementBinderContext(null, "probe", new HintValueContext(), mock(SQLStatement.class));
    
    private final OrderByItemSegment item = mock(OrderByItemSegment.class);
    
    private final Multimap<CaseInsensitiveString, TableSegmentBinderContext> projections = ImmutableMultimap.of(CaseInsensitiveString.of(""), mock(TableSegmentBinderContext.class));
    
    private final Multimap<CaseInsensitiveString, TableSegmentBinderContext> localTables = ImmutableMultimap.of(CaseInsensitiveString.of("t_user"), mock(TableSegmentBinderContext.class));
    
    private final Multimap<CaseInsensitiveString, TableSegmentBinderContext> outerTables = ImmutableMultimap.of(CaseInsensitiveString.of("t_order"), mock(TableSegmentBinderContext.class));
    
    @Test
    void assertBindProjectedColumnBeforeOuterTable() {
        OrderByItemSegment boundItem = mock(OrderByItemSegment.class);
        binderContext.setOrderByFromTableBinderContexts(outerTables);
        Collection<Multimap<CaseInsensitiveString, TableSegmentBinderContext>> observedScopes = new LinkedList<>();
        try (MockedStatic<OrderByItemSegmentBinder> mockedBinder = mockStatic(OrderByItemSegmentBinder.class)) {
            mockedBinder.when(() -> OrderByItemSegmentBinder.bind(item, binderContext, projections, outerTables, SegmentType.ORDER_BY)).thenAnswer(invocation -> {
                observedScopes.add(binderContext.getOrderByFromTableBinderContexts());
                return boundItem;
            });
            OrderBySegment actual = OrderBySegmentBinder.bind(new OrderBySegment(20, 30, Collections.singleton(item)), binderContext, projections, localTables, outerTables);
            assertThat(actual.getOrderByItems(), contains(boundItem));
            assertThat(actual.getStartIndex(), is(20));
            assertThat(actual.getStopIndex(), is(30));
            assertThat(observedScopes, contains(localTables));
            assertThat(binderContext.getOrderByFromTableBinderContexts(), sameInstance(outerTables));
            mockedBinder.verify(() -> OrderByItemSegmentBinder.bind(item, binderContext, projections, outerTables, SegmentType.ORDER_BY));
            mockedBinder.verifyNoMoreInteractions();
        }
    }
    
    @Test
    void assertBindUnprojectedColumnWithLocalTablesBeforeOuterTables() {
        OrderByItemSegment boundItem = mock(OrderByItemSegment.class);
        try (MockedStatic<OrderByItemSegmentBinder> mockedBinder = mockStatic(OrderByItemSegmentBinder.class)) {
            mockedBinder.when(() -> OrderByItemSegmentBinder.bind(item, binderContext, projections, outerTables, SegmentType.ORDER_BY))
                    .thenThrow(new ColumnNotFoundException("user_id", "order clause"));
            mockedBinder.when(() -> OrderByItemSegmentBinder.bind(item, binderContext, localTables, outerTables, SegmentType.ORDER_BY)).thenReturn(boundItem);
            OrderBySegment actual = OrderBySegmentBinder.bind(new OrderBySegment(20, 30, Collections.singleton(item)), binderContext, projections, localTables, outerTables);
            assertThat(actual.getOrderByItems(), contains(boundItem));
            mockedBinder.verify(() -> OrderByItemSegmentBinder.bind(item, binderContext, localTables, outerTables, SegmentType.ORDER_BY));
            assertNull(binderContext.getOrderByFromTableBinderContexts());
        }
    }
    
    @Test
    void assertBindUnknownColumnPropagatesFailure() {
        ColumnNotFoundException expected = new ColumnNotFoundException("missing", "order clause");
        try (MockedStatic<OrderByItemSegmentBinder> mockedBinder = mockStatic(OrderByItemSegmentBinder.class)) {
            mockedBinder.when(() -> OrderByItemSegmentBinder.bind(item, binderContext, projections, outerTables, SegmentType.ORDER_BY)).thenThrow(expected);
            mockedBinder.when(() -> OrderByItemSegmentBinder.bind(item, binderContext, localTables, outerTables, SegmentType.ORDER_BY)).thenThrow(expected);
            ColumnNotFoundException actual = assertThrows(ColumnNotFoundException.class,
                    () -> OrderBySegmentBinder.bind(new OrderBySegment(20, 30, Collections.singleton(item)), binderContext, projections, localTables, outerTables));
            assertThat(actual, sameInstance(expected));
            assertNull(binderContext.getOrderByFromTableBinderContexts());
        }
    }
}
