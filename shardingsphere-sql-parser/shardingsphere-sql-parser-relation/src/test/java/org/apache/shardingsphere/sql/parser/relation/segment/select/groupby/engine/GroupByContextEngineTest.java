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

package org.apache.shardingsphere.sql.parser.relation.segment.select.groupby.engine;

import com.google.common.base.Optional;
import org.apache.shardingsphere.sql.parser.relation.segment.select.groupby.GroupByContext;
import org.apache.shardingsphere.sql.parser.relation.segment.select.orderby.OrderByItem;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.order.GroupBySegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.order.item.ColumnOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.order.item.IndexOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.SelectStatement;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class GroupByContextEngineTest {
    
    @Test
    public void assertCreateGroupByContextWithoutGroupBy() {
        SelectStatement selectStatement = mock(SelectStatement.class);
        when(selectStatement.getGroupBy()).thenReturn(Optional.<GroupBySegment>absent());
        GroupByContext actualGroupByContext = new GroupByContextEngine().createGroupByContext(selectStatement);
        assertTrue(actualGroupByContext.getItems().isEmpty());
        assertThat(actualGroupByContext.getLastIndex(), is(0));
    }
    
    @Test
    public void assertCreateGroupByContextWithGroupBy() {
        SelectStatement selectStatement = mock(SelectStatement.class);
        ColumnOrderByItemSegment columnOrderByItemSegment = mock(ColumnOrderByItemSegment.class);
        IndexOrderByItemSegment indexOrderByItemSegment1 = mock(IndexOrderByItemSegment.class);
        when(indexOrderByItemSegment1.getColumnIndex()).thenReturn(2);
        IndexOrderByItemSegment indexOrderByItemSegment2 = mock(IndexOrderByItemSegment.class);
        when(indexOrderByItemSegment2.getColumnIndex()).thenReturn(3);
        GroupBySegment groupBySegment = mock(GroupBySegment.class);
        when(groupBySegment.getGroupByItems()).thenReturn(Arrays.asList(columnOrderByItemSegment, indexOrderByItemSegment1, indexOrderByItemSegment2));
        when(groupBySegment.getStopIndex()).thenReturn(10);
        when(selectStatement.getGroupBy()).thenReturn(Optional.of(groupBySegment));
        GroupByContext actualGroupByContext = new GroupByContextEngine().createGroupByContext(selectStatement);
        OrderByItem orderByItem1 = new OrderByItem(indexOrderByItemSegment1);
        orderByItem1.setIndex(2);
        OrderByItem orderByItem2 = new OrderByItem(indexOrderByItemSegment2);
        orderByItem2.setIndex(3);
        assertThat(actualGroupByContext.getItems(), is((Collection<OrderByItem>) Arrays.asList(new OrderByItem(columnOrderByItemSegment), orderByItem1, orderByItem2)));
        assertThat(actualGroupByContext.getLastIndex(), is(10));
        List<OrderByItem> results = new ArrayList<>(actualGroupByContext.getItems());
        assertThat(results.get(0).getIndex(), is(0));
        assertThat(results.get(1).getIndex(), is(2));
        assertThat(results.get(2).getIndex(), is(3));
    }
}
