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

package org.apache.shardingsphere.sharding.merge.dql.groupby;

import org.apache.shardingsphere.database.exception.core.exception.syntax.table.NoSuchTableException;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.Projection;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.AggregationDistinctProjection;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.AggregationProjection;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;
import org.apache.shardingsphere.infra.merge.result.impl.memory.MemoryMergedResult;
import org.apache.shardingsphere.infra.merge.result.impl.memory.MemoryQueryResultRow;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.sharding.exception.data.NotImplementComparableValueException;
import org.apache.shardingsphere.sharding.merge.dql.groupby.aggregation.AggregationUnit;
import org.apache.shardingsphere.sharding.merge.dql.groupby.aggregation.AggregationUnitFactory;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.statement.core.enums.AggregationType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Memory merged result for group by.
 */
public final class GroupByMemoryMergedResult extends MemoryMergedResult<ShardingRule> {
    
    public GroupByMemoryMergedResult(final List<QueryResult> queryResults, final SelectStatementContext selectStatementContext, final ShardingSphereSchema schema) throws SQLException {
        super(null, schema, selectStatementContext, queryResults);
    }
    
    @Override
    protected List<MemoryQueryResultRow> init(final ShardingRule shardingRule, final ShardingSphereSchema schema,
                                              final SQLStatementContext sqlStatementContext, final List<QueryResult> queryResults) throws SQLException {
        SelectStatementContext selectStatementContext = (SelectStatementContext) sqlStatementContext;
        Map<GroupByValue, MemoryQueryResultRow> dataMap = new HashMap<>(1024, 1F);
        Map<GroupByValue, Map<AggregationProjection, AggregationUnit>> aggregationMap = new HashMap<>(1024, 1F);
        for (QueryResult each : queryResults) {
            while (each.next()) {
                GroupByValue groupByValue = new GroupByValue(each, selectStatementContext.getGroupByContext().getItems());
                initForFirstGroupByValue(selectStatementContext, each, groupByValue, dataMap, aggregationMap);
                aggregate(selectStatementContext, each, groupByValue, aggregationMap);
            }
        }
        setAggregationValueToMemoryRow(selectStatementContext, dataMap, aggregationMap);
        List<Boolean> valueCaseSensitive = queryResults.isEmpty() ? Collections.emptyList() : getValueCaseSensitive(queryResults.iterator().next(), selectStatementContext, schema);
        return getMemoryResultSetRows(selectStatementContext, dataMap, valueCaseSensitive);
    }
    
    private void initForFirstGroupByValue(final SelectStatementContext selectStatementContext, final QueryResult queryResult,
                                          final GroupByValue groupByValue, final Map<GroupByValue, MemoryQueryResultRow> dataMap,
                                          final Map<GroupByValue, Map<AggregationProjection, AggregationUnit>> aggregationMap) throws SQLException {
        if (!dataMap.containsKey(groupByValue)) {
            dataMap.put(groupByValue, new MemoryQueryResultRow(queryResult));
        }
        aggregationMap.computeIfAbsent(groupByValue, unused -> selectStatementContext.getProjectionsContext().getAggregationProjections().stream()
                .collect(Collectors.toMap(Function.identity(),
                        input -> AggregationUnitFactory.create(input.getType(), input instanceof AggregationDistinctProjection, input.getSeparator().orElse(null)))));
    }
    
    private void aggregate(final SelectStatementContext selectStatementContext, final QueryResult queryResult,
                           final GroupByValue groupByValue, final Map<GroupByValue, Map<AggregationProjection, AggregationUnit>> aggregationMap) throws SQLException {
        for (AggregationProjection each : selectStatementContext.getProjectionsContext().getAggregationProjections()) {
            List<Comparable<?>> values = new ArrayList<>(2);
            if (each.getDerivedAggregationProjections().isEmpty()) {
                values.add(getAggregationValue(queryResult, each));
            } else {
                for (AggregationProjection derived : each.getDerivedAggregationProjections()) {
                    values.add(getAggregationValue(queryResult, derived));
                }
            }
            aggregationMap.get(groupByValue).get(each).merge(values);
        }
    }
    
    private Comparable<?> getAggregationValue(final QueryResult queryResult, final AggregationProjection aggregationProjection) throws SQLException {
        Object result = queryResult.getValue(aggregationProjection.getIndex(), Object.class);
        ShardingSpherePreconditions.checkState(null == result || result instanceof Comparable, () -> new NotImplementComparableValueException("Aggregation", result));
        return (Comparable<?>) result;
    }
    
    private void setAggregationValueToMemoryRow(final SelectStatementContext selectStatementContext,
                                                final Map<GroupByValue, MemoryQueryResultRow> dataMap, final Map<GroupByValue, Map<AggregationProjection, AggregationUnit>> aggregationMap) {
        for (Entry<GroupByValue, MemoryQueryResultRow> entry : dataMap.entrySet()) {
            for (AggregationProjection each : selectStatementContext.getProjectionsContext().getAggregationProjections()) {
                entry.getValue().setCell(each.getIndex(), aggregationMap.get(entry.getKey()).get(each).getResult());
            }
        }
    }
    
    private List<Boolean> getValueCaseSensitive(final QueryResult queryResult, final SelectStatementContext selectStatementContext, final ShardingSphereSchema schema) throws SQLException {
        int columnCount = queryResult.getMetaData().getColumnCount();
        List<Boolean> result = new ArrayList<>(columnCount + 1);
        result.add(false);
        for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
            result.add(getValueCaseSensitiveFromTables(queryResult, selectStatementContext, schema, columnIndex));
        }
        return result;
    }
    
    private boolean getValueCaseSensitiveFromTables(final QueryResult queryResult,
                                                    final SelectStatementContext selectStatementContext, final ShardingSphereSchema schema, final int columnIndex) throws SQLException {
        for (SimpleTableSegment each : selectStatementContext.getTablesContext().getSimpleTables()) {
            String tableName = each.getTableName().getIdentifier().getValue();
            ShardingSpherePreconditions.checkState(schema.containsTable(tableName), () -> new NoSuchTableException(tableName));
            ShardingSphereTable table = schema.getTable(tableName);
            String columnName = queryResult.getMetaData().getColumnName(columnIndex);
            if (table.containsColumn(columnName)) {
                return table.getColumn(columnName).isCaseSensitive();
            }
        }
        return false;
    }
    
    private List<MemoryQueryResultRow> getMemoryResultSetRows(final SelectStatementContext selectStatementContext,
                                                              final Map<GroupByValue, MemoryQueryResultRow> dataMap, final List<Boolean> valueCaseSensitive) {
        if (dataMap.isEmpty()) {
            boolean hasGroupBy = !selectStatementContext.getGroupByContext().getItems().isEmpty();
            boolean hasAggregations = !selectStatementContext.getProjectionsContext().getAggregationProjections().isEmpty();
            if (hasGroupBy || !hasAggregations) {
                return Collections.emptyList();
            }
            Object[] data = generateReturnData(selectStatementContext);
            return Collections.singletonList(new MemoryQueryResultRow(data));
        }
        List<MemoryQueryResultRow> result = new ArrayList<>(dataMap.values());
        result.sort(new GroupByRowComparator(selectStatementContext, valueCaseSensitive));
        return result;
    }
    
    private Object[] generateReturnData(final SelectStatementContext selectStatementContext) {
        List<Projection> projections = new LinkedList<>(selectStatementContext.getProjectionsContext().getExpandProjections());
        Object[] result = new Object[projections.size()];
        for (int i = 0; i < projections.size(); i++) {
            if (projections.get(i) instanceof AggregationProjection && AggregationType.COUNT == ((AggregationProjection) projections.get(i)).getType()) {
                result[i] = 0;
            }
        }
        return result;
    }
}
