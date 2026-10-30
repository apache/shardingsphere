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
import org.apache.shardingsphere.infra.binder.engine.segment.dml.assign.AssignmentSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.TableSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.context.TableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.order.OrderBySegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.predicate.WhereSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.returning.ReturningSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.with.WithSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinder;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementCopyUtils;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.ReturningSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.SetAssignmentSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.OrderBySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.WithSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.UpdateStatement;

/**
 * Update statement binder.
 */
public final class UpdateStatementBinder implements SQLStatementBinder<UpdateStatement> {
    
    @Override
    public UpdateStatement bind(final UpdateStatement sqlStatement, final SQLStatementBinderContext binderContext) {
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> outerTableBinderContexts = LinkedHashMultimap.create();
        WithSegment boundWith = sqlStatement.getWith().map(optional -> WithSegmentBinder.bind(optional, binderContext, outerTableBinderContexts)).orElse(null);
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts = LinkedHashMultimap.create();
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> targetTableBinderContexts = LinkedHashMultimap.create();
        TableSegment boundFrom;
        TableSegment boundTable;
        if (sqlStatement.isTargetTableIsFromAlias()) {
            boundFrom = sqlStatement.getFrom().map(optional -> TableSegmentBinder.bind(optional, binderContext, tableBinderContexts, outerTableBinderContexts)).orElse(null);
            putTargetAliasBinderContexts((SimpleTableSegment) sqlStatement.getTable(), tableBinderContexts, targetTableBinderContexts);
            boundTable = TableSegmentBinder.bind(sqlStatement.getTable(), binderContext, tableBinderContexts, outerTableBinderContexts);
        } else {
            boundTable = TableSegmentBinder.bind(sqlStatement.getTable(), binderContext, targetTableBinderContexts, outerTableBinderContexts);
            tableBinderContexts.putAll(targetTableBinderContexts);
            boundFrom = sqlStatement.getFrom().map(optional -> TableSegmentBinder.bind(optional, binderContext, tableBinderContexts, outerTableBinderContexts)).orElse(null);
        }
        SetAssignmentSegment boundSetAssignment = sqlStatement.getAssignment()
                .map(optional -> AssignmentSegmentBinder.bind(optional, binderContext, targetTableBinderContexts, tableBinderContexts, outerTableBinderContexts)).orElse(null);
        WhereSegment boundWhere = sqlStatement.getWhere().map(optional -> WhereSegmentBinder.bind(optional, binderContext, tableBinderContexts, outerTableBinderContexts)).orElse(null);
        OrderBySegment boundOrderBy = sqlStatement.getOrderBy()
                .map(optional -> OrderBySegmentBinder.bind(optional, binderContext, tableBinderContexts, tableBinderContexts, outerTableBinderContexts)).orElse(null);
        ReturningSegment boundReturning = sqlStatement.getReturning()
                .map(optional -> ReturningSegmentBinder.bind(optional, binderContext, boundTable, tableBinderContexts, outerTableBinderContexts)).orElse(null);
        return copy(sqlStatement, boundWith, boundTable, boundFrom, boundSetAssignment, boundWhere, boundOrderBy, boundReturning);
    }
    
    private void putTargetAliasBinderContexts(final SimpleTableSegment tableSegment, final Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts,
                                              final Multimap<CaseInsensitiveString, TableSegmentBinderContext> targetTableBinderContexts) {
        String tableName = tableSegment.getTableName().getIdentifier().getValue();
        CaseInsensitiveString key = CaseInsensitiveString.of(tableName);
        if (tableBinderContexts.containsKey(key)) {
            targetTableBinderContexts.putAll(key, tableBinderContexts.get(key));
            return;
        }
        for (TableSegmentBinderContext each : tableBinderContexts.values()) {
            if (each.getOriginalTableName().map(optional -> optional.getValue().equalsIgnoreCase(tableName)).orElse(false)) {
                targetTableBinderContexts.put(key, each);
            }
        }
    }
    
    private UpdateStatement copy(final UpdateStatement sqlStatement, final WithSegment boundWith, final TableSegment boundTable, final TableSegment boundFrom,
                                 final SetAssignmentSegment boundSetAssignment, final WhereSegment boundWhere, final OrderBySegment boundOrderBy,
                                 final ReturningSegment boundReturning) {
        UpdateStatement result = UpdateStatement.builder().databaseType(sqlStatement.getDatabaseType()).with(boundWith).table(boundTable)
                .from(boundFrom).setAssignment(boundSetAssignment).where(boundWhere).orderBy(boundOrderBy).limit(sqlStatement.getLimit().orElse(null))
                .returning(boundReturning).errorLogging(sqlStatement.getErrorLogging().orElse(null))
                .targetTableIsFromAlias(sqlStatement.isTargetTableIsFromAlias()).build();
        SQLStatementCopyUtils.copyAttributes(sqlStatement, result);
        return result;
    }
}
