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

package org.apache.shardingsphere.infra.binder.engine.segment.dml.from.type;

import com.cedarsoftware.util.CaseInsensitiveMap;
import com.cedarsoftware.util.CaseInsensitiveMap.CaseInsensitiveString;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.join.DialectJoinOption;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.binder.engine.segment.SegmentType;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.expression.ExpressionSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.expression.type.ColumnSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.TableSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.context.TableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.sql.parser.statement.core.enums.JoinType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.JoinTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SubqueryTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableSegment;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Join table segment binder.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JoinTableSegmentBinder {
    
    /**
     * Bind join table segment.
     *
     * @param segment join table segment
     * @param binderContext SQL statement binder context
     * @param tableBinderContexts table binder contexts
     * @param outerTableBinderContexts outer table binder contexts
     * @return bound join table segment
     */
    public static JoinTableSegment bind(final JoinTableSegment segment, final SQLStatementBinderContext binderContext,
                                        final Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts,
                                        final Multimap<CaseInsensitiveString, TableSegmentBinderContext> outerTableBinderContexts) {
        JoinTableSegment result = new JoinTableSegment();
        result.setStartIndex(segment.getStartIndex());
        result.setStopIndex(segment.getStopIndex());
        segment.getAliasSegment().ifPresent(result::setAlias);
        result.setNatural(segment.isNatural());
        result.setJoinType(segment.getJoinType());
        result.setLeft(TableSegmentBinder.bind(segment.getLeft(), binderContext, tableBinderContexts, outerTableBinderContexts));
        result.setRight(TableSegmentBinder.bind(segment.getRight(), binderContext, tableBinderContexts, outerTableBinderContexts));
        result.setCondition(ExpressionSegmentBinder.bind(segment.getCondition(), SegmentType.JOIN_ON, binderContext, tableBinderContexts, outerTableBinderContexts));
        result.setUsing(bindUsingColumns(segment.getUsing(), tableBinderContexts));
        result.getUsing().forEach(each -> binderContext.getUsingColumnNames().add(each.getIdentifier().getValue()));
        Map<String, ProjectionSegment> usingColumnsByNaturalJoin;
        if (result.isNatural()) {
            usingColumnsByNaturalJoin = getUsingColumnsByNaturalJoin(result, tableBinderContexts);
            Collection<ColumnSegment> derivedUsingColumns = getDerivedUsingColumns(usingColumnsByNaturalJoin);
            result.setDerivedUsing(bindUsingColumns(derivedUsingColumns, tableBinderContexts));
            result.getDerivedUsing().forEach(each -> binderContext.getUsingColumnNames().add(each.getIdentifier().getValue()));
        } else {
            usingColumnsByNaturalJoin = Collections.emptyMap();
        }
        result.getDerivedJoinTableProjectionSegments()
                .addAll(getDerivedJoinTableProjectionSegments(result, binderContext.getSqlStatement().getDatabaseType(), usingColumnsByNaturalJoin, tableBinderContexts));
        binderContext.getJoinTableProjectionSegments().addAll(result.getDerivedJoinTableProjectionSegments());
        return result;
    }
    
    private static Collection<ColumnSegment> getDerivedUsingColumns(final Map<String, ProjectionSegment> usingColumnsByNaturalJoin) {
        Collection<ColumnSegment> result = new LinkedList<>();
        for (ProjectionSegment each : usingColumnsByNaturalJoin.values()) {
            if (each instanceof ColumnProjectionSegment) {
                ColumnSegment column = ((ColumnProjectionSegment) each).getColumn();
                result.add(new ColumnSegment(column.getStartIndex(), column.getStopIndex(), column.getIdentifier()));
            }
        }
        return result;
    }
    
    private static List<ColumnSegment> bindUsingColumns(final Collection<ColumnSegment> usingColumns, final Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts) {
        List<ColumnSegment> result = new LinkedList<>();
        for (ColumnSegment each : usingColumns) {
            result.add(ColumnSegmentBinder.bindUsingColumn(each, SegmentType.JOIN_USING, tableBinderContexts));
        }
        return result;
    }
    
    private static Collection<ProjectionSegment> getDerivedJoinTableProjectionSegments(final JoinTableSegment segment, final DatabaseType databaseType,
                                                                                       final Map<String, ProjectionSegment> usingColumnsByNaturalJoin,
                                                                                       final Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts) {
        DialectJoinOption joinOrderOption = new DatabaseTypeRegistry(databaseType).getDialectDatabaseMetaData().getJoinOption();
        Collection<ProjectionSegment> projectionSegments = getProjectionSegments(segment, joinOrderOption, tableBinderContexts);
        if (segment.getUsing().isEmpty() && !segment.isNatural()) {
            return projectionSegments;
        }
        Collection<ProjectionSegment> result = new LinkedList<>();
        Map<String, ProjectionSegment> originalUsingColumns = segment.getUsing().isEmpty() ? usingColumnsByNaturalJoin : getUsingColumns(projectionSegments, segment.getUsing(), segment.getJoinType());
        Collection<ProjectionSegment> orderedUsingColumns = joinOrderOption.isUsingColumnsByProjectionOrder()
                ? getJoinUsingColumnsByProjectionOrder(projectionSegments, originalUsingColumns)
                : originalUsingColumns.values();
        result.addAll(orderedUsingColumns);
        result.addAll(getJoinRemainingColumns(projectionSegments, originalUsingColumns));
        return result;
    }
    
    private static Collection<ProjectionSegment> getProjectionSegments(final JoinTableSegment segment, final DialectJoinOption joinOrderOption,
                                                                       final Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts) {
        Collection<ProjectionSegment> result = new LinkedList<>();
        if (joinOrderOption.isRightColumnsByFirstOrder() && JoinType.RIGHT.name().equalsIgnoreCase(segment.getJoinType()) && (!segment.getUsing().isEmpty() || segment.isNatural())) {
            result.addAll(getProjectionSegments(segment.getRight(), tableBinderContexts));
            result.addAll(getProjectionSegments(segment.getLeft(), tableBinderContexts));
        } else {
            result.addAll(getProjectionSegments(segment.getLeft(), tableBinderContexts));
            result.addAll(getProjectionSegments(segment.getRight(), tableBinderContexts));
        }
        return result;
    }
    
    private static Collection<ProjectionSegment> getProjectionSegments(final TableSegment tableSegment, final Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts) {
        Collection<ProjectionSegment> result = new LinkedList<>();
        if (tableSegment instanceof SimpleTableSegment) {
            String tableAliasOrName = tableSegment.getAliasName().orElseGet(() -> ((SimpleTableSegment) tableSegment).getTableName().getIdentifier().getValue());
            result.addAll(getProjectionSegmentsByTableAliasOrName(tableBinderContexts, tableAliasOrName));
        } else if (tableSegment instanceof JoinTableSegment) {
            result.addAll(((JoinTableSegment) tableSegment).getDerivedJoinTableProjectionSegments());
        } else if (tableSegment instanceof SubqueryTableSegment) {
            result.addAll(getProjectionSegmentsByTableAliasOrName(tableBinderContexts, tableSegment.getAliasName().orElse("")));
        }
        return result;
    }
    
    private static Collection<ProjectionSegment> getProjectionSegmentsByTableAliasOrName(final Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts,
                                                                                         final String tableAliasOrName) {
        ShardingSpherePreconditions.checkNotNull(tableAliasOrName, () -> new IllegalStateException("Table alias or name for shorthand projection segment owner can not be null."));
        ShardingSpherePreconditions.checkContains(tableBinderContexts.keySet(), CaseInsensitiveString.of(tableAliasOrName),
                () -> new IllegalStateException(String.format("Can not find table binder context by table alias or name %s.", tableAliasOrName)));
        return tableBinderContexts.get(CaseInsensitiveString.of(tableAliasOrName)).iterator().next().getProjectionSegments();
    }
    
    private static Map<String, ProjectionSegment> getUsingColumnsByNaturalJoin(final JoinTableSegment segment, final Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts) {
        Map<String, ProjectionSegment> result = new CaseInsensitiveMap<>();
        Collection<ProjectionSegment> leftProjections = getProjectionSegments(segment.getLeft(), tableBinderContexts);
        Map<String, ProjectionSegment> rightProjections = new CaseInsensitiveMap<>();
        getProjectionSegments(segment.getRight(), tableBinderContexts).forEach(each -> rightProjections.put(each.getColumnLabel(), each));
        for (ProjectionSegment each : leftProjections) {
            String columnLabel = each.getColumnLabel();
            if (rightProjections.containsKey(columnLabel)) {
                result.put(columnLabel, each);
            }
        }
        return result;
    }
    
    private static Map<String, ProjectionSegment> getUsingColumns(final Collection<ProjectionSegment> projectionSegments, final Collection<ColumnSegment> usingColumns, final String joinType) {
        Multimap<CaseInsensitiveString, ProjectionSegment> columnLabelProjectionSegments = LinkedHashMultimap.create();
        for (ProjectionSegment projectionSegment : projectionSegments) {
            if (null != projectionSegment.getColumnLabel()) {
                columnLabelProjectionSegments.put(CaseInsensitiveString.of(projectionSegment.getColumnLabel()), projectionSegment);
            }
        }
        Map<String, ProjectionSegment> result = new CaseInsensitiveMap<>();
        for (ColumnSegment each : usingColumns) {
            LinkedList<ProjectionSegment> groupProjectionSegments = new LinkedList<>(columnLabelProjectionSegments.get(CaseInsensitiveString.of(each.getIdentifier().getValue())));
            if (!groupProjectionSegments.isEmpty()) {
                ProjectionSegment targetProjectionSegment =
                        JoinType.RIGHT.name().equalsIgnoreCase(joinType) ? groupProjectionSegments.descendingIterator().next() : groupProjectionSegments.iterator().next();
                result.put(targetProjectionSegment.getColumnLabel(), targetProjectionSegment);
            }
        }
        return result;
    }
    
    private static Collection<ProjectionSegment> getJoinUsingColumnsByProjectionOrder(final Collection<ProjectionSegment> projectionSegments, final Map<String, ProjectionSegment> usingColumns) {
        Map<String, ProjectionSegment> result = new CaseInsensitiveMap<>(usingColumns.size(), 1F);
        for (ProjectionSegment each : projectionSegments) {
            String columnLabel = each.getColumnLabel();
            if (!result.containsKey(columnLabel) && usingColumns.containsKey(columnLabel)) {
                result.put(columnLabel, each);
            }
        }
        return result.values();
    }
    
    private static Collection<ProjectionSegment> getJoinRemainingColumns(final Collection<ProjectionSegment> projectionSegments, final Map<String, ProjectionSegment> usingColumns) {
        Collection<ProjectionSegment> result = new LinkedList<>();
        for (ProjectionSegment each : projectionSegments) {
            if (!usingColumns.containsKey(each.getColumnLabel())) {
                result.add(each);
            }
        }
        return result;
    }
}
