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

package com.dangdang.ddframe.rdb.sharding.merger.component.other;

import com.dangdang.ddframe.rdb.sharding.jdbc.adapter.AbstractRowSetResultSetAdapter;
import com.dangdang.ddframe.rdb.sharding.merger.row.OrderByRow;
import com.dangdang.ddframe.rdb.sharding.merger.row.Row;
import com.dangdang.ddframe.rdb.sharding.parser.result.merger.OrderByColumn;
import lombok.RequiredArgsConstructor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * 内存结果集.
 * 
 * @author gaohongtao
 */
@RequiredArgsConstructor
public class MemoryOrderByResultSet extends AbstractRowSetResultSetAdapter {
    
    private final List<OrderByColumn> orderByColumns;
    
    private Iterator<OrderByRow> orderByRowsIterator;
    
    @Override
    protected void initRows(final List<ResultSet> resultSets) throws SQLException {
        List<OrderByRow> orderByRows = new LinkedList<>();
        for (ResultSet each : resultSets) {
            while (each.next()) {
                orderByRows.add(new OrderByRow(orderByColumns, each));
            }
        }
        Collections.sort(orderByRows);
        orderByRowsIterator = orderByRows.iterator();
    }
    
    @Override
    protected Row nextRow() throws SQLException {
        if (orderByRowsIterator.hasNext()) {
            return orderByRowsIterator.next();
        }
        return null;
    }
}
