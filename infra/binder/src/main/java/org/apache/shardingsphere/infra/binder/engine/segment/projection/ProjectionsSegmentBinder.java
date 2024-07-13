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

package org.apache.shardingsphere.infra.binder.engine.segment.projection;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.binder.engine.segment.SegmentType;
import org.apache.shardingsphere.infra.binder.engine.segment.projection.type.ColumnProjectionSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.segment.projection.type.ShorthandProjectionSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.segment.expression.ExpressionSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.segment.from.context.TableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.engine.segment.projection.type.SubqueryProjectionSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ShorthandProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.SubqueryProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableSegment;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

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
                                          final Map<String, TableSegmentBinderContext> tableBinderContexts, final Map<String, TableSegmentBinderContext> outerTableBinderContexts) {
        ProjectionsSegment result = new ProjectionsSegment(segment.getStartIndex(), segment.getStopIndex());
        result.setDistinctRow(segment.isDistinctRow());
        result.getProjections().addAll(segment.getProjections().stream()
                .map(each -> bind(each, binderContext, boundTableSegment, tableBinderContexts, outerTableBinderContexts)).collect(Collectors.toList()));
        return result;
    }
    
    private static ProjectionSegment bind(final ProjectionSegment projectionSegment, final SQLStatementBinderContext binderContext, final TableSegment boundTableSegment,
                                          final Map<String, TableSegmentBinderContext> tableBinderContexts, final Map<String, TableSegmentBinderContext> outerTableBinderContexts) {
        if (projectionSegment instanceof ColumnProjectionSegment) {
            return ColumnProjectionSegmentBinder.bind((ColumnProjectionSegment) projectionSegment, binderContext, tableBinderContexts);
        }
        if (projectionSegment instanceof ShorthandProjectionSegment) {
            return ShorthandProjectionSegmentBinder.bind((ShorthandProjectionSegment) projectionSegment, boundTableSegment, tableBinderContexts);
        }
        if (projectionSegment instanceof SubqueryProjectionSegment) {
            Map<String, TableSegmentBinderContext> newOuterTableBinderContexts = new LinkedHashMap<>(outerTableBinderContexts.size() + tableBinderContexts.size(), 1F);
            newOuterTableBinderContexts.putAll(outerTableBinderContexts);
            newOuterTableBinderContexts.putAll(tableBinderContexts);
            return SubqueryProjectionSegmentBinder.bind((SubqueryProjectionSegment) projectionSegment, binderContext, newOuterTableBinderContexts);
        }
        if (projectionSegment instanceof ExpressionProjectionSegment) {
            ExpressionSegment boundExpressionSegment = ExpressionSegmentBinder.bind(
                    ((ExpressionProjectionSegment) projectionSegment).getExpr(), SegmentType.PROJECTION, binderContext, tableBinderContexts, outerTableBinderContexts);
            ExpressionProjectionSegment result = new ExpressionProjectionSegment(
                    projectionSegment.getStartIndex(), projectionSegment.getStopIndex(), ((ExpressionProjectionSegment) projectionSegment).getText(), boundExpressionSegment);
            result.setAlias(((ExpressionProjectionSegment) projectionSegment).getAliasSegment());
            return result;
        }
        // TODO support more ProjectionSegment bound
        return projectionSegment;
    }
}
