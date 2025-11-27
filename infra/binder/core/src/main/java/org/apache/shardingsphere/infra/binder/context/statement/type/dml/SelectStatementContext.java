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

package org.apache.shardingsphere.infra.binder.context.statement.type.dml;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.infra.binder.context.available.WhereContextAvailable;
import org.apache.shardingsphere.infra.binder.context.aware.ParameterAware;
import org.apache.shardingsphere.infra.binder.context.segment.select.groupby.GroupByContext;
import org.apache.shardingsphere.infra.binder.context.segment.select.orderby.OrderByContext;
import org.apache.shardingsphere.infra.binder.context.segment.select.pagination.PaginationContext;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.ProjectionsContext;
import org.apache.shardingsphere.infra.binder.context.segment.table.TablesContext;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.sql.parser.statement.core.enums.SubqueryType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.bound.ColumnSegmentBoundInfo;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Select SQL statement context.
 */
@Getter
@Setter
public final class SelectStatementContext implements SQLStatementContext, WhereContextAvailable, ParameterAware {
    
    private final SelectStatementBaseContext baseContext;
    
    private SelectStatementBindingContext bindingContext;
    
    public SelectStatementContext(final SelectStatement sqlStatement, final ShardingSphereMetaData metaData,
                                  final String currentDatabaseName, final Collection<TableSegment> inheritedTables) {
        baseContext = new SelectStatementBaseContext(sqlStatement, metaData, currentDatabaseName, inheritedTables);
        bindingContext = new SelectStatementBindingContext(Collections.emptyList(), baseContext);
    }
    
    /**
     * Judge whether contains join query or not.
     *
     * @return whether contains join query or not
     */
    public boolean isContainsJoinQuery() {
        return baseContext.isContainsJoinQuery();
    }
    
    /**
     * Judge whether contains subquery or not.
     *
     * @return whether contains subquery or not
     */
    public boolean isContainsSubquery() {
        return baseContext.isContainsSubquery();
    }
    
    /**
     * Judge whether contains having or not.
     *
     * @return whether contains having or not
     */
    public boolean isContainsHaving() {
        return baseContext.isContainsHaving();
    }
    
    /**
     * Judge whether contains combine or not.
     *
     * @return whether contains combine or not
     */
    public boolean isContainsCombine() {
        return baseContext.isContainsCombine();
    }
    
    /**
     * Judge whether contains dollar parameter marker or not.
     *
     * @return whether contains dollar parameter marker or not
     */
    public boolean isContainsDollarParameterMarker() {
        return baseContext.isContainsDollarParameterMarker();
    }
    
    /**
     * Judge whether contains partial distinct aggregation.
     *
     * @return whether contains partial distinct aggregation
     */
    public boolean isContainsPartialDistinctAggregation() {
        return baseContext.isContainsPartialDistinctAggregation();
    }
    
    /**
     * Set indexes.
     *
     * @param columnLabelIndexMap map for column label and index
     */
    public void setIndexes(final Map<String, Integer> columnLabelIndexMap) {
        baseContext.setIndexes(columnLabelIndexMap);
    }
    
    /**
     * Judge group by and order by sequence is same or not.
     *
     * @return group by and order by sequence is same or not
     */
    public boolean isSameGroupByAndOrderByItems() {
        return baseContext.isSameGroupByAndOrderByItems();
    }
    
    /**
     * Find column bound info.
     *
     * @param columnIndex column index
     * @return column bound info
     */
    public Optional<ColumnSegmentBoundInfo> findColumnBoundInfo(final int columnIndex) {
        return baseContext.findColumnBoundInfo(columnIndex);
    }
    
    /**
     * Judge whether sql statement contains table subquery segment or not.
     *
     * @return whether sql statement contains table subquery segment or not
     */
    public boolean containsTableSubquery() {
        return baseContext.containsTableSubquery();
    }
    
    /**
     * Judge whether contains derived projections.
     *
     * @return contains derived projections or not
     */
    public boolean containsDerivedProjections() {
        return baseContext.containsDerivedProjections();
    }
    
    @Override
    public Collection<WhereSegment> getWhereSegments() {
        return baseContext.getWhereSegments();
    }
    
    @Override
    public Collection<ColumnSegment> getColumnSegments() {
        return baseContext.getColumnSegments();
    }
    
    @Override
    public Collection<BinaryOperationExpression> getJoinConditions() {
        return baseContext.getJoinConditions();
    }
    
    @Override
    public SelectStatement getSqlStatement() {
        return baseContext.getSqlStatement();
    }
    
    @Override
    public TablesContext getTablesContext() {
        return baseContext.getTablesContext();
    }
    
    /**
     * Set subquery type.
     *
     * @param subqueryType subquery type
     */
    public void setSubqueryType(final SubqueryType subqueryType) {
        baseContext.setSubqueryType(subqueryType);
    }
    
    /**
     * Get subquery type.
     *
     * @return subquery type
     */
    public SubqueryType getSubqueryType() {
        return baseContext.getSubqueryType();
    }
    
    /**
     * Get subquery contexts.
     *
     * @return subquery contexts
     */
    public Map<Integer, SelectStatementContext> getSubqueryContexts() {
        return baseContext.getSubqueryContexts();
    }
    
    /**
     * Get projections context.
     *
     * @return projections context
     */
    public ProjectionsContext getProjectionsContext() {
        return baseContext.getProjectionsContext();
    }
    
    /**
     * Get group by context.
     *
     * @return group by context
     */
    public GroupByContext getGroupByContext() {
        return baseContext.getGroupByContext();
    }
    
    /**
     * Get order by context.
     *
     * @return order by context
     */
    public OrderByContext getOrderByContext() {
        return baseContext.getOrderByContext();
    }
    
    /**
     * Set need aggregate rewrite.
     *
     * @param needAggregateRewrite need aggregate rewrite
     */
    public void setNeedAggregateRewrite(final boolean needAggregateRewrite) {
        baseContext.setNeedAggregateRewrite(needAggregateRewrite);
    }
    
    /**
     * Judge whether need aggregate rewrite or not.
     *
     * @return whether need aggregate rewrite or not
     */
    public boolean isNeedAggregateRewrite() {
        return baseContext.isNeedAggregateRewrite();
    }
    
    /**
     * Judge whether contains enhanced table or not.
     *
     * @return whether contains enhanced table or not
     */
    public boolean isContainsEnhancedTable() {
        return baseContext.isContainsEnhancedTable();
    }
    
    /**
     * Get pagination context.
     *
     * @return pagination context
     */
    public PaginationContext getPaginationContext() {
        return bindingContext.getPaginationContext();
    }
    
    @Override
    public void bindParameters(final List<Object> params) {
        if (!params.isEmpty()) {
            bindingContext = new SelectStatementBindingContext(params, baseContext);
        }
    }
}
