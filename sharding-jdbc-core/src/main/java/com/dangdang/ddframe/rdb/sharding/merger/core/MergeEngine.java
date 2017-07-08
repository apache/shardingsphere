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

package com.dangdang.ddframe.rdb.sharding.merger.core;

import com.dangdang.ddframe.rdb.sharding.merger.core.decorator.LimitDecoratorResultSetMerger;
import com.dangdang.ddframe.rdb.sharding.merger.core.memory.GroupByMemoryResultSetMerger;
import com.dangdang.ddframe.rdb.sharding.merger.core.stream.IteratorStreamResultSetMerger;
import com.dangdang.ddframe.rdb.sharding.merger.core.stream.OrderByStreamResultSetMerger;
import com.dangdang.ddframe.rdb.sharding.merger.util.ResultSetUtil;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.select.SelectStatement;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * 分片结果集归并引擎.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class MergeEngine {
    
    /**
     * 获取结果集.
     *
     * @param resultSets 结果集列表
     * @param selectStatement SQL语句对象
     * @return 结果集包装
     * @throws SQLException SQL异常
     */
    public static ResultSetMerger getResultSet(final List<ResultSet> resultSets, final SelectStatement selectStatement) throws SQLException {
        selectStatement.setIndexForItems(ResultSetUtil.getColumnLabelIndexMap(resultSets.get(0)));
        ResultSetMerger result = !selectStatement.getGroupByItems().isEmpty() || !selectStatement.getAggregationSelectItems().isEmpty()
                ? buildMemoryResultSet(resultSets, selectStatement) : buildStreamResultSet(resultSets, selectStatement);
        return buildDecorateResultSet(result, selectStatement);
    }
    
    private static ResultSetMerger buildMemoryResultSet(final List<ResultSet> resultSets, final SelectStatement selectStatement) throws SQLException {
        return new GroupByMemoryResultSetMerger(
                ResultSetUtil.getColumnLabelIndexMap(resultSets.get(0)), resultSets, selectStatement.getGroupByItems(), selectStatement.getOrderByItems(), selectStatement.getAggregationSelectItems());
    }
    
    private static ResultSetMerger buildStreamResultSet(final List<ResultSet> resultSets, final SelectStatement selectStatement) throws SQLException {
        if (selectStatement.getGroupByItems().isEmpty() && selectStatement.getOrderByItems().isEmpty()) {
            return new IteratorStreamResultSetMerger(resultSets);
        }
        return new OrderByStreamResultSetMerger(resultSets, selectStatement.getOrderByItems());
    }
    
    private static ResultSetMerger buildDecorateResultSet(final ResultSetMerger resultSetMerger, final SelectStatement selectStatement) throws SQLException {
        ResultSetMerger result = resultSetMerger;
        if (null != selectStatement.getLimit()) {
            result = new LimitDecoratorResultSetMerger(result, selectStatement.getLimit());
        }
        return result;
    }
}
