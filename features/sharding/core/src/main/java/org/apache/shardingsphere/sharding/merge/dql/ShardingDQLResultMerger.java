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

import com.cedarsoftware.util.CaseInsensitiveMap;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.DialectDatabaseMetaData;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.binder.context.segment.select.orderby.OrderByItem;
import org.apache.shardingsphere.infra.binder.context.segment.select.pagination.PaginationContext;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;
import org.apache.shardingsphere.infra.merge.engine.merger.ResultMerger;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.merge.result.impl.stream.IteratorStreamMergedResult;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.sharding.merge.dql.groupby.GroupByMemoryMergedResult;
import org.apache.shardingsphere.sharding.merge.dql.groupby.GroupByStreamMergedResult;
import org.apache.shardingsphere.sharding.merge.dql.orderby.OrderByStreamMergedResult;
import org.apache.shardingsphere.sharding.merge.dql.pagination.builder.PaginationDecoratorMergedResultBuilder;
import org.apache.shardingsphere.sql.parser.statement.core.enums.OrderDirection;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.item.IndexOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.statement.core.util.SQLUtils;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * DQL result merger for Sharding.
 */
@RequiredArgsConstructor
public final class ShardingDQLResultMerger implements ResultMerger {
    
    private final DatabaseType protocolType;
    
    @Override
    public MergedResult merge(final List<QueryResult> queryResults, final SQLStatementContext sqlStatementContext,
                              final ShardingSphereDatabase database, final ConnectionContext connectionContext) throws SQLException {
        if (1 == queryResults.size() && !isNeedAggregateRewrite(sqlStatementContext)) {
            return new IteratorStreamMergedResult(queryResults);
        }
        Map<String, Integer> columnLabelIndexMap = getColumnLabelIndexMap(queryResults.get(0));
        SelectStatementContext selectStatementContext = (SelectStatementContext) sqlStatementContext;
        selectStatementContext.setIndexes(columnLabelIndexMap);
        MergedResult mergedResult = build(queryResults, selectStatementContext, columnLabelIndexMap, database);
        return decorate(queryResults, selectStatementContext, mergedResult);
    }
    
    private boolean isNeedAggregateRewrite(final SQLStatementContext sqlStatementContext) {
        return sqlStatementContext instanceof SelectStatementContext && ((SelectStatementContext) sqlStatementContext).isNeedAggregateRewrite();
    }
    
    private Map<String, Integer> getColumnLabelIndexMap(final QueryResult queryResult) throws SQLException {
        Map<String, Integer> result = new CaseInsensitiveMap<>();
        for (int i = queryResult.getMetaData().getColumnCount(); i > 0; i--) {
            result.put(SQLUtils.getExactlyValue(queryResult.getMetaData().getColumnLabel(i)), i);
        }
        return result;
    }
    
    private MergedResult build(final List<QueryResult> queryResults, final SelectStatementContext selectStatementContext,
                               final Map<String, Integer> columnLabelIndexMap, final ShardingSphereDatabase database) throws SQLException {
        String defaultSchemaName = new DatabaseTypeRegistry(selectStatementContext.getSqlStatement().getDatabaseType()).getDefaultSchemaName(database.getName());
        ShardingSphereSchema schema = selectStatementContext.getTablesContext().getSchemaName()
                .map(database::getSchema).orElseGet(() -> database.getSchema(defaultSchemaName));
        if (isNeedProcessGroupBy(selectStatementContext)) {
            return getGroupByMergedResult(queryResults, selectStatementContext, columnLabelIndexMap, schema);
        }
        if (isNeedProcessDistinctRow(selectStatementContext)) {
            setGroupByForDistinctRow(selectStatementContext);
            return getGroupByMergedResult(queryResults, selectStatementContext, columnLabelIndexMap, schema);
        }
        if (isNeedProcessOrderBy(selectStatementContext)) {
            return new OrderByStreamMergedResult(queryResults, selectStatementContext, schema);
        }
        return new IteratorStreamMergedResult(queryResults);
    }
    
    private boolean isNeedProcessGroupBy(final SelectStatementContext selectStatementContext) {
        return !selectStatementContext.getGroupByContext().getItems().isEmpty() || !selectStatementContext.getProjectionsContext().getAggregationProjections().isEmpty();
    }
    
    private boolean isNeedProcessDistinctRow(final SelectStatementContext selectStatementContext) {
        return selectStatementContext.getProjectionsContext().isDistinctRow();
    }
    
    private void setGroupByForDistinctRow(final SelectStatementContext selectStatementContext) {
        DialectDatabaseMetaData dialectDatabaseMetaData = new DatabaseTypeRegistry(selectStatementContext.getSqlStatement().getDatabaseType()).getDialectDatabaseMetaData();
        for (int index = 1; index <= selectStatementContext.getProjectionsContext().getExpandProjections().size(); index++) {
            OrderByItem orderByItem = new OrderByItem(new IndexOrderByItemSegment(-1, -1, index, OrderDirection.ASC, dialectDatabaseMetaData.getDefaultNullsOrderType()));
            orderByItem.setIndex(index);
            selectStatementContext.getGroupByContext().getItems().add(orderByItem);
        }
    }
    
    private MergedResult getGroupByMergedResult(final List<QueryResult> queryResults, final SelectStatementContext selectStatementContext,
                                                final Map<String, Integer> columnLabelIndexMap, final ShardingSphereSchema schema) throws SQLException {
        return selectStatementContext.isSameGroupByAndOrderByItems()
                ? new GroupByStreamMergedResult(columnLabelIndexMap, queryResults, selectStatementContext, schema)
                : new GroupByMemoryMergedResult(queryResults, selectStatementContext, schema);
    }
    
    private boolean isNeedProcessOrderBy(final SelectStatementContext selectStatementContext) {
        return !selectStatementContext.getOrderByContext().getItems().isEmpty();
    }
    
    private MergedResult decorate(final List<QueryResult> queryResults, final SelectStatementContext selectStatementContext, final MergedResult mergedResult) throws SQLException {
        PaginationContext paginationContext = selectStatementContext.getPaginationContext();
        if (!paginationContext.isHasPagination() || 1 == queryResults.size()) {
            return mergedResult;
        }
        Optional<PaginationDecoratorMergedResultBuilder> paginationDecoratorMergedResultBuilder = DatabaseTypedSPILoader.findService(PaginationDecoratorMergedResultBuilder.class, protocolType);
        return paginationDecoratorMergedResultBuilder.isPresent() ? paginationDecoratorMergedResultBuilder.get().build(mergedResult, paginationContext) : mergedResult;
    }
}
