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

package com.dangdang.ddframe.rdb.sharding.merger.resultset.memory.row;

import com.dangdang.ddframe.rdb.sharding.merger.fixture.MergerTestUtil;
import com.dangdang.ddframe.rdb.sharding.merger.fixture.TestResultSetRow;
import com.dangdang.ddframe.rdb.sharding.parser.result.merger.OrderByColumn;
import org.junit.Test;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class OrderByResultSetRowTest {
    
    @Test
    public void assertCompareToForAsc() throws SQLException {
        OrderByResultSetRow orderByResultSetRow1 = new OrderByResultSetRow(createResultSet("order_1", "order_2", "other_1"), 
                Arrays.asList(new OrderByColumn(1, OrderByColumn.OrderByType.ASC), new OrderByColumn(2, OrderByColumn.OrderByType.ASC)));
        OrderByResultSetRow orderByResultSetRow2 = new OrderByResultSetRow(createResultSet("order_3", "order_4", "other_2"), 
                Arrays.asList(new OrderByColumn(1, OrderByColumn.OrderByType.ASC), new OrderByColumn(2, OrderByColumn.OrderByType.ASC)));
        assertTrue(orderByResultSetRow1.compareTo(orderByResultSetRow2) < 0);
    }
    
    @Test
    public void assertCompareToForDesc() throws SQLException {
        OrderByResultSetRow orderByResultSetRow1 = new OrderByResultSetRow(createResultSet("order_1", "order_2", "other_1"), 
                Arrays.asList(new OrderByColumn(1, OrderByColumn.OrderByType.DESC), new OrderByColumn(2, OrderByColumn.OrderByType.DESC)));
        OrderByResultSetRow orderByResultSetRow2 = new OrderByResultSetRow(createResultSet("order_3", "order_4", "other_2"), 
                Arrays.asList(new OrderByColumn(1, OrderByColumn.OrderByType.DESC), new OrderByColumn(2, OrderByColumn.OrderByType.DESC)));
        assertTrue(orderByResultSetRow1.compareTo(orderByResultSetRow2) > 0);
    }
    
    @Test
    public void assertCompareToWhenEqual() throws SQLException {
        OrderByResultSetRow orderByResultSetRow = new OrderByResultSetRow(createResultSet("order_1", "order_2", "other"), 
                Arrays.asList(new OrderByColumn(1, OrderByColumn.OrderByType.DESC), new OrderByColumn(2, OrderByColumn.OrderByType.DESC)));
        assertThat(orderByResultSetRow.compareTo(orderByResultSetRow), is(0));
    }
    
    @Test
    public void assertToString() throws SQLException {
        assertThat(new OrderByResultSetRow(createResultSet("order_1", "order_2", "other"), 
                Arrays.asList(new OrderByColumn(1, OrderByColumn.OrderByType.DESC), new OrderByColumn(2, OrderByColumn.OrderByType.DESC))).toString(), 
                is("Order by columns value is [order_1, order_2]"));
    }
    
    private ResultSet createResultSet(final Object... values) throws SQLException {
        return MergerTestUtil.mockResult(Arrays.asList("order_col_1", "order_col_2", "other_col"), Collections.<ResultSetRow>singletonList(new TestResultSetRow(values)));
    }
}
