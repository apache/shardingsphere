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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.apache.groovy.util.Maps;
import org.apache.shardingsphere.infra.binder.engine.segment.SegmentType;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.context.TableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.context.type.FunctionTableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.context.type.SimpleTableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.kernel.metadata.ColumnNotFoundException;
import org.apache.shardingsphere.infra.exception.kernel.syntax.AmbiguousColumnException;
import org.apache.shardingsphere.sql.parser.statement.core.enums.TableSourceType;
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

/**
 * Column segment binder.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ColumnSegmentBinder {
    
    private static final Collection<String> EXCLUDE_BIND_COLUMNS = new CaseInsensitiveSet<>(Arrays.asList(
            "ROWNUM", "ROW_NUMBER", "ROWNUM_", "ROWID", "SYSDATE", "SYSTIMESTAMP", "CURRENT_TIMESTAMP", "LOCALTIMESTAMP", "UID", "USER", "NEXTVAL", "LEVEL", "DAY"));
    
    private static final Map<SegmentType, String> SEGMENT_TYPE_MESSAGES = Maps.of(SegmentType.PROJECTION, "field list", SegmentType.JOIN_ON, "on clause", SegmentType.JOIN_USING, "from clause",
            SegmentType.PREDICATE, "where clause", SegmentType.HAVING, "having clause", SegmentType.ORDER_BY, "order clause", SegmentType.GROUP_BY, "group statement", SegmentType.INSERT_COLUMNS,
            "field list");
    
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
        ColumnSegmentInfo columnSegmentInfo = getColumnSegmentInfo(segment, parentSegmentType, tableSegmentBinderContexts, outerTableBinderContexts, binderContext);
        Optional<ColumnSegment> inputColumnSegment = columnSegmentInfo.getInputColumnSegment();
        inputColumnSegment.ifPresent(optional -> result.setVariable(optional.isVariable()));
        segment.getOwner().ifPresent(optional -> result.setOwner(bindOwnerTableContext(optional, inputColumnSegment.orElse(null))));
        result.setColumnBoundInfo(createColumnSegmentBoundInfo(segment, inputColumnSegment.orElse(null), columnSegmentInfo.getTableSourceType()));
        return result;
    }
    
    private static ColumnSegment copy(final ColumnSegment segment) {
        ColumnSegment result = new ColumnSegment(segment.getStartIndex(), segment.getStopIndex(), segment.getIdentifier());
        result.setNestedObjectAttributes(segment.getNestedObjectAttributes());
        result.setVariable(segment.isVariable());
        segment.getLeftParentheses().ifPresent(result::setLeftParentheses);
        segment.getRightParentheses().ifPresent(result::setRightParentheses);
        return result;
    }
    
    private static OwnerSegment bindOwnerTableContext(final OwnerSegment owner, final ColumnSegment inputColumnSegment) {
        IdentifierValue originalDatabase = null == inputColumnSegment ? null : inputColumnSegment.getColumnBoundInfo().getOriginalDatabase();
        IdentifierValue originalSchema = null == inputColumnSegment ? null : inputColumnSegment.getColumnBoundInfo().getOriginalSchema();
        if (null != originalDatabase && null != originalSchema) {
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
            return Collections.singleton(new SimpleTableSegmentBinderContext(binderContext.getJoinTableProjectionSegments(), TableSourceType.TEMPORARY_TABLE));
        }
        return tableBinderContexts.values();
    }
    
    private static Collection<TableSegmentBinderContext> getTableBinderContextByOwner(final String owner, final Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts,
                                                                                      final Multimap<CaseInsensitiveString, TableSegmentBinderContext> outerTableBinderContexts,
                                                                                      final Multimap<CaseInsensitiveString, TableSegmentBinderContext> externalTableBinderContexts) {
        if (null == owner) {
            return Collections.emptyList();
        }
        CaseInsensitiveString caseInsensitiveOwner = CaseInsensitiveString.of(owner);
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
    
    private static ColumnSegmentInfo getColumnSegmentInfo(final ColumnSegment segment, final SegmentType parentSegmentType, final Collection<TableSegmentBinderContext> tableBinderContexts,
                                                          final Multimap<CaseInsensitiveString, TableSegmentBinderContext> outerTableBinderContexts,
                                                          final SQLStatementBinderContext binderContext) {
        ColumnSegmentInfo result = getInputInfoFromTableBinderContexts(tableBinderContexts, segment, parentSegmentType);
        if (!result.getInputColumnSegment().isPresent()) {
            result = new ColumnSegmentInfo(findInputColumnSegmentFromOuterTable(segment, outerTableBinderContexts).orElse(null), TableSourceType.TEMPORARY_TABLE);
        }
        if (!result.getInputColumnSegment().isPresent()) {
            result = new ColumnSegmentInfo(findInputColumnSegmentFromExternalTables(segment, binderContext.getExternalTableBinderContexts()).orElse(null), TableSourceType.TEMPORARY_TABLE);
        }
        if (!result.getInputColumnSegment().isPresent()) {
            result = new ColumnSegmentInfo(findInputColumnSegmentByVariables(segment, binderContext.getSqlStatement().getVariableNames()).orElse(null), TableSourceType.TEMPORARY_TABLE);
        }
        if (!result.getInputColumnSegment().isPresent()) {
            result = new ColumnSegmentInfo(findInputColumnSegmentByPivotColumns(segment, binderContext.getPivotColumnNames()).orElse(null), TableSourceType.TEMPORARY_TABLE);
        }
        ShardingSpherePreconditions.checkState(result.getInputColumnSegment().isPresent() || isSkipColumnBind(tableBinderContexts, outerTableBinderContexts.values()),
                () -> new ColumnNotFoundException(segment.getExpression(), SEGMENT_TYPE_MESSAGES.getOrDefault(parentSegmentType, UNKNOWN_SEGMENT_TYPE_MESSAGE)));
        return result;
    }
    
    private static ColumnSegmentInfo getInputInfoFromTableBinderContexts(final Collection<TableSegmentBinderContext> tableBinderContexts,
                                                                         final ColumnSegment segment, final SegmentType parentSegmentType) {
        ColumnSegment inputColumnSegment = null;
        TableSourceType tableSourceType = TableSourceType.TEMPORARY_TABLE;
        for (TableSegmentBinderContext each : tableBinderContexts) {
            Optional<ProjectionSegment> projectionSegment = each.findProjectionSegmentByColumnLabel(segment.getIdentifier().getValue());
            if (!projectionSegment.isPresent()) {
                continue;
            }
            if (projectionSegment.get() instanceof ColumnProjectionSegment) {
                ShardingSpherePreconditions.checkState(null == inputColumnSegment,
                        () -> new AmbiguousColumnException(segment.getExpression(), SEGMENT_TYPE_MESSAGES.getOrDefault(parentSegmentType, UNKNOWN_SEGMENT_TYPE_MESSAGE)));
            }
            inputColumnSegment = getColumnSegment(projectionSegment.get());
            tableSourceType = TableSourceType.MIXED_TABLE == each.getTableSourceType() ? getTableSourceTypeFromInputColumn(inputColumnSegment) : each.getTableSourceType();
            if (each instanceof SimpleTableSegmentBinderContext && ((SimpleTableSegmentBinderContext) each).isFromWithSegment()) {
                break;
            }
        }
        return new ColumnSegmentInfo(inputColumnSegment, tableSourceType);
    }
    
    private static TableSourceType getTableSourceTypeFromInputColumn(final ColumnSegment inputColumnSegment) {
        return null == inputColumnSegment ? TableSourceType.TEMPORARY_TABLE : inputColumnSegment.getColumnBoundInfo().getTableSourceType();
    }
    
    private static ColumnSegment getColumnSegment(final ProjectionSegment projectionSegment) {
        if (projectionSegment instanceof ColumnProjectionSegment) {
            return ((ColumnProjectionSegment) projectionSegment).getColumn();
        }
        return new ColumnSegment(0, 0, new IdentifierValue(projectionSegment.getColumnLabel()));
    }
    
    private static Optional<ColumnSegment> findInputColumnSegmentByPivotColumns(final ColumnSegment segment, final Collection<String> pivotColumnNames) {
        return pivotColumnNames.isEmpty() || !pivotColumnNames.contains(segment.getIdentifier().getValue()) ? Optional.empty() : Optional.of(new ColumnSegment(0, 0, segment.getIdentifier()));
    }
    
    private static Optional<ColumnSegment> findInputColumnSegmentFromOuterTable(final ColumnSegment segment,
                                                                                final Multimap<CaseInsensitiveString, TableSegmentBinderContext> outerTableBinderContexts) {
        ListIterator<TableSegmentBinderContext> listIterator = new ArrayList<>(outerTableBinderContexts.values()).listIterator(outerTableBinderContexts.size());
        while (listIterator.hasPrevious()) {
            TableSegmentBinderContext each = listIterator.previous();
            Optional<ProjectionSegment> result = each.findProjectionSegmentByColumnLabel(segment.getIdentifier().getValue());
            if (result.isPresent()) {
                return Optional.of(createColumnSegment(result.get()));
            }
        }
        return Optional.empty();
    }
    
    private static ColumnSegment createColumnSegment(final ProjectionSegment projectionSegment) {
        return projectionSegment instanceof ColumnProjectionSegment ? ((ColumnProjectionSegment) projectionSegment).getColumn()
                : new ColumnSegment(0, 0, new IdentifierValue(projectionSegment.getColumnLabel()));
    }
    
    private static Optional<ColumnSegment> findInputColumnSegmentFromExternalTables(final ColumnSegment segment,
                                                                                    final Multimap<CaseInsensitiveString, TableSegmentBinderContext> externalTableBinderContexts) {
        for (TableSegmentBinderContext each : externalTableBinderContexts.values()) {
            Optional<ProjectionSegment> result = each.findProjectionSegmentByColumnLabel(segment.getIdentifier().getValue());
            if (result.isPresent()) {
                return Optional.of(createColumnSegment(result.get()));
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
    
    private static boolean isSkipColumnBind(final Collection<TableSegmentBinderContext> tableBinderContexts, final Collection<TableSegmentBinderContext> outerBinderContexts) {
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
        List<ColumnSegmentInfo> usingColumnSegmentInfos = findUsingColumnSegmentInfos(tableBinderContexts.values(), segment.getIdentifier().getValue());
        ShardingSpherePreconditions.checkState(usingColumnSegmentInfos.size() >= 2,
                () -> new ColumnNotFoundException(segment.getExpression(), SEGMENT_TYPE_MESSAGES.getOrDefault(parentSegmentType, UNKNOWN_SEGMENT_TYPE_MESSAGE)));
        ColumnSegmentInfo usingColumnInputInfo = usingColumnSegmentInfos.get(0);
        ColumnSegmentInfo otherUsingColumnInputInfo = usingColumnSegmentInfos.get(1);
        result.setColumnBoundInfo(createColumnSegmentBoundInfo(segment, usingColumnInputInfo.getInputColumnSegment().orElse(null), usingColumnInputInfo.getTableSourceType()));
        result.setOtherUsingColumnBoundInfo(createColumnSegmentBoundInfo(segment, otherUsingColumnInputInfo.getInputColumnSegment().orElse(null), otherUsingColumnInputInfo.getTableSourceType()));
        return result;
    }
    
    private static List<ColumnSegmentInfo> findUsingColumnSegmentInfos(final Collection<TableSegmentBinderContext> tableBinderContexts, final String columnName) {
        List<ColumnSegmentInfo> result = new ArrayList<>(tableBinderContexts.size());
        for (TableSegmentBinderContext each : tableBinderContexts) {
            Optional<ProjectionSegment> projectionSegment = each.findProjectionSegmentByColumnLabel(columnName);
            if (!projectionSegment.isPresent()) {
                continue;
            }
            ColumnSegment columnSegment = projectionSegment.get() instanceof ColumnProjectionSegment ? ((ColumnProjectionSegment) projectionSegment.get()).getColumn() : null;
            result.add(new ColumnSegmentInfo(columnSegment, each.getTableSourceType()));
        }
        return result;
    }
    
    /**
     * Create column segment bound info.
     *
     * @param segment column segment
     * @param inputColumnSegment input column segment
     * @param tableSourceType table source type
     * @return created column segment bound info
     */
    public static ColumnSegmentBoundInfo createColumnSegmentBoundInfo(final ColumnSegment segment, final ColumnSegment inputColumnSegment, final TableSourceType tableSourceType) {
        IdentifierValue originalDatabase = null == inputColumnSegment ? null : inputColumnSegment.getColumnBoundInfo().getOriginalDatabase();
        IdentifierValue originalSchema = null == inputColumnSegment ? null : inputColumnSegment.getColumnBoundInfo().getOriginalSchema();
        IdentifierValue segmentOriginalTable = segment.getColumnBoundInfo().getOriginalTable();
        IdentifierValue originalTable = Strings.isNullOrEmpty(segmentOriginalTable.getValue())
                ? Optional.ofNullable(inputColumnSegment).map(optional -> optional.getColumnBoundInfo().getOriginalTable()).orElse(segmentOriginalTable)
                : segmentOriginalTable;
        IdentifierValue segmentOriginalColumn = segment.getColumnBoundInfo().getOriginalColumn();
        IdentifierValue originalColumn = Optional.ofNullable(inputColumnSegment).map(optional -> optional.getColumnBoundInfo().getOriginalColumn()).orElse(segmentOriginalColumn);
        return new ColumnSegmentBoundInfo(new TableSegmentBoundInfo(originalDatabase, originalSchema), originalTable, originalColumn, tableSourceType);
    }
    
    @RequiredArgsConstructor
    @Getter
    private static class ColumnSegmentInfo {
        
        private final ColumnSegment inputColumnSegment;
        
        private final TableSourceType tableSourceType;
        
        Optional<ColumnSegment> getInputColumnSegment() {
            return Optional.ofNullable(inputColumnSegment);
        }
    }
}
