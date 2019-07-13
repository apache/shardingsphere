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
import org.apache.shardingsphere.core.optimize.statement.sharding.dml.select.item.AggregationDistinctSelectItem;
import org.apache.shardingsphere.core.optimize.statement.sharding.dml.select.item.AggregationSelectItem;
import org.apache.shardingsphere.core.optimize.statement.sharding.dml.select.item.DerivedCommonSelectItem;
import org.apache.shardingsphere.core.optimize.statement.sharding.dml.select.item.SelectItem;
import org.apache.shardingsphere.core.optimize.statement.sharding.dml.select.item.SelectItems;
import org.apache.shardingsphere.core.optimize.statement.sharding.dml.select.item.ShorthandSelectItem;
import org.apache.shardingsphere.core.optimize.statement.sharding.dml.select.orderby.OrderBy;
import org.apache.shardingsphere.core.optimize.statement.sharding.dml.select.orderby.OrderByItem;
import org.apache.shardingsphere.core.parse.constant.DerivedColumn;
import org.apache.shardingsphere.core.parse.sql.context.table.Table;
import org.apache.shardingsphere.core.parse.sql.segment.dml.SelectItemsSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.item.SelectItemSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.order.item.ColumnOrderByItemSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.order.item.IndexOrderByItemSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.order.item.OrderByItemSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.order.item.TextOrderByItemSegment;
import org.apache.shardingsphere.core.parse.sql.statement.SQLStatement;
import org.apache.shardingsphere.core.parse.sql.statement.dml.SelectStatement;

import java.util.Collection;
import java.util.LinkedList;

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
        SelectItemsSegment selectItemsSegment = getSelectItemsSegment(selectStatement);
        Collection<SelectItem> items = getSelectItemList(selectItemsSegment, selectStatement);
        appendAverageDerivedColumns(items);
        items.addAll(getDerivedGroupByColumns(selectStatement, items, groupBy.getItems()));
        items.addAll(getDerivedOrderByColumns(selectStatement, items, orderBy.getItems()));
        SelectItems result = new SelectItems(items, selectItemsSegment.isDistinctRow(), selectItemsSegment.getStopIndex());
        result.setContainStar(isUnqualifiedShorthandItem(items));
        return result;
    }
    
    private SelectItemsSegment getSelectItemsSegment(final SelectStatement selectStatement) {
        Optional<SelectItemsSegment> result = selectStatement.findSQLSegment(SelectItemsSegment.class);
        Preconditions.checkState(result.isPresent());
        return result.get();
    }
    
    private Collection<SelectItem> getSelectItemList(final SelectItemsSegment selectItemsSegment, final SQLStatement sqlStatement) {
        Collection<SelectItem> result = new LinkedList<>();
        for (SelectItemSegment each : selectItemsSegment.getSelectItems()) {
            Optional<SelectItem> selectItem = selectItemEngine.createSelectItem(each, sqlStatement);
            if (selectItem.isPresent()) {
                result.add(selectItem.get());
            }
        }
        return result;
    }
    
    private void appendAverageDerivedColumns(final Collection<SelectItem> items) {
        int derivedColumnOffset = 0;
        for (SelectItem each : items) {
            if (each instanceof AggregationSelectItem && AggregationType.AVG == ((AggregationSelectItem) each).getType()) {
                appendAverageDerivedColumns(derivedColumnOffset++, each);
                // TODO replace avg to constant, avoid calculate useless avg
            }
        }
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
                AggregationType.COUNT, averageDistinctSelectItem.getInnerExpression(), countAlias, averageDistinctSelectItem.getDistinctInnerExpression());
        String sumAlias = DerivedColumn.AVG_SUM_ALIAS.getDerivedColumnAlias(derivedColumnOffset);
        AggregationDistinctSelectItem sumDistinctSelectItem = new AggregationDistinctSelectItem(
                AggregationType.SUM, averageDistinctSelectItem.getInnerExpression(), sumAlias, averageDistinctSelectItem.getDistinctInnerExpression());
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
    
    private Collection<SelectItem> getDerivedGroupByColumns(final SelectStatement selectStatement, final Collection<SelectItem> selectItems, final Collection<OrderByItem> groupByItems) {
        return getDerivedOrderColumns(selectStatement, selectItems, groupByItems, DerivedColumn.GROUP_BY_ALIAS);
    }
    
    private Collection<SelectItem> getDerivedOrderByColumns(final SelectStatement selectStatement, final Collection<SelectItem> selectItems, final Collection<OrderByItem> orderByItems) {
        return getDerivedOrderColumns(selectStatement, selectItems, orderByItems, DerivedColumn.ORDER_BY_ALIAS);
    }
    
    private Collection<SelectItem> getDerivedOrderColumns(final SelectStatement selectStatement, 
                                                          final Collection<SelectItem> selectItems, final Collection<OrderByItem> orderItems, final DerivedColumn derivedColumn) {
        Collection<SelectItem> result = new LinkedList<>();
        int derivedColumnOffset = 0;
        for (OrderByItem each : orderItems) {
            if (!containsItem(selectStatement, selectItems, each.getSegment())) {
                result.add(new DerivedCommonSelectItem(((TextOrderByItemSegment) each.getSegment()).getText(), derivedColumn.getDerivedColumnAlias(derivedColumnOffset++)));
            }
        }
        return result;
    }
    
    private boolean containsItem(final SelectStatement selectStatement, final Collection<SelectItem> items, final OrderByItemSegment orderByItemSegment) {
        return orderByItemSegment instanceof IndexOrderByItemSegment
                || containsItemInShorthandItems(selectStatement, items, orderByItemSegment) || containsItemInSelectItems(items, orderByItemSegment);
    }
    
    private boolean containsItemInShorthandItems(final SelectStatement selectStatement, final Collection<SelectItem> items, final OrderByItemSegment orderByItemSegment) {
        return isUnqualifiedShorthandItem(items)
                || containsItemWithOwnerInShorthandItems(selectStatement, items, orderByItemSegment) || containsItemWithoutOwnerInShorthandItems(selectStatement, items, orderByItemSegment);
    }
    
    private boolean isUnqualifiedShorthandItem(final Collection<SelectItem> items) {
        if (1 != items.size()) {
            return false;
        }
        SelectItem item = items.iterator().next();
        return item instanceof ShorthandSelectItem && !((ShorthandSelectItem) item).getOwner().isPresent();
    }
    
    private boolean containsItemWithOwnerInShorthandItems(final SelectStatement selectStatement, final Collection<SelectItem> items, final OrderByItemSegment orderItem) {
        return orderItem instanceof ColumnOrderByItemSegment && ((ColumnOrderByItemSegment) orderItem).getColumn().getOwner().isPresent()
                && findShorthandItem(selectStatement, items, ((ColumnOrderByItemSegment) orderItem).getColumn().getOwner().get().getName()).isPresent();
    }
    
    private Optional<ShorthandSelectItem> findShorthandItem(final SelectStatement selectStatement, final Collection<SelectItem> items, final String tableNameOrAlias) {
        Optional<Table> table = selectStatement.getTables().find(tableNameOrAlias);
        if (!table.isPresent()) {
            return Optional.absent();
        }
        for (SelectItem each : items) {
            if (!(each instanceof ShorthandSelectItem)) {
                continue;
            }
            ShorthandSelectItem shorthandSelectItem = (ShorthandSelectItem) each;
            if (shorthandSelectItem.getOwner().isPresent() && selectStatement.getTables().find(shorthandSelectItem.getOwner().get()).equals(table)) {
                return Optional.of(shorthandSelectItem);
            }
        }
        return Optional.absent();
    }
    
    private boolean containsItemWithoutOwnerInShorthandItems(final SelectStatement selectStatement, final Collection<SelectItem> items, final OrderByItemSegment orderItem) {
        if (!(orderItem instanceof ColumnOrderByItemSegment)) {
            return false;
        }
        if (!((ColumnOrderByItemSegment) orderItem).getColumn().getOwner().isPresent()) {
            for (ShorthandSelectItem each : getQualifiedShorthandItems(items)) {
                if (isSameSelectItem(selectStatement, each, (ColumnOrderByItemSegment) orderItem)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    private Collection<ShorthandSelectItem> getQualifiedShorthandItems(final Collection<SelectItem> items) {
        Collection<ShorthandSelectItem> result = new LinkedList<>();
        for (SelectItem each : items) {
            if (each instanceof ShorthandSelectItem && ((ShorthandSelectItem) each).getOwner().isPresent()) {
                result.add((ShorthandSelectItem) each);
            }
        }
        return result;
    }
    
    private boolean isSameSelectItem(final SelectStatement selectStatement, final ShorthandSelectItem shorthandSelectItem, final ColumnOrderByItemSegment orderItem) {
        Preconditions.checkState(shorthandSelectItem.getOwner().isPresent());
        Optional<Table> table = selectStatement.getTables().find(shorthandSelectItem.getOwner().get());
        return table.isPresent() && shardingTableMetaData.containsColumn(table.get().getName(), orderItem.getColumn().getName());
    }
    
    private boolean containsItemInSelectItems(final Collection<SelectItem> items, final OrderByItemSegment orderItem) {
        for (SelectItem each : items) {
            if (orderItem instanceof IndexOrderByItemSegment) {
                return true;
            }
            if (isSameAlias(each, (TextOrderByItemSegment) orderItem) || isSameQualifiedName(each, (TextOrderByItemSegment) orderItem)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean isSameAlias(final SelectItem selectItem, final TextOrderByItemSegment orderItem) {
        return selectItem.getAlias().isPresent() && (orderItem.getText().equalsIgnoreCase(selectItem.getAlias().get()) || orderItem.getText().equalsIgnoreCase(selectItem.getExpression()));
    }
    
    private boolean isSameQualifiedName(final SelectItem selectItem, final TextOrderByItemSegment orderItem) {
        return !selectItem.getAlias().isPresent() && selectItem.getExpression().equalsIgnoreCase(orderItem.getText());
    }
}
