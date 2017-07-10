/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.rdb.sharding.merger.groupby.row;

import com.dangdang.ddframe.rdb.sharding.constant.OrderType;
import com.dangdang.ddframe.rdb.sharding.merger.fixture.MergerTestUtil;
import com.dangdang.ddframe.rdb.sharding.merger.orderby.OrderByValue;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.OrderItem;
import org.junit.Test;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class OrderByValueTest {
    
    @Test
    public void assertCompareToForAsc() throws SQLException {
        OrderByValue orderByValue1 = new OrderByValue(
                createResultSet("order_1", "order_2", "other_1"), Arrays.asList(new OrderItem(1, OrderType.ASC), new OrderItem(2, OrderType.ASC)));
        orderByValue1.next();
        OrderByValue orderByValue2 = new OrderByValue(
                createResultSet("order_3", "order_4", "other_2"), Arrays.asList(new OrderItem(1, OrderType.ASC), new OrderItem(2, OrderType.ASC)));
        orderByValue2.next();
        assertTrue(orderByValue1.compareTo(orderByValue2) < 0);
    }
    
    @Test
    public void assertCompareToForDesc() throws SQLException {
        OrderByValue orderByValue1 = new OrderByValue(
                createResultSet("order_1", "order_2", "other_1"), Arrays.asList(new OrderItem(1, OrderType.DESC), new OrderItem(2, OrderType.DESC)));
        orderByValue1.next();
        OrderByValue orderByValue2 = new OrderByValue(
                createResultSet("order_3", "order_4", "other_2"), Arrays.asList(new OrderItem(1, OrderType.DESC), new OrderItem(2, OrderType.DESC)));
        orderByValue2.next();
        assertTrue(orderByValue1.compareTo(orderByValue2) > 0);
    }
    
    @Test
    public void assertCompareToWhenEqual() throws SQLException {
        OrderByValue orderByValue = new OrderByValue(
                createResultSet("order_1", "order_2", "other"), Arrays.asList(new OrderItem(1, OrderType.DESC), new OrderItem(2, OrderType.DESC)));
        orderByValue.next();
        assertThat(orderByValue.compareTo(orderByValue), is(0));
    }
    
    private ResultSet createResultSet(final Object... values) throws SQLException {
        return MergerTestUtil.mockResult(Arrays.asList("order_col_1", "order_col_2", "other_col"), Arrays.asList(values));
    }
}
