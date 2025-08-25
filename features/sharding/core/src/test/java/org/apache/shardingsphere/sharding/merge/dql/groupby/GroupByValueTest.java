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

import org.apache.shardingsphere.database.connector.core.metadata.database.enums.NullsOrderType;
import org.apache.shardingsphere.infra.binder.context.segment.select.orderby.OrderByItem;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;
import org.apache.shardingsphere.sql.parser.statement.core.enums.OrderDirection;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.item.IndexOrderByItemSegment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GroupByValueTest {
    
    @Mock
    private QueryResult queryResult;
    
    @BeforeEach
    void setUp() throws SQLException {
        when(queryResult.getValue(1, Object.class)).thenReturn("1");
        when(queryResult.getValue(3, Object.class)).thenReturn("3");
    }
    
    @Test
    void assertGetGroupByValues() throws SQLException {
        List<?> actual = new GroupByValue(queryResult, Arrays.asList(
                createOrderByItem(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.ASC, NullsOrderType.FIRST)),
                createOrderByItem(new IndexOrderByItemSegment(0, 0, 3, OrderDirection.DESC, NullsOrderType.FIRST)))).getGroupValues();
        List<?> expected = Arrays.asList("1", "3");
        assertThat(actual, is(expected));
    }
    
    @Test
    void assertGroupByValueEquals() throws SQLException {
        GroupByValue groupByValue1 = new GroupByValue(queryResult, Arrays.asList(
                createOrderByItem(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.ASC, NullsOrderType.FIRST)),
                createOrderByItem(new IndexOrderByItemSegment(0, 0, 3, OrderDirection.DESC, NullsOrderType.FIRST))));
        GroupByValue groupByValue2 = new GroupByValue(queryResult, Arrays.asList(
                createOrderByItem(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.ASC, NullsOrderType.FIRST)),
                createOrderByItem(new IndexOrderByItemSegment(0, 0, 3, OrderDirection.DESC, NullsOrderType.FIRST))));
        assertThat(groupByValue1, is(groupByValue2));
        assertThat(groupByValue2, is(groupByValue1));
        assertThat(groupByValue1.hashCode(), is(groupByValue2.hashCode()));
    }
    
    @Test
    void assertGroupByValueNotEquals() throws SQLException {
        GroupByValue groupByValue1 = new GroupByValue(queryResult, Arrays.asList(
                createOrderByItem(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.ASC, NullsOrderType.FIRST)),
                createOrderByItem(new IndexOrderByItemSegment(0, 0, 3, OrderDirection.DESC, NullsOrderType.FIRST))));
        GroupByValue groupByValue2 = new GroupByValue(queryResult, Arrays.asList(
                createOrderByItem(new IndexOrderByItemSegment(0, 0, 3, OrderDirection.ASC, NullsOrderType.FIRST)),
                createOrderByItem(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.DESC, NullsOrderType.FIRST))));
        assertThat(groupByValue1, not(groupByValue2));
        assertThat(groupByValue1.hashCode(), not(groupByValue2.hashCode()));
    }
    
    private OrderByItem createOrderByItem(final IndexOrderByItemSegment indexOrderByItemSegment) {
        OrderByItem result = new OrderByItem(indexOrderByItemSegment);
        result.setIndex(indexOrderByItemSegment.getColumnIndex());
        return result;
    }
}
