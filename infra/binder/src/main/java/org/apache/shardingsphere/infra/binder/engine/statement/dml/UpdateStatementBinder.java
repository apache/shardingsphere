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
import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.assign.AssignmentSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.TableSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.context.TableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.order.OrderBySegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.predicate.WhereSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.with.WithSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinder;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dml.UpdateStatement;

/**
 * Update statement binder.
 */
public final class UpdateStatementBinder implements SQLStatementBinder<UpdateStatement> {
    
    @Override
    public UpdateStatement bind(final UpdateStatement sqlStatement, final SQLStatementBinderContext binderContext) {
        UpdateStatement result = copy(sqlStatement);
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts = LinkedHashMultimap.create();
        sqlStatement.getWithSegment().ifPresent(optional -> result.setWithSegment(WithSegmentBinder.bind(optional, binderContext, binderContext.getExternalTableBinderContexts())));
        result.setTable(TableSegmentBinder.bind(sqlStatement.getTable(), binderContext, tableBinderContexts, LinkedHashMultimap.create()));
        sqlStatement.getFrom().ifPresent(optional -> result.setFrom(TableSegmentBinder.bind(optional, binderContext, tableBinderContexts, LinkedHashMultimap.create())));
        sqlStatement.getAssignmentSegment().ifPresent(optional -> result.setSetAssignment(AssignmentSegmentBinder.bind(optional, binderContext, tableBinderContexts, LinkedHashMultimap.create())));
        sqlStatement.getWhere().ifPresent(optional -> result.setWhere(WhereSegmentBinder.bind(optional, binderContext, tableBinderContexts, LinkedHashMultimap.create())));
        sqlStatement.getOrderBy().ifPresent(optional -> result.setOrderBy(
                OrderBySegmentBinder.bind(optional, binderContext, LinkedHashMultimap.create(), tableBinderContexts, LinkedHashMultimap.create())));
        return result;
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private UpdateStatement copy(final UpdateStatement sqlStatement) {
        UpdateStatement result = sqlStatement.getClass().getDeclaredConstructor().newInstance();
        sqlStatement.getLimit().ifPresent(result::setLimit);
        result.addParameterMarkerSegments(sqlStatement.getParameterMarkerSegments());
        result.getCommentSegments().addAll(sqlStatement.getCommentSegments());
        result.getVariableNames().addAll(sqlStatement.getVariableNames());
        return result;
    }
}
