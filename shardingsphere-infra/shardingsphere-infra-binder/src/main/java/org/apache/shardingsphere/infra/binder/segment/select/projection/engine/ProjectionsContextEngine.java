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

package org.apache.shardingsphere.infra.binder.segment.select.projection.engine;

import org.apache.shardingsphere.infra.binder.segment.select.groupby.GroupByContext;
import org.apache.shardingsphere.infra.binder.segment.select.orderby.OrderByContext;
import org.apache.shardingsphere.infra.binder.segment.select.orderby.OrderByItem;
import org.apache.shardingsphere.infra.binder.segment.select.projection.DerivedColumn;
import org.apache.shardingsphere.infra.binder.segment.select.projection.Projection;
import org.apache.shardingsphere.infra.binder.segment.select.projection.ProjectionsContext;
import org.apache.shardingsphere.infra.binder.segment.select.projection.impl.ColumnProjection;
import org.apache.shardingsphere.infra.binder.segment.select.projection.impl.DerivedProjection;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.item.ColumnOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.item.IndexOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.item.OrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.item.TextOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.util.SQLUtil;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Projections context engine.
 */
public final class ProjectionsContextEngine {
    
    private final ProjectionEngine projectionEngine;
    
    public ProjectionsContextEngine(final ShardingSphereSchema schema, final DatabaseType databaseType) {
        projectionEngine = new ProjectionEngine(schema, databaseType);
    }
    
    /**
     * Create projections context.
     *
     * @param table table segment
     * @param projectionsSegment projection segments
     * @param groupByContext group by context
     * @param orderByContext order by context
     * @return projections context
     */
    public ProjectionsContext createProjectionsContext(final TableSegment table, final ProjectionsSegment projectionsSegment, 
                                                       final GroupByContext groupByContext, final OrderByContext orderByContext) {
        Collection<Projection> projections = getProjections(table, projectionsSegment);
        ProjectionsContext result = new ProjectionsContext(projectionsSegment.getStartIndex(), projectionsSegment.getStopIndex(), projectionsSegment.isDistinctRow(), projections);
        result.getProjections().addAll(getDerivedGroupByColumns(groupByContext, projections));
        result.getProjections().addAll(getDerivedOrderByColumns(orderByContext, projections));
        return result;
    }
    
    private Collection<Projection> getProjections(final TableSegment table, final ProjectionsSegment projectionsSegment) {
        Collection<Projection> result = new LinkedList<>();
        for (ProjectionSegment each : projectionsSegment.getProjections()) {
            projectionEngine.createProjection(table, each).ifPresent(result::add);
        }
        return result;
    }
    
    private Collection<Projection> getDerivedGroupByColumns(final GroupByContext groupByContext, final Collection<Projection> projections) {
        return getDerivedOrderColumns(groupByContext.getItems(), DerivedColumn.GROUP_BY_ALIAS, projections);
    }
    
    private Collection<Projection> getDerivedOrderByColumns(final OrderByContext orderByContext, final Collection<Projection> projections) {
        return getDerivedOrderColumns(orderByContext.getItems(), DerivedColumn.ORDER_BY_ALIAS, projections);
    }
    
    private Collection<Projection> getDerivedOrderColumns(final Collection<OrderByItem> orderItems, final DerivedColumn derivedColumn, final Collection<Projection> projections) {
        Collection<Projection> result = new LinkedList<>();
        int derivedColumnOffset = 0;
        Map<String, List<ColumnProjection>> columnProjections = projectionEngine.getColumnProjections(projections).stream().collect(Collectors.groupingBy(each -> each.getName().toLowerCase()));
        for (OrderByItem each : orderItems) {
            if (!containsProjection(each.getSegment(), columnProjections, projections)) {
                result.add(new DerivedProjection(((TextOrderByItemSegment) each.getSegment()).getText(), derivedColumn.getDerivedColumnAlias(derivedColumnOffset++), each.getSegment()));
            }
        }
        return result;
    }
    
    private boolean containsProjection(final OrderByItemSegment orderByItemSegment, final Map<String, List<ColumnProjection>> columnProjections, final Collection<Projection> projections) {
        if (orderByItemSegment instanceof IndexOrderByItemSegment) {
            return true;
        }
        if (orderByItemSegment instanceof ColumnOrderByItemSegment) {
            ColumnSegment columnSegment = ((ColumnOrderByItemSegment) orderByItemSegment).getColumn();
            List<ColumnProjection> columns = columnProjections.getOrDefault(columnSegment.getIdentifier().getValue().toLowerCase(), Collections.emptyList());
            return columnSegment.getOwner().isPresent() ? columns.stream().anyMatch(each 
                -> columnSegment.getOwner().get().getIdentifier().getValue().equalsIgnoreCase(each.getOwner())) : !columns.isEmpty();
        }
        for (Projection each : projections) {
            if (isSameAlias(each, (TextOrderByItemSegment) orderByItemSegment) || isSameQualifiedName(each, (TextOrderByItemSegment) orderByItemSegment)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean isSameAlias(final Projection projection, final TextOrderByItemSegment orderItem) {
        return projection.getAlias().isPresent() 
                && (SQLUtil.getExactlyValue(orderItem.getText()).equalsIgnoreCase(projection.getAlias().get()) || orderItem.getText().equalsIgnoreCase(projection.getExpression()));
    }
    
    private boolean isSameQualifiedName(final Projection projection, final TextOrderByItemSegment orderItem) {
        return !projection.getAlias().isPresent() && projection.getExpression().equalsIgnoreCase(orderItem.getText());
    }
}
