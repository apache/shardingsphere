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

import com.google.common.collect.Maps;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.AggregationDistinctProjection;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.AggregationProjection;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.sharding.exception.data.NotImplementComparableValueException;
import org.apache.shardingsphere.sharding.merge.dql.groupby.aggregation.AggregationUnit;
import org.apache.shardingsphere.sharding.merge.dql.groupby.aggregation.AggregationUnitFactory;
import org.apache.shardingsphere.sharding.merge.dql.orderby.OrderByStreamMergedResult;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Stream merged result for group by.
 */
public final class GroupByStreamMergedResult extends OrderByStreamMergedResult {
    
    private final SelectStatementContext selectStatementContext;
    
    private final List<Object> currentRow;
    
    private List<?> currentGroupByValues;
    
    public GroupByStreamMergedResult(final Map<String, Integer> labelAndIndexMap, final List<QueryResult> queryResults,
                                     final SelectStatementContext selectStatementContext, final ShardingSphereSchema schema) throws SQLException {
        super(queryResults, selectStatementContext, schema);
        this.selectStatementContext = selectStatementContext;
        currentRow = new ArrayList<>(labelAndIndexMap.size());
        currentGroupByValues = getOrderByValuesQueue().isEmpty()
                ? Collections.emptyList()
                : new GroupByValue(getCurrentQueryResult(), selectStatementContext.getGroupByContext().getItems()).getGroupValues();
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
    
    @HighFrequencyInvocation
    private boolean aggregateCurrentGroupByRowAndNext() throws SQLException {
        boolean result = false;
        boolean cachedRow = false;
        Map<AggregationProjection, AggregationUnit> aggregationUnitMap = Maps.toMap(selectStatementContext.getProjectionsContext().getAggregationProjections(),
                input -> AggregationUnitFactory.create(input.getType(), input instanceof AggregationDistinctProjection, input.getSeparator().orElse(null)));
        Collection<AggregationProjection> aggregationProjections = aggregationUnitMap.keySet();
        while (currentGroupByValues.equals(new GroupByValue(getCurrentQueryResult(), selectStatementContext.getGroupByContext().getItems()).getGroupValues())) {
            aggregate(aggregationUnitMap, aggregationProjections);
            if (!cachedRow) {
                cacheCurrentRow();
                cachedRow = true;
            }
            result = super.next();
            if (!result) {
                break;
            }
        }
        setAggregationValueToCurrentRow(aggregationUnitMap, aggregationProjections);
        return result;
    }
    
    private void aggregate(final Map<AggregationProjection, AggregationUnit> aggregationUnitMap, final Collection<AggregationProjection> aggregationProjections) throws SQLException {
        for (AggregationProjection each : aggregationProjections) {
            List<Comparable<?>> values = getAggregationValues(each);
            aggregationUnitMap.get(each).merge(values);
        }
    }
    
    private List<Comparable<?>> getAggregationValues(final AggregationProjection aggregationProjection) throws SQLException {
        List<Comparable<?>> result = new ArrayList<>(2);
        if (aggregationProjection.getDerivedAggregationProjections().isEmpty()) {
            result.add(getAggregationValue(aggregationProjection));
        } else {
            for (AggregationProjection each : aggregationProjection.getDerivedAggregationProjections()) {
                result.add(getAggregationValue(each));
            }
        }
        return result;
    }
    
    private Comparable<?> getAggregationValue(final AggregationProjection aggregationProjection) throws SQLException {
        Object result = getCurrentQueryResult().getValue(aggregationProjection.getIndex(), Object.class);
        ShardingSpherePreconditions.checkState(null == result || result instanceof Comparable, () -> new NotImplementComparableValueException("Aggregation", result));
        return (Comparable<?>) result;
    }
    
    private void cacheCurrentRow() throws SQLException {
        for (int i = 0; i < getCurrentQueryResult().getMetaData().getColumnCount(); i++) {
            currentRow.add(getCurrentQueryResult().getValue(i + 1, Object.class));
        }
    }
    
    private void setAggregationValueToCurrentRow(final Map<AggregationProjection, AggregationUnit> aggregationUnitMap, final Collection<AggregationProjection> aggregationProjections) {
        for (AggregationProjection each : aggregationProjections) {
            currentRow.set(each.getIndex() - 1, aggregationUnitMap.get(each).getResult());
        }
    }
    
    @Override
    public Object getValue(final int columnIndex, final Class<?> type) {
        Object result = currentRow.get(columnIndex - 1);
        setWasNull(null == result);
        return result;
    }
    
    @Override
    public Object getCalendarValue(final int columnIndex, final Class<?> type, @SuppressWarnings("UseOfObsoleteDateTimeApi") final Calendar calendar) {
        Object result = currentRow.get(columnIndex - 1);
        setWasNull(null == result);
        return result;
    }
}
