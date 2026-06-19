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

package org.apache.shardingsphere.infra.binder.engine.statement.dml;

import com.cedarsoftware.util.CaseInsensitiveMap.CaseInsensitiveString;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.binder.engine.segment.SegmentType;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.combine.CombineSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.expression.ExpressionSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.expression.type.WindowItemSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.TableSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.context.TableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.context.type.SimpleTableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.lock.LockSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.order.GroupBySegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.order.OrderBySegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.predicate.HavingSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.predicate.HierarchicalQuerySegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.predicate.WhereSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.projection.ProjectionsSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.with.WithSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.segment.util.SubqueryTableBindUtils;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinder;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementCopyUtils;
import org.apache.shardingsphere.sql.parser.statement.core.enums.TableSourceType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.combine.CombineSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.GroupBySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.OrderBySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.HavingSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.HierarchicalQuerySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.LockSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.ModelColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.ModelSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.WindowItemSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.WindowSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.WithSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;

/**
 * Select statement binder.
 */
@RequiredArgsConstructor
public final class SelectStatementBinder implements SQLStatementBinder<SelectStatement> {
    
    private final Multimap<CaseInsensitiveString, TableSegmentBinderContext> outerTableBinderContexts;
    
    public SelectStatementBinder() {
        outerTableBinderContexts = LinkedHashMultimap.create();
    }
    
    @Override
    public SelectStatement bind(final SelectStatement sqlStatement, final SQLStatementBinderContext binderContext) {
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts = LinkedHashMultimap.create();
        WithSegment boundWith = sqlStatement.getWith().map(optional -> WithSegmentBinder.bind(optional, binderContext, tableBinderContexts)).orElse(null);
        Optional<TableSegment> boundFrom = sqlStatement.getFrom().map(optional -> TableSegmentBinder.bind(optional, binderContext, tableBinderContexts, outerTableBinderContexts));
        ModelSegment boundModel = sqlStatement.getModel().map(optional -> bindModelSegment(optional, binderContext, tableBinderContexts)).orElse(null);
        ProjectionsSegment boundProjections = bindProjections(sqlStatement, binderContext, boundFrom.orElse(null), tableBinderContexts);
        WhereSegment boundWhere = sqlStatement.getWhere().map(optional -> WhereSegmentBinder.bind(optional, binderContext, tableBinderContexts, outerTableBinderContexts)).orElse(null);
        HierarchicalQuerySegment boundHierarchicalQuery = sqlStatement.getHierarchicalQuery().map(
                optional -> HierarchicalQuerySegmentBinder.bind(optional, binderContext, tableBinderContexts, outerTableBinderContexts)).orElse(null);
        CombineSegment boundCombine = sqlStatement.getCombine().map(optional -> CombineSegmentBinder.bind(optional, binderContext, outerTableBinderContexts)).orElse(null);
        LockSegment boundLock = sqlStatement.getLock().map(optional -> LockSegmentBinder.bind(optional, binderContext, tableBinderContexts, outerTableBinderContexts)).orElse(null);
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> currentTableBinderContexts = createCurrentTableBinderContexts(sqlStatement, binderContext, boundProjections);
        GroupBySegment boundGroupBy =
                sqlStatement.getGroupBy().map(optional -> GroupBySegmentBinder.bind(optional, binderContext, currentTableBinderContexts, tableBinderContexts, outerTableBinderContexts)).orElse(null);
        OrderBySegment boundOrderBy =
                sqlStatement.getOrderBy().map(optional -> OrderBySegmentBinder.bind(optional, binderContext, currentTableBinderContexts, tableBinderContexts, outerTableBinderContexts)).orElse(null);
        HavingSegment boundHaving =
                sqlStatement.getHaving().map(optional -> HavingSegmentBinder.bind(optional, binderContext, currentTableBinderContexts, tableBinderContexts, outerTableBinderContexts)).orElse(null);
        WindowSegment boundWindow = sqlStatement.getWindow().map(optional -> bindWindowSegment(optional, binderContext, tableBinderContexts, outerTableBinderContexts)).orElse(null);
        return copy(sqlStatement, boundWith, boundFrom.orElse(null), boundProjections, boundWhere,
                boundHierarchicalQuery, boundCombine, boundLock, boundGroupBy, boundOrderBy, boundHaving, boundModel, boundWindow);
    }
    
    private ModelSegment bindModelSegment(final ModelSegment segment, final SQLStatementBinderContext binderContext,
                                          final Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts) {
        ModelSegment result = new ModelSegment(segment.getStartIndex(), segment.getStopIndex());
        result.getReferenceModelSelects().addAll(segment.getReferenceModelSelects());
        result.getOrderBySegments().addAll(segment.getOrderBySegments());
        bindModelColumns(segment.getPartitionColumns(), result.getPartitionColumns(), binderContext, tableBinderContexts);
        bindModelColumns(segment.getDimensionColumns(), result.getDimensionColumns(), binderContext, tableBinderContexts);
        bindModelColumns(segment.getMeasureColumns(), result.getMeasureColumns(), binderContext, tableBinderContexts);
        result.getCellAssignmentColumns().addAll(segment.getCellAssignmentColumns());
        result.getCellAssignmentSelects().addAll(segment.getCellAssignmentSelects());
        return result;
    }
    
    private void bindModelColumns(final Collection<ModelColumnSegment> segments, final Collection<ModelColumnSegment> boundSegments,
                                  final SQLStatementBinderContext binderContext, final Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts) {
        for (ModelColumnSegment each : segments) {
            ModelColumnSegment boundSegment = new ModelColumnSegment(each.getStartIndex(), each.getStopIndex(),
                    ExpressionSegmentBinder.bind(each.getExpression(), SegmentType.PROJECTION, binderContext, tableBinderContexts, outerTableBinderContexts));
            each.getAliasSegment().ifPresent(boundSegment::setAlias);
            boundSegments.add(boundSegment);
        }
    }
    
    private ProjectionsSegment bindProjections(final SelectStatement sqlStatement, final SQLStatementBinderContext binderContext, final TableSegment boundFrom,
                                               final Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts) {
        Collection<String> modelColumnNames = appendModelColumnNames(sqlStatement, binderContext);
        try {
            return ProjectionsSegmentBinder.bind(sqlStatement.getProjections(), binderContext, boundFrom, tableBinderContexts, outerTableBinderContexts);
        } finally {
            binderContext.getModelColumnNames().removeAll(modelColumnNames);
        }
    }
    
    private Collection<String> appendModelColumnNames(final SelectStatement sqlStatement, final SQLStatementBinderContext binderContext) {
        Collection<String> result = new LinkedList<>();
        if (sqlStatement.getModel().isPresent()) {
            ModelSegment modelSegment = sqlStatement.getModel().get();
            appendModelAliasColumnNames(modelSegment.getPartitionColumns(), result, binderContext);
            appendModelAliasColumnNames(modelSegment.getDimensionColumns(), result, binderContext);
            appendModelAliasColumnNames(modelSegment.getMeasureColumns(), result, binderContext);
            appendModelCellAssignmentColumnNames(modelSegment.getCellAssignmentColumns(), result, binderContext);
        }
        return result;
    }
    
    private void appendModelAliasColumnNames(final Collection<ModelColumnSegment> modelColumns, final Collection<String> appendedColumnNames,
                                             final SQLStatementBinderContext binderContext) {
        for (ModelColumnSegment each : modelColumns) {
            Optional<String> columnName = getModelColumnName(each);
            if (columnName.isPresent() && binderContext.getModelColumnNames().add(columnName.get())) {
                appendedColumnNames.add(columnName.get());
            }
        }
    }
    
    private void appendModelCellAssignmentColumnNames(final Collection<ColumnSegment> modelColumns, final Collection<String> appendedColumnNames,
                                                      final SQLStatementBinderContext binderContext) {
        for (ColumnSegment each : modelColumns) {
            String columnName = each.getIdentifier().getValue();
            if (binderContext.getModelColumnNames().add(columnName)) {
                appendedColumnNames.add(columnName);
            }
        }
    }
    
    private Optional<String> getModelColumnName(final ModelColumnSegment modelColumn) {
        if (modelColumn.getAlias().isPresent()) {
            return Optional.of(modelColumn.getAlias().get().getValue());
        }
        return modelColumn.getExpression() instanceof ColumnSegment ? Optional.of(((ColumnSegment) modelColumn.getExpression()).getIdentifier().getValue()) : Optional.empty();
    }
    
    private Multimap<CaseInsensitiveString, TableSegmentBinderContext> createCurrentTableBinderContexts(final SelectStatement sqlStatement, final SQLStatementBinderContext binderContext,
                                                                                                        final ProjectionsSegment boundProjections) {
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> result = LinkedHashMultimap.create();
        TableSourceType tableSourceType = sqlStatement.getCombine().isPresent() ? TableSourceType.TEMPORARY_TABLE : TableSourceType.MIXED_TABLE;
        Collection<ProjectionSegment> subqueryProjections = SubqueryTableBindUtils.createSubqueryProjections(
                boundProjections.getProjections(), new IdentifierValue(""), binderContext.getSqlStatement().getDatabaseType(), tableSourceType);
        result.put(CaseInsensitiveString.of(""), new SimpleTableSegmentBinderContext(subqueryProjections, tableSourceType));
        return result;
    }
    
    private SelectStatement copy(final SelectStatement sqlStatement, final WithSegment boundWith, final TableSegment boundFrom, final ProjectionsSegment boundProjections,
                                 final WhereSegment boundWhere, final HierarchicalQuerySegment boundHierarchicalQuery, final CombineSegment boundCombine,
                                 final LockSegment boundLock, final GroupBySegment boundGroupBy, final OrderBySegment boundOrderBy, final HavingSegment boundHaving,
                                 final ModelSegment boundModel, final WindowSegment boundWindow) {
        SelectStatement result = SelectStatement.builder().databaseType(sqlStatement.getDatabaseType()).with(boundWith).from(boundFrom).projections(boundProjections)
                .where(boundWhere).hierarchicalQuery(boundHierarchicalQuery).combine(boundCombine).lock(boundLock).groupBy(boundGroupBy).orderBy(boundOrderBy).having(boundHaving)
                .limit(sqlStatement.getLimit().orElse(null)).window(boundWindow).model(boundModel)
                .subqueryType(sqlStatement.getSubqueryType().orElse(null)).build();
        SQLStatementCopyUtils.copyAttributes(sqlStatement, result);
        return result;
    }
    
    private WindowSegment bindWindowSegment(final WindowSegment segment, final SQLStatementBinderContext binderContext,
                                            final Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts,
                                            final Multimap<CaseInsensitiveString, TableSegmentBinderContext> outerTableBinderContexts) {
        WindowSegment result = new WindowSegment(segment.getStartIndex(), segment.getStopIndex());
        for (WindowItemSegment each : segment.getItemSegments()) {
            result.getItemSegments().add(WindowItemSegmentBinder.bind(each, SegmentType.PROJECTION, binderContext, tableBinderContexts, outerTableBinderContexts));
        }
        return result;
    }
}
