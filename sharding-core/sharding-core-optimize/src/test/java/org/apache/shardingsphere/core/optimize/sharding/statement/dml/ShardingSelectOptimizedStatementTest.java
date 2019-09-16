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

package org.apache.shardingsphere.core.optimize.sharding.statement.dml;

import com.google.common.collect.Lists;
import org.apache.shardingsphere.core.metadata.column.ColumnMetaData;
import org.apache.shardingsphere.core.metadata.table.TableMetaData;
import org.apache.shardingsphere.core.metadata.table.TableMetas;
import org.apache.shardingsphere.core.optimize.encrypt.condition.EncryptCondition;
import org.apache.shardingsphere.core.optimize.sharding.segment.condition.ShardingCondition;
import org.apache.shardingsphere.core.optimize.sharding.segment.select.groupby.GroupBy;
import org.apache.shardingsphere.core.optimize.sharding.segment.select.item.ColumnSelectItem;
import org.apache.shardingsphere.core.optimize.sharding.segment.select.item.SelectItem;
import org.apache.shardingsphere.core.optimize.sharding.segment.select.item.SelectItems;
import org.apache.shardingsphere.core.optimize.sharding.segment.select.orderby.OrderBy;
import org.apache.shardingsphere.core.optimize.sharding.segment.select.orderby.OrderByItem;
import org.apache.shardingsphere.core.parse.core.constant.OrderDirection;
import org.apache.shardingsphere.core.parse.sql.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.order.item.ColumnOrderByItemSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.order.item.IndexOrderByItemSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.order.item.OrderByItemSegment;
import org.apache.shardingsphere.core.parse.sql.segment.generic.TableSegment;
import org.apache.shardingsphere.core.parse.sql.statement.dml.SelectStatement;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

public class ShardingSelectOptimizedStatementTest {
    
    private static final String INDEX_ORDER_BY = "IndexOrderBy";
    
    private static final String COLUMN_ORDER_BY_WITH_OWNER = "ColumnOrderByWithOwner";
    
    private static final String COLUMN_ORDER_BY_WITH_ALIAS = "ColumnOrderByWithAlias";
    
    private static final String COLUMN_ORDER_BY_WITHOUT_OWNER_ALIAS = "ColumnOrderByWithoutOwnerAlias";
    
    @Test
    public void assertSetIndexForItemsByIndexOrderBy() {
        ShardingSelectOptimizedStatement shardingSelectOptimizedStatement = new ShardingSelectOptimizedStatement(
            new SelectStatement(), Collections.<ShardingCondition>emptyList(), Collections.<EncryptCondition>emptyList(),
            new GroupBy(Collections.<OrderByItem>emptyList(), 0), createOrderBy(INDEX_ORDER_BY), createSelectItems(), null);
        shardingSelectOptimizedStatement.setIndexForItems(Collections.<String, Integer>emptyMap());
        assertThat(shardingSelectOptimizedStatement.getOrderBy().getItems().iterator().next().getIndex(), is(4));
    }
    
    @Test
    public void assertSetIndexForItemsByColumnOrderByWithOwner() {
        ShardingSelectOptimizedStatement shardingSelectOptimizedStatement = new ShardingSelectOptimizedStatement(
            new SelectStatement(), Collections.<ShardingCondition>emptyList(), Collections.<EncryptCondition>emptyList(),
            new GroupBy(Collections.<OrderByItem>emptyList(), 0), createOrderBy(COLUMN_ORDER_BY_WITH_OWNER), createSelectItems(), null);
        shardingSelectOptimizedStatement.setIndexForItems(Collections.<String, Integer>emptyMap());
        assertThat(shardingSelectOptimizedStatement.getOrderBy().getItems().iterator().next().getIndex(), is(1));
    }
    
    @Test
    public void assertSetIndexForItemsByColumnOrderByWithAlias() {
        ShardingSelectOptimizedStatement shardingSelectOptimizedStatement = new ShardingSelectOptimizedStatement(
            new SelectStatement(), Collections.<ShardingCondition>emptyList(), Collections.<EncryptCondition>emptyList(),
            new GroupBy(Collections.<OrderByItem>emptyList(), 0), createOrderBy(COLUMN_ORDER_BY_WITH_ALIAS), createSelectItems(), null);
        Map<String, Integer> columnLabelIndexMap = new HashMap<>();
        columnLabelIndexMap.put("n", 2);
        shardingSelectOptimizedStatement.setIndexForItems(columnLabelIndexMap);
        assertThat(shardingSelectOptimizedStatement.getOrderBy().getItems().iterator().next().getIndex(), is(2));
    }
    
    @Test
    public void assertSetIndexForItemsByColumnOrderByWithoutAlias() {
        ShardingSelectOptimizedStatement shardingSelectOptimizedStatement = new ShardingSelectOptimizedStatement(
            new SelectStatement(), Collections.<ShardingCondition>emptyList(), Collections.<EncryptCondition>emptyList(),
            new GroupBy(Collections.<OrderByItem>emptyList(), 0), createOrderBy(COLUMN_ORDER_BY_WITHOUT_OWNER_ALIAS), createSelectItems(), null);
        Map<String, Integer> columnLabelIndexMap = new HashMap<>();
        columnLabelIndexMap.put("id", 3);
        shardingSelectOptimizedStatement.setIndexForItems(columnLabelIndexMap);
        assertThat(shardingSelectOptimizedStatement.getOrderBy().getItems().iterator().next().getIndex(), is(3));
    }
    
    @Test
    public void assertIsSameGroupByAndOrderByItems() {
        ShardingSelectOptimizedStatement shardingSelectOptimizedStatement = new ShardingSelectOptimizedStatement(
            new SelectStatement(), Collections.<ShardingCondition>emptyList(), Collections.<EncryptCondition>emptyList(),
            new GroupBy(Collections.<OrderByItem>emptyList(), 0), new OrderBy(Collections.<OrderByItem>emptyList(), false), createSelectItems(), null);
        assertFalse(shardingSelectOptimizedStatement.isSameGroupByAndOrderByItems());
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
        return new SelectItems(0, 0, true, selectItems, Collections.<TableSegment>emptyList(), createTableMetas());
    }
    
    private SelectItem getColumnSelectItemWithoutOwner() {
        return new ColumnSelectItem("table", "name", null);
    }
    
    private SelectItem getColumnSelectItemWithoutOwner(final boolean hasAlias) {
        return new ColumnSelectItem(null, hasAlias ? "name" : "id", hasAlias ? "n" : null);
    }
    
    private TableMetas createTableMetas() {
        Map<String, TableMetaData> tables = new HashMap<>(1, 1);
        tables.put("table", new TableMetaData(Arrays.asList(new ColumnMetaData("id", "number", true), new ColumnMetaData("name", "varchar", false)), Collections.<String>emptyList()));
        return new TableMetas(tables);
    }
}
