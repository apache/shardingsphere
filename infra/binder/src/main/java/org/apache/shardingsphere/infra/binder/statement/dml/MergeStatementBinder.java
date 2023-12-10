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

import com.cedarsoftware.util.CaseInsensitiveMap;
import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.binder.enums.SegmentType;
import org.apache.shardingsphere.infra.binder.segment.expression.ExpressionSegmentBinder;
import org.apache.shardingsphere.infra.binder.segment.expression.impl.ColumnSegmentBinder;
import org.apache.shardingsphere.infra.binder.segment.from.TableSegmentBinder;
import org.apache.shardingsphere.infra.binder.segment.from.TableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.segment.where.WhereSegmentBinder;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementBinder;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.AssignmentSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.ColumnAssignmentSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.InsertValuesSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.SetAssignmentSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionWithParamsSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.MergeStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.UpdateStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.handler.dml.InsertStatementHandler;
import org.apache.shardingsphere.sql.parser.sql.dialect.handler.dml.UpdateStatementHandler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Merge statement binder.
 */
public final class MergeStatementBinder implements SQLStatementBinder<MergeStatement> {
    
    @Override
    public MergeStatement bind(final MergeStatement sqlStatement, final ShardingSphereMetaData metaData, final String defaultDatabaseName) {
        return bind(sqlStatement, metaData, defaultDatabaseName, Collections.emptyMap());
    }
    
    @SneakyThrows
    private MergeStatement bind(final MergeStatement sqlStatement, final ShardingSphereMetaData metaData, final String defaultDatabaseName,
                                final Map<String, TableSegmentBinderContext> externalTableBinderContexts) {
        MergeStatement result = sqlStatement.getClass().getDeclaredConstructor().newInstance();
        SQLStatementBinderContext statementBinderContext = new SQLStatementBinderContext(metaData, defaultDatabaseName, sqlStatement.getDatabaseType(), sqlStatement.getVariableNames());
        statementBinderContext.getExternalTableBinderContexts().putAll(externalTableBinderContexts);
        Map<String, TableSegmentBinderContext> targetTableBinderContexts = new CaseInsensitiveMap<>();
        TableSegment boundedTargetTableSegment = TableSegmentBinder.bind(sqlStatement.getTarget(), statementBinderContext, targetTableBinderContexts, Collections.emptyMap());
        Map<String, TableSegmentBinderContext> sourceTableBinderContexts = new CaseInsensitiveMap<>();
        TableSegment boundedSourceTableSegment = TableSegmentBinder.bind(sqlStatement.getSource(), statementBinderContext, sourceTableBinderContexts, Collections.emptyMap());
        result.setTarget(boundedTargetTableSegment);
        result.setSource(boundedSourceTableSegment);
        Map<String, TableSegmentBinderContext> tableBinderContexts = new LinkedHashMap<>();
        tableBinderContexts.putAll(sourceTableBinderContexts);
        tableBinderContexts.putAll(targetTableBinderContexts);
        if (sqlStatement.getExpression() != null) {
            ExpressionWithParamsSegment expression = new ExpressionWithParamsSegment(sqlStatement.getExpression().getStartIndex(), sqlStatement.getExpression().getStopIndex(),
                    ExpressionSegmentBinder.bind(sqlStatement.getExpression().getExpr(), SegmentType.JOIN_ON, statementBinderContext, tableBinderContexts, Collections.emptyMap()));
            expression.getParameterMarkerSegments().addAll(sqlStatement.getExpression().getParameterMarkerSegments());
            result.setExpression(expression);
        }
        result.setInsert(Optional.ofNullable(sqlStatement.getInsert()).map(optional -> bindMergeInsert(optional,
                (SimpleTableSegment) boundedTargetTableSegment, statementBinderContext, targetTableBinderContexts, sourceTableBinderContexts)).orElse(null));
        result.setUpdate(Optional.ofNullable(sqlStatement.getUpdate()).map(optional -> bindMergeUpdate(optional,
                (SimpleTableSegment) boundedTargetTableSegment, statementBinderContext, targetTableBinderContexts, sourceTableBinderContexts)).orElse(null));
        result.addParameterMarkerSegments(sqlStatement.getParameterMarkerSegments());
        result.getCommentSegments().addAll(sqlStatement.getCommentSegments());
        return result;
    }
    
    @SneakyThrows
    private InsertStatement bindMergeInsert(final InsertStatement sqlStatement, final SimpleTableSegment tableSegment, final SQLStatementBinderContext statementBinderContext,
                                            final Map<String, TableSegmentBinderContext> targetTableBinderContexts, final Map<String, TableSegmentBinderContext> sourceTableBinderContexts) {
        InsertStatement result = sqlStatement.getClass().getDeclaredConstructor().newInstance();
        result.setTable(tableSegment);
        sqlStatement.getInsertColumns().ifPresent(result::setInsertColumns);
        sqlStatement.getInsertSelect().ifPresent(result::setInsertSelect);
        SQLStatementBinderContext insertStatementBinderContext = new SQLStatementBinderContext(statementBinderContext.getMetaData(), statementBinderContext.getDefaultDatabaseName(),
                statementBinderContext.getDatabaseType(), statementBinderContext.getVariableNames());
        insertStatementBinderContext.getExternalTableBinderContexts().putAll(statementBinderContext.getExternalTableBinderContexts());
        insertStatementBinderContext.getExternalTableBinderContexts().putAll(sourceTableBinderContexts);
        Collection<InsertValuesSegment> insertValues = new LinkedList<>();
        for (InsertValuesSegment each : sqlStatement.getValues()) {
            List<ExpressionSegment> values = new LinkedList<>();
            for (ExpressionSegment value : each.getValues()) {
                values.add(ExpressionSegmentBinder.bind(value, SegmentType.VALUES, insertStatementBinderContext, targetTableBinderContexts, sourceTableBinderContexts));
            }
            insertValues.add(new InsertValuesSegment(each.getStartIndex(), each.getStopIndex(), values));
        }
        result.getValues().addAll(insertValues);
        InsertStatementHandler.getOnDuplicateKeyColumnsSegment(sqlStatement).ifPresent(optional -> InsertStatementHandler.setOnDuplicateKeyColumnsSegment(result, optional));
        InsertStatementHandler.getSetAssignmentSegment(sqlStatement).ifPresent(optional -> InsertStatementHandler.setSetAssignmentSegment(result, optional));
        InsertStatementHandler.getWithSegment(sqlStatement).ifPresent(optional -> InsertStatementHandler.setWithSegment(result, optional));
        InsertStatementHandler.getOutputSegment(sqlStatement).ifPresent(optional -> InsertStatementHandler.setOutputSegment(result, optional));
        InsertStatementHandler.getMultiTableInsertType(sqlStatement).ifPresent(optional -> InsertStatementHandler.setMultiTableInsertType(result, optional));
        InsertStatementHandler.getMultiTableInsertIntoSegment(sqlStatement).ifPresent(optional -> InsertStatementHandler.setMultiTableInsertIntoSegment(result, optional));
        InsertStatementHandler.getMultiTableConditionalIntoSegment(sqlStatement).ifPresent(optional -> InsertStatementHandler.setMultiTableConditionalIntoSegment(result, optional));
        InsertStatementHandler.getReturningSegment(sqlStatement).ifPresent(optional -> InsertStatementHandler.setReturningSegment(result, optional));
        InsertStatementHandler.getWhereSegment(sqlStatement).ifPresent(optional -> InsertStatementHandler.setWhereSegment(result,
                WhereSegmentBinder.bind(optional, insertStatementBinderContext, targetTableBinderContexts, sourceTableBinderContexts)));
        result.addParameterMarkerSegments(sqlStatement.getParameterMarkerSegments());
        result.getCommentSegments().addAll(sqlStatement.getCommentSegments());
        return result;
    }
    
    @SneakyThrows
    private UpdateStatement bindMergeUpdate(final UpdateStatement sqlStatement, final SimpleTableSegment tableSegment, final SQLStatementBinderContext statementBinderContext,
                                            final Map<String, TableSegmentBinderContext> targetTableBinderContexts, final Map<String, TableSegmentBinderContext> sourceTableBinderContexts) {
        UpdateStatement result = sqlStatement.getClass().getDeclaredConstructor().newInstance();
        result.setTable(tableSegment);
        Collection<AssignmentSegment> assignments = new LinkedList<>();
        SQLStatementBinderContext updateStatementBinderContext = new SQLStatementBinderContext(statementBinderContext.getMetaData(), statementBinderContext.getDefaultDatabaseName(),
                statementBinderContext.getDatabaseType(), statementBinderContext.getVariableNames());
        updateStatementBinderContext.getExternalTableBinderContexts().putAll(statementBinderContext.getExternalTableBinderContexts());
        updateStatementBinderContext.getExternalTableBinderContexts().putAll(sourceTableBinderContexts);
        for (AssignmentSegment each : sqlStatement.getSetAssignment().getAssignments()) {
            List<ColumnSegment> columnSegments = new ArrayList<>(each.getColumns().size());
            each.getColumns().forEach(column -> columnSegments.add(
                    ColumnSegmentBinder.bind(column, SegmentType.SET_ASSIGNMENT, updateStatementBinderContext, targetTableBinderContexts, Collections.emptyMap())));
            ExpressionSegment value = ExpressionSegmentBinder.bind(each.getValue(), SegmentType.SET_ASSIGNMENT, updateStatementBinderContext, targetTableBinderContexts, Collections.emptyMap());
            ColumnAssignmentSegment columnAssignmentSegment = new ColumnAssignmentSegment(each.getStartIndex(), each.getStopIndex(), columnSegments, value);
            assignments.add(columnAssignmentSegment);
        }
        SetAssignmentSegment setAssignmentSegment = new SetAssignmentSegment(sqlStatement.getSetAssignment().getStartIndex(), sqlStatement.getSetAssignment().getStopIndex(), assignments);
        result.setSetAssignment(setAssignmentSegment);
        sqlStatement.getWhere().ifPresent(optional -> result.setWhere(WhereSegmentBinder.bind(optional, updateStatementBinderContext, targetTableBinderContexts, Collections.emptyMap())));
        UpdateStatementHandler.getDeleteWhereSegment(sqlStatement).ifPresent(optional -> UpdateStatementHandler.setDeleteWhereSegment(result,
                WhereSegmentBinder.bind(optional, updateStatementBinderContext, targetTableBinderContexts, Collections.emptyMap())));
        UpdateStatementHandler.getOrderBySegment(sqlStatement).ifPresent(optional -> UpdateStatementHandler.setOrderBySegment(result, optional));
        UpdateStatementHandler.getLimitSegment(sqlStatement).ifPresent(optional -> UpdateStatementHandler.setLimitSegment(result, optional));
        UpdateStatementHandler.getWithSegment(sqlStatement).ifPresent(optional -> UpdateStatementHandler.setWithSegment(result, optional));
        result.addParameterMarkerSegments(sqlStatement.getParameterMarkerSegments());
        result.getCommentSegments().addAll(sqlStatement.getCommentSegments());
        return result;
    }
}
