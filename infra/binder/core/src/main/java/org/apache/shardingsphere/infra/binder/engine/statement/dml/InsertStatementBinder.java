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
import org.apache.shardingsphere.infra.binder.engine.segment.SegmentType;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.assign.AssignmentSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.column.InsertColumnsSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.expression.ExpressionSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.expression.type.SubquerySegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.context.TableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.type.SimpleTableSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.with.WithSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinder;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementCopyUtils;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.InsertValuesSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.SetAssignmentSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.InsertColumnsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.OnDuplicateKeyColumnsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.WithSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.InsertStatement;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Insert statement binder.
 */
public final class InsertStatementBinder implements SQLStatementBinder<InsertStatement> {
    
    @Override
    public InsertStatement bind(final InsertStatement sqlStatement, final SQLStatementBinderContext binderContext) {
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> outerTableBinderContexts = LinkedHashMultimap.create();
        WithSegment boundWith = sqlStatement.getWith().map(optional -> WithSegmentBinder.bind(optional, binderContext, outerTableBinderContexts)).orElse(null);
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts = LinkedHashMultimap.create();
        SimpleTableSegment boundTable = sqlStatement.getTable().map(optional -> SimpleTableSegmentBinder.bind(optional, binderContext, tableBinderContexts)).orElse(null);
        InsertColumnsSegment boundInsertColumns = sqlStatement.getInsertColumns().isPresent() && !sqlStatement.getInsertColumns().get().getColumns().isEmpty()
                ? InsertColumnsSegmentBinder.bind(sqlStatement.getInsertColumns().get(), binderContext, tableBinderContexts)
                : sqlStatement.getInsertColumns().orElse(null);
        SetAssignmentSegment boundSetAssignment = sqlStatement.getSetAssignment()
                .map(optional -> AssignmentSegmentBinder.bind(optional, binderContext, tableBinderContexts, outerTableBinderContexts)).orElse(null);
        Collection<InsertValuesSegment> boundValues = bindInsertValues(sqlStatement, binderContext, tableBinderContexts, outerTableBinderContexts);
        SubquerySegment boundInsertSelect = sqlStatement.getInsertSelect().map(optional -> SubquerySegmentBinder.bind(optional, binderContext, tableBinderContexts)).orElse(null);
        OnDuplicateKeyColumnsSegment boundOnDuplicateKeyColumns = sqlStatement.getOnDuplicateKeyColumns()
                .map(optional -> AssignmentSegmentBinder.bind(optional, binderContext, tableBinderContexts, outerTableBinderContexts)).orElse(null);
        InsertStatement result = copy(sqlStatement, boundWith, boundTable, boundInsertColumns, boundSetAssignment, boundInsertSelect, boundOnDuplicateKeyColumns, boundValues);
        if (!sqlStatement.getInsertColumns().isPresent() || sqlStatement.getInsertColumns().get().getColumns().isEmpty()) {
            tableBinderContexts.values().forEach(each -> result.getDerivedInsertColumns().addAll(getVisibleColumns(each.getProjectionSegments())));
        }
        return result;
    }
    
    private Collection<InsertValuesSegment> bindInsertValues(final InsertStatement sqlStatement, final SQLStatementBinderContext binderContext,
                                                             final Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts,
                                                             final Multimap<CaseInsensitiveString, TableSegmentBinderContext> outerTableBinderContexts) {
        Collection<InsertValuesSegment> result = new LinkedList<>();
        for (InsertValuesSegment each : sqlStatement.getValues()) {
            List<ExpressionSegment> boundValues = new LinkedList<>();
            for (ExpressionSegment value : each.getValues()) {
                boundValues.add(ExpressionSegmentBinder.bind(value, SegmentType.VALUES, binderContext, tableBinderContexts, outerTableBinderContexts));
            }
            result.add(new InsertValuesSegment(each.getStartIndex(), each.getStopIndex(), boundValues));
        }
        return result;
    }
    
    private InsertStatement copy(final InsertStatement sqlStatement, final WithSegment boundWith, final SimpleTableSegment boundTable,
                                 final InsertColumnsSegment boundInsertColumns, final SetAssignmentSegment boundSetAssignment, final SubquerySegment boundInsertSelect,
                                 final OnDuplicateKeyColumnsSegment boundOnDuplicateKeyColumns,
                                 final Collection<InsertValuesSegment> boundValues) {
        InsertStatement result = InsertStatement.builder().databaseType(sqlStatement.getDatabaseType()).table(boundTable).insertColumns(boundInsertColumns)
                .insertSelect(boundInsertSelect).setAssignment(boundSetAssignment).onDuplicateKeyColumns(boundOnDuplicateKeyColumns)
                .valueReference(sqlStatement.getValueReference().orElse(null)).returning(sqlStatement.getReturning().orElse(null))
                .output(sqlStatement.getOutput().orElse(null)).with(boundWith).multiTableInsertType(sqlStatement.getMultiTableInsertType().orElse(null))
                .multiTableInsertInto(sqlStatement.getMultiTableInsertInto().orElse(null)).multiTableConditionalInto(sqlStatement.getMultiTableConditionalInto().orElse(null))
                .where(sqlStatement.getWhere().orElse(null)).exec(sqlStatement.getExec().orElse(null)).withTableHint(sqlStatement.getWithTableHint().orElse(null))
                .rowSetFunction(sqlStatement.getRowSetFunction().orElse(null)).ignore(sqlStatement.isIgnore()).replace(sqlStatement.isReplace())
                .values(new LinkedList<>(boundValues)).build();
        SQLStatementCopyUtils.copyAttributes(sqlStatement, result);
        return result;
    }
    
    private Collection<ColumnSegment> getVisibleColumns(final Collection<ProjectionSegment> projectionSegments) {
        return projectionSegments.stream().filter(each -> each instanceof ColumnProjectionSegment
                && each.isVisible()).map(each -> ((ColumnProjectionSegment) each).getColumn()).collect(Collectors.toList());
    }
}
