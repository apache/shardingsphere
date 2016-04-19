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

package com.dangdang.ddframe.rdb.sharding.merger.component.coupling;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import com.dangdang.ddframe.rdb.sharding.jdbc.adapter.AbstractRowSetResultSetAdapter;
import com.dangdang.ddframe.rdb.sharding.merger.component.CouplingResultSet;
import com.dangdang.ddframe.rdb.sharding.merger.row.GroupByRow;
import com.dangdang.ddframe.rdb.sharding.merger.row.Row;
import com.dangdang.ddframe.rdb.sharding.parser.result.merger.AggregationColumn;
import com.dangdang.ddframe.rdb.sharding.parser.result.merger.GroupByColumn;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 分组节点结果集.
 * 
 * @author gaohongtao
 */
@RequiredArgsConstructor
@Slf4j
public class GroupByCouplingResultSet extends AbstractRowSetResultSetAdapter implements CouplingResultSet {
    
    private final List<GroupByColumn> groupByColumns;
    
    private final List<AggregationColumn> aggregationColumns;
    
    private ResultSet resultSet;
    
    private boolean hasNext;
    
    @Override
    public void init(final ResultSet preResultSet) {
        setResultSets(Collections.singletonList(preResultSet));
    }
    
    @Override
    protected void initRows(final List<ResultSet> resultSets) throws SQLException {
        resultSet = resultSets.get(0);
        hasNext = resultSet.next();
    }
    
    @Override
    protected Row nextRow() throws SQLException {
        if (!hasNext) {
            return null;
        }
        GroupByRow row = new GroupByRow(resultSet, groupByColumns, aggregationColumns);
        if (aggregationColumns.isEmpty()) {
            return row;
        }
        for (List<Object> groupByKey = row.getGroupByKey(); hasNext && (groupByColumns.isEmpty() || groupByKey.equals(row.getGroupByKey())); hasNext = resultSet.next()) {
            row.aggregate();
        }
        row.generateResult();
        return row;
    }
}
