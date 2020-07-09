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

package org.apache.shardingsphere.sql.parser.binder.statement.dml;

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.ToString;
import org.apache.shardingsphere.sql.parser.binder.metadata.schema.SchemaMetaData;
import org.apache.shardingsphere.sql.parser.binder.segment.select.groupby.GroupByContext;
import org.apache.shardingsphere.sql.parser.binder.segment.select.groupby.engine.GroupByContextEngine;
import org.apache.shardingsphere.sql.parser.binder.segment.select.orderby.OrderByContext;
import org.apache.shardingsphere.sql.parser.binder.segment.select.orderby.OrderByItem;
import org.apache.shardingsphere.sql.parser.binder.segment.select.orderby.engine.OrderByContextEngine;
import org.apache.shardingsphere.sql.parser.binder.segment.select.pagination.PaginationContext;
import org.apache.shardingsphere.sql.parser.binder.segment.select.pagination.engine.PaginationContextEngine;
import org.apache.shardingsphere.sql.parser.binder.segment.select.projection.Projection;
import org.apache.shardingsphere.sql.parser.binder.segment.select.projection.ProjectionsContext;
import org.apache.shardingsphere.sql.parser.binder.segment.select.projection.engine.ProjectionsContextEngine;
import org.apache.shardingsphere.sql.parser.binder.segment.select.projection.impl.AggregationProjection;
import org.apache.shardingsphere.sql.parser.binder.segment.table.TablesContext;
import org.apache.shardingsphere.sql.parser.binder.statement.CommonSQLStatementContext;
import org.apache.shardingsphere.sql.parser.binder.type.TableAvailable;
import org.apache.shardingsphere.sql.parser.binder.type.WhereAvailable;
import org.apache.shardingsphere.sql.parser.sql.predicate.PredicateExtractor;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.JoinSpecificationSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.JoinedTableSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.TableFactorSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.TableReferenceSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.column.ColumnSegment;
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
import org.apache.shardingsphere.sql.parser.sql.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.generic.table.SubqueryTableSegment;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.util.SQLUtil;

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
public final class SelectStatementContext extends CommonSQLStatementContext<SelectStatement> implements TableAvailable, WhereAvailable {
    
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
        tablesContext = new TablesContext(getSimpleTableSegments());
        this.groupByContext = groupByContext;
        this.orderByContext = orderByContext;
        this.projectionsContext = projectionsContext;
        this.paginationContext = paginationContext;
        containsSubquery = containsSubquery();
    }
    
    public SelectStatementContext(final SchemaMetaData schemaMetaData, final String sql, final List<Object> parameters, final SelectStatement sqlStatement) {
        super(sqlStatement);
        tablesContext = new TablesContext(getSimpleTableSegments());
        groupByContext = new GroupByContextEngine().createGroupByContext(sqlStatement);
        orderByContext = new OrderByContextEngine().createOrderBy(sqlStatement, groupByContext);
        projectionsContext = new ProjectionsContextEngine(schemaMetaData).createProjectionsContext(sql, getSimpleTableSegments(), getSqlStatement().getProjections(), groupByContext, orderByContext);
        paginationContext = new PaginationContextEngine().createPaginationContext(sqlStatement, projectionsContext, parameters);
        containsSubquery = containsSubquery();
    }
    
    private boolean containsSubquery() {
        Collection<WhereSegment> subqueryPredicateSegments = getSqlStatement().getSubqueryWhereSegments();
        for (WhereSegment each : subqueryPredicateSegments) {
            if (!each.getAndPredicates().isEmpty()) {
                return true;
            }
        }
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
    public Collection<SimpleTableSegment> getAllTables() {
        Collection<SimpleTableSegment> result = new LinkedList<>(getSimpleTableSegments());
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
    
    private Collection<SimpleTableSegment> getAllTablesFromWhere(final WhereSegment where) {
        Collection<SimpleTableSegment> result = new LinkedList<>();
        for (AndPredicate each : where.getAndPredicates()) {
            for (PredicateSegment predicate : each.getPredicates()) {
                result.addAll(new PredicateExtractor(getSimpleTableSegments(), predicate).extractTables());
            }
        }
        return result;
    }
    
    private Collection<SimpleTableSegment> getAllTablesFromProjections(final ProjectionsSegment projections) {
        Collection<SimpleTableSegment> result = new LinkedList<>();
        for (ProjectionSegment each : projections.getProjections()) {
            Optional<SimpleTableSegment> table = getTableSegment(each);
            table.ifPresent(result::add);
        }
        return result;
    }
    
    private Optional<SimpleTableSegment> getTableSegment(final ProjectionSegment each) {
        Optional<OwnerSegment> owner = getTableOwner(each);
        if (owner.isPresent() && isTable(owner.get(), getSimpleTableSegments())) {
            return Optional .of(new SimpleTableSegment(owner.get().getStartIndex(), owner.get().getStopIndex(), owner.get().getIdentifier()));
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
    
    private Collection<SimpleTableSegment> getAllTablesFromOrderByItems(final Collection<OrderByItemSegment> orderByItems) {
        Collection<SimpleTableSegment> result = new LinkedList<>();
        for (OrderByItemSegment each : orderByItems) {
            if (each instanceof ColumnOrderByItemSegment) {
                Optional<OwnerSegment> owner = ((ColumnOrderByItemSegment) each).getColumn().getOwner();
                if (owner.isPresent() && isTable(owner.get(), getSimpleTableSegments())) {
                    Preconditions.checkState(((ColumnOrderByItemSegment) each).getColumn().getOwner().isPresent());
                    OwnerSegment segment = ((ColumnOrderByItemSegment) each).getColumn().getOwner().get();
                    result.add(new SimpleTableSegment(segment.getStartIndex(), segment.getStopIndex(), segment.getIdentifier()));
                }
            }
        }
        return result;
    }
    
    private boolean isTable(final OwnerSegment owner, final Collection<SimpleTableSegment> tables) {
        for (SimpleTableSegment each : tables) {
            if (owner.getIdentifier().getValue().equals(each.getAlias().orElse(null))) {
                return false;
            }
        }
        return true;
    }
    
    private boolean isTable(final SimpleTableSegment owner, final Collection<SimpleTableSegment> tableSegments) {
        for (SimpleTableSegment each : tableSegments) {
            String tableName = owner.getTableName().getIdentifier().getValue();
            if (tableName.equals(each.getAlias().orElse(null)) && !tableName.equals(each.getTableName().getIdentifier().getValue())) {
                return false;
            }
        }
        return true;
    }
    
    @Override
    public Optional<WhereSegment> getWhere() {
        return getSqlStatement().getWhere();
    }
    
    /**
     * get tables.
     * @return tables.
     */
    public Collection<SimpleTableSegment> getSimpleTableSegments() {
        Collection<SimpleTableSegment> tables = getTables();
        Collection<SimpleTableSegment> result = new LinkedList<>();
        for (SimpleTableSegment each : tables) {
            if (isTable(each, tables)) {
                result.add(each);
            }
        }
        return result;
    }
    
    private Collection<SimpleTableSegment> getTables() {
        SelectStatement selectStatement = getSqlStatement();
        Collection<SimpleTableSegment> result = new LinkedList<>();
        for (TableReferenceSegment each : selectStatement.getTableReferences()) {
            result.addAll(getTablesFromTableReference(each));
        }
        return result;
    }
    
    private Collection<SimpleTableSegment> getTablesFromTableFactor(final TableFactorSegment tableFactorSegment) {
        Collection<SimpleTableSegment> result = new LinkedList<>();
        if (null != tableFactorSegment.getTable() && tableFactorSegment.getTable() instanceof SimpleTableSegment) {
            result.add((SimpleTableSegment) tableFactorSegment.getTable());
        }
        // TODO subquery in from support not use alias
        if (null != tableFactorSegment.getTable() && tableFactorSegment.getTable() instanceof SubqueryTableSegment) {
            SelectStatement subselect = ((SubqueryTableSegment) tableFactorSegment.getTable()).getSubquery().getSelect();
            for (TableReferenceSegment each : subselect.getTableReferences()) {
                result.addAll(getTablesFromTableReference(each));
            }
        }
        if (null != tableFactorSegment.getTableReferences() && !tableFactorSegment.getTableReferences().isEmpty()) {
            for (TableReferenceSegment each: tableFactorSegment.getTableReferences()) {
                result.addAll(getTablesFromTableReference(each));
            }
        }
        return result;
    }
    
    private Collection<SimpleTableSegment> getTablesFromTableReference(final TableReferenceSegment tableReferenceSegment) {
        Collection<SimpleTableSegment> result = new LinkedList<>();
        if (null != tableReferenceSegment.getTableFactor()) {
            result.addAll(getTablesFromTableFactor(tableReferenceSegment.getTableFactor()));
        }
        if (null != tableReferenceSegment.getJoinedTables()) {
            for (JoinedTableSegment each : tableReferenceSegment.getJoinedTables()) {
                result.addAll(getTablesFromJoinTable(each));
            }
        }
        return result;
    }
    
    private Collection<SimpleTableSegment> getTablesFromJoinTable(final JoinedTableSegment joinedTableSegment) {
        Collection<SimpleTableSegment> result = new LinkedList<>();
        if (null != joinedTableSegment.getTableFactor()) {
            result.addAll(getTablesFromTableFactor(joinedTableSegment.getTableFactor()));
        }
        if (null != joinedTableSegment.getJoinSpecification()) {
            result.addAll(getTablesFromJoinSpecification(joinedTableSegment.getJoinSpecification()));
        }
        return result;
    }
    
    private Collection<SimpleTableSegment> getTablesFromJoinSpecification(final JoinSpecificationSegment joinSpecificationSegment) {
        Collection<SimpleTableSegment> result = new LinkedList<>();
        Collection<AndPredicate> andPredicates = joinSpecificationSegment.getAndPredicates();
        for (AndPredicate each : andPredicates) {
            for (PredicateSegment e : each.getPredicates()) {
                if (null != e.getColumn() && (e.getColumn().getOwner().isPresent())) {
                    OwnerSegment ownerSegment = e.getColumn().getOwner().get();
                    result.add(new SimpleTableSegment(ownerSegment.getStartIndex(), ownerSegment.getStopIndex(), ownerSegment.getIdentifier()));
                }
                if (null != e.getRightValue() && (e.getRightValue() instanceof ColumnSegment) && ((ColumnSegment) e.getRightValue()).getOwner().isPresent()) {
                    OwnerSegment ownerSegment = ((ColumnSegment) e.getRightValue()).getOwner().get();
                    result.add(new SimpleTableSegment(ownerSegment.getStartIndex(), ownerSegment.getStopIndex(), ownerSegment.getIdentifier()));
                }
            }
        }
        return result;
    }
}
