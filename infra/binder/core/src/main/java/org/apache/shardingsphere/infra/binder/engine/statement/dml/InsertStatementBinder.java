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
import org.apache.shardingsphere.infra.binder.engine.segment.dml.column.InsertColumnsSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.expression.type.SubquerySegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.context.TableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.type.SimpleTableSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.with.WithSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinder;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dml.InsertStatement;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Insert statement binder.
 */
public final class InsertStatementBinder implements SQLStatementBinder<InsertStatement> {
    
    @Override
    public InsertStatement bind(final InsertStatement sqlStatement, final SQLStatementBinderContext binderContext) {
        InsertStatement result = copy(sqlStatement);
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts = LinkedHashMultimap.create();
        sqlStatement.getWithSegment().ifPresent(optional -> result.setWithSegment(WithSegmentBinder.bind(optional, binderContext, binderContext.getExternalTableBinderContexts())));
        sqlStatement.getTable().ifPresent(optional -> result.setTable(SimpleTableSegmentBinder.bind(optional, binderContext, tableBinderContexts)));
        if (sqlStatement.getInsertColumns().isPresent() && !sqlStatement.getInsertColumns().get().getColumns().isEmpty()) {
            result.setInsertColumns(InsertColumnsSegmentBinder.bind(sqlStatement.getInsertColumns().get(), binderContext, tableBinderContexts));
        } else {
            sqlStatement.getInsertColumns().ifPresent(result::setInsertColumns);
            tableBinderContexts.values().forEach(each -> result.getDerivedInsertColumns().addAll(getVisibleColumns(each.getProjectionSegments())));
        }
        sqlStatement.getSetAssignment().ifPresent(optional -> result.setSetAssignment(AssignmentSegmentBinder.bind(optional, binderContext, tableBinderContexts, LinkedHashMultimap.create())));
        sqlStatement.getInsertSelect().ifPresent(optional -> result.setInsertSelect(SubquerySegmentBinder.bind(optional, binderContext, tableBinderContexts)));
        return result;
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private InsertStatement copy(final InsertStatement sqlStatement) {
        InsertStatement result = sqlStatement.getClass().getDeclaredConstructor().newInstance();
        result.getValues().addAll(sqlStatement.getValues());
        sqlStatement.getOnDuplicateKeyColumns().ifPresent(result::setOnDuplicateKeyColumns);
        sqlStatement.getOutputSegment().ifPresent(result::setOutputSegment);
        sqlStatement.getMultiTableInsertType().ifPresent(result::setMultiTableInsertType);
        sqlStatement.getMultiTableInsertIntoSegment().ifPresent(result::setMultiTableInsertIntoSegment);
        sqlStatement.getMultiTableConditionalIntoSegment().ifPresent(result::setMultiTableConditionalIntoSegment);
        sqlStatement.getReturningSegment().ifPresent(result::setReturningSegment);
        result.addParameterMarkerSegments(sqlStatement.getParameterMarkerSegments());
        result.getCommentSegments().addAll(sqlStatement.getCommentSegments());
        result.getVariableNames().addAll(sqlStatement.getVariableNames());
        return result;
    }
    
    private Collection<ColumnSegment> getVisibleColumns(final Collection<ProjectionSegment> projectionSegments) {
        return projectionSegments.stream()
                .filter(each -> each instanceof ColumnProjectionSegment && each.isVisible()).map(each -> ((ColumnProjectionSegment) each).getColumn()).collect(Collectors.toList());
    }
}
