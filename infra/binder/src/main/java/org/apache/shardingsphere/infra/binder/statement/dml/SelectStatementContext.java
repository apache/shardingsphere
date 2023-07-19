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
import lombok.Setter;
import org.apache.shardingsphere.dialect.exception.syntax.database.NoDatabaseSelectedException;
import org.apache.shardingsphere.dialect.exception.syntax.database.UnknownDatabaseException;
import org.apache.shardingsphere.infra.binder.aware.ParameterAware;
import org.apache.shardingsphere.infra.binder.segment.select.groupby.GroupByContext;
import org.apache.shardingsphere.infra.binder.segment.select.groupby.engine.GroupByContextEngine;
import org.apache.shardingsphere.infra.binder.segment.select.orderby.OrderByContext;
import org.apache.shardingsphere.infra.binder.segment.select.orderby.OrderByItem;
import org.apache.shardingsphere.infra.binder.segment.select.orderby.engine.OrderByContextEngine;
import org.apache.shardingsphere.infra.binder.segment.select.pagination.PaginationContext;
import org.apache.shardingsphere.infra.binder.segment.select.pagination.engine.PaginationContextEngine;
import org.apache.shardingsphere.infra.binder.segment.select.projection.Projection;
import org.apache.shardingsphere.infra.binder.segment.select.projection.ProjectionsContext;
import org.apache.shardingsphere.infra.binder.segment.select.projection.engine.ProjectionsContextEngine;
import org.apache.shardingsphere.infra.binder.segment.select.projection.impl.AggregationDistinctProjection;
import org.apache.shardingsphere.infra.binder.segment.select.projection.impl.AggregationProjection;
import org.apache.shardingsphere.infra.binder.segment.select.projection.impl.ColumnProjection;
import org.apache.shardingsphere.infra.binder.segment.select.projection.impl.ParameterMarkerProjection;
import org.apache.shardingsphere.infra.binder.segment.select.projection.impl.SubqueryProjection;
import org.apache.shardingsphere.infra.binder.segment.table.TablesContext;
import org.apache.shardingsphere.infra.binder.statement.CommonSQLStatementContext;
import org.apache.shardingsphere.infra.binder.type.TableAvailable;
import org.apache.shardingsphere.infra.binder.type.WhereAvailable;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.rule.identifier.type.TableContainedRule;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.sql.parser.sql.common.enums.ParameterMarkerType;
import org.apache.shardingsphere.sql.parser.sql.common.enums.SubqueryType;
import org.apache.shardingsphere.sql.parser.sql.common.extractor.TableExtractor;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.item.ColumnOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.item.ExpressionOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.item.IndexOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.item.OrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.item.TextOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.JoinTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SubqueryTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.common.util.ColumnExtractor;
import org.apache.shardingsphere.sql.parser.sql.common.util.ExpressionExtractUtils;
import org.apache.shardingsphere.sql.parser.sql.common.util.SQLUtils;
import org.apache.shardingsphere.sql.parser.sql.common.util.SubqueryExtractUtils;
import org.apache.shardingsphere.sql.parser.sql.common.util.WhereExtractUtils;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Select SQL statement context.
 */
@Getter
@Setter
public final class SelectStatementContext extends CommonSQLStatementContext implements TableAvailable, WhereAvailable, ParameterAware {
    
    private final TablesContext tablesContext;
    
    private final ProjectionsContext projectionsContext;
    
    private final GroupByContext groupByContext;
    
    private final OrderByContext orderByContext;
    
    private final Map<Integer, SelectStatementContext> subqueryContexts;
    
    private final Collection<WhereSegment> whereSegments = new LinkedList<>();
    
    private final Collection<ColumnSegment> columnSegments = new LinkedList<>();
    
    private final boolean containsEnhancedTable;
    
    private SubqueryType subqueryType;
    
    private boolean needAggregateRewrite;
    
    private PaginationContext paginationContext;
    
    public SelectStatementContext(final ShardingSphereMetaData metaData, final List<Object> params, final SelectStatement sqlStatement, final String defaultDatabaseName) {
        super(sqlStatement);
        extractWhereSegments(whereSegments, sqlStatement);
        ColumnExtractor.extractColumnSegments(columnSegments, whereSegments);
        subqueryContexts = createSubqueryContexts(metaData, params, defaultDatabaseName);
        tablesContext = new TablesContext(getAllTableSegments(), subqueryContexts, getDatabaseType());
        String databaseName = tablesContext.getDatabaseName().orElse(defaultDatabaseName);
        groupByContext = new GroupByContextEngine().createGroupByContext(sqlStatement);
        orderByContext = new OrderByContextEngine().createOrderBy(sqlStatement, groupByContext);
        projectionsContext = new ProjectionsContextEngine(databaseName, getSchemas(metaData, databaseName), getDatabaseType())
                .createProjectionsContext(getSqlStatement().getFrom(), getSqlStatement().getProjections(), groupByContext, orderByContext);
        paginationContext = new PaginationContextEngine().createPaginationContext(sqlStatement, projectionsContext, params, whereSegments);
        containsEnhancedTable = isContainsEnhancedTable(metaData, databaseName, getTablesContext().getTableNames());
    }
    
    private boolean isContainsEnhancedTable(final ShardingSphereMetaData metaData, final String databaseName, final Collection<String> tableNames) {
        for (TableContainedRule each : getTableContainedRules(metaData, databaseName)) {
            for (String tableName : tableNames) {
                if (each.getEnhancedTableMapper().contains(tableName)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    private Collection<TableContainedRule> getTableContainedRules(final ShardingSphereMetaData metaData, final String databaseName) {
        if (null == databaseName) {
            ShardingSpherePreconditions.checkState(tablesContext.getSimpleTableSegments().isEmpty(), NoDatabaseSelectedException::new);
            return Collections.emptyList();
        }
        ShardingSphereDatabase database = metaData.getDatabase(databaseName);
        ShardingSpherePreconditions.checkNotNull(database, () -> new UnknownDatabaseException(databaseName));
        return database.getRuleMetaData().findRules(TableContainedRule.class);
    }
    
    private Map<Integer, SelectStatementContext> createSubqueryContexts(final ShardingSphereMetaData metaData, final List<Object> params, final String defaultDatabaseName) {
        Collection<SubquerySegment> subquerySegments = SubqueryExtractUtils.getSubquerySegments(getSqlStatement());
        Map<Integer, SelectStatementContext> result = new HashMap<>(subquerySegments.size(), 1F);
        for (SubquerySegment each : subquerySegments) {
            SelectStatementContext subqueryContext = new SelectStatementContext(metaData, params, each.getSelect(), defaultDatabaseName);
            subqueryContext.setSubqueryType(each.getSubqueryType());
            result.put(each.getStartIndex(), subqueryContext);
        }
        return result;
    }
    
    private Map<String, ShardingSphereSchema> getSchemas(final ShardingSphereMetaData metaData, final String databaseName) {
        if (null == databaseName) {
            ShardingSpherePreconditions.checkState(tablesContext.getSimpleTableSegments().isEmpty(), NoDatabaseSelectedException::new);
            return Collections.emptyMap();
        }
        ShardingSphereDatabase database = metaData.getDatabase(databaseName);
        ShardingSpherePreconditions.checkNotNull(database, () -> new UnknownDatabaseException(databaseName));
        return database.getSchemas();
    }
    
    /**
     * Judge whether contains join query or not.
     *
     * @return whether contains join query or not
     */
    public boolean isContainsJoinQuery() {
        return getSqlStatement().getFrom() instanceof JoinTableSegment;
    }
    
    /**
     * Judge whether contains subquery or not.
     *
     * @return whether contains subquery or not
     */
    public boolean isContainsSubquery() {
        return !subqueryContexts.isEmpty();
    }
    
    /**
     * Judge whether contains having or not.
     *
     * @return whether contains having or not
     */
    public boolean isContainsHaving() {
        return getSqlStatement().getHaving().isPresent();
    }
    
    /**
     * Judge whether contains combine or not.
     *
     * @return whether contains combine or not
     */
    public boolean isContainsCombine() {
        return getSqlStatement().getCombine().isPresent();
    }
    
    /**
     * Judge whether contains dollar parameter marker or not.
     * 
     * @return whether contains dollar parameter marker or not
     */
    public boolean isContainsDollarParameterMarker() {
        for (Projection each : projectionsContext.getProjections()) {
            if (each instanceof ParameterMarkerProjection && ParameterMarkerType.DOLLAR == ((ParameterMarkerProjection) each).getParameterMarkerType()) {
                return true;
            }
        }
        for (ParameterMarkerExpressionSegment each : getParameterMarkerExpressions()) {
            if (ParameterMarkerType.DOLLAR == each.getParameterMarkerType()) {
                return true;
            }
        }
        return false;
    }
    
    private Collection<ParameterMarkerExpressionSegment> getParameterMarkerExpressions() {
        Collection<ExpressionSegment> expressions = new LinkedList<>();
        for (WhereSegment each : whereSegments) {
            expressions.add(each.getExpr());
        }
        return ExpressionExtractUtils.getParameterMarkerExpressions(expressions);
    }
    
    /**
     * Judge whether contains partial distinct aggregation.
     * 
     * @return whether contains partial distinct aggregation
     */
    public boolean isContainsPartialDistinctAggregation() {
        Collection<Projection> aggregationProjections = projectionsContext.getProjections().stream().filter(AggregationProjection.class::isInstance).collect(Collectors.toList());
        Collection<AggregationDistinctProjection> aggregationDistinctProjections = projectionsContext.getAggregationDistinctProjections();
        return aggregationProjections.size() > 1 && !aggregationDistinctProjections.isEmpty() && aggregationProjections.size() != aggregationDistinctProjections.size();
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
            String columnLabel = SQLUtils.getExactlyValue(each.getColumnLabel());
            Preconditions.checkState(columnLabelIndexMap.containsKey(columnLabel), "Can't find index: %s, please add alias for aggregate selections", each);
            each.setIndex(columnLabelIndexMap.get(columnLabel));
            for (AggregationProjection derived : each.getDerivedAggregationProjections()) {
                String derivedColumnLabel = SQLUtils.getExactlyValue(derived.getColumnLabel());
                Preconditions.checkState(columnLabelIndexMap.containsKey(derivedColumnLabel), "Can't find index: %s", derived);
                derived.setIndex(columnLabelIndexMap.get(derivedColumnLabel));
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
            String columnLabel = getAlias(each.getSegment()).orElseGet(() -> getOrderItemText((TextOrderByItemSegment) each.getSegment()));
            Preconditions.checkState(columnLabelIndexMap.containsKey(columnLabel), "Can't find index: %s", each);
            if (columnLabelIndexMap.containsKey(columnLabel)) {
                each.setIndex(columnLabelIndexMap.get(columnLabel));
            }
        }
    }
    
    private Optional<String> getAlias(final OrderByItemSegment orderByItem) {
        if (projectionsContext.isUnqualifiedShorthandProjection()) {
            return Optional.empty();
        }
        String rawName = SQLUtils.getExactlyValue(((TextOrderByItemSegment) orderByItem).getText());
        for (Projection each : projectionsContext.getProjections()) {
            Optional<String> result = each.getAlias().map(IdentifierValue::getValue);
            if (SQLUtils.getExactlyExpression(rawName).equalsIgnoreCase(SQLUtils.getExactlyExpression(SQLUtils.getExactlyValue(each.getColumnName())))) {
                return result;
            }
            if (rawName.equalsIgnoreCase(result.orElse(null))) {
                return Optional.of(rawName);
            }
            if (isSameColumnName(each, rawName)) {
                return result;
            }
        }
        return Optional.empty();
    }
    
    private boolean isSameColumnName(final Projection projection, final String name) {
        return projection instanceof ColumnProjection && name.equalsIgnoreCase(((ColumnProjection) projection).getName().getValue());
    }
    
    private String getOrderItemText(final TextOrderByItemSegment orderByItemSegment) {
        if (orderByItemSegment instanceof ColumnOrderByItemSegment) {
            return SQLUtils.getExactlyValue(((ColumnOrderByItemSegment) orderByItemSegment).getColumn().getIdentifier().getValue());
        }
        return SQLUtils.getExactlyValue(((ExpressionOrderByItemSegment) orderByItemSegment).getExpression());
    }
    
    /**
     * Judge group by and order by sequence is same or not.
     *
     * @return group by and order by sequence is same or not
     */
    public boolean isSameGroupByAndOrderByItems() {
        return !groupByContext.getItems().isEmpty() && groupByContext.getItems().equals(orderByContext.getItems());
    }
    
    /**
     * Find column projection.
     * 
     * @param columnIndex column index
     * @return find column projection
     */
    public Optional<ColumnProjection> findColumnProjection(final int columnIndex) {
        List<Projection> expandProjections = projectionsContext.getExpandProjections();
        if (expandProjections.size() < columnIndex) {
            return Optional.empty();
        }
        Projection projection = expandProjections.get(columnIndex - 1);
        if (projection instanceof ColumnProjection) {
            return Optional.of((ColumnProjection) projection);
        }
        if (projection instanceof SubqueryProjection && ((SubqueryProjection) projection).getProjection() instanceof ColumnProjection) {
            return Optional.of((ColumnProjection) ((SubqueryProjection) projection).getProjection());
        }
        return Optional.empty();
    }
    
    @Override
    public SelectStatement getSqlStatement() {
        return (SelectStatement) super.getSqlStatement();
    }
    
    @Override
    public Collection<SimpleTableSegment> getAllTables() {
        return tablesContext.getSimpleTableSegments();
    }
    
    @Override
    public Collection<WhereSegment> getWhereSegments() {
        return whereSegments;
    }
    
    @Override
    public Collection<ColumnSegment> getColumnSegments() {
        return columnSegments;
    }
    
    private void extractWhereSegments(final Collection<WhereSegment> whereSegments, final SelectStatement selectStatement) {
        selectStatement.getWhere().ifPresent(whereSegments::add);
        whereSegments.addAll(WhereExtractUtils.getSubqueryWhereSegments(selectStatement));
        whereSegments.addAll(WhereExtractUtils.getJoinWhereSegments(selectStatement));
    }
    
    private Collection<TableSegment> getAllTableSegments() {
        TableExtractor tableExtractor = new TableExtractor();
        tableExtractor.extractTablesFromSelect(getSqlStatement());
        Collection<TableSegment> result = new LinkedList<>(tableExtractor.getRewriteTables());
        for (TableSegment each : tableExtractor.getTableContext()) {
            if (each instanceof SubqueryTableSegment) {
                result.add(each);
            }
        }
        return result;
    }
    
    @Override
    public void setUpParameters(final List<Object> params) {
        paginationContext = new PaginationContextEngine().createPaginationContext(getSqlStatement(), projectionsContext, params, whereSegments);
    }
}
