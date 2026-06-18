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

package org.apache.shardingsphere.infra.binder.engine.segment.dml.projection.type;

import com.cedarsoftware.util.CaseInsensitiveMap.CaseInsensitiveString;
import com.google.common.collect.Multimap;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.context.TableSegmentBinderContext;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ShorthandProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.PivotSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.bound.ColumnSegmentBoundInfo;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.JoinTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SubqueryTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableSegment;

import java.util.Collection;
import java.util.Optional;

/**
 * Shorthand projection segment binder.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShorthandProjectionSegmentBinder {
    
    /**
     * Bind shorthand projection segment.
     *
     * @param segment table segment
     * @param boundTableSegment bound table segment
     * @param tableBinderContexts table binder contexts
     * @return bound shorthand projection segment
     */
    public static ShorthandProjectionSegment bind(final ShorthandProjectionSegment segment, final TableSegment boundTableSegment,
                                                  final Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts) {
        ShorthandProjectionSegment result = copy(segment);
        if (segment.getOwner().isPresent()) {
            if (isOwnerOfTable(segment.getOwner().get().getIdentifier().getValue(), boundTableSegment) && expandPivotColumns(boundTableSegment, tableBinderContexts, result)) {
                return result;
            }
            expandVisibleColumns(getProjectionSegmentsByTableAliasOrName(tableBinderContexts, segment.getOwner().get().getIdentifier().getValue()), result);
        } else {
            expandNoOwnerProjections(boundTableSegment, tableBinderContexts, result);
        }
        return result;
    }
    
    private static ShorthandProjectionSegment copy(final ShorthandProjectionSegment segment) {
        ShorthandProjectionSegment result = new ShorthandProjectionSegment(segment.getStartIndex(), segment.getStopIndex());
        segment.getOwner().ifPresent(result::setOwner);
        segment.getAliasSegment().ifPresent(result::setAlias);
        return result;
    }
    
    private static Collection<ProjectionSegment> getProjectionSegmentsByTableAliasOrName(final Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts,
                                                                                         final String tableAliasOrName) {
        ShardingSpherePreconditions.checkNotNull(tableAliasOrName, () -> new IllegalStateException("Table alias or name for shorthand projection segment owner can not be null."));
        ShardingSpherePreconditions.checkContains(tableBinderContexts.keySet(), CaseInsensitiveString.of(tableAliasOrName),
                () -> new IllegalStateException(String.format("Can not find table binder context by table alias or name %s.", tableAliasOrName)));
        return tableBinderContexts.get(CaseInsensitiveString.of(tableAliasOrName)).iterator().next().getProjectionSegments();
    }
    
    private static void expandVisibleColumns(final Collection<ProjectionSegment> projectionSegments, final ShorthandProjectionSegment segment) {
        for (ProjectionSegment each : projectionSegments) {
            if (each.isVisible()) {
                segment.getActualProjectionSegments().add(each);
            }
        }
    }
    
    private static void expandNoOwnerProjections(final TableSegment boundTableSegment, final Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts,
                                                 final ShorthandProjectionSegment segment) {
        if (expandPivotColumns(boundTableSegment, tableBinderContexts, segment)) {
            return;
        }
        if (boundTableSegment instanceof SimpleTableSegment) {
            String tableAliasOrName = boundTableSegment.getAliasName().orElseGet(() -> ((SimpleTableSegment) boundTableSegment).getTableName().getIdentifier().getValue());
            expandVisibleColumns(getProjectionSegmentsByTableAliasOrName(tableBinderContexts, tableAliasOrName), segment);
        } else if (boundTableSegment instanceof JoinTableSegment) {
            expandVisibleColumns(((JoinTableSegment) boundTableSegment).getDerivedJoinTableProjectionSegments(), segment);
        } else if (boundTableSegment instanceof SubqueryTableSegment) {
            expandVisibleColumns(getProjectionSegmentsByTableAliasOrName(tableBinderContexts, boundTableSegment.getAliasName().orElse("")), segment);
        }
    }
    
    private static boolean isOwnerOfTable(final String owner, final TableSegment boundTableSegment) {
        return boundTableSegment.getAliasName().map(optional -> optional.equalsIgnoreCase(owner)).orElse(false)
                || boundTableSegment instanceof SimpleTableSegment && ((SimpleTableSegment) boundTableSegment).getTableName().getIdentifier().getValue().equalsIgnoreCase(owner);
    }
    
    private static boolean expandPivotColumns(final TableSegment boundTableSegment, final Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts,
                                              final ShorthandProjectionSegment segment) {
        Optional<PivotSegment> pivotSegment = getPivotSegment(boundTableSegment);
        if (!pivotSegment.isPresent() || pivotSegment.get().isUnPivot()) {
            return false;
        }
        Collection<ProjectionSegment> projectionSegments = getProjectionSegmentsByTableSegment(boundTableSegment, tableBinderContexts);
        for (ProjectionSegment each : projectionSegments) {
            if (each.isVisible() && !isPivotInputColumn(each, pivotSegment.get())) {
                segment.getActualProjectionSegments().add(each);
            }
        }
        return true;
    }
    
    private static Optional<PivotSegment> getPivotSegment(final TableSegment boundTableSegment) {
        if (boundTableSegment instanceof SimpleTableSegment) {
            return ((SimpleTableSegment) boundTableSegment).getPivot();
        }
        if (boundTableSegment instanceof SubqueryTableSegment) {
            return ((SubqueryTableSegment) boundTableSegment).getPivot();
        }
        return Optional.empty();
    }
    
    private static Collection<ProjectionSegment> getProjectionSegmentsByTableSegment(final TableSegment boundTableSegment,
                                                                                     final Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts) {
        String tableAliasOrName = boundTableSegment instanceof SimpleTableSegment
                ? boundTableSegment.getAliasName().orElseGet(() -> ((SimpleTableSegment) boundTableSegment).getTableName().getIdentifier().getValue())
                : boundTableSegment.getAliasName().orElse("");
        return getProjectionSegmentsByTableAliasOrName(tableBinderContexts, tableAliasOrName);
    }
    
    private static boolean isPivotInputColumn(final ProjectionSegment projectionSegment, final PivotSegment pivotSegment) {
        if (!(projectionSegment instanceof ColumnProjectionSegment)) {
            return false;
        }
        ColumnSegment columnSegment = ((ColumnProjectionSegment) projectionSegment).getColumn();
        return containsColumn(pivotSegment.getPivotForColumns(), columnSegment) || containsColumn(pivotSegment.getPivotAggregationColumns(), columnSegment);
    }
    
    private static boolean containsColumn(final Collection<ColumnSegment> columns, final ColumnSegment columnSegment) {
        for (ColumnSegment each : columns) {
            if (isSameColumn(each, columnSegment)) {
                return true;
            }
        }
        return false;
    }
    
    private static boolean isSameColumn(final ColumnSegment left, final ColumnSegment right) {
        ColumnSegmentBoundInfo leftBoundInfo = left.getColumnBoundInfo();
        ColumnSegmentBoundInfo rightBoundInfo = right.getColumnBoundInfo();
        if (null != leftBoundInfo && null != rightBoundInfo) {
            return leftBoundInfo.getOriginalDatabase().equals(rightBoundInfo.getOriginalDatabase()) && leftBoundInfo.getOriginalSchema().equals(rightBoundInfo.getOriginalSchema())
                    && leftBoundInfo.getOriginalTable().equals(rightBoundInfo.getOriginalTable()) && leftBoundInfo.getOriginalColumn().equals(rightBoundInfo.getOriginalColumn());
        }
        return left.getIdentifier().getValue().equalsIgnoreCase(right.getIdentifier().getValue());
    }
}
