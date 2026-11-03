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

package org.apache.shardingsphere.infra.binder.engine.statement.dml;

import com.cedarsoftware.util.CaseInsensitiveMap.CaseInsensitiveString;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.ReturningSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.assign.AssignmentSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.TableSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.context.TableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.order.OrderBySegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.predicate.WhereSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.projection.type.ShorthandProjectionSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.with.WithSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinder;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementCopyUtils;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.ReturningSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.SetAssignmentSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ShorthandProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.OrderBySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.WithSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.JoinTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.UpdateStatement;

import javax.annotation.Nullable;

/**
 * Update statement binder.
 */
public final class UpdateStatementBinder implements SQLStatementBinder<UpdateStatement> {
    
    @Override
    public UpdateStatement bind(final UpdateStatement sqlStatement, final SQLStatementBinderContext binderContext) {
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> outerTableBinderContexts = LinkedHashMultimap.create();
        WithSegment boundWith = sqlStatement.getWith().map(optional -> WithSegmentBinder.bind(optional, binderContext, outerTableBinderContexts)).orElse(null);
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts = LinkedHashMultimap.create();
        TableSegment boundFrom;
        TableSegment boundTable;
        if (sqlStatement.isTargetTableIsFromAlias()) {
            boundFrom = sqlStatement.getFrom().map(optional -> TableSegmentBinder.bind(optional, binderContext, tableBinderContexts, outerTableBinderContexts)).orElse(null);
            boundTable = TableSegmentBinder.bind(sqlStatement.getTable(), binderContext, tableBinderContexts, outerTableBinderContexts);
        } else {
            boundTable = TableSegmentBinder.bind(sqlStatement.getTable(), binderContext, tableBinderContexts, outerTableBinderContexts);
            boundFrom = sqlStatement.getFrom().map(optional -> TableSegmentBinder.bind(optional, binderContext, tableBinderContexts, outerTableBinderContexts)).orElse(null);
        }
        SetAssignmentSegment boundSetAssignment = sqlStatement.getAssignment()
                .map(optional -> AssignmentSegmentBinder.bind(optional, binderContext, tableBinderContexts, outerTableBinderContexts)).orElse(null);
        WhereSegment boundWhere = sqlStatement.getWhere().map(optional -> WhereSegmentBinder.bind(optional, binderContext, tableBinderContexts, outerTableBinderContexts)).orElse(null);
        OrderBySegment boundOrderBy = sqlStatement.getOrderBy()
                .map(optional -> OrderBySegmentBinder.bind(optional, binderContext, tableBinderContexts, tableBinderContexts, outerTableBinderContexts)).orElse(null);
        ReturningSegment boundReturning = sqlStatement.getReturning()
                .map(optional -> ReturningSegmentBinder.bind(optional, binderContext, getReturningTableScope(optional, boundTable, boundFrom, tableBinderContexts),
                        tableBinderContexts, outerTableBinderContexts))
                .orElse(null);
        return copy(sqlStatement, boundWith, boundTable, boundFrom, boundSetAssignment, boundWhere, boundOrderBy, boundReturning);
    }
    
    @HighFrequencyInvocation
    private TableSegment getReturningTableScope(final ReturningSegment returning, final TableSegment table, @Nullable final TableSegment from,
                                                final Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableContexts) {
        if (null != from) {
            for (ProjectionSegment each : returning.getProjections().getProjections()) {
                if (each instanceof ShorthandProjectionSegment && !((ShorthandProjectionSegment) each).getOwner().isPresent()) {
                    return createReturningTableScope(table, from, tableContexts);
                }
            }
        }
        return table;
    }
    
    private JoinTableSegment createReturningTableScope(final TableSegment table, final TableSegment from,
                                                       final Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableContexts) {
        JoinTableSegment result = new JoinTableSegment();
        result.setStartIndex(table.getStartIndex());
        result.setStopIndex(from.getStopIndex());
        result.setLeft(table);
        result.setRight(from);
        result.setJoinType("CROSS");
        result.getDerivedJoinTableProjectionSegments().addAll(ShorthandProjectionSegmentBinder.bind(new ShorthandProjectionSegment(0, 0), table, tableContexts).getActualProjectionSegments());
        result.getDerivedJoinTableProjectionSegments().addAll(ShorthandProjectionSegmentBinder.bind(new ShorthandProjectionSegment(0, 0), from, tableContexts).getActualProjectionSegments());
        return result;
    }
    
    private UpdateStatement copy(final UpdateStatement sqlStatement, final WithSegment boundWith, final TableSegment boundTable, final TableSegment boundFrom,
                                 final SetAssignmentSegment boundSetAssignment, final WhereSegment boundWhere, final OrderBySegment boundOrderBy,
                                 @Nullable final ReturningSegment boundReturning) {
        UpdateStatement result = UpdateStatement.builder().databaseType(sqlStatement.getDatabaseType()).with(boundWith).table(boundTable)
                .from(boundFrom).setAssignment(boundSetAssignment).where(boundWhere).orderBy(boundOrderBy).limit(sqlStatement.getLimit().orElse(null))
                .returning(boundReturning).errorLogging(sqlStatement.getErrorLogging().orElse(null))
                .targetTableIsFromAlias(sqlStatement.isTargetTableIsFromAlias()).build();
        SQLStatementCopyUtils.copyAttributes(sqlStatement, result);
        return result;
    }
}
