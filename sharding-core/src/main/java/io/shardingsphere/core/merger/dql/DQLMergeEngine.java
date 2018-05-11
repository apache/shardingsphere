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

import io.shardingsphere.core.constant.DatabaseType;
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
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * DQL result set merge engine.
 *
 * @author zhangliang
 */
public final class DQLMergeEngine implements MergeEngine {
    
    private final List<QueryResult> queryResults;
    
    private final SelectStatement selectStatement;
    
    private final Map<String, Integer> columnLabelIndexMap;
    
    public DQLMergeEngine(final List<QueryResult> queryResults, final SelectStatement selectStatement) throws SQLException {
        this.queryResults = queryResults;
        this.selectStatement = selectStatement;
        columnLabelIndexMap = getColumnLabelIndexMap(queryResults.get(0));
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
        selectStatement.setIndexForItems(columnLabelIndexMap);
        return decorate(build());
    }
    
    private MergedResult build() throws SQLException {
        if (!selectStatement.getGroupByItems().isEmpty() || !selectStatement.getAggregationSelectItems().isEmpty()) {
            if (selectStatement.isSameGroupByAndOrderByItems()) {
                return new GroupByStreamMergedResult(columnLabelIndexMap, queryResults, selectStatement);
            } else {
                return new GroupByMemoryMergedResult(columnLabelIndexMap, queryResults, selectStatement);
            }
        }
        if (!selectStatement.getOrderByItems().isEmpty()) {
            return new OrderByStreamMergedResult(queryResults, selectStatement.getOrderByItems());
        }
        return new IteratorStreamMergedResult(queryResults);
    }
    
    private MergedResult decorate(final MergedResult mergedResult) throws SQLException {
        Limit limit = selectStatement.getLimit();
        if (null == limit) {
            return mergedResult;
        }
        if (DatabaseType.MySQL == limit.getDatabaseType() || DatabaseType.PostgreSQL == limit.getDatabaseType() || DatabaseType.H2 == limit.getDatabaseType()) {
            return new LimitDecoratorMergedResult(mergedResult, selectStatement.getLimit());
        }
        if (DatabaseType.Oracle == limit.getDatabaseType()) {
            return new RowNumberDecoratorMergedResult(mergedResult, selectStatement.getLimit());
        }
        if (DatabaseType.SQLServer == limit.getDatabaseType()) {
            return new TopAndRowNumberDecoratorMergedResult(mergedResult, selectStatement.getLimit());
        }
        return mergedResult;
    }
}
