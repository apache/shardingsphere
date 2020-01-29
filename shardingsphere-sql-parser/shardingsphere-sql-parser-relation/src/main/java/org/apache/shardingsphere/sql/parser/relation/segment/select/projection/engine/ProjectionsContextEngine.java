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

package org.apache.shardingsphere.sql.parser.relation.segment.select.projection.engine;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.sql.parser.relation.metadata.RelationMetas;
import org.apache.shardingsphere.sql.parser.relation.segment.select.groupby.GroupByContext;
import org.apache.shardingsphere.sql.parser.relation.segment.select.orderby.OrderByContext;
import org.apache.shardingsphere.sql.parser.relation.segment.select.orderby.OrderByItem;
import org.apache.shardingsphere.sql.parser.relation.segment.select.projection.DerivedColumn;
import org.apache.shardingsphere.sql.parser.relation.segment.select.projection.Projection;
import org.apache.shardingsphere.sql.parser.relation.segment.select.projection.ProjectionsContext;
import org.apache.shardingsphere.sql.parser.relation.segment.select.projection.impl.DerivedProjection;
import org.apache.shardingsphere.sql.parser.relation.segment.select.projection.impl.ShorthandProjection;
import org.apache.shardingsphere.sql.parser.relation.segment.table.Table;
import org.apache.shardingsphere.sql.parser.relation.segment.table.TablesContext;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.order.item.ColumnOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.order.item.IndexOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.order.item.OrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.order.item.TextOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.generic.TableSegment;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.SelectStatement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Projections context engine.
 *
 * @author zhangliang
 * @author sunbufu
 */
@RequiredArgsConstructor
public final class ProjectionsContextEngine {
    
    private final RelationMetas relationMetas;
    
    private final ProjectionEngine projectionEngine = new ProjectionEngine();
    
    /**
     * Create projections context.
     *
     * @param sql SQL
     * @param selectStatement SQL statement
     * @param groupByContext group by context
     * @param orderByContext order by context
     * @return projections context
     */
    public ProjectionsContext createProjectionsContext(final String sql, final SelectStatement selectStatement, final GroupByContext groupByContext, final OrderByContext orderByContext) {
        ProjectionsSegment projectionsSegment = selectStatement.getProjections();
        Collection<Projection> projections = getProjections(sql, projectionsSegment);
        ProjectionsContext result = new ProjectionsContext(
                projectionsSegment.getStartIndex(), projectionsSegment.getStopIndex(), projectionsSegment.isDistinctRow(), projections, getColumnLabels(selectStatement.getTables(), projections));
        TablesContext tablesContext = new TablesContext(selectStatement);
        result.getProjections().addAll(getDerivedGroupByColumns(tablesContext, projections, groupByContext));
        result.getProjections().addAll(getDerivedOrderByColumns(tablesContext, projections, orderByContext));
        return result;
    }
    
    private Collection<Projection> getProjections(final String sql, final ProjectionsSegment projectionsSegment) {
        Collection<Projection> result = new LinkedList<>();
        for (ProjectionSegment each : projectionsSegment.getProjections()) {
            Optional<Projection> projection = projectionEngine.createProjection(sql, each);
            if (projection.isPresent()) {
                result.add(projection.get());
            }
        }
        return result;
    }
    
    private List<String> getColumnLabels(final Collection<TableSegment> tables, final Collection<Projection> projections) {
        List<String> result = new ArrayList<>(projections.size());
        for (Projection each : projections) {
            if (each instanceof ShorthandProjection) {
                result.addAll(getShorthandColumnLabels(tables, (ShorthandProjection) each));
            } else {
                result.add(each.getColumnLabel());
            }
        }
        return result;
    }
    
    private Collection<String> getShorthandColumnLabels(final Collection<TableSegment> tables, final ShorthandProjection shorthandProjection) {
        return shorthandProjection.getOwner().isPresent()
                ? getQualifiedShorthandColumnLabels(tables, shorthandProjection.getOwner().get()) : getUnqualifiedShorthandColumnLabels(tables);
    }
    
    private Collection<String> getQualifiedShorthandColumnLabels(final Collection<TableSegment> tables, final String owner) {
        for (TableSegment each : tables) {
            if (owner.equalsIgnoreCase(each.getAlias().or(each.getTableName()))) {
                return relationMetas.getAllColumnNames(each.getTableName());
            }
        }
        return Collections.emptyList();
    }
    
    private Collection<String> getUnqualifiedShorthandColumnLabels(final Collection<TableSegment> tables) {
        Collection<String> result = new LinkedList<>();
        for (TableSegment each : tables) {
            result.addAll(relationMetas.getAllColumnNames(each.getTableName()));
        }
        return result;
    }
    
    private Collection<Projection> getDerivedGroupByColumns(final TablesContext tablesContext, final Collection<Projection> projections, final GroupByContext groupByContext) {
        return getDerivedOrderColumns(tablesContext, projections, groupByContext.getItems(), DerivedColumn.GROUP_BY_ALIAS);
    }
    
    private Collection<Projection> getDerivedOrderByColumns(final TablesContext tablesContext, final Collection<Projection> projections, final OrderByContext orderByContext) {
        return getDerivedOrderColumns(tablesContext, projections, orderByContext.getItems(), DerivedColumn.ORDER_BY_ALIAS);
    }
    
    private Collection<Projection> getDerivedOrderColumns(final TablesContext tablesContext,
                                                          final Collection<Projection> projections, final Collection<OrderByItem> orderItems, final DerivedColumn derivedColumn) {
        Collection<Projection> result = new LinkedList<>();
        int derivedColumnOffset = 0;
        for (OrderByItem each : orderItems) {
            if (!containsProjection(tablesContext, projections, each.getSegment())) {
                result.add(new DerivedProjection(((TextOrderByItemSegment) each.getSegment()).getText(), derivedColumn.getDerivedColumnAlias(derivedColumnOffset++)));
            }
        }
        return result;
    }
    
    private boolean containsProjection(final TablesContext tablesContext, final Collection<Projection> projections, final OrderByItemSegment orderByItemSegment) {
        return orderByItemSegment instanceof IndexOrderByItemSegment
                || containsItemInShorthandProjection(tablesContext, projections, orderByItemSegment) || containsProjection(projections, orderByItemSegment);
    }
    
    private boolean containsProjection(final Collection<Projection> projections, final OrderByItemSegment orderItem) {
        for (Projection each : projections) {
            if (orderItem instanceof IndexOrderByItemSegment) {
                return true;
            }
            if (isSameAlias(each, (TextOrderByItemSegment) orderItem) || isSameQualifiedName(each, (TextOrderByItemSegment) orderItem)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean containsItemInShorthandProjection(final TablesContext tablesContext, final Collection<Projection> projections, final OrderByItemSegment orderByItemSegment) {
        return isUnqualifiedShorthandProjection(projections) || containsItemWithOwnerInShorthandProjections(tablesContext, projections, orderByItemSegment)
                || containsItemWithoutOwnerInShorthandProjections(tablesContext, projections, orderByItemSegment);
    }
    
    private boolean isUnqualifiedShorthandProjection(final Collection<Projection> projections) {
        if (1 != projections.size()) {
            return false;
        }
        Projection projection = projections.iterator().next();
        return projection instanceof ShorthandProjection && !((ShorthandProjection) projection).getOwner().isPresent();
    }
    
    private boolean containsItemWithOwnerInShorthandProjections(final TablesContext tablesContext, final Collection<Projection> projections, final OrderByItemSegment orderItem) {
        return orderItem instanceof ColumnOrderByItemSegment && ((ColumnOrderByItemSegment) orderItem).getColumn().getOwner().isPresent()
                && findShorthandProjection(tablesContext, projections, ((ColumnOrderByItemSegment) orderItem).getColumn().getOwner().get().getTableName()).isPresent();
    }
    
    private Optional<ShorthandProjection> findShorthandProjection(final TablesContext tablesContext, final Collection<Projection> projections, final String tableNameOrAlias) {
        Optional<Table> table = tablesContext.find(tableNameOrAlias);
        if (!table.isPresent()) {
            return Optional.absent();
        }
        for (Projection each : projections) {
            if (!(each instanceof ShorthandProjection)) {
                continue;
            }
            ShorthandProjection shorthandProjection = (ShorthandProjection) each;
            if (shorthandProjection.getOwner().isPresent() && tablesContext.find(shorthandProjection.getOwner().get()).equals(table)) {
                return Optional.of(shorthandProjection);
            }
        }
        return Optional.absent();
    }
    
    private boolean containsItemWithoutOwnerInShorthandProjections(final TablesContext tablesContext, final Collection<Projection> projections, final OrderByItemSegment orderItem) {
        if (!(orderItem instanceof ColumnOrderByItemSegment)) {
            return false;
        }
        if (!((ColumnOrderByItemSegment) orderItem).getColumn().getOwner().isPresent()) {
            for (ShorthandProjection each : getQualifiedShorthandProjections(projections)) {
                if (isSameProjection(tablesContext, each, (ColumnOrderByItemSegment) orderItem)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    private Collection<ShorthandProjection> getQualifiedShorthandProjections(final Collection<Projection> projections) {
        Collection<ShorthandProjection> result = new LinkedList<>();
        for (Projection each : projections) {
            if (each instanceof ShorthandProjection && ((ShorthandProjection) each).getOwner().isPresent()) {
                result.add((ShorthandProjection) each);
            }
        }
        return result;
    }
    
    private boolean isSameProjection(final TablesContext tablesContext, final ShorthandProjection shorthandProjection, final ColumnOrderByItemSegment orderItem) {
        Preconditions.checkState(shorthandProjection.getOwner().isPresent());
        Optional<Table> table = tablesContext.find(shorthandProjection.getOwner().get());
        return table.isPresent() && relationMetas.containsColumn(table.get().getName(), orderItem.getColumn().getName());
    }
    
    private boolean isSameAlias(final Projection projection, final TextOrderByItemSegment orderItem) {
        return projection.getAlias().isPresent() && (orderItem.getText().equalsIgnoreCase(projection.getAlias().get()) || orderItem.getText().equalsIgnoreCase(projection.getExpression()));
    }
    
    private boolean isSameQualifiedName(final Projection projection, final TextOrderByItemSegment orderItem) {
        return !projection.getAlias().isPresent() && projection.getExpression().equalsIgnoreCase(orderItem.getText());
    }
}
