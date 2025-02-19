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
import com.google.common.collect.Multimap;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.context.TableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.context.type.SimpleTableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.complex.CommonTableExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ShorthandProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.WithSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.bound.ColumnSegmentBoundInfo;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.bound.TableSegmentBoundInfo;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;

/**
 * With segment binder.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class WithSegmentBinder {
    
    /**
     * Bind with segment.
     *
     * @param segment with segment
     * @param binderContext SQL statement binder context
     * @param externalTableBinderContexts external table binder contexts
     * @return bound with segment
     */
    public static WithSegment bind(final WithSegment segment, final SQLStatementBinderContext binderContext,
                                   final Multimap<CaseInsensitiveString, TableSegmentBinderContext> externalTableBinderContexts) {
        Collection<CommonTableExpressionSegment> boundCommonTableExpressions = new LinkedList<>();
        for (CommonTableExpressionSegment each : segment.getCommonTableExpressions()) {
            CommonTableExpressionSegment boundCommonTableExpression = CommonTableExpressionSegmentBinder.bind(each, binderContext, segment.isRecursive());
            boundCommonTableExpressions.add(boundCommonTableExpression);
            if (segment.isRecursive() && each.getAliasName().isPresent()) {
                externalTableBinderContexts.removeAll(new CaseInsensitiveString(each.getAliasName().get()));
            }
            bindWithColumns(each.getColumns(), boundCommonTableExpression);
            each.getAliasName().ifPresent(optional -> externalTableBinderContexts.put(new CaseInsensitiveString(optional), createWithTableBinderContext(boundCommonTableExpression)));
        }
        return new WithSegment(segment.getStartIndex(), segment.getStopIndex(), boundCommonTableExpressions);
    }
    
    private static SimpleTableSegmentBinderContext createWithTableBinderContext(final CommonTableExpressionSegment commonTableExpressionSegment) {
        return new SimpleTableSegmentBinderContext(commonTableExpressionSegment.getSubquery().getSelect().getProjections().getProjections());
    }
    
    private static void bindWithColumns(final Collection<ColumnSegment> columns, final CommonTableExpressionSegment boundCommonTableExpression) {
        if (columns.isEmpty()) {
            return;
        }
        Map<String, ColumnProjectionSegment> columnProjections = extractWithSubqueryColumnProjections(boundCommonTableExpression);
        columns.forEach(each -> {
            ColumnProjectionSegment projectionSegment = columnProjections.get(each.getIdentifier().getValue());
            if (null != projectionSegment) {
                each.setColumnBoundInfo(createColumnSegmentBoundInfo(each, projectionSegment.getColumn()));
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
    }
    
    private static String getColumnName(final ColumnProjectionSegment columnProjection) {
        return columnProjection.getAliasName().orElse(columnProjection.getColumn().getIdentifier().getValue());
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
