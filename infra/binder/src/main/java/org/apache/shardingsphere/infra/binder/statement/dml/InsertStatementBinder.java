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

package org.apache.shardingsphere.infra.binder.statement.dml;

import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.binder.segment.column.InsertColumnsSegmentBinder;
import org.apache.shardingsphere.infra.binder.segment.expression.impl.SubquerySegmentBinder;
import org.apache.shardingsphere.infra.binder.segment.from.TableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.segment.from.impl.SimpleTableSegmentBinder;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementBinder;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.handler.dml.InsertStatementHandler;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;

/**
 * Select statement binder.
 */
public final class InsertStatementBinder implements SQLStatementBinder<InsertStatement> {
    
    @Override
    public InsertStatement bind(final InsertStatement sqlStatement, final ShardingSphereMetaData metaData, final String defaultDatabaseName) {
        return bind(sqlStatement, metaData, defaultDatabaseName, Collections.emptyMap());
    }
    
    @SneakyThrows
    private InsertStatement bind(final InsertStatement sqlStatement, final ShardingSphereMetaData metaData, final String defaultDatabaseName,
                                 final Map<String, TableSegmentBinderContext> externalTableBinderContexts) {
        InsertStatement result = sqlStatement.getClass().getDeclaredConstructor().newInstance();
        SQLStatementBinderContext statementBinderContext = new SQLStatementBinderContext(metaData, defaultDatabaseName, sqlStatement.getDatabaseType(), sqlStatement.getVariableNames());
        statementBinderContext.getExternalTableBinderContexts().putAll(externalTableBinderContexts);
        Map<String, TableSegmentBinderContext> tableBinderContexts = new LinkedHashMap<>();
        Optional.ofNullable(sqlStatement.getTable()).ifPresent(optional -> result.setTable(SimpleTableSegmentBinder.bind(optional, statementBinderContext, tableBinderContexts)));
        if (sqlStatement.getInsertColumns().isPresent() && !sqlStatement.getInsertColumns().get().getColumns().isEmpty()) {
            result.setInsertColumns(InsertColumnsSegmentBinder.bind(sqlStatement.getInsertColumns().get(), statementBinderContext, tableBinderContexts));
        } else {
            sqlStatement.getInsertColumns().ifPresent(result::setInsertColumns);
            tableBinderContexts.values().forEach(each -> result.getDerivedInsertColumns().addAll(getVisibleColumns(each.getProjectionSegments())));
        }
        sqlStatement.getInsertSelect().ifPresent(optional -> result.setInsertSelect(SubquerySegmentBinder.bind(optional, statementBinderContext, tableBinderContexts)));
        result.getValues().addAll(sqlStatement.getValues());
        InsertStatementHandler.getOnDuplicateKeyColumnsSegment(sqlStatement).ifPresent(optional -> InsertStatementHandler.setOnDuplicateKeyColumnsSegment(result, optional));
        InsertStatementHandler.getSetAssignmentSegment(sqlStatement).ifPresent(optional -> InsertStatementHandler.setSetAssignmentSegment(result, optional));
        InsertStatementHandler.getWithSegment(sqlStatement).ifPresent(optional -> InsertStatementHandler.setWithSegment(result, optional));
        InsertStatementHandler.getOutputSegment(sqlStatement).ifPresent(optional -> InsertStatementHandler.setOutputSegment(result, optional));
        InsertStatementHandler.getMultiTableInsertType(sqlStatement).ifPresent(optional -> InsertStatementHandler.setMultiTableInsertType(result, optional));
        InsertStatementHandler.getMultiTableInsertIntoSegment(sqlStatement).ifPresent(optional -> InsertStatementHandler.setMultiTableInsertIntoSegment(result, optional));
        InsertStatementHandler.getMultiTableConditionalIntoSegment(sqlStatement).ifPresent(optional -> InsertStatementHandler.setMultiTableConditionalIntoSegment(result, optional));
        InsertStatementHandler.getReturningSegment(sqlStatement).ifPresent(optional -> InsertStatementHandler.setReturningSegment(result, optional));
        result.addParameterMarkerSegments(sqlStatement.getParameterMarkerSegments());
        result.getCommentSegments().addAll(sqlStatement.getCommentSegments());
        return result;
    }
    
    private Collection<ColumnSegment> getVisibleColumns(final Collection<ProjectionSegment> projectionSegments) {
        Collection<ColumnSegment> result = new LinkedList<>();
        for (ProjectionSegment each : projectionSegments) {
            if (each instanceof ColumnProjectionSegment && each.isVisible()) {
                result.add(((ColumnProjectionSegment) each).getColumn());
            }
        }
        return result;
    }
}
