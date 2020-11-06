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
import com.google.common.collect.Maps;
import org.apache.shardingsphere.sharding.merge.dql.groupby.aggregation.AggregationUnit;
import org.apache.shardingsphere.sharding.merge.dql.groupby.aggregation.AggregationUnitFactory;
import org.apache.shardingsphere.sharding.merge.dql.orderby.OrderByStreamMergedResult;
import org.apache.shardingsphere.infra.metadata.schema.model.physical.PhysicalSchemaMetaData;
import org.apache.shardingsphere.infra.binder.segment.select.projection.impl.AggregationDistinctProjection;
import org.apache.shardingsphere.infra.binder.segment.select.projection.impl.AggregationProjection;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.executor.sql.QueryResult;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Stream merged result for group by.
 */
public final class GroupByStreamMergedResult extends OrderByStreamMergedResult {
    
    private final SelectStatementContext selectStatementContext;
    
    private final List<Object> currentRow;
    
    private List<?> currentGroupByValues;
    
    public GroupByStreamMergedResult(final Map<String, Integer> labelAndIndexMap, final List<QueryResult> queryResults,
                                     final SelectStatementContext selectStatementContext, final PhysicalSchemaMetaData schemaMetaData) throws SQLException {
        super(queryResults, selectStatementContext, schemaMetaData);
        this.selectStatementContext = selectStatementContext;
        currentRow = new ArrayList<>(labelAndIndexMap.size());
        currentGroupByValues = getOrderByValuesQueue().isEmpty()
                ? Collections.emptyList() : new GroupByValue(getCurrentQueryResult(), selectStatementContext.getGroupByContext().getItems()).getGroupValues();
    }
    
    @Override
    public boolean next() throws SQLException {
        currentRow.clear();
        if (getOrderByValuesQueue().isEmpty()) {
            return false;
        }
        if (isFirstNext()) {
            super.next();
        }
        if (aggregateCurrentGroupByRowAndNext()) {
            currentGroupByValues = new GroupByValue(getCurrentQueryResult(), selectStatementContext.getGroupByContext().getItems()).getGroupValues();
        }
        return true;
    }
    
    private boolean aggregateCurrentGroupByRowAndNext() throws SQLException {
        boolean result = false;
        Map<AggregationProjection, AggregationUnit> aggregationUnitMap = Maps.toMap(
                selectStatementContext.getProjectionsContext().getAggregationProjections(), input -> AggregationUnitFactory.create(input.getType(), input instanceof AggregationDistinctProjection));
        while (currentGroupByValues.equals(new GroupByValue(getCurrentQueryResult(), selectStatementContext.getGroupByContext().getItems()).getGroupValues())) {
            aggregate(aggregationUnitMap);
            cacheCurrentRow();
            result = super.next();
            if (!result) {
                break;
            }
        }
        setAggregationValueToCurrentRow(aggregationUnitMap);
        return result;
    }
    
    private void aggregate(final Map<AggregationProjection, AggregationUnit> aggregationUnitMap) throws SQLException {
        for (Entry<AggregationProjection, AggregationUnit> entry : aggregationUnitMap.entrySet()) {
            List<Comparable<?>> values = new ArrayList<>(2);
            if (entry.getKey().getDerivedAggregationProjections().isEmpty()) {
                values.add(getAggregationValue(entry.getKey()));
            } else {
                for (AggregationProjection each : entry.getKey().getDerivedAggregationProjections()) {
                    values.add(getAggregationValue(each));
                }
            }
            entry.getValue().merge(values);
        }
    }
    
    private void cacheCurrentRow() throws SQLException {
        for (int i = 0; i < getCurrentQueryResult().getColumnCount(); i++) {
            currentRow.add(getCurrentQueryResult().getValue(i + 1, Object.class));
        }
    }
    
    private Comparable<?> getAggregationValue(final AggregationProjection aggregationProjection) throws SQLException {
        Object result = getCurrentQueryResult().getValue(aggregationProjection.getIndex(), Object.class);
        Preconditions.checkState(null == result || result instanceof Comparable, "Aggregation value must implements Comparable");
        return (Comparable<?>) result;
    }
    
    private void setAggregationValueToCurrentRow(final Map<AggregationProjection, AggregationUnit> aggregationUnitMap) {
        for (Entry<AggregationProjection, AggregationUnit> entry : aggregationUnitMap.entrySet()) {
            currentRow.set(entry.getKey().getIndex() - 1, entry.getValue().getResult());
        }
    }
    
    @Override
    public Object getValue(final int columnIndex, final Class<?> type) {
        Object result = currentRow.get(columnIndex - 1);
        setWasNull(null == result);
        return result;
    }
    
    @Override
    public Object getCalendarValue(final int columnIndex, final Class<?> type, final Calendar calendar) {
        Object result = currentRow.get(columnIndex - 1);
        setWasNull(null == result);
        return result;
    }
}
