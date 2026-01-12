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

package org.apache.shardingsphere.infra.binder.engine.segment.dml.projection;

import com.cedarsoftware.util.CaseInsensitiveMap.CaseInsensitiveString;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import java.util.LinkedList;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.binder.engine.segment.SegmentType;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.expression.ExpressionSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.context.TableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.context.type.SimpleTableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.projection.type.ColumnProjectionSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.projection.type.ShorthandProjectionSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.projection.type.SubqueryProjectionSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.segment.util.SubqueryTableBindUtils;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.infra.exception.kernel.metadata.ColumnNotFoundException;
import org.apache.shardingsphere.sql.parser.statement.core.enums.TableSourceType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.AggregationDistinctProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.AggregationProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ShorthandProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.SubqueryProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.FunctionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.complex.ComplexExpressionSegment;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Projections segment binder.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ProjectionsSegmentBinder {
    
    /**
     * Bind projections segment.
     *
     * @param segment table segment
     * @param binderContext statement binder context
     * @param boundTableSegment bound table segment
     * @param tableBinderContexts table binder contexts
     * @param outerTableBinderContexts outer table binder contexts
     * @return bound projections segment
     */
    public static ProjectionsSegment bind(final ProjectionsSegment segment, final SQLStatementBinderContext binderContext, final TableSegment boundTableSegment,
                                          final Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts,
                                          final Multimap<CaseInsensitiveString, TableSegmentBinderContext> outerTableBinderContexts) {
        ProjectionsSegment result = new ProjectionsSegment(segment.getStartIndex(), segment.getStopIndex());
        result.setDistinctRow(segment.isDistinctRow());
        for (ProjectionSegment each : segment.getProjections()) {
            Multimap<CaseInsensitiveString, TableSegmentBinderContext> currentTableBinderContexts = createCurrentTableBinderContexts(binderContext, result.getProjections());
            result.getProjections().add(bind(binderContext, boundTableSegment, currentTableBinderContexts, tableBinderContexts, outerTableBinderContexts, each));
        }
        return result;
    }
    
    private static ProjectionSegment bind(final SQLStatementBinderContext binderContext, final TableSegment boundTableSegment,
                                          final Multimap<CaseInsensitiveString, TableSegmentBinderContext> currentTableBinderContexts,
                                          final Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts,
                                          final Multimap<CaseInsensitiveString, TableSegmentBinderContext> outerTableBinderContexts,
                                          final ProjectionSegment projectionSegment) {
        try {
            return bind(projectionSegment, binderContext, boundTableSegment, tableBinderContexts, outerTableBinderContexts);
        } catch (final ColumnNotFoundException ignored) {
            return bind(projectionSegment, binderContext, boundTableSegment, currentTableBinderContexts, outerTableBinderContexts);
        }
    }
    
    private static ProjectionSegment bind(final ProjectionSegment projectionSegment, final SQLStatementBinderContext binderContext, final TableSegment boundTableSegment,
                                          final Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts,
                                          final Multimap<CaseInsensitiveString, TableSegmentBinderContext> outerTableBinderContexts) {
        if (projectionSegment instanceof ColumnProjectionSegment) {
            return ColumnProjectionSegmentBinder.bind((ColumnProjectionSegment) projectionSegment, binderContext, tableBinderContexts, outerTableBinderContexts);
        }
        if (projectionSegment instanceof ShorthandProjectionSegment) {
            return ShorthandProjectionSegmentBinder.bind((ShorthandProjectionSegment) projectionSegment, boundTableSegment, tableBinderContexts);
        }
        if (projectionSegment instanceof SubqueryProjectionSegment) {
            Multimap<CaseInsensitiveString, TableSegmentBinderContext> newOuterTableBinderContexts = LinkedHashMultimap.create();
            newOuterTableBinderContexts.putAll(outerTableBinderContexts);
            newOuterTableBinderContexts.putAll(tableBinderContexts);
            return SubqueryProjectionSegmentBinder.bind((SubqueryProjectionSegment) projectionSegment, binderContext, newOuterTableBinderContexts);
        }
        if (projectionSegment instanceof ExpressionProjectionSegment) {
            ExpressionSegment boundExpressionSegment = ExpressionSegmentBinder.bind(
                    ((ExpressionProjectionSegment) projectionSegment).getExpr(), SegmentType.PROJECTION, binderContext, tableBinderContexts, outerTableBinderContexts);
            ExpressionProjectionSegment result = new ExpressionProjectionSegment(
                    projectionSegment.getStartIndex(), projectionSegment.getStopIndex(), ((ExpressionProjectionSegment) projectionSegment).getText(), boundExpressionSegment);
            result.getAggregationProjectionSegments().addAll(extractAggregationProjectionSegments(boundExpressionSegment));
            ((ExpressionProjectionSegment) projectionSegment).getAliasSegment().ifPresent(result::setAlias);
            return result;
        }
        if (projectionSegment instanceof AggregationDistinctProjectionSegment) {
            return bindAggregationDistinctProjection((AggregationDistinctProjectionSegment) projectionSegment, binderContext, tableBinderContexts, outerTableBinderContexts);
        }
        if (projectionSegment instanceof AggregationProjectionSegment) {
            return bindAggregationProjection((AggregationProjectionSegment) projectionSegment, binderContext, tableBinderContexts, outerTableBinderContexts);
        }
        // TODO support more ProjectionSegment bound
        return projectionSegment;
    }
    
    private static AggregationDistinctProjectionSegment bindAggregationDistinctProjection(final AggregationDistinctProjectionSegment aggregationDistinctSegment,
                                                                                          final SQLStatementBinderContext binderContext,
                                                                                          final Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts,
                                                                                          final Multimap<CaseInsensitiveString, TableSegmentBinderContext> outerTableBinderContexts) {
        AggregationDistinctProjectionSegment result = new AggregationDistinctProjectionSegment(aggregationDistinctSegment.getStartIndex(), aggregationDistinctSegment.getStopIndex(),
                aggregationDistinctSegment.getType(), aggregationDistinctSegment.getExpression(), aggregationDistinctSegment.getDistinctInnerExpression(),
                aggregationDistinctSegment.getSeparator().orElse(null));
        aggregationDistinctSegment.getParameters()
                .forEach(each -> result.getParameters().add(ExpressionSegmentBinder.bind(each, SegmentType.PROJECTION, binderContext, tableBinderContexts, outerTableBinderContexts)));
        aggregationDistinctSegment.getAliasSegment().ifPresent(result::setAlias);
        return result;
    }
    
    private static AggregationProjectionSegment bindAggregationProjection(final AggregationProjectionSegment aggregationSegment, final SQLStatementBinderContext binderContext,
                                                                          final Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts,
                                                                          final Multimap<CaseInsensitiveString, TableSegmentBinderContext> outerTableBinderContexts) {
        AggregationProjectionSegment result =
                new AggregationProjectionSegment(aggregationSegment.getStartIndex(), aggregationSegment.getStopIndex(), aggregationSegment.getType(), aggregationSegment.getExpression(),
                        aggregationSegment.getSeparator().orElse(null));
        aggregationSegment.getParameters()
                .forEach(each -> result.getParameters().add(ExpressionSegmentBinder.bind(each, SegmentType.PROJECTION, binderContext, tableBinderContexts, outerTableBinderContexts)));
        aggregationSegment.getAliasSegment().ifPresent(result::setAlias);
        return result;
    }
    
    private static Multimap<CaseInsensitiveString, TableSegmentBinderContext> createCurrentTableBinderContexts(final SQLStatementBinderContext binderContext,
                                                                                                               final Collection<ProjectionSegment> projections) {
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> result = LinkedHashMultimap.create();
        Collection<ProjectionSegment> subqueryProjections = SubqueryTableBindUtils.createSubqueryProjections(
                projections, new IdentifierValue(""), binderContext.getSqlStatement().getDatabaseType(), TableSourceType.TEMPORARY_TABLE);
        result.put(new CaseInsensitiveString(""), new SimpleTableSegmentBinderContext(subqueryProjections, TableSourceType.TEMPORARY_TABLE));
        return result;
    }
    private static Collection<AggregationProjectionSegment> extractAggregationProjectionSegments(final ExpressionSegment expression) {
        Collection<AggregationProjectionSegment> result = new LinkedList<>();
        if (expression instanceof AggregationProjectionSegment) {
            result.add((AggregationProjectionSegment) expression);
        }
        if (expression instanceof FunctionSegment) {
            for (ExpressionSegment each : ((FunctionSegment) expression).getParameters()) {
                result.addAll(extractAggregationProjectionSegments(each));
            }
        }
        return result;
    }
}
