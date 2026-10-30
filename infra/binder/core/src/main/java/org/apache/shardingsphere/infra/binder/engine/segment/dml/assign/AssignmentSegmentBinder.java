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

package org.apache.shardingsphere.infra.binder.engine.segment.dml.assign;

import com.cedarsoftware.util.CaseInsensitiveMap.CaseInsensitiveString;
import com.google.common.collect.Multimap;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.binder.engine.segment.SegmentType;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.expression.ExpressionSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.expression.type.ColumnSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.context.TableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.ColumnAssignmentSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.SetAssignmentSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.OnDuplicateKeyColumnsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;

import java.util.List;
import java.util.stream.Collectors;

/**
 *  Assignment segment binder.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AssignmentSegmentBinder {
    
    /**
     * Bind assignment segment.
     *
     * @param segment assignment segment
     * @param binderContext SQL statement binder context
     * @param tableBinderContexts table binder contexts
     * @param outerTableBinderContexts outer table binder contexts
     * @return bound assignment segment
     */
    public static SetAssignmentSegment bind(final SetAssignmentSegment segment, final SQLStatementBinderContext binderContext,
                                            final Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts,
                                            final Multimap<CaseInsensitiveString, TableSegmentBinderContext> outerTableBinderContexts) {
        return bind(segment, binderContext, tableBinderContexts, tableBinderContexts, outerTableBinderContexts);
    }
    
    /**
     * Bind assignment columns against the target table and values against all visible tables.
     *
     * @param segment assignment segment
     * @param binderContext SQL statement binder context
     * @param assignmentColumnTableBinderContexts target table binder contexts
     * @param assignmentValueTableBinderContexts visible table binder contexts
     * @param outerTableBinderContexts outer table binder contexts
     * @return bound assignment segment
     */
    public static SetAssignmentSegment bind(final SetAssignmentSegment segment, final SQLStatementBinderContext binderContext,
                                            final Multimap<CaseInsensitiveString, TableSegmentBinderContext> assignmentColumnTableBinderContexts,
                                            final Multimap<CaseInsensitiveString, TableSegmentBinderContext> assignmentValueTableBinderContexts,
                                            final Multimap<CaseInsensitiveString, TableSegmentBinderContext> outerTableBinderContexts) {
        return new SetAssignmentSegment(segment.getStartIndex(), segment.getStopIndex(), segment.getAssignments().stream()
                .map(each -> bindColumnAssignmentSegment(each, binderContext, assignmentColumnTableBinderContexts, assignmentValueTableBinderContexts,
                        outerTableBinderContexts))
                .collect(Collectors.toList()));
    }
    
    /**
     * Bind on duplicate key columns segment.
     *
     * @param segment on duplicate key columns segment
     * @param binderContext SQL statement binder context
     * @param tableBinderContexts table binder contexts
     * @param outerTableBinderContexts outer table binder contexts
     * @return bound on duplicate key columns segment
     */
    public static OnDuplicateKeyColumnsSegment bind(final OnDuplicateKeyColumnsSegment segment, final SQLStatementBinderContext binderContext,
                                                    final Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts,
                                                    final Multimap<CaseInsensitiveString, TableSegmentBinderContext> outerTableBinderContexts) {
        boolean excludedTableAvailable = binderContext.isExcludedTableAvailable();
        binderContext.setExcludedTableAvailable(true);
        try {
            return new OnDuplicateKeyColumnsSegment(segment.getStartIndex(), segment.getStopIndex(), segment.getColumns().stream()
                    .map(each -> bindColumnAssignmentSegment(each, binderContext, tableBinderContexts, tableBinderContexts, outerTableBinderContexts)).collect(Collectors.toList()),
                    segment.getConflictColumns(), segment.getConflictWhereColumns());
        } finally {
            binderContext.setExcludedTableAvailable(excludedTableAvailable);
        }
    }
    
    private static ColumnAssignmentSegment bindColumnAssignmentSegment(final ColumnAssignmentSegment columnAssignmentSegment, final SQLStatementBinderContext binderContext,
                                                                       final Multimap<CaseInsensitiveString, TableSegmentBinderContext> assignmentColumnTableBinderContexts,
                                                                       final Multimap<CaseInsensitiveString, TableSegmentBinderContext> assignmentValueTableBinderContexts,
                                                                       final Multimap<CaseInsensitiveString, TableSegmentBinderContext> outerTableBinderContexts) {
        List<ColumnSegment> boundColumns = columnAssignmentSegment.getColumns().stream()
                .map(each -> ColumnSegmentBinder.bind(each, SegmentType.SET_ASSIGNMENT_COLUMNS, binderContext, assignmentColumnTableBinderContexts, outerTableBinderContexts))
                .collect(Collectors.toList());
        ExpressionSegment boundValue = ExpressionSegmentBinder.bind(columnAssignmentSegment.getValue(), SegmentType.SET_ASSIGNMENT, binderContext,
                assignmentValueTableBinderContexts, outerTableBinderContexts);
        return new ColumnAssignmentSegment(columnAssignmentSegment.getStartIndex(), columnAssignmentSegment.getStopIndex(), boundColumns, boundValue);
    }
}
