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

import com.cedarsoftware.util.CaseInsensitiveMap.CaseInsensitiveString;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.binder.engine.segment.SegmentType;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.expression.type.ColumnSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.context.TableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.context.type.SimpleTableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.type.SubqueryTableSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.segment.util.SubqueryTableBindUtils;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.kernel.syntax.DifferenceInColumnCountOfSelectListAndColumnNameListException;
import org.apache.shardingsphere.infra.exception.kernel.syntax.DuplicateCommonTableExpressionAliasException;
import org.apache.shardingsphere.sql.parser.statement.core.enums.TableSourceType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.complex.CommonTableExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SubqueryTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

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
     * @param externalTableBinderContexts external table binder contexts
     * @param recursive recursive
     * @return bound common table expression segment
     */
    public static CommonTableExpressionSegment bind(final CommonTableExpressionSegment segment, final SQLStatementBinderContext binderContext,
                                                    final Multimap<CaseInsensitiveString, TableSegmentBinderContext> externalTableBinderContexts, final boolean recursive) {
        if (segment.getAliasName().isPresent()) {
            ShardingSpherePreconditions.checkState(!binderContext.getCommonTableExpressionsSegmentsUniqueAliases().contains(segment.getAliasName().get()),
                    () -> new DuplicateCommonTableExpressionAliasException(segment.getAliasName().get()));
            binderContext.getCommonTableExpressionsSegmentsUniqueAliases().add(segment.getAliasName().get());
        }
        if (recursive && segment.getAliasName().isPresent()) {
            binderContext.getExternalTableBinderContexts().put(new CaseInsensitiveString(segment.getAliasName().get()),
                    new SimpleTableSegmentBinderContext(segment.getColumns().stream().map(ColumnProjectionSegment::new).collect(Collectors.toList()), TableSourceType.TEMPORARY_TABLE));
        }
        SubqueryTableSegment subqueryTableSegment = new SubqueryTableSegment(segment.getStartIndex(), segment.getStopIndex(), segment.getSubquery());
        segment.getAliasSegment().ifPresent(subqueryTableSegment::setAlias);
        SubqueryTableSegment boundSubquerySegment =
                SubqueryTableSegmentBinder.bind(subqueryTableSegment, binderContext, LinkedHashMultimap.create(), binderContext.getExternalTableBinderContexts(), true);
        CommonTableExpressionSegment result = new CommonTableExpressionSegment(
                segment.getStartIndex(), segment.getStopIndex(), boundSubquerySegment.getAliasSegment().orElse(null), boundSubquerySegment.getSubquery());
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> currentTableBinderContexts =
                createCurrentTableBinderContexts(segment.getColumns(), binderContext, boundSubquerySegment.getSubquery().getSelect());
        segment.getColumns()
                .forEach(each -> result.getColumns().add(ColumnSegmentBinder.bind(each, SegmentType.DEFINITION_COLUMNS, binderContext, currentTableBinderContexts, LinkedHashMultimap.create())));
        putExternalTableBinderContext(segment, externalTableBinderContexts, recursive, currentTableBinderContexts);
        return result;
    }
    
    private static Multimap<CaseInsensitiveString, TableSegmentBinderContext> createCurrentTableBinderContexts(final Collection<ColumnSegment> definitionColumns,
                                                                                                               final SQLStatementBinderContext binderContext, final SelectStatement selectStatement) {
        Collection<ProjectionSegment> subqueryProjections = SubqueryTableBindUtils.createSubqueryProjections(
                selectStatement.getProjections().getProjections(), new IdentifierValue(""), binderContext.getSqlStatement().getDatabaseType(), TableSourceType.TEMPORARY_TABLE);
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> result = LinkedHashMultimap.create();
        Collection<ProjectionSegment> boundDefinitionColumns = createBoundDefinitionColumns(definitionColumns, subqueryProjections);
        Collection<ProjectionSegment> boundProjectionSegments = definitionColumns.isEmpty() ? subqueryProjections : new LinkedList<>(boundDefinitionColumns);
        SimpleTableSegmentBinderContext tableSegmentBinderContext = new SimpleTableSegmentBinderContext(boundProjectionSegments, TableSourceType.TEMPORARY_TABLE);
        tableSegmentBinderContext.setFromWithSegment(true);
        result.put(new CaseInsensitiveString(""), tableSegmentBinderContext);
        return result;
    }
    
    private static Collection<ProjectionSegment> createBoundDefinitionColumns(final Collection<ColumnSegment> definitionColumns, final Collection<ProjectionSegment> projectionSegments) {
        Collection<ProjectionSegment> result = new LinkedList<>();
        Collection<ColumnSegment> boundDefinitionColumns = bindDefinitionColumns(definitionColumns, new ArrayList<>(projectionSegments));
        boundDefinitionColumns.forEach(each -> result.add(new ColumnProjectionSegment(each)));
        return result;
    }
    
    private static Collection<ColumnSegment> bindDefinitionColumns(final Collection<ColumnSegment> definitionColumns, final List<ProjectionSegment> projectionSegments) {
        ShardingSpherePreconditions.checkState(definitionColumns.isEmpty() || definitionColumns.size() == projectionSegments.size(),
                DifferenceInColumnCountOfSelectListAndColumnNameListException::new);
        int index = 0;
        Collection<ColumnSegment> result = new LinkedList<>();
        for (ColumnSegment each : definitionColumns) {
            ColumnSegment boundColumnSegment = copy(each);
            ProjectionSegment projectionSegment = projectionSegments.get(index);
            if (projectionSegment instanceof ColumnProjectionSegment) {
                boundColumnSegment.setColumnBoundInfo(
                        ColumnSegmentBinder.createColumnSegmentBoundInfo(each, ((ColumnProjectionSegment) projectionSegment).getColumn(), TableSourceType.TEMPORARY_TABLE));
            }
            result.add(boundColumnSegment);
            index++;
        }
        return result;
    }
    
    private static ColumnSegment copy(final ColumnSegment segment) {
        ColumnSegment result = new ColumnSegment(segment.getStartIndex(), segment.getStopIndex(), segment.getIdentifier());
        result.setNestedObjectAttributes(segment.getNestedObjectAttributes());
        segment.getOwner().ifPresent(result::setOwner);
        result.setVariable(segment.isVariable());
        segment.getLeftParentheses().ifPresent(result::setLeftParentheses);
        segment.getRightParentheses().ifPresent(result::setRightParentheses);
        return result;
    }
    
    private static void putExternalTableBinderContext(final CommonTableExpressionSegment segment, final Multimap<CaseInsensitiveString, TableSegmentBinderContext> externalTableBinderContexts,
                                                      final boolean recursive, final Multimap<CaseInsensitiveString, TableSegmentBinderContext> currentTableBinderContexts) {
        if (!segment.getAliasName().isPresent()) {
            return;
        }
        if (recursive && segment.getAliasName().isPresent()) {
            externalTableBinderContexts.removeAll(new CaseInsensitiveString(segment.getAliasName().get()));
        }
        externalTableBinderContexts.putAll(new CaseInsensitiveString(segment.getAliasName().get()), currentTableBinderContexts.values());
    }
}
