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

package org.apache.shardingsphere.infra.binder.segment.assign;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.binder.enums.SegmentType;
import org.apache.shardingsphere.infra.binder.segment.expression.ExpressionSegmentBinder;
import org.apache.shardingsphere.infra.binder.segment.expression.impl.ColumnSegmentBinder;
import org.apache.shardingsphere.infra.binder.segment.from.TableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.ColumnAssignmentSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.SetAssignmentSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *  Assignment segment binder.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AssignmentSegmentBinder {
    
    /**
     * Bind assignment segment.
     *
     * @param segment assignment segment
     * @param statementBinderContext statement binder context
     * @param tableBinderContexts table binder contexts
     * @param outerTableBinderContexts outer table binder contexts
     * @return bound assignment segment
     */
    public static SetAssignmentSegment bind(final SetAssignmentSegment segment, final SQLStatementBinderContext statementBinderContext,
                                            final Map<String, TableSegmentBinderContext> tableBinderContexts, final Map<String, TableSegmentBinderContext> outerTableBinderContexts) {
        Collection<ColumnAssignmentSegment> assignments = new LinkedList<>();
        for (ColumnAssignmentSegment each : segment.getAssignments()) {
            assignments.add(new ColumnAssignmentSegment(each.getStartIndex(), each.getStopIndex(), bindColumns(each.getColumns(), statementBinderContext, tableBinderContexts,
                    outerTableBinderContexts), bindValue(each.getValue(), statementBinderContext, tableBinderContexts, outerTableBinderContexts)));
        }
        return new SetAssignmentSegment(segment.getStartIndex(), segment.getStopIndex(), assignments);
    }
    
    private static List<ColumnSegment> bindColumns(final List<ColumnSegment> columns, final SQLStatementBinderContext statementBinderContext,
                                                   final Map<String, TableSegmentBinderContext> tableBinderContexts, final Map<String, TableSegmentBinderContext> outerTableBinderContexts) {
        List<ColumnSegment> result = new LinkedList<>();
        for (ColumnSegment each : columns) {
            result.add(ColumnSegmentBinder.bind(each, SegmentType.SET_ASSIGNMENT, statementBinderContext, tableBinderContexts, outerTableBinderContexts));
        }
        return result;
    }
    
    private static ExpressionSegment bindValue(final ExpressionSegment value, final SQLStatementBinderContext statementBinderContext,
                                               final Map<String, TableSegmentBinderContext> tableBinderContexts, final Map<String, TableSegmentBinderContext> outerTableBinderContexts) {
        return ExpressionSegmentBinder.bind(value, SegmentType.SET_ASSIGNMENT, statementBinderContext, tableBinderContexts, outerTableBinderContexts);
    }
}
