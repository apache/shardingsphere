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

package org.apache.shardingsphere.sql.parser.binder.statement.impl;

import com.google.common.collect.Lists;
import org.apache.shardingsphere.sql.parser.sql.constant.AggregationType;
import org.apache.shardingsphere.sql.parser.sql.constant.OrderDirection;
import org.apache.shardingsphere.sql.parser.binder.segment.select.groupby.GroupByContext;
import org.apache.shardingsphere.sql.parser.binder.segment.select.orderby.OrderByContext;
import org.apache.shardingsphere.sql.parser.binder.segment.select.orderby.OrderByItem;
import org.apache.shardingsphere.sql.parser.binder.segment.select.projection.Projection;
import org.apache.shardingsphere.sql.parser.binder.segment.select.projection.ProjectionsContext;
import org.apache.shardingsphere.sql.parser.binder.segment.select.projection.impl.AggregationProjection;
import org.apache.shardingsphere.sql.parser.binder.segment.select.projection.impl.ColumnProjection;
import org.apache.shardingsphere.sql.parser.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.order.GroupBySegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.order.OrderBySegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.order.item.ColumnOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.order.item.IndexOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.order.item.OrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.value.identifier.IdentifierValue;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class SelectStatementContextTest {
    
    private static final String INDEX_ORDER_BY = "IndexOrderBy";
    
    private static final String COLUMN_ORDER_BY_WITH_OWNER = "ColumnOrderByWithOwner";
    
    private static final String COLUMN_ORDER_BY_WITH_ALIAS = "ColumnOrderByWithAlias";
    
    private static final String COLUMN_ORDER_BY_WITHOUT_OWNER_ALIAS = "ColumnOrderByWithoutOwnerAlias";
    
    @Test
    public void assertSetIndexForItemsByIndexOrderBy() {
        SelectStatementContext selectStatementContext = new SelectStatementContext(
                new SelectStatement(), new GroupByContext(Collections.emptyList(), 0), createOrderBy(INDEX_ORDER_BY), createProjectionsContext(), null);
        selectStatementContext.setIndexes(Collections.emptyMap());
        assertThat(selectStatementContext.getOrderByContext().getItems().iterator().next().getIndex(), is(4));
    }
    
    @Test
    public void assertSetIndexForItemsByColumnOrderByWithOwner() {
        SelectStatementContext selectStatementContext = new SelectStatementContext(
                new SelectStatement(), new GroupByContext(Collections.emptyList(), 0), createOrderBy(COLUMN_ORDER_BY_WITH_OWNER), createProjectionsContext(), null);
        selectStatementContext.setIndexes(Collections.emptyMap());
        assertThat(selectStatementContext.getOrderByContext().getItems().iterator().next().getIndex(), is(1));
    }
    
    @Test
    public void assertSetIndexForItemsByColumnOrderByWithAlias() {
        SelectStatementContext selectStatementContext = new SelectStatementContext(
                new SelectStatement(), new GroupByContext(Collections.emptyList(), 0), createOrderBy(COLUMN_ORDER_BY_WITH_ALIAS), createProjectionsContext(), null);
        Map<String, Integer> columnLabelIndexMap = new HashMap<>();
        columnLabelIndexMap.put("n", 2);
        selectStatementContext.setIndexes(columnLabelIndexMap);
        assertThat(selectStatementContext.getOrderByContext().getItems().iterator().next().getIndex(), is(2));
    }
    
    @Test
    public void assertSetIndexForItemsByColumnOrderByWithoutAlias() {
        SelectStatementContext selectStatementContext = new SelectStatementContext(
                new SelectStatement(), new GroupByContext(Collections.emptyList(), 0), createOrderBy(COLUMN_ORDER_BY_WITHOUT_OWNER_ALIAS), createProjectionsContext(), null);
        Map<String, Integer> columnLabelIndexMap = new HashMap<>();
        columnLabelIndexMap.put("id", 3);
        selectStatementContext.setIndexes(columnLabelIndexMap);
        assertThat(selectStatementContext.getOrderByContext().getItems().iterator().next().getIndex(), is(3));
    }
    
    @Test
    public void assertIsSameGroupByAndOrderByItems() {
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.setProjections(new ProjectionsSegment(0, 0));
        selectStatement.setGroupBy(new GroupBySegment(0, 0, Collections.singletonList(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.DESC, OrderDirection.DESC))));
        selectStatement.setOrderBy(new OrderBySegment(0, 0, Collections.singletonList(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.DESC, OrderDirection.DESC))));
        SelectStatementContext selectStatementContext = new SelectStatementContext(null, Collections.emptyList(), selectStatement);
        assertTrue(selectStatementContext.isSameGroupByAndOrderByItems());
    }
    
    @Test
    public void assertIsNotSameGroupByAndOrderByItemsWhenEmptyGroupBy() {
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.setProjections(new ProjectionsSegment(0, 0));
        SelectStatementContext selectStatementContext = new SelectStatementContext(null, Collections.emptyList(), selectStatement);
        assertFalse(selectStatementContext.isSameGroupByAndOrderByItems());
    }
    
    @Test
    public void assertIsNotSameGroupByAndOrderByItemsWhenDifferentGroupByAndOrderBy() {
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.setProjections(new ProjectionsSegment(0, 0));
        selectStatement.setGroupBy(new GroupBySegment(0, 0, Collections.singletonList(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.ASC, OrderDirection.DESC))));
        selectStatement.setOrderBy(new OrderBySegment(0, 0, Collections.singletonList(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.DESC, OrderDirection.DESC))));
        SelectStatementContext selectStatementContext = new SelectStatementContext(null, Collections.emptyList(), selectStatement);
        assertFalse(selectStatementContext.isSameGroupByAndOrderByItems());
    }
    
    @Test
    public void assertSetIndexWhenAggregationProjectionsPresent() {
        AggregationProjection aggregationProjection = new AggregationProjection(AggregationType.MAX, "", "id");
        aggregationProjection.getDerivedAggregationProjections().addAll(Collections.singletonList(aggregationProjection));
        ProjectionsContext projectionsContext = new ProjectionsContext(0, 0, false, Collections.singletonList(aggregationProjection));
        
        SelectStatementContext selectStatementContext = new SelectStatementContext(
                new SelectStatement(), new GroupByContext(Collections.emptyList(), 0), createOrderBy(COLUMN_ORDER_BY_WITHOUT_OWNER_ALIAS), projectionsContext, null);
        Map<String, Integer> columnLabelIndexMap = new HashMap<>();
        columnLabelIndexMap.put("id", 3);
        selectStatementContext.setIndexes(columnLabelIndexMap);
        assertThat(selectStatementContext.getOrderByContext().getItems().iterator().next().getIndex(), is(3));
    }
    
    private OrderByContext createOrderBy(final String type) {
        OrderByItemSegment orderByItemSegment = createOrderByItemSegment(type);
        OrderByItem orderByItem = new OrderByItem(orderByItemSegment);
        return new OrderByContext(Lists.newArrayList(orderByItem), true);
    }
    
    private OrderByItemSegment createOrderByItemSegment(final String type) {
        switch (type) {
            case INDEX_ORDER_BY:
                return new IndexOrderByItemSegment(0, 0, 4, OrderDirection.ASC, OrderDirection.ASC);
            case COLUMN_ORDER_BY_WITH_OWNER:
                ColumnSegment columnSegment = new ColumnSegment(0, 0, new IdentifierValue("name"));
                columnSegment.setOwner(new OwnerSegment(0, 0, new IdentifierValue("table")));
                return new ColumnOrderByItemSegment(columnSegment, OrderDirection.ASC, OrderDirection.ASC);
            case COLUMN_ORDER_BY_WITH_ALIAS:
                return new ColumnOrderByItemSegment(new ColumnSegment(0, 0, new IdentifierValue("n")), OrderDirection.ASC, OrderDirection.ASC);
            default:
                return new ColumnOrderByItemSegment(new ColumnSegment(0, 0, new IdentifierValue("id")), OrderDirection.ASC, OrderDirection.ASC);
        }
    }
    
    private ProjectionsContext createProjectionsContext() {
        return new ProjectionsContext(
                0, 0, true, Arrays.asList(getColumnProjectionWithoutOwner(), getColumnProjectionWithoutOwner(true), getColumnProjectionWithoutOwner(false)));
    }
    
    private Projection getColumnProjectionWithoutOwner() {
        return new ColumnProjection("table", "name", null);
    }
    
    private Projection getColumnProjectionWithoutOwner(final boolean hasAlias) {
        return new ColumnProjection(null, hasAlias ? "name" : "id", hasAlias ? "n" : null);
    }
}
