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

package org.apache.shardingsphere.sql.parser.relation.statement.dml;

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.ToString;
import org.apache.shardingsphere.sql.parser.relation.metadata.RelationMetas;
import org.apache.shardingsphere.sql.parser.relation.segment.select.groupby.GroupByContext;
import org.apache.shardingsphere.sql.parser.relation.segment.select.groupby.engine.GroupByContextEngine;
import org.apache.shardingsphere.sql.parser.relation.segment.select.orderby.OrderByContext;
import org.apache.shardingsphere.sql.parser.relation.segment.select.orderby.OrderByItem;
import org.apache.shardingsphere.sql.parser.relation.segment.select.orderby.engine.OrderByContextEngine;
import org.apache.shardingsphere.sql.parser.relation.segment.select.pagination.PaginationContext;
import org.apache.shardingsphere.sql.parser.relation.segment.select.pagination.engine.PaginationContextEngine;
import org.apache.shardingsphere.sql.parser.relation.segment.select.projection.Projection;
import org.apache.shardingsphere.sql.parser.relation.segment.select.projection.ProjectionsContext;
import org.apache.shardingsphere.sql.parser.relation.segment.select.projection.engine.ProjectionsContextEngine;
import org.apache.shardingsphere.sql.parser.relation.segment.select.projection.impl.AggregationProjection;
import org.apache.shardingsphere.sql.parser.relation.segment.table.TableAvailable;
import org.apache.shardingsphere.sql.parser.relation.segment.table.TablesContext;
import org.apache.shardingsphere.sql.parser.relation.statement.CommonSQLStatementContext;
import org.apache.shardingsphere.sql.parser.sql.predicate.PredicateExtractor;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.order.item.ColumnOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.order.item.ExpressionOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.order.item.IndexOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.order.item.OrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.order.item.TextOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.AndPredicate;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.PredicateSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.generic.OwnerAvailable;
import org.apache.shardingsphere.sql.parser.sql.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.generic.TableSegment;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.util.SQLUtil;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Select SQL statement context.
 */
@Getter
@ToString(callSuper = true)
public final class SelectStatementContext extends CommonSQLStatementContext<SelectStatement> implements TableAvailable {
    
    private final TablesContext tablesContext;
    
    private final ProjectionsContext projectionsContext;
    
    private final GroupByContext groupByContext;
    
    private final OrderByContext orderByContext;
    
    private final PaginationContext paginationContext;
    
    private final boolean containsSubquery;

    // TODO to be remove, for test case only
    public SelectStatementContext(final SelectStatement sqlStatement, final GroupByContext groupByContext,
                                  final OrderByContext orderByContext, final ProjectionsContext projectionsContext, final PaginationContext paginationContext) {
        super(sqlStatement);
        tablesContext = new TablesContext(sqlStatement.getTables());
        this.groupByContext = groupByContext;
        this.orderByContext = orderByContext;
        this.projectionsContext = projectionsContext;
        this.paginationContext = paginationContext;
        containsSubquery = containsSubquery();
    }
    
    public SelectStatementContext(final RelationMetas relationMetas, final String sql, final List<Object> parameters, final SelectStatement sqlStatement) {
        super(sqlStatement);
        tablesContext = new TablesContext(sqlStatement.getTables());
        groupByContext = new GroupByContextEngine().createGroupByContext(sqlStatement);
        orderByContext = new OrderByContextEngine().createOrderBy(sqlStatement, groupByContext);
        projectionsContext = new ProjectionsContextEngine(relationMetas).createProjectionsContext(sql, sqlStatement, groupByContext, orderByContext);
        paginationContext = new PaginationContextEngine().createPaginationContext(sqlStatement, projectionsContext, parameters);
        containsSubquery = containsSubquery();
    }
    
    private boolean containsSubquery() {
        // FIXME process subquery
//        Collection<SubqueryPredicateSegment> subqueryPredicateSegments = getSqlStatement().findSQLSegments(SubqueryPredicateSegment.class);
//        for (SubqueryPredicateSegment each : subqueryPredicateSegments) {
//            if (!each.getAndPredicates().isEmpty()) {
//                return true;
//            }
//        }
        return false;
    }
    
    /**
     * Set indexes.
     *
     * @param columnLabelIndexMap map for column label and index
     */
    public void setIndexes(final Map<String, Integer> columnLabelIndexMap) {
        setIndexForAggregationProjection(columnLabelIndexMap);
        setIndexForOrderItem(columnLabelIndexMap, orderByContext.getItems());
        setIndexForOrderItem(columnLabelIndexMap, groupByContext.getItems());
    }
    
    private void setIndexForAggregationProjection(final Map<String, Integer> columnLabelIndexMap) {
        for (AggregationProjection each : projectionsContext.getAggregationProjections()) {
            Preconditions.checkState(columnLabelIndexMap.containsKey(each.getColumnLabel()), "Can't find index: %s, please add alias for aggregate selections", each);
            each.setIndex(columnLabelIndexMap.get(each.getColumnLabel()));
            for (AggregationProjection derived : each.getDerivedAggregationProjections()) {
                Preconditions.checkState(columnLabelIndexMap.containsKey(derived.getColumnLabel()), "Can't find index: %s", derived);
                derived.setIndex(columnLabelIndexMap.get(derived.getColumnLabel()));
            }
        }
    }
    
    private void setIndexForOrderItem(final Map<String, Integer> columnLabelIndexMap, final Collection<OrderByItem> orderByItems) {
        for (OrderByItem each : orderByItems) {
            if (each.getSegment() instanceof IndexOrderByItemSegment) {
                each.setIndex(((IndexOrderByItemSegment) each.getSegment()).getColumnIndex());
                continue;
            }
            if (each.getSegment() instanceof ColumnOrderByItemSegment && ((ColumnOrderByItemSegment) each.getSegment()).getColumn().getOwner().isPresent()) {
                Optional<Integer> itemIndex = projectionsContext.findProjectionIndex(((ColumnOrderByItemSegment) each.getSegment()).getText());
                if (itemIndex.isPresent()) {
                    each.setIndex(itemIndex.get());
                    continue;
                }
            }
            String columnLabel = getAlias(((TextOrderByItemSegment) each.getSegment()).getText()).orElseGet(() -> getOrderItemText((TextOrderByItemSegment) each.getSegment()));
            Preconditions.checkState(columnLabelIndexMap.containsKey(columnLabel), "Can't find index: %s", each);
            if (columnLabelIndexMap.containsKey(columnLabel)) {
                each.setIndex(columnLabelIndexMap.get(columnLabel));
            }
        }
    }
    
    private Optional<String> getAlias(final String name) {
        if (projectionsContext.isUnqualifiedShorthandProjection()) {
            return Optional.empty();
        }
        String rawName = SQLUtil.getExactlyValue(name);
        for (Projection each : projectionsContext.getProjections()) {
            if (SQLUtil.getExactlyExpression(rawName).equalsIgnoreCase(SQLUtil.getExactlyExpression(SQLUtil.getExactlyValue(each.getExpression())))) {
                return each.getAlias();
            }
            if (rawName.equalsIgnoreCase(each.getAlias().orElse(null))) {
                return Optional.of(rawName);
            }
        }
        return Optional.empty();
    }
    
    private String getOrderItemText(final TextOrderByItemSegment orderByItemSegment) {
        return orderByItemSegment instanceof ColumnOrderByItemSegment
                ? ((ColumnOrderByItemSegment) orderByItemSegment).getColumn().getIdentifier().getValue() : ((ExpressionOrderByItemSegment) orderByItemSegment).getExpression();
    }
    
    /**
     * Judge group by and order by sequence is same or not.
     *
     * @return group by and order by sequence is same or not
     */
    public boolean isSameGroupByAndOrderByItems() {
        return !groupByContext.getItems().isEmpty() && groupByContext.getItems().equals(orderByContext.getItems());
    }
    
    @Override
    public Collection<TableSegment> getAllTables() {
        Collection<TableSegment> result = new LinkedList<>(getSqlStatement().getTables());
        if (getSqlStatement().getWhere().isPresent()) {
            result.addAll(getAllTablesFromWhere(getSqlStatement().getWhere().get()));
        }
        result.addAll(getAllTablesFromProjections(getSqlStatement().getProjections()));
        if (getSqlStatement().getGroupBy().isPresent()) {
            result.addAll(getAllTablesFromOrderByItems(getSqlStatement().getGroupBy().get().getGroupByItems()));
        }
        if (getSqlStatement().getOrderBy().isPresent()) {
            result.addAll(getAllTablesFromOrderByItems(getSqlStatement().getOrderBy().get().getOrderByItems()));
        }
        return result;
    }
    
    private Collection<TableSegment> getAllTablesFromWhere(final WhereSegment where) {
        Collection<TableSegment> result = new LinkedList<>();
        for (AndPredicate each : where.getAndPredicates()) {
            for (PredicateSegment predicate : each.getPredicates()) {
                result.addAll(new PredicateExtractor(getSqlStatement().getTables(), predicate).extractTables());
            }
        }
        return result;
    }
    
    private Collection<TableSegment> getAllTablesFromProjections(final ProjectionsSegment projections) {
        Collection<TableSegment> result = new LinkedList<>();
        for (ProjectionSegment each : projections.getProjections()) {
            Optional<TableSegment> table = getTableSegment(each);
            table.ifPresent(result::add);
        }
        return result;
    }
    
    private Optional<TableSegment> getTableSegment(final ProjectionSegment each) {
        Optional<OwnerSegment> owner = getTableOwner(each);
        if (owner.isPresent() && isTable(owner.get(), getSqlStatement().getTables())) {
            return Optional .of(new TableSegment(owner.get().getStartIndex(), owner.get().getStopIndex(), owner.get().getIdentifier()));
        }
        return Optional.empty();
    }
    
    private Optional<OwnerSegment> getTableOwner(final ProjectionSegment each) {
        if (each instanceof OwnerAvailable) {
            return ((OwnerAvailable) each).getOwner();
        }
        if (each instanceof ColumnProjectionSegment) { 
            return ((ColumnProjectionSegment) each).getColumn().getOwner();
        }
        return Optional.empty();
    }
    
    private Collection<TableSegment> getAllTablesFromOrderByItems(final Collection<OrderByItemSegment> orderByItems) {
        Collection<TableSegment> result = new LinkedList<>();
        for (OrderByItemSegment each : orderByItems) {
            if (each instanceof ColumnOrderByItemSegment) {
                Optional<OwnerSegment> owner = ((ColumnOrderByItemSegment) each).getColumn().getOwner();
                if (owner.isPresent() && isTable(owner.get(), getSqlStatement().getTables())) {
                    Preconditions.checkState(((ColumnOrderByItemSegment) each).getColumn().getOwner().isPresent());
                    OwnerSegment segment = ((ColumnOrderByItemSegment) each).getColumn().getOwner().get();
                    result.add(new TableSegment(segment.getStartIndex(), segment.getStopIndex(), segment.getIdentifier()));
                }
            }
        }
        return result;
    }
    
    private boolean isTable(final OwnerSegment owner, final Collection<TableSegment> tables) {
        for (TableSegment each : tables) {
            if (owner.getIdentifier().getValue().equals(each.getAlias().orElse(null))) {
                return false;
            }
        }
        return true;
    }
}
