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

package org.apache.shardingsphere.infra.binder.engine.segment.from.type;

import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.util.ProjectionUtils;
import org.apache.shardingsphere.infra.binder.engine.segment.from.context.type.SimpleTableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.engine.segment.from.context.TableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.engine.segment.parameter.ParameterMarkerExpressionSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.infra.binder.engine.statement.dml.SelectStatementBinder;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.database.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.AggregationProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ShorthandProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.AliasAvailable;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.AliasSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.bound.ColumnSegmentBoundInfo;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SubqueryTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;

/**
 * Subquery table segment binder.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SubqueryTableSegmentBinder {
    
    /**
     * Bind subquery table segment.
     *
     * @param segment join table segment
     * @param binderContext SQL statement binder context
     * @param tableBinderContexts table binder contexts
     * @param outerTableBinderContexts outer table binder contexts
     * @return bound subquery table segment
     */
    public static SubqueryTableSegment bind(final SubqueryTableSegment segment, final SQLStatementBinderContext binderContext,
                                            final Map<String, TableSegmentBinderContext> tableBinderContexts, final Map<String, TableSegmentBinderContext> outerTableBinderContexts) {
        fillPivotColumnNamesInBinderContext(segment, binderContext);
        SQLStatementBinderContext subqueryBinderContext = new SQLStatementBinderContext(segment.getSubquery().getSelect(), binderContext.getMetaData(), binderContext.getCurrentDatabaseName());
        subqueryBinderContext.getExternalTableBinderContexts().putAll(binderContext.getExternalTableBinderContexts());
        SelectStatement boundSubSelect = new SelectStatementBinder(outerTableBinderContexts).bind(segment.getSubquery().getSelect(), subqueryBinderContext);
        SubquerySegment boundSubquerySegment = new SubquerySegment(segment.getSubquery().getStartIndex(), segment.getSubquery().getStopIndex(), boundSubSelect, segment.getSubquery().getText());
        boundSubquerySegment.setSubqueryType(segment.getSubquery().getSubqueryType());
        IdentifierValue subqueryTableName = segment.getAliasSegment().map(AliasSegment::getIdentifier).orElseGet(() -> new IdentifierValue(""));
        bindParameterMarkerProjection(boundSubquerySegment, subqueryTableName);
        SubqueryTableSegment result = new SubqueryTableSegment(segment.getStartIndex(), segment.getStopIndex(), boundSubquerySegment);
        segment.getAliasSegment().ifPresent(result::setAlias);
        tableBinderContexts.put(subqueryTableName.getValue().toLowerCase(),
                new SimpleTableSegmentBinderContext(createSubqueryProjections(boundSubSelect.getProjections().getProjections(), subqueryTableName, binderContext.getDatabaseType())));
        return result;
    }
    
    private static void fillPivotColumnNamesInBinderContext(final SubqueryTableSegment segment, final SQLStatementBinderContext binderContext) {
        segment.getPivot().ifPresent(optional -> optional.getPivotColumns().forEach(each -> binderContext.getPivotColumnNames().add(each.getIdentifier().getValue().toLowerCase())));
    }
    
    private static void bindParameterMarkerProjection(final SubquerySegment boundSubquerySegment, final IdentifierValue subqueryTableName) {
        SelectStatement boundSelect = boundSubquerySegment.getSelect();
        Collection<ProjectionSegment> projections = new LinkedList<>(boundSelect.getProjections().getProjections());
        boundSelect.getProjections().getProjections().clear();
        for (ProjectionSegment each : projections) {
            if (!(each instanceof ParameterMarkerExpressionSegment)) {
                boundSelect.getProjections().getProjections().add(each);
                continue;
            }
            ParameterMarkerExpressionSegment parameterMarkerProjection = (ParameterMarkerExpressionSegment) each;
            // TODO add database and schema in ColumnSegmentBoundInfo
            boundSelect.getProjections().getProjections().add(ParameterMarkerExpressionSegmentBinder.bind(parameterMarkerProjection, Collections.singletonMap(parameterMarkerProjection,
                    new ColumnSegmentBoundInfo(new IdentifierValue(""), new IdentifierValue(""), subqueryTableName, parameterMarkerProjection.getAlias().orElseGet(() -> new IdentifierValue(""))))));
        }
    }
    
    private static Collection<ProjectionSegment> createSubqueryProjections(final Collection<ProjectionSegment> projections, final IdentifierValue subqueryTableName, final DatabaseType databaseType) {
        Collection<ProjectionSegment> result = new LinkedList<>();
        for (ProjectionSegment each : projections) {
            if (each instanceof ColumnProjectionSegment) {
                result.add(createColumnProjection((ColumnProjectionSegment) each, subqueryTableName));
            } else if (each instanceof ShorthandProjectionSegment) {
                result.addAll(createSubqueryProjections(((ShorthandProjectionSegment) each).getActualProjectionSegments(), subqueryTableName, databaseType));
            } else if (each instanceof ExpressionProjectionSegment) {
                result.add(createColumnProjection((ExpressionProjectionSegment) each, subqueryTableName, databaseType));
            } else if (each instanceof AggregationProjectionSegment) {
                result.add(createColumnProjection((AggregationProjectionSegment) each, subqueryTableName, databaseType));
            } else {
                result.add(each);
            }
        }
        return result;
    }
    
    private static ColumnProjectionSegment createColumnProjection(final ColumnProjectionSegment originalColumn, final IdentifierValue subqueryTableName) {
        ColumnSegment newColumnSegment = new ColumnSegment(0, 0, originalColumn.getAlias().orElseGet(() -> originalColumn.getColumn().getIdentifier()));
        if (!Strings.isNullOrEmpty(subqueryTableName.getValue())) {
            newColumnSegment.setOwner(new OwnerSegment(0, 0, subqueryTableName));
        }
        newColumnSegment.setColumnBoundInfo(
                new ColumnSegmentBoundInfo(originalColumn.getColumn().getColumnBoundInfo().getOriginalDatabase(), originalColumn.getColumn().getColumnBoundInfo().getOriginalSchema(),
                        originalColumn.getColumn().getColumnBoundInfo().getOriginalTable(), originalColumn.getColumn().getColumnBoundInfo().getOriginalColumn()));
        ColumnProjectionSegment result = new ColumnProjectionSegment(newColumnSegment);
        result.setVisible(originalColumn.isVisible());
        return result;
    }
    
    private static ColumnProjectionSegment createColumnProjection(final ExpressionSegment expressionSegment, final IdentifierValue subqueryTableName, final DatabaseType databaseType) {
        ColumnSegment newColumnSegment = new ColumnSegment(0, 0,
                new IdentifierValue(getColumnNameFromExpression(expressionSegment, databaseType), new DatabaseTypeRegistry(databaseType).getDialectDatabaseMetaData().getQuoteCharacter()));
        if (!Strings.isNullOrEmpty(subqueryTableName.getValue())) {
            newColumnSegment.setOwner(new OwnerSegment(0, 0, subqueryTableName));
        }
        ColumnProjectionSegment result = new ColumnProjectionSegment(newColumnSegment);
        result.setVisible(true);
        return result;
    }
    
    private static String getColumnNameFromExpression(final ExpressionSegment expressionSegment, final DatabaseType databaseType) {
        String result;
        if (expressionSegment instanceof AliasAvailable && ((AliasAvailable) expressionSegment).getAlias().isPresent()) {
            result = ProjectionUtils.getColumnLabelFromAlias(((AliasAvailable) expressionSegment).getAlias().get(), databaseType);
        } else {
            result = ProjectionUtils.getColumnNameFromExpression(expressionSegment.getText(), databaseType);
        }
        return result;
    }
}
