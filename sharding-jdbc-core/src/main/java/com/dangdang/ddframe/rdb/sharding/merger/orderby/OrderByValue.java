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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.dangdang.ddframe.rdb.sharding.merger.common.ResultSetUtil;
import com.dangdang.ddframe.rdb.sharding.parser.result.merger.OrderByColumn;
import com.google.common.base.Preconditions;

import lombok.RequiredArgsConstructor;

/**
 * 基于结果集的排序对象.
 * 
 * @author zhangliang
 */
public final class OrderByValue implements Comparable<OrderByValue> {
    
    private final List<OrderByColumn> orderByColumns;
    
    private final Value orderByValue;
    
    public OrderByValue(final List<OrderByColumn> orderByColumns, final ResultSet resultSet) throws SQLException {
        this.orderByColumns = orderByColumns;
        orderByValue = new Value(orderByColumns, getValues(resultSet));
    }
    
    private List<Comparable<?>> getValues(final ResultSet resultSet) throws SQLException {
        List<Comparable<?>> result = new ArrayList<>(orderByColumns.size());
        for (OrderByColumn each : orderByColumns) {
            Object value = ResultSetUtil.getValue(each, resultSet);
            Preconditions.checkState(value instanceof Comparable, "Sharding-JDBC: order by value must extends Comparable");
            result.add((Comparable<?>) value);
        }
        return result;
    }
    
    @Override
    public int compareTo(final OrderByValue otherOrderByValue) {
        return orderByValue.compareTo(otherOrderByValue.orderByValue);
    }
    
    @RequiredArgsConstructor
    static final class Value implements Comparable<Value> {
        
        private final List<OrderByColumn> orderByColumns;
        
        private final List<Comparable<?>> values;
        
        @Override
        public int compareTo(final Value otherOrderByValue) {
            for (int i = 0; i < orderByColumns.size(); i++) {
                OrderByColumn thisOrderByColumn = orderByColumns.get(i);
                int result = ResultSetUtil.compareTo(values.get(i), otherOrderByValue.values.get(i), thisOrderByColumn.getOrderByType());
                if (0 != result) {
                    return result;
                }
            }
            return 0;
        }
    }
}
