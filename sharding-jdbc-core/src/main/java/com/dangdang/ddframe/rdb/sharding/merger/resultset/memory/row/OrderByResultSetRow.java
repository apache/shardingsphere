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

import com.dangdang.ddframe.rdb.sharding.merger.util.ResultSetUtil;
import com.dangdang.ddframe.rdb.sharding.parser.result.merger.OrderByColumn;
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
    
    private final List<OrderByColumn> orderByColumns;
    
    private final List<Comparable<?>> orderByValues;
    
    public OrderByResultSetRow(final ResultSet resultSet, final List<OrderByColumn> orderByColumns) throws SQLException {
        super(resultSet);
        this.orderByColumns = orderByColumns;
        orderByValues = loadOrderByValues();
    }
    
    private List<Comparable<?>> loadOrderByValues() {
        List<Comparable<?>> result = new ArrayList<>(orderByColumns.size());
        for (OrderByColumn each : orderByColumns) {
            Object value = getCell(each.getColumnIndex());
            Preconditions.checkState(value instanceof Comparable, "Sharding-JDBC: order by value must extends Comparable");
            result.add((Comparable<?>) value);
        }
        return result;
    }
    
    @Override
    public int compareTo(final OrderByResultSetRow otherOrderByValue) {
        for (int i = 0; i < orderByColumns.size(); i++) {
            OrderByColumn thisOrderByColumn = orderByColumns.get(i);
            int result = ResultSetUtil.compareTo(orderByValues.get(i), otherOrderByValue.orderByValues.get(i), thisOrderByColumn.getOrderByType());
            if (0 != result) {
                return result;
            }
        }
        return 0;
    }
    
    @Override
    public String toString() {
        return String.format("Order by columns value is %s", orderByValues);
    }
}
