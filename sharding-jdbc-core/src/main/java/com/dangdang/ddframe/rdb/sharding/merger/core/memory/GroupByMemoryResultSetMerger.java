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

package com.dangdang.ddframe.rdb.sharding.merger.core.memory;

import com.dangdang.ddframe.rdb.sharding.merger.memory.row.GroupByResultSetRow;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.OrderItem;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.selectitem.AggregationSelectItem;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 内存分组归并结果集接口.
 *
 * @author zhangliang
 */
public final class GroupByMemoryResultSetMerger extends AbstractMemoryResultSetMerger {
    
    private final List<OrderItem> groupByItems;
    
    private final List<OrderItem> orderByItems;
    
    private final List<AggregationSelectItem> aggregationColumns;
    
    private final Map<List<Comparable<?>>, GroupByResultSetRow> dataMap;
    
    private Iterator<GroupByResultSetRow> data;
    
    public GroupByMemoryResultSetMerger(final Map<String, Integer> labelAndIndexMap, final List<ResultSet> resultSets,
                                        final List<OrderItem> groupByItems, final List<OrderItem> orderByItems, final List<AggregationSelectItem> aggregationColumns) throws SQLException {
        super(labelAndIndexMap);
        this.groupByItems = groupByItems;
        this.orderByItems = orderByItems;
        this.aggregationColumns = aggregationColumns;
        dataMap = new HashMap<>(1024);
        init(resultSets);
    }
    
    private void init(final List<ResultSet> resultSets) throws SQLException {
        for (ResultSet each : resultSets) {
            while (each.next()) {
                GroupByResultSetRow groupByResultSetRow = new GroupByResultSetRow(each, groupByItems, orderByItems, aggregationColumns);
                if (!dataMap.containsKey(groupByResultSetRow.getGroupItemValues())) {
                    dataMap.put(groupByResultSetRow.getGroupItemValues(), groupByResultSetRow);
                }
                dataMap.get(groupByResultSetRow.getGroupItemValues()).aggregate(each);
            }
        }
        for (GroupByResultSetRow each : dataMap.values()) {
            each.generateResult();
        }
        List<GroupByResultSetRow> data = new ArrayList<>(dataMap.values());
        Collections.sort(data);
        this.data = data.iterator();
        if (!data.isEmpty()) {
            setCurrentResultSetRow(data.get(0));
        }
    }
    
    @Override
    public boolean next() throws SQLException {
        if (data.hasNext()) {
            setCurrentResultSetRow(data.next());
            return true;
        }
        return false;
    }
}
