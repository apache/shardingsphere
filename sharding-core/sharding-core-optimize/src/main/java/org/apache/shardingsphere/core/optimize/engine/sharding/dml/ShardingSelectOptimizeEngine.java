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

package org.apache.shardingsphere.core.optimize.engine.sharding.dml;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import org.apache.shardingsphere.core.constant.AggregationType;
import org.apache.shardingsphere.core.metadata.table.ShardingTableMetaData;
import org.apache.shardingsphere.core.optimize.engine.OptimizeEngine;
import org.apache.shardingsphere.core.optimize.statement.encrypt.condition.WhereClauseEncryptConditionEngine;
import org.apache.shardingsphere.core.optimize.statement.sharding.dml.condition.engine.WhereClauseShardingConditionEngine;
import org.apache.shardingsphere.core.optimize.statement.sharding.dml.select.Pagination;
import org.apache.shardingsphere.core.optimize.statement.sharding.dml.select.ShardingSelectOptimizedStatement;
import org.apache.shardingsphere.core.optimize.statement.sharding.dml.select.groupby.GroupBy;
import org.apache.shardingsphere.core.optimize.statement.sharding.dml.select.groupby.GroupByEngine;
import org.apache.shardingsphere.core.optimize.statement.sharding.dml.select.orderby.OrderByEngine;
import org.apache.shardingsphere.core.optimize.statement.sharding.dml.select.orderby.OrderByItem;
import org.apache.shardingsphere.core.parse.constant.DerivedColumn;
import org.apache.shardingsphere.core.parse.sql.context.selectitem.AggregationDistinctSelectItem;
import org.apache.shardingsphere.core.parse.sql.context.selectitem.AggregationSelectItem;
import org.apache.shardingsphere.core.parse.sql.context.selectitem.DerivedCommonSelectItem;
import org.apache.shardingsphere.core.parse.sql.context.selectitem.DistinctSelectItem;
import org.apache.shardingsphere.core.parse.sql.context.selectitem.SelectItem;
import org.apache.shardingsphere.core.parse.sql.context.selectitem.StarSelectItem;
import org.apache.shardingsphere.core.parse.sql.context.table.Table;
import org.apache.shardingsphere.core.parse.sql.segment.dml.order.item.ColumnOrderByItemSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.order.item.IndexOrderByItemSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.order.item.OrderByItemSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.order.item.TextOrderByItemSegment;
import org.apache.shardingsphere.core.parse.sql.statement.dml.SelectStatement;
import org.apache.shardingsphere.core.rule.ShardingRule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

/**
 * Select optimize engine for sharding.
 *
 * @author zhangliang
 */
public final class ShardingSelectOptimizeEngine implements OptimizeEngine {
    
    private final ShardingTableMetaData shardingTableMetaData;
    
    private final SelectStatement selectStatement;
    
    private final List<Object> parameters;
    
    private final WhereClauseShardingConditionEngine shardingConditionEngine;
    
    private final WhereClauseEncryptConditionEngine encryptConditionEngine;
    
    private final OrderByEngine orderByEngine;
    
    private final GroupByEngine groupByEngine;
    
    public ShardingSelectOptimizeEngine(final ShardingRule shardingRule, final ShardingTableMetaData shardingTableMetaData, final SelectStatement selectStatement, final List<Object> parameters) {
        this.shardingTableMetaData = shardingTableMetaData;
        this.selectStatement = selectStatement;
        this.parameters = parameters;
        shardingConditionEngine = new WhereClauseShardingConditionEngine(shardingRule, shardingTableMetaData);
        encryptConditionEngine = new WhereClauseEncryptConditionEngine(shardingRule.getEncryptRule(), shardingTableMetaData);
        orderByEngine = new OrderByEngine();
        groupByEngine = new GroupByEngine();
    }
    
    @Override
    public ShardingSelectOptimizedStatement optimize() {
        Collection<SelectItem> items = new LinkedHashSet<>(selectStatement.getItems());
        Collection<OrderByItem> orderByItems = orderByEngine.getOrderByItems(selectStatement);
        GroupBy groupBy = groupByEngine.createGroupBy(selectStatement);
        boolean toAppendOrderByItems = false;
        if (!groupBy.getItems().isEmpty() && orderByItems.isEmpty()) {
            orderByItems.addAll(groupBy.getItems());
            toAppendOrderByItems = true;
        }
        items.addAll(getDerivedColumns(orderByItems, groupBy.getItems()));
        ShardingSelectOptimizedStatement result = new ShardingSelectOptimizedStatement(selectStatement, 
                new ArrayList<>(shardingConditionEngine.createShardingConditions(selectStatement, parameters)), 
                encryptConditionEngine.createEncryptConditions(selectStatement), appendAverageDerivedColumns(items), groupBy);
        result.getOrderByItems().addAll(orderByItems);
        result.setToAppendOrderByItems(toAppendOrderByItems);
        setPagination(result);
        return result;
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
                AggregationType.COUNT, averageDistinctSelectItem.getInnerExpression(), Optional.of(countAlias), averageDistinctSelectItem.getDistinctColumnName());
        String sumAlias = DerivedColumn.AVG_SUM_ALIAS.getDerivedColumnAlias(derivedColumnOffset);
        AggregationDistinctSelectItem sumDistinctSelectItem = new AggregationDistinctSelectItem(
                AggregationType.SUM, averageDistinctSelectItem.getInnerExpression(), Optional.of(sumAlias), averageDistinctSelectItem.getDistinctColumnName());
        averageDistinctSelectItem.getDerivedAggregationSelectItems().clear();
        averageDistinctSelectItem.getDerivedAggregationSelectItems().add(countDistinctSelectItem);
        averageDistinctSelectItem.getDerivedAggregationSelectItems().add(sumDistinctSelectItem);
    }
    
    private void appendDerivedAggregationSelectItems(final AggregationSelectItem averageSelectItem, final int derivedColumnOffset) {
        String countAlias = DerivedColumn.AVG_COUNT_ALIAS.getDerivedColumnAlias(derivedColumnOffset);
        AggregationSelectItem countSelectItem = new AggregationSelectItem(AggregationType.COUNT, averageSelectItem.getInnerExpression(), Optional.of(countAlias));
        String sumAlias = DerivedColumn.AVG_SUM_ALIAS.getDerivedColumnAlias(derivedColumnOffset);
        AggregationSelectItem sumSelectItem = new AggregationSelectItem(AggregationType.SUM, averageSelectItem.getInnerExpression(), Optional.of(sumAlias));
        averageSelectItem.getDerivedAggregationSelectItems().clear();
        averageSelectItem.getDerivedAggregationSelectItems().add(countSelectItem);
        averageSelectItem.getDerivedAggregationSelectItems().add(sumSelectItem);
    }
    
    private Collection<SelectItem> getDerivedColumns(final Collection<OrderByItem> orderByItems, final Collection<OrderByItem> groupByItems) {
        Collection<SelectItem> result = new LinkedList<>();
        if (!orderByItems.isEmpty()) {
            result.addAll(appendDerivedOrderColumns(orderByItems));
        }
        if (!groupByItems.isEmpty()) {
            result.addAll(appendDerivedGroupColumns(groupByItems));
        }
        return result;
    }
    
    private Collection<SelectItem> appendDerivedOrderColumns(final Collection<OrderByItem> orderItems) {
        Collection<SelectItem> result = new LinkedList<>();
        int derivedColumnOffset = 0;
        for (OrderByItem each : orderItems) {
            if (!containsItem(each.getSegment())) {
                String alias = DerivedColumn.ORDER_BY_ALIAS.getDerivedColumnAlias(derivedColumnOffset++);
                result.add(new DerivedCommonSelectItem(((TextOrderByItemSegment) each.getSegment()).getText(), Optional.of(alias)));
            }
        }
        return result;
    }
    
    private Collection<SelectItem> appendDerivedGroupColumns(final Collection<OrderByItem> orderByItems) {
        Collection<SelectItem> result = new LinkedList<>();
        int derivedColumnOffset = 0;
        for (OrderByItem each : orderByItems) {
            if (!containsItem(each.getSegment())) {
                String alias = DerivedColumn.GROUP_BY_ALIAS.getDerivedColumnAlias(derivedColumnOffset++);
                result.add(new DerivedCommonSelectItem(((TextOrderByItemSegment) each.getSegment()).getText(), Optional.of(alias)));
            }
        }
        return result;
    }
    
    private boolean containsItem(final OrderByItemSegment orderByItemSegment) {
        return orderByItemSegment instanceof IndexOrderByItemSegment || containsItemInStarSelectItems(orderByItemSegment) || containsItemInSelectItems(orderByItemSegment);
    }
    
    private boolean containsItemInStarSelectItems(final OrderByItemSegment orderByItemSegment) {
        return selectStatement.hasUnqualifiedStarSelectItem()
                || containsItemWithOwnerInStarSelectItems(orderByItemSegment) || containsItemWithoutOwnerInStarSelectItems(orderByItemSegment);
    }
    
    private boolean containsItemWithOwnerInStarSelectItems(final OrderByItemSegment orderItem) {
        return orderItem instanceof ColumnOrderByItemSegment && ((ColumnOrderByItemSegment) orderItem).getColumn().getOwner().isPresent()
                && selectStatement.findStarSelectItem(((ColumnOrderByItemSegment) orderItem).getColumn().getOwner().get().getName()).isPresent();
    }
    
    private boolean containsItemWithoutOwnerInStarSelectItems(final OrderByItemSegment orderItem) {
        if (!(orderItem instanceof ColumnOrderByItemSegment)) {
            return false;
        }
        if (!((ColumnOrderByItemSegment) orderItem).getColumn().getOwner().isPresent()) {
            for (StarSelectItem each : selectStatement.getQualifiedStarSelectItems()) {
                if (isSameSelectItem(each, (ColumnOrderByItemSegment) orderItem)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    private boolean isSameSelectItem(final StarSelectItem starSelectItem, final ColumnOrderByItemSegment orderItem) {
        Preconditions.checkState(starSelectItem.getOwner().isPresent());
        Optional<Table> table = selectStatement.getTables().find(starSelectItem.getOwner().get());
        return table.isPresent() && shardingTableMetaData.containsColumn(table.get().getName(), orderItem.getColumn().getName());
    }
    
    private boolean containsItemInSelectItems(final OrderByItemSegment orderItem) {
        for (SelectItem each : selectStatement.getItems()) {
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
    
    private void setPagination(final ShardingSelectOptimizedStatement optimizedStatement) {
        if (null != selectStatement.getOffset() || null != selectStatement.getRowCount()) {
            optimizedStatement.setPagination(new Pagination(selectStatement.getOffset(), selectStatement.getRowCount(), parameters));
        }
    }
}
