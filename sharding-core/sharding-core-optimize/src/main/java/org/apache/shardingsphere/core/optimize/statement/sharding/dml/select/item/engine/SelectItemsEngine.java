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

package org.apache.shardingsphere.core.optimize.statement.sharding.dml.select.item.engine;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.core.constant.AggregationType;
import org.apache.shardingsphere.core.metadata.table.ShardingTableMetaData;
import org.apache.shardingsphere.core.optimize.statement.sharding.dml.select.groupby.GroupBy;
import org.apache.shardingsphere.core.optimize.statement.sharding.dml.select.item.SelectItems;
import org.apache.shardingsphere.core.optimize.statement.sharding.dml.select.orderby.OrderBy;
import org.apache.shardingsphere.core.optimize.statement.sharding.dml.select.orderby.OrderByItem;
import org.apache.shardingsphere.core.parse.constant.DerivedColumn;
import org.apache.shardingsphere.core.parse.sql.context.selectitem.AggregationDistinctSelectItem;
import org.apache.shardingsphere.core.parse.sql.context.selectitem.AggregationSelectItem;
import org.apache.shardingsphere.core.parse.sql.context.selectitem.DerivedCommonSelectItem;
import org.apache.shardingsphere.core.parse.sql.context.selectitem.DistinctSelectItem;
import org.apache.shardingsphere.core.parse.sql.context.selectitem.SelectItem;
import org.apache.shardingsphere.core.parse.sql.context.selectitem.StarSelectItem;
import org.apache.shardingsphere.core.parse.sql.context.table.Table;
import org.apache.shardingsphere.core.parse.sql.segment.dml.SelectItemsSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.item.AggregationDistinctSelectItemSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.item.AggregationSelectItemSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.item.ColumnSelectItemSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.item.ExpressionSelectItemSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.item.SelectItemSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.item.ShorthandSelectItemSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.order.item.ColumnOrderByItemSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.order.item.IndexOrderByItemSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.order.item.OrderByItemSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.order.item.TextOrderByItemSegment;
import org.apache.shardingsphere.core.parse.sql.statement.SQLStatement;
import org.apache.shardingsphere.core.parse.sql.statement.dml.SelectStatement;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Set;

/**
 * Select items engine.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class SelectItemsEngine {
    
    private final ShardingTableMetaData shardingTableMetaData;
    
    private final SelectItemEngine selectItemEngine = new SelectItemEngine();
    
    /**
     * Create select items.
     * 
     * @param selectStatement SQL statement
     * @param groupBy group by
     * @param orderBy order by
     * @return select items
     */
    public SelectItems createSelectItems(final SelectStatement selectStatement, final GroupBy groupBy, final OrderBy orderBy) {
        Optional<SelectItemsSegment> selectItemsSegment = selectStatement.findSQLSegment(SelectItemsSegment.class);
        Preconditions.checkState(selectItemsSegment.isPresent());
        SelectItems result = new SelectItems(selectItemsSegment.get().getStopIndex());
        Collection<SelectItem> items = getSelectItemList(selectItemsSegment.get(), selectStatement);
        items.addAll(getDerivedColumns(selectStatement, items, groupBy, orderBy));
        result.getItems().addAll(appendAverageDerivedColumns(items));
        result.setContainStar(1 == items.size() && items.iterator().next() instanceof StarSelectItem);
        return result;
    }
    
    private Collection<SelectItem> getSelectItemList(final SelectItemsSegment selectItemsSegment, final SQLStatement sqlStatement) {
        return selectItemsSegment.isHasRowDistinct() ? getDistinctSelectItemList(selectItemsSegment, sqlStatement) : getCommonSelectItemList(selectItemsSegment, sqlStatement);
    }
    
    private Collection<SelectItem> getDistinctSelectItemList(final SelectItemsSegment selectItemsSegment, final SQLStatement sqlStatement) {
        Collection<SelectItem> result = new LinkedList<>();
        Iterator<SelectItemSegment> selectItemSegmentIterator = selectItemsSegment.getSelectItems().iterator();
        SelectItemSegment firstSelectItemSegment = selectItemSegmentIterator.next();
        Set<String> distinctColumnNames = new LinkedHashSet<>();
        DistinctSelectItem distinctSelectItem = null;
        int derivedColumnCount = 0;
        if (firstSelectItemSegment instanceof ShorthandSelectItemSegment) {
            Optional<SelectItem> selectItem = selectItemEngine.createSelectItem(firstSelectItemSegment, sqlStatement);
            if (selectItem.isPresent()) {
                result.add(selectItem.get());
            }
            result.add(new DistinctSelectItem(distinctColumnNames, null));
        } else if (firstSelectItemSegment instanceof ColumnSelectItemSegment) {
            ColumnSelectItemSegment columnSelectItemSegment = (ColumnSelectItemSegment) firstSelectItemSegment;
            distinctSelectItem = new DistinctSelectItem(distinctColumnNames, columnSelectItemSegment.getAlias().orNull());
            result.add(distinctSelectItem);
            distinctColumnNames.add(columnSelectItemSegment.getName());
        } else if (firstSelectItemSegment instanceof ExpressionSelectItemSegment) {
            distinctSelectItem = createDistinctExpressionItem(sqlStatement, distinctColumnNames, (ExpressionSelectItemSegment) firstSelectItemSegment);
            result.add(distinctSelectItem);
        } else {
            derivedColumnCount = setDistinctFunctionAlias(firstSelectItemSegment, derivedColumnCount);
            Optional<SelectItem> selectItem = selectItemEngine.createSelectItem(firstSelectItemSegment, sqlStatement);
            if (selectItem.isPresent()) {
                result.add(selectItem.get());
            }
        }
        while (selectItemSegmentIterator.hasNext()) {
            SelectItemSegment nextSelectItemSegment = selectItemSegmentIterator.next();
            Optional<SelectItem> selectItem = selectItemEngine.createSelectItem(nextSelectItemSegment, sqlStatement);
            if (selectItem.isPresent()) {
                result.add(selectItem.get());
            }
            if (nextSelectItemSegment instanceof ColumnSelectItemSegment) {
                derivedColumnCount = setDistinctFunctionAlias(nextSelectItemSegment, derivedColumnCount);
                distinctColumnNames.add(((ColumnSelectItemSegment) nextSelectItemSegment).getName());
            }
        }
        if (null != distinctSelectItem) {
            distinctSelectItem.getDistinctColumnNames().addAll(distinctColumnNames);
        }
        return result;
    }
    
    private int setDistinctFunctionAlias(final SelectItemSegment selectItemSegment, final int derivedColumnCount) {
        if (selectItemSegment instanceof AggregationSelectItemSegment) {
            AggregationSelectItemSegment aggregationSelectItemSegment = (AggregationSelectItemSegment) selectItemSegment;
            Optional<String> alias = aggregationSelectItemSegment.getAlias();
            if (aggregationSelectItemSegment instanceof AggregationDistinctSelectItemSegment && !alias.isPresent()) {
                ((AggregationSelectItemSegment) selectItemSegment).setAlias(DerivedColumn.AGGREGATION_DISTINCT_DERIVED.getDerivedColumnAlias(derivedColumnCount));
                return derivedColumnCount + 1;
            }
        }
        return derivedColumnCount;
    }
    
    private DistinctSelectItem createDistinctExpressionItem(final SQLStatement sqlStatement, 
                                                            final Set<String> distinctColumnNames, final ExpressionSelectItemSegment expressionSelectItemSegment) {
        DistinctSelectItem result = new DistinctSelectItem(distinctColumnNames, expressionSelectItemSegment.getAlias().orNull());
        String expression = sqlStatement.getLogicSQL().substring(expressionSelectItemSegment.getStartIndex(), expressionSelectItemSegment.getStopIndex() + 1);
        int leftParenIndex = expression.indexOf("(");
        if (-1 != leftParenIndex) {
            int rightParenIndex = expression.lastIndexOf(")");
            if (-1 == rightParenIndex) {
                rightParenIndex = expression.length();
            }
            distinctColumnNames.add(expression.substring(leftParenIndex + 1, rightParenIndex));
        }
        return result;
    }
    
    private Collection<SelectItem> getCommonSelectItemList(final SelectItemsSegment selectItemsSegment, final SQLStatement sqlStatement) {
        Collection<SelectItem> result = new LinkedList<>();
        for (SelectItemSegment each : selectItemsSegment.getSelectItems()) {
            Optional<SelectItem> selectItem = selectItemEngine.createSelectItem(each, sqlStatement);
            if (selectItem.isPresent()) {
                result.add(selectItem.get());
            }
        }
        return result;
    }
    
    private Collection<SelectItem> getDerivedColumns(final SelectStatement selectStatement, final Collection<SelectItem> items, final GroupBy groupBy, final OrderBy orderBy) {
        Collection<SelectItem> result = new LinkedList<>();
        if (!groupBy.getItems().isEmpty()) {
            result.addAll(appendDerivedGroupColumns(selectStatement, items, groupBy.getItems()));
        }
        if (!orderBy.getItems().isEmpty()) {
            result.addAll(appendDerivedOrderColumns(selectStatement, items, orderBy.getItems()));
        }
        return result;
    }
    
    private Collection<SelectItem> appendDerivedOrderColumns(final SelectStatement selectStatement, final Collection<SelectItem> items, final Collection<OrderByItem> orderItems) {
        Collection<SelectItem> result = new LinkedList<>();
        int derivedColumnOffset = 0;
        for (OrderByItem each : orderItems) {
            if (!containsItem(selectStatement, items, each.getSegment())) {
                result.add(new DerivedCommonSelectItem(((TextOrderByItemSegment) each.getSegment()).getText(), DerivedColumn.ORDER_BY_ALIAS.getDerivedColumnAlias(derivedColumnOffset++)));
            }
        }
        return result;
    }
    
    private Collection<SelectItem> appendDerivedGroupColumns(final SelectStatement selectStatement, final Collection<SelectItem> items, final Collection<OrderByItem> orderByItems) {
        Collection<SelectItem> result = new LinkedList<>();
        int derivedColumnOffset = 0;
        for (OrderByItem each : orderByItems) {
            if (!containsItem(selectStatement, items, each.getSegment())) {
                result.add(new DerivedCommonSelectItem(((TextOrderByItemSegment) each.getSegment()).getText(), DerivedColumn.GROUP_BY_ALIAS.getDerivedColumnAlias(derivedColumnOffset++)));
            }
        }
        return result;
    }
    
    private boolean containsItem(final SelectStatement selectStatement, final Collection<SelectItem> items, final OrderByItemSegment orderByItemSegment) {
        return orderByItemSegment instanceof IndexOrderByItemSegment
                || containsItemInStarSelectItems(selectStatement, items, orderByItemSegment) || containsItemInSelectItems(items, orderByItemSegment);
    }
    
    private boolean containsItemInStarSelectItems(final SelectStatement selectStatement, final Collection<SelectItem> items, final OrderByItemSegment orderByItemSegment) {
        return hasUnqualifiedStarSelectItem(items)
                || containsItemWithOwnerInStarSelectItems(selectStatement, items, orderByItemSegment) || containsItemWithoutOwnerInStarSelectItems(selectStatement, items, orderByItemSegment);
    }
    
    private boolean hasUnqualifiedStarSelectItem(final Collection<SelectItem> items) {
        for (SelectItem each : items) {
            if (each instanceof StarSelectItem && !((StarSelectItem) each).getOwner().isPresent()) {
                return true;
            }
        }
        return false;
    }
    
    private boolean containsItemWithOwnerInStarSelectItems(final SelectStatement selectStatement, final Collection<SelectItem> items, final OrderByItemSegment orderItem) {
        return orderItem instanceof ColumnOrderByItemSegment && ((ColumnOrderByItemSegment) orderItem).getColumn().getOwner().isPresent()
                && findStarSelectItem(selectStatement, items, ((ColumnOrderByItemSegment) orderItem).getColumn().getOwner().get().getName()).isPresent();
    }
    
    private Optional<StarSelectItem> findStarSelectItem(final SelectStatement selectStatement, final Collection<SelectItem> items, final String tableNameOrAlias) {
        Optional<Table> table = selectStatement.getTables().find(tableNameOrAlias);
        if (!table.isPresent()) {
            return Optional.absent();
        }
        for (SelectItem each : items) {
            if (!(each instanceof StarSelectItem)) {
                continue;
            }
            StarSelectItem starSelectItem = (StarSelectItem) each;
            if (starSelectItem.getOwner().isPresent() && selectStatement.getTables().find(starSelectItem.getOwner().get()).equals(table)) {
                return Optional.of(starSelectItem);
            }
        }
        return Optional.absent();
    }
    
    private boolean containsItemWithoutOwnerInStarSelectItems(final SelectStatement selectStatement, final Collection<SelectItem> items, final OrderByItemSegment orderItem) {
        if (!(orderItem instanceof ColumnOrderByItemSegment)) {
            return false;
        }
        if (!((ColumnOrderByItemSegment) orderItem).getColumn().getOwner().isPresent()) {
            for (StarSelectItem each : getQualifiedStarSelectItems(items)) {
                if (isSameSelectItem(selectStatement, each, (ColumnOrderByItemSegment) orderItem)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    private Collection<StarSelectItem> getQualifiedStarSelectItems(final Collection<SelectItem> items) {
        Collection<StarSelectItem> result = new LinkedList<>();
        for (SelectItem each : items) {
            if (each instanceof StarSelectItem && ((StarSelectItem) each).getOwner().isPresent()) {
                result.add((StarSelectItem) each);
            }
        }
        return result;
    }
    
    private boolean isSameSelectItem(final SelectStatement selectStatement, final StarSelectItem starSelectItem, final ColumnOrderByItemSegment orderItem) {
        Preconditions.checkState(starSelectItem.getOwner().isPresent());
        Optional<Table> table = selectStatement.getTables().find(starSelectItem.getOwner().get());
        return table.isPresent() && shardingTableMetaData.containsColumn(table.get().getName(), orderItem.getColumn().getName());
    }
    
    private boolean containsItemInSelectItems(final Collection<SelectItem> items, final OrderByItemSegment orderItem) {
        for (SelectItem each : items) {
            if (orderItem instanceof IndexOrderByItemSegment) {
                return true;
            }
            if (containsItemInDistinctItems(each, (TextOrderByItemSegment) orderItem)
                    || isSameAlias(each, (TextOrderByItemSegment) orderItem) || isSameQualifiedName(each, (TextOrderByItemSegment) orderItem)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean containsItemInDistinctItems(final SelectItem selectItem, final TextOrderByItemSegment orderItem) {
        return selectItem instanceof DistinctSelectItem && ((DistinctSelectItem) selectItem).getDistinctColumnNames().contains(orderItem.getText());
    }
    
    private boolean isSameAlias(final SelectItem selectItem, final TextOrderByItemSegment orderItem) {
        return selectItem.getAlias().isPresent() && (orderItem.getText().equalsIgnoreCase(selectItem.getAlias().get()) || orderItem.getText().equalsIgnoreCase(selectItem.getExpression()));
    }
    
    private boolean isSameQualifiedName(final SelectItem selectItem, final TextOrderByItemSegment orderItem) {
        return !selectItem.getAlias().isPresent() && selectItem.getExpression().equalsIgnoreCase(orderItem.getText());
    }
    
    private Collection<SelectItem> appendAverageDerivedColumns(final Collection<SelectItem> items) {
        Collection<SelectItem> result = new LinkedList<>(items);
        int derivedColumnOffset = 0;
        for (SelectItem each : items) {
            if (each instanceof AggregationSelectItem && AggregationType.AVG == ((AggregationSelectItem) each).getType()) {
                appendAverageDerivedColumns(derivedColumnOffset, each);
                // TODO replace avg to constant, avoid calculate useless avg
                derivedColumnOffset++;
            }
        }
        return result;
    }
    
    private void appendAverageDerivedColumns(final int derivedColumnOffset, final SelectItem selectItem) {
        if (selectItem instanceof AggregationDistinctSelectItem) {
            appendDerivedAggregationDistinctSelectItems((AggregationDistinctSelectItem) selectItem, derivedColumnOffset);
        } else {
            appendDerivedAggregationSelectItems((AggregationSelectItem) selectItem, derivedColumnOffset);
        }
    }
    
    private void appendDerivedAggregationDistinctSelectItems(final AggregationDistinctSelectItem averageDistinctSelectItem, final int derivedColumnOffset) {
        String countAlias = DerivedColumn.AVG_COUNT_ALIAS.getDerivedColumnAlias(derivedColumnOffset);
        AggregationDistinctSelectItem countDistinctSelectItem = new AggregationDistinctSelectItem(
                AggregationType.COUNT, averageDistinctSelectItem.getInnerExpression(), countAlias, averageDistinctSelectItem.getDistinctColumnName());
        String sumAlias = DerivedColumn.AVG_SUM_ALIAS.getDerivedColumnAlias(derivedColumnOffset);
        AggregationDistinctSelectItem sumDistinctSelectItem = new AggregationDistinctSelectItem(
                AggregationType.SUM, averageDistinctSelectItem.getInnerExpression(), sumAlias, averageDistinctSelectItem.getDistinctColumnName());
        averageDistinctSelectItem.getDerivedAggregationItems().clear();
        averageDistinctSelectItem.getDerivedAggregationItems().add(countDistinctSelectItem);
        averageDistinctSelectItem.getDerivedAggregationItems().add(sumDistinctSelectItem);
    }
    
    private void appendDerivedAggregationSelectItems(final AggregationSelectItem averageSelectItem, final int derivedColumnOffset) {
        String countAlias = DerivedColumn.AVG_COUNT_ALIAS.getDerivedColumnAlias(derivedColumnOffset);
        AggregationSelectItem countSelectItem = new AggregationSelectItem(AggregationType.COUNT, averageSelectItem.getInnerExpression(), countAlias);
        String sumAlias = DerivedColumn.AVG_SUM_ALIAS.getDerivedColumnAlias(derivedColumnOffset);
        AggregationSelectItem sumSelectItem = new AggregationSelectItem(AggregationType.SUM, averageSelectItem.getInnerExpression(), sumAlias);
        averageSelectItem.getDerivedAggregationItems().clear();
        averageSelectItem.getDerivedAggregationItems().add(countSelectItem);
        averageSelectItem.getDerivedAggregationItems().add(sumSelectItem);
    }
}
