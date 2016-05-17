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

package com.dangdang.ddframe.rdb.sharding.merger.orderby;

import com.dangdang.ddframe.rdb.sharding.merger.fixture.MockResultSet;
import com.dangdang.ddframe.rdb.sharding.merger.row.OrderByRow;
import com.dangdang.ddframe.rdb.sharding.parser.result.merger.OrderByColumn;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class OrderByRowTest {
    
    private OrderByRow row;
    
    private List<OrderByColumn> orderByColumns = Collections.singletonList(new OrderByColumn(1, OrderByColumn.OrderByType.ASC));
    
    @Before
    public void setup() throws SQLException {
        MockResultSet rs = new MockResultSet<>(1);
        rs.next();
        row = new OrderByRow(orderByColumns, rs);
    }
    
    @Test
    public void testGetCellSuccess() throws Exception {
        assertThat((int) row.getCell(1), is(1));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testGetCellError() throws Exception {
        assertThat((int) row.getCell(2), is(1));
    }
    
    @Test
    public void testContainsCell() throws Exception {
        assertThat(row.containsCell(1), is(true));
        assertThat(row.containsCell(0), is(false));
        assertThat(row.containsCell(2), is(false));
    }
    
    @Test
    public void testCompareTo() throws Exception {
        MockResultSet rs = new MockResultSet<>(2);
        rs.next();
        assertThat(row.compareTo(new OrderByRow(orderByColumns, rs)), is(-1));
        rs = new MockResultSet<>(1);
        rs.next();
        assertThat(row.compareTo(new OrderByRow(orderByColumns, rs)), is(0));
        rs = new MockResultSet<>(0);
        rs.next();
        assertThat(row.compareTo(new OrderByRow(orderByColumns, rs)), is(1));
    }
}
