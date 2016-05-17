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

package com.dangdang.ddframe.rdb.sharding.merger.row;

import com.dangdang.ddframe.rdb.sharding.merger.fixture.MockResultSet;
import com.dangdang.ddframe.rdb.sharding.parser.result.merger.AggregationColumn;
import com.dangdang.ddframe.rdb.sharding.parser.result.merger.GroupByColumn;
import com.dangdang.ddframe.rdb.sharding.parser.result.merger.OrderByColumn;
import com.google.common.base.Optional;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class GroupByRowTest {
    
    @Test
    public void testToString() throws Exception {
        Map<String, Object> rs1 = new LinkedHashMap<>();
        rs1.put("user_id", 1);
        rs1.put("number", 10);
        Map<String, Object> rs2 = new LinkedHashMap<>();
        rs2.put("user_id", 1);
        rs2.put("number", 20);
        MockResultSet rs = new MockResultSet<>(Arrays.asList(rs1, rs2));
        assertThat(rs.next(), is(true));
    
        GroupByColumn groupByColumn = new GroupByColumn(Optional.<String>absent(), "user_id", Optional.<String>absent(), OrderByColumn.OrderByType.ASC);
        groupByColumn.setColumnIndex(1);
        AggregationColumn aggregationColumn = new AggregationColumn("SUM(0)", AggregationColumn.AggregationType.SUM, Optional.<String>absent(), Optional.<String>absent());
        aggregationColumn.setColumnIndex(2);
        
        GroupByRow row = new GroupByRow(rs, Collections.singletonList(groupByColumn), Collections.singletonList(aggregationColumn));
        row.aggregate();
        assertThat(row.toString(), is("GroupByKey is: [1]; Aggregation result is: [{index:2, type:SUM, value:10}]"));
        assertThat(rs.next(), is(true));
        row.aggregate();
        row.generateResult();
        assertThat(row.toString(), is("GroupByKey is: [1]; Aggregation result is: [{index:2, type:SUM, value:30}]"));
        assertThat(rs.next(), is(false));
    }
}
