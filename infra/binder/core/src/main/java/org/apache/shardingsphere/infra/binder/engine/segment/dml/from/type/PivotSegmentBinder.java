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

package org.apache.shardingsphere.infra.binder.engine.segment.dml.from.type;

import com.cedarsoftware.util.CaseInsensitiveMap.CaseInsensitiveString;
import com.google.common.collect.Multimap;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.binder.engine.segment.SegmentType;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.expression.type.ColumnSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.context.TableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.PivotSegment;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Pivot segment binder.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PivotSegmentBinder {
    
    /**
     * Bind pivot segment.
     *
     * @param segment pivot segment
     * @param binderContext SQL statement binder context
     * @param tableBinderContexts table binder contexts
     * @param outerTableBinderContexts outer table binder contexts
     * @return bound pivot segment
     */
    public static PivotSegment bind(final PivotSegment segment, final SQLStatementBinderContext binderContext,
                                    final Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts,
                                    final Multimap<CaseInsensitiveString, TableSegmentBinderContext> outerTableBinderContexts) {
        PivotSegment result = segment.isUnPivot()
                ? new PivotSegment(segment.getStartIndex(), segment.getStopIndex(), copy(segment.getPivotForColumns()),
                        bind(segment.getPivotInColumns(), binderContext, tableBinderContexts, outerTableBinderContexts), true)
                : new PivotSegment(segment.getStartIndex(), segment.getStopIndex(),
                        bind(segment.getPivotForColumns(), binderContext, tableBinderContexts, outerTableBinderContexts), copy(segment.getPivotInColumns()));
        result.getPivotAggregationColumns().addAll(bind(segment.getPivotAggregationColumns(), binderContext, tableBinderContexts, outerTableBinderContexts));
        result.setXml(segment.isXml());
        if (null != segment.getUnpivotColumns()) {
            result.setUnpivotColumns(copy(segment.getUnpivotColumns()));
        }
        return result;
    }
    
    private static Collection<ColumnSegment> bind(final Collection<ColumnSegment> segments, final SQLStatementBinderContext binderContext,
                                                  final Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts,
                                                  final Multimap<CaseInsensitiveString, TableSegmentBinderContext> outerTableBinderContexts) {
        Collection<ColumnSegment> result = new LinkedList<>();
        for (ColumnSegment each : segments) {
            result.add(ColumnSegmentBinder.bind(each, SegmentType.PROJECTION, binderContext, tableBinderContexts, outerTableBinderContexts));
        }
        return result;
    }
    
    private static Collection<ColumnSegment> copy(final Collection<ColumnSegment> segments) {
        Collection<ColumnSegment> result = new LinkedList<>();
        for (ColumnSegment each : segments) {
            result.add(copy(each));
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
        result.setColumnBoundInfo(segment.getColumnBoundInfo());
        result.setOtherUsingColumnBoundInfo(segment.getOtherUsingColumnBoundInfo());
        return result;
    }
}
