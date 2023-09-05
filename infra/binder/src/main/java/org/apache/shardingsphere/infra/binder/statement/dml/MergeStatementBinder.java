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
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.shardingsphere.infra.binder.enums.SegmentType;
import org.apache.shardingsphere.infra.binder.segment.expression.ExpressionSegmentBinder;
import org.apache.shardingsphere.infra.binder.segment.expression.impl.ColumnSegmentBinder;
import org.apache.shardingsphere.infra.binder.segment.from.TableSegmentBinder;
import org.apache.shardingsphere.infra.binder.segment.from.TableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.segment.where.WhereSegmentBinder;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementBinder;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.infra.binder.statement.ddl.CursorRecordTableBinderContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.AssignmentSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.ColumnAssignmentSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.InsertValuesSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.SetAssignmentSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Merge statement binder.
 */
public final class MergeStatementBinder implements SQLStatementBinder<MergeStatement> {
    
    @SneakyThrows
    @Override
    public MergeStatement bind(final MergeStatement sqlStatement, final ShardingSphereMetaData metaData,
                               final String defaultDatabaseName, final CursorRecordTableBinderContext cursorRecordTableBinderContext) {
        MergeStatement result = sqlStatement.getClass().getDeclaredConstructor().newInstance();
        Map<String, TableSegmentBinderContext> tableBinderContexts = new CaseInsensitiveMap<>();
        SQLStatementBinderContext statementBinderContext = new SQLStatementBinderContext(metaData, defaultDatabaseName, sqlStatement.getDatabaseType(), sqlStatement.getVariableNames());
        TableSegment boundedTargetTableSegment = TableSegmentBinder.bind(sqlStatement.getTarget(), statementBinderContext, tableBinderContexts, cursorRecordTableBinderContext);
        TableSegment boundedSourceTableSegment = TableSegmentBinder.bind(sqlStatement.getSource(), statementBinderContext, tableBinderContexts, cursorRecordTableBinderContext);
        result.setTarget(boundedTargetTableSegment);
        result.setSource(boundedSourceTableSegment);
        result.setExpr(ExpressionSegmentBinder.bind(sqlStatement.getExpr(), SegmentType.JOIN_ON, statementBinderContext, tableBinderContexts, Collections.emptyMap(), cursorRecordTableBinderContext));
        result.setInsert(Optional.ofNullable(sqlStatement.getInsert()).map(optional -> bindMergeInsert(optional,
                (SimpleTableSegment) boundedTargetTableSegment, statementBinderContext, tableBinderContexts, cursorRecordTableBinderContext)).orElse(null));
        result.setUpdate(Optional.ofNullable(sqlStatement.getUpdate()).map(optional -> bindMergeUpdate(optional,
                (SimpleTableSegment) boundedTargetTableSegment, statementBinderContext, tableBinderContexts, cursorRecordTableBinderContext)).orElse(null));
        result.addParameterMarkerSegments(sqlStatement.getParameterMarkerSegments());
        result.getCommentSegments().addAll(sqlStatement.getCommentSegments());
        return result;
    }
    
    @SneakyThrows
    private InsertStatement bindMergeInsert(final InsertStatement sqlStatement, final SimpleTableSegment tableSegment, final SQLStatementBinderContext statementBinderContext,
                                            final Map<String, TableSegmentBinderContext> tableBinderContexts, final CursorRecordTableBinderContext cursorRecordTableBinderContext) {
        InsertStatement result = sqlStatement.getClass().getDeclaredConstructor().newInstance();
        result.setTable(tableSegment);
        sqlStatement.getInsertColumns().ifPresent(result::setInsertColumns);
        sqlStatement.getInsertSelect().ifPresent(result::setInsertSelect);
        Collection<InsertValuesSegment> insertValues = new LinkedList<>();
        for (InsertValuesSegment each : sqlStatement.getValues()) {
            List<ExpressionSegment> values = new LinkedList<>();
            for (ExpressionSegment value : each.getValues()) {
                values.add(ExpressionSegmentBinder.bind(value, SegmentType.VALUES, statementBinderContext, tableBinderContexts, Collections.emptyMap(), cursorRecordTableBinderContext));
            }
            insertValues.add(new InsertValuesSegment(each.getStartIndex(), each.getStopIndex(), values));
        }
        result.getValues().addAll(insertValues);
        InsertStatementHandler.getOnDuplicateKeyColumnsSegment(sqlStatement).ifPresent(optional -> InsertStatementHandler.setOnDuplicateKeyColumnsSegment(result, optional));
        InsertStatementHandler.getSetAssignmentSegment(sqlStatement).ifPresent(optional -> InsertStatementHandler.setSetAssignmentSegment(result, optional));
        InsertStatementHandler.getWithSegment(sqlStatement).ifPresent(optional -> InsertStatementHandler.setWithSegment(result, optional));
        InsertStatementHandler.getOutputSegment(sqlStatement).ifPresent(optional -> InsertStatementHandler.setOutputSegment(result, optional));
        InsertStatementHandler.getInsertMultiTableElementSegment(sqlStatement).ifPresent(optional -> InsertStatementHandler.setInsertMultiTableElementSegment(result, optional));
        InsertStatementHandler.getReturningSegment(sqlStatement).ifPresent(optional -> InsertStatementHandler.setReturningSegment(result, optional));
        InsertStatementHandler.getWhereSegment(sqlStatement).ifPresent(optional -> InsertStatementHandler.setWhereSegment(result,
                WhereSegmentBinder.bind(optional, statementBinderContext, tableBinderContexts, Collections.emptyMap(), cursorRecordTableBinderContext)));
        result.addParameterMarkerSegments(sqlStatement.getParameterMarkerSegments());
        result.getCommentSegments().addAll(sqlStatement.getCommentSegments());
        return result;
    }
    
    @SneakyThrows
    private UpdateStatement bindMergeUpdate(final UpdateStatement sqlStatement, final SimpleTableSegment tableSegment, final SQLStatementBinderContext statementBinderContext,
                                            final Map<String, TableSegmentBinderContext> tableBinderContexts, final CursorRecordTableBinderContext cursorRecordTableBinderContext) {
        UpdateStatement result = sqlStatement.getClass().getDeclaredConstructor().newInstance();
        result.setTable(tableSegment);
        Collection<AssignmentSegment> assignments = new LinkedList<>();
        for (AssignmentSegment each : sqlStatement.getSetAssignment().getAssignments()) {
            List<ColumnSegment> columnSegments = new ArrayList<>(each.getColumns().size());
            each.getColumns().forEach(column -> columnSegments.add(ColumnSegmentBinder.bind(column, SegmentType.SET_ASSIGNMENT, statementBinderContext, tableBinderContexts, Collections.emptyMap(),
                    cursorRecordTableBinderContext)));
            ExpressionSegment value = ExpressionSegmentBinder.bind(each.getValue(), SegmentType.SET_ASSIGNMENT, statementBinderContext, tableBinderContexts, Collections.emptyMap(),
                    cursorRecordTableBinderContext);
            ColumnAssignmentSegment columnAssignmentSegment = new ColumnAssignmentSegment(each.getStartIndex(), each.getStopIndex(), columnSegments, value);
            assignments.add(columnAssignmentSegment);
        }
        SetAssignmentSegment setAssignmentSegment = new SetAssignmentSegment(sqlStatement.getSetAssignment().getStartIndex(), sqlStatement.getSetAssignment().getStopIndex(), assignments);
        result.setSetAssignment(setAssignmentSegment);
        sqlStatement.getWhere().ifPresent(optional -> result.setWhere(WhereSegmentBinder.bind(optional, statementBinderContext, tableBinderContexts, Collections.emptyMap(),
                cursorRecordTableBinderContext)));
        UpdateStatementHandler.getOrderBySegment(sqlStatement).ifPresent(optional -> UpdateStatementHandler.setOrderBySegment(result, optional));
        UpdateStatementHandler.getLimitSegment(sqlStatement).ifPresent(optional -> UpdateStatementHandler.setLimitSegment(result, optional));
        UpdateStatementHandler.getWithSegment(sqlStatement).ifPresent(optional -> UpdateStatementHandler.setWithSegment(result, optional));
        result.addParameterMarkerSegments(sqlStatement.getParameterMarkerSegments());
        result.getCommentSegments().addAll(sqlStatement.getCommentSegments());
        return result;
    }
}
