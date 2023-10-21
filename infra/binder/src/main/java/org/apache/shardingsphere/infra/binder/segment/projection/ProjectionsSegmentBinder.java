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

package org.apache.shardingsphere.infra.binder.segment.projection;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.binder.enums.SegmentType;
import org.apache.shardingsphere.infra.binder.segment.expression.ExpressionSegmentBinder;
import org.apache.shardingsphere.infra.binder.segment.from.TableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.segment.projection.impl.ColumnProjectionSegmentBinder;
import org.apache.shardingsphere.infra.binder.segment.projection.impl.ShorthandProjectionSegmentBinder;
import org.apache.shardingsphere.infra.binder.segment.projection.impl.SubqueryProjectionSegmentBinder;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ShorthandProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.SubqueryProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableSegment;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Projections segment binder.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ProjectionsSegmentBinder {
    
    /**
     * Bind projections segment with metadata.
     *
     * @param segment table segment
     * @param statementBinderContext statement binder context
     * @param boundedTableSegment bounded table segment
     * @param tableBinderContexts table binder contexts
     * @param outerTableBinderContexts outer table binder contexts
     * @return bounded projections segment
     */
    public static ProjectionsSegment bind(final ProjectionsSegment segment, final SQLStatementBinderContext statementBinderContext, final TableSegment boundedTableSegment,
                                          final Map<String, TableSegmentBinderContext> tableBinderContexts, final Map<String, TableSegmentBinderContext> outerTableBinderContexts) {
        ProjectionsSegment result = new ProjectionsSegment(segment.getStartIndex(), segment.getStopIndex());
        result.setDistinctRow(segment.isDistinctRow());
        segment.getProjections().forEach(each -> result.getProjections().add(bind(each, statementBinderContext, boundedTableSegment, tableBinderContexts, outerTableBinderContexts)));
        return result;
    }
    
    private static ProjectionSegment bind(final ProjectionSegment projectionSegment, final SQLStatementBinderContext statementBinderContext,
                                          final TableSegment boundedTableSegment, final Map<String, TableSegmentBinderContext> tableBinderContexts,
                                          final Map<String, TableSegmentBinderContext> outerTableBinderContexts) {
        if (projectionSegment instanceof ColumnProjectionSegment) {
            return ColumnProjectionSegmentBinder.bind((ColumnProjectionSegment) projectionSegment, statementBinderContext, tableBinderContexts);
        }
        if (projectionSegment instanceof ShorthandProjectionSegment) {
            return ShorthandProjectionSegmentBinder.bind((ShorthandProjectionSegment) projectionSegment, boundedTableSegment, tableBinderContexts);
        }
        if (projectionSegment instanceof SubqueryProjectionSegment) {
            Map<String, TableSegmentBinderContext> newOuterTableBinderContexts = new LinkedHashMap<>();
            newOuterTableBinderContexts.putAll(outerTableBinderContexts);
            newOuterTableBinderContexts.putAll(tableBinderContexts);
            return SubqueryProjectionSegmentBinder.bind((SubqueryProjectionSegment) projectionSegment, statementBinderContext, newOuterTableBinderContexts);
        }
        if (projectionSegment instanceof ExpressionProjectionSegment) {
            ExpressionProjectionSegment result = new ExpressionProjectionSegment(projectionSegment.getStartIndex(), projectionSegment.getStopIndex(),
                    ((ExpressionProjectionSegment) projectionSegment).getText(), ExpressionSegmentBinder.bind(((ExpressionProjectionSegment) projectionSegment).getExpr(), SegmentType.PROJECTION,
                            statementBinderContext, tableBinderContexts, outerTableBinderContexts));
            result.setAlias(((ExpressionProjectionSegment) projectionSegment).getAliasSegment());
            return result;
        }
        // TODO support more ProjectionSegment bind
        return projectionSegment;
    }
}
