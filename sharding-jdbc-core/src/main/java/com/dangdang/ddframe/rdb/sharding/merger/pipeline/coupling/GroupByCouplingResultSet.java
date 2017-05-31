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

package com.dangdang.ddframe.rdb.sharding.merger.pipeline.coupling;

import com.dangdang.ddframe.rdb.sharding.merger.ResultSetMergeContext;
import com.dangdang.ddframe.rdb.sharding.merger.resultset.memory.AbstractMemoryResultSet;
import com.dangdang.ddframe.rdb.sharding.merger.resultset.memory.row.GroupByResultSetRow;
import com.dangdang.ddframe.rdb.sharding.merger.resultset.memory.row.ResultSetRow;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.selectitem.AggregationSelectItem;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.GroupBy;
import com.google.common.base.Optional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

/**
 * 分组的连接结果集.
 * 
 * @author gaohongtao
 * @author zhangliang
 */
public final class GroupByCouplingResultSet extends AbstractMemoryResultSet {
    
    private final List<GroupBy> groupByList;
    
    private final List<AggregationSelectItem> aggregationColumns;
    
    private ResultSet resultSet;
    
    private boolean hasNext;
    
    public GroupByCouplingResultSet(final ResultSet resultSet, final ResultSetMergeContext resultSetMergeContext) throws SQLException {
        super(Collections.singletonList(resultSet));
        groupByList = resultSetMergeContext.getSqlStatement().getGroupByList();
        aggregationColumns = resultSetMergeContext.getSqlStatement().getAggregationSelectItems();
    }
    
    @Override
    protected void initRows(final List<ResultSet> resultSets) throws SQLException {
        resultSet = resultSets.get(0);
        hasNext = resultSet.next();
    }
    
    @Override
    protected Optional<? extends ResultSetRow> nextRow() throws SQLException {
        if (!hasNext) {
            return Optional.absent();
        }
        GroupByResultSetRow result = new GroupByResultSetRow(resultSet, groupByList, aggregationColumns);
        List<Object> groupByValues = result.getGroupByValues();
        while (hasNext && (groupByList.isEmpty() || groupByValues.equals(result.getGroupByValues()))) {
            result.aggregate();
            hasNext = resultSet.next();
        }
        result.generateResult();
        return Optional.of(result);
    }
}
