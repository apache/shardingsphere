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

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.shardingsphere.infra.executor.sql.QueryResult;
import org.apache.shardingsphere.infra.merge.result.impl.memory.MemoryMergedResult;
import org.apache.shardingsphere.infra.merge.result.impl.memory.MemoryQueryResultRow;
import org.apache.shardingsphere.sharding.merge.dql.groupby.aggregation.AggregationUnit;
import org.apache.shardingsphere.sharding.merge.dql.groupby.aggregation.AggregationUnitFactory;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.infra.metadata.schema.model.physical.PhysicalColumnMetaData;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.schema.model.physical.PhysicalTableMetaData;
import org.apache.shardingsphere.infra.binder.segment.select.projection.Projection;
import org.apache.shardingsphere.infra.binder.segment.select.projection.impl.AggregationDistinctProjection;
import org.apache.shardingsphere.infra.binder.segment.select.projection.impl.AggregationProjection;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.sql.parser.sql.common.constant.AggregationType;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Memory merged result for group by.
 */
public final class GroupByMemoryMergedResult extends MemoryMergedResult<ShardingRule> {
    
    public GroupByMemoryMergedResult(final List<QueryResult> queryResults, final SelectStatementContext selectStatementContext, final ShardingSphereSchema schema) throws SQLException {
        super(null, schema, selectStatementContext, queryResults);
    }
    
    @Override
    protected List<MemoryQueryResultRow> init(final ShardingRule shardingRule,
                                              final ShardingSphereSchema schema, final SQLStatementContext sqlStatementContext, final List<QueryResult> queryResults) throws SQLException {
        SelectStatementContext selectStatementContext = (SelectStatementContext) sqlStatementContext;
        Map<GroupByValue, MemoryQueryResultRow> dataMap = new HashMap<>(1024);
        Map<GroupByValue, Map<AggregationProjection, AggregationUnit>> aggregationMap = new HashMap<>(1024);
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
        if (!aggregationMap.containsKey(groupByValue)) {
            Map<AggregationProjection, AggregationUnit> map = Maps.toMap(selectStatementContext.getProjectionsContext().getAggregationProjections(), 
                input -> AggregationUnitFactory.create(input.getType(), input instanceof AggregationDistinctProjection));
            aggregationMap.put(groupByValue, map);
        }
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
        Preconditions.checkState(null == result || result instanceof Comparable, "Aggregation value must implements Comparable");
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
        List<Boolean> result = Lists.newArrayList(false);
        for (int columnIndex = 1; columnIndex <= queryResult.getColumnCount(); columnIndex++) {
            result.add(getValueCaseSensitiveFromTables(queryResult, selectStatementContext, schema, columnIndex));
        }
        return result;
    }
    
    private boolean getValueCaseSensitiveFromTables(final QueryResult queryResult, 
                                                    final SelectStatementContext selectStatementContext, final ShardingSphereSchema schema, final int columnIndex) throws SQLException {
        for (SimpleTableSegment each : selectStatementContext.getSimpleTableSegments()) {
            String tableName = each.getTableName().getIdentifier().getValue();
            PhysicalTableMetaData tableMetaData = schema.get(tableName);
            Map<String, PhysicalColumnMetaData> columns = tableMetaData.getColumns();
            String columnName = queryResult.getColumnName(columnIndex);
            if (columns.containsKey(columnName)) {
                return columns.get(columnName).isCaseSensitive();
            }
        }
        return false;
    }
    
    private List<MemoryQueryResultRow> getMemoryResultSetRows(final SelectStatementContext selectStatementContext,
                                                              final Map<GroupByValue, MemoryQueryResultRow> dataMap, final List<Boolean> valueCaseSensitive) {
        if (dataMap.isEmpty()) {
            Object[] data = generateReturnData(selectStatementContext);
            return Collections.singletonList(new MemoryQueryResultRow(data));
        }
        List<MemoryQueryResultRow> result = new ArrayList<>(dataMap.values());
        result.sort(new GroupByRowComparator(selectStatementContext, valueCaseSensitive));
        return result;
    }
    
    private Object[] generateReturnData(final SelectStatementContext selectStatementContext) {
        List<Projection> projections = new LinkedList<>(selectStatementContext.getProjectionsContext().getProjections());
        Object[] result = new Object[projections.size()];
        for (int i = 0; i < projections.size(); i++) {
            if (projections.get(i) instanceof AggregationProjection && AggregationType.COUNT == ((AggregationProjection) projections.get(i)).getType()) {
                result[i] = 0;
            }
        }
        return result;
    }
}
