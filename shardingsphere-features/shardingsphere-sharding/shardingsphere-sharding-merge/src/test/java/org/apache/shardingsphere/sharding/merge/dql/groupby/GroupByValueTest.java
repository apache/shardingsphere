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

package org.apache.shardingsphere.sharding.merge.dql.groupby;

import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResultSet;
import org.apache.shardingsphere.infra.binder.segment.select.orderby.OrderByItem;
import org.apache.shardingsphere.sql.parser.sql.common.constant.OrderDirection;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.item.IndexOrderByItemSegment;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class GroupByValueTest {
    
    @Mock
    private QueryResultSet queryResultSet;
    
    @Before
    public void setUp() throws SQLException {
        when(queryResultSet.getValue(1, Object.class)).thenReturn("1");
        when(queryResultSet.getValue(3, Object.class)).thenReturn("3");
    }
    
    @Test
    public void assertGetGroupByValues() throws SQLException {
        List<?> actual = new GroupByValue(queryResultSet, Arrays.asList(
                        createOrderByItem(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.ASC, OrderDirection.ASC)),
                        createOrderByItem(new IndexOrderByItemSegment(0, 0, 3, OrderDirection.DESC, OrderDirection.ASC)))).getGroupValues();
        List<?> expected = Arrays.asList("1", "3");
        assertThat(actual, is(expected));
    }
    
    @Test
    public void assertGroupByValueEquals() throws SQLException {
        GroupByValue groupByValue1 = new GroupByValue(queryResultSet, Arrays.asList(
                        createOrderByItem(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.ASC, OrderDirection.ASC)), 
                        createOrderByItem(new IndexOrderByItemSegment(0, 0, 3, OrderDirection.DESC, OrderDirection.ASC))));
        GroupByValue groupByValue2 = new GroupByValue(queryResultSet, Arrays.asList(
                        createOrderByItem(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.ASC, OrderDirection.ASC)), 
                        createOrderByItem(new IndexOrderByItemSegment(0, 0, 3, OrderDirection.DESC, OrderDirection.ASC))));
        assertThat(groupByValue1, is(groupByValue2));
        assertThat(groupByValue2, is(groupByValue1));
        assertThat(groupByValue1.hashCode(), is(groupByValue2.hashCode()));
    }
    
    @Test
    public void assertGroupByValueNotEquals() throws SQLException {
        GroupByValue groupByValue1 = new GroupByValue(queryResultSet, Arrays.asList(
                        createOrderByItem(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.ASC, OrderDirection.ASC)), 
                        createOrderByItem(new IndexOrderByItemSegment(0, 0, 3, OrderDirection.DESC, OrderDirection.ASC))));
        GroupByValue groupByValue2 = new GroupByValue(queryResultSet, Arrays.asList(
                        createOrderByItem(new IndexOrderByItemSegment(0, 0, 3, OrderDirection.ASC, OrderDirection.ASC)), 
                        createOrderByItem(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.DESC, OrderDirection.ASC))));
        assertThat(groupByValue1, not(groupByValue2));
        assertThat(groupByValue1.hashCode(), not(groupByValue2.hashCode()));
    }
    
    private OrderByItem createOrderByItem(final IndexOrderByItemSegment indexOrderByItemSegment) {
        OrderByItem result = new OrderByItem(indexOrderByItemSegment);
        result.setIndex(indexOrderByItemSegment.getColumnIndex());
        return result;
    }
}
