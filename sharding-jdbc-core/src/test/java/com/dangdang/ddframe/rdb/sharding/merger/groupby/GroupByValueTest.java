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

package com.dangdang.ddframe.rdb.sharding.merger.groupby;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

import com.dangdang.ddframe.rdb.sharding.merger.common.ResultSetQueryIndex;
import com.dangdang.ddframe.rdb.sharding.parser.result.merger.OrderByColumn;
import com.dangdang.ddframe.rdb.sharding.parser.result.merger.OrderByColumn.OrderByType;

public final class GroupByValueTest {
    
    @Test
    public void assertPutSuccess() {
        GroupByValue groupByValue = new GroupByValue();
        groupByValue.put(1, "name1", 1);
        assertThat(groupByValue.getValue(new ResultSetQueryIndex(1)), is((Object) 1));
        assertThat(groupByValue.getValue(new ResultSetQueryIndex("name1")), is((Object) 1));
    }
    
    @Test
    public void assertPutForDuplicatedKey() {
        GroupByValue groupByValue = new GroupByValue();
        groupByValue.put(1, "name1", 1);
        groupByValue.put(1, "name1", 2);
        assertThat(groupByValue.getValue(new ResultSetQueryIndex(1)), is((Object) 1));
        assertThat(groupByValue.getValue(new ResultSetQueryIndex("name1")), is((Object) 1));
    }
    
    @Test
    public void assertCompareToForOtherValueIsNull() {
        GroupByValue groupByValue = new GroupByValue();
        assertThat(groupByValue.compareTo(null), is(-1));
    }
    
    @Test
    public void assertCompareToForLess() {
        GroupByValue groupByValue = new GroupByValue();
        groupByValue.put(1, "name1", 1);
        groupByValue.put(2, "name2", 2);
        groupByValue.addOrderColumns(Arrays.asList(new OrderByColumn("name1", OrderByType.ASC), new OrderByColumn("name2", OrderByType.ASC)));
        GroupByValue otherGroupByValue = new GroupByValue();
        otherGroupByValue.put(1, "name1", 1);
        otherGroupByValue.put(2, "name2", 3);
        otherGroupByValue.addOrderColumns(Arrays.asList(new OrderByColumn("name1", OrderByType.ASC), new OrderByColumn("name2", OrderByType.ASC)));
        assertTrue(groupByValue.compareTo(otherGroupByValue) < 0);
    }
    
    @Test
    public void assertCompareToForGreat() {
        GroupByValue groupByValue = new GroupByValue();
        groupByValue.put(1, "name1", 1);
        groupByValue.put(2, "name2", 2);
        groupByValue.addOrderColumns(Arrays.asList(new OrderByColumn("name1", OrderByType.ASC), new OrderByColumn("name2", OrderByType.ASC)));
        GroupByValue otherGroupByValue = new GroupByValue();
        otherGroupByValue.put(1, "name1", 1);
        otherGroupByValue.put(2, "name2", 1);
        otherGroupByValue.addOrderColumns(Arrays.asList(new OrderByColumn("name1", OrderByType.ASC), new OrderByColumn("name2", OrderByType.ASC)));
        assertTrue(groupByValue.compareTo(otherGroupByValue) > 0);
    }
    
    @Test
    public void assertCompareToForEqual() {
        GroupByValue groupByValue = new GroupByValue();
        groupByValue.put(1, "name1", 1);
        groupByValue.put(2, "name2", 2);
        groupByValue.addOrderColumns(Arrays.asList(new OrderByColumn("name1", OrderByType.ASC), new OrderByColumn("name2", OrderByType.ASC)));
        GroupByValue otherGroupByValue = new GroupByValue();
        otherGroupByValue.put(1, "name1", 1);
        otherGroupByValue.put(2, "name2", 2);
        otherGroupByValue.addOrderColumns(Arrays.asList(new OrderByColumn("name1", OrderByType.ASC), new OrderByColumn("name2", OrderByType.ASC)));
        assertTrue(groupByValue.compareTo(otherGroupByValue) == 0);
    }
}
