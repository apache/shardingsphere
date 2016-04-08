/**
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

package com.dangdang.ddframe.rdb.sharding.merger.orderby;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.dangdang.ddframe.rdb.sharding.parser.result.merger.OrderByColumn;
import com.dangdang.ddframe.rdb.sharding.parser.result.merger.OrderByColumn.OrderByType;

public final class OrderByValueTest {
    
    @Test
    public void assertCompareToWithSame() {
        List<OrderByColumn> columns = Arrays.asList(new OrderByColumn("col1", OrderByType.ASC), new OrderByColumn("col2", OrderByType.DESC));
        List<Comparable<?>> values = createValues(1, 2);
        OrderByValue.Value orderByValue1 = new OrderByValue.Value(columns, values);
        OrderByValue.Value orderByValue2 = new OrderByValue.Value(columns, values);
        assertThat(orderByValue1.compareTo(orderByValue2), is(0));
    }
    
    @Test
    public void assertCompareToWithAscForFirstValue() {
        List<OrderByColumn> columns = Arrays.asList(new OrderByColumn("col1", OrderByType.ASC), new OrderByColumn("col2", OrderByType.DESC));
        OrderByValue.Value orderByValue1 = new OrderByValue.Value(columns, createValues(1, 2));
        OrderByValue.Value orderByValue2 = new OrderByValue.Value(columns, createValues(2, 2));
        assertTrue(orderByValue1.compareTo(orderByValue2) < 0);
    }
    
    @Test
    public void assertCompareToWithDescForFirstValue() {
        List<OrderByColumn> columns = Arrays.asList(new OrderByColumn("col1", OrderByType.ASC), new OrderByColumn("col2", OrderByType.DESC));
        OrderByValue.Value orderByValue1 = new OrderByValue.Value(columns, createValues(1, 2));
        OrderByValue.Value orderByValue2 = new OrderByValue.Value(columns, createValues(2, 2));
        assertTrue(orderByValue1.compareTo(orderByValue2) < 0);
    }
    
    @Test
    public void assertCompareToWithAscForSecondValue() {
        List<OrderByColumn> columns = Arrays.asList(new OrderByColumn("col1", OrderByType.ASC), new OrderByColumn("col2", OrderByType.DESC));
        OrderByValue.Value orderByValue1 = new OrderByValue.Value(columns, createValues(2, 1));
        OrderByValue.Value orderByValue2 = new OrderByValue.Value(columns, createValues(2, 2));
        assertTrue(orderByValue1.compareTo(orderByValue2) > 0);
    }
    
    @Test
    public void assertCompareToWithDescForSecondValue() {
        List<OrderByColumn> columns = Arrays.asList(new OrderByColumn("col1", OrderByType.ASC), new OrderByColumn("col2", OrderByType.DESC));
        OrderByValue.Value orderByValue1 = new OrderByValue.Value(columns, createValues(2, 1));
        OrderByValue.Value orderByValue2 = new OrderByValue.Value(columns, createValues(2, 2));
        assertTrue(orderByValue1.compareTo(orderByValue2) > 0);
    }
    
    private List<Comparable<?>> createValues(final int... values) {
        List<Comparable<?>> result = new ArrayList<>(values.length);
        for (int each : values) {
            result.add(each);
        }
        return result;
    }
}
