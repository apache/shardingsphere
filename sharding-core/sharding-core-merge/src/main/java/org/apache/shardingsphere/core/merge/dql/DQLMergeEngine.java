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
import org.apache.shardingsphere.core.database.DatabaseTypes;
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
import org.apache.shardingsphere.core.metadata.table.TableMetaData;
import org.apache.shardingsphere.core.metadata.table.TableMetas;
import org.apache.shardingsphere.sql.parser.relation.metadata.RelationMetaData;
import org.apache.shardingsphere.sql.parser.relation.metadata.RelationMetas;
import org.apache.shardingsphere.sql.parser.relation.segment.select.pagination.PaginationContext;
import org.apache.shardingsphere.sql.parser.relation.segment.select.projection.impl.AggregationDistinctProjection;
import org.apache.shardingsphere.sql.parser.relation.statement.impl.SelectSQLStatementContext;
import org.apache.shardingsphere.core.route.SQLRouteResult;
import org.apache.shardingsphere.spi.database.DatabaseType;
import org.apache.shardingsphere.sql.parser.util.SQLUtil;

import java.sql.SQLException;
import java.util.HashMap;
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
    
    private final SelectSQLStatementContext selectSQLStatementContext;
    
    private final List<QueryResult> queryResults;
    
    @Getter
    private final Map<String, Integer> columnLabelIndexMap;
    
    public DQLMergeEngine(final DatabaseType databaseType, final TableMetas tableMetas, final SQLRouteResult routeResult, final List<QueryResult> queryResults) throws SQLException {
        this.databaseType = databaseType;
        this.routeResult = routeResult;
        this.selectSQLStatementContext = (SelectSQLStatementContext) routeResult.getSqlStatementContext();
        this.queryResults = getRealQueryResults(getRelationMetas(tableMetas), queryResults);
        columnLabelIndexMap = getColumnLabelIndexMap(this.queryResults.get(0));
    }
    
    private RelationMetas getRelationMetas(final TableMetas tableMetas) {
        Map<String, RelationMetaData> result = new HashMap<>(tableMetas.getAllTableNames().size());
        for (String each : tableMetas.getAllTableNames()) {
            TableMetaData tableMetaData = tableMetas.get(each);
            result.put(each, new RelationMetaData(tableMetaData.getColumns().keySet()));
        }
        return new RelationMetas(result);
    }
    
    private List<QueryResult> getRealQueryResults(final RelationMetas relationMetas, final List<QueryResult> queryResults) throws SQLException {
        List<QueryResult> result = queryResults;
        if (1 == result.size()) {
            return result;
        }
        List<AggregationDistinctProjection> aggregationDistinctProjections = selectSQLStatementContext.getProjectionsContext().getAggregationDistinctProjections();
        if (!aggregationDistinctProjections.isEmpty()) {
            result = getDividedQueryResults(new AggregationDistinctQueryResult(queryResults, aggregationDistinctProjections));
        }
        if (isNeedProcessDistinctRow()) {
            result = getDividedQueryResults(new DistinctQueryResult(queryResults, selectSQLStatementContext.getColumnLabels(relationMetas)));
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
    
    private boolean isNeedProcessDistinctRow() {
        return selectSQLStatementContext.getProjectionsContext().isDistinctRow() && !isNeedProcessGroupBy();
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
        selectSQLStatementContext.setIndexes(columnLabelIndexMap);
        return decorate(build());
    }
    
    private MergedResult build() throws SQLException {
        if (isNeedProcessGroupBy()) {
            return getGroupByMergedResult();
        }
        if (!selectSQLStatementContext.getOrderByContext().getItems().isEmpty()) {
            return new OrderByStreamMergedResult(queryResults, selectSQLStatementContext.getOrderByContext().getItems());
        }
        return new IteratorStreamMergedResult(queryResults);
    }
    
    private boolean isNeedProcessGroupBy() {
        return !selectSQLStatementContext.getGroupByContext().getItems().isEmpty() || !selectSQLStatementContext.getProjectionsContext().getAggregationProjections().isEmpty();
    }
    
    private MergedResult getGroupByMergedResult() throws SQLException {
        return selectSQLStatementContext.isSameGroupByAndOrderByItems()
                ? new GroupByStreamMergedResult(columnLabelIndexMap, queryResults, selectSQLStatementContext)
                : new GroupByMemoryMergedResult(columnLabelIndexMap, queryResults, selectSQLStatementContext);
    }
    
    private MergedResult decorate(final MergedResult mergedResult) throws SQLException {
        PaginationContext paginationContext = ((SelectSQLStatementContext) routeResult.getSqlStatementContext()).getPaginationContext();
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
