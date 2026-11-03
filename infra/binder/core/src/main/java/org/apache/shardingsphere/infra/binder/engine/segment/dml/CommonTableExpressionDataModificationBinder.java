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

package org.apache.shardingsphere.infra.binder.engine.segment.dml;

import com.cedarsoftware.util.CaseInsensitiveMap.CaseInsensitiveString;
import com.google.common.collect.Multimap;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.expression.type.ColumnSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.context.TableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.context.type.SimpleTableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.infra.binder.engine.type.DMLStatementBindEngine;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.kernel.syntax.DifferenceInColumnCountOfSelectListAndColumnNameListException;
import org.apache.shardingsphere.sql.parser.statement.core.enums.TableSourceType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.ReturningSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.complex.CommonTableExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ShorthandProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.AliasAvailable;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.DMLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.UpdateStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

/**
 * Bind a data-modifying common table expression and its returned relation.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CommonTableExpressionDataModificationBinder {
    
    /**
     * Bind an INSERT, UPDATE or DELETE statement and expose its RETURNING values in the parent scope.
     *
     * @param segment common table expression
     * @param context parent binder context
     * @param tableContexts preceding table contexts
     * @return bound common table expression
     */
    public static CommonTableExpressionSegment bind(final CommonTableExpressionSegment segment, final SQLStatementBinderContext context,
                                                    final Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableContexts) {
        SQLStatementBinderContext childContext = new SQLStatementBinderContext(context.getMetaData(), context.getCurrentDatabaseName(), context.getHintValueContext(),
                segment.getDataModification(), context.getCurrentSchema());
        childContext.getExternalTableBinderContexts().putAll(context.getExternalTableBinderContexts());
        childContext.getExternalTableBinderContexts().putAll(tableContexts);
        childContext.getCommonTableExpressionsSegmentsUniqueAliases().addAll(context.getCommonTableExpressionsSegmentsUniqueAliases());
        DMLStatement statement = new DMLStatementBindEngine().bind(segment.getDataModification(), childContext);
        context.getCommonTableExpressionsSegmentsUniqueAliases().addAll(childContext.getCommonTableExpressionsSegmentsUniqueAliases());
        CommonTableExpressionSegment result = new CommonTableExpressionSegment(segment.getStartIndex(), segment.getStopIndex(), segment.getAliasSegment().orElse(null), null, statement);
        Collection<ProjectionSegment> outputs = new ArrayList<>();
        getReturning(statement).ifPresent(returning -> collectOutputColumns(returning.getProjections().getProjections(), outputs));
        ShardingSpherePreconditions.checkState(segment.getColumns().isEmpty() || segment.getColumns().size() == outputs.size(), DifferenceInColumnCountOfSelectListAndColumnNameListException::new);
        if (!segment.getColumns().isEmpty()) {
            outputs.clear();
            for (ColumnSegment each : segment.getColumns()) {
                ColumnProjectionSegment projection = createOutputColumn(each.getStartIndex(), each.getStopIndex(), each.getIdentifier());
                outputs.add(projection);
                result.getColumns().add(projection.getColumn());
            }
        }
        SimpleTableSegmentBinderContext outputContext = new SimpleTableSegmentBinderContext(outputs, TableSourceType.TEMPORARY_TABLE);
        outputContext.setFromWithSegment(true);
        segment.getAliasName().ifPresent(alias -> {
            tableContexts.put(CaseInsensitiveString.of(alias), outputContext);
            context.getExternalTableBinderContexts().put(CaseInsensitiveString.of(alias), outputContext);
        });
        return result;
    }
    
    private static Optional<ReturningSegment> getReturning(final DMLStatement statement) {
        if (statement instanceof InsertStatement) {
            return ((InsertStatement) statement).getReturning();
        }
        if (statement instanceof UpdateStatement) {
            return ((UpdateStatement) statement).getReturning();
        }
        return ((DeleteStatement) statement).getReturning();
    }
    
    private static void collectOutputColumns(final Collection<ProjectionSegment> projections, final Collection<ProjectionSegment> outputColumns) {
        for (ProjectionSegment each : projections) {
            if (each instanceof ShorthandProjectionSegment) {
                collectOutputColumns(((ShorthandProjectionSegment) each).getActualProjectionSegments(), outputColumns);
            } else {
                IdentifierValue name = each instanceof AliasAvailable ? ((AliasAvailable) each).getAlias().orElseGet(() -> new IdentifierValue(each.getColumnLabel()))
                        : new IdentifierValue(each.getColumnLabel());
                outputColumns.add(createOutputColumn(each.getStartIndex(), each.getStopIndex(), name));
            }
        }
    }
    
    private static ColumnProjectionSegment createOutputColumn(final int startIndex, final int stopIndex, final IdentifierValue name) {
        ColumnSegment result = new ColumnSegment(startIndex, stopIndex, name);
        result.setColumnBoundInfo(ColumnSegmentBinder.createColumnSegmentBoundInfo(result, null, TableSourceType.TEMPORARY_TABLE));
        return new ColumnProjectionSegment(result);
    }
}
