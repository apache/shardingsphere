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

package org.apache.shardingsphere.core.merge.dql.orderby;

import org.apache.shardingsphere.core.constant.OrderDirection;
import org.apache.shardingsphere.core.merge.fixture.TestQueryResult;
import org.apache.shardingsphere.core.parse.parser.context.orderby.OrderItem;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class OrderByValueTest {
    
    @Mock
    private ResultSet resultSet1;
    
    @Mock
    private ResultSet resultSet2;
    
    @Before
    public void setUp() throws SQLException {
        when(resultSet1.next()).thenReturn(true, false);
        when(resultSet2.next()).thenReturn(true, false);
        when(resultSet1.getObject(1)).thenReturn("1");
        when(resultSet1.getObject(2)).thenReturn("2");
    }
    
    @Test
    public void assertCompareToForAsc() throws SQLException {
        OrderByValue orderByValue1 = new OrderByValue(
                new TestQueryResult(resultSet1), Arrays.asList(new OrderItem(1, OrderDirection.ASC, OrderDirection.ASC), new OrderItem(2, OrderDirection.ASC, OrderDirection.ASC)));
        assertTrue(orderByValue1.next());
        when(resultSet2.getObject(1)).thenReturn("3");
        when(resultSet2.getObject(2)).thenReturn("4");
        OrderByValue orderByValue2 = new OrderByValue(
                new TestQueryResult(resultSet2), Arrays.asList(new OrderItem(1, OrderDirection.ASC, OrderDirection.ASC), new OrderItem(2, OrderDirection.ASC, OrderDirection.ASC)));
        assertTrue(orderByValue2.next());
        assertTrue(orderByValue1.compareTo(orderByValue2) < 0);
        assertFalse(orderByValue1.getQueryResult().next());
        assertFalse(orderByValue2.getQueryResult().next());
    }
    
    @Test
    public void assertCompareToForDesc() throws SQLException {
        OrderByValue orderByValue1 = new OrderByValue(
                new TestQueryResult(resultSet1), Arrays.asList(new OrderItem(1, OrderDirection.DESC, OrderDirection.ASC), new OrderItem(2, OrderDirection.DESC, OrderDirection.ASC)));
        assertTrue(orderByValue1.next());
        when(resultSet2.getObject(1)).thenReturn("3");
        when(resultSet2.getObject(2)).thenReturn("4");
        OrderByValue orderByValue2 = new OrderByValue(
                new TestQueryResult(resultSet2), Arrays.asList(new OrderItem(1, OrderDirection.DESC, OrderDirection.ASC), new OrderItem(2, OrderDirection.DESC, OrderDirection.ASC)));
        assertTrue(orderByValue2.next());
        assertTrue(orderByValue1.compareTo(orderByValue2) > 0);
        assertFalse(orderByValue1.getQueryResult().next());
        assertFalse(orderByValue2.getQueryResult().next());
    }
    
    @Test
    public void assertCompareToWhenEqual() throws SQLException {
        OrderByValue orderByValue1 = new OrderByValue(
                new TestQueryResult(resultSet1), Arrays.asList(new OrderItem(1, OrderDirection.ASC, OrderDirection.ASC), new OrderItem(2, OrderDirection.DESC, OrderDirection.ASC)));
        assertTrue(orderByValue1.next());
        when(resultSet2.getObject(1)).thenReturn("1");
        when(resultSet2.getObject(2)).thenReturn("2");
        OrderByValue orderByValue2 = new OrderByValue(
                new TestQueryResult(resultSet2), Arrays.asList(new OrderItem(1, OrderDirection.ASC, OrderDirection.ASC), new OrderItem(2, OrderDirection.DESC, OrderDirection.ASC)));
        assertTrue(orderByValue2.next());
        assertThat(orderByValue1.compareTo(orderByValue2), is(0));
        assertFalse(orderByValue1.getQueryResult().next());
        assertFalse(orderByValue2.getQueryResult().next());
    }
}
