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
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.AggregationSelectItemContext;
import com.dangdang.ddframe.rdb.sharding.constant.AggregationType;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.GroupByContext;
import com.dangdang.ddframe.rdb.sharding.constant.OrderType;
import com.google.common.base.Optional;
import org.junit.Test;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class GroupByResultSetRowTest {
    
    @Test
    public void assertGetGroupByValues() throws SQLException {
        ResultSet resultSet = MergerTestUtil.mockResult(Arrays.asList("group_col_1", "group_col_2", "other_col"),
                Collections.<ResultSetRow>singletonList(new TestResultSetRow("group_1", "group_2", "other")));
        List<Object> actual = new GroupByResultSetRow(resultSet, Arrays.asList(createGroupByContext("group_col_1", 1), createGroupByContext("group_col_2", 2)),
                Collections.singletonList(new AggregationSelectItemContext("SUM(0)", Optional.<String>absent(), -1, AggregationType.SUM))).getGroupByValues();
        assertThat(actual.size(), is(2));
        assertThat(actual.get(0).toString(), is("group_1"));
        assertThat(actual.get(1).toString(), is("group_2"));
    }
    
    @Test
    public void assertToString() throws Exception {
        ResultSet rs = MergerTestUtil.mockResult(Arrays.asList("user_id", "number"), Arrays.<ResultSetRow>asList(new TestResultSetRow(1, 10), new TestResultSetRow(1, 20)));
        assertTrue(rs.next());
        GroupByContext groupByContext = new GroupByContext(Optional.<String>absent(), "user_id", OrderType.ASC, Optional.<String>absent());
        groupByContext.setColumnIndex(1);
        AggregationSelectItemContext aggregationColumn = new AggregationSelectItemContext("SUM(0)",  Optional.<String>absent(), 2, AggregationType.SUM);
        aggregationColumn.setColumnIndex(2);
        GroupByResultSetRow row = new GroupByResultSetRow(rs, Collections.singletonList(groupByContext), Collections.singletonList(aggregationColumn));
        row.aggregate();
        assertTrue(rs.next());
        row.aggregate();
        row.generateResult();
        assertFalse(rs.next());
    }
    
    private GroupByContext createGroupByContext(final String columnName, final int columnIndex) {
        GroupByContext result = new GroupByContext(Optional.<String>absent(), columnName, OrderType.ASC, Optional.<String>absent());
        result.setColumnIndex(columnIndex);
        return result;
    }
}
