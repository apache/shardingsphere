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

package com.dangdang.ddframe.rdb.sharding.merger;

import com.dangdang.ddframe.rdb.sharding.merger.decorator.LimitDecoratorResultSetMerger;
import com.dangdang.ddframe.rdb.sharding.merger.memory.GroupByMemoryResultSetMerger;
import com.dangdang.ddframe.rdb.sharding.merger.stream.IteratorStreamResultSetMerger;
import com.dangdang.ddframe.rdb.sharding.merger.stream.OrderByStreamResultSetMerger;
import com.dangdang.ddframe.rdb.sharding.merger.util.ResultSetUtil;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.select.SelectStatement;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * 分片结果集归并引擎.
 *
 * @author zhangliang
 */
public final class MergeEngine {
    
    private final List<ResultSet> resultSets;
    
    private final SelectStatement selectStatement;
    
    private final Map<String, Integer> columnLabelIndexMap;
    
    public MergeEngine(final List<ResultSet> resultSets, final SelectStatement selectStatement) throws SQLException {
        this.resultSets = resultSets;
        this.selectStatement = selectStatement;
        columnLabelIndexMap = ResultSetUtil.getColumnLabelIndexMap(resultSets.get(0));
    }
    
    /**
     * 合并结果集.
     *
     * @return 归并完毕后的结果集
     * @throws SQLException SQL异常
     */
    public ResultSetMerger merge() throws SQLException {
        selectStatement.setIndexForItems(columnLabelIndexMap);
        ResultSetMerger result = !selectStatement.getGroupByItems().isEmpty() || !selectStatement.getAggregationSelectItems().isEmpty() ? buildMemoryResultSet() : buildStreamResultSet();
        return buildDecorateResultSet(result);
    }
    
    private ResultSetMerger buildMemoryResultSet() throws SQLException {
        return new GroupByMemoryResultSetMerger(columnLabelIndexMap, resultSets, selectStatement.getGroupByItems(), selectStatement.getOrderByItems(), selectStatement.getAggregationSelectItems());
    }
    
    private ResultSetMerger buildStreamResultSet() throws SQLException {
        if (selectStatement.getGroupByItems().isEmpty() && selectStatement.getOrderByItems().isEmpty()) {
            return new IteratorStreamResultSetMerger(resultSets);
        }
        return new OrderByStreamResultSetMerger(resultSets, selectStatement.getOrderByItems());
    }
    
    private ResultSetMerger buildDecorateResultSet(final ResultSetMerger resultSetMerger) throws SQLException {
        ResultSetMerger result = resultSetMerger;
        if (null != selectStatement.getLimit()) {
            result = new LimitDecoratorResultSetMerger(result, selectStatement.getLimit());
        }
        return result;
    }
}
