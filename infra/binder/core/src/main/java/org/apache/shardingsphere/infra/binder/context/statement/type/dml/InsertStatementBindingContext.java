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

package org.apache.shardingsphere.infra.binder.context.statement.type.dml;

import lombok.Getter;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.binder.context.segment.insert.keygen.GeneratedKeyContext;
import org.apache.shardingsphere.infra.binder.context.segment.insert.keygen.engine.GeneratedKeyContextEngine;
import org.apache.shardingsphere.infra.binder.context.segment.insert.values.InsertSelectContext;
import org.apache.shardingsphere.infra.binder.context.segment.insert.values.InsertValueContext;
import org.apache.shardingsphere.infra.binder.context.segment.insert.values.OnDuplicateUpdateContext;
import org.apache.shardingsphere.infra.binder.context.segment.table.TablesContext;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.sql.parser.statement.core.enums.SubqueryType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.ColumnAssignmentSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.OnDuplicateKeyColumnsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.combine.CombineSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.complex.CommonTableExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.ParameterMarkerSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.WithSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Insert SQL statement binding context.
 */
public final class InsertStatementBindingContext implements SQLStatementContext {
    
    private final InsertStatementBaseContext baseContext;
    
    @Getter
    private final List<InsertValueContext> insertValueContexts;
    
    @Getter
    private final InsertSelectContext insertSelectContext;
    
    @Getter
    private final OnDuplicateUpdateContext onDuplicateKeyUpdateValueContext;
    
    private final GeneratedKeyContext generatedKeyContext;
    
    public InsertStatementBindingContext(final InsertStatementBaseContext baseContext, final List<Object> params, final ShardingSphereMetaData metaData, final String currentDatabaseName) {
        this.baseContext = baseContext;
        AtomicInteger parametersOffset = new AtomicInteger(getInitialParametersOffset());
        insertValueContexts = getInsertValueContexts(params, parametersOffset, baseContext.getValueExpressions());
        insertSelectContext = getInsertSelectContext(params, parametersOffset, metaData, currentDatabaseName).orElse(null);
        parametersOffset.addAndGet(getOnConflictTargetParameterCount());
        onDuplicateKeyUpdateValueContext = getOnDuplicateKeyUpdateValueContext(params, parametersOffset).orElse(null);
        generatedKeyContext = new GeneratedKeyContextEngine(baseContext.getSqlStatement(), baseContext.getSchema())
                .createGenerateKeyContext(baseContext.getInsertColumnNamesAndIndexes(), insertValueContexts, params).orElse(null);
    }
    
    private int getInitialParametersOffset() {
        Collection<ParameterMarkerSegment> markers = baseContext.getSqlStatement().getParameterMarkers();
        if (markers.isEmpty()) {
            return 0;
        }
        int result = Integer.MAX_VALUE;
        for (ParameterMarkerSegment each : markers) {
            result = Math.min(result, each.getParameterIndex());
        }
        int valuesStartIndex;
        if (!baseContext.getSqlStatement().getValues().isEmpty()) {
            valuesStartIndex = baseContext.getSqlStatement().getValues().iterator().next().getStartIndex();
        } else if (!baseContext.getSqlStatement().getInsertSelect().isPresent() && !baseContext.getSqlStatement().getSetAssignment().isPresent()
                && baseContext.getSqlStatement().getTable().isPresent()) {
            valuesStartIndex = baseContext.getSqlStatement().getTable().get().getStartIndex();
        } else {
            return result;
        }
        int valuesParametersOffset = Integer.MAX_VALUE;
        for (ParameterMarkerSegment each : markers) {
            if (each.getStartIndex() < valuesStartIndex) {
                result = Math.max(result, each.getParameterIndex() + 1);
            } else {
                valuesParametersOffset = Math.min(valuesParametersOffset, each.getParameterIndex());
            }
        }
        return Integer.MAX_VALUE == valuesParametersOffset ? result : valuesParametersOffset;
    }
    
    private List<InsertValueContext> getInsertValueContexts(final List<Object> params, final AtomicInteger paramsOffset, final List<List<ExpressionSegment>> valueExpressions) {
        List<InsertValueContext> result = new ArrayList<>(valueExpressions.size());
        for (Collection<ExpressionSegment> each : valueExpressions) {
            InsertValueContext insertValueContext = new InsertValueContext(each, params, paramsOffset.get());
            result.add(insertValueContext);
            paramsOffset.addAndGet(insertValueContext.getParameterCount());
        }
        return result;
    }
    
    private Optional<InsertSelectContext> getInsertSelectContext(final List<Object> params, final AtomicInteger paramsOffset,
                                                                 final ShardingSphereMetaData metaData, final String currentDatabaseName) {
        if (!baseContext.getSqlStatement().getInsertSelect().isPresent()) {
            return Optional.empty();
        }
        SubquerySegment insertSelectSegment = baseContext.getSqlStatement().getInsertSelect().get();
        SelectStatementContext selectStatementContext = new SelectStatementContext(createInsertSelectStatement(insertSelectSegment), metaData, currentDatabaseName, Collections.emptyList());
        selectStatementContext.bindParameters(params);
        selectStatementContext.setSubqueryType(SubqueryType.INSERT_SELECT);
        setCombineSelectSubqueryType(selectStatementContext);
        setProjectionSelectSubqueryType(selectStatementContext);
        InsertSelectContext insertSelectContext = new InsertSelectContext(selectStatementContext, params, paramsOffset.get());
        paramsOffset.addAndGet(insertSelectContext.getSelectStatementContext().getSqlStatement().getParameterCount());
        return Optional.of(insertSelectContext);
    }
    
    private SelectStatement createInsertSelectStatement(final SubquerySegment insertSelectSegment) {
        SelectStatement selectStatement = insertSelectSegment.getSelect();
        Optional<WithSegment> withSegment = baseContext.getSqlStatement().getWith();
        if (!withSegment.isPresent()) {
            return selectStatement;
        }
        SelectStatement result = SelectStatement.builder().databaseType(selectStatement.getDatabaseType()).projections(selectStatement.getProjections())
                .from(selectStatement.getFrom().orElse(null)).where(selectStatement.getWhere().orElse(null)).hierarchicalQuery(selectStatement.getHierarchicalQuery().orElse(null))
                .groupBy(selectStatement.getGroupBy().orElse(null)).having(selectStatement.getHaving().orElse(null)).orderBy(selectStatement.getOrderBy().orElse(null))
                .combine(selectStatement.getCombine().orElse(null)).with(createInsertSelectWithSegment(withSegment.get(), selectStatement))
                .subqueryType(selectStatement.getSubqueryType().orElse(null))
                .limit(selectStatement.getLimit().orElse(null)).lock(selectStatement.getLock().orElse(null)).window(selectStatement.getWindow().orElse(null))
                .into(selectStatement.getInto().orElse(null)).model(selectStatement.getModel().orElse(null)).outfile(selectStatement.getOutfile().orElse(null))
                .withTableHint(selectStatement.getWithTableHint().orElse(null)).values(selectStatement.getValues()).build();
        result.addParameterMarkers(getInsertSelectParameterMarkers(insertSelectSegment, withSegment.get()));
        result.getVariableNames().addAll(selectStatement.getVariableNames());
        result.getComments().addAll(selectStatement.getComments());
        return result;
    }
    
    private WithSegment createInsertSelectWithSegment(final WithSegment outerWith, final SelectStatement selectStatement) {
        if (!selectStatement.getWith().isPresent()) {
            return outerWith;
        }
        WithSegment innerWith = selectStatement.getWith().get();
        Collection<CommonTableExpressionSegment> commonTableExpressions = new LinkedList<>(outerWith.getCommonTableExpressions());
        commonTableExpressions.addAll(innerWith.getCommonTableExpressions());
        return new WithSegment(outerWith.getStartIndex(), innerWith.getStopIndex(), commonTableExpressions, outerWith.isRecursive() || innerWith.isRecursive());
    }
    
    private Collection<ParameterMarkerSegment> getInsertSelectParameterMarkers(final SubquerySegment insertSelectSegment, final WithSegment withSegment) {
        Collection<ParameterMarkerSegment> result = new LinkedList<>();
        for (ParameterMarkerSegment each : baseContext.getSqlStatement().getParameterMarkers()) {
            if (withSegment.getStartIndex() <= each.getStartIndex() && each.getStopIndex() <= withSegment.getStopIndex()
                    || insertSelectSegment.getStartIndex() <= each.getStartIndex() && each.getStopIndex() <= insertSelectSegment.getStopIndex()) {
                result.add(each);
            }
        }
        return result;
    }
    
    private void setCombineSelectSubqueryType(final SelectStatementContext selectStatementContext) {
        if (selectStatementContext.getSqlStatement().getCombine().isPresent()) {
            CombineSegment combineSegment = selectStatementContext.getSqlStatement().getCombine().get();
            Optional.ofNullable(selectStatementContext.getSubqueryContexts().get(combineSegment.getLeft().getStartIndex()))
                    .ifPresent(optional -> optional.setSubqueryType(SubqueryType.INSERT_SELECT));
            Optional.ofNullable(selectStatementContext.getSubqueryContexts().get(combineSegment.getRight().getStartIndex()))
                    .ifPresent(optional -> optional.setSubqueryType(SubqueryType.INSERT_SELECT));
        }
    }
    
    private void setProjectionSelectSubqueryType(final SelectStatementContext selectStatementContext) {
        for (Entry<Integer, SelectStatementContext> entry : selectStatementContext.getSubqueryContexts().entrySet()) {
            if (entry.getKey() >= selectStatementContext.getProjectionsContext().getStartIndex() && entry.getKey() <= selectStatementContext.getProjectionsContext().getStopIndex()) {
                entry.getValue().setSubqueryType(SubqueryType.INSERT_SELECT);
            }
        }
    }
    
    @HighFrequencyInvocation
    private int getOnConflictTargetParameterCount() {
        Optional<OnDuplicateKeyColumnsSegment> conflict = baseContext.getSqlStatement().getOnDuplicateKeyColumns();
        if (!conflict.isPresent()) {
            return 0;
        }
        int firstAssignmentStart = Integer.MAX_VALUE;
        for (ColumnAssignmentSegment each : conflict.get().getColumns()) {
            firstAssignmentStart = Math.min(firstAssignmentStart, each.getStartIndex());
        }
        int result = 0;
        for (ParameterMarkerSegment each : baseContext.getSqlStatement().getParameterMarkers()) {
            if (each.getStartIndex() >= conflict.get().getStartIndex() && each.getStopIndex() <= conflict.get().getStopIndex()
                    && each.getStartIndex() < firstAssignmentStart) {
                result++;
            }
        }
        return result;
    }
    
    private Optional<OnDuplicateUpdateContext> getOnDuplicateKeyUpdateValueContext(final List<Object> params, final AtomicInteger parametersOffset) {
        Optional<OnDuplicateKeyColumnsSegment> onDuplicateKeyColumnsSegment = baseContext.getSqlStatement().getOnDuplicateKeyColumns();
        if (!onDuplicateKeyColumnsSegment.isPresent()) {
            return Optional.empty();
        }
        Collection<ColumnAssignmentSegment> onDuplicateKeyColumns = onDuplicateKeyColumnsSegment.get().getColumns();
        OnDuplicateUpdateContext onDuplicateUpdateContext = new OnDuplicateUpdateContext(onDuplicateKeyColumns, params, parametersOffset.get(),
                baseContext.getSqlStatement().getParameterMarkers());
        parametersOffset.addAndGet(onDuplicateUpdateContext.getParameterCount());
        return Optional.of(onDuplicateUpdateContext);
    }
    
    /**
     * Get grouped parameters.
     *
     * @return grouped parameters
     */
    public List<List<Object>> getGroupedParameters() {
        List<List<Object>> result = new LinkedList<>();
        for (InsertValueContext each : insertValueContexts) {
            result.add(each.getParameters());
        }
        return result;
    }
    
    /**
     * Get on duplicate key update parameters.
     *
     * @return on duplicate key update parameters
     */
    @SuppressWarnings("CollectionWithoutInitialCapacity")
    public List<Object> getOnDuplicateKeyUpdateParameters() {
        return null == onDuplicateKeyUpdateValueContext ? new ArrayList<>() : onDuplicateKeyUpdateValueContext.getParameters();
    }
    
    /**
     * Get generated key context.
     *
     * @return generated key context
     */
    public Optional<GeneratedKeyContext> getGeneratedKeyContext() {
        return Optional.ofNullable(generatedKeyContext);
    }
    
    @Override
    public InsertStatement getSqlStatement() {
        return baseContext.getSqlStatement();
    }
    
    @Override
    public TablesContext getTablesContext() {
        return baseContext.getTablesContext();
    }
}
