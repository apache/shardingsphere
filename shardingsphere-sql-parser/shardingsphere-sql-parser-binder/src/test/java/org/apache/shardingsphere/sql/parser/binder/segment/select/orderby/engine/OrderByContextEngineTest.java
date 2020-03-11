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

package org.apache.shardingsphere.sql.parser.binder.segment.select.orderby.engine;

import org.apache.shardingsphere.sql.parser.sql.constant.OrderDirection;
import org.apache.shardingsphere.sql.parser.binder.segment.select.groupby.GroupByContext;
import org.apache.shardingsphere.sql.parser.binder.segment.select.orderby.OrderByContext;
import org.apache.shardingsphere.sql.parser.binder.segment.select.orderby.OrderByItem;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.order.OrderBySegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.order.item.ColumnOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.order.item.IndexOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.order.item.OrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.value.identifier.IdentifierValue;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class OrderByContextEngineTest {
    
    @Test
    public void assertCreateOrderByWithoutOrderBy() {
        SelectStatement selectStatement = new SelectStatement();
        OrderByItem orderByItem1 = new OrderByItem(new IndexOrderByItemSegment(0, 1, 1, OrderDirection.ASC, OrderDirection.DESC));
        OrderByItem orderByItem2 = new OrderByItem(new IndexOrderByItemSegment(1, 2, 2, OrderDirection.ASC, OrderDirection.DESC));
        Collection<OrderByItem> orderByItems = Arrays.asList(orderByItem1, orderByItem2);
        GroupByContext groupByContext = new GroupByContext(orderByItems, 0);
        OrderByContext actualOrderByContext = new OrderByContextEngine().createOrderBy(selectStatement, groupByContext);
        assertThat(actualOrderByContext.getItems(), is(orderByItems));
        assertTrue(actualOrderByContext.isGenerated());
    }
    
    @Test
    public void assertCreateOrderByWithOrderBy() {
        SelectStatement selectStatement = new SelectStatement();
        OrderByItemSegment columnOrderByItemSegment = new ColumnOrderByItemSegment(new ColumnSegment(0, 1, new IdentifierValue("column1")), OrderDirection.ASC);
        OrderByItemSegment indexOrderByItemSegment1 = new IndexOrderByItemSegment(1, 2, 2, OrderDirection.ASC, OrderDirection.DESC);
        OrderByItemSegment indexOrderByItemSegment2 = new IndexOrderByItemSegment(2, 3, 3, OrderDirection.ASC, OrderDirection.DESC);
        OrderBySegment orderBySegment = new OrderBySegment(0, 1, Arrays.asList(columnOrderByItemSegment, indexOrderByItemSegment1, indexOrderByItemSegment2));
        selectStatement.setOrderBy(orderBySegment);
        GroupByContext emptyGroupByContext = new GroupByContext(Collections.emptyList(), 0);
        OrderByContext actualOrderByContext = new OrderByContextEngine().createOrderBy(selectStatement, emptyGroupByContext);
        OrderByItem expectedOrderByItem1 = new OrderByItem(columnOrderByItemSegment);
        OrderByItem expectedOrderByItem2 = new OrderByItem(indexOrderByItemSegment1);
        expectedOrderByItem2.setIndex(2);
        OrderByItem expectedOrderByItem3 = new OrderByItem(indexOrderByItemSegment2);
        expectedOrderByItem3.setIndex(3);
        assertThat(actualOrderByContext.getItems(), is(Arrays.asList(expectedOrderByItem1, expectedOrderByItem2, expectedOrderByItem3)));
        assertFalse(actualOrderByContext.isGenerated());
    }

    @Test
    public void assertCreateOrderInDistinctByWithoutOrderBy() {
        SelectStatement selectStatement = new SelectStatement();
        ColumnProjectionSegment columnProjectionSegment1 = new ColumnProjectionSegment(new ColumnSegment(0, 1, new IdentifierValue("column1")));
        ColumnProjectionSegment columnProjectionSegment2 = new ColumnProjectionSegment(new ColumnSegment(1, 2, new IdentifierValue("column2")));
        List<ProjectionSegment> list = Arrays.asList(columnProjectionSegment1, columnProjectionSegment2);
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 1);
        projectionsSegment.setDistinctRow(true);
        projectionsSegment.getProjections().addAll(list);
        selectStatement.setProjections(projectionsSegment);
        GroupByContext groupByContext = new GroupByContext(Collections.emptyList(), 0);
        OrderByContext actualOrderByContext = new OrderByContextEngine().createOrderBy(selectStatement, groupByContext);
        assertThat(actualOrderByContext.getItems().size(), is(list.size()));
        List<OrderByItem> items = (List<OrderByItem>) actualOrderByContext.getItems();
        assertThat(((ColumnOrderByItemSegment) items.get(0).getSegment()).getColumn(), is(columnProjectionSegment1.getColumn()));
        assertThat(((ColumnOrderByItemSegment) items.get(1).getSegment()).getColumn(), is(columnProjectionSegment2.getColumn()));
        assertTrue(actualOrderByContext.isGenerated());
    }
}
