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
import org.apache.shardingsphere.database.connector.core.metadata.database.enums.QuoteCharacter;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.extractor.ProjectionIdentifierExtractEngine;
import org.apache.shardingsphere.infra.binder.engine.segment.SegmentType;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.expression.type.ColumnSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.context.TableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.context.type.SimpleTableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.type.SubqueryTableSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.segment.util.SubqueryTableBindUtils;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.infra.binder.engine.statement.dml.DeleteStatementBinder;
import org.apache.shardingsphere.infra.binder.engine.statement.dml.InsertStatementBinder;
import org.apache.shardingsphere.infra.binder.engine.statement.dml.SelectStatementBinder;
import org.apache.shardingsphere.infra.binder.engine.statement.dml.UpdateStatementBinder;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.generic.UnsupportedSQLOperationException;
import org.apache.shardingsphere.infra.exception.kernel.syntax.DifferenceInColumnCountOfSelectListAndColumnNameListException;
import org.apache.shardingsphere.infra.exception.kernel.syntax.DuplicateCommonTableExpressionAliasException;
import org.apache.shardingsphere.sql.parser.statement.core.enums.TableSourceType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.ReturningSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.complex.CommonTableExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ShorthandProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.SubqueryProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.AliasAvailable;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.bound.ColumnSegmentBoundInfo;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.bound.TableSegmentBoundInfo;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SubqueryTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.DMLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.UpdateStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
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
     * @param tableBinderContexts table binder contexts
     * @param recursive recursive
     * @return bound common table expression segment
     */
    public static CommonTableExpressionSegment bind(final CommonTableExpressionSegment segment, final SQLStatementBinderContext binderContext,
                                                    final Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts, final boolean recursive) {
        ShardingSpherePreconditions.checkState(null == segment.getDataModification() || segment.getDataModification() instanceof InsertStatement
                || segment.getDataModification() instanceof UpdateStatement || segment.getDataModification() instanceof DeleteStatement,
                () -> new UnsupportedSQLOperationException("Data-modifying common table expressions"));
        if (segment.getAliasName().isPresent()) {
            ShardingSpherePreconditions.checkState(!binderContext.getCommonTableExpressionsSegmentsUniqueAliases().contains(segment.getAliasName().get()),
                    () -> new DuplicateCommonTableExpressionAliasException(segment.getAliasName().get()));
            binderContext.getCommonTableExpressionsSegmentsUniqueAliases().add(segment.getAliasName().get());
        }
        if (recursive && null == segment.getDataModification() && segment.getAliasName().isPresent()) {
            SimpleTableSegmentBinderContext recursiveTableBinderContext =
                    new SimpleTableSegmentBinderContext(createRecursiveWithProjections(segment, binderContext), TableSourceType.TEMPORARY_TABLE);
            tableBinderContexts.put(CaseInsensitiveString.of(segment.getAliasName().get()), recursiveTableBinderContext);
            binderContext.getExternalTableBinderContexts().put(CaseInsensitiveString.of(segment.getAliasName().get()), recursiveTableBinderContext);
        }
        if (null != segment.getDataModification()) {
            return bindDataModificationCommonTableExpression(segment, binderContext, tableBinderContexts, recursive);
        }
        SubqueryTableSegment subqueryTableSegment = new SubqueryTableSegment(segment.getStartIndex(), segment.getStopIndex(), segment.getSubquery());
        segment.getAliasSegment().ifPresent(subqueryTableSegment::setAlias);
        SubqueryTableSegment boundSubquerySegment =
                SubqueryTableSegmentBinder.bind(subqueryTableSegment, binderContext, LinkedHashMultimap.create(), tableBinderContexts, true);
        CommonTableExpressionSegment result = new CommonTableExpressionSegment(segment.getStartIndex(), segment.getStopIndex(), boundSubquerySegment.getAliasSegment().orElse(null),
                boundSubquerySegment.getSubquery(), null);
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> currentTableBinderContexts =
                createCurrentTableBinderContexts(segment.getColumns(), binderContext,
                        getSubqueryProjections(boundSubquerySegment.getSubquery()), boundSubquerySegment.getSubquery().getStopIndex());
        segment.getColumns().forEach(each -> result.getColumns().add(ColumnSegmentBinder.bind(each,
                SegmentType.DEFINITION_COLUMNS, binderContext, currentTableBinderContexts, LinkedHashMultimap.create())));
        segment.getSearchColumns().forEach(each -> result.getSearchColumns().add(ColumnSegmentBinder.bind(
                each, SegmentType.DEFINITION_COLUMNS, binderContext, currentTableBinderContexts, LinkedHashMultimap.create())));
        segment.getCycleColumns().forEach(each -> result.getCycleColumns().add(ColumnSegmentBinder.bind(
                each, SegmentType.DEFINITION_COLUMNS, binderContext, currentTableBinderContexts, LinkedHashMultimap.create())));
        putTableBinderContext(segment, tableBinderContexts, recursive);
        putExternalTableBinderContext(segment, binderContext, recursive, currentTableBinderContexts);
        return result;
    }
    
    private static CommonTableExpressionSegment bindDataModificationCommonTableExpression(final CommonTableExpressionSegment segment,
                                                                                          final SQLStatementBinderContext binderContext,
                                                                                          final Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts,
                                                                                          final boolean recursive) {
        DMLStatement boundDataModificationStatement = bindDataModificationStatement(segment.getDataModification(), binderContext);
        CommonTableExpressionSegment result = new CommonTableExpressionSegment(segment.getStartIndex(), segment.getStopIndex(),
                segment.getAliasSegment().orElse(null), null, boundDataModificationStatement);
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> currentTableBinderContexts = createCurrentTableBinderContexts(segment.getColumns(), binderContext,
                createDataModificationProjections(segment, boundDataModificationStatement, binderContext), segment.getStopIndex());
        segment.getColumns().forEach(each -> result.getColumns().add(ColumnSegmentBinder.bind(each,
                SegmentType.DEFINITION_COLUMNS, binderContext, currentTableBinderContexts, LinkedHashMultimap.create())));
        segment.getSearchColumns().forEach(each -> result.getSearchColumns().add(ColumnSegmentBinder.bind(
                each, SegmentType.DEFINITION_COLUMNS, binderContext, currentTableBinderContexts, LinkedHashMultimap.create())));
        segment.getCycleColumns().forEach(each -> result.getCycleColumns().add(ColumnSegmentBinder.bind(
                each, SegmentType.DEFINITION_COLUMNS, binderContext, currentTableBinderContexts, LinkedHashMultimap.create())));
        putTableBinderContext(segment, tableBinderContexts, recursive);
        putExternalTableBinderContext(segment, binderContext, recursive, currentTableBinderContexts);
        return result;
    }
    
    private static DMLStatement bindDataModificationStatement(final DMLStatement sqlStatement, final SQLStatementBinderContext binderContext) {
        SQLStatementBinderContext dataModificationBinderContext = new SQLStatementBinderContext(
                binderContext.getMetaData(), binderContext.getCurrentDatabaseName(), binderContext.getHintValueContext(), sqlStatement, binderContext.getCurrentSchema());
        dataModificationBinderContext.getExternalTableBinderContexts().putAll(binderContext.getExternalTableBinderContexts());
        dataModificationBinderContext.getCommonTableExpressionsSegmentsUniqueAliases().addAll(binderContext.getCommonTableExpressionsSegmentsUniqueAliases());
        if (sqlStatement instanceof InsertStatement) {
            return new InsertStatementBinder().bind((InsertStatement) sqlStatement, dataModificationBinderContext);
        }
        if (sqlStatement instanceof UpdateStatement) {
            return new UpdateStatementBinder().bind((UpdateStatement) sqlStatement, dataModificationBinderContext);
        }
        return new DeleteStatementBinder().bind((DeleteStatement) sqlStatement, dataModificationBinderContext);
    }
    
    private static ProjectionsSegment createDataModificationProjections(final CommonTableExpressionSegment segment, final DMLStatement sqlStatement,
                                                                        final SQLStatementBinderContext binderContext) {
        Optional<ReturningSegment> returningSegment = findReturningSegment(sqlStatement);
        ProjectionsSegment result = returningSegment.map(optional -> new ProjectionsSegment(optional.getProjections().getStartIndex(), optional.getProjections().getStopIndex()))
                .orElseGet(() -> new ProjectionsSegment(segment.getStartIndex(), segment.getStopIndex()));
        ProjectionIdentifierExtractEngine extractEngine = new ProjectionIdentifierExtractEngine(binderContext.getSqlStatement().getDatabaseType());
        QuoteCharacter quoteCharacter = new DatabaseTypeRegistry(binderContext.getSqlStatement().getDatabaseType()).getDialectDatabaseMetaData().getQuoteCharacter();
        returningSegment.map(ReturningSegment::getProjections).ifPresent(returningProjections -> returningProjections.getProjections()
                .forEach(each -> appendDataModificationProjection(result, each, extractEngine, quoteCharacter)));
        return result;
    }
    
    private static Optional<ReturningSegment> findReturningSegment(final DMLStatement sqlStatement) {
        if (sqlStatement instanceof InsertStatement) {
            return ((InsertStatement) sqlStatement).getReturning();
        }
        if (sqlStatement instanceof UpdateStatement) {
            return ((UpdateStatement) sqlStatement).getReturning();
        }
        return ((DeleteStatement) sqlStatement).getReturning();
    }
    
    private static void appendDataModificationProjection(final ProjectionsSegment projections, final ProjectionSegment projectionSegment,
                                                         final ProjectionIdentifierExtractEngine extractEngine, final QuoteCharacter quoteCharacter) {
        if (projectionSegment instanceof ShorthandProjectionSegment) {
            ((ShorthandProjectionSegment) projectionSegment).getActualProjectionSegments().forEach(each -> appendDataModificationProjection(projections, each, extractEngine, quoteCharacter));
            return;
        }
        IdentifierValue columnName = new IdentifierValue(getDataModificationColumnLabel(projectionSegment, extractEngine), quoteCharacter);
        ColumnSegment columnSegment = new ColumnSegment(projectionSegment.getStartIndex(), projectionSegment.getStopIndex(), columnName);
        columnSegment.setColumnBoundInfo(new ColumnSegmentBoundInfo(new TableSegmentBoundInfo(new IdentifierValue(""), new IdentifierValue("")),
                new IdentifierValue(""), columnName, TableSourceType.TEMPORARY_TABLE));
        projections.getProjections().add(new ColumnProjectionSegment(columnSegment));
    }
    
    private static String getDataModificationColumnLabel(final ProjectionSegment projectionSegment, final ProjectionIdentifierExtractEngine extractEngine) {
        if (projectionSegment instanceof AliasAvailable && ((AliasAvailable) projectionSegment).getAlias().isPresent()) {
            return extractEngine.getIdentifierValue(((AliasAvailable) projectionSegment).getAlias().get());
        }
        if (projectionSegment instanceof ExpressionSegment) {
            return extractEngine.getColumnNameFromExpression((ExpressionSegment) projectionSegment);
        }
        if (projectionSegment instanceof SubqueryProjectionSegment) {
            return extractEngine.getColumnNameFromSubquery((SubqueryProjectionSegment) projectionSegment);
        }
        return projectionSegment instanceof ColumnProjectionSegment
                ? extractEngine.getIdentifierValue(((ColumnProjectionSegment) projectionSegment).getColumn().getIdentifier())
                : projectionSegment.getColumnLabel();
    }
    
    private static Multimap<CaseInsensitiveString, TableSegmentBinderContext> createCurrentTableBinderContexts(final Collection<ColumnSegment> columns,
                                                                                                               final SQLStatementBinderContext binderContext,
                                                                                                               final ProjectionsSegment projections, final int bodyStopIndex) {
        Collection<ProjectionSegment> subqueryProjections = SubqueryTableBindUtils.createSubqueryProjections(
                projections.getProjections(), new IdentifierValue(""), binderContext.getSqlStatement().getDatabaseType(), TableSourceType.TEMPORARY_TABLE);
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> result = LinkedHashMultimap.create();
        Collection<ColumnSegment> definitionColumnSegments = getDefinitionColumns(columns, bodyStopIndex);
        Collection<ProjectionSegment> boundDefinitionColumns = createBoundDefinitionColumns(definitionColumnSegments, subqueryProjections);
        Collection<ProjectionSegment> boundProjectionSegments = definitionColumnSegments.isEmpty() ? new LinkedList<>(subqueryProjections) : new LinkedList<>(boundDefinitionColumns);
        getGeneratedColumns(columns, bodyStopIndex).forEach(each -> boundProjectionSegments.add(createGeneratedColumnProjection(each)));
        SimpleTableSegmentBinderContext tableSegmentBinderContext = new SimpleTableSegmentBinderContext(boundProjectionSegments, TableSourceType.TEMPORARY_TABLE);
        tableSegmentBinderContext.setFromWithSegment(true);
        result.put(CaseInsensitiveString.of(""), tableSegmentBinderContext);
        return result;
    }
    
    private static ProjectionsSegment getSubqueryProjections(final SubquerySegment subquerySegment) {
        return subquerySegment.getSelect().getCombine().map(optional -> optional.getLeft().getSelect().getProjections()).orElse(subquerySegment.getSelect().getProjections());
    }
    
    private static Collection<ColumnSegment> getDefinitionColumns(final Collection<ColumnSegment> columns, final int bodyStopIndex) {
        return columns.stream().filter(each -> each.getStartIndex() <= bodyStopIndex).collect(Collectors.toList());
    }
    
    private static Collection<ColumnSegment> getGeneratedColumns(final Collection<ColumnSegment> columns, final int bodyStopIndex) {
        return columns.stream().filter(each -> each.getStartIndex() > bodyStopIndex).collect(Collectors.toList());
    }
    
    private static ProjectionSegment createGeneratedColumnProjection(final ColumnSegment columnSegment) {
        ColumnSegment result = copy(columnSegment);
        result.setColumnBoundInfo(ColumnSegmentBinder.createColumnSegmentBoundInfo(columnSegment, null, TableSourceType.TEMPORARY_TABLE));
        return new ColumnProjectionSegment(result);
    }
    
    private static Collection<ProjectionSegment> createRecursiveWithProjections(final CommonTableExpressionSegment segment, final SQLStatementBinderContext binderContext) {
        SelectStatement boundAnchorSelect = bindAnchorSelect(segment, binderContext);
        Collection<ProjectionSegment> subqueryProjections = SubqueryTableBindUtils.createSubqueryProjections(
                boundAnchorSelect.getProjections().getProjections(), new IdentifierValue(""), binderContext.getSqlStatement().getDatabaseType(), TableSourceType.TEMPORARY_TABLE);
        Collection<ColumnSegment> definitionColumns = getDefinitionColumns(segment.getColumns(), segment.getSubquery().getStopIndex());
        return definitionColumns.isEmpty() ? subqueryProjections : createBoundDefinitionColumns(definitionColumns, subqueryProjections);
    }
    
    private static SelectStatement bindAnchorSelect(final CommonTableExpressionSegment segment, final SQLStatementBinderContext binderContext) {
        return new SelectStatementBinder().bind(segment.getSubquery().getSelect().getCombine()
                .map(optional -> optional.getLeft().getSelect()).orElse(segment.getSubquery().getSelect()), binderContext);
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
    
    private static void putTableBinderContext(final CommonTableExpressionSegment segment, final Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts,
                                              final boolean recursive) {
        if (!segment.getAliasName().isPresent()) {
            return;
        }
        if (recursive) {
            tableBinderContexts.removeAll(CaseInsensitiveString.of(segment.getAliasName().get()));
        }
    }
    
    private static void putExternalTableBinderContext(final CommonTableExpressionSegment segment, final SQLStatementBinderContext binderContext,
                                                      final boolean recursive, final Multimap<CaseInsensitiveString, TableSegmentBinderContext> currentTableBinderContexts) {
        if (!segment.getAliasName().isPresent()) {
            return;
        }
        if (recursive) {
            binderContext.getExternalTableBinderContexts().removeAll(CaseInsensitiveString.of(segment.getAliasName().get()));
        }
        binderContext.getExternalTableBinderContexts().putAll(CaseInsensitiveString.of(segment.getAliasName().get()), currentTableBinderContexts.values());
    }
}
