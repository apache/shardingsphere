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

package org.apache.shardingsphere.core.merge.dql;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import lombok.Getter;
import org.apache.shardingsphere.core.constant.DatabaseType;
import org.apache.shardingsphere.core.execute.sql.execute.result.AggregationDistinctQueryResult;
import org.apache.shardingsphere.core.execute.sql.execute.result.DistinctQueryResult;
import org.apache.shardingsphere.core.execute.sql.execute.result.QueryResult;
import org.apache.shardingsphere.core.merge.MergeEngine;
import org.apache.shardingsphere.core.merge.MergedResult;
import org.apache.shardingsphere.core.merge.dql.groupby.GroupByMemoryMergedResult;
import org.apache.shardingsphere.core.merge.dql.groupby.GroupByStreamMergedResult;
import org.apache.shardingsphere.core.merge.dql.iterator.IteratorStreamMergedResult;
import org.apache.shardingsphere.core.merge.dql.orderby.OrderByStreamMergedResult;
import org.apache.shardingsphere.core.merge.dql.pagination.LimitDecoratorMergedResult;
import org.apache.shardingsphere.core.merge.dql.pagination.RowNumberDecoratorMergedResult;
import org.apache.shardingsphere.core.merge.dql.pagination.TopAndRowNumberDecoratorMergedResult;
import org.apache.shardingsphere.core.parse.antlr.sql.statement.dml.SelectStatement;
import org.apache.shardingsphere.core.parse.old.parser.context.limit.Limit;
import org.apache.shardingsphere.core.parse.util.SQLUtil;
import org.apache.shardingsphere.core.route.SQLRouteResult;

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
    
    private final SQLRouteResult routeResult;
    
    private final SelectStatement selectStatement;
    
    private final List<QueryResult> queryResults;
    
    @Getter
    private final Map<String, Integer> columnLabelIndexMap;
    
    public DQLMergeEngine(final DatabaseType databaseType, final SQLRouteResult routeResult, final List<QueryResult> queryResults) throws SQLException {
        this.databaseType = databaseType;
        this.routeResult = routeResult;
        this.selectStatement = (SelectStatement) routeResult.getSqlStatement();
        this.queryResults = getRealQueryResults(queryResults);
        columnLabelIndexMap = getColumnLabelIndexMap(this.queryResults.get(0));
    }
    
    private List<QueryResult> getRealQueryResults(final List<QueryResult> queryResults) {
        List<QueryResult> result = queryResults;
        if (1 == result.size()) {
            return result;
        }
        if (!selectStatement.getAggregationDistinctSelectItems().isEmpty()) {
            result = getDividedQueryResults(new AggregationDistinctQueryResult(queryResults, selectStatement.getAggregationDistinctSelectItems()));
        }
        if (isNeedProcessDistinctSelectItem()) {
            result = getDividedQueryResults(new DistinctQueryResult(queryResults, new ArrayList<>(selectStatement.getDistinctSelectItem().get().getDistinctColumnLabels())));
        }
        return result.isEmpty() ? queryResults : result;
    }
    
    private List<QueryResult> getDividedQueryResults(final DistinctQueryResult distinctQueryResult) {
        return Lists.transform(distinctQueryResult.divide(), new Function<DistinctQueryResult, QueryResult>() {
            
            @Override
            public QueryResult apply(final DistinctQueryResult input) {
                return input;
            }
        });
    }
    
    private boolean isNeedProcessDistinctSelectItem() {
        return selectStatement.getDistinctSelectItem().isPresent() && selectStatement.getGroupByItems().isEmpty();
    }
    
    private Map<String, Integer> getColumnLabelIndexMap(final QueryResult queryResult) throws SQLException {
        Map<String, Integer> result = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (int i = queryResult.getColumnCount(); i > 0; i--) {
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
        Limit limit = routeResult.getLimit();
        if (null == limit || 1 == queryResults.size()) {
            return mergedResult;
        }
        if (DatabaseType.MySQL == databaseType || DatabaseType.PostgreSQL == databaseType || DatabaseType.H2 == databaseType) {
            return new LimitDecoratorMergedResult(mergedResult, routeResult.getLimit());
        }
        if (DatabaseType.Oracle == databaseType) {
            return new RowNumberDecoratorMergedResult(mergedResult, routeResult.getLimit());
        }
        if (DatabaseType.SQLServer == databaseType) {
            return new TopAndRowNumberDecoratorMergedResult(mergedResult, routeResult.getLimit());
        }
        return mergedResult;
    }
}
