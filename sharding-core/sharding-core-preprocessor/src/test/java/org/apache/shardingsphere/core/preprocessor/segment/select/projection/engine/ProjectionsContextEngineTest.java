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

package org.apache.shardingsphere.core.preprocessor.segment.select.projection.engine;

import org.apache.shardingsphere.core.parse.sql.segment.dml.order.item.IndexOrderByItemSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.order.item.TextOrderByItemSegment;
import org.apache.shardingsphere.core.preprocessor.segment.select.groupby.GroupByContext;
import org.apache.shardingsphere.core.preprocessor.segment.select.orderby.OrderByContext;
import org.apache.shardingsphere.core.preprocessor.segment.select.orderby.OrderByItem;
import org.apache.shardingsphere.core.preprocessor.segment.select.projection.ProjectionsContext;
import org.apache.shardingsphere.core.parse.sql.segment.dml.item.SelectItemSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.item.SelectItemsSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.item.ShorthandSelectItemSegment;
import org.apache.shardingsphere.core.parse.sql.segment.generic.SchemaSegment;
import org.apache.shardingsphere.core.parse.sql.segment.generic.TableSegment;
import org.apache.shardingsphere.core.parse.sql.statement.dml.SelectStatement;

import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ProjectionsContextEngineTest {
    
    @Test
    public void assertProjectionsContextCreatedProperly() {
        ProjectionsContextEngine projectionsContextEngine = new ProjectionsContextEngine(null);
        SelectStatement selectStatement = mock(SelectStatement.class);
        when(selectStatement.getSelectItems()).thenReturn(mock(SelectItemsSegment.class));
        ProjectionsContext projectionsContext = projectionsContextEngine.createProjectionsContext(null, selectStatement, mock(GroupByContext.class), mock(OrderByContext.class));
        assertNotNull(projectionsContext);
    }
    
    @Test
    public void assertProjectionsContextCreatedProperlyWhenSelectItemPresent() {
        SelectStatement selectStatement = mock(SelectStatement.class);
        SelectItemsSegment selectItemsSegment = mock(SelectItemsSegment.class);
        when(selectStatement.getSelectItems()).thenReturn(selectItemsSegment);
        ShorthandSelectItemSegment shorthandSelectItemSegment = new ShorthandSelectItemSegment(0, 10, "text");
        TableSegment owner = new TableSegment(0, 10, "name");
        owner.setOwner(new SchemaSegment(0, 10, "name"));
        shorthandSelectItemSegment.setOwner(owner);
        when(selectItemsSegment.getSelectItems()).thenReturn(Collections.<SelectItemSegment>singleton(shorthandSelectItemSegment));
        ProjectionsContext projectionsContext = new ProjectionsContextEngine(null).createProjectionsContext(null, selectStatement, mock(GroupByContext.class), mock(OrderByContext.class));
        assertNotNull(projectionsContext);
    }
    
    @Test
    public void createProjectionsContextWhenOrderByContextOrderItemsPresent() {
        SelectStatement selectStatement = mock(SelectStatement.class);
        SelectItemsSegment selectItemsSegment = mock(SelectItemsSegment.class);
        when(selectStatement.getSelectItems()).thenReturn(selectItemsSegment);
        ShorthandSelectItemSegment shorthandSelectItemSegment = new ShorthandSelectItemSegment(0, 10, "text");
        TableSegment owner = new TableSegment(0, 10, "name");
        owner.setOwner(new SchemaSegment(0, 10, "name"));
        shorthandSelectItemSegment.setOwner(owner);
        when(selectItemsSegment.getSelectItems()).thenReturn(Collections.<SelectItemSegment>singleton(shorthandSelectItemSegment));
        OrderByContext orderByContext = mock(OrderByContext.class);
        OrderByItem orderByItem = mock(OrderByItem.class);
        when(orderByItem.getSegment()).thenReturn(mock(IndexOrderByItemSegment.class));
        when(orderByContext.getItems()).thenReturn(Collections.singletonList(orderByItem));
        ProjectionsContext actual = new ProjectionsContextEngine(null).createProjectionsContext(null, selectStatement, mock(GroupByContext.class), orderByContext);
        assertNotNull(actual);
    }
    
    @Test
    public void assertProjectionsContextCreatedProperlyWhenOrderByItemSegmentNotInstanceOfIndexOrderByItemSegment() {
        SelectStatement selectStatement = mock(SelectStatement.class);
        SelectItemsSegment selectItemsSegment = mock(SelectItemsSegment.class);
        when(selectStatement.getSelectItems()).thenReturn(selectItemsSegment);
        ShorthandSelectItemSegment shorthandSelectItemSegment = new ShorthandSelectItemSegment(0, 10, "text");
        TableSegment owner = new TableSegment(0, 10, "name");
        owner.setOwner(new SchemaSegment(0, 10, "name"));
        shorthandSelectItemSegment.setOwner(owner);
        when(selectItemsSegment.getSelectItems()).thenReturn(Collections.<SelectItemSegment>singleton(shorthandSelectItemSegment));
        OrderByContext orderByContext = mock(OrderByContext.class);
        OrderByItem orderByItem = mock(OrderByItem.class);
        when(orderByItem.getSegment()).thenReturn(mock(TextOrderByItemSegment.class));
        when(orderByContext.getItems()).thenReturn(Collections.singletonList(orderByItem));
        ProjectionsContext projectionsContext = new ProjectionsContextEngine(null).createProjectionsContext(null, selectStatement, mock(GroupByContext.class), orderByContext);
        assertNotNull(projectionsContext);
    }
}
