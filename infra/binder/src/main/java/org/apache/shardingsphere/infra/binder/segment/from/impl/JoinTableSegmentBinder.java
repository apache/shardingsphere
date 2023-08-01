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

package org.apache.shardingsphere.infra.binder.segment.from.impl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.binder.segment.from.TableSegmentBinder;
import org.apache.shardingsphere.infra.binder.segment.from.TableSegmentBinderContext;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.database.mysql.type.MySQLDatabaseType;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.sql.parser.sql.common.enums.JoinType;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.JoinTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SubqueryTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableSegment;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Join table segment binder.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JoinTableSegmentBinder {
    
    /**
     * Bind join table segment with metadata.
     *
     * @param segment join table segment
     * @param metaData meta data
     * @param defaultDatabaseName default database name
     * @param databaseType database type
     * @param tableBinderContexts table binder contexts
     * @return bounded join table segment
     */
    public static JoinTableSegment bind(final JoinTableSegment segment, final ShardingSphereMetaData metaData, final String defaultDatabaseName,
                                        final DatabaseType databaseType, final Map<String, TableSegmentBinderContext> tableBinderContexts) {
        JoinTableSegment result = new JoinTableSegment();
        result.setStartIndex(segment.getStartIndex());
        result.setStopIndex(segment.getStopIndex());
        segment.getAliasSegment().ifPresent(result::setAlias);
        result.setLeft(TableSegmentBinder.bind(segment.getLeft(), metaData, defaultDatabaseName, databaseType, tableBinderContexts));
        result.setNatural(segment.isNatural());
        result.setJoinType(segment.getJoinType());
        result.setRight(TableSegmentBinder.bind(segment.getRight(), metaData, defaultDatabaseName, databaseType, tableBinderContexts));
        result.setCondition(segment.getCondition());
        // TODO bind condition and using column in join table segment
        result.setUsing(segment.getUsing());
        result.getJoinTableProjectionSegments().addAll(getJoinTableProjectionSegments(segment, databaseType, tableBinderContexts));
        return result;
    }
    
    private static Collection<ProjectionSegment> getJoinTableProjectionSegments(final JoinTableSegment segment, final DatabaseType databaseType,
                                                                                final Map<String, TableSegmentBinderContext> tableBinderContexts) {
        Collection<ProjectionSegment> projectionSegments = getProjectionSegments(segment, databaseType, tableBinderContexts);
        if (segment.getUsing().isEmpty() && !segment.isNatural()) {
            return projectionSegments;
        }
        Collection<ProjectionSegment> result = new LinkedList<>();
        Map<String, ProjectionSegment> originalUsingColumns =
                segment.getUsing().isEmpty() ? getUsingColumnsByNaturalJoin(segment, tableBinderContexts) : getUsingColumns(projectionSegments, segment.getUsing());
        Collection<ProjectionSegment> orderedUsingColumns =
                databaseType instanceof MySQLDatabaseType ? getJoinUsingColumnsByProjectionOrder(projectionSegments, originalUsingColumns) : originalUsingColumns.values();
        result.addAll(orderedUsingColumns);
        result.addAll(getJoinRemainingColumns(projectionSegments, originalUsingColumns));
        return result;
    }
    
    private static Collection<ProjectionSegment> getProjectionSegments(final JoinTableSegment segment, final DatabaseType databaseType,
                                                                       final Map<String, TableSegmentBinderContext> tableBinderContexts) {
        Collection<ProjectionSegment> result = new LinkedList<>();
        if (databaseType instanceof MySQLDatabaseType && JoinType.RIGHT.name().equalsIgnoreCase(segment.getJoinType()) && (!segment.getUsing().isEmpty() || segment.isNatural())) {
            result.addAll(getProjectionSegments(segment.getRight(), tableBinderContexts));
            result.addAll(getProjectionSegments(segment.getLeft(), tableBinderContexts));
        } else {
            result.addAll(getProjectionSegments(segment.getLeft(), tableBinderContexts));
            result.addAll(getProjectionSegments(segment.getRight(), tableBinderContexts));
        }
        return result;
    }
    
    private static Collection<ProjectionSegment> getProjectionSegments(final TableSegment tableSegment, final Map<String, TableSegmentBinderContext> tableBinderContexts) {
        Collection<ProjectionSegment> result = new LinkedList<>();
        if (tableSegment instanceof SimpleTableSegment) {
            String tableAliasOrName = tableSegment.getAliasName().orElseGet(() -> ((SimpleTableSegment) tableSegment).getTableName().getIdentifier().getValue());
            result.addAll(getProjectionSegmentsByTableAliasOrName(tableBinderContexts, tableAliasOrName));
        } else if (tableSegment instanceof JoinTableSegment) {
            result.addAll(((JoinTableSegment) tableSegment).getJoinTableProjectionSegments());
        } else if (tableSegment instanceof SubqueryTableSegment) {
            result.addAll(getProjectionSegmentsByTableAliasOrName(tableBinderContexts, tableSegment.getAliasName().orElse("")));
        }
        return result;
    }
    
    private static Collection<ProjectionSegment> getProjectionSegmentsByTableAliasOrName(final Map<String, TableSegmentBinderContext> tableBinderContexts, final String tableAliasOrName) {
        ShardingSpherePreconditions.checkState(tableBinderContexts.containsKey(tableAliasOrName),
                () -> new IllegalStateException(String.format("Can not find table binder context by table alias or name %s.", tableAliasOrName)));
        return tableBinderContexts.get(tableAliasOrName).getProjectionSegments();
    }
    
    private static Map<String, ProjectionSegment> getUsingColumnsByNaturalJoin(final JoinTableSegment segment, final Map<String, TableSegmentBinderContext> tableBinderContexts) {
        Map<String, ProjectionSegment> result = new LinkedHashMap<>();
        Collection<ProjectionSegment> leftProjections = getProjectionSegments(segment.getLeft(), tableBinderContexts);
        Map<String, ProjectionSegment> rightProjections = new LinkedHashMap<>();
        getProjectionSegments(segment.getRight(), tableBinderContexts).forEach(each -> rightProjections.put(each.getColumnLabel().toLowerCase(), each));
        for (ProjectionSegment each : leftProjections) {
            String columnLabel = each.getColumnLabel().toLowerCase();
            if (rightProjections.containsKey(columnLabel)) {
                result.put(columnLabel, each);
            }
        }
        return result;
    }
    
    private static Map<String, ProjectionSegment> getUsingColumns(final Collection<ProjectionSegment> projectionSegments, final Collection<ColumnSegment> usingColumns) {
        Map<String, ProjectionSegment> columnLabelProjectionSegments = new LinkedHashMap<>(projectionSegments.size(), 1F);
        projectionSegments.forEach(each -> columnLabelProjectionSegments.putIfAbsent(each.getColumnLabel().toLowerCase(), each));
        Map<String, ProjectionSegment> result = new LinkedHashMap<>();
        for (ColumnSegment each : usingColumns) {
            ProjectionSegment projectionSegment = columnLabelProjectionSegments.get(each.getIdentifier().getValue().toLowerCase());
            if (null != projectionSegment) {
                result.put(projectionSegment.getColumnLabel().toLowerCase(), projectionSegment);
            }
        }
        return result;
    }
    
    private static Collection<ProjectionSegment> getJoinUsingColumnsByProjectionOrder(final Collection<ProjectionSegment> projectionSegments,
                                                                                      final Map<String, ProjectionSegment> usingColumns) {
        Map<String, ProjectionSegment> result = new LinkedHashMap<>(usingColumns.size(), 1F);
        for (ProjectionSegment each : projectionSegments) {
            String columnLabel = each.getColumnLabel().toLowerCase();
            if (!result.containsKey(columnLabel) && usingColumns.containsKey(columnLabel)) {
                result.put(columnLabel, each);
            }
        }
        return result.values();
    }
    
    private static Collection<ProjectionSegment> getJoinRemainingColumns(final Collection<ProjectionSegment> projectionSegments, final Map<String, ProjectionSegment> usingColumns) {
        Collection<ProjectionSegment> result = new LinkedList<>();
        for (ProjectionSegment each : projectionSegments) {
            if (!usingColumns.containsKey(each.getColumnLabel().toLowerCase())) {
                result.add(each);
            }
        }
        return result;
    }
}
