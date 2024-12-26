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

package org.apache.shardingsphere.infra.binder.engine.segment.dml.expression.type;

import com.cedarsoftware.util.CaseInsensitiveMap.CaseInsensitiveString;
import com.cedarsoftware.util.CaseInsensitiveSet;
import com.google.common.base.Strings;
import com.google.common.collect.Multimap;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.groovy.util.Maps;
import org.apache.shardingsphere.infra.binder.engine.segment.SegmentType;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.context.TableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.context.type.FunctionTableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.context.type.SimpleTableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.kernel.metadata.ColumnNotFoundException;
import org.apache.shardingsphere.infra.exception.kernel.syntax.AmbiguousColumnException;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.bound.ColumnSegmentBoundInfo;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.bound.TableSegmentBoundInfo;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Column segment binder.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ColumnSegmentBinder {
    
    private static final Collection<String> EXCLUDE_BIND_COLUMNS = new CaseInsensitiveSet<>(Arrays.asList(
            "ROWNUM", "ROW_NUMBER", "ROWNUM_", "ROWID", "SYSDATE", "SYSTIMESTAMP", "CURRENT_TIMESTAMP", "LOCALTIMESTAMP", "UID", "USER", "NEXTVAL", "LEVEL"));
    
    private static final Map<SegmentType, String> SEGMENT_TYPE_MESSAGES = Maps.of(SegmentType.PROJECTION, "field list", SegmentType.JOIN_ON, "on clause", SegmentType.JOIN_USING, "from clause",
            SegmentType.PREDICATE, "where clause", SegmentType.ORDER_BY, "order clause", SegmentType.GROUP_BY, "group statement", SegmentType.INSERT_COLUMNS, "field list");
    
    private static final String UNKNOWN_SEGMENT_TYPE_MESSAGE = "unknown clause";
    
    /**
     * Bind column segment.
     *
     * @param segment column segment
     * @param parentSegmentType parent segment type
     * @param binderContext statement binder context
     * @param tableBinderContexts table binder contexts
     * @param outerTableBinderContexts outer table binder contexts
     * @return bound column segment
     */
    public static ColumnSegment bind(final ColumnSegment segment, final SegmentType parentSegmentType, final SQLStatementBinderContext binderContext,
                                     final Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts,
                                     final Multimap<CaseInsensitiveString, TableSegmentBinderContext> outerTableBinderContexts) {
        if (EXCLUDE_BIND_COLUMNS.contains(segment.getIdentifier().getValue())) {
            return segment;
        }
        ColumnSegment result = copy(segment);
        Collection<TableSegmentBinderContext> tableSegmentBinderContexts = getTableSegmentBinderContexts(segment, parentSegmentType, binderContext, tableBinderContexts, outerTableBinderContexts);
        Optional<ColumnSegment> inputColumnSegment = findInputColumnSegment(segment, parentSegmentType, tableSegmentBinderContexts, outerTableBinderContexts, binderContext);
        inputColumnSegment.ifPresent(optional -> result.setVariable(optional.isVariable()));
        segment.getOwner().ifPresent(optional -> result.setOwner(bindOwnerTableContext(optional, inputColumnSegment.orElse(null))));
        result.setColumnBoundInfo(createColumnSegmentBoundInfo(segment, inputColumnSegment.orElse(null)));
        return result;
    }
    
    private static ColumnSegment copy(final ColumnSegment segment) {
        ColumnSegment result = new ColumnSegment(segment.getStartIndex(), segment.getStopIndex(), segment.getIdentifier());
        segment.getLeftParentheses().ifPresent(result::setLeftParentheses);
        segment.getRightParentheses().ifPresent(result::setRightParentheses);
        return result;
    }
    
    private static OwnerSegment bindOwnerTableContext(final OwnerSegment owner, final ColumnSegment inputColumnSegment) {
        IdentifierValue originalDatabase = null == inputColumnSegment ? null : inputColumnSegment.getColumnBoundInfo().getOriginalDatabase();
        IdentifierValue originalSchema = null == inputColumnSegment ? null : inputColumnSegment.getColumnBoundInfo().getOriginalSchema();
        if (originalDatabase != null && originalSchema != null) {
            owner.setTableBoundInfo(new TableSegmentBoundInfo(originalDatabase, originalSchema));
        }
        return owner;
    }
    
    private static Collection<TableSegmentBinderContext> getTableSegmentBinderContexts(final ColumnSegment segment, final SegmentType parentSegmentType,
                                                                                       final SQLStatementBinderContext binderContext,
                                                                                       final Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts,
                                                                                       final Multimap<CaseInsensitiveString, TableSegmentBinderContext> outerTableBinderContexts) {
        if (segment.getOwner().isPresent()) {
            String owner = segment.getOwner().get().getIdentifier().getValue();
            return getTableBinderContextByOwner(owner, tableBinderContexts, outerTableBinderContexts, binderContext.getExternalTableBinderContexts());
        }
        if (!binderContext.getJoinTableProjectionSegments().isEmpty() && isNeedUseJoinTableProjectionBind(segment, parentSegmentType, binderContext)) {
            return Collections.singleton(new SimpleTableSegmentBinderContext(binderContext.getJoinTableProjectionSegments()));
        }
        return tableBinderContexts.values();
    }
    
    private static Collection<TableSegmentBinderContext> getTableBinderContextByOwner(final String owner, final Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts,
                                                                                      final Multimap<CaseInsensitiveString, TableSegmentBinderContext> outerTableBinderContexts,
                                                                                      final Multimap<CaseInsensitiveString, TableSegmentBinderContext> externalTableBinderContexts) {
        CaseInsensitiveString caseInsensitiveOwner = new CaseInsensitiveString(owner);
        if (tableBinderContexts.containsKey(caseInsensitiveOwner)) {
            return tableBinderContexts.get(caseInsensitiveOwner);
        }
        if (outerTableBinderContexts.containsKey(caseInsensitiveOwner)) {
            return outerTableBinderContexts.get(caseInsensitiveOwner);
        }
        if (externalTableBinderContexts.containsKey(caseInsensitiveOwner)) {
            return externalTableBinderContexts.get(caseInsensitiveOwner);
        }
        return Collections.emptyList();
    }
    
    private static boolean isNeedUseJoinTableProjectionBind(final ColumnSegment segment, final SegmentType parentSegmentType, final SQLStatementBinderContext binderContext) {
        return SegmentType.PROJECTION == parentSegmentType
                || SegmentType.PREDICATE == parentSegmentType && binderContext.getUsingColumnNames().contains(segment.getIdentifier().getValue());
    }
    
    private static Optional<ColumnSegment> findInputColumnSegment(final ColumnSegment segment, final SegmentType parentSegmentType, final Collection<TableSegmentBinderContext> tableBinderContexts,
                                                                  final Multimap<CaseInsensitiveString, TableSegmentBinderContext> outerTableBinderContexts,
                                                                  final SQLStatementBinderContext binderContext) {
        ColumnSegment result = null;
        boolean isFindInputColumn = false;
        for (TableSegmentBinderContext each : tableBinderContexts) {
            Optional<ProjectionSegment> projectionSegment = each.findProjectionSegmentByColumnLabel(segment.getIdentifier().getValue());
            if (!projectionSegment.isPresent()) {
                continue;
            }
            if (projectionSegment.get() instanceof ColumnProjectionSegment) {
                ShardingSpherePreconditions.checkState(null == result,
                        () -> new AmbiguousColumnException(segment.getExpression(), SEGMENT_TYPE_MESSAGES.getOrDefault(parentSegmentType, UNKNOWN_SEGMENT_TYPE_MESSAGE)));
            }
            result = getColumnSegment(projectionSegment.get());
            isFindInputColumn = true;
        }
        if (!isFindInputColumn) {
            Optional<ProjectionSegment> projectionSegment = findInputColumnSegmentFromOuterTable(segment, outerTableBinderContexts);
            isFindInputColumn = projectionSegment.isPresent();
            if (projectionSegment.isPresent() && projectionSegment.get() instanceof ColumnProjectionSegment) {
                result = ((ColumnProjectionSegment) projectionSegment.get()).getColumn();
            }
        }
        if (!isFindInputColumn) {
            Optional<ProjectionSegment> projectionSegment = findInputColumnSegmentFromExternalTables(segment, binderContext.getExternalTableBinderContexts());
            isFindInputColumn = projectionSegment.isPresent();
            if (projectionSegment.isPresent() && projectionSegment.get() instanceof ColumnProjectionSegment) {
                result = ((ColumnProjectionSegment) projectionSegment.get()).getColumn();
            }
        }
        if (!isFindInputColumn) {
            result = findInputColumnSegmentByVariables(segment, binderContext.getSqlStatement().getVariableNames()).orElse(null);
            isFindInputColumn = null != result;
        }
        if (!isFindInputColumn) {
            result = findInputColumnSegmentByPivotColumns(segment, binderContext.getPivotColumnNames()).orElse(null);
            isFindInputColumn = null != result;
        }
        ShardingSpherePreconditions.checkState(isFindInputColumn || containsFunctionTable(tableBinderContexts, outerTableBinderContexts.values()),
                () -> new ColumnNotFoundException(segment.getExpression(), SEGMENT_TYPE_MESSAGES.getOrDefault(parentSegmentType, UNKNOWN_SEGMENT_TYPE_MESSAGE)));
        return Optional.ofNullable(result);
    }
    
    private static ColumnSegment getColumnSegment(final ProjectionSegment projectionSegment) {
        if (projectionSegment instanceof ColumnProjectionSegment) {
            return ((ColumnProjectionSegment) projectionSegment).getColumn();
        }
        return null;
    }
    
    private static Optional<ColumnSegment> findInputColumnSegmentByPivotColumns(final ColumnSegment segment, final Collection<String> pivotColumnNames) {
        if (pivotColumnNames.isEmpty()) {
            return Optional.empty();
        }
        if (pivotColumnNames.contains(segment.getIdentifier().getValue())) {
            return Optional.of(new ColumnSegment(0, 0, segment.getIdentifier()));
        }
        return Optional.empty();
    }
    
    private static Optional<ProjectionSegment> findInputColumnSegmentFromOuterTable(final ColumnSegment segment,
                                                                                    final Multimap<CaseInsensitiveString, TableSegmentBinderContext> outerTableBinderContexts) {
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
    
    private static Optional<ProjectionSegment> findInputColumnSegmentFromExternalTables(final ColumnSegment segment,
                                                                                        final Multimap<CaseInsensitiveString, TableSegmentBinderContext> externalTableBinderContexts) {
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
        if (variableNames.contains(segment.getIdentifier().getValue())) {
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
    
    private static ColumnSegmentBoundInfo createColumnSegmentBoundInfo(final ColumnSegment segment, final ColumnSegment inputColumnSegment) {
        IdentifierValue originalDatabase = null == inputColumnSegment ? null : inputColumnSegment.getColumnBoundInfo().getOriginalDatabase();
        IdentifierValue originalSchema = null == inputColumnSegment ? null : inputColumnSegment.getColumnBoundInfo().getOriginalSchema();
        IdentifierValue segmentOriginalTable = segment.getColumnBoundInfo().getOriginalTable();
        IdentifierValue originalTable = Strings.isNullOrEmpty(segmentOriginalTable.getValue())
                ? Optional.ofNullable(inputColumnSegment).map(optional -> optional.getColumnBoundInfo().getOriginalTable()).orElse(segmentOriginalTable)
                : segmentOriginalTable;
        IdentifierValue segmentOriginalColumn = segment.getColumnBoundInfo().getOriginalColumn();
        IdentifierValue originalColumn = Optional.ofNullable(inputColumnSegment).map(optional -> optional.getColumnBoundInfo().getOriginalColumn()).orElse(segmentOriginalColumn);
        return new ColumnSegmentBoundInfo(new TableSegmentBoundInfo(originalDatabase, originalSchema), originalTable, originalColumn);
    }
    
    /**
     * Bind using column segment.
     *
     * @param segment using column segment
     * @param parentSegmentType parent segment type
     * @param tableBinderContexts table binder contexts
     * @return bound using column segment
     */
    public static ColumnSegment bindUsingColumn(final ColumnSegment segment, final SegmentType parentSegmentType,
                                                final Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts) {
        ColumnSegment result = copy(segment);
        List<ColumnSegment> usingInputColumnSegments = findUsingInputColumnSegments(segment.getIdentifier().getValue(), tableBinderContexts.values());
        ShardingSpherePreconditions.checkState(usingInputColumnSegments.size() >= 2,
                () -> new ColumnNotFoundException(segment.getExpression(), SEGMENT_TYPE_MESSAGES.getOrDefault(parentSegmentType, UNKNOWN_SEGMENT_TYPE_MESSAGE)));
        result.setColumnBoundInfo(createColumnSegmentBoundInfo(segment, usingInputColumnSegments.get(0)));
        result.setOtherUsingColumnBoundInfo(createColumnSegmentBoundInfo(segment, usingInputColumnSegments.get(1)));
        return result;
    }
    
    private static List<ColumnSegment> findUsingInputColumnSegments(final String columnName, final Collection<TableSegmentBinderContext> tableBinderContexts) {
        return tableBinderContexts.stream()
                .map(each -> each.findProjectionSegmentByColumnLabel(columnName))
                .filter(optional -> optional.isPresent() && optional.get() instanceof ColumnProjectionSegment)
                .map(each -> ((ColumnProjectionSegment) each.get()).getColumn()).collect(Collectors.toList());
    }
}
