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

package org.apache.shardingsphere.sql.parser.binder.segment.select.projection.engine;

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.sql.parser.binder.metadata.schema.SchemaMetaData;
import org.apache.shardingsphere.sql.parser.binder.segment.select.groupby.GroupByContext;
import org.apache.shardingsphere.sql.parser.binder.segment.select.orderby.OrderByContext;
import org.apache.shardingsphere.sql.parser.binder.segment.select.orderby.OrderByItem;
import org.apache.shardingsphere.sql.parser.binder.segment.select.projection.DerivedColumn;
import org.apache.shardingsphere.sql.parser.binder.segment.select.projection.Projection;
import org.apache.shardingsphere.sql.parser.binder.segment.select.projection.ProjectionsContext;
import org.apache.shardingsphere.sql.parser.binder.segment.select.projection.impl.DerivedProjection;
import org.apache.shardingsphere.sql.parser.binder.segment.select.projection.impl.ShorthandProjection;
import org.apache.shardingsphere.sql.parser.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.order.item.ColumnOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.order.item.IndexOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.order.item.OrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.order.item.TextOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.SelectStatement;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;

/**
 * Projections context engine.
 */
public final class ProjectionsContextEngine {
    
    private final SchemaMetaData schemaMetaData;
    
    private final ProjectionEngine projectionEngine;
    
    public ProjectionsContextEngine(final SchemaMetaData schemaMetaData) {
        this.schemaMetaData = schemaMetaData;
        projectionEngine = new ProjectionEngine(schemaMetaData);
    }
    
    /**
     * Create projections context.
     *
     * @param sql SQL
     * @param context SQL statement context
     * @param groupByContext group by context
     * @param orderByContext order by context
     * @return projections context
     */
    public ProjectionsContext createProjectionsContext(final String sql, final SelectStatementContext context, final GroupByContext groupByContext, final OrderByContext orderByContext) {
        SelectStatement selectStatement = context.getSqlStatement();
        ProjectionsSegment projectionsSegment = selectStatement.getProjections();
        Collection<Projection> projections = getProjections(sql, context.getSimpleTableSegments(selectStatement), projectionsSegment);
        ProjectionsContext result = new ProjectionsContext(projectionsSegment.getStartIndex(), projectionsSegment.getStopIndex(), projectionsSegment.isDistinctRow(), projections);
        result.getProjections().addAll(getDerivedGroupByColumns(projections, groupByContext, context));
        result.getProjections().addAll(getDerivedOrderByColumns(projections, orderByContext, context));
        return result;
    }
    
    private Collection<Projection> getProjections(final String sql, final Collection<SimpleTableSegment> tableSegments, final ProjectionsSegment projectionsSegment) {
        Collection<Projection> result = new LinkedList<>();
        for (ProjectionSegment each : projectionsSegment.getProjections()) {
            projectionEngine.createProjection(sql, tableSegments, each).ifPresent(result::add);
        }
        return result;
    }
    
    private Collection<Projection> getDerivedGroupByColumns(final Collection<Projection> projections, final GroupByContext groupByContext, final SelectStatementContext selectStatementContext) {
        return getDerivedOrderColumns(projections, groupByContext.getItems(), DerivedColumn.GROUP_BY_ALIAS, selectStatementContext);
    }
    
    private Collection<Projection> getDerivedOrderByColumns(final Collection<Projection> projections, final OrderByContext orderByContext, final SelectStatementContext selectStatementContext) {
        return getDerivedOrderColumns(projections, orderByContext.getItems(), DerivedColumn.ORDER_BY_ALIAS, selectStatementContext);
    }
    
    private Collection<Projection> getDerivedOrderColumns(final Collection<Projection> projections, 
                                                          final Collection<OrderByItem> orderItems, final DerivedColumn derivedColumn, final SelectStatementContext selectStatementContext) {
        Collection<Projection> result = new LinkedList<>();
        int derivedColumnOffset = 0;
        for (OrderByItem each : orderItems) {
            if (!containsProjection(projections, each.getSegment(), selectStatementContext)) {
                result.add(new DerivedProjection(((TextOrderByItemSegment) each.getSegment()).getText(), derivedColumn.getDerivedColumnAlias(derivedColumnOffset++)));
            }
        }
        return result;
    }
    
    private boolean containsProjection(final Collection<Projection> projections, final OrderByItemSegment orderByItemSegment, final SelectStatementContext selectStatementContext) {
        return orderByItemSegment instanceof IndexOrderByItemSegment
                || containsItemInShorthandProjection(projections, orderByItemSegment, selectStatementContext) || containsProjection(projections, orderByItemSegment);
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
    
    private boolean containsItemInShorthandProjection(final Collection<Projection> projections, final OrderByItemSegment orderByItemSegment, final SelectStatementContext selectStatementContext) {
        return isUnqualifiedShorthandProjection(projections) || containsItemWithOwnerInShorthandProjections(projections, orderByItemSegment, selectStatementContext)
                || containsItemWithoutOwnerInShorthandProjections(projections, orderByItemSegment, selectStatementContext);
    }
    
    private boolean isUnqualifiedShorthandProjection(final Collection<Projection> projections) {
        if (1 != projections.size()) {
            return false;
        }
        Projection projection = projections.iterator().next();
        return projection instanceof ShorthandProjection && !((ShorthandProjection) projection).getOwner().isPresent();
    }
    
    private boolean containsItemWithOwnerInShorthandProjections(final Collection<Projection> projections, final OrderByItemSegment orderItem, final SelectStatementContext selectStatementContext) {
        return orderItem instanceof ColumnOrderByItemSegment && ((ColumnOrderByItemSegment) orderItem).getColumn().getOwner().isPresent()
                && findShorthandProjection(projections, ((ColumnOrderByItemSegment) orderItem).getColumn().getOwner().get().getIdentifier().getValue(), selectStatementContext).isPresent();
    }
    
    private Optional<ShorthandProjection> findShorthandProjection(final Collection<Projection> projections, final String tableNameOrAlias, final SelectStatementContext selectStatementContext) {
        SimpleTableSegment tableSegment = find(tableNameOrAlias, selectStatementContext);
        for (Projection each : projections) {
            if (!(each instanceof ShorthandProjection)) {
                continue;
            }
            ShorthandProjection shorthandProjection = (ShorthandProjection) each;
            if (shorthandProjection.getOwner().isPresent() && find(
                    shorthandProjection.getOwner().get(), selectStatementContext).getTableName().getIdentifier().getValue().equalsIgnoreCase(tableSegment.getTableName().getIdentifier().getValue())) {
                return Optional.of(shorthandProjection);
            }
        }
        return Optional.empty();
    }
    
    private boolean containsItemWithoutOwnerInShorthandProjections(final Collection<Projection> projections, final OrderByItemSegment orderItem, final SelectStatementContext selectStatementContext) {
        if (!(orderItem instanceof ColumnOrderByItemSegment)) {
            return false;
        }
        if (!((ColumnOrderByItemSegment) orderItem).getColumn().getOwner().isPresent()) {
            for (ShorthandProjection each : getQualifiedShorthandProjections(projections)) {
                if (isSameProjection(each, (ColumnOrderByItemSegment) orderItem, selectStatementContext)) {
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
    
    private boolean isSameProjection(final ShorthandProjection shorthandProjection, final ColumnOrderByItemSegment orderItem, final SelectStatementContext selectStatementContext) {
        Preconditions.checkState(shorthandProjection.getOwner().isPresent());
        SimpleTableSegment tableSegment = find(shorthandProjection.getOwner().get(), selectStatementContext);
        return schemaMetaData.containsColumn(tableSegment.getTableName().getIdentifier().getValue(), orderItem.getColumn().getIdentifier().getValue());
    }
    
    private boolean isSameAlias(final Projection projection, final TextOrderByItemSegment orderItem) {
        return projection.getAlias().isPresent() && (orderItem.getText().equalsIgnoreCase(projection.getAlias().get()) || orderItem.getText().equalsIgnoreCase(projection.getExpression()));
    }
    
    private boolean isSameQualifiedName(final Projection projection, final TextOrderByItemSegment orderItem) {
        return !projection.getAlias().isPresent() && projection.getExpression().equalsIgnoreCase(orderItem.getText());
    }
    
    private SimpleTableSegment find(final String tableNameOrAlias, final SelectStatementContext selectStatementContext) {
        for (SimpleTableSegment each : selectStatementContext.getSimpleTableSegments(selectStatementContext.getSqlStatement())) {
            if (tableNameOrAlias.equalsIgnoreCase(each.getTableName().getIdentifier().getValue()) || tableNameOrAlias.equals(each.getAlias().orElse(null))) {
                return each;
            }
        }
        throw new IllegalStateException("Can not find owner from table.");
    }
}
