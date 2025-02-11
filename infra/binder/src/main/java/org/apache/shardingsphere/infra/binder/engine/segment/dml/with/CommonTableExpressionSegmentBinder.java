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

package org.apache.shardingsphere.infra.binder.engine.segment.dml.with;

import com.cedarsoftware.util.CaseInsensitiveMap;
import com.cedarsoftware.util.CaseInsensitiveMap.CaseInsensitiveString;
import com.google.common.base.Strings;
import com.google.common.collect.LinkedHashMultimap;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.binder.engine.segment.SegmentType;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.expression.type.ColumnSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.context.type.SimpleTableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.type.SubqueryTableSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.segment.util.SubqueryTableBindUtils;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.kernel.syntax.DifferenceInColumnCountOfSelectListAndColumnNameListException;
import org.apache.shardingsphere.infra.exception.kernel.syntax.DuplicateCommonTableExpressionAliasException;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.complex.CommonTableExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ShorthandProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.bound.ColumnSegmentBoundInfo;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.bound.TableSegmentBoundInfo;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SubqueryTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;

/**
 * Common table expression segment binder.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CommonTableExpressionSegmentBinder {
    
    /**
     * Bind common table expression segment.
     *
     * @param segment common table expression segment
     * @param binderContext SQL statement binder context
     * @param recursive recursive
     * @return bound common table expression segment
     */
    public static CommonTableExpressionSegment bind(final CommonTableExpressionSegment segment, final SQLStatementBinderContext binderContext, final boolean recursive) {
        if (segment.getAliasName().isPresent()) {
            ShardingSpherePreconditions.checkState(!binderContext.getCommonTableExpressionsSegmentsUniqueAliases().contains(segment.getAliasName().get()),
                    () -> new DuplicateCommonTableExpressionAliasException(segment.getAliasName().get()));
            binderContext.getCommonTableExpressionsSegmentsUniqueAliases().add(segment.getAliasName().get());
        }
        if (recursive && segment.getAliasName().isPresent()) {
            binderContext.getExternalTableBinderContexts().put(new CaseInsensitiveString(segment.getAliasName().get()),
                    createWithTableBinderContext(segment, binderContext));
        }
        SubqueryTableSegment subqueryTableSegment = new SubqueryTableSegment(segment.getStartIndex(), segment.getStopIndex(), segment.getSubquery());
        subqueryTableSegment.setAlias(segment.getAliasSegment());
        SubqueryTableSegment boundSubquerySegment =
                SubqueryTableSegmentBinder.bind(subqueryTableSegment, binderContext, LinkedHashMultimap.create(), binderContext.getExternalTableBinderContexts());
        CommonTableExpressionSegment result = new CommonTableExpressionSegment(
                segment.getStartIndex(), segment.getStopIndex(), boundSubquerySegment.getAliasSegment().orElse(null), boundSubquerySegment.getSubquery());
        result.getColumns().addAll(segment.getColumns());
        if (result.getAliasName().isPresent()) {
            binderContext.getExternalTableBinderContexts().removeAll(new CaseInsensitiveString(result.getAliasName().get()));
            binderContext.getExternalTableBinderContexts().put(new CaseInsensitiveString(result.getAliasName().get()),
                    createWithTableBinderContext(result, binderContext));
        }
        result.getColumns().clear();
        segment.getColumns()
                .forEach(each -> result.getColumns().add(ColumnSegmentBinder.bind(each, SegmentType.DEFINITION_COLUMNS, binderContext, LinkedHashMultimap.create(), LinkedHashMultimap.create())));
        return result;
    }
    
    private static SimpleTableSegmentBinderContext createWithTableBinderContext(final CommonTableExpressionSegment commonTableExpressionSegment, final SQLStatementBinderContext binderContext) {
        if (commonTableExpressionSegment.getColumns().isEmpty()) {
            return new SimpleTableSegmentBinderContext(SubqueryTableBindUtils.createSubqueryProjections(commonTableExpressionSegment.getSubquery().getSelect().getProjections().getProjections(),
                    commonTableExpressionSegment.getAliasSegment().getIdentifier(), binderContext.getSqlStatement().getDatabaseType()));
        } else {
            Collection<ProjectionSegment> projectionSegments = new LinkedList<>();
            bindWithColumns(commonTableExpressionSegment.getColumns(), commonTableExpressionSegment);
            commonTableExpressionSegment.getColumns().forEach(each -> projectionSegments.add(new ColumnProjectionSegment(each)));
            return new SimpleTableSegmentBinderContext(projectionSegments);
        }
    }
    
    private static void bindWithColumns(final Collection<ColumnSegment> columns, final CommonTableExpressionSegment boundCommonTableExpression) {
        if (columns.isEmpty()) {
            return;
        }
        Map<String, ColumnProjectionSegment> columnProjections = extractWithSubqueryColumnProjections(boundCommonTableExpression);
        ShardingSpherePreconditions.checkState(columns.isEmpty() || columnProjections.size() == columns.size(),
                DifferenceInColumnCountOfSelectListAndColumnNameListException::new);
        Iterator<ColumnProjectionSegment> projectionSegmentIterator = columnProjections.values().iterator();
        columns.forEach(each -> {
            if (projectionSegmentIterator.hasNext()) {
                each.setColumnBoundInfo(createColumnSegmentBoundInfo(each, projectionSegmentIterator.next().getColumn()));
            }
        });
    }
    
    private static Map<String, ColumnProjectionSegment> extractWithSubqueryColumnProjections(final CommonTableExpressionSegment boundCommonTableExpression) {
        Map<String, ColumnProjectionSegment> result = new CaseInsensitiveMap<>();
        Collection<ProjectionSegment> projections = boundCommonTableExpression.getSubquery().getSelect().getProjections().getProjections();
        projections.forEach(each -> extractWithSubqueryColumnProjections(each, result));
        return result;
    }
    
    private static void extractWithSubqueryColumnProjections(final ProjectionSegment projectionSegment, final Map<String, ColumnProjectionSegment> result) {
        if (projectionSegment instanceof ColumnProjectionSegment) {
            result.put(getColumnName((ColumnProjectionSegment) projectionSegment), (ColumnProjectionSegment) projectionSegment);
        }
        if (projectionSegment instanceof ShorthandProjectionSegment) {
            ((ShorthandProjectionSegment) projectionSegment).getActualProjectionSegments().forEach(eachProjection -> {
                if (eachProjection instanceof ColumnProjectionSegment) {
                    result.put(getColumnName((ColumnProjectionSegment) eachProjection), (ColumnProjectionSegment) eachProjection);
                }
            });
        }
        if (projectionSegment instanceof ExpressionProjectionSegment) {
            result.put(getColumnName((ExpressionProjectionSegment) projectionSegment), getColumnProjectionSegment(projectionSegment));
        }
    }
    
    private static String getColumnName(final ColumnProjectionSegment columnProjection) {
        return columnProjection.getAliasName().orElse(columnProjection.getColumn().getIdentifier().getValue());
    }
    
    private static String getColumnName(final ExpressionProjectionSegment projectionSegment) {
        return projectionSegment.getAliasName().orElse(projectionSegment.getText());
    }
    
    private static ColumnProjectionSegment getColumnProjectionSegment(final ProjectionSegment projectionSegment) {
        return new ColumnProjectionSegment(
                new ColumnSegment(projectionSegment.getStartIndex(), projectionSegment.getStopIndex(), new IdentifierValue(getColumnName((ExpressionProjectionSegment) projectionSegment))));
    }
    
    private static ColumnSegmentBoundInfo createColumnSegmentBoundInfo(final ColumnSegment segment, final ColumnSegment inputColumnSegment) {
        IdentifierValue originalDatabase = null == inputColumnSegment ? null : inputColumnSegment.getColumnBoundInfo().getOriginalDatabase();
        IdentifierValue originalSchema = null == inputColumnSegment ? null : inputColumnSegment.getColumnBoundInfo().getOriginalSchema();
        IdentifierValue segmentOriginalTable = segment.getColumnBoundInfo().getOriginalTable();
        IdentifierValue originalTable = Strings.isNullOrEmpty(segmentOriginalTable.getValue())
                ? Optional.ofNullable(inputColumnSegment).map(optional -> optional.getColumnBoundInfo().getOriginalTable()).orElse(segmentOriginalTable)
                : segmentOriginalTable;
        IdentifierValue segmentOriginalColumn = segment.getColumnBoundInfo().getOriginalColumn();
        IdentifierValue originalColumn = Optional.ofNullable(inputColumnSegment).map(optional -> optional.getColumnBoundInfo().getOriginalColumn()).orElse(segmentOriginalColumn);
        return new ColumnSegmentBoundInfo(new TableSegmentBoundInfo(originalDatabase, originalSchema), originalTable, originalColumn);
    }
}
