/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.sharding.merge.dql;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.core.database.DatabaseTypes;
import org.apache.shardingsphere.sharding.merge.dql.groupby.GroupByMemoryMergedResult;
import org.apache.shardingsphere.sharding.merge.dql.groupby.GroupByStreamMergedResult;
import org.apache.shardingsphere.sharding.merge.dql.iterator.IteratorStreamMergedResult;
import org.apache.shardingsphere.sharding.merge.dql.orderby.OrderByStreamMergedResult;
import org.apache.shardingsphere.sharding.merge.dql.pagination.LimitDecoratorMergedResult;
import org.apache.shardingsphere.sharding.merge.dql.pagination.RowNumberDecoratorMergedResult;
import org.apache.shardingsphere.sharding.merge.dql.pagination.TopAndRowNumberDecoratorMergedResult;
import org.apache.shardingsphere.spi.database.DatabaseType;
import org.apache.shardingsphere.sql.parser.core.constant.OrderDirection;
import org.apache.shardingsphere.sql.parser.relation.segment.select.orderby.OrderByItem;
import org.apache.shardingsphere.sql.parser.relation.segment.select.pagination.PaginationContext;
import org.apache.shardingsphere.sql.parser.relation.statement.impl.SelectSQLStatementContext;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.order.item.IndexOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.util.SQLUtil;
import org.apache.shardingsphere.underlying.execute.QueryResult;
import org.apache.shardingsphere.underlying.merge.MergeEngine;
import org.apache.shardingsphere.underlying.merge.MergedResult;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * DQL result set merge engine.
 *
 * @author zhangliang
 * @author panjuan
 */
@RequiredArgsConstructor
public final class DQLMergeEngine implements MergeEngine {
    
    private final DatabaseType databaseType;
    
    private final SelectSQLStatementContext selectSQLStatementContext;
    
    private final List<QueryResult> queryResults;
    
    @Override
    public MergedResult merge() throws SQLException {
        if (1 == queryResults.size()) {
            return new IteratorStreamMergedResult(queryResults);
        }
        Map<String, Integer> columnLabelIndexMap = getColumnLabelIndexMap(queryResults.get(0));
        selectSQLStatementContext.setIndexes(columnLabelIndexMap);
        return decorate(build(columnLabelIndexMap));
    }
    
    private Map<String, Integer> getColumnLabelIndexMap(final QueryResult queryResult) throws SQLException {
        Map<String, Integer> result = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (int i = queryResult.getColumnCount(); i > 0; i--) {
            result.put(SQLUtil.getExactlyValue(queryResult.getColumnLabel(i)), i);
        }
        return result;
    }
    
    private MergedResult build(final Map<String, Integer> columnLabelIndexMap) throws SQLException {
        if (isNeedProcessGroupBy()) {
            return getGroupByMergedResult(columnLabelIndexMap);
        }
        if (isNeedProcessDistinctRow()) {
            setGroupByForDistinctRow();
            return getGroupByMergedResult(columnLabelIndexMap);
        }
        if (isNeedProcessOrderBy()) {
            return new OrderByStreamMergedResult(queryResults, selectSQLStatementContext.getOrderByContext().getItems());
        }
        return new IteratorStreamMergedResult(queryResults);
    }
    
    private boolean isNeedProcessGroupBy() {
        return !selectSQLStatementContext.getGroupByContext().getItems().isEmpty() || !selectSQLStatementContext.getProjectionsContext().getAggregationProjections().isEmpty();
    }
    
    private boolean isNeedProcessDistinctRow() {
        return selectSQLStatementContext.getProjectionsContext().isDistinctRow();
    }
    
    private void setGroupByForDistinctRow() {
        for (int index = 1; index <= selectSQLStatementContext.getProjectionsContext().getColumnLabels().size(); index++) {
            OrderByItem orderByItem = new OrderByItem(new IndexOrderByItemSegment(-1, -1, index, OrderDirection.ASC, OrderDirection.ASC));
            orderByItem.setIndex(index);
            selectSQLStatementContext.getGroupByContext().getItems().add(orderByItem);
        }
    }
    
    private MergedResult getGroupByMergedResult(final Map<String, Integer> columnLabelIndexMap) throws SQLException {
        return selectSQLStatementContext.isSameGroupByAndOrderByItems()
                ? new GroupByStreamMergedResult(columnLabelIndexMap, queryResults, selectSQLStatementContext)
                : new GroupByMemoryMergedResult(queryResults, selectSQLStatementContext);
    }
    
    private boolean isNeedProcessOrderBy() {
        return !selectSQLStatementContext.getOrderByContext().getItems().isEmpty();
    }
    
    private MergedResult decorate(final MergedResult mergedResult) throws SQLException {
        PaginationContext paginationContext = selectSQLStatementContext.getPaginationContext();
        if (!paginationContext.isHasPagination() || 1 == queryResults.size()) {
            return mergedResult;
        }
        String trunkDatabaseName = DatabaseTypes.getTrunkDatabaseType(databaseType.getName()).getName();
        if ("MySQL".equals(trunkDatabaseName) || "PostgreSQL".equals(trunkDatabaseName)) {
            return new LimitDecoratorMergedResult(mergedResult, paginationContext);
        }
        if ("Oracle".equals(trunkDatabaseName)) {
            return new RowNumberDecoratorMergedResult(mergedResult, paginationContext);
        }
        if ("SQLServer".equals(trunkDatabaseName)) {
            return new TopAndRowNumberDecoratorMergedResult(mergedResult, paginationContext);
        }
        return mergedResult;
    }
}
