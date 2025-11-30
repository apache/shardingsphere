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
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.InsertStatement;

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
        AtomicInteger parametersOffset = new AtomicInteger(0);
        insertValueContexts = getInsertValueContexts(params, parametersOffset, baseContext.getValueExpressions());
        insertSelectContext = getInsertSelectContext(params, parametersOffset, metaData, currentDatabaseName).orElse(null);
        onDuplicateKeyUpdateValueContext = getOnDuplicateKeyUpdateValueContext(params, parametersOffset).orElse(null);
        generatedKeyContext = new GeneratedKeyContextEngine(baseContext.getSqlStatement(), baseContext.getSchema())
                .createGenerateKeyContext(baseContext.getInsertColumnNamesAndIndexes(), insertValueContexts, params).orElse(null);
    }
    
    private List<InsertValueContext> getInsertValueContexts(final List<Object> params, final AtomicInteger paramsOffset, final List<List<ExpressionSegment>> valueExpressions) {
        List<InsertValueContext> result = new LinkedList<>();
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
        SelectStatementContext selectStatementContext = new SelectStatementContext(insertSelectSegment.getSelect(), metaData, currentDatabaseName, Collections.emptyList());
        selectStatementContext.bindParameters(params);
        selectStatementContext.setSubqueryType(SubqueryType.INSERT_SELECT);
        setCombineSelectSubqueryType(selectStatementContext);
        setProjectionSelectSubqueryType(selectStatementContext);
        InsertSelectContext insertSelectContext = new InsertSelectContext(selectStatementContext, params, paramsOffset.get());
        paramsOffset.addAndGet(insertSelectContext.getSelectStatementContext().getSqlStatement().getParameterCount());
        return Optional.of(insertSelectContext);
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
    
    private Optional<OnDuplicateUpdateContext> getOnDuplicateKeyUpdateValueContext(final List<Object> params, final AtomicInteger parametersOffset) {
        Optional<OnDuplicateKeyColumnsSegment> onDuplicateKeyColumnsSegment = baseContext.getSqlStatement().getOnDuplicateKeyColumns();
        if (!onDuplicateKeyColumnsSegment.isPresent()) {
            return Optional.empty();
        }
        Collection<ColumnAssignmentSegment> onDuplicateKeyColumns = onDuplicateKeyColumnsSegment.get().getColumns();
        OnDuplicateUpdateContext onDuplicateUpdateContext = new OnDuplicateUpdateContext(onDuplicateKeyColumns, params, parametersOffset.get());
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
