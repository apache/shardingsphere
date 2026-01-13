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

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.database.exception.core.exception.syntax.database.NoDatabaseSelectedException;
import org.apache.shardingsphere.database.exception.core.exception.syntax.database.UnknownDatabaseException;
import org.apache.shardingsphere.infra.binder.context.segment.select.groupby.GroupByContext;
import org.apache.shardingsphere.infra.binder.context.segment.select.groupby.engine.GroupByContextEngine;
import org.apache.shardingsphere.infra.binder.context.segment.select.orderby.OrderByContext;
import org.apache.shardingsphere.infra.binder.context.segment.select.orderby.OrderByItem;
import org.apache.shardingsphere.infra.binder.context.segment.select.orderby.engine.OrderByContextEngine;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.Projection;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.ProjectionsContext;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.engine.ProjectionsContextEngine;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.AggregationDistinctProjection;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.AggregationProjection;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.ColumnProjection;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.ParameterMarkerProjection;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.SubqueryProjection;
import org.apache.shardingsphere.infra.binder.context.segment.table.TablesContext;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rule.attribute.table.TableMapperRuleAttribute;
import org.apache.shardingsphere.sql.parser.statement.core.enums.ParameterMarkerType;
import org.apache.shardingsphere.sql.parser.statement.core.enums.SubqueryType;
import org.apache.shardingsphere.sql.parser.statement.core.extractor.ColumnExtractor;
import org.apache.shardingsphere.sql.parser.statement.core.extractor.ExpressionExtractor;
import org.apache.shardingsphere.sql.parser.statement.core.extractor.SubqueryExtractor;
import org.apache.shardingsphere.sql.parser.statement.core.extractor.TableExtractor;
import org.apache.shardingsphere.sql.parser.statement.core.extractor.WhereExtractor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.item.ColumnOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.item.ExpressionOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.item.IndexOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.item.OrderByItemSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.item.TextOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.bound.ColumnSegmentBoundInfo;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.JoinTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SubqueryTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.util.SQLUtils;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Select SQL statement base context.
 */
@Getter
@Setter
public final class SelectStatementBaseContext implements SQLStatementContext {
    
    private final SelectStatement sqlStatement;
    
    private final TablesContext tablesContext;
    
    private final ProjectionsContext projectionsContext;
    
    private final GroupByContext groupByContext;
    
    private final OrderByContext orderByContext;
    
    private final Map<Integer, SelectStatementContext> subqueryContexts;
    
    private final Collection<WhereSegment> whereSegments;
    
    private final Collection<ColumnSegment> columnSegments;
    
    private final Collection<BinaryOperationExpression> joinConditions = new LinkedList<>();
    
    private final boolean containsEnhancedTable;
    
    private SubqueryType subqueryType;
    
    private boolean needAggregateRewrite;
    
    public SelectStatementBaseContext(final SelectStatement sqlStatement, final ShardingSphereMetaData metaData, final String currentDatabaseName, final Collection<TableSegment> inheritedTables) {
        this.sqlStatement = sqlStatement;
        whereSegments = createWhereSegments(sqlStatement);
        columnSegments = ColumnExtractor.extractColumnSegments(whereSegments);
        Collection<TableSegment> tableSegments = getAllTableSegments(inheritedTables);
        ExpressionExtractor.extractJoinConditions(joinConditions, whereSegments);
        subqueryContexts = createSubqueryContexts(metaData, currentDatabaseName, tableSegments);
        tablesContext = new TablesContext(tableSegments, subqueryContexts);
        groupByContext = new GroupByContextEngine().createGroupByContext(sqlStatement);
        orderByContext = new OrderByContextEngine(sqlStatement.getDatabaseType()).createOrderBy(sqlStatement, groupByContext);
        projectionsContext = new ProjectionsContextEngine(sqlStatement.getDatabaseType()).createProjectionsContext(sqlStatement.getProjections(), groupByContext, orderByContext);
        containsEnhancedTable = isContainsEnhancedTable(metaData, tablesContext.getDatabaseNames(), currentDatabaseName);
    }
    
    private Collection<WhereSegment> createWhereSegments(final SelectStatement selectStatement) {
        Collection<WhereSegment> result = new LinkedList<>();
        selectStatement.getWhere().ifPresent(result::add);
        result.addAll(WhereExtractor.extractSubqueryWhereSegments(selectStatement));
        result.addAll(WhereExtractor.extractJoinWhereSegments(selectStatement));
        return result;
    }
    
    private Collection<TableSegment> getAllTableSegments(final Collection<TableSegment> inheritedTables) {
        TableExtractor tableExtractor = new TableExtractor();
        appendInheritedSimpleTables(inheritedTables, tableExtractor);
        tableExtractor.extractTablesFromSelect(sqlStatement);
        Collection<TableSegment> result = new LinkedList<>(tableExtractor.getRewriteTables());
        for (TableSegment each : tableExtractor.getTableContext()) {
            if (each instanceof SubqueryTableSegment) {
                result.add(each);
            }
        }
        return result;
    }
    
    private void appendInheritedSimpleTables(final Collection<TableSegment> inheritedTables, final TableExtractor tableExtractor) {
        for (TableSegment each : inheritedTables) {
            if (each instanceof SimpleTableSegment) {
                tableExtractor.getTableContext().add(each);
            }
        }
    }
    
    private boolean isContainsEnhancedTable(final ShardingSphereMetaData metaData, final Collection<String> databaseNames, final String currentDatabaseName) {
        for (String each : databaseNames) {
            if (isContainsEnhancedTable(metaData, each, getTablesContext().getTableNames())) {
                return true;
            }
        }
        return null != currentDatabaseName && isContainsEnhancedTable(metaData, currentDatabaseName, getTablesContext().getTableNames());
    }
    
    private boolean isContainsEnhancedTable(final ShardingSphereMetaData metaData, final String databaseName, final Collection<String> tableNames) {
        for (TableMapperRuleAttribute each : getTableMapperRuleAttributes(metaData, databaseName)) {
            for (String tableName : tableNames) {
                if (each.getEnhancedTableNames().contains(tableName)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    private Collection<TableMapperRuleAttribute> getTableMapperRuleAttributes(final ShardingSphereMetaData metaData, final String databaseName) {
        if (null == databaseName) {
            ShardingSpherePreconditions.checkMustEmpty(tablesContext.getSimpleTables(), NoDatabaseSelectedException::new);
            return Collections.emptyList();
        }
        ShardingSphereDatabase database = metaData.getDatabase(databaseName);
        ShardingSpherePreconditions.checkNotNull(database, () -> new UnknownDatabaseException(databaseName));
        return database.getRuleMetaData().getAttributes(TableMapperRuleAttribute.class);
    }
    
    private Map<Integer, SelectStatementContext> createSubqueryContexts(final ShardingSphereMetaData metaData, final String currentDatabaseName, final Collection<TableSegment> tableSegments) {
        Collection<SubquerySegment> subquerySegments = SubqueryExtractor.extractSubquerySegments(sqlStatement, false);
        Map<Integer, SelectStatementContext> result = new HashMap<>(subquerySegments.size(), 1F);
        for (SubquerySegment each : subquerySegments) {
            SelectStatementContext subqueryContext = new SelectStatementContext(each.getSelect(), metaData, currentDatabaseName, tableSegments);
            each.getSelect().getSubqueryType().ifPresent(subqueryContext::setSubqueryType);
            result.put(each.getStartIndex(), subqueryContext);
        }
        return result;
    }
    
    /**
     * Judge whether contains join query or not.
     *
     * @return whether contains join query or not
     */
    public boolean isContainsJoinQuery() {
        return sqlStatement.getFrom().isPresent() && sqlStatement.getFrom().get() instanceof JoinTableSegment;
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
        return sqlStatement.getHaving().isPresent();
    }
    
    /**
     * Judge whether contains combine or not.
     *
     * @return whether contains combine or not
     */
    public boolean isContainsCombine() {
        return sqlStatement.getCombine().isPresent();
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
        return ExpressionExtractor.getParameterMarkerExpressions(expressions);
    }
    
    /**
     * Judge whether contains partial distinct aggregation.
     *
     * @return whether contains partial distinct aggregation
     */
    public boolean isContainsPartialDistinctAggregation() {
        Collection<Projection> aggregationProjections = getAggregationProjections(projectionsContext.getProjections());
        Collection<AggregationDistinctProjection> aggregationDistinctProjections = projectionsContext.getAggregationDistinctProjections();
        return aggregationProjections.size() > 1 && !aggregationDistinctProjections.isEmpty() && aggregationProjections.size() != aggregationDistinctProjections.size();
    }
    
    private Collection<Projection> getAggregationProjections(final Collection<Projection> projections) {
        Collection<Projection> result = new LinkedList<>();
        for (Projection each : projections) {
            if (each instanceof AggregationProjection) {
                result.add(each);
            }
        }
        return result;
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
        for (AggregationProjection each : projectionsContext.getExpandAggregationProjections()) {
            String columnLabel = SQLUtils.getExactlyValue(each.getAlias().map(IdentifierValue::getValue).orElse(each.getColumnName()));
            Preconditions.checkState(columnLabelIndexMap.containsKey(columnLabel), "Can't find index: %s, please add alias for aggregate selections", each);
            each.setIndex(columnLabelIndexMap.get(columnLabel));
            for (AggregationProjection derived : each.getDerivedAggregationProjections()) {
                String derivedColumnLabel = SQLUtils.getExactlyValue(
                        derived.getAlias().map(IdentifierValue::getValue).orElse(derived.getColumnName()));
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
            if (SQLUtils.getExactlyExpression(rawName).equalsIgnoreCase(SQLUtils.getExactlyExpression(SQLUtils.getExactlyValue(each.getExpression())))) {
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
     * Find column bound info.
     *
     * @param columnIndex column index
     * @return column bound info
     */
    public Optional<ColumnSegmentBoundInfo> findColumnBoundInfo(final int columnIndex) {
        List<Projection> expandProjections = projectionsContext.getExpandProjections();
        if (expandProjections.size() < columnIndex) {
            return Optional.empty();
        }
        Projection projection = expandProjections.get(columnIndex - 1);
        if (projection instanceof ColumnProjection) {
            return Optional.of(((ColumnProjection) projection).getColumnBoundInfo());
        }
        if (projection instanceof SubqueryProjection && ((SubqueryProjection) projection).getProjection() instanceof ColumnProjection) {
            return Optional.of(((ColumnProjection) ((SubqueryProjection) projection).getProjection()).getColumnBoundInfo());
        }
        return Optional.empty();
    }
    
    /**
     * Judge whether sql statement contains table subquery segment or not.
     *
     * @return whether sql statement contains table subquery segment or not
     */
    public boolean containsTableSubquery() {
        return sqlStatement.getFrom().isPresent() && sqlStatement.getFrom().get() instanceof SubqueryTableSegment || sqlStatement.getWith().isPresent();
    }
    
    /**
     * Judge whether contains derived projections.
     *
     * @return contains derived projections or not
     */
    public boolean containsDerivedProjections() {
        return containsEnhancedTable && !projectionsContext.getExpandProjections().isEmpty();
    }
}
