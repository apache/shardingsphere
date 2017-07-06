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

import com.dangdang.ddframe.rdb.sharding.merger.util.ResultSetUtil;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.OrderItem;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.selectitem.AggregationSelectItem;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.SQLStatement;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 分片结果集归并工厂.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class MergeResultSetFactory {
    
    /**
     * 获取结果集.
     *
     * @param resultSets 结果集列表
     * @param sqlStatement SQL语句对象
     * @return 结果集包装
     * @throws SQLException SQL异常
     */
    public static Optional<MergeResultSet> getResultSet(final List<ResultSet> resultSets, final SQLStatement sqlStatement) throws SQLException {
        return buildResultSet(resultSets, sqlStatement);
    }
    
    private static Optional<MergeResultSet> buildResultSet(final List<ResultSet> resultSets, final SQLStatement sqlStatement) throws SQLException {
        Map<String, Integer> columnLabelIndexMap = ResultSetUtil.getColumnLabelIndexMap(resultSets.get(0));
        setIndexForAggregationItem(sqlStatement, columnLabelIndexMap);
        setIndexForOrderItem(columnLabelIndexMap, sqlStatement.getOrderByItems());
        setIndexForOrderItem(columnLabelIndexMap, sqlStatement.getGroupByItems());
        List<ResultSet> filteredResults = new ArrayList<>(resultSets.size());
        for (ResultSet each : resultSets) {
            if (each.next()) {
                filteredResults.add(new FilteredResultSet(each));
            }
        }
        if (filteredResults.isEmpty()) {
            return Optional.absent();
        }
        return Optional.of(!sqlStatement.getGroupByItems().isEmpty() || !sqlStatement.getAggregationSelectItems().isEmpty()
                ? buildMemoryResultSet(filteredResults, sqlStatement) : buildStreamResultSet(filteredResults, sqlStatement));
    }
    
    private static MergeResultSet buildMemoryResultSet(final List<ResultSet> resultSets, final SQLStatement sqlStatement) throws SQLException {
        return new GroupByMemoryMergeResultSet(
                ResultSetUtil.getColumnLabelIndexMap(resultSets.get(0)), resultSets, sqlStatement.getGroupByItems(), sqlStatement.getOrderByItems(), sqlStatement.getAggregationSelectItems());
    }
    
    private static MergeResultSet buildStreamResultSet(final List<ResultSet> resultSets, final SQLStatement sqlStatement) throws SQLException {
        if (sqlStatement.getGroupByItems().isEmpty() && sqlStatement.getOrderByItems().isEmpty()) {
            return new IteratorStreamMergeResultSet(resultSets);
        }
        return new OrderByStreamMergeResultSet(resultSets, sqlStatement.getOrderByItems());
    }
    
    private static void setIndexForAggregationItem(final SQLStatement sqlStatement, final Map<String, Integer> columnLabelIndexMap) {
        for (AggregationSelectItem each : sqlStatement.getAggregationSelectItems()) {
            Preconditions.checkState(columnLabelIndexMap.containsKey(each.getColumnLabel()), String.format("Can't find index: %s, please add alias for aggregate selections", each));
            each.setIndex(columnLabelIndexMap.get(each.getColumnLabel()));
            for (AggregationSelectItem derived : each.getDerivedAggregationSelectItems()) {
                Preconditions.checkState(columnLabelIndexMap.containsKey(derived.getColumnLabel()), String.format("Can't find index: %s", derived));
                derived.setIndex(columnLabelIndexMap.get(derived.getColumnLabel()));
            }
        }
    }
    
    private static void setIndexForOrderItem(final Map<String, Integer> columnLabelIndexMap, final List<OrderItem> orderItems) {
        for (OrderItem each : orderItems) {
            if (-1 != each.getIndex()) {
                continue;
            }
            Preconditions.checkState(columnLabelIndexMap.containsKey(each.getColumnLabel()), String.format("Can't find index: %s", each));
            if (columnLabelIndexMap.containsKey(each.getColumnLabel())) {
                each.setIndex(columnLabelIndexMap.get(each.getColumnLabel()));
            }
        }
    }
}
