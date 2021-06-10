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

package org.apache.shardingsphere.infra.binder.statement.dml;

import com.google.common.base.Preconditions;
import lombok.Getter;
import org.apache.shardingsphere.infra.binder.segment.select.groupby.GroupByContext;
import org.apache.shardingsphere.infra.binder.segment.select.groupby.engine.GroupByContextEngine;
import org.apache.shardingsphere.infra.binder.segment.select.having.HavingColumn;
import org.apache.shardingsphere.infra.binder.segment.select.having.HavingContext;
import org.apache.shardingsphere.infra.binder.segment.select.having.engine.HavingContextEngine;
import org.apache.shardingsphere.infra.binder.segment.select.orderby.OrderByContext;
import org.apache.shardingsphere.infra.binder.segment.select.orderby.OrderByItem;
import org.apache.shardingsphere.infra.binder.segment.select.orderby.engine.OrderByContextEngine;
import org.apache.shardingsphere.infra.binder.segment.select.pagination.PaginationContext;
import org.apache.shardingsphere.infra.binder.segment.select.pagination.engine.PaginationContextEngine;
import org.apache.shardingsphere.infra.binder.segment.select.projection.Projection;
import org.apache.shardingsphere.infra.binder.segment.select.projection.ProjectionsContext;
import org.apache.shardingsphere.infra.binder.segment.select.projection.engine.ProjectionsContextEngine;
import org.apache.shardingsphere.infra.binder.segment.select.projection.impl.AggregationProjection;
import org.apache.shardingsphere.infra.binder.segment.table.TablesContext;
import org.apache.shardingsphere.infra.binder.statement.CommonSQLStatementContext;
import org.apache.shardingsphere.infra.binder.type.TableAvailable;
import org.apache.shardingsphere.infra.binder.type.WhereAvailable;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.sql.parser.sql.common.extractor.TableExtractor;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.item.ColumnOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.item.ExpressionOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.item.IndexOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.item.TextOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.JoinTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SubqueryTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.common.util.SQLUtil;
import org.apache.shardingsphere.sql.parser.sql.common.util.WhereSegmentExtractUtils;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.handler.dml.SelectStatementHandler;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Select SQL statement context.
 */
@Getter
public final class SelectStatementContext extends CommonSQLStatementContext<SelectStatement> implements TableAvailable, WhereAvailable {
    
    private final TablesContext tablesContext;
    
    private final ProjectionsContext projectionsContext;
    
    private final GroupByContext groupByContext;
    
    private final HavingContext havingContext;
    
    private final OrderByContext orderByContext;
    
    private final PaginationContext paginationContext;
    
    private final boolean containsSubquery;
    
    private final int generateOrderByStartIndex;

    // TODO to be remove, for test case only
    public SelectStatementContext(final SelectStatement sqlStatement, final GroupByContext groupByContext,
                                  final OrderByContext orderByContext, final ProjectionsContext projectionsContext, final PaginationContext paginationContext) {
        super(sqlStatement);
        tablesContext = new TablesContext(getAllSimpleTableSegments());
        this.groupByContext = groupByContext;
        this.havingContext = new HavingContextEngine().createHavingContext(sqlStatement);
        this.orderByContext = orderByContext;
        this.projectionsContext = projectionsContext;
        this.paginationContext = paginationContext;
        containsSubquery = containsSubquery();
        generateOrderByStartIndex = generateOrderByStartIndex();
    }
    
    public SelectStatementContext(final ShardingSphereSchema schema, final List<Object> parameters, final SelectStatement sqlStatement) {
        super(sqlStatement);
        tablesContext = new TablesContext(getAllSimpleTableSegments());
        groupByContext = new GroupByContextEngine().createGroupByContext(sqlStatement);
        havingContext = new HavingContextEngine().createHavingContext(sqlStatement);
        orderByContext = new OrderByContextEngine().createOrderBy(schema, sqlStatement, groupByContext);
        projectionsContext = new ProjectionsContextEngine(schema).createProjectionsContext(getFromSimpleTableSegments(), getSqlStatement().getProjections(), groupByContext, orderByContext);
        paginationContext = new PaginationContextEngine().createPaginationContext(sqlStatement, projectionsContext, parameters);
        containsSubquery = containsSubquery();
        generateOrderByStartIndex = generateOrderByStartIndex();
    }
    
    private boolean containsSubquery() {
        Collection<WhereSegment> subqueryPredicateSegments = WhereSegmentExtractUtils.getSubqueryWhereSegments(getSqlStatement());
        for (WhereSegment each : subqueryPredicateSegments) {
            if (null != each) {
                return true;
            }
        }
        return false;
    }
    
    private int generateOrderByStartIndex() {
        SelectStatement sqlStatement = getSqlStatement();
        int stopIndex;
        if (SelectStatementHandler.getWindowSegment(sqlStatement).isPresent()) {
            stopIndex = SelectStatementHandler.getWindowSegment(sqlStatement).get().getStopIndex();
        } else if (sqlStatement.getHaving().isPresent()) {
            stopIndex = sqlStatement.getHaving().get().getStopIndex();
        } else if (sqlStatement.getGroupBy().isPresent()) {
            stopIndex = sqlStatement.getGroupBy().get().getStopIndex();
        } else if (sqlStatement.getWhere().isPresent()) {
            stopIndex = sqlStatement.getWhere().get().getStopIndex();
        } else {
            stopIndex = getAllSimpleTableSegments().stream().mapToInt(SimpleTableSegment::getStopIndex).max().orElse(0);
        }
        return stopIndex + 1;
    }
    
    /**
     * Whether it contain join query.
     *
     * @return contain join query or not
     */
    public boolean isContainsJoinQuery() {
        return getSqlStatement().getFrom() instanceof JoinTableSegment;
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
        setIndexForHaving(columnLabelIndexMap);
    }
    
    private void setIndexForHaving(final Map<String, Integer> columnLabelIndexMap) {
        for (HavingColumn each : havingContext.getColumns()) {
            String columnLabel = each.getSegment().getIdentifier().getValue();
            Preconditions.checkState(columnLabelIndexMap.containsKey(columnLabel), "Can't find index: %s.", each);
            each.setIndex(columnLabelIndexMap.get(columnLabel));
        }
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
        TableExtractor tableExtractor = new TableExtractor();
        tableExtractor.extractTablesFromSelect(getSqlStatement());
        return tableExtractor.getRewriteTables();
    }
    
    @Override
    public Optional<WhereSegment> getWhere() {
        return getSqlStatement().getWhere();
    }
    
    /**
     * Get all tables.
     * 
     * @return all tables
     */
    public Collection<SimpleTableSegment> getAllSimpleTableSegments() {
        TableExtractor tableExtractor = new TableExtractor();
        tableExtractor.extractTablesFromSelect(getSqlStatement());
        return tableExtractor.getRewriteTables();
    }
    
    /**
     * Get tables with from clause.
     *
     * @return tables with from clause
     */
    public Collection<SimpleTableSegment> getFromSimpleTableSegments() {
        Collection<SimpleTableSegment> result = new LinkedList<>();
        TableExtractor extractor = new TableExtractor();
        result.addAll(extractor.extractTablesWithFromClause(getSqlStatement()));
        result.addAll(getTemporarySimpleTableSegments(extractor.getTableContext()));
        return result;
    }
    
    private Collection<SimpleTableSegment> getTemporarySimpleTableSegments(final Collection<TableSegment> tableSegments) {
        return tableSegments.stream().filter(each -> each instanceof SubqueryTableSegment).map(each
            -> new SimpleTableSegment(each.getStartIndex(), each.getStopIndex(), new IdentifierValue(each.getAlias().orElse("")))).collect(Collectors.toList());
    }
}
