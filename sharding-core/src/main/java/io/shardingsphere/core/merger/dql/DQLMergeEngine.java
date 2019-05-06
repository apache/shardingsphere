/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.core.merger.dql;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.executor.sql.execute.result.AggregationDistinctQueryResult;
import io.shardingsphere.core.executor.sql.execute.result.DistinctQueryResult;
import io.shardingsphere.core.merger.MergeEngine;
import io.shardingsphere.core.merger.MergedResult;
import io.shardingsphere.core.merger.QueryResult;
import io.shardingsphere.core.merger.dql.groupby.GroupByMemoryMergedResult;
import io.shardingsphere.core.merger.dql.groupby.GroupByStreamMergedResult;
import io.shardingsphere.core.merger.dql.iterator.IteratorStreamMergedResult;
import io.shardingsphere.core.merger.dql.orderby.OrderByStreamMergedResult;
import io.shardingsphere.core.merger.dql.pagination.LimitDecoratorMergedResult;
import io.shardingsphere.core.merger.dql.pagination.RowNumberDecoratorMergedResult;
import io.shardingsphere.core.merger.dql.pagination.TopAndRowNumberDecoratorMergedResult;
import io.shardingsphere.core.parsing.parser.context.limit.Limit;
import io.shardingsphere.core.parsing.parser.sql.dql.select.SelectStatement;
import io.shardingsphere.core.util.SQLUtil;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * DQL result set merge engine.
 *
 * @author zhangliang
 * @author panjuan
 */
public final class DQLMergeEngine implements MergeEngine {
    
    private final DatabaseType databaseType;
    
    private final SelectStatement selectStatement;
    
    private final List<QueryResult> queryResults;
    
    private final Map<String, Integer> columnLabelIndexMap;
    
    public DQLMergeEngine(final DatabaseType databaseType, final SelectStatement selectStatement, final List<QueryResult> queryResults) throws SQLException {
        this.databaseType = databaseType;
        this.selectStatement = selectStatement;
        this.queryResults = getRealQueryResults(queryResults);
        columnLabelIndexMap = getColumnLabelIndexMap(this.queryResults.get(0));
    }
    
    private List<QueryResult> getRealQueryResults(final List<QueryResult> queryResults) {
        if (1 == queryResults.size()) {
            return queryResults;
        }
        if (!selectStatement.getAggregationDistinctSelectItems().isEmpty()) {
            return getDividedQueryResults(new AggregationDistinctQueryResult(queryResults, selectStatement.getAggregationDistinctSelectItems()));
        }
        if (selectStatement.getDistinctSelectItem().isPresent()) {
            return getDividedQueryResults(new DistinctQueryResult(queryResults, new ArrayList<>(selectStatement.getDistinctSelectItem().get().getDistinctColumnLabels())));
        }
        return queryResults;
    }
    
    private List<QueryResult> getDividedQueryResults(final DistinctQueryResult distinctQueryResult) {
        return Lists.transform(distinctQueryResult.divide(), new Function<DistinctQueryResult, QueryResult>() {
            
            @Override
            public QueryResult apply(final DistinctQueryResult input) {
                return input;
            }
        });
    }
    
    private Map<String, Integer> getColumnLabelIndexMap(final QueryResult queryResult) throws SQLException {
        Map<String, Integer> result = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (int i = 1; i <= queryResult.getColumnCount(); i++) {
            result.put(SQLUtil.getExactlyValue(queryResult.getColumnLabel(i)), i);
        }
        return result;
    }
    
    @Override
    public MergedResult merge() throws SQLException {
        if (1 == queryResults.size()) {
            return new IteratorStreamMergedResult(queryResults);
        }
        selectStatement.setIndexForItems(columnLabelIndexMap);
        return decorate(build());
    }
    
    private MergedResult build() throws SQLException {
        if (!selectStatement.getGroupByItems().isEmpty() || !selectStatement.getAggregationSelectItems().isEmpty()) {
            return getGroupByMergedResult();
        }
        if (!selectStatement.getOrderByItems().isEmpty()) {
            return new OrderByStreamMergedResult(queryResults, selectStatement.getOrderByItems());
        }
        return new IteratorStreamMergedResult(queryResults);
    }
    
    private MergedResult getGroupByMergedResult() throws SQLException {
        if (selectStatement.isSameGroupByAndOrderByItems()) {
            return new GroupByStreamMergedResult(columnLabelIndexMap, queryResults, selectStatement);
        } else {
            return new GroupByMemoryMergedResult(columnLabelIndexMap, queryResults, selectStatement);
        }
    }
    
    private MergedResult decorate(final MergedResult mergedResult) throws SQLException {
        Limit limit = selectStatement.getLimit();
        if (null == limit || 1 == queryResults.size()) {
            return mergedResult;
        }
        if (DatabaseType.MySQL == databaseType || DatabaseType.PostgreSQL == databaseType || DatabaseType.H2 == databaseType) {
            return new LimitDecoratorMergedResult(mergedResult, selectStatement.getLimit());
        }
        if (DatabaseType.Oracle == databaseType) {
            return new RowNumberDecoratorMergedResult(mergedResult, selectStatement.getLimit());
        }
        if (DatabaseType.SQLServer == databaseType) {
            return new TopAndRowNumberDecoratorMergedResult(mergedResult, selectStatement.getLimit());
        }
        return mergedResult;
    }
}
