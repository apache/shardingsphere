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

package com.dangdang.ddframe.rdb.sharding.merger.groupby;

import com.dangdang.ddframe.rdb.sharding.constant.OrderType;
import com.dangdang.ddframe.rdb.sharding.merger.common.MemoryResultSetRow;
import com.dangdang.ddframe.rdb.sharding.merger.util.ResultSetUtil;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.OrderItem;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.select.SelectStatement;
import com.google.common.base.Preconditions;
import lombok.RequiredArgsConstructor;

import java.util.Comparator;
import java.util.List;

/**
 * 内存分组内存数据行比较器.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class GroupByRowComparator implements Comparator<MemoryResultSetRow> {
    
    private final SelectStatement selectStatement;
    
    private final OrderType nullOrderType;
    
    @Override
    public int compare(final MemoryResultSetRow o1, final MemoryResultSetRow o2) {
        if (!selectStatement.getOrderByItems().isEmpty()) {
            return compare(o1, o2, selectStatement.getOrderByItems());
        }
        return compare(o1, o2, selectStatement.getGroupByItems());
    }
    
    private int compare(final MemoryResultSetRow o1, final MemoryResultSetRow o2, final List<OrderItem> orderItems) {
        for (OrderItem each : orderItems) {
            Object orderValue1 = o1.getCell(each.getIndex());
            Preconditions.checkState(null == orderValue1 || orderValue1 instanceof Comparable, "Order by value must implements Comparable");
            Object orderValue2 = o2.getCell(each.getIndex());
            Preconditions.checkState(null == orderValue2 || orderValue2 instanceof Comparable, "Order by value must implements Comparable");
            int result = ResultSetUtil.compareTo((Comparable) orderValue1, (Comparable) orderValue2, each.getType(), nullOrderType);
            if (0 != result) {
                return result;
            }
        }
        return 0;
    }
}
