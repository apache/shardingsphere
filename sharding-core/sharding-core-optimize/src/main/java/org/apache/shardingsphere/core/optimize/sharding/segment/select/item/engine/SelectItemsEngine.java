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

package org.apache.shardingsphere.core.optimize.sharding.segment.select.item.engine;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.core.metadata.table.ShardingTableMetaData;
import org.apache.shardingsphere.core.optimize.api.segment.Table;
import org.apache.shardingsphere.core.optimize.api.segment.Tables;
import org.apache.shardingsphere.core.optimize.sharding.segment.select.groupby.GroupBy;
import org.apache.shardingsphere.core.optimize.sharding.segment.select.item.DerivedColumn;
import org.apache.shardingsphere.core.optimize.sharding.segment.select.item.DerivedCommonSelectItem;
import org.apache.shardingsphere.core.optimize.sharding.segment.select.item.SelectItem;
import org.apache.shardingsphere.core.optimize.sharding.segment.select.item.SelectItems;
import org.apache.shardingsphere.core.optimize.sharding.segment.select.item.ShorthandSelectItem;
import org.apache.shardingsphere.core.optimize.sharding.segment.select.orderby.OrderBy;
import org.apache.shardingsphere.core.optimize.sharding.segment.select.orderby.OrderByItem;
import org.apache.shardingsphere.core.parse.sql.segment.dml.item.SelectItemSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.item.SelectItemsSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.order.item.ColumnOrderByItemSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.order.item.IndexOrderByItemSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.order.item.OrderByItemSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.order.item.TextOrderByItemSegment;
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
     * @param sql SQL
     * @param selectStatement SQL statement
     * @param groupBy group by
     * @param orderBy order by
     * @return select items
     */
    public SelectItems createSelectItems(final String sql, final SelectStatement selectStatement, final GroupBy groupBy, final OrderBy orderBy) {
        SelectItemsSegment selectItemsSegment = selectStatement.getSelectItems();
        Collection<SelectItem> items = getSelectItemList(sql, selectItemsSegment);
        SelectItems result = new SelectItems(items, selectItemsSegment.isDistinctRow(), selectItemsSegment.getStopIndex());
        Tables tables = new Tables(selectStatement);
        result.getItems().addAll(getDerivedGroupByColumns(tables, items, groupBy));
        result.getItems().addAll(getDerivedOrderByColumns(tables, items, orderBy));
        return result;
    }
    
    private Collection<SelectItem> getSelectItemList(final String sql, final SelectItemsSegment selectItemsSegment) {
        Collection<SelectItem> result = new LinkedList<>();
        for (SelectItemSegment each : selectItemsSegment.getSelectItems()) {
            Optional<SelectItem> selectItem = selectItemEngine.createSelectItem(sql, each);
            if (selectItem.isPresent()) {
                result.add(selectItem.get());
            }
        }
        return result;
    }
    
    private Collection<SelectItem> getDerivedGroupByColumns(final Tables tables, final Collection<SelectItem> selectItems, final GroupBy groupBy) {
        return getDerivedOrderColumns(tables, selectItems, groupBy.getItems(), DerivedColumn.GROUP_BY_ALIAS);
    }
    
    private Collection<SelectItem> getDerivedOrderByColumns(final Tables tables, final Collection<SelectItem> selectItems, final OrderBy orderBy) {
        return getDerivedOrderColumns(tables, selectItems, orderBy.getItems(), DerivedColumn.ORDER_BY_ALIAS);
    }
    
    private Collection<SelectItem> getDerivedOrderColumns(final Tables tables, final Collection<SelectItem> selectItems, final Collection<OrderByItem> orderItems, final DerivedColumn derivedColumn) {
        Collection<SelectItem> result = new LinkedList<>();
        int derivedColumnOffset = 0;
        for (OrderByItem each : orderItems) {
            if (!containsItem(tables, selectItems, each.getSegment())) {
                result.add(new DerivedCommonSelectItem(((TextOrderByItemSegment) each.getSegment()).getText(), derivedColumn.getDerivedColumnAlias(derivedColumnOffset++)));
            }
        }
        return result;
    }
    
    private boolean containsItem(final Tables tables, final Collection<SelectItem> items, final OrderByItemSegment orderByItemSegment) {
        return orderByItemSegment instanceof IndexOrderByItemSegment
                || containsItemInShorthandItems(tables, items, orderByItemSegment) || containsItemInSelectItems(items, orderByItemSegment);
    }
    
    private boolean containsItemInShorthandItems(final Tables tables, final Collection<SelectItem> items, final OrderByItemSegment orderByItemSegment) {
        return isUnqualifiedShorthandItem(items)
                || containsItemWithOwnerInShorthandItems(tables, items, orderByItemSegment) || containsItemWithoutOwnerInShorthandItems(tables, items, orderByItemSegment);
    }
    
    private boolean isUnqualifiedShorthandItem(final Collection<SelectItem> items) {
        if (1 != items.size()) {
            return false;
        }
        SelectItem item = items.iterator().next();
        return item instanceof ShorthandSelectItem && !((ShorthandSelectItem) item).getOwner().isPresent();
    }
    
    private boolean containsItemWithOwnerInShorthandItems(final Tables tables, final Collection<SelectItem> items, final OrderByItemSegment orderItem) {
        return orderItem instanceof ColumnOrderByItemSegment && ((ColumnOrderByItemSegment) orderItem).getColumn().getOwner().isPresent()
                && findShorthandItem(tables, items, ((ColumnOrderByItemSegment) orderItem).getColumn().getOwner().get().getTableName()).isPresent();
    }
    
    private Optional<ShorthandSelectItem> findShorthandItem(final Tables tables, final Collection<SelectItem> items, final String tableNameOrAlias) {
        Optional<Table> table = tables.find(tableNameOrAlias);
        if (!table.isPresent()) {
            return Optional.absent();
        }
        for (SelectItem each : items) {
            if (!(each instanceof ShorthandSelectItem)) {
                continue;
            }
            ShorthandSelectItem shorthandSelectItem = (ShorthandSelectItem) each;
            if (shorthandSelectItem.getOwner().isPresent() && tables.find(shorthandSelectItem.getOwner().get()).equals(table)) {
                return Optional.of(shorthandSelectItem);
            }
        }
        return Optional.absent();
    }
    
    private boolean containsItemWithoutOwnerInShorthandItems(final Tables tables, final Collection<SelectItem> items, final OrderByItemSegment orderItem) {
        if (!(orderItem instanceof ColumnOrderByItemSegment)) {
            return false;
        }
        if (!((ColumnOrderByItemSegment) orderItem).getColumn().getOwner().isPresent()) {
            for (ShorthandSelectItem each : getQualifiedShorthandItems(items)) {
                if (isSameSelectItem(tables, each, (ColumnOrderByItemSegment) orderItem)) {
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
    
    private boolean isSameSelectItem(final Tables tables, final ShorthandSelectItem shorthandSelectItem, final ColumnOrderByItemSegment orderItem) {
        Preconditions.checkState(shorthandSelectItem.getOwner().isPresent());
        Optional<Table> table = tables.find(shorthandSelectItem.getOwner().get());
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
