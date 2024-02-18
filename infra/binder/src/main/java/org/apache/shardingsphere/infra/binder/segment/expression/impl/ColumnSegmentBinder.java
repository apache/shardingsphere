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

package org.apache.shardingsphere.infra.binder.segment.expression.impl;

import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.groovy.util.Maps;
import org.apache.shardingsphere.infra.binder.enums.SegmentType;
import org.apache.shardingsphere.infra.binder.segment.from.FunctionTableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.segment.from.SimpleTableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.segment.from.TableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.infra.exception.AmbiguousColumnException;
import org.apache.shardingsphere.infra.exception.UnknownColumnException;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.bounded.ColumnSegmentBoundedInfo;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map;
import java.util.Optional;

/**
 * Column segment binder.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ColumnSegmentBinder {
    
    private static final Collection<String> EXCLUDE_BIND_COLUMNS = new LinkedHashSet<>(Arrays.asList("ROWNUM", "ROW_NUMBER", "ROWNUM_", "SYSDATE", "SYSTIMESTAMP", "CURRENT_TIMESTAMP",
            "LOCALTIMESTAMP", "UID", "USER", "NEXTVAL", "ROWID", "LEVEL"));
    
    private static final Map<SegmentType, String> SEGMENT_TYPE_MESSAGES = Maps.of(SegmentType.PROJECTION, "field list", SegmentType.JOIN_ON, "on clause", SegmentType.JOIN_USING, "from clause",
            SegmentType.PREDICATE, "where clause", SegmentType.ORDER_BY, "order clause", SegmentType.GROUP_BY, "group statement", SegmentType.INSERT_COLUMNS, "field list");
    
    private static final String UNKNOWN_SEGMENT_TYPE_MESSAGE = "unknown clause";
    
    /**
     * Bind column segment with metadata.
     *
     * @param segment table segment
     * @param parentSegmentType parent segment type
     * @param statementBinderContext statement binder context
     * @param tableBinderContexts table binder contexts
     * @param outerTableBinderContexts outer table binder contexts
     * @return bounded column segment
     */
    public static ColumnSegment bind(final ColumnSegment segment, final SegmentType parentSegmentType, final SQLStatementBinderContext statementBinderContext,
                                     final Map<String, TableSegmentBinderContext> tableBinderContexts, final Map<String, TableSegmentBinderContext> outerTableBinderContexts) {
        if (EXCLUDE_BIND_COLUMNS.contains(segment.getIdentifier().getValue().toUpperCase())) {
            return segment;
        }
        ColumnSegment result = new ColumnSegment(segment.getStartIndex(), segment.getStopIndex(), segment.getIdentifier());
        segment.getOwner().ifPresent(result::setOwner);
        Collection<TableSegmentBinderContext> tableBinderContextValues =
                getTableSegmentBinderContexts(segment, parentSegmentType, statementBinderContext, tableBinderContexts, outerTableBinderContexts);
        Optional<ColumnSegment> inputColumnSegment = findInputColumnSegment(segment, parentSegmentType, tableBinderContextValues, outerTableBinderContexts, statementBinderContext);
        inputColumnSegment.ifPresent(optional -> result.setVariable(optional.isVariable()));
        result.setColumnBoundedInfo(createColumnSegmentBoundedInfo(segment, inputColumnSegment.orElse(null)));
        segment.getParentheses().forEach(each -> result.getParentheses().add(each));
        return result;
    }
    
    private static Collection<TableSegmentBinderContext> getTableSegmentBinderContexts(final ColumnSegment segment, final SegmentType parentSegmentType,
                                                                                       final SQLStatementBinderContext statementBinderContext,
                                                                                       final Map<String, TableSegmentBinderContext> tableBinderContexts,
                                                                                       final Map<String, TableSegmentBinderContext> outerTableBinderContexts) {
        if (segment.getOwner().isPresent()) {
            return getTableBinderContextByOwner(segment.getOwner().get().getIdentifier().getValue().toLowerCase(), tableBinderContexts, outerTableBinderContexts,
                    statementBinderContext.getExternalTableBinderContexts());
        }
        if (!statementBinderContext.getJoinTableProjectionSegments().isEmpty() && isNeedUseJoinTableProjectionBind(segment, parentSegmentType, statementBinderContext)) {
            return Collections.singleton(new SimpleTableSegmentBinderContext(statementBinderContext.getJoinTableProjectionSegments()));
        }
        return tableBinderContexts.values();
    }
    
    private static boolean isNeedUseJoinTableProjectionBind(final ColumnSegment segment, final SegmentType parentSegmentType, final SQLStatementBinderContext statementBinderContext) {
        return SegmentType.PROJECTION == parentSegmentType
                || SegmentType.PREDICATE == parentSegmentType && statementBinderContext.getUsingColumnNames().contains(segment.getIdentifier().getValue().toLowerCase());
    }
    
    private static Collection<TableSegmentBinderContext> getTableBinderContextByOwner(final String owner, final Map<String, TableSegmentBinderContext> tableBinderContexts,
                                                                                      final Map<String, TableSegmentBinderContext> outerTableBinderContexts,
                                                                                      final Map<String, TableSegmentBinderContext> externalTableBinderContexts) {
        if (tableBinderContexts.containsKey(owner)) {
            return Collections.singleton(tableBinderContexts.get(owner));
        }
        if (outerTableBinderContexts.containsKey(owner)) {
            return Collections.singleton(outerTableBinderContexts.get(owner));
        }
        if (externalTableBinderContexts.containsKey(owner)) {
            return Collections.singleton(externalTableBinderContexts.get(owner));
        }
        return Collections.emptyList();
    }
    
    private static Optional<ColumnSegment> findInputColumnSegment(final ColumnSegment segment, final SegmentType parentSegmentType, final Collection<TableSegmentBinderContext> tableBinderContexts,
                                                                  final Map<String, TableSegmentBinderContext> outerTableBinderContexts, final SQLStatementBinderContext statementBinderContext) {
        ColumnSegment result = null;
        boolean isFindInputColumn = false;
        for (TableSegmentBinderContext each : tableBinderContexts) {
            Optional<ProjectionSegment> projectionSegment = each.findProjectionSegmentByColumnLabel(segment.getIdentifier().getValue());
            if (projectionSegment.isPresent() && projectionSegment.get() instanceof ColumnProjectionSegment) {
                ShardingSpherePreconditions.checkState(null == result,
                        () -> new AmbiguousColumnException(segment.getExpression(), SEGMENT_TYPE_MESSAGES.getOrDefault(parentSegmentType, UNKNOWN_SEGMENT_TYPE_MESSAGE)));
                result = ((ColumnProjectionSegment) projectionSegment.get()).getColumn();
            }
            if (!isFindInputColumn && projectionSegment.isPresent()) {
                isFindInputColumn = true;
            }
        }
        if (!isFindInputColumn) {
            Optional<ProjectionSegment> projectionSegment = findInputColumnSegmentFromOuterTable(segment, outerTableBinderContexts);
            isFindInputColumn = projectionSegment.isPresent();
            if (projectionSegment.isPresent() && projectionSegment.get() instanceof ColumnProjectionSegment) {
                result = ((ColumnProjectionSegment) projectionSegment.get()).getColumn();
            }
        }
        if (!isFindInputColumn) {
            Optional<ProjectionSegment> projectionSegment = findInputColumnSegmentFromExternalTables(segment, statementBinderContext.getExternalTableBinderContexts());
            isFindInputColumn = projectionSegment.isPresent();
            if (projectionSegment.isPresent() && projectionSegment.get() instanceof ColumnProjectionSegment) {
                result = ((ColumnProjectionSegment) projectionSegment.get()).getColumn();
            }
        }
        if (!isFindInputColumn) {
            result = findInputColumnSegmentByVariables(segment, statementBinderContext.getVariableNames()).orElse(null);
            isFindInputColumn = result != null;
        }
        if (!isFindInputColumn) {
            result = findInputColumnSegmentByPivotColumns(segment, statementBinderContext.getPivotColumnNames()).orElse(null);
            isFindInputColumn = result != null;
        }
        ShardingSpherePreconditions.checkState(isFindInputColumn || containsFunctionTable(tableBinderContexts, outerTableBinderContexts.values()),
                () -> new UnknownColumnException(segment.getExpression(), SEGMENT_TYPE_MESSAGES.getOrDefault(parentSegmentType, UNKNOWN_SEGMENT_TYPE_MESSAGE)));
        return Optional.ofNullable(result);
    }
    
    private static Optional<ColumnSegment> findInputColumnSegmentByPivotColumns(final ColumnSegment segment, final Collection<String> pivotColumnNames) {
        if (pivotColumnNames.isEmpty()) {
            return Optional.empty();
        }
        if (pivotColumnNames.contains(segment.getIdentifier().getValue().toLowerCase())) {
            return Optional.of(new ColumnSegment(0, 0, segment.getIdentifier()));
        }
        return Optional.empty();
    }
    
    private static Optional<ProjectionSegment> findInputColumnSegmentFromOuterTable(final ColumnSegment segment, final Map<String, TableSegmentBinderContext> outerTableBinderContexts) {
        ListIterator<TableSegmentBinderContext> listIterator = new ArrayList<>(outerTableBinderContexts.values()).listIterator(outerTableBinderContexts.size());
        while (listIterator.hasPrevious()) {
            TableSegmentBinderContext each = listIterator.previous();
            Optional<ProjectionSegment> result = each.findProjectionSegmentByColumnLabel(segment.getIdentifier().getValue());
            if (result.isPresent()) {
                return result;
            }
        }
        return Optional.empty();
    }
    
    private static Optional<ProjectionSegment> findInputColumnSegmentFromExternalTables(final ColumnSegment segment, final Map<String, TableSegmentBinderContext> externalTableBinderContexts) {
        for (TableSegmentBinderContext each : externalTableBinderContexts.values()) {
            Optional<ProjectionSegment> result = each.findProjectionSegmentByColumnLabel(segment.getIdentifier().getValue());
            if (result.isPresent()) {
                return result;
            }
        }
        return Optional.empty();
    }
    
    private static Optional<ColumnSegment> findInputColumnSegmentByVariables(final ColumnSegment segment, final Collection<String> variableNames) {
        if (variableNames.isEmpty()) {
            return Optional.empty();
        }
        if (variableNames.contains(segment.getIdentifier().getValue().toLowerCase())) {
            ColumnSegment result = new ColumnSegment(0, 0, segment.getIdentifier());
            result.setVariable(true);
            return Optional.of(result);
        }
        return Optional.empty();
    }
    
    private static boolean containsFunctionTable(final Collection<TableSegmentBinderContext> tableBinderContexts, final Collection<TableSegmentBinderContext> outerBinderContexts) {
        for (TableSegmentBinderContext each : tableBinderContexts) {
            if (each instanceof FunctionTableSegmentBinderContext) {
                return true;
            }
        }
        for (TableSegmentBinderContext each : outerBinderContexts) {
            if (each instanceof FunctionTableSegmentBinderContext) {
                return true;
            }
        }
        return false;
    }
    
    private static ColumnSegmentBoundedInfo createColumnSegmentBoundedInfo(final ColumnSegment segment, final ColumnSegment inputColumnSegment) {
        IdentifierValue originalDatabase = null == inputColumnSegment ? null : inputColumnSegment.getColumnBoundedInfo().getOriginalDatabase();
        IdentifierValue originalSchema = null == inputColumnSegment ? null : inputColumnSegment.getColumnBoundedInfo().getOriginalSchema();
        IdentifierValue segmentOriginalTable = segment.getColumnBoundedInfo().getOriginalTable();
        IdentifierValue originalTable = Strings.isNullOrEmpty(segmentOriginalTable.getValue())
                ? Optional.ofNullable(inputColumnSegment).map(optional -> optional.getColumnBoundedInfo().getOriginalTable()).orElse(segmentOriginalTable)
                : segmentOriginalTable;
        IdentifierValue segmentOriginalColumn = segment.getColumnBoundedInfo().getOriginalColumn();
        IdentifierValue originalColumn = Strings.isNullOrEmpty(segmentOriginalColumn.getValue())
                ? Optional.ofNullable(inputColumnSegment).map(optional -> optional.getColumnBoundedInfo().getOriginalColumn()).orElse(segmentOriginalColumn)
                : segmentOriginalColumn;
        return new ColumnSegmentBoundedInfo(originalDatabase, originalSchema, originalTable, originalColumn);
    }
    
    /**
     * Bind using column segment with metadata.
     *
     * @param segment using column segment
     * @param parentSegmentType parent segment type
     * @param tableBinderContexts table binder contexts
     * @return bounded using column segment
     */
    public static ColumnSegment bindUsingColumn(final ColumnSegment segment, final SegmentType parentSegmentType, final Map<String, TableSegmentBinderContext> tableBinderContexts) {
        ColumnSegment result = new ColumnSegment(segment.getStartIndex(), segment.getStopIndex(), segment.getIdentifier());
        segment.getOwner().ifPresent(result::setOwner);
        Collection<TableSegmentBinderContext> tableBinderContextValues = tableBinderContexts.values();
        Collection<ColumnSegment> usingInputColumnSegments = findUsingInputColumnSegments(segment.getIdentifier().getValue(), tableBinderContextValues);
        ShardingSpherePreconditions.checkState(usingInputColumnSegments.size() >= 2,
                () -> new UnknownColumnException(segment.getExpression(), SEGMENT_TYPE_MESSAGES.getOrDefault(parentSegmentType, UNKNOWN_SEGMENT_TYPE_MESSAGE)));
        Iterator<ColumnSegment> iterator = usingInputColumnSegments.iterator();
        result.setColumnBoundedInfo(createColumnSegmentBoundedInfo(segment, iterator.next()));
        result.setOtherUsingColumnBoundedInfo(createColumnSegmentBoundedInfo(segment, iterator.next()));
        return result;
    }
    
    private static Collection<ColumnSegment> findUsingInputColumnSegments(final String columnName, final Collection<TableSegmentBinderContext> tableBinderContexts) {
        Collection<ColumnSegment> result = new LinkedList<>();
        for (TableSegmentBinderContext each : tableBinderContexts) {
            Optional<ProjectionSegment> projectionSegment = each.findProjectionSegmentByColumnLabel(columnName);
            if (projectionSegment.isPresent() && projectionSegment.get() instanceof ColumnProjectionSegment) {
                result.add(((ColumnProjectionSegment) projectionSegment.get()).getColumn());
            }
        }
        return result;
    }
}
