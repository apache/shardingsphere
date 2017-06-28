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

import com.dangdang.ddframe.rdb.sharding.constant.OrderType;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.OrderItem;
import com.google.common.base.Preconditions;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 具有排序功能的数据行对象.
 * 
 * @author gaohongtao
 * @author zhangliang
 */
public final class OrderByResultSetRow extends AbstractResultSetRow implements Comparable<OrderByResultSetRow> {
    
    private final List<OrderItem> orderItems;
    
    private final List<Comparable<?>> orderValues;
    
    public OrderByResultSetRow(final ResultSet resultSet, final List<OrderItem> orderItems) throws SQLException {
        super(resultSet);
        this.orderItems = orderItems;
        orderValues = getOrderValues();
    }
    
    private List<Comparable<?>> getOrderValues() {
        List<Comparable<?>> result = new ArrayList<>(orderItems.size());
        for (OrderItem each : orderItems) {
            Object value = getCell(each.getIndex());
            Preconditions.checkState(value instanceof Comparable, "Order by value must implements Comparable");
            result.add((Comparable<?>) value);
        }
        return result;
    }
    
    @Override
    public int compareTo(final OrderByResultSetRow o) {
        for (int i = 0; i < orderItems.size(); i++) {
            OrderItem thisOrderBy = orderItems.get(i);
            int result = compareTo(orderValues.get(i), o.orderValues.get(i), thisOrderBy.getType());
            if (0 != result) {
                return result;
            }
        }
        return 0;
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static int compareTo(final Comparable thisValue, final Comparable otherValue, final OrderType type) {
        return OrderType.ASC == type ? thisValue.compareTo(otherValue) : -thisValue.compareTo(otherValue);
    }
}
