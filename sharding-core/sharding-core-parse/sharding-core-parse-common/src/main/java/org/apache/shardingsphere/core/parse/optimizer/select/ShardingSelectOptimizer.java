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

package org.apache.shardingsphere.core.parse.optimizer.select;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import lombok.Setter;
import org.apache.shardingsphere.core.metadata.table.ShardingTableMetaData;
import org.apache.shardingsphere.core.parse.aware.ShardingTableMetaDataAware;
import org.apache.shardingsphere.core.parse.constant.DerivedColumn;
import org.apache.shardingsphere.core.parse.optimizer.SQLStatementOptimizer;
import org.apache.shardingsphere.core.parse.sql.context.selectitem.DerivedCommonSelectItem;
import org.apache.shardingsphere.core.parse.sql.context.selectitem.DistinctSelectItem;
import org.apache.shardingsphere.core.parse.sql.context.selectitem.SelectItem;
import org.apache.shardingsphere.core.parse.sql.context.selectitem.StarSelectItem;
import org.apache.shardingsphere.core.parse.sql.context.table.Table;
import org.apache.shardingsphere.core.parse.sql.segment.dml.order.item.ColumnOrderByItemSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.order.item.IndexOrderByItemSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.order.item.OrderByItemSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.order.item.TextOrderByItemSegment;
import org.apache.shardingsphere.core.parse.sql.statement.SQLStatement;
import org.apache.shardingsphere.core.parse.sql.statement.dml.SelectStatement;

import java.util.List;

/**
 * Select optimizer for sharding.
 *
 * @author duhongjun
 * @author panjuan
 */
@Setter
public final class ShardingSelectOptimizer implements SQLStatementOptimizer, ShardingTableMetaDataAware {
    
    private ShardingTableMetaData shardingTableMetaData;
    
    @Override
    public void optimize(final SQLStatement sqlStatement) {
        appendDerivedColumns((SelectStatement) sqlStatement, shardingTableMetaData);
        appendDerivedOrderBy((SelectStatement) sqlStatement);
    }
    
    private void appendDerivedColumns(final SelectStatement selectStatement, final ShardingTableMetaData shardingTableMetaData) {
        if (!selectStatement.getOrderByItems().isEmpty()) {
            appendDerivedOrderColumns(selectStatement.getOrderByItems(), selectStatement, shardingTableMetaData);
        }
        if (!selectStatement.getGroupByItems().isEmpty()) {
            appendDerivedGroupColumns(selectStatement.getGroupByItems(), selectStatement, shardingTableMetaData);
        }
    }
    
    private void appendDerivedOrderColumns(final List<OrderByItemSegment> orderItems, final SelectStatement selectStatement, final ShardingTableMetaData shardingTableMetaData) {
        int derivedColumnOffset = 0;
        for (OrderByItemSegment each : orderItems) {
            if (!containsItem(selectStatement, each, shardingTableMetaData)) {
                String alias = DerivedColumn.ORDER_BY_ALIAS.getDerivedColumnAlias(derivedColumnOffset++);
                selectStatement.getItems().add(new DerivedCommonSelectItem(((TextOrderByItemSegment) each).getText(), Optional.of(alias)));
            }
        }
    }
    
    private void appendDerivedGroupColumns(final List<OrderByItemSegment> orderItems, final SelectStatement selectStatement, final ShardingTableMetaData shardingTableMetaData) {
        int derivedColumnOffset = 0;
        for (OrderByItemSegment each : orderItems) {
            if (!containsItem(selectStatement, each, shardingTableMetaData)) {
                String alias = DerivedColumn.GROUP_BY_ALIAS.getDerivedColumnAlias(derivedColumnOffset++);
                selectStatement.getItems().add(new DerivedCommonSelectItem(((TextOrderByItemSegment) each).getText(), Optional.of(alias)));
            }
        }
    }
    
    private boolean containsItem(final SelectStatement selectStatement, final OrderByItemSegment orderItem, final ShardingTableMetaData shardingTableMetaData) {
        return orderItem instanceof IndexOrderByItemSegment
                || containsItemInStarSelectItems(selectStatement, orderItem, shardingTableMetaData) || containsItemInSelectItems(selectStatement, orderItem);
    }
    
    private boolean containsItemInStarSelectItems(final SelectStatement selectStatement, final OrderByItemSegment orderItem, final ShardingTableMetaData shardingTableMetaData) {
        return selectStatement.hasUnqualifiedStarSelectItem()
                || containsItemWithOwnerInStarSelectItems(selectStatement, orderItem) || containsItemWithoutOwnerInStarSelectItems(selectStatement, orderItem, shardingTableMetaData);
    }
    
    private boolean containsItemWithOwnerInStarSelectItems(final SelectStatement selectStatement, final OrderByItemSegment orderItem) {
        return orderItem instanceof ColumnOrderByItemSegment && ((ColumnOrderByItemSegment) orderItem).getColumn().getOwner().isPresent()
                && selectStatement.findStarSelectItem(((ColumnOrderByItemSegment) orderItem).getColumn().getOwner().get().getName()).isPresent();
    }
    
    private boolean containsItemWithoutOwnerInStarSelectItems(final SelectStatement selectStatement, final OrderByItemSegment orderItem, final ShardingTableMetaData shardingTableMetaData) {
        if (!(orderItem instanceof ColumnOrderByItemSegment)) {
            return false;
        }
        if (!((ColumnOrderByItemSegment) orderItem).getColumn().getOwner().isPresent()) {
            for (StarSelectItem each : selectStatement.getQualifiedStarSelectItems()) {
                if (isSameSelectItem(selectStatement, each, (ColumnOrderByItemSegment) orderItem, shardingTableMetaData)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    private boolean isSameSelectItem(final SelectStatement selectStatement, 
                                     final StarSelectItem starSelectItem, final ColumnOrderByItemSegment orderItem, final ShardingTableMetaData shardingTableMetaData) {
        Preconditions.checkState(starSelectItem.getOwner().isPresent());
        Optional<Table> table = selectStatement.getTables().find(starSelectItem.getOwner().get());
        return table.isPresent() && shardingTableMetaData.containsColumn(table.get().getName(), orderItem.getColumn().getName());
    }
    
    private boolean containsItemInSelectItems(final SelectStatement selectStatement, final OrderByItemSegment orderItem) {
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
    
    private void appendDerivedOrderBy(final SelectStatement selectStatement) {
        if (!selectStatement.getGroupByItems().isEmpty() && selectStatement.getOrderByItems().isEmpty()) {
            selectStatement.getOrderByItems().addAll(selectStatement.getGroupByItems());
            selectStatement.setToAppendOrderByItems(true);
        }
    }
}
