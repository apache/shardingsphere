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

package org.apache.shardingsphere.core.optimize.statement.impl;

import com.google.common.collect.Lists;
import org.apache.shardingsphere.core.optimize.segment.select.groupby.GroupBy;
import org.apache.shardingsphere.core.optimize.segment.select.item.impl.ColumnSelectItem;
import org.apache.shardingsphere.core.optimize.segment.select.item.SelectItem;
import org.apache.shardingsphere.core.optimize.segment.select.item.SelectItems;
import org.apache.shardingsphere.core.optimize.segment.select.orderby.OrderBy;
import org.apache.shardingsphere.core.optimize.segment.select.orderby.OrderByItem;
import org.apache.shardingsphere.core.parse.core.constant.OrderDirection;
import org.apache.shardingsphere.core.parse.sql.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.item.SelectItemsSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.order.GroupBySegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.order.OrderBySegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.order.item.ColumnOrderByItemSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.order.item.IndexOrderByItemSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.order.item.OrderByItemSegment;
import org.apache.shardingsphere.core.parse.sql.segment.generic.TableSegment;
import org.apache.shardingsphere.core.parse.sql.statement.dml.SelectStatement;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class SelectOptimizedStatementTest {
    
    private static final String INDEX_ORDER_BY = "IndexOrderBy";
    
    private static final String COLUMN_ORDER_BY_WITH_OWNER = "ColumnOrderByWithOwner";
    
    private static final String COLUMN_ORDER_BY_WITH_ALIAS = "ColumnOrderByWithAlias";
    
    private static final String COLUMN_ORDER_BY_WITHOUT_OWNER_ALIAS = "ColumnOrderByWithoutOwnerAlias";
    
    @Test
    public void assertSetIndexForItemsByIndexOrderBy() {
        SelectOptimizedStatement selectOptimizedStatement = new SelectOptimizedStatement(
                new SelectStatement(), new GroupBy(Collections.<OrderByItem>emptyList(), 0), createOrderBy(INDEX_ORDER_BY), createSelectItems(), null);
        selectOptimizedStatement.setIndexForItems(Collections.<String, Integer>emptyMap());
        assertThat(selectOptimizedStatement.getOrderBy().getItems().iterator().next().getIndex(), is(4));
    }
    
    @Test
    public void assertSetIndexForItemsByColumnOrderByWithOwner() {
        SelectOptimizedStatement selectOptimizedStatement = new SelectOptimizedStatement(
                new SelectStatement(), new GroupBy(Collections.<OrderByItem>emptyList(), 0), createOrderBy(COLUMN_ORDER_BY_WITH_OWNER), createSelectItems(), null);
        selectOptimizedStatement.setIndexForItems(Collections.<String, Integer>emptyMap());
        assertThat(selectOptimizedStatement.getOrderBy().getItems().iterator().next().getIndex(), is(1));
    }
    
    @Test
    public void assertSetIndexForItemsByColumnOrderByWithAlias() {
        SelectOptimizedStatement selectOptimizedStatement = new SelectOptimizedStatement(
                new SelectStatement(), new GroupBy(Collections.<OrderByItem>emptyList(), 0), createOrderBy(COLUMN_ORDER_BY_WITH_ALIAS), createSelectItems(), null);
        Map<String, Integer> columnLabelIndexMap = new HashMap<>();
        columnLabelIndexMap.put("n", 2);
        selectOptimizedStatement.setIndexForItems(columnLabelIndexMap);
        assertThat(selectOptimizedStatement.getOrderBy().getItems().iterator().next().getIndex(), is(2));
    }
    
    @Test
    public void assertSetIndexForItemsByColumnOrderByWithoutAlias() {
        SelectOptimizedStatement selectOptimizedStatement = new SelectOptimizedStatement(
                new SelectStatement(), new GroupBy(Collections.<OrderByItem>emptyList(), 0), createOrderBy(COLUMN_ORDER_BY_WITHOUT_OWNER_ALIAS), createSelectItems(), null);
        Map<String, Integer> columnLabelIndexMap = new HashMap<>();
        columnLabelIndexMap.put("id", 3);
        selectOptimizedStatement.setIndexForItems(columnLabelIndexMap);
        assertThat(selectOptimizedStatement.getOrderBy().getItems().iterator().next().getIndex(), is(3));
    }
    
    @Test
    public void assertIsSameGroupByAndOrderByItems() {
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.setSelectItems(new SelectItemsSegment(0, 0, false));
        selectStatement.setGroupBy(new GroupBySegment(0, 0, Collections.<OrderByItemSegment>singletonList(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.DESC, OrderDirection.DESC))));
        selectStatement.setOrderBy(new OrderBySegment(0, 0, Collections.<OrderByItemSegment>singletonList(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.DESC, OrderDirection.DESC))));
        SelectOptimizedStatement selectOptimizedStatement = new SelectOptimizedStatement(null, "", Collections.emptyList(), selectStatement);
        assertTrue(selectOptimizedStatement.isSameGroupByAndOrderByItems());
    }
    
    @Test
    public void assertIsNotSameGroupByAndOrderByItemsWhenEmptyGroupBy() {
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.setSelectItems(new SelectItemsSegment(0, 0, false));
        SelectOptimizedStatement selectOptimizedStatement = new SelectOptimizedStatement(null, "", Collections.emptyList(), selectStatement);
        assertFalse(selectOptimizedStatement.isSameGroupByAndOrderByItems());
    }
    
    @Test
    public void assertIsNotSameGroupByAndOrderByItemsWhenDifferentGroupByAndOrderBy() {
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.setSelectItems(new SelectItemsSegment(0, 0, false));
        selectStatement.setGroupBy(new GroupBySegment(0, 0, Collections.<OrderByItemSegment>singletonList(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.ASC, OrderDirection.DESC))));
        selectStatement.setOrderBy(new OrderBySegment(0, 0, Collections.<OrderByItemSegment>singletonList(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.DESC, OrderDirection.DESC))));
        SelectOptimizedStatement selectOptimizedStatement = new SelectOptimizedStatement(null, "", Collections.emptyList(), selectStatement);
        assertFalse(selectOptimizedStatement.isSameGroupByAndOrderByItems());
    }
    
    private OrderBy createOrderBy(final String type) {
        OrderByItemSegment orderByItemSegment = createOrderByItemSegment(type);
        OrderByItem orderByItem = new OrderByItem(orderByItemSegment);
        return new OrderBy(Lists.newArrayList(orderByItem), true);
    }
    
    private OrderByItemSegment createOrderByItemSegment(final String type) {
        switch (type) {
            case INDEX_ORDER_BY:
                return new IndexOrderByItemSegment(0, 0, 4, OrderDirection.ASC, OrderDirection.ASC);
            case COLUMN_ORDER_BY_WITH_OWNER:
                ColumnSegment columnSegment = new ColumnSegment(0, 0, "name");
                columnSegment.setOwner(new TableSegment(0, 0, "table"));
                return new ColumnOrderByItemSegment(0, 0, columnSegment, OrderDirection.ASC, OrderDirection.ASC);
            case COLUMN_ORDER_BY_WITH_ALIAS:
                return new ColumnOrderByItemSegment(0, 0, new ColumnSegment(0, 0, "n"), OrderDirection.ASC, OrderDirection.ASC);
            default:
                return new ColumnOrderByItemSegment(0, 0, new ColumnSegment(0, 0, "id"), OrderDirection.ASC, OrderDirection.ASC);
        }
    }
    
    private SelectItems createSelectItems() {
        Collection<SelectItem> selectItems = Lists.newArrayList(getColumnSelectItemWithoutOwner(), getColumnSelectItemWithoutOwner(true), getColumnSelectItemWithoutOwner(false));
        return new SelectItems(0, 0, true, selectItems);
    }
    
    private SelectItem getColumnSelectItemWithoutOwner() {
        return new ColumnSelectItem("table", "name", null);
    }
    
    private SelectItem getColumnSelectItemWithoutOwner(final boolean hasAlias) {
        return new ColumnSelectItem(null, hasAlias ? "name" : "id", hasAlias ? "n" : null);
    }
}
