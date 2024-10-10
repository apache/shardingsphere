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

package org.apache.shardingsphere.infra.binder.context.segment.select.orderby;

import org.apache.shardingsphere.sql.parser.statement.core.enums.OrderDirection;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.item.OrderByItemSegment;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings("SimplifiableAssertion")
class OrderByItemTest {
    
    @SuppressWarnings("ConstantValue")
    @Test
    void assertEqualsWithNullObject() {
        assertFalse(new OrderByItem(mock(OrderByItemSegment.class)).equals(null));
    }
    
    @Test
    void assertEqualsWithDifferentOrderDirections() {
        OrderByItemSegment segment1 = mock(OrderByItemSegment.class);
        when(segment1.getOrderDirection()).thenReturn(OrderDirection.ASC);
        OrderByItemSegment segment2 = mock(OrderByItemSegment.class);
        when(segment2.getOrderDirection()).thenReturn(OrderDirection.DESC);
        assertFalse(new OrderByItem(segment1).equals(new OrderByItem(segment2)));
    }
    
    @Test
    void assertEqualsWithDifferentIndexes() {
        OrderByItem orderByItem1 = new OrderByItem(mock(OrderByItemSegment.class));
        orderByItem1.setIndex(1);
        OrderByItem orderByItem2 = new OrderByItem(mock(OrderByItemSegment.class));
        orderByItem2.setIndex(2);
        assertFalse(orderByItem1.equals(orderByItem2));
    }
    
    @Test
    void assertEquals() {
        assertTrue(new OrderByItem(mock(OrderByItemSegment.class)).equals(new OrderByItem(mock(OrderByItemSegment.class))));
    }
    
    @Test
    void assertHashcode() {
        OrderByItemSegment segment1 = mock(OrderByItemSegment.class);
        when(segment1.getOrderDirection()).thenReturn(OrderDirection.ASC);
        OrderByItemSegment segment2 = mock(OrderByItemSegment.class);
        when(segment2.getOrderDirection()).thenReturn(OrderDirection.DESC);
        assertThat(new OrderByItem(segment1).hashCode(), is(new OrderByItem(segment1).hashCode()));
    }
}
