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
import org.apache.shardingsphere.infra.binder.segment.table.TablesContext;
import org.apache.shardingsphere.infra.binder.statement.CommonSQLStatementContext;
import org.apache.shardingsphere.infra.binder.type.TableAvailable;
import org.apache.shardingsphere.infra.binder.type.WhereAvailable;
import org.apache.shardingsphere.infra.exception.SchemaNotExistedException;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.sql.parser.sql.common.extractor.TableExtractor;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.AggregationProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.item.ColumnOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.item.ExpressionOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.item.IndexOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.item.TextOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.JoinTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SubqueryTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.common.util.SQLUtil;
import org.apache.shardingsphere.sql.parser.sql.common.util.SubqueryExtractUtil;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;

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
    
    private final OrderByContext orderByContext;
    
    private final PaginationContext paginationContext;
    
    private final boolean containsSubquery;
    
    private final boolean needExecuteByCalcite;

    public SelectStatementContext(final Map<String, ShardingSphereMetaData> metaDataMap, final List<Object> parameters, final SelectStatement sqlStatement, final String defaultSchemaName) {
        super(sqlStatement);
        tablesContext = new TablesContext(getAllSimpleTableSegments());
        ShardingSphereSchema schema = getSchema(metaDataMap, defaultSchemaName);
        groupByContext = new GroupByContextEngine().createGroupByContext(sqlStatement);
        orderByContext = new OrderByContextEngine().createOrderBy(schema, sqlStatement, groupByContext);
        projectionsContext = new ProjectionsContextEngine(schema, getDatabaseType())
                .createProjectionsContext(getFromSimpleTableSegments(), getSqlStatement().getProjections(), groupByContext, orderByContext);
        paginationContext = new PaginationContextEngine().createPaginationContext(sqlStatement, projectionsContext, parameters);
        Collection<SubquerySegment> subquerySegments = SubqueryExtractUtil.getSubquerySegments(getSqlStatement());
        containsSubquery = !subquerySegments.isEmpty();
        needExecuteByCalcite = checkNeedExecuteByCalcite(subquerySegments);
    }
    
    private boolean checkNeedExecuteByCalcite(final Collection<SubquerySegment> subquerySegments) {
        return isContainsHaving() || isContainsSubqueryAggregation(subquerySegments) || isContainsPartialDistinctAggregation();
    }
    
    private ShardingSphereSchema getSchema(final Map<String, ShardingSphereMetaData> metaDataMap, final String defaultSchemaName) {
        String schemaName = tablesContext.getSchemaName().orElse(defaultSchemaName);
        ShardingSphereMetaData metaData = metaDataMap.get(schemaName);
        if (null == metaData) {
            throw new SchemaNotExistedException(schemaName);
        }
        return metaData.getSchema();
    }
    
    private boolean isContainsSubqueryAggregation(final Collection<SubquerySegment> subquerySegments) {
        return subquerySegments.stream().flatMap(each -> each.getSelect().getProjections().getProjections().stream()).anyMatch(each -> each instanceof AggregationProjectionSegment);
    }
    
    /**
     * Judge whether contains join query or not.
     *
     * @return whether contains join query or not
     */
    public boolean isContainsJoinQuery() {
        return getSqlStatement().getFrom() instanceof JoinTableSegment;
    }
    
    private boolean isContainsHaving() {
        return getSqlStatement().getHaving().isPresent();
    }
    
    private boolean isContainsPartialDistinctAggregation() {
        Collection<Projection> aggregationProjections = projectionsContext.getProjections().stream().filter(each -> each instanceof AggregationProjection).collect(Collectors.toList());
        Collection<AggregationDistinctProjection> aggregationDistinctProjections = projectionsContext.getAggregationDistinctProjections();
        return aggregationProjections.size() > 1 && aggregationDistinctProjections.size() > 0 && aggregationProjections.size() != aggregationDistinctProjections.size();
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
            String columnLabel = SQLUtil.getExactlyValue(each.getColumnLabel());
            Preconditions.checkState(columnLabelIndexMap.containsKey(columnLabel), "Can't find index: %s, please add alias for aggregate selections", each);
            each.setIndex(columnLabelIndexMap.get(columnLabel));
            for (AggregationProjection derived : each.getDerivedAggregationProjections()) {
                String derivedColumnLabel = SQLUtil.getExactlyValue(derived.getColumnLabel());
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
        return tablesContext.getOriginalTables();
    }
    
    @Override
    public Optional<WhereSegment> getWhere() {
        return getSqlStatement().getWhere();
    }
    
    private Collection<SimpleTableSegment> getAllSimpleTableSegments() {
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
        return tableSegments.stream().filter(each -> each instanceof SubqueryTableSegment).map(each -> new SimpleTableSegment(
                new TableNameSegment(each.getStartIndex(), each.getStopIndex(), new IdentifierValue(each.getAlias().orElse(""))))).collect(Collectors.toList());
    }
}
